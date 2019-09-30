package com.webank.webase.front.monitor;

import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.PbftView;
import org.fisco.bcos.web3j.protocol.core.methods.response.PendingTxSize;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

public class scheduledTest extends TestBase {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private ScheduledFuture<?> future;


    public class MyRunnable implements Runnable {
        public void run() {
            System.out.println("MyRunnable.run()，" + new Date());

            System.out.println("begin sync chain data");
            Long currentTime = System.currentTimeMillis();
            //to do  add  more group
            Monitor monitor = new Monitor();
            CompletableFuture<BlockNumber> blockHeightFuture = web3j.getBlockNumber().sendAsync();
            CompletableFuture<PbftView> pbftViewFuture = web3j.getPbftView().sendAsync();
            CompletableFuture<PendingTxSize> pendingTxSizeFuture = web3j.getPendingTxSize().sendAsync();
            try {
                monitor.setBlockHeight(blockHeightFuture.get().getBlockNumber());
                monitor.setPbftView(pbftViewFuture.get().getPbftView());
                monitor.setPendingTransactionCount(pendingTxSizeFuture.get().getPendingTxSize());
                monitor.setTimestamp(currentTime);
                monitor.setGroupId(1);
                System.out.println(monitor);
            }catch (ExecutionException | InterruptedException e){
                System.out.println("sync chain data error " + e.getMessage());
            }
            System.out.println("insert success =  " + monitor.getId());

        }
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Test
    public void startCron() {
//        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        future = threadPoolTaskScheduler.schedule(new MyRunnable(), new CronTrigger("0/5 * * * * *"));
        System.out.println("DynamicTaskController.startCron()");
    }
    @Test
    public void stopCron() {
        if (future != null) {
            future.cancel(true);
        }
        System.out.println("DynamicTaskController.stopCron()");
    }
    @Test
    public void changeCron() {
//        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        stopCron();// 先停止，在开启.
        future = threadPoolTaskScheduler.schedule(new MyRunnable(), new CronTrigger("*/10 * * * * *"));
        System.out.println("DynamicTaskController.changeCron()");
    }

}
