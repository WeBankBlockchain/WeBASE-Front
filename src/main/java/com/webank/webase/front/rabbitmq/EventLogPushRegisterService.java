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

package com.webank.webase.front.rabbitmq;

import com.webank.webase.front.rabbitmq.callback.event.MQEventLogPushWithDecodedCallBack;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.fisco.bcos.channel.event.filter.TopicTools;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 在org.fisco.bcos.channel.client.Service中注册EventLogPush不会持久化
 * 如果重启了则需要重新注册
 * @author marsli
 */
@Slf4j
@Service
public class EventLogPushRegisterService {

    @Autowired
    Map<Integer, org.fisco.bcos.channel.client.Service> serviceMap;

    @Autowired
    private RabbitMQPublisher rabbitMQPublisher;

    /**
     *
     * @param params
     * @param callbackExchangeName
     * @param callbackRoutingKey
     */
    public void registerEventLogPush(int groupId, String abi, EventLogUserParams params,
                                     String callbackExchangeName, String callbackRoutingKey) {
        log.debug("registerEventLogPush params:{},callbackExchangeName:{},callbackRoutingKey:{}",
            params, callbackExchangeName, callbackRoutingKey);
        // TODO 如果exchange不存在，会发往死信队列
        // 传入abi作decoder:
        TransactionDecoder decoder = new TransactionDecoder(abi);
        MQEventLogPushWithDecodedCallBack callBack =
                new MQEventLogPushWithDecodedCallBack(rabbitMQPublisher,
                        callbackExchangeName, callbackRoutingKey, decoder);
        org.fisco.bcos.channel.client.Service service = serviceMap.get(groupId);
        service.registerEventLogFilter(params, callBack);
    }


}
