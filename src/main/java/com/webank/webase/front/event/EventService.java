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

package com.webank.webase.front.event;

import com.webank.webase.front.abi.AbiService;
import com.webank.webase.front.abi.entity.AbiInfo;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.ContractStatus;
import com.webank.webase.front.base.enums.EventTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.contract.ContractService;
import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.contract.entity.RspContractNoAbi;
import com.webank.webase.front.event.callback.ContractEventCallback;
import com.webank.webase.front.event.callback.SyncEventLogCallback;
import com.webank.webase.front.event.entity.EventTopicParam;
import com.webank.webase.front.event.entity.NewBlockEventInfo;
import com.webank.webase.front.event.entity.ContractEventInfo;
import com.webank.webase.front.event.entity.PublisherHelper;
import com.webank.webase.front.event.entity.RspContractInfo;
import com.webank.webase.front.event.entity.RspEventLog;
import com.webank.webase.front.util.FrontUtils;
import com.webank.webase.front.util.RabbitMQUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.IOException;
import java.util.ArrayList;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.fisco.bcos.web3j.tx.txdecode.LogResult;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.webank.webase.front.util.RabbitMQUtils.CONTRACT_EVENT_CALLBACK_MAP;
import static com.webank.webase.front.util.RabbitMQUtils.BLOCK_ROUTING_KEY_MAP;
import static com.webank.webase.front.util.RabbitMQUtils.ROUTING_KEY_BLOCK;
import static com.webank.webase.front.util.RabbitMQUtils.ROUTING_KEY_EVENT;

/**
 * event notify in message queue service
 * including new block event and contract event log push notify
 * @author marsli
 */
@Slf4j
@Service
public class EventService {

    @Autowired
    private Map<Integer, org.fisco.bcos.channel.client.Service> serviceMap;
    @Autowired
    private ContractEventInfoRepository contractEventInfoRepository;
    @Autowired
    private NewBlockEventInfoRepository newBlockEventInfoRepository;
    @Autowired
    private MQService mqService;
    @Autowired
    private MQPublisher mqPublisher;
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private Constants constants;
    @Autowired
    private ContractService contractService;
    @Autowired
    private AbiService abiService;
    private static final String TYPE_CONTRACT = "contract";
    private static final String TYPE_ABI_INFO = "abi";

