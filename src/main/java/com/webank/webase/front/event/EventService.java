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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.webank.webase.front.util.RabbitMQUtils.BLOCK_ROUTING_KEY_MAP;
import static com.webank.webase.front.util.RabbitMQUtils.CONTRACT_EVENT_CALLBACK_MAP;

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
     */
    public List<NewBlockEventInfo> registerNewBlockEvent(String appId, int groupId,
                                                         String exchangeName, String queueName,
                                                         String routingKey) {
        log.info("start registerNewBlockEvent appId:{},groupId:{},exchangeName:{},queueName:{}",
                appId, groupId, exchangeName, queueName);
        mqService.bindQueue2Exchange(exchangeName, queueName, routingKey);
        // save to db 通过db来保证不重复注册
        String infoId = addNewBlockEventInfo(EventTypes.BLOCK_NOTIFY.getValue(),
                appId, groupId, exchangeName, queueName, routingKey);
        log.info("registerNewBlockEvent saved to db successfully");
        // record groupId, exchange, routingKey for all block notify
        BLOCK_ROUTING_KEY_MAP.put(appId, new PublisherHelper(groupId, exchangeName, routingKey));
        log.info("end registerNewBlockEvent, infoId:{}", infoId);
        return newBlockEventInfoRepository.findByAppId(appId);
    }


    /**
     * 在org.fisco.bcos.channel.client.Service中注册EventLogPush不会持久化
     * 如果重启了则需要重新注册
     * register ContractEventCallback
     * @param abi single one
     * @param contractAddress single one
     */
    public List<ContractEventInfo> registerContractEvent(String appId, int groupId, String exchangeName, String queueName,
                                                         String routingKey, String abi, String fromBlock, String toBlock,
                                                         String contractAddress, List<String> topicList) {
        log.info("start registerContractEvent appId:{},groupId:{},contractAddress:{},params:{},exchangeName:{},queueName:{}",
                appId, groupId, abi, contractAddress , exchangeName, queueName);
        mqService.bindQueue2Exchange(exchangeName, queueName, routingKey);
        // save to db first, 通过db来保证不重复注册
        String infoId = addContractEventInfo(EventTypes.EVENT_LOG_PUSH.getValue(), appId, groupId,
                exchangeName, queueName, routingKey,
                abi, fromBlock, toBlock, contractAddress, topicList);
        log.info("registerContractEvent saved to db successfully");

        // 传入abi作decoder
        TransactionDecoder decoder = new TransactionDecoder(abi);
        // init EventLogUserParams for register
        EventLogUserParams params = RabbitMQUtils.initSingleEventLogUserParams(fromBlock,
                toBlock, contractAddress, topicList);
        ContractEventCallback callBack =
                new ContractEventCallback(mqPublisher,
                        exchangeName, routingKey, decoder, groupId, appId);
        org.fisco.bcos.channel.client.Service service = serviceMap.get(groupId);
        service.registerEventLogFilter(params, callBack);
        // mark this callback is on(true)
        callBack.setRunning(true);
        CONTRACT_EVENT_CALLBACK_MAP.put(infoId, callBack);
        log.info("end registerContractEvent infoId:{}", infoId);
        return contractEventInfoRepository.findByAppId(appId);
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
        registerInfo.setCreateTime(LocalDateTime.now());
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
        registerInfo.setCreateTime(LocalDateTime.now());
        try{
            ContractEventInfo saved = contractEventInfoRepository.save(registerInfo);
            return saved.getId();
        } catch (Exception e) {
            log.error("insert error:[]", e);
            throw new FrontException(ConstantCode.DATA_REPEAT_IN_DB_ERROR);
        }

    }

    /**
     * findByGroupId, return list of new block event register info
     * @param groupId
     * @param page 分页与按时间降序
     */
    public List<NewBlockEventInfo> getNewBlockInfoList(int groupId, Pageable page) {
        return newBlockEventInfoRepository.findByGroupId(groupId, page);
    }

    /**
     * full list
     */
    public List<NewBlockEventInfo> getNewBlockInfoList(int groupId) {
        return newBlockEventInfoRepository.findByGroupId(groupId);
    }

    /**
     * findByGroupIdAndAppId
     * @param groupId
     * @param appId
     */
    public List<NewBlockEventInfo> getNewBlockInfo(int groupId, String appId) {
        return newBlockEventInfoRepository.findByGroupIdAndAppId(groupId, appId);
    }

    /**
     * remove from BLOCK_ROUTING_KEY_MAP to stop pushing message
     * @param infoId
     * @return left info
     */
    public List<NewBlockEventInfo> unregisterNewBlock(String infoId, String appId, int groupId, String exchangeName,
                                                      String queueName, String routingKey) {
        log.debug("unregisterNewBlock appId:{},groupId:{},exchangeName:{},queueName:{},routingKey:{}",
                appId, groupId, exchangeName, queueName, routingKey);
        if (!newBlockEventInfoRepository.exists(infoId)) {
            throw new FrontException(ConstantCode.DATA_NOT_EXIST_ERROR);
        }
        BLOCK_ROUTING_KEY_MAP.remove(appId, new PublisherHelper(groupId, exchangeName, routingKey));
        mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
        // remove from db
        newBlockEventInfoRepository.delete(infoId);
        return newBlockEventInfoRepository.findByAppId(appId);
    }

    /**
     * return list of contract event register info
     * @param page 分页与按时间降序
     * @return
     */
    public List<ContractEventInfo> getContractEventInfoList(int groupId, Pageable page) {
        return contractEventInfoRepository.findByGroupId(groupId, page);
    }

    /**
     * full list
     */
    public List<ContractEventInfo> getContractEventInfoList(int groupId) {
        return contractEventInfoRepository.findByGroupId(groupId);
    }

    public List<ContractEventInfo> getContractEventInfo(int groupId, String appId) {
        return contractEventInfoRepository.findByGroupIdAndAppId(groupId, appId);
    }

    /**
     * set callback's id empty and remove from CONTRACT_EVENT_CALLBACK_MAP to stop pushing message
     * @param infoId
     * @param appId
     * @param groupId
     * @param exchangeName
     * @param queueName
     * @param routingKey
     * @return left info
     */
    public List<ContractEventInfo> unregisterContractEvent(String infoId, String appId, int groupId, String exchangeName,
                                                      String queueName, String routingKey) {
        log.debug("unregisterContractEvent infoId:{},appId:{},groupId:{},exchangeName:{},queueName:{},routingKey:{}",
                infoId, appId, groupId, exchangeName, queueName, routingKey);
        if (!contractEventInfoRepository.exists(infoId)) {
            throw new FrontException(ConstantCode.DATA_NOT_EXIST_ERROR);
        }
        // set callback's id empty to stop callback pushing message
        CONTRACT_EVENT_CALLBACK_MAP.get(infoId).setRunning(false);
        CONTRACT_EVENT_CALLBACK_MAP.remove(infoId);

        mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
        // remove from db
        contractEventInfoRepository.delete(infoId);
        return contractEventInfoRepository.findByAppId(appId);
    }
}
