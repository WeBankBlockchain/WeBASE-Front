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

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.event.entity.ContractEventInfo;
import com.webank.webase.front.event.entity.DecodedEventLog;
import com.webank.webase.front.event.entity.EventTopicParam;
import com.webank.webase.front.event.entity.NewBlockEventInfo;
import com.webank.webase.front.event.entity.ReqContractEventRegister;
import com.webank.webase.front.event.entity.ReqEventLogList;
import com.webank.webase.front.event.entity.ReqNewBlockEventRegister;
import com.webank.webase.front.event.entity.ReqUnregister;
import com.webank.webase.front.event.entity.RspContractInfo;
import com.webank.webase.front.util.AbiUtil;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.model.EventLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 虽然mq-server限制了用户读取队列权限，但是用户可以无节制发送订阅请求
 * 待添加鉴权： 需要限制指定的appId订阅对应的queue
 * @author marsli
 */
@Api(value = "/event", tags = "event push manage controller")
@Slf4j
@RestController
@RequestMapping(value = "event")
public class EventController extends BaseController {

    @Autowired
    private EventService eventService;
    @Autowired
    private Web3ApiService web3ApiService;


    @ApiOperation(value = "registerNewBlockEvent",
            notes = "register registerNewBlockEvent and push message to mq")
    @ApiImplicitParam(name = "reqNewBlockEventRegister", value = "注册出块通知所需配置",
            required = true, dataType = "ReqNewBlockEventRegister")
    @PostMapping("newBlockEvent")
    public BaseResponse registerNewBlockEvent(
            @Valid @RequestBody ReqNewBlockEventRegister reqNewBlockEventRegister, BindingResult result) {
        log.debug("start registerNewBlockEvent. {}", reqNewBlockEventRegister);
        checkParamResult(result);
        String appId = reqNewBlockEventRegister.getAppId();
        if (!CommonUtils.isLetterDigit(appId)) {
            throw new FrontException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
        }
        int groupId = reqNewBlockEventRegister.getGroupId();
        String exchangeName = reqNewBlockEventRegister.getExchangeName();
        // username as queue name
        String queueName = reqNewBlockEventRegister.getQueueName();
        eventService.registerNewBlockEvent(appId, groupId, exchangeName, queueName);
        log.debug("end registerNewBlockEvent. ");
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }


    @ApiOperation(value = "registerContractEvent",
            notes = "register contract event callback and push message to mq")
    @ApiImplicitParam(name = "reqContractEventRegister", value = "EventLogUserParams与消息队列所需配置",
            required = true, dataType = "ReqContractEventRegister")
    @PostMapping("contractEvent")
    public BaseResponse registerContractEvent(
            @Valid @RequestBody ReqContractEventRegister reqContractEventRegister, BindingResult result){
        log.debug("start registerContractEvent. {}", reqContractEventRegister);
        checkParamResult(result);
        int groupId = reqContractEventRegister.getGroupId();
        String appId = reqContractEventRegister.getAppId();
        if (!CommonUtils.isLetterDigit(appId)) {
            throw new FrontException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
        }
        String fromBlock = reqContractEventRegister.getFromBlock();
        String toBlock = reqContractEventRegister.getToBlock();
        // 0 < fromBlock <= toBlock, latest means latest block
        if ("0".equals(fromBlock) || "0".equals(toBlock)) {
            return new BaseResponse(ConstantCode.BLOCK_RANGE_PARAM_INVALID);
        }
        if ("latest".equals(fromBlock) && !"latest".equals(toBlock)) {
            return new BaseResponse(ConstantCode.BLOCK_RANGE_PARAM_INVALID);
        }
        if (!"latest".equals(fromBlock) && !"latest".equals(toBlock) &&
                Integer.parseInt(toBlock) < Integer.parseInt(fromBlock)) {
            return new BaseResponse(ConstantCode.BLOCK_RANGE_PARAM_INVALID);
        }
        String contractAddress = reqContractEventRegister.getContractAddress();
        List<String> topicList = reqContractEventRegister.getTopicList();
        List<Object> contractAbi = reqContractEventRegister.getContractAbi();
        String abiStr = JsonUtils.toJSONString(contractAbi);
        AbiUtil.checkAbi(abiStr);
        String exchangeName = reqContractEventRegister.getExchangeName();
        // username as queue name
        String queueName = reqContractEventRegister.getQueueName();
        // register contract event log push in service
        eventService.registerContractEvent(appId, groupId,
                exchangeName, queueName, abiStr, fromBlock, toBlock,
                contractAddress, topicList);
        log.debug("end registerContractEvent. ");
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }

