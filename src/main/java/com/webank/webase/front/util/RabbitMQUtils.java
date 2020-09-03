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

package com.webank.webase.front.util;

import com.webank.webase.front.event.callback.ContractEventCallback;
import com.webank.webase.front.event.entity.PublisherHelper;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.fisco.bcos.channel.event.filter.TopicTools;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author marsli
 */
@Slf4j
public class RabbitMQUtils {

    public static final String ROUTING_KEY_EVENT = "event";
    public static final String ROUTING_KEY_BLOCK = "block";
    /**
     * map of (appId, "group_id,exchange_name,routing_key"), one app only needs one block notify
     */
    public static Map<String, PublisherHelper> BLOCK_ROUTING_KEY_MAP = new ConcurrentHashMap<>();

    /**
     * map of ("ContractEventInfo id", ContractEventCallback instance)
     */
    public static Map<String, ContractEventCallback> CONTRACT_EVENT_CALLBACK_MAP = new ConcurrentHashMap<>();

    /**
     * new exchange by rabbitAdmin
     * @param rabbitAdmin
     */
    public static void declareExchange(RabbitAdmin rabbitAdmin, String exchangeName){
        DirectExchange directExchange = new DirectExchange(exchangeName);
        rabbitAdmin.declareExchange(directExchange);
    }

    /**
     * new exchange and bind queue by rabbitAdmin
     * @param rabbitAdmin
     * @param exchangeName
     * @param queueName
     * @param routingKey
     */
    public static void declareExchangeAndQueue(RabbitAdmin rabbitAdmin,
                                                String exchangeName, String queueName, String routingKey){
        DirectExchange directExchange = new DirectExchange(exchangeName);
        rabbitAdmin.declareExchange(directExchange);
        rabbitAdmin.declareQueue(new Queue(queueName));
    }

    /**
     * bind queue with routing key to existed exchange
     * @param rabbitAdmin
     * @param targetExchange
     * @param queueName
     * @param routingKey
     */
    public static void bindQueue(RabbitAdmin rabbitAdmin,
                                 DirectExchange targetExchange, String queueName, String routingKey){
        rabbitAdmin.declareQueue(new Queue(queueName));
        Binding binding = BindingBuilder.bind(new Queue(queueName)).to(targetExchange).with(routingKey);
        rabbitAdmin.declareBinding(binding);
    }

    /**
     * init EventLogUserParams with single contract address
     * @param fromBlock
     * @param toBlock
     * @param contractAddress
     * @param topicList
     * @return
     */
    public static EventLogUserParams initSingleEventLogUserParams(String fromBlock, String toBlock,
                                                            String contractAddress, List<String> topicList) {
        EventLogUserParams params = new EventLogUserParams();
        params.setFromBlock(fromBlock);
        params.setToBlock(toBlock);

        // addresses，设置为Java合约对象的地址
        List<String> addresses = new ArrayList<>();
        addresses.add(contractAddress);
        params.setAddresses(addresses);
        List<Object> topics = new ArrayList<>();
        // put multiple event in topics[0]
        List<String> topicSigList = new ArrayList<>();
        topicList.forEach(t -> topicSigList.add(TopicTools.stringToTopic(t)));
        log.debug("initSingleEventLogUserParams topicSigList :{}", topicSigList);
        log.debug("initSingleEventLogUserParams topicList:{}", topicList);
        topics.add(topicSigList);
        params.setTopics(topics);

        return params;
    }

    /**
     * init EventLogUserParams with multiple addresses and topics
     * @param fromBlock
     * @param toBlock
     * @param contractAddressList
     * @param topicList
     * @return
     */
    public static EventLogUserParams initMultipleEventLogUserParams(String fromBlock, String toBlock,
                                                                  List<String> contractAddressList, List<Object> topicList) {
        EventLogUserParams params = new EventLogUserParams();
        params.setFromBlock(fromBlock);
        params.setToBlock(toBlock);

        // addresses，设置为Java合约对象的地址
        List<String> addresses = new ArrayList<>(contractAddressList);
        params.setAddresses(addresses);
        List<Object> topics = new ArrayList<>();
        topicList.forEach(t -> {
            if (t instanceof String) {
                topics.add(TopicTools.stringToTopic((String)t));
            }// instanceof others, convert by TopicTools before add
        });
        params.setTopics(topics);
        return params;
    }

}
