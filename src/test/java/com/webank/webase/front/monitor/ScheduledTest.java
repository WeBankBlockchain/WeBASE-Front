/*
 * Copyright 2014-2020 the original author or authors.
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

import com.webank.webase.front.base.TestBase;
import com.webank.webase.front.monitor.entity.Monitor;
import com.webank.webase.front.web3api.Web3ApiService;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.client.protocol.response.PbftView;
import org.fisco.bcos.sdk.client.protocol.response.PendingTxSize;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public class ScheduledTest extends TestBase {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private ScheduledFuture<?> future;
    @Autowired
    private Web3ApiService web3ApiService;


    public class MyRunnable implements Runnable {
        public void run() {
            System.out.println("MyRunnable.run()，" + new Date());

            System.out.println("begin sync chain data");
            Long currentTime = System.currentTimeMillis();
            // add  more group
            Client web3j = web3ApiService.getWeb3j(1);
            Monitor monitor = new Monitor();
            BlockNumber blockHeight = web3j.getBlockNumber();
            PbftView pbftView = web3j.getPbftView();
            PendingTxSize pendingTxSize = web3j.getPendingTxSize();
            monitor.setBlockHeight(blockHeight.getBlockNumber());
            monitor.setPbftView(pbftView.getPbftView());
            monitor.setPendingTransactionCount(pendingTxSize.getPendingTxSize());
            monitor.setTimestamp(currentTime);
            monitor.setGroupId(1);
            System.out.println(monitor);
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
