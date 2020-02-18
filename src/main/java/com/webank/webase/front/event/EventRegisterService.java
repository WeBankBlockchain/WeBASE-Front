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

package com.webank.webase.front.event;

import com.webank.webase.front.base.enums.EventTypes;
import com.webank.webase.front.event.callback.MQEventLogPushWithDecodedCallBack;
import com.webank.webase.front.event.entity.EventRegisterInfo;
import com.webank.webase.front.util.FrontUtils;
import com.webank.webase.front.util.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.webank.webase.front.util.RabbitMQUtils.BLOCK_ROUTING_KEY_MAP;

/**
 * 在org.fisco.bcos.channel.client.Service中注册EventLogPush不会持久化
 * 如果重启了则需要重新注册
 * @author marsli
 */
@Slf4j
@Service
public class EventRegisterService {

    @Autowired
    Map<Integer, org.fisco.bcos.channel.client.Service> serviceMap;

    @Autowired
    EventRegisterInfoRepository registerInfoRepository;

    @Autowired
    private RabbitMQRegisterService rabbitMQRegisterService;

    @Autowired
    private RabbitMQPublisher rabbitMQPublisher;

    public void registerBlockNotify(String appId, int groupId,
                                    String exchangeName, String queueName, String routingKey) {
        // TODO 如果exchange不存在，会发往死信队列
        log.debug("registerDecodedEventLogPush appId:{},groupId:{},exchangeNarme:{},queueName:{}",
                appId, groupId, exchangeName, queueName);
        rabbitMQRegisterService.bindQueue2Exchange(exchangeName, queueName, routingKey);
        BLOCK_ROUTING_KEY_MAP.put(appId, routingKey);
        // save to db
        addRegisterInfo(EventTypes.BLOCK_NOTIFY.getValue(),
                appId, groupId, exchangeName, queueName, routingKey,
                null, null, null, null, null);
    }

    /**
     * register DecodedEventLogPushCallback
     * @param groupId
     * @param abi single one
     * @param fromBlock
     * @param toBlock
     * @param contractAddress single one
     * @param topicList
     * @param exchangeName
     * @param queueName
     */
    public void registerDecodedEventLogPush(String appId, int groupId,
                                            String exchangeName, String queueName, String routingKey,
                                            String abi, String fromBlock,
                                            String toBlock, String contractAddress, List<String> topicList
                                            ) {
        // TODO 如果exchange不存在，会发往死信队列
        rabbitMQRegisterService.bindQueue2Exchange(exchangeName, queueName, routingKey);
        // 传入abi作decoder:
        TransactionDecoder decoder = new TransactionDecoder(abi);
        // init EventLogUserParams for register
        EventLogUserParams params = RabbitMQUtils.initSingleEventLogUserParams(fromBlock,
                toBlock, contractAddress, topicList);
        MQEventLogPushWithDecodedCallBack callBack =
                new MQEventLogPushWithDecodedCallBack(rabbitMQPublisher,
                        exchangeName, queueName, decoder, groupId, appId);
        log.debug("registerDecodedEventLogPush appId:{},groupId:{},abi:{},params:{},exchangeNarme:{},queueName:{}",
                appId, groupId, abi, params, exchangeName, queueName);
        org.fisco.bcos.channel.client.Service service = serviceMap.get(groupId);
        service.registerEventLogFilter(params, callBack);
        // save to db
        addRegisterInfo(EventTypes.EVENT_LOG_PUSH.getValue(), appId, groupId, exchangeName, queueName, routingKey,
                abi, fromBlock, toBlock, contractAddress, topicList);
    }

    private void addRegisterInfo(int eventType, String appId, int groupId,
                                 String exchangeName, String queueName, String routingKey,
                                 String abi, String fromBlock,
                                 String toBlock, String contractAddress, List<String> topicList) {
        EventRegisterInfo registerInfo = new EventRegisterInfo();
        registerInfo.setEventType(eventType);
        registerInfo.setAppId(appId);
        registerInfo.setGroupId(groupId);
        registerInfo.setContractAbi(abi);
        registerInfo.setFromBlock(fromBlock);
        registerInfo.setToBlock(toBlock);
        registerInfo.setContractAddress(contractAddress);
        // List converts to [a,b,c]
        registerInfo.setTopicList(FrontUtils.listStr2String(topicList));
        registerInfo.setExchangeName(exchangeName);
        registerInfo.setQueueName(queueName);
        registerInfo.setRoutingKey(routingKey);
        registerInfoRepository.save(registerInfo);
    }



}
