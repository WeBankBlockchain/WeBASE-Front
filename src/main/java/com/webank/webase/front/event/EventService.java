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

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.EventTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.event.callback.ContractEventCallback;
import com.webank.webase.front.event.entity.NewBlockEventInfo;
import com.webank.webase.front.event.entity.ContractEventInfo;
import com.webank.webase.front.event.entity.PublisherHelper;
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
 * event notify in message queue service
 * including new block event and contract event log push notify
 * @author marsli
 */
@Slf4j
@Service
public class EventService {

    @Autowired
    Map<Integer, org.fisco.bcos.channel.client.Service> serviceMap;

    @Autowired
    ContractEventInfoRepository contractEventInfoRepository;
    @Autowired
    NewBlockEventInfoRepository newBlockEventInfoRepository;
    @Autowired
    private MQService mqService;

    @Autowired
    private MQPublisher mqPublisher;

    /**
     * register NewBlockEventCallBack
     * @param appId
     * @param groupId
     * @param exchangeName
     * @param queueName
     * @param routingKey
     * @return
     */
    public List<NewBlockEventInfo> registerNewBlockEvent(String appId, int groupId,
                                                         String exchangeName, String queueName,
                                                         String routingKey) {
        log.debug("registerNewBlockEvent appId:{},groupId:{},exchangeName:{},queueName:{}",
                appId, groupId, exchangeName, queueName);
        mqService.bindQueue2Exchange(exchangeName, queueName, routingKey);
        // save to db 通过db来保证不重复注册
        addNewBlockEventInfo(EventTypes.BLOCK_NOTIFY.getValue(),
                appId, groupId, exchangeName, queueName, routingKey);
        // record groupId, exchange, routingKey for all block notify
        BLOCK_ROUTING_KEY_MAP.put(appId, new PublisherHelper(groupId, exchangeName, routingKey));
        return newBlockEventInfoRepository.findByQueueName(queueName);
    }


    /**
     * 在org.fisco.bcos.channel.client.Service中注册EventLogPush不会持久化
     * 如果重启了则需要重新注册
     * register ContractEventCallback
     * @param groupId
     * @param abi single one
     * @param fromBlock
     * @param toBlock
     * @param contractAddress single one
     * @param topicList
     * @param exchangeName
     * @param queueName
     */
    public List<ContractEventInfo> registerContractEvent(String appId, int groupId, String exchangeName, String queueName,
                                                         String routingKey, String abi, String fromBlock, String toBlock,
                                                         String contractAddress, List<String> topicList) {
        mqService.bindQueue2Exchange(exchangeName, queueName, routingKey);
        // 传入abi作decoder:
        TransactionDecoder decoder = new TransactionDecoder(abi);
        // init EventLogUserParams for register
        EventLogUserParams params = RabbitMQUtils.initSingleEventLogUserParams(fromBlock,
                toBlock, contractAddress, topicList);
        ContractEventCallback callBack =
                new ContractEventCallback(mqPublisher,
                        exchangeName, routingKey, decoder, groupId, appId);
        log.debug("registerContractEvent appId:{},groupId:{},abi:{},params:{},exchangeName:{},queueName:{}",
                appId, groupId, abi, params, exchangeName, queueName);
        // save to db 通过db来保证不重复注册
        String infoId = addContractEventInfo(EventTypes.EVENT_LOG_PUSH.getValue(), appId, groupId,
                exchangeName, queueName, routingKey,
                abi, fromBlock, toBlock, contractAddress, topicList);
        org.fisco.bcos.channel.client.Service service = serviceMap.get(groupId);
        service.registerEventLogFilter(params, callBack);
        return contractEventInfoRepository.findByQueueName(queueName);
    }

    private String addNewBlockEventInfo(int eventType, String appId, int groupId,
                                      String exchangeName, String queueName, String routingKey) {
        NewBlockEventInfo registerInfo = new NewBlockEventInfo();
        registerInfo.setEventType(eventType);
        registerInfo.setAppId(appId);
        registerInfo.setGroupId(groupId);
        registerInfo.setExchangeName(exchangeName);
        registerInfo.setQueueName(queueName);
        registerInfo.setRoutingKey(routingKey);
        try{
            NewBlockEventInfo saved = newBlockEventInfoRepository.save(registerInfo);
            return saved.getId();
        } catch (Exception e) {
            log.error("insert error:[]", e);
            throw new FrontException(ConstantCode.DATA_REPEAT_IN_DB_ERROR);
        }
    }

    private String addContractEventInfo(int eventType, String appId, int groupId,
                                 String exchangeName, String queueName, String routingKey,
                                 String abi, String fromBlock, String toBlock,
                                 String contractAddress, List<String> topicList) {
        ContractEventInfo registerInfo = new ContractEventInfo();
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
        try{
            ContractEventInfo saved = contractEventInfoRepository.save(registerInfo);
            return saved.getId();
        } catch (Exception e) {
            log.error("insert error:[]", e);
            throw new FrontException(ConstantCode.DATA_REPEAT_IN_DB_ERROR);
        }

    }



}
