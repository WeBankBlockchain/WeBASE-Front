/*
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.front.monitor;

import com.webank.webase.front.monitor.entity.Monitor;
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

public class ScheduledTest extends TestBase {

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
