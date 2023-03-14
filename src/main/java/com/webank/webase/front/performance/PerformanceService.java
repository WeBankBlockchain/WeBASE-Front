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
package com.webank.webase.front.performance;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.performance.entity.Performance;
import com.webank.webase.front.performance.result.Data;
import com.webank.webase.front.performance.result.LineDataList;
import com.webank.webase.front.performance.result.PerformanceData;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Host monitor: monitor computer's performance such as cpu, memory, disk etc.
 */

@Slf4j
@Service
public class PerformanceService {

    @Autowired
    private PerformanceRepository performanceRepository;
    @Autowired
    private Constants constants;
    // host upload bps(bit per second)
    private static final String TXBPS = "txbps";
    // host download bps(bit per second)
    private static final String RXBPS = "rxbps";
    private static final String LOCAL_HOST_IP = "127.0.0.1";

    private static Sigar sigar = new Sigar();

    /**
     * findByTime.
     * 
     * @param startTime startTime
     * @param endTime endTime
     * @return
     */
    public List<Performance> findByTime(LocalDateTime startTime, LocalDateTime endTime) {
        return performanceRepository.findByTimeBetween(
                startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    /**
     * findContrastDataByTime.
     * 
     * @param startTime startTime
     * @param endTime endTime
     * @param contrastStartTime contrastStartTime
     * @param contrastEndTime contrastEndTime
     * @param gap gap
     * @return
     */
    public List<PerformanceData> findContrastDataByTime(LocalDateTime startTime,
            LocalDateTime endTime, LocalDateTime contrastStartTime, LocalDateTime contrastEndTime,
            int gap) {

        List<Performance> performanceList;
        if (startTime == null || endTime == null) {
            performanceList = new ArrayList<>();
        } else {
            performanceList = performanceRepository.findByTimeBetween(
                    startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        List<Performance> contrastPerformanceList = new ArrayList<>();
        if (contrastStartTime != null && contrastEndTime != null) {
            contrastPerformanceList = performanceRepository.findByTimeBetween(
                    contrastStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    contrastEndTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        return transferToPerformanceData(transferListByGap(performanceList, gap),
                transferListByGap(contrastPerformanceList, gap));
    }

    public Page<Performance> pagingQuery(Integer pageNumber, Integer pageSize,
            LocalDateTime beginDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Specification<Performance> queryParam = new Specification<Performance>() {
            @Override
            public Predicate toPredicate(Root<Performance> root, CriteriaQuery<?> criteriaQuery,
                    CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
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
        return performanceRepository.findAll(queryParam, pageable);
    }

    /**
     * less than beginDate or larger than endDate
     * order by id
     * @param pageNumber
     * @param pageSize
     * @param beginDate
     * @param endDate
     * @return
     */
    @Transactional
    public BasePageResponse pagingQueryStat(Integer pageNumber, Integer pageSize,
        LocalDateTime beginDate, LocalDateTime endDate) {
        // get larger than endDate
        Pageable pageableEnd = PageRequest.of(pageNumber - 1,
            pageSize / 2,  Sort.by(Direction.ASC, "id"));
        Specification<Performance> queryEndParam = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (endDate != null) {
                // larger than
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"),
                    endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            }
            // less than beginDate or larger than endDate
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        // get less than beginDate
        Pageable pageableBegin = PageRequest.of(pageNumber - 1,
            pageSize / 2, Sort.by(Direction.DESC, "id"));
        Specification<Performance> queryBeginParam = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (beginDate != null) {
                // less than begin
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"),
                    beginDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            }
            // less than beginDate or larger than endDate
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        // start query
        Page<Performance> pageEnd = performanceRepository.findAll(queryEndParam, pageableEnd);
        Page<Performance> pageBegin = performanceRepository.findAll(queryBeginParam, pageableBegin);
        log.debug("pagingQueryStat pageEnd count:{}, pageBegin count:{} ", pageEnd.getSize(), pageBegin.getSize());
        // concat two list
        long totalCount = pageEnd.getTotalElements() + pageBegin.getTotalElements();
        List<Performance> resultList = new ArrayList<>();
        resultList.addAll(pageEnd.getContent());
        resultList.addAll(pageBegin.getContent());
        BasePageResponse response = new BasePageResponse(ConstantCode.RET_SUCCEED);
        response.setTotalCount(totalCount);
        response.setData(resultList);
        return response;
    }

    private List<PerformanceData> transferToPerformanceData(List<Performance> performanceList,
            List<Performance> contrastPerformanceList) {
        List<Long> timestampList = new ArrayList<>();
        List<BigDecimal> memoryValueList = new ArrayList<>();
        List<BigDecimal> cpuValueList = new ArrayList<>();
        List<BigDecimal> diskValueList = new ArrayList<>();
        List<BigDecimal> rxbpsValueList = new ArrayList<>();
        List<BigDecimal> txbpsValueList = new ArrayList<>();
        for (Performance performance : performanceList) {
            cpuValueList.add(performance.getCpuUseRatio());
            memoryValueList.add(performance.getMemoryUseRatio());
            diskValueList.add(performance.getDiskUseRatio());
            timestampList.add(performance.getTimestamp());
            rxbpsValueList.add(performance.getRxbps());
            txbpsValueList.add(performance.getTxbps());
        }
        performanceList.clear();

        List<Long> contrastTimestampList = new ArrayList<>();
        List<BigDecimal> contrastMemoryValueList = new ArrayList<>();
        List<BigDecimal> contrastCpuValueList = new ArrayList<>();
        List<BigDecimal> contrastDiskValueList = new ArrayList<>();
        List<BigDecimal> contrastRxbpsValueList = new ArrayList<>();
        List<BigDecimal> contrastTxbpsValueList = new ArrayList<>();
        for (Performance performance : contrastPerformanceList) {
            contrastCpuValueList.add(performance.getCpuUseRatio());
            contrastMemoryValueList.add(performance.getMemoryUseRatio());
            contrastDiskValueList.add(performance.getDiskUseRatio());
            contrastRxbpsValueList.add(performance.getRxbps());
            contrastTxbpsValueList.add(performance.getTxbps());
            contrastTimestampList.add(performance.getTimestamp());
        }
        contrastPerformanceList.clear();
        List<PerformanceData> performanceDataList = new ArrayList<>();
        performanceDataList.add(
                new PerformanceData("cpu", new Data(new LineDataList(timestampList, cpuValueList),
                        new LineDataList(contrastTimestampList, contrastCpuValueList))));
        performanceDataList
                .add(new PerformanceData("memory", new Data(new LineDataList(null, memoryValueList),
                        new LineDataList(null, contrastMemoryValueList))));
        performanceDataList
                .add(new PerformanceData("disk", new Data(new LineDataList(null, diskValueList),
                        new LineDataList(null, contrastDiskValueList))));
        performanceDataList
                .add(new PerformanceData(TXBPS, new Data(new LineDataList(null, txbpsValueList),
                        new LineDataList(null, contrastTxbpsValueList))));
        performanceDataList
                .add(new PerformanceData(RXBPS, new Data(new LineDataList(null, rxbpsValueList),
                        new LineDataList(null, contrastRxbpsValueList))));
        return performanceDataList;
    }

    public boolean toggleSync(boolean toggle) throws Exception {
        constants.setMonitorEnabled(toggle);
        if (constants.isMonitorEnabled() == toggle) {
            log.debug("toggle sync performance status to " + toggle);
            return toggle;
        } else {
            throw new FrontException("Fail to toggle sync performance status to " + toggle);
        }
    }

    public boolean getToggleStatus() throws Exception {
        return constants.isMonitorEnabled();
    }

    /**
     * syncPerformanceInfo per 5s
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void syncPerformanceInfo() throws SigarException {
        log.debug("begin sync performance");
        if (!constants.isMonitorEnabled()) {
            return;
        }
        Performance performance = new Performance();
        performance.setMemoryUseRatio(getMemoryRatio());
        performance.setCpuUseRatio(getCpuRatio());
        performance.setDiskUseRatio(getDiskRatio());
        Long currentTime = System.currentTimeMillis();
        performance.setTimestamp(currentTime);

        try {
            Map<String, Long> map = getNetSpeed();
            performance.setTxbps(new BigDecimal(map.get(TXBPS)));
            performance.setRxbps(new BigDecimal(map.get(RXBPS)));
        } catch (Exception e) {
            log.error("get net speed failed.", e);
        }

        performanceRepository.save(performance);
        log.debug("insert success =  " + performance.getId());
    }

    /**
     * deletePerformanceInfoPerWeek at 00:00:00 per week
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void deletePerformanceInfoPerWeek() throws SigarException {
        log.debug("begin delete performance");
        if (!constants.isMonitorEnabled()) {
            return;
        }
        Long currentTime = System.currentTimeMillis();
        Long weekAgo = currentTime - 3600 * 24 * 7 * 1000;
        int i = performanceRepository.deleteTimeAgo(weekAgo);
        log.debug("delete record count = " + i);
    }


    private BigDecimal getCpuRatio() throws SigarException {
        CpuPerc cpuPerc = sigar.getCpuPerc();
        return BigDecimal.valueOf(cpuPerc.getCombined() * 100);

    }

    private BigDecimal getMemoryRatio() throws SigarException {
        ;
        Mem mem = sigar.getMem();
        // log.info("内存总量: " + mem.getTotal() / 1024L + "K av");
        return BigDecimal.valueOf(mem.getUsedPercent());
    }

    /**
     * getDiskRatio.
     * 
     * @return
     */
    public BigDecimal getDiskRatio() throws SigarException {
        double use;
        use = sigar.getFileSystemUsage(constants.getMonitorDisk()).getUsePercent();
        return BigDecimal.valueOf(use * 100); // 硬盘使用百分率%
    }

    /**
     * getNetSpeed.
     * 
     * @return
     */
    public Map<String, Long> getNetSpeed()
            throws UnknownHostException, SigarException, InterruptedException {
        Map<String, Long> map = new HashMap<String, Long>();
        String ip = getIp();

        String[] ifNames = sigar.getNetInterfaceList();
        long rxbps = 0;
        long txbps = 0;
        for (int i = 0; i < ifNames.length; i++) {
            String name = ifNames[i];
            NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
            if (ifconfig.getAddress().equals(ip)) {
                long start = System.currentTimeMillis();
                NetInterfaceStat statStart = sigar.getNetInterfaceStat(name);
                long rxBytesStart = statStart.getRxBytes();
                long txBytesStart = statStart.getTxBytes();
                Thread.sleep(1000);
                long end = System.currentTimeMillis();
                NetInterfaceStat statEnd = sigar.getNetInterfaceStat(name);
                long rxBytesEnd = statEnd.getRxBytes();
                long txBytesEnd = statEnd.getTxBytes();

                rxbps = ((rxBytesEnd - rxBytesStart) * 8 / (end - start) * 1000) / 1024 / 8;
                txbps = ((txBytesEnd - txBytesStart) * 8 / (end - start) * 1000) / 1024 / 8;
                break;
            }
        }
        map.put(RXBPS, rxbps);
        map.put(TXBPS, txbps);
        return map;
    }

    /**
     * getConfigInfo.
     * 
     * @return
     */
    public Map<String, String> getConfigInfo() throws UnknownHostException, SigarException {
        log.info("getConfigInfo.");
        Map<String, String> configMap = new HashMap<>();
        String ip = getIp();
        log.debug("local ip:    " + ip);
        configMap.put("ip", ip);
        Mem mem = sigar.getMem();
        log.debug("memory total:    " + mem.getTotal() / 1024L + "K av");
        log.debug("memory used now:    " + mem.getUsed() / 1024L + "K used");
        configMap.put("memoryTotalSize", Long.toString(mem.getTotal() / 1024L));
        configMap.put("memoryUsedSize", Long.toString(mem.getUsed() / 1024L));
        CpuPerc cpu = sigar.getCpuPerc();
        CpuInfo[] infos = sigar.getCpuInfoList();
        log.debug("CPU mhz:    " + infos[0].getMhz());
        log.debug("CPU core number:    " + infos.length);
        configMap.put("cpuSize", Integer.toString(infos[0].getMhz()));
        configMap.put("cpuAmount", Integer.toString(infos.length));
        long total;
        long use;
        FileSystem[] fslist = sigar.getFileSystemList();
        log.debug("****fs " + fslist.length);
        use = sigar.getFileSystemUsage(constants.getMonitorDisk()).getUsed();
        total = sigar.getFileSystemUsage(constants.getMonitorDisk()).getTotal();
        log.debug("diskTotalSize:    " + total);
        log.debug("diskUsedSize:    " + use);
        configMap.put("diskTotalSize", Long.toString(total));
        configMap.put("diskUsedSize", Long.toString(use));
        return configMap;
    }

    /**
     * transferListByGap.
     * 
     * @param arrayList arrayList
     * @param gap gap
     * @return
     */
    public List transferListByGap(List arrayList, int gap) {
        if (gap == 0) {
            throw new FrontException("gap cannot be 0");
        }
        List newPerformanceList = fillList(arrayList);
        List ilist = new ArrayList<>();
        int len = newPerformanceList.size();
        for (int i = 0; i < len; i = i + gap) {
            ilist.add(newPerformanceList.get(i));
        }
        return ilist;
    }

    private List<Performance> fillList(List<Performance> performanceList) {
        List<Performance> newPerformanceList = new ArrayList<>();
        for (int i = 0; i < performanceList.size() - 1; i++) {
            Long startTime = performanceList.get(i).getTimestamp();
            Long endTime = performanceList.get(i + 1).getTimestamp();
            if (endTime - startTime > 10000) {
                log.debug("****startTime" + startTime);
                log.debug("****endTime" + endTime);
                while (endTime - startTime > 5000) {
                    Performance emptyPerformance = new Performance();
                    emptyPerformance.setTimestamp(startTime + 5000);
                    newPerformanceList.add(emptyPerformance);
                    log.debug("****insert" + startTime);
                    startTime = startTime + 5000;
                }
            } else {
                newPerformanceList.add(performanceList.get(i));
            }
        }
        return newPerformanceList;
    }

    private String getIp() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            log.debug("get ip fail, return '127.0.0.1', error:{}", e.getMessage());
            return LOCAL_HOST_IP;
        }
    }
}