    @ApiOperation(value = "getNewBlockEventInfo",
            notes = "get registered NewBlockEvent info by app id")
    @ApiImplicitParam(name = "appId", value = "应用编号",
            required = true, dataType = "String")
    @GetMapping("newBlockEvent/{groupId}/{appId}")
    public BaseResponse getNewBlockEventByAppId(@PathVariable("groupId") int groupId,
                                                @PathVariable("appId") String appId) {
        log.debug("start getNewBlockEventInfo groupId:{},appId:{}", groupId, appId);
        List<NewBlockEventInfo> resList = eventService.getNewBlockInfo(groupId, appId);
        log.debug("end getNewBlockEventInfo resList count. {}", resList.size());
        return new BaseResponse(ConstantCode.RET_SUCCESS, resList);
    }

    @ApiOperation(value = "getNewBlockEventInfo",
            notes = "get registered NewBlockEvent info by page")
    @GetMapping(value = {"newBlockEvent/list/{groupId}/{pageNumber}/{pageSize}",
            "newBlockEvent/list/{groupId}"})
    public BasePageResponse getNewBlockEventInfo(@PathVariable("groupId") Integer groupId,
                                                 @PathVariable(value = "pageNumber", required = false) Integer pageNumber,
                                                 @PathVariable(value = "pageSize", required = false) Integer pageSize) {
        log.debug("start getNewBlockEventInfo. groupId:{}", groupId);
        List<NewBlockEventInfo> resList;
        if (pageNumber == null || pageSize == null) {
             resList = eventService.getNewBlockInfoList(groupId);
        } else {
            if (pageNumber < 1) {
                return new BasePageResponse(ConstantCode.PARAM_ERROR, null, 0);
            }
            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize,
                    Sort.by(Sort.Direction.DESC, "createTime"));
            resList = eventService.getNewBlockInfoList(groupId, pageable);
        }
        log.debug("end getNewBlockEventInfo resList count. {}", resList.size());
        return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
    }


    @ApiOperation(value = "unregisterNewBlockEvent",
            notes = "unregister NewBlockEvent")
    @ApiImplicitParam(name = "reqUnregister", value = "注册出块通知所需配置与数据表的id值",
            required = true, dataType = "ReqNewBlockEventRegister")
    @DeleteMapping("newBlockEvent")
    public BaseResponse unregisterNewBlockEvent(
            @Valid @RequestBody ReqUnregister reqUnregister) {
        log.debug("start unregisterNewBlockEvent reqUnregister. {}", reqUnregister);
        String infoId = reqUnregister.getId();
        String appId = reqUnregister.getAppId();
        int groupId = reqUnregister.getGroupId();
        String exchangeName = reqUnregister.getExchangeName();
        // username as queue name
        String queueName = reqUnregister.getQueueName();
        eventService.unregisterNewBlock(infoId, appId, groupId,
                exchangeName, queueName);
        log.debug("end unregisterNewBlockEvent. ");
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }

    /* 1.4.2 */

    @ApiOperation(value = "getContractEventInfo",
            notes = "get registered contract event info by app id")
    @ApiImplicitParam(name = "appId", value = "应用编号",
            required = true, dataType = "String")
    @GetMapping("contractEvent/{groupId}/{appId}")
    public BaseResponse getContractEventByAppId(@PathVariable("groupId") int groupId,
                                                @PathVariable("appId") String appId) {
        log.debug("start getContractEventInfo appId:{}", appId);
        List<ContractEventInfo> resList = eventService.getContractEventInfo(groupId, appId);
        log.debug("end getContractEventInfo resList count. {}", resList.size());
        return new BaseResponse(ConstantCode.RET_SUCCESS, resList);
    }

    @ApiOperation(value = "getContractEventInfo",
            notes = "get registered contract event info by page")
    @GetMapping(value = {"contractEvent/list/{groupId}/{pageNumber}/{pageSize}",
            "contractEvent/list/{groupId}"})
    public BasePageResponse getContractEventInfo(@PathVariable("groupId") Integer groupId,
                                                 @PathVariable(value = "pageNumber", required = false) Integer pageNumber,
                                                 @PathVariable(value = "pageSize", required = false) Integer pageSize) {
        log.debug("start getContractEventInfo.");
        List<ContractEventInfo> resList;
        if (pageNumber == null || pageSize == null) {
            resList = eventService.getContractEventInfoList(groupId);
        } else {
            if (pageNumber < 1) {
                return new BasePageResponse(ConstantCode.PARAM_ERROR, null, 0);
            }
            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize,
                    Sort.by(Sort.Direction.DESC, "createTime"));
            resList = eventService.getContractEventInfoList(groupId, pageable);
        }
        log.debug("end getContractEventInfo resList count. {}", resList.size());
        return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
    }

    @ApiOperation(value = "unregisterContractEvent",
            notes = "unregister contract event")
    @ApiImplicitParam(name = "reqUnregister", value = "注册出块通知所需配置与数据表的id值",
            required = true, dataType = "ReqContractEventRegister")
    @DeleteMapping("contractEvent")
    public BaseResponse unregisterContractEvent(
            @Valid @RequestBody ReqUnregister reqUnregister) {
        log.debug("start unregisterContractEvent reqUnregister. {}", reqUnregister);
        String infoId = reqUnregister.getId();
        String appId = reqUnregister.getAppId();
        int groupId = reqUnregister.getGroupId();
        String exchangeName = reqUnregister.getExchangeName();
        // username as queue name
        String queueName = reqUnregister.getQueueName();
        eventService.unregisterContractEvent(infoId, appId, groupId,
                exchangeName, queueName);
        log.debug("end unregisterContractEvent. ");
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }

    @ApiOperation(value = "listContractEventLogs",
        notes = "get event logs from block's tx receipts")
    @ApiImplicitParam(name = "reqEventLogList", value = "获取区块EventLog所需参数",
        required = true, dataType = "ReqEventLogList")
    @PostMapping("eventLogs/list")
    public BasePageResponse listContractEventLogs(
        @Valid @RequestBody ReqEventLogList reqEventLogList, BindingResult result){
        log.debug("start listContractEventLogs. reqEventLogList:{}", reqEventLogList);
        checkParamResult(result);
        int groupId = reqEventLogList.getGroupId();
        // v1.5.4 support block auto set as latest block number by set value as -1
        Integer fromBlock = reqEventLogList.getFromBlock();
        Integer toBlock = reqEventLogList.getToBlock();
        // 0 < fromBlock <= toBlock, latest means block number now
        if (fromBlock == 0 || toBlock == 0) {
            return new BasePageResponse(ConstantCode.BLOCK_RANGE_PARAM_INVALID);
        }
        // check block height
        Integer blockHeight = web3ApiService.getBlockNumber(groupId).intValue();
        if (blockHeight < toBlock) {
            log.error("getContractEventLog error for request blockHeight greater than blockHeight.");
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        if (fromBlock == -1) {
            fromBlock = blockHeight;
        }
        if (toBlock == -1) {
            toBlock = blockHeight;
        }
        // is to is -1, all from can be greater than to;
        if (fromBlock > toBlock) {
            return new BasePageResponse(ConstantCode.BLOCK_RANGE_PARAM_INVALID);
        }

        String contractAddress = reqEventLogList.getContractAddress();
        EventTopicParam eventTopicParam = reqEventLogList.getTopics();
        List<Object> contractAbi = reqEventLogList.getContractAbi();
        String abiStr = JsonUtils.toJSONString(contractAbi);
        AbiUtil.checkAbi(abiStr);
        // get event log from each block's tx receipts
        List<DecodedEventLog> resList = eventService.getContractEventLog(groupId, contractAddress, abiStr,
            fromBlock, toBlock, eventTopicParam);
        log.debug("end listContractEventLogs resList:{}. ", resList);
        return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
    }

    /**
     * query list of contract only contain groupId and contractAddress and contractName
     */
    @ApiOperation(value = "get list contract/abi", notes = "get list contract/abi")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "groupId", value = "groupId", required = true,
            dataType = "Integer"),
        @ApiImplicitParam(name = "type", value = "type", required = true,
            dataType = "String"),
        @ApiImplicitParam(name = "contractAddress", value = "contractAddress", required = true,
            dataType = "String")
    })
    @GetMapping("/contractInfo/{groupId}/{type}/{contractAddress}")
    public BaseResponse findByAddress( @PathVariable Integer groupId,
        @PathVariable String type, @PathVariable String contractAddress) {
        BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
        log.info("findByAddress start. groupId:{},contractAddress:{},type:{}", groupId, contractAddress, type);
        Object abiInfo = eventService.getAbiByAddressFromBoth(groupId, type, contractAddress);
        response.setData(abiInfo);
        return response;
    }

    /**
     * query list of contract only contain groupId and contractAddress and contractName
     */
    @ApiOperation(value = "get list contract/abi", notes = "get list contract/abi")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "groupId", value = "groupId", required = true,
            dataType = "Integer"),
    })
    @GetMapping("/listAddress/{groupId}")
    public BaseResponse listAbi(@PathVariable Integer groupId) throws IOException {
        BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
        log.info("listAbi start. groupId:{}", groupId);

        List<RspContractInfo> resultList = eventService.getContractInfoListFromBoth(groupId);
        response.setData(resultList);
        return response;
    }


}
