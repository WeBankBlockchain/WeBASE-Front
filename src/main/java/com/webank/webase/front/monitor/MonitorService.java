package com.webank.webcaf.front.monitor;

import com.webank.webcaf.front.performance.Performance;
import com.webank.webcaf.front.performance.result.Data;
import com.webank.webcaf.front.performance.result.LineDataList;
import com.webank.webcaf.front.performance.result.PerformanceData;
import lombok.extern.slf4j.Slf4j;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.methods.response.EthBlockNumber;
import org.bcos.web3j.protocol.core.methods.response.EthPbftView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class MonitorService {
    @Autowired
    Web3j web3j;
    @Autowired
    MonitorRepository monitorRepository;

    public List<PerformanceData> findContrastDataByTime(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime contrastStartTime, LocalDateTime contrastEndTime, int gap) throws Exception {

        List<Monitor> monitorList;
        if (startTime == null && endTime == null) {
            // startTime= LocalDate.now().atTime(0,0,0);
            //endTime = LocalDateTime.now();
            monitorList = new ArrayList<>();
        } else {
            monitorList = monitorRepository.findByTimeBetween(startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        List<Monitor> contrastMonitorList = new ArrayList<>();
        if (contrastStartTime != null && contrastEndTime != null) {
            contrastMonitorList = monitorRepository.findByTimeBetween(contrastStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), contrastEndTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        return transferToPerformanceData(transferListByGap(monitorList, gap), transferListByGap(contrastMonitorList, gap));

    }

    private List<PerformanceData> transferToPerformanceData(List<Monitor> monitorList, List<Monitor> contrastMonitorList) {
        List<Long> timestampList = new ArrayList<>();
        List<BigDecimal> blockHeightValueList = new ArrayList<>();
        List<BigDecimal> pbftViewValueList = new ArrayList<>();
        for (Monitor monitor : monitorList) {
            blockHeightValueList.add(new BigDecimal(monitor.getBlockHeight()));
            pbftViewValueList.add(new BigDecimal(monitor.getPbftView()));
            timestampList.add(monitor.getTimestamp());
        }
        monitorList.clear();

        List<Long> contrastTimestampList = new ArrayList<>();
        List<BigDecimal> contrastBlockHeightValueList = new ArrayList<>();
        List<BigDecimal> contrastPbftViewValueList = new ArrayList<>();
        for (Monitor monitor : contrastMonitorList) {
            contrastBlockHeightValueList.add(new BigDecimal(monitor.getBlockHeight()));
            contrastPbftViewValueList.add(new BigDecimal(monitor.getPbftView()));
            contrastTimestampList.add(monitor.getTimestamp());
        }
        contrastMonitorList.clear();
        List<PerformanceData> performanceDataList = new ArrayList<>();
        performanceDataList.add(new PerformanceData("blockHeight", new Data(new LineDataList(timestampList, blockHeightValueList), new LineDataList(contrastTimestampList, contrastBlockHeightValueList))));
        performanceDataList.add(new PerformanceData("pbftView", new Data(new LineDataList(null, pbftViewValueList), new LineDataList(null, contrastPbftViewValueList))));
        return performanceDataList;
    }

    public List transferListByGap(List arrayList, int gap) throws Exception {
        if (gap == 0) {
            throw new Exception("gap cannot be 0");
        }
        List newMonitorList= fillList(arrayList);
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
            if (endTime - startTime > 6000) {
                log.info("****startTime" + startTime);
                log.info("****endTime" + endTime);
                while (endTime - startTime > 5000) {
                    Monitor emptyMonitor = new Monitor();
                    emptyMonitor.setTimestamp(startTime + 5000);
                    newMonitorList.add(emptyMonitor);
                    log.info("****insert" + startTime);
                    startTime = startTime + 5000;
                }
            } else {
                newMonitorList.add(monitorList.get(i));
            }
        }
        return newMonitorList;
    }
    @Scheduled(cron = "0/5 * * * * ?")
    public void syncMonitorInfo() throws  ExecutionException, InterruptedException {
        log.info("begin sync chain data");
        Monitor monitor = new Monitor();
       Long currentTime = System.currentTimeMillis();
       CompletableFuture<EthBlockNumber> blockHeightFuture =  web3j.ethBlockNumber().sendAsync();
       CompletableFuture<EthPbftView> pbftViewFuture = web3j.ethPbftView().sendAsync();

        monitor.setBlockHeight(blockHeightFuture.get().getBlockNumber());
        monitor.setPbftView(pbftViewFuture.get().getEthPbftView());
        monitor.setTimestamp(currentTime);
        monitorRepository.save(monitor);
        log.info("insert success =  " + monitor.getId());
    }


    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteMonitorInfoPerWeek()   {
        log.info("begin delete monitor");
        Long currentTime = System.currentTimeMillis();
        Long aWeekAgo = currentTime - 3600 * 24 * 7 * 1000;
        int i = monitorRepository.deleteTimeAgo(aWeekAgo);
        log.info("delete record count = " + i);
    }
}
