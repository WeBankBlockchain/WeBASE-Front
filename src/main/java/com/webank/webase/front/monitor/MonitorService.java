package com.webank.webase.front.monitor;

import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.performance.result.Data;
import com.webank.webase.front.performance.result.LineDataList;
import com.webank.webase.front.performance.result.PerformanceData;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.PbftView;
import org.fisco.bcos.web3j.protocol.core.methods.response.PendingTxSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class MonitorService {
    @Autowired
    Map<Integer,Web3j> web3jMap;
    @Autowired
    MonitorRepository monitorRepository;

    public List<PerformanceData> findContrastDataByTime(int groupId, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime contrastStartTime, LocalDateTime contrastEndTime, int gap)  {

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
        List<BigDecimal> pendingCountValueList = new ArrayList<>();
        for (Monitor monitor : monitorList) {
            blockHeightValueList.add(new BigDecimal(monitor.getBlockHeight()));
            pbftViewValueList.add(new BigDecimal(monitor.getPbftView()));
            pbftViewValueList.add(new BigDecimal(monitor.getPbftView()));
            pendingCountValueList.add(new BigDecimal(monitor.getPendingTransactionCount()));
            timestampList.add(monitor.getTimestamp());
        }
        monitorList.clear();

        List<Long> contrastTimestampList = new ArrayList<>();
        List<BigDecimal> contrastBlockHeightValueList = new ArrayList<>();
        List<BigDecimal> contrastPbftViewValueList = new ArrayList<>();
        List<BigDecimal> contrastPendingCountValueList = new ArrayList<>();
        for (Monitor monitor : contrastMonitorList) {
            contrastBlockHeightValueList.add(new BigDecimal(monitor.getBlockHeight()));
            contrastPbftViewValueList.add(new BigDecimal(monitor.getPbftView()));
            contrastPendingCountValueList.add(new BigDecimal(monitor.getPendingTransactionCount()));
            contrastTimestampList.add(monitor.getTimestamp());
        }
        contrastMonitorList.clear();
        List<PerformanceData> performanceDataList = new ArrayList<>();
        performanceDataList.add(new PerformanceData("blockHeight", new Data(new LineDataList(timestampList, blockHeightValueList), new LineDataList(contrastTimestampList, contrastBlockHeightValueList))));
        performanceDataList.add(new PerformanceData("pbftView", new Data(new LineDataList(null, pbftViewValueList), new LineDataList(null, contrastPbftViewValueList))));
        performanceDataList.add(new PerformanceData("pendingCount", new Data(new LineDataList(null, pendingCountValueList), new LineDataList(null, contrastPendingCountValueList))));
        return performanceDataList;
    }

    public List transferListByGap(List arrayList, int gap)  {
        if (gap == 0) {
             throw new FrontException("gap cannot be 0");
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
    public void syncMonitorInfo() throws ExecutionException, InterruptedException, IOException {
        log.info("begin sync chain data");
        Monitor monitor = new Monitor();
        Long currentTime = System.currentTimeMillis();
        //to do  add  more group
        CompletableFuture<BlockNumber> blockHeightFuture = web3jMap.get(1).getBlockNumber().sendAsync();
        CompletableFuture<PbftView> pbftViewFuture = web3jMap.get(1).getPbftView().sendAsync();
        CompletableFuture<PendingTxSize> pendingTxSizeFuture = web3jMap.get(1).getPendingTxSize().sendAsync();

        monitor.setBlockHeight(blockHeightFuture.get().getBlockNumber());
        monitor.setPbftView(pbftViewFuture.get().getPbftView());
        monitor.setPendingTransactionCount(pendingTxSizeFuture.get().getPendingTxSize());
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
