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

import com.webank.webase.front.event.*;
import com.webank.webase.front.event.callback.MQEventLogPushWithDecodedCallBack;
import com.webank.webase.front.event.entity.BlockNotifyInfo;
import com.webank.webase.front.event.entity.EventLogPushInfo;
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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.webank.webase.front.util.RabbitMQUtils.*;

/**
 * get EventLogPushRegisterInfo from db
 * automatically register in service after restart
 * initialize map of block and event log push
 * @author marsli
 */
@Slf4j
@Component
public class EventRegisterTask implements ApplicationRunner {

	@Autowired
	MQService mqService;
	@Autowired
	BlockNotifyInfoRepository blockNotifyInfoRepository;
	@Autowired
	EventLogPushInfoRepository eventLogPushInfoRepository;
	@Autowired
	MQPublisher mqPublisher;
	@Autowired
	Map<Integer, Service> serviceMap;


	/**
	 * Callback used to run the bean.
	 *
	 * @param args incoming application arguments
	 * @throws Exception on error
	 */
	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("ApplicationRunner start. ");
		registerStart();
	}

	public void registerStart() {
		// after front restart, register only one time
		try{
			log.info("Register task starts.");
			for (Integer groupId: serviceMap.keySet()) {
				List<BlockNotifyInfo> blockNotifyInfoList =
						blockNotifyInfoRepository.findByGroupId(groupId);
				List<EventLogPushInfo> eventLogPushInfoList =
						eventLogPushInfoRepository.findByGroupId(groupId);
				log.debug("Register task groupId:{},blockNotifyInfoList count:{},eventLogPushInfoList count:{}",
						groupId, blockNotifyInfoList.size(), eventLogPushInfoList.size());
				// foreach register
				blockNotifyInfoList.forEach(this::registerBlockNotify);
				eventLogPushInfoList.forEach(this::registerEventLogPush);
			}
			log.info("Register task finish.");
		}catch (Exception ex) {
			log.error("Register task error: ", ex);
		}
	}


	private void registerBlockNotify(BlockNotifyInfo registerInfo) {
		String queueName = registerInfo.getQueueName();
		String appId = registerInfo.getAppId();
		String exchangeName = registerInfo.getExchangeName();
		int groupId = registerInfo.getGroupId();
		String blockRoutingKey = registerInfo.getRoutingKey();
		log.debug("registerBlockNotify task  BlockNotifyInfo:{}", registerInfo);
		mqService.bindQueue2Exchange(exchangeName,
				queueName, blockRoutingKey);
		// record groupId, exchange, routingKey for all block notify
		PublisherHelper blockPublishInfo = new PublisherHelper(groupId,
				exchangeName, blockRoutingKey);
		BLOCK_ROUTING_KEY_MAP.put(appId, blockPublishInfo);
	}

	private void registerEventLogPush(EventLogPushInfo rInfo) {
		List<String> topicList = FrontUtils.string2ListStr(rInfo.getTopicList());
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
		log.debug("registerEventLogPush task EventLogPushInfo:{}", rInfo);
		// bind queue to exchange by routing key "queueName_event"
		mqService.bindQueue2Exchange(exchangeName, queueName, eventRoutingKey);
		MQEventLogPushWithDecodedCallBack callBack =
				new MQEventLogPushWithDecodedCallBack(mqPublisher, exchangeName,
						eventRoutingKey, decoder, groupId, appId);
		org.fisco.bcos.channel.client.Service service = serviceMap.get(groupId);
		service.registerEventLogFilter(params, callBack);
	}
}
