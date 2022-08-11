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

import static com.webank.webase.front.util.RabbitMQUtils.BLOCK_ROUTING_KEY_MAP;
import static com.webank.webase.front.util.RabbitMQUtils.CONTRACT_EVENT_CALLBACK_MAP;
import static com.webank.webase.front.util.RabbitMQUtils.ROUTING_KEY_BLOCK;
import static com.webank.webase.front.util.RabbitMQUtils.ROUTING_KEY_EVENT;

import com.webank.webase.front.abi.AbiService;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.Web3Config;
import com.webank.webase.front.base.enums.ContractStatus;
import com.webank.webase.front.base.enums.EventTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.contract.ContractService;
import com.webank.webase.front.contract.entity.RspContractNoAbi;
import com.webank.webase.front.event.callback.ContractEventCallback;
import com.webank.webase.front.event.callback.NewBlockEventCallback;
import com.webank.webase.front.event.callback.SyncEventLogCallback;
import com.webank.webase.front.event.entity.ContractEventInfo;
import com.webank.webase.front.event.entity.DecodedEventLog;
import com.webank.webase.front.event.entity.EventTopicParam;
import com.webank.webase.front.event.entity.NewBlockEventInfo;
import com.webank.webase.front.event.entity.PublisherHelper;
import com.webank.webase.front.event.entity.RspContractInfo;
import com.webank.webase.front.util.FrontUtils;
import com.webank.webase.front.util.RabbitMQUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.codec.ContractCodec;
import org.fisco.bcos.sdk.v3.eventsub.EventSubParams;
import org.fisco.bcos.sdk.v3.eventsub.EventSubscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * event notify in message queue service
 * including new block event and contract event log push notify
 * @author marsli
 */
