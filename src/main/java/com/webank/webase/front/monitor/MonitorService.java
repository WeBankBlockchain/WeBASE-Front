/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.monitor;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.NodeConfig;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.monitor.entity.GroupSizeInfo;
import com.webank.webase.front.monitor.entity.Monitor;
import com.webank.webase.front.performance.result.Data;
import com.webank.webase.front.performance.result.LineDataList;
import com.webank.webase.front.performance.result.PerformanceData;
import com.webank.webase.front.util.CleanPathUtil;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.client.protocol.response.PbftView;
import org.fisco.bcos.sdk.client.protocol.response.PendingTxSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Node monitor service distinguished from host monitor: performance
 */

@Slf4j
@Service
public class MonitorService {
    @Autowired
    BcosSDK bcosSDK;
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    MonitorRepository monitorRepository;
    @Autowired
    Constants constants;
    @Autowired
    NodeConfig nodeConfig;

    public List<PerformanceData> findContrastDataByTime(int groupId, LocalDateTime startTime,
            LocalDateTime endTime, LocalDateTime contrastStartTime, LocalDateTime contrastEndTime,
            int gap) {

        List<Monitor> monitorList;
        if (startTime == null || endTime == null) {
            monitorList = new ArrayList<>();
        } else {
            monitorList = monitorRepository.findByTimeBetween(groupId,
                    startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        List<Monitor> contrastMonitorList = new ArrayList<>();
        if (contrastStartTime != null && contrastEndTime != null) {
            contrastMonitorList = monitorRepository.findByTimeBetween(groupId,
                    contrastStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    contrastEndTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        return transferToPerformanceData(transferListByGap(monitorList, gap),
                transferListByGap(contrastMonitorList, gap));
    }

    public Page<Monitor> pagingQuery(int groupId, Integer pageNumber, Integer pageSize,
            LocalDateTime beginDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Specification<Monitor> queryParam = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("groupId"), groupId));
            if (beginDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"),
                        beginDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"),
                        endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return monitorRepository.findAll(queryParam, pageable);
    }

    private List<PerformanceData> transferToPerformanceData(List<Monitor> monitorList,
            List<Monitor> contrastMonitorList) {
        List<Long> timestampList = new ArrayList<>();
        List<BigDecimal> blockHeightValueList = new ArrayList<>();
        List<BigDecimal> pbftViewValueList = new ArrayList<>();
        List<BigDecimal> pendingCountValueList = new ArrayList<>();
        for (Monitor monitor : monitorList) {
            blockHeightValueList.add(monitor.getBlockHeight() == null ? null
                    : new BigDecimal(monitor.getBlockHeight()));
            pbftViewValueList.add(
                    monitor.getPbftView() == null ? null : new BigDecimal(monitor.getPbftView()));
            pendingCountValueList.add(monitor.getPendingTransactionCount() == null ? null
                    : new BigDecimal(monitor.getPendingTransactionCount()));
            timestampList.add(monitor.getTimestamp());
        }
        monitorList.clear();

        List<Long> contrastTimestampList = new ArrayList<>();
        List<BigDecimal> contrastBlockHeightValueList = new ArrayList<>();
        List<BigDecimal> contrastPbftViewValueList = new ArrayList<>();
        List<BigDecimal> contrastPendingCountValueList = new ArrayList<>();
        for (Monitor monitor : contrastMonitorList) {
            contrastBlockHeightValueList.add(monitor.getBlockHeight() == null ? null
                    : new BigDecimal(monitor.getBlockHeight()));
            contrastPbftViewValueList.add(
                    monitor.getPbftView() == null ? null : new BigDecimal(monitor.getPbftView()));
            contrastPendingCountValueList.add(monitor.getPendingTransactionCount() == null ? null
                    : new BigDecimal(monitor.getPendingTransactionCount()));
            contrastTimestampList.add(monitor.getTimestamp());
        }
        contrastMonitorList.clear();
        List<PerformanceData> performanceDataList = new ArrayList<>();
        performanceDataList.add(new PerformanceData("blockHeight",
                new Data(new LineDataList(timestampList, blockHeightValueList),
                        new LineDataList(contrastTimestampList, contrastBlockHeightValueList))));
        performanceDataList.add(
                new PerformanceData("pbftView", new Data(new LineDataList(null, pbftViewValueList),
                        new LineDataList(null, contrastPbftViewValueList))));
        performanceDataList.add(new PerformanceData("pendingCount",
                new Data(new LineDataList(null, pendingCountValueList),
                        new LineDataList(null, contrastPendingCountValueList))));
        return performanceDataList;
    }

    public List transferListByGap(List arrayList, int gap) {
        if (gap == 0) {
            throw new FrontException("gap cannot be 0");
        }
        List newMonitorList = fillList(arrayList);
        List ilist = new ArrayList<>();
        int len = newMonitorList.size();
        for (int i = 0; i < len; i = i + gap) {
            ilist.add(newMonitorList.get(i));
        }
        return ilist;
    }

    private List<Monitor> fillList(List<Monitor> monitorList) {
        List<Monitor> newMonitorList = new ArrayList<>();
        for (int i = 0; i < monitorList.size() - 1; i++) {
            Long startTime = monitorList.get(i).getTimestamp();
            Long endTime = monitorList.get(i + 1).getTimestamp();
            if (endTime - startTime > 10000) {
                log.debug("****startTime" + startTime);
                log.debug("****endTime" + endTime);
                while (endTime - startTime > 5000) {
                    Monitor emptyMonitor = new Monitor();
                    emptyMonitor.setTimestamp(startTime + 5000);
                    newMonitorList.add(emptyMonitor);
                    log.debug("****insert" + startTime);
                    startTime = startTime + 5000;
                }
            } else {
                newMonitorList.add(monitorList.get(i));
            }
        }
        return newMonitorList;
    }

    /**
     * scheduled task to sync Monitor Info per 5s
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void syncMonitorInfo() throws ExecutionException, InterruptedException {
        log.debug("begin sync chain data");
        if (!constants.isMonitorEnabled()) {
            return;
        }
        Long currentTime = System.currentTimeMillis();
        // to do add more group
        for (Integer groupId : bcosSDK.getGroupManagerService().getGroupList()) {
            Client web3j = web3ApiService.getWeb3j(groupId);
            Monitor monitor = new Monitor();
            BlockNumber blockHeight = web3j.getBlockNumber();
            PbftView pbftView = web3j.getPbftView();
            PendingTxSize pendingTxSize = web3j.getPendingTxSize();
            monitor.setBlockHeight(blockHeight.getBlockNumber());
            monitor.setPbftView(pbftView.getPbftView());
            monitor.setPendingTransactionCount(pendingTxSize.getPendingTxSize());
            monitor.setTimestamp(currentTime);
            monitor.setGroupId(groupId);
            monitorRepository.save(monitor);
            log.debug("insert success =  " + monitor.getId());
        }
    }

    /**
     * scheduled task to delete Monitor Info at 00:00:00 per week
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteMonitorInfoPerWeek() {
        log.debug("begin delete monitor");
        Long currentTime = System.currentTimeMillis();
        Long aWeekAgo = currentTime - 3600 * 24 * 7 * 1000;
        int i = monitorRepository.deleteTimeAgo(aWeekAgo);
        log.debug("delete record count = " + i);
    }

    public List<GroupSizeInfo> getGroupSizeInfos() {
        List<GroupSizeInfo> data = new ArrayList<>();
        String groupDataPath = constants.getNodePath() + File.separator + nodeConfig.getGroupDataPath();
        File f = new File(CleanPathUtil.cleanString(groupDataPath));
        File[] fs = f.listFiles();
        if (fs == null) {
            return data;
        }
        // get info
        for (File file : fs) {
            if (file.isDirectory()) {
                String name = file.getName();
                int groupId = CommonUtils.extractFigureFromStr(name);
                data.add(new GroupSizeInfo(groupId, name, file.getAbsolutePath(),
                        CommonUtils.getFolderSize(file)));
            }
        }
        // set unit: KB
        for (GroupSizeInfo groupSizeInfo : data) {
            groupSizeInfo.setSize(groupSizeInfo.getSize() / 1024L);
        }
        return data;
    }

    /**
     * less than beginDate or larger than endDate
     * order by id
     * @param groupId
     * @param pageNumber
     * @param pageSize
     * @param beginDate
     * @param endDate
     * @return BasePageResponse
     */
    @Transactional
    public BasePageResponse pagingQueryStat(int groupId, Integer pageNumber, Integer pageSize,
        LocalDateTime beginDate, LocalDateTime endDate) {
        // get larger than endDate
        Pageable pageableEnd = PageRequest.of(pageNumber - 1, pageSize / 2,
            Sort.by(Direction.ASC, "id"));
        Specification<Monitor> queryEndParam = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("groupId"), groupId));
            if (endDate != null) {
                // larger than endDate
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"),
                    endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        // get less than beginDate
        Pageable pageableBegin = PageRequest.of(pageNumber - 1, pageSize / 2,
            Sort.by(Direction.DESC, "id"));
        Specification<Monitor> queryBeginParam = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("groupId"), groupId));
            if (beginDate != null) {
                // less than begin
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"),
                    beginDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        // start query
        Page<Monitor> pageEnd = monitorRepository.findAll(queryEndParam, pageableEnd);
        Page<Monitor> pageBegin = monitorRepository.findAll(queryBeginParam, pageableBegin);
        log.debug("pagingQueryStat pageEnd count:{}, pageBegin count:{} ", pageEnd.getSize(), pageBegin.getSize());
        // concat two list
        long totalCount = pageEnd.getTotalElements() + pageBegin.getTotalElements();
        List<Monitor> resultList = new ArrayList<>();
        resultList.addAll(pageEnd.getContent());
        resultList.addAll(pageBegin.getContent());
        BasePageResponse response = new BasePageResponse(ConstantCode.RET_SUCCEED);
        response.setTotalCount(totalCount);
        response.setData(resultList);
        return response;
    }
}
