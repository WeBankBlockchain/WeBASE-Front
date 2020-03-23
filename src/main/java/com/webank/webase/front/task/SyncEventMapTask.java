/**
 * Copyright 2014-2019 the original author or authors.
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

import com.google.common.collect.Lists;
import com.webank.webase.front.event.ContractEventInfoRepository;
import com.webank.webase.front.event.NewBlockEventInfoRepository;
import com.webank.webase.front.event.callback.ContractEventCallback;
import com.webank.webase.front.event.entity.ContractEventInfo;
import com.webank.webase.front.event.entity.NewBlockEventInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.webank.webase.front.util.RabbitMQUtils.BLOCK_ROUTING_KEY_MAP;
import static com.webank.webase.front.util.RabbitMQUtils.CONTRACT_EVENT_CALLBACK_MAP;

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
		List<NewBlockEventInfo> blockInfoList = Lists.newArrayList(newBlockEventInfoRepository.findAll());
		for(String appId : BLOCK_ROUTING_KEY_MAP.keySet()){
			long equalCount = 0;
			equalCount = blockInfoList.stream()
					.filter(info -> appId.equals(info.getAppId()))
					.count();
			// remove from map that not in db's list
			if(equalCount == 0) {
				BLOCK_ROUTING_KEY_MAP.remove(appId);
				removeCount++;
			}
		}
		log.debug("end cleanNewBlockEventMap. removeCount:{}", removeCount);
	}

	private void cleanContractEventMap() {
		log.debug("start cleanContractEventMap. ");
		int removeCount = 0;
		List<ContractEventInfo> contractEventInfoList = Lists.newArrayList(contractEventInfoRepository.findAll());
		for (String infoId : CONTRACT_EVENT_CALLBACK_MAP.keySet()) {
			long equalCount = 0;
			equalCount = contractEventInfoList.stream()
					.filter(info -> infoId.equals(info.getId()))
					.count();
			// remove from map that not in db's list
			if (equalCount == 0) {
				ContractEventCallback callback = CONTRACT_EVENT_CALLBACK_MAP.get(infoId);
				callback.setRunning(false);
				CONTRACT_EVENT_CALLBACK_MAP.remove(infoId);
				removeCount++;
			}
		}
		log.debug("end cleanContractEventMap. removeCount:{}", removeCount);
	}

}