@Slf4j
@Service
public class EventService {

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
    private Web3Config web3Config;
    @Autowired
    private BcosSDK bcosSDK;
//    @Autowired
//    private Map<String ,EventSubscribe> eventSubscribeMap;
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
    public List<NewBlockEventInfo> registerNewBlockEvent(String appId, String groupId,
                                                         String exchangeName, String queueName) {
        log.info("start registerNewBlockEvent appId:{},groupId:{},exchangeName:{},queueName:{}",
                appId, groupId, exchangeName, queueName);
        // String blockRoutingKey = queueName + "_" + ROUTING_KEY_BLOCK + "_" + appId;
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 4);
        String routingKey = appId + "_" + ROUTING_KEY_BLOCK + "_" + randomStr;
        this.checkNewBlockEventExist(appId, exchangeName, queueName);
        this.handleRegNewBlock(appId, groupId, exchangeName, queueName, routingKey);
        return newBlockEventInfoRepository.findByAppId(appId);
    }

    @Transactional
    public void handleRegNewBlock(String appId, String groupId, String exchangeName, String queueName,
        String routingKey) {
        mqService.bindQueue2Exchange(exchangeName, queueName, routingKey);
//        String registerId = null;
        try {
            NewBlockEventCallback callback = new NewBlockEventCallback(mqPublisher, groupId,
                new PublisherHelper(groupId, exchangeName, routingKey), true);
            bcosSDK.registerBlockNotifier(groupId, callback);
            // save to db 通过db来保证不重复注册
            String infoId = addNewBlockEventInfo(EventTypes.BLOCK_NOTIFY.getValue(),
                appId, groupId, exchangeName, queueName, routingKey, null);
            log.info("registerNewBlockEvent saved to db successfully");
            // record groupId, exchange, routingKey for all block notify
            BLOCK_ROUTING_KEY_MAP.put(infoId, callback);
            log.info("end registerNewBlockEvent, infoId:{}", infoId);
        } catch (Exception e) {
            log.error("register newBlockEvent error:[]", e);
            mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
//            groupManagerService.eraseBlockNotifyCallback(registerId); todo
            throw new FrontException(ConstantCode.REGISTER_FAILED_ERROR);
        }
    }


    /**
     * 在org.fisco.bcos.channel.client.Service中注册EventLogPush不会持久化
     * 如果重启了则需要重新注册
     * register ContractEventCallback
     * @param abi single one
     * @param contractAddress single one
     * @param topicList single one
     */
    @Transactional
    public List<ContractEventInfo> registerContractEvent(String appId, String groupId, String exchangeName, String queueName,
                                                         String abi, String fromBlock, String toBlock,
                                                         String contractAddress, List<String> topicList) {
        log.info("start registerContractEvent appId:{},groupId:{},abi:{},contractAddress:{},exchangeName:{},queueName:{},topicList:{}",
                appId, groupId, abi, contractAddress, exchangeName, queueName, topicList);
        // String eventRoutingKey = queueName + "_" + ROUTING_KEY_EVENT + "_" + appId;
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 4);
        String routingKey = appId + "_" + ROUTING_KEY_EVENT + "_" + randomStr;
        this.checkContractEventExist(appId, exchangeName, queueName, contractAddress);
        this.handleRegContract(appId, groupId, exchangeName, queueName, routingKey,
            abi, fromBlock, toBlock, contractAddress, topicList);
        return contractEventInfoRepository.findByAppId(appId);
    }

    @Transactional
    public void handleRegContract(String appId, String groupId, String exchangeName, String queueName, String routingKey,
        String abi, String fromBlock, String toBlock, String contractAddress, List<String> topicList) {
        mqService.bindQueue2Exchange(exchangeName, queueName, routingKey);
        // to register or unregister
        EventSubscribe eventSubscribe = this.getEventSubscribe(String.valueOf(groupId));
        String registerId = null;
        try {
            // init EventLogUserParams for register
            EventSubParams params = RabbitMQUtils.initSingleEventLogUserParams(fromBlock,
                toBlock, contractAddress, topicList, web3ApiService.getWeb3j(groupId).getCryptoSuite());
            ContractEventCallback callback = new ContractEventCallback(mqPublisher, exchangeName, routingKey, groupId, appId,
                new ContractCodec(web3ApiService.getCryptoSuite(groupId), false), abi, topicList);
            // register
            registerId = eventSubscribe.subscribeEvent(params, callback);
            // save to db first
            String infoId = addContractEventInfo(EventTypes.EVENT_LOG_PUSH.getValue(), appId, groupId,
                exchangeName, queueName, routingKey, abi, fromBlock, toBlock, contractAddress, topicList,
                registerId);
            log.info("registerContractEvent saved to db successfully");
            CONTRACT_EVENT_CALLBACK_MAP.put(registerId, callback);
            log.info("end registerContractEvent infoId:{}, registerId:{}", infoId, registerId);
        } catch (Exception e) {
            log.error("Register contractEvent failed: ", e);
            // make transactional
            mqService.unbindQueueFromExchange(exchangeName, queueName, routingKey);
            if (registerId != null) {
                eventSubscribe.unsubscribeEvent(registerId);
            }
            throw new FrontException(ConstantCode.REGISTER_FAILED_ERROR);
        }
    }

    @Transactional
    public String addNewBlockEventInfo(int eventType, String appId, String groupId,
        String exchangeName, String queueName, String routingKey, String registerId) {

        NewBlockEventInfo eventRow = newBlockEventInfoRepository
            .findNewBlockEventInfo(appId, exchangeName, queueName);
        if (Objects.nonNull(eventRow)) {
            log.warn("addNewBlockEventInfo already exist: eventRow:{}", eventRow);
            return eventRow.getId();
        }
        NewBlockEventInfo registerInfo = new NewBlockEventInfo();
        registerInfo.setAppId(appId);
        registerInfo.setExchangeName(exchangeName);
        registerInfo.setQueueName(queueName);
        registerInfo.setGroupId(groupId);
        registerInfo.setRoutingKey(routingKey);
        registerInfo.setEventType(eventType);
        registerInfo.setCreateTime(LocalDateTime.now());
        registerInfo.setRegisterId(registerId);
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

    @Transactional
    public String addContractEventInfo(int eventType, String appId, String groupId,
        String exchangeName, String queueName, String routingKey, String abi,
        String fromBlock, String toBlock, String contractAddress, List<String> topicList,
        String registerId) throws FrontException {

        ContractEventInfo eventRow = contractEventInfoRepository
            .findContractEventInfo(appId, exchangeName, queueName, contractAddress);
        if (Objects.nonNull(eventRow)) {
            log.warn("addContractEventInfo already exist: eventRow:{}", eventRow);
            return eventRow.getId();
        }
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
        registerInfo.setRegisterId(registerId);
        try{
            ContractEventInfo saved = contractEventInfoRepository.save(registerInfo);
            return saved.getId();
        } catch (Exception e) {
            log.error("insert error:[]", e);
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
    public List<NewBlockEventInfo> getNewBlockInfoList(String groupId, Pageable page) {
        return newBlockEventInfoRepository.findByGroupId(groupId, page);
    }

    /**
     * full list
     */
    public List<NewBlockEventInfo> getNewBlockInfoList(String groupId) {
        return newBlockEventInfoRepository.findByGroupId(groupId);
    }

    /**
     * findByGroupIdAndAppId
     * @param groupId
     * @param appId
     */
    public List<NewBlockEventInfo> getNewBlockInfo(String groupId, String appId) {
        return newBlockEventInfoRepository.findByGroupIdAndAppId(groupId, appId);
    }

    /**
     * remove from BLOCK_ROUTING_KEY_MAP to stop pushing message
     * @param infoId
     * @return left info
     */
    public List<NewBlockEventInfo> unregisterNewBlock(String infoId, String appId, String groupId, String exchangeName,
                                                      String queueName) {
        log.debug("unregisterNewBlock appId:{},groupId:{},exchangeName:{},queueName:{}",
                appId, groupId, exchangeName, queueName);
        NewBlockEventInfo eventInfo = newBlockEventInfoRepository.findById(infoId).orElse(null);
        if (Objects.isNull(eventInfo)) {
            throw new FrontException(ConstantCode.DATA_NOT_EXIST_ERROR);
        }
        try {
//            String registerId = eventInfo.getRegisterId(); todo 加上取消注册
//            groupManagerService.eraseBlockNotifyCallback(registerId);
//            BLOCK_ROUTING_KEY_MAP.remove(registerId);
            NewBlockEventCallback callback = BLOCK_ROUTING_KEY_MAP.get(infoId);
            if (callback != null) {
                callback.setRunning(false);
            }
            BLOCK_ROUTING_KEY_MAP.remove(infoId);
            mqService.unbindQueueFromExchange(exchangeName, queueName, eventInfo.getRoutingKey());
        } catch (Exception e) {
            log.error("unregisterNewBlock error: ", e);
            throw new FrontException(ConstantCode.UNREGISTER_FAILED_ERROR);
        }
        // remove from db
        newBlockEventInfoRepository.deleteById(infoId);
        return newBlockEventInfoRepository.findByAppId(appId);
    }

    /**
     * return list of contract event register info
     * @param page 分页与按时间降序
     * @return
     */
    public List<ContractEventInfo> getContractEventInfoList(String groupId, Pageable page) {
        return contractEventInfoRepository.findByGroupId(groupId, page);
    }

    /**
     * full list
     */
    public List<ContractEventInfo> getContractEventInfoList(String groupId) {
        return contractEventInfoRepository.findByGroupId(groupId);
    }

    public List<ContractEventInfo> getContractEventInfo(String groupId, String appId) {
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
    public List<ContractEventInfo> unregisterContractEvent(String infoId, String appId, String groupId, String exchangeName,
                                                      String queueName) {
        log.debug("unregisterContractEvent infoId:{},appId:{},groupId:{},exchangeName:{},queueName:{}",
                infoId, appId, groupId, exchangeName, queueName);
        ContractEventInfo eventInfo = contractEventInfoRepository.findById(infoId).orElse(null);
        if (Objects.isNull(eventInfo)) {
            throw new FrontException(ConstantCode.DATA_NOT_EXIST_ERROR);
        }
        EventSubscribe eventSubscribe = this.getEventSubscribe(String.valueOf(groupId));

        try {
            String registerId = eventInfo.getRegisterId();
            // set callback's id empty to stop callback pushing message
            ContractEventCallback callback = CONTRACT_EVENT_CALLBACK_MAP.get(registerId);
            if (Objects.isNull(callback)) {
                log.warn("unregister failed for it's unregistered in map");
            }
            eventSubscribe.unsubscribeEvent(registerId);
            CONTRACT_EVENT_CALLBACK_MAP.remove(registerId);
            mqService.unbindQueueFromExchange(exchangeName, queueName, eventInfo.getRoutingKey());
        } catch (Exception e) {
            log.error("unregisterNewBlock error: ", e);
            throw new FrontException(ConstantCode.UNREGISTER_FAILED_ERROR);
        }
        // remove from db
        contractEventInfoRepository.deleteById(infoId);
        return contractEventInfoRepository.findByAppId(appId);
    }

    /**
     * sync get history event
     * cannot filter by indexed param, only filter by event name and contractAddress
     */
    public List<DecodedEventLog> getContractEventLog(String groupId, String contractAddress, String abi,
        Integer fromBlock, Integer toBlock, EventTopicParam eventTopicParam) {
        log.info("start getContractEventLog groupId:{},contractAddress:{},fromBlock:{},toBlock:{},eventTopicParam:{}",
            groupId, contractAddress, fromBlock, toBlock, eventTopicParam);
        // check block height
        Integer blockHeight = web3ApiService.getBlockNumber(groupId).intValue();
        if (blockHeight < toBlock) {
            log.error("getContractEventLog error for request blockHeight greater than blockHeight.");
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }

        EventSubParams eventParam = RabbitMQUtils.initEventTopicParam(fromBlock, toBlock,
            contractAddress, eventTopicParam, web3ApiService.getCryptoSuite(groupId));
        log.info("getContractEventLog eventParam:{}", eventParam);
        // final CompletableFuture<List<EventLog>> callbackFuture = new CompletableFuture<>();
        final CompletableFuture<List<DecodedEventLog>> callbackFuture = new CompletableFuture<>();
        ContractCodec abiCodec = new ContractCodec(web3ApiService.getCryptoSuite(groupId), false);
        SyncEventLogCallback callback = new SyncEventLogCallback(abiCodec, abi,
            eventTopicParam.getEventName().split("\\(")[0], callbackFuture);
        EventSubscribe eventSubscribe = this.getEventSubscribe(String.valueOf(groupId));
        String registerId = eventSubscribe.subscribeEvent(eventParam, callback);

        List<DecodedEventLog> resultList;
        try {
            resultList = callbackFuture.get(constants.getEventCallbackWait(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            log.error("getContractEventLog callbackFuture error: e", e);
            throw new FrontException(ConstantCode.GET_EVENT_CALLBACK_ERROR);
        } catch (TimeoutException e) {
            log.error("getContractEventLog callbackFuture timeout: {}s, error:{}", constants.getEventCallbackWait(), e);
            throw new FrontException(ConstantCode.GET_EVENT_CALLBACK_TIMEOUT_ERROR);
        } finally {
            log.info("end get event log callback and unsubscribe registerId:{}", registerId);
            eventSubscribe.unsubscribeEvent(registerId);
        }
        return resultList;
    }

    public Object getAbiByAddressFromBoth(String groupId, String type, String contractAddress) {
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
    public List<RspContractInfo> getContractInfoListFromBoth(String groupId) throws IOException {
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

    public EventSubscribe getEventSubscribe(String groupId) {
        EventSubscribe eventSubscribe = bcosSDK.getEventSubscribe(groupId);
//        EventSubscribe eventSubscribe = eventSubscribeMap.get(groupId);
//        if (eventSubscribe == null) {
//            List<String> groupList = web3ApiService.getGroupList();
//            if (!groupList.contains(groupId)) {
//                log.error("getEventSubscribe group id not exist! groupId:{}", groupId);
//                throw new FrontException(ConstantCode.GROUPID_NOT_EXIST);
//            }
//            // else, groupList contains this groupId, try to build new eventSubscribe
//            try {
//                EventSubscribe eventSubscribeNew = EventSubscribe.build(groupId, web3Config.getConfigOptionFromFile());
//                log.info("getEventSubscribe eventSubscribeNew:{}", eventSubscribeNew);
//                eventSubscribeMap.put(groupId, eventSubscribeNew);
//                return eventSubscribeNew;
//            } catch (ConfigException | JniException e) {
//                log.error("build new eventSubscribe of groupId:{} failed:{}", groupId, e);
//                throw new FrontException(ConstantCode.BUILD_NEW_EVENT_SUBSCRIBE_FAILED);
//            }
//        }
        return eventSubscribe;
    }
}
