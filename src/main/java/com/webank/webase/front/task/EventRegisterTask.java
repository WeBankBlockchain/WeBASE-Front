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

import com.webank.webase.front.base.enums.EventTypes;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.event.EventRegisterInfoRepository;
import com.webank.webase.front.event.EventRegisterService;
import com.webank.webase.front.event.RabbitMQRegisterService;
import com.webank.webase.front.event.entity.EventRegisterInfo;
import com.webank.webase.front.util.FrontUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.fisco.bcos.channel.client.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
public class EventRegisterTask {

	@Autowired
	EventRegisterService registerService;
	@Autowired
	RabbitMQRegisterService rabbitMQRegisterService;
	@Autowired
	EventRegisterInfoRepository registerInfoRepository;
	@Autowired
	Map<Integer, Service> serviceMap;

	@Scheduled(cron = "${constant.registerCron}")
	public void taskStart() {
		registerStart();
	}

	public synchronized void registerStart() {
		// after front restart, register only one time
		if(!Constants.registerTaskEnable) {
			log.info("Register task stop for already done.");
			return;
		}
		try{
			log.info("Register task starts.");
			for (Integer groupId: serviceMap.keySet()) {
				List<EventRegisterInfo> registerList =
						registerInfoRepository.findByGroupId(groupId);
				log.debug("Register task registerList count:{}", registerList.size());
				// register
				registerList.forEach(rInfo -> {
					if (rInfo.getEventType() == EventTypes.BLOCK_NOTIFY.getValue()) {
						registerBlockNotify(rInfo);
					} else if (rInfo.getEventType() == EventTypes.EVENT_LOG_PUSH.getValue()) {
						registerEventLogPush(rInfo);
					}
				});
			}
			Constants.registerTaskEnable = false;
			log.info("Register task finish.");
		}catch (Exception ex) {
			log.error("Register task error: ", ex);
		}
	}

	private void registerBlockNotify(EventRegisterInfo registerInfo) {
		String queueName = registerInfo.getQueueName();
		log.debug("registerBlockNotify task  registerInfo:{}", registerInfo);
		String blockRoutingKey = queueName +  "_" + ROUTING_KEY_BLOCK;
		rabbitMQRegisterService.bindQueue2Exchange(registerInfo.getExchangeName(),
				queueName, blockRoutingKey);
		BLOCK_ROUTING_KEY_MAP.put(registerInfo.getAppId(), blockRoutingKey);
	}

	private void registerEventLogPush(EventRegisterInfo rInfo) {
		List<String> topicList = FrontUtils.string2ListStr(rInfo.getTopicList());
		String queueName = rInfo.getQueueName();
		log.debug("registerEventLogPush task registerInfo:{}", rInfo);
		// bind queue to exchange by routing key "queueName_event"
		String eventRoutingKey = queueName +  "_" + ROUTING_KEY_EVENT;
		rabbitMQRegisterService.bindQueue2Exchange(rInfo.getExchangeName(),
				queueName, eventRoutingKey);
		registerService.registerDecodedEventLogPush(
				rInfo.getAppId(),
				rInfo.getGroupId(),
				rInfo.getExchangeName(),
				queueName,
				eventRoutingKey,
				rInfo.getContractAbi(),
				rInfo.getFromBlock(),
				rInfo.getToBlock(),
				rInfo.getContractAddress(),
				topicList
		);
	}
}
