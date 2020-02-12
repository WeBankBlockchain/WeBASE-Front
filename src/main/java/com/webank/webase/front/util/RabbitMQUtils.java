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

package com.webank.webase.front.util;

import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.fisco.bcos.channel.event.filter.TopicTools;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author marsli
 */
public class RabbitMQUtils {


    /**
     * bind queue name for rabbitAdmin
     * @param rabbitAdmin
     * @param queueNames
     */
    public static void declareQueues(RabbitAdmin rabbitAdmin, List<String> queueNames){
        Map<String,List<String>> map = distinctList(queueNames);
        for (String string : map.keySet()){
            FanoutExchange fanoutExchange = new FanoutExchange(string);
            for(String string1 : map.get(string)){
                Binding binding = BindingBuilder.bind(new Queue(string1)).to(fanoutExchange);
                rabbitAdmin.declareQueue(new Queue(string1));
                rabbitAdmin.declareExchange(fanoutExchange);
                rabbitAdmin.declareBinding(binding);
            }
        }
    }

    /**
     * separate queue names
     * @param list queue1,queue2...
     * @return
     */
    public static Map<String,List<String>> distinctList(List<String> list){
        Map<String,List<String>> map = new HashMap<>();
        for (String string :list){
            if(!string.contains(".")){
                continue;
            }
            List<String> list1 = new ArrayList<>();
            String key =string.split("\\.")[0];
            if(map.keySet().contains(key)){
                map.get(key).add(string);
            }else{
                list1.add(string);
                map.put(key,list1);
            }
        }
        return map;
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
        topicList.forEach(t -> topics.add(TopicTools.stringToTopic((String)t)));
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