    /**
     * register NewBlockEventCallBack
     */
    @Transactional
    public List<NewBlockEventInfo> registerNewBlockEvent(String appId, int groupId,
                                                         String exchangeName, String queueName) {
        log.info("start registerNewBlockEvent appId:{},groupId:{},exchangeName:{},queueName:{}",
                appId, groupId, exchangeName, queueName);
        // String blockRoutingKey = queueName + "_" + ROUTING_KEY_BLOCK + "_" + appId;
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 4);
        String routingKey = appId + "_" + ROUTING_KEY_BLOCK + "_" + randomStr;
        mqService.bindQueue2Exchange(exchangeName, queueName, routingKey);
        try {
            // save to db 通过db来保证不重复注册
            String infoId = addNewBlockEventInfo(EventTypes.BLOCK_NOTIFY.getValue(),
                    appId, groupId, exchangeName, queueName, routingKey);
            log.info("registerNewBlockEvent saved to db successfully");
            // record groupId, exchange, routingKey for all block notify
            BLOCK_ROUTING_KEY_MAP.put(appId, new PublisherHelper(groupId, exchangeName, routingKey));
            log.info("end registerNewBlockEvent, infoId:{}", infoId);
        } catch (FrontException frontException) {
            log.error("register newBlockEvent error:[]", frontException);
            mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
            throw frontException;
        } catch (Exception e) {
            log.error("register newBlockEvent error:{}", e);
            mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
            throw new FrontException(ConstantCode.REGISTER_FAILED_ERROR);
        }
        return newBlockEventInfoRepository.findByAppId(appId);
    }


    /**
     * 在org.fisco.bcos.channel.client.Service中注册EventLogPush不会持久化
     * 如果重启了则需要重新注册
     * register ContractEventCallback
     * @param abi single one
     * @param contractAddress single one
     */
    @Transactional
    public List<ContractEventInfo> registerContractEvent(String appId, int groupId, String exchangeName, String queueName,
                                                         String abi, String fromBlock, String toBlock,
                                                         String contractAddress, List<String> topicList) {
        log.info("start registerContractEvent appId:{},groupId:{},contractAddress:{},params:{},exchangeName:{},queueName:{}",
                appId, groupId, abi, contractAddress , exchangeName, queueName);
        // String eventRoutingKey = queueName + "_" + ROUTING_KEY_EVENT + "_" + appId;
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 4);
        String routingKey = appId + "_" + ROUTING_KEY_EVENT + "_" + randomStr;
        mqService.bindQueue2Exchange(exchangeName, queueName, routingKey);
        try {
            // save to db first
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
        } catch (FrontException frontException) {
            log.error("Register contractEvent failed: ", frontException);
            // make transactional
            mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
            throw frontException;
        } catch (Exception e) {
            log.error("Register contractEvent failed: ", e);
            // make transactional
            mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
            throw new FrontException(ConstantCode.REGISTER_FAILED_ERROR);
        }
        return contractEventInfoRepository.findByAppId(appId);
    }

    private String addNewBlockEventInfo(int eventType, String appId, int groupId,
                                      String exchangeName, String queueName, String routingKey) {
        checkNewBlockEventExist(appId, exchangeName, queueName);
        NewBlockEventInfo registerInfo = new NewBlockEventInfo();
        registerInfo.setAppId(appId);
        registerInfo.setExchangeName(exchangeName);
        registerInfo.setQueueName(queueName);
        registerInfo.setGroupId(groupId);
        registerInfo.setRoutingKey(routingKey);
        registerInfo.setEventType(eventType);
        registerInfo.setCreateTime(LocalDateTime.now());
        NewBlockEventInfo saved = newBlockEventInfoRepository.save(registerInfo);
        return saved.getId();

    }

    public void checkNewBlockEventExist(String appId, String exchangeName, String queueName) {
        NewBlockEventInfo eventRow = newBlockEventInfoRepository
                .findNewBlockEventInfo(appId, exchangeName, queueName);
        if (Objects.nonNull(eventRow)) {
            log.warn("insert NewBlockEventInfo error for already exist. ");
            throw new FrontException(ConstantCode.DATA_REPEAT_IN_DB_ERROR);
        }
    }

    private String addContractEventInfo(int eventType, String appId, int groupId,
                                 String exchangeName, String queueName, String routingKey,
                                 String abi, String fromBlock, String toBlock,
                                 String contractAddress, List<String> topicList) throws FrontException {
        checkContractEventExist(appId, exchangeName, queueName, contractAddress);
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
            log.error("insert error:{}", e);
            throw new FrontException(ConstantCode.DATA_REPEAT_IN_DB_ERROR);
        }
    }

    public void checkContractEventExist(String appId, String exchangeName,
                                        String queueName, String contractAddress) {
        ContractEventInfo eventRow = contractEventInfoRepository
                .findContractEventInfo(appId, exchangeName, queueName, contractAddress);
        if (Objects.nonNull(eventRow)) {
            log.warn("insert ContractEventInfo error for already exist. ");
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
                                                      String queueName) {
        log.debug("unregisterNewBlock appId:{},groupId:{},exchangeName:{},queueName:{}",
                appId, groupId, exchangeName, queueName);
        NewBlockEventInfo eventInfo = newBlockEventInfoRepository.findById(infoId);
        if (Objects.isNull(eventInfo)) {
            throw new FrontException(ConstantCode.DATA_NOT_EXIST_ERROR);
        }
        try {
            String routingKey = eventInfo.getRoutingKey();
            BLOCK_ROUTING_KEY_MAP.remove(appId);
            mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
        } catch (Exception e) {
            log.error("unregisterNewBlock error: ", e);
            throw new FrontException(ConstantCode.UNREGISTER_FAILED_ERROR);
        }
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
     * @return left info
     */
    public List<ContractEventInfo> unregisterContractEvent(String infoId, String appId, int groupId, String exchangeName,
                                                      String queueName) {
        log.debug("unregisterContractEvent infoId:{},appId:{},groupId:{},exchangeName:{},queueName:{}",
                infoId, appId, groupId, exchangeName, queueName);
        ContractEventInfo eventInfo = contractEventInfoRepository.findById(infoId);
        if (Objects.isNull(eventInfo)) {
            throw new FrontException(ConstantCode.DATA_NOT_EXIST_ERROR);
        }
        try {
            // set callback's id empty to stop callback pushing message
            ContractEventCallback callback = CONTRACT_EVENT_CALLBACK_MAP.get(infoId);
            if (Objects.isNull(callback)) {
                log.warn("unregister failed for it's unregistered in map");
            }
            CONTRACT_EVENT_CALLBACK_MAP.remove(infoId);
            String routingKey = eventInfo.getRoutingKey();
            mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
        } catch (Exception e) {
            log.error("unregisterNewBlock error: ", e);
            throw new FrontException(ConstantCode.UNREGISTER_FAILED_ERROR);
        }
        // remove from db
        contractEventInfoRepository.delete(infoId);
        return contractEventInfoRepository.findByAppId(appId);
    }

    /**
     * sync get history event
     * cannot filter by indexed param, only filter by event name and contractAddress
     */
    public List<LogResult> getContractEventLog(int groupId, String contractAddress, String abi,
        Integer fromBlock, Integer toBlock, EventTopicParam eventTopicParam) {
        log.info("start getContractEventLog groupId:{},contractAddress:{},fromBlock:{},toBlock:{},eventTopicParam:{}",
            groupId, contractAddress, fromBlock, toBlock, eventTopicParam);
        // check block height
        Integer blockHeight = web3ApiService.getBlockNumber(groupId).intValue();
        if (blockHeight < toBlock) {
            log.error("getContractEventLog error for request blockHeight greater than blockHeight.");
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }

        // 传入abi作decoder，解析logs
        TransactionDecoder decoder = new TransactionDecoder(abi);

        EventLogUserParams eventParam = RabbitMQUtils.initEventTopicParam(fromBlock, toBlock,
            contractAddress, eventTopicParam);
        log.info("getContractEventLog eventParam:{}", eventParam);
        final CompletableFuture<List<LogResult>> callbackFuture = new CompletableFuture<>();
        SyncEventLogCallback callBack = new SyncEventLogCallback(decoder, callbackFuture);
        org.fisco.bcos.channel.client.Service service = serviceMap.get(groupId);
        // async send register
        service.registerEventLogFilter(eventParam, callBack);

        List<LogResult> resultList;
        try {
            resultList = callbackFuture.get(constants.getEventCallbackWait(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            log.error("getContractEventLog callbackFuture error: e", e);
            throw new FrontException(ConstantCode.GET_EVENT_CALLBACK_ERROR);
        } catch (TimeoutException e) {
            log.error("getContractEventLog callbackFuture timeout: {}s, error:{}", constants.getEventCallbackWait(), e);
            throw new FrontException(ConstantCode.GET_EVENT_CALLBACK_TIMEOUT_ERROR);
        }
        return resultList;
    }

    public Object getAbiByAddressFromBoth(int groupId, String type, String contractAddress) {
        if (TYPE_CONTRACT.equals(type)) {
            return contractService.findByGroupIdAndAddress(groupId, contractAddress);
        } else if (TYPE_ABI_INFO.equals(type)) {
            return abiService.getAbiByGroupIdAndAddress(groupId, contractAddress);
        } else {
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
    }

    /**
     * get abi info from both
     * @param groupId
     * @return
     * @throws IOException
     */
    public List<RspContractInfo> getContractInfoListFromBoth(int groupId) throws IOException {
        // get contract list
        List<RspContractNoAbi> contractList = contractService.findAllContractNoAbi(groupId, ContractStatus.DEPLOYED.getValue());
        // get abi list
        List<RspContractNoAbi> abiInfoList = abiService.getListByGroupIdNoAbi(groupId);
        // add abi info and contract info in result list
        List<RspContractInfo> resultList = new ArrayList<>();
        contractList.forEach(c -> resultList.add(new RspContractInfo(TYPE_CONTRACT, c.getContractAddress(), c.getContractName())));
        abiInfoList.forEach(c -> resultList.add(new RspContractInfo(TYPE_ABI_INFO, c.getContractAddress(), c.getContractName())));
        return resultList;
    }

}
