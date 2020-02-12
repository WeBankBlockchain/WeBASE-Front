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

package com.webank.webase.front.rabbitmq.task;

import com.webank.webase.front.rabbitmq.EventLogPushRegisterInfoRepository;
import com.webank.webase.front.rabbitmq.EventLogPushRegisterService;
import com.webank.webase.front.rabbitmq.entity.EventLogPushRegisterInfo;
import com.webank.webase.front.util.FrontUtils;
import com.webank.webase.front.util.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.springframework.amqp.rabbit.connection.RabbitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * get EventLogPushRegisterInfo from db
 * automatically register in service
 * @author marsli
 */
@Slf4j
@Component
public class RegisterEventLogPushTask {

	@Autowired
	EventLogPushRegisterService registerService;
	@Autowired
	EventLogPushRegisterInfoRepository registerInfoRepository;
	@Autowired
	Map<Integer, Service> serviceMap;

	@Scheduled(cron = "${constant.registerCron}")
	public void taskStart() {
		registerStart();
	}

	public synchronized void registerStart() {

		try{
			for (Integer groupId: serviceMap.keySet()) {
				List<EventLogPushRegisterInfo> registerList =
						registerInfoRepository.findByGroupId(groupId);
				// register
				registerList.forEach(rInfo -> {
					List<String> topicList = FrontUtils.string2ListStr(rInfo.getTopicList());
					log.debug("register task groupId:{}, rInfo:{}", groupId, rInfo);
					registerService.registerDecodedEventLogPush(
							groupId,
							rInfo.getContractAbi(),
							rInfo.getFromBlock(),
							rInfo.getToBlock(),
							rInfo.getContractAddress(),
							topicList,
							rInfo.getExchangeName(),
							rInfo.getRoutingKey());
				});
			}
		}catch (Exception ex) {
			log.error("register task error: ", ex);
		}
	}
}
