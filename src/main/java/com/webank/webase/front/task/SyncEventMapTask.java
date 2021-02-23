/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.task;

import static com.webank.webase.front.util.RabbitMQUtils.BLOCK_ROUTING_KEY_MAP;
import static com.webank.webase.front.util.RabbitMQUtils.CONTRACT_EVENT_CALLBACK_MAP;

import com.google.common.collect.Lists;
import com.webank.webase.front.event.ContractEventInfoRepository;
import com.webank.webase.front.event.EventService;
import com.webank.webase.front.event.NewBlockEventInfoRepository;
import com.webank.webase.front.event.callback.ContractEventCallback;
import com.webank.webase.front.event.entity.ContractEventInfo;
import com.webank.webase.front.event.entity.NewBlockEventInfo;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.eventsub.EventSubscribe;
import org.fisco.bcos.sdk.service.GroupManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * sync event registered map with db's data
 * if map contains event that db not have, rm it from map;
 */
@Slf4j
@Component
public class SyncEventMapTask {

    @Autowired
    NewBlockEventInfoRepository newBlockEventInfoRepository;
    @Autowired
    ContractEventInfoRepository contractEventInfoRepository;
    @Autowired
    private BcosSDK bcosSDK;
    @Autowired
    private EventService eventService;

    @Scheduled(fixedDelayString = "${constant.syncEventMapTaskFixedDelay}")
    public void taskStart() {
        syncEventMapTask();
    }

    public synchronized void syncEventMapTask() {
        log.debug("start syncEventMapStart task");
        cleanNewBlockEventMap();
        cleanContractEventMap();
        log.debug("end syncEventMapStart task");

    }

    private void cleanNewBlockEventMap() {
        log.debug("start cleanNewBlockEventMap. ");
        int removeCount = 0;
        GroupManagerService groupManagerService = bcosSDK.getGroupManagerService();
        List<NewBlockEventInfo> blockInfoList = Lists.newArrayList(newBlockEventInfoRepository.findAll());
        for (String registerId : BLOCK_ROUTING_KEY_MAP.keySet()) {
            // check whether map contains callback that not in db
            long equalCount = blockInfoList.stream()
                    .filter(info -> registerId.equals(info.getRegisterId()))
                    .count();
            // remove from map that not in db's list
            if (equalCount == 0) {
                log.debug("remove new block callback of registerId:{}", registerId);
                groupManagerService.eraseBlockNotifyCallback(registerId);
                BLOCK_ROUTING_KEY_MAP.remove(registerId);
                removeCount++;
            }
        }
        log.debug("end cleanNewBlockEventMap. removeCount:{}", removeCount);
    }

    private void cleanContractEventMap() {
        log.debug("start cleanContractEventMap. ");
        int removeCount = 0;
        List<ContractEventInfo> contractEventInfoList = Lists.newArrayList(contractEventInfoRepository.findAll());
        for (String registerId : CONTRACT_EVENT_CALLBACK_MAP.keySet()) {
            // check whether map contains callback that not in db
            long equalCount = contractEventInfoList.stream()
                    .filter(info -> registerId.equals(info.getRegisterId()))
                    .count();
            // remove from map that not in db's list
            if (equalCount == 0) {
                log.debug("remove event callback of registerId:{}", registerId);
                ContractEventCallback callback = CONTRACT_EVENT_CALLBACK_MAP.get(registerId);
                EventSubscribe eventSubscribe = bcosSDK.getEventSubscribe(callback.getGroupId());
                eventSubscribe.unsubscribeEvent(registerId, callback);
                CONTRACT_EVENT_CALLBACK_MAP.remove(registerId);
                removeCount++;
            }
        }
        log.debug("end cleanContractEventMap. removeCount:{}", removeCount);
    }

}
