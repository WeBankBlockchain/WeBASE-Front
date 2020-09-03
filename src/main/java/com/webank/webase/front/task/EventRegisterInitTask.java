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

import com.webank.webase.front.event.*;
import com.webank.webase.front.event.callback.ContractEventCallback;
import com.webank.webase.front.event.entity.NewBlockEventInfo;
import com.webank.webase.front.event.entity.ContractEventInfo;
import com.webank.webase.front.event.entity.PublisherHelper;
import com.webank.webase.front.util.FrontUtils;
import com.webank.webase.front.util.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.webank.webase.front.util.RabbitMQUtils.*;

/**
 * initialize contract event and new block event notify
 * sync registered callback and map with db's data per 5s
 * @case1: if map contains event that db not have, rm it from map;
 * @case2: if map not contains that db have, put it in map;
 * @author marsli
 */
@Slf4j
@Component
public class EventRegisterInitTask {

	@Autowired
	MQService mqService;
	@Autowired
	NewBlockEventInfoRepository newBlockEventInfoRepository;
	@Autowired
	ContractEventInfoRepository contractEventInfoRepository;
	@Autowired
	MQPublisher mqPublisher;
	@Autowired
	Map<Integer, Service> serviceMap;


	/**
	 * Callback used to run the bean.
	 */
	@Scheduled(fixedDelayString = "${constant.eventRegisterTaskFixedDelay}")
	public void taskStart() {
		syncEventRegisterTask();
	}

	/**
	 * after front restart, re-register
	 */
	public synchronized void syncEventRegisterTask() {
		try{
			log.debug("Register task starts.");
			for (Integer groupId: serviceMap.keySet()) {
				List<NewBlockEventInfo> newBlockEventInfoList =
						newBlockEventInfoRepository.findByGroupId(groupId);
				List<ContractEventInfo> contractEventInfoList =
						contractEventInfoRepository.findByGroupId(groupId);
				log.debug("Register task groupId:{},newBlockEventInfoList count:{},contractEventInfoList count:{}",
						groupId, newBlockEventInfoList.size(), contractEventInfoList.size());
				// foreach register
				newBlockEventInfoList.stream()
						.filter(info -> !BLOCK_ROUTING_KEY_MAP.containsKey(info.getAppId()))
						.forEach(this::registerNewBlockEvent);
				contractEventInfoList.stream()
						.filter(info -> !CONTRACT_EVENT_CALLBACK_MAP.containsKey(info.getId()))
						.forEach(this::registerContractEvent);
			}
			log.debug("Register task finish.");
		}catch (Exception ex) {
			log.error("Register task error: ", ex);
		}
	}


	private void registerNewBlockEvent(NewBlockEventInfo registerInfo) {
		log.debug("start registerNewBlockEvent appId:{}", registerInfo.getAppId());
		String appId = registerInfo.getAppId();
		String exchangeName = registerInfo.getExchangeName();
		String queueName = registerInfo.getQueueName();
		int groupId = registerInfo.getGroupId();
		String blockRoutingKey = registerInfo.getRoutingKey();
		mqService.bindQueue2Exchange(exchangeName,
				queueName, blockRoutingKey);
		// record groupId, exchange, routingKey for all block notify
		PublisherHelper blockPublishInfo = new PublisherHelper(groupId,
				exchangeName, blockRoutingKey);
		BLOCK_ROUTING_KEY_MAP.put(appId, blockPublishInfo);
		log.debug("end registerNewBlockEvent successful appId:{}", appId);
	}

	private void registerContractEvent(ContractEventInfo rInfo) {
		log.debug("start registerContractEvent infoId:{}", rInfo.getId());
		List<String> topicList = FrontUtils.string2ListStr(rInfo.getTopicList());
		log.debug("registerContractEvent topicList:{}", topicList);
		String exchangeName = rInfo.getExchangeName();
		String queueName = rInfo.getQueueName();
		String appId = rInfo.getAppId();
		int groupId = rInfo.getGroupId();
		String eventRoutingKey = rInfo.getRoutingKey();
		String contractAddress = rInfo.getContractAddress();
		String abi = rInfo.getContractAbi();
		String fromBlock = rInfo.getFromBlock();
		String toBlock = rInfo.getToBlock();
		// 传入abi作decoder:
		TransactionDecoder decoder = new TransactionDecoder(abi);
		// init EventLogUserParams for register
		EventLogUserParams params = RabbitMQUtils.initSingleEventLogUserParams(
				fromBlock, toBlock, contractAddress, topicList);
		// bind queue to exchange by routing key "queueName_event"
		mqService.bindQueue2Exchange(exchangeName, queueName, eventRoutingKey);
		ContractEventCallback callBack =
				new ContractEventCallback(mqPublisher, exchangeName,
						eventRoutingKey, decoder, groupId, appId);
		org.fisco.bcos.channel.client.Service service = serviceMap.get(groupId);
		service.registerEventLogFilter(params, callBack);
		callBack.setRunning(true);
		CONTRACT_EVENT_CALLBACK_MAP.put(rInfo.getId(), callBack);
		log.debug("end registerContractEvent successful infoId:{}", rInfo.getId());
	}
}
