package com.webank.webase.front.performance;

import com.webank.webase.front.performance.relust.Data;
import com.webank.webase.front.performance.relust.LineDataList;
import com.webank.webase.front.performance.relust.PerformanceData;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Slf4j
@Service
public class PerformanceService {

    @Autowired
    private PerformanceRepository performanceRepository;


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
            int gap) throws Exception {

        List<Performance> performanceList;
        if (startTime == null && endTime == null) {
            // startTime= LocalDate.now().atTime(0,0,0);
            // endTime = LocalDateTime.now();
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
                .add(new PerformanceData("txbps", new Data(new LineDataList(null, txbpsValueList),
                        new LineDataList(null, contrastTxbpsValueList))));
        performanceDataList
                .add(new PerformanceData("rxbps", new Data(new LineDataList(null, rxbpsValueList),
                        new LineDataList(null, contrastRxbpsValueList))));
        return performanceDataList;
    }

    /**
     * syncPerformanceInfo.
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void syncPerformanceInfo() throws SigarException {
        log.info("begin sync");
        Performance performance = new Performance();
        performance.setMemoryUseRatio(getMemoryRatio());
        performance.setCpuUseRatio(getCpuRatio());
        performance.setDiskUseRatio(getDiskRatio());
        Long currentTime = System.currentTimeMillis();
        performance.setTimestamp(currentTime);

        try {
            Map<String, Long> map = getNetSpeed();
            performance.setTxbps(new BigDecimal(map.get("txbps")));
            performance.setRxbps(new BigDecimal(map.get("rxbps")));
        } catch (Exception e) {
            log.error("get net speed failed.");
        }

        performanceRepository.save(performance);
        log.info("insert success =  " + performance.getId());
    }

    /**
     * deletePerformanceInfoPerWeek.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void deletePerformanceInfoPerWeek() throws SigarException {
        log.info("begin delete");
        Long currentTime = System.currentTimeMillis();
        Long weekAgo = currentTime - 3600 * 24 * 7 * 1000;
        int i = performanceRepository.deleteTimeAgo(weekAgo);
        log.info("delete record count = " + i);
    }


    private BigDecimal getCpuRatio() throws SigarException {
        CpuPerc cpuPerc = sigar.getCpuPerc();
        return new BigDecimal(cpuPerc.getCombined() * 100);

    }

    private BigDecimal getMemoryRatio() throws SigarException {
        ;
        Mem mem = sigar.getMem();
        // System.out.println("内存总量: " + mem.getTotal() / 1024L + "K av");
        return new BigDecimal(mem.getUsedPercent());
    }

    /**
     * getDiskRatio.
     * 
     * @return
     */
    public BigDecimal getDiskRatio() throws SigarException {
        double use;
        use = sigar.getFileSystemUsage("/data").getUsePercent();
        return new BigDecimal(use * 100); // 硬盘使用百分率%
    }

    /**
     * getNetSpeed.
     * 
     * @return
     */
    public Map<String, Long> getNetSpeed()
            throws UnknownHostException, SigarException, InterruptedException {
        Map<String, Long> map = new HashMap<String, Long>();
        InetAddress addr;
        addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        Sigar sigar = new Sigar();
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
        map.put("rxbps", rxbps);
        map.put("txbps", txbps);
        return map;
    }

    /**
     * getConfigInfo.
     * 
     * @return
     */
    public Map<String, String> getConfigInfo() throws UnknownHostException, SigarException {
        Map<String, String> configMap = new HashMap<>();
        InetAddress addr;
        addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        System.out.println("本地ip地址:    " + ip);
        configMap.put("ip", ip);
        Mem mem = sigar.getMem();
        System.out.println("内存总量:    " + mem.getTotal() / 1024L + "K av");
        System.out.println("当前内存使用量:    " + mem.getUsed() / 1024L + "K used");
        configMap.put("memoryTotalSize", Long.toString(mem.getTotal() / 1024L));
        configMap.put("memoryUsedSize", Long.toString(mem.getUsed() / 1024L));
        CpuPerc cpu = sigar.getCpuPerc();
        CpuInfo[] infos = sigar.getCpuInfoList();
        System.out.println("CPU的大小:    " + infos[0].getMhz());
        System.out.println("CPU的核数:    " + infos.length);
        configMap.put("cpuSize", Integer.toString(infos[0].getMhz()));
        configMap.put("cpuAmount", Integer.toString(infos.length));
        long total;
        long use;
        FileSystem[] fslist = sigar.getFileSystemList();
        log.info("****fs " + fslist.length);
        use = sigar.getFileSystemUsage("/data").getUsed();
        total = sigar.getFileSystemUsage("/data").getTotal();
        System.out.println("文件系统总量:    " + total);
        System.out.println("文件系统已使用量:    " + use);
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
    public List transferListByGap(List arrayList, int gap) throws Exception {
        if (gap == 0) {
            throw new Exception("gap cannot be 0");
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
            if (endTime - startTime > 6000) {
                log.info("****startTime" + startTime);
                log.info("****endTime" + endTime);
                while (endTime - startTime > 5000) {
                    Performance emptyPerformance = new Performance();
                    emptyPerformance.setTimestamp(startTime + 5000);
                    newPerformanceList.add(emptyPerformance);
                    log.info("****insert" + startTime);
                    startTime = startTime + 5000;
                }
            } else {
                newPerformanceList.add(performanceList.get(i));
            }
        }
        return newPerformanceList;
    }
}


