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
import com.webank.webase.front.event.callback.NewBlockEventCallback;
import com.webank.webase.front.event.entity.EventTopicParam;
import com.webank.webase.front.event.entity.EventTopicParam.IndexedParamType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.fisco.bcos.sdk.abi.tools.TopicTools;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.eventsub.EventLogParams;
import org.fisco.bcos.sdk.utils.Numeric;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

/**
 * @author marsli
 */
public class RabbitMQUtils {

    public static final String ROUTING_KEY_EVENT = "event";
    public static final String ROUTING_KEY_BLOCK = "block";
    /**
     * map of (registerId, "group_id,exchange_name,routing_key"), one app only needs one block notify
     */
    public static Map<String, NewBlockEventCallback> BLOCK_ROUTING_KEY_MAP = new ConcurrentHashMap<>();

    /**
     * map of ("registerId", ContractEventCallback instance)
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
    public static EventLogParams initSingleEventLogUserParams(String fromBlock, String toBlock,
        String contractAddress, List<String> topicList, CryptoSuite cryptoSuite) {
        EventLogParams params = new EventLogParams();
        params.setFromBlock(fromBlock);
        params.setToBlock(toBlock);

        // addresses，设置为Java合约对象的地址
        List<String> addresses = new ArrayList<>();
        addresses.add(contractAddress);
        params.setAddresses(addresses);
        List<Object> topics = new ArrayList<>();
        // put multiple event in topics[0]
        List<String> topicSigList = new ArrayList<>();
        TopicTools tool = new TopicTools(cryptoSuite);
        topicList.forEach(t -> topicSigList.add(tool.stringToTopic(t)));
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
    public static EventLogParams initMultipleEventLogUserParams(String fromBlock, String toBlock,
        List<String> contractAddressList, List<Object> topicList, CryptoSuite cryptoSuite) {
        EventLogParams params = new EventLogParams();
        params.setFromBlock(fromBlock);
        params.setToBlock(toBlock);

        // addresses，设置为Java合约对象的地址
        List<String> addresses = new ArrayList<>(contractAddressList);
        params.setAddresses(addresses);
        List<Object> topics = new ArrayList<>();
        // put multiple event in topics[0]
        TopicTools tool = new TopicTools(cryptoSuite);
        List<String> topicSigList = new ArrayList<>();
        topicList.forEach(t -> {
            if (t instanceof String) {
                topicSigList.add(tool.stringToTopic((String)t));
            }// instanceof others, convert by TopicTools before add
        });
        topics.add(topicSigList);
        params.setTopics(topics);
        return params;
    }

    /**
     * eventTopicParam to topics
     * for sync get event log
      */
    public static EventLogParams initEventTopicParam(Integer fromBlock, Integer toBlock,
        String contractAddress, EventTopicParam eventTopicParam, CryptoSuite cryptoSuite) {
        TopicTools tool = new TopicTools(cryptoSuite);

        EventLogParams params = new EventLogParams();
        params.setFromBlock(String.valueOf(fromBlock));
        params.setToBlock(String.valueOf(toBlock));

        // addresses，设置为Java合约对象的地址
        List<String> addresses = new ArrayList<>();
        addresses.add(contractAddress);
        params.setAddresses(addresses);
        List<Object> topics = new ArrayList<>();
        // put event name in topics[0],
        topics.add(tool.stringToTopic(eventTopicParam.getEventName()));
        // if indexed param is null, add null, else add its sig value
        topics.add(Optional
                .ofNullable(eventTopicParam.getIndexed1())
                .map(i -> getIndexedParamTypeSig(i, tool))
                .orElse(null));
        topics.add(Optional
            .ofNullable(eventTopicParam.getIndexed2())
            .map(i -> getIndexedParamTypeSig(i, tool))
            .orElse(null));
        topics.add(Optional
            .ofNullable(eventTopicParam.getIndexed3())
            .map(i -> getIndexedParamTypeSig(i, tool))
            .orElse(null));
        params.setTopics(topics);

        return params;
    }

    private static String getIndexedParamTypeSig(IndexedParamType paramType, TopicTools tool) {

        String type = paramType.getType();
        String value = paramType.getValue();
        // if null, not filer
        if (value == null || "".equals(value)) {
            return null;
        }

        if (type.contains("int")) {
            return tool.integerToTopic(new BigInteger(value));
        } else if ("string".equals(type)) {
            return tool.stringToTopic(value);
        } else if ("bool".equals(type)) {
            return tool.boolToTopic(Boolean.parseBoolean(value));
        } else if ("address".equals(type)){
            return tool.addressToTopic(value);
        } else if (type.contains("bytes")) {
            if ("bytes".equals(type)) {
                return tool.bytesToTopic(Numeric.hexStringToByteArray(value));
            } else {
                // bytesN
                return tool.byteNToTopic(Numeric.hexStringToByteArray(value));
            }
        } else {
            return null;
        }

    }
}
