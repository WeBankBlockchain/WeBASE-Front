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

import com.webank.webase.front.rabbitmq.mqservice.RabbitMQPublisher;
import com.webank.webase.front.rabbitmq.callback.event.MQEventLogPushWithDecodedCallBack;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.fisco.bcos.channel.event.filter.TopicTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author marsli
 */
@Service
public class EventLogPushRegisterService {

    @Autowired
    private org.fisco.bcos.channel.client.Service service;

    @Autowired
    private RabbitMQPublisher rabbitMQPublisher;

    /**
     *
     * @param params
     * @param callbackExchangeName
     * @param callbackRoutingKey
     */
    public void registerEventLogPush(EventLogUserParams params,
                                     String callbackExchangeName, String callbackRoutingKey) {
        // TODO 如果exchange不存在，会发往死信队列
        MQEventLogPushWithDecodedCallBack callBack =
                new MQEventLogPushWithDecodedCallBack(rabbitMQPublisher,
                        callbackExchangeName, callbackRoutingKey);
        service.registerEventLogFilter(params, callBack);
    }

    /**
     * init EventLogUserParams
     * @param fromBlock
     * @param toBlock
     * @param contractAddress
     * @param topicList
     * @return
     */
    public EventLogUserParams setEventLogUserParams(String fromBlock, String toBlock,
                                     List<String> contractAddress, List<Object> topicList) {
        EventLogUserParams params = new EventLogUserParams();
        params.setFromBlock(fromBlock);
        params.setToBlock(toBlock);

        // addresses，设置为Java合约对象的地址
        List<String> addresses = new ArrayList<>(contractAddress);
        params.setAddresses(addresses);
        // ex: topics.add(TopicTools.stringToTopic("TransferEvent(int256,string,string,uint256)"));
        List<Object> topics = new ArrayList<>();
        topicList.forEach(topic -> {
            if (topic instanceof String) {
                topics.add(TopicTools.stringToTopic((String)topic));
            }
            // instanceof others, convert by TopicTools before add
        });
        params.setTopics(topics);

        return params;
    }

}
