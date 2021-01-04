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

import com.webank.webase.front.base.config.NodeConfig;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.monitor.entity.GroupSizeInfo;
import com.webank.webase.front.monitor.entity.Monitor;
import com.webank.webase.front.performance.result.Data;
import com.webank.webase.front.performance.result.LineDataList;
import com.webank.webase.front.performance.result.PerformanceData;
import com.webank.webase.front.util.CommonUtils;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.PbftView;
import org.fisco.bcos.web3j.protocol.core.methods.response.PendingTxSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Node monitor service distinguished from host monitor: performance
 */

@Slf4j
@Service
public class MonitorService {
    @Autowired
    Map<Integer, Web3j> web3jMap;
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
        Pageable pageable = new PageRequest(pageNumber - 1, pageSize);
        Specification<Monitor> queryParam = new Specification<Monitor>() {
            @Override
            public Predicate toPredicate(Root<Monitor> root, CriteriaQuery<?> criteriaQuery,
                    CriteriaBuilder criteriaBuilder) {
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
            }
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
        for (Map.Entry<Integer, Web3j> entry : web3jMap.entrySet()) {
            Monitor monitor = new Monitor();
            CompletableFuture<BlockNumber> blockHeightFuture =
                    entry.getValue().getBlockNumber().sendAsync();
            CompletableFuture<PbftView> pbftViewFuture = entry.getValue().getPbftView().sendAsync();
            CompletableFuture<PendingTxSize> pendingTxSizeFuture =
                    entry.getValue().getPendingTxSize().sendAsync();

            monitor.setBlockHeight(blockHeightFuture.get().getBlockNumber());
            monitor.setPbftView(pbftViewFuture.get().getPbftView());
            monitor.setPendingTransactionCount(pendingTxSizeFuture.get().getPendingTxSize());
            monitor.setTimestamp(currentTime);
            monitor.setGroupId(entry.getKey());
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
        File f = new File(constants.getNodePath() + File.separator + nodeConfig.getGroupDataPath());
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
}
