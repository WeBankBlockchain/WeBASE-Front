/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.task;

import com.webank.webase.front.web3api.Web3ApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SyncAvailableGroupTask {

    @Autowired
    private Web3ApiService web3ApiService;

    @Scheduled(fixedDelayString = "${constant.syncAvailableGroupTaskFixedDelay}")
    public void taskStart() {
        syncGroupListMapTask();
    }

    public synchronized void syncGroupListMapTask() {

        log.debug("start syncGroupListMapTask task");
        web3ApiService.refreshAvailableGroupMap();
        log.debug("end syncGroupListMapTask task");
    }
}
