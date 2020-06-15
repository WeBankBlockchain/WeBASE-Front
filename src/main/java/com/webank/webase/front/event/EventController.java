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
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.event.entity.*;
import com.webank.webase.front.util.AbiUtil;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


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

    @ApiOperation(value = "registerNewBlockEvent",
            notes = "register registerNewBlockEvent and push message to mq")
    @ApiImplicitParam(name = "ReqNewBlockEventRegister", value = "注册出块通知所需配置",
            required = true, dataType = "ReqNewBlockEventRegister")
    @PostMapping("newBlockEvent")
    public BaseResponse registerNewBlockEvent(
            @Valid @RequestBody ReqNewBlockEventRegister reqNewBlockEventRegister, BindingResult result) {
        log.debug("start registerNewBlockEvent. {}", reqNewBlockEventRegister);
        checkParamResult(result);
        String appId = reqNewBlockEventRegister.getAppId();
        if (!CommonUtils.isLetterDigit(appId)) {
            throw new FrontException(ConstantCode.PARAM_INVALID);
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
    @ApiImplicitParam(name = "ReqContractEventRegister", value = "EventLogUserParams与消息队列所需配置",
            required = true, dataType = "ReqContractEventRegister")
    @PostMapping("contractEvent")
    public BaseResponse registerContractEvent(
            @Valid @RequestBody ReqContractEventRegister reqContractEventRegister, BindingResult result){
        log.debug("start registerContractEvent. {}", reqContractEventRegister);
        checkParamResult(result);
        int groupId = reqContractEventRegister.getGroupId();
        String appId = reqContractEventRegister.getAppId();
		if (!CommonUtils.isLetterDigit(appId)) {
			throw new FrontException(ConstantCode.PARAM_INVALID);
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
            Pageable pageable = new PageRequest(pageNumber - 1, pageSize,
                    new Sort(Sort.Direction.DESC, "createTime"));
            resList = eventService.getNewBlockInfoList(groupId, pageable);
        }
        log.debug("end getNewBlockEventInfo resList count. {}", resList.size());
        return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
    }


    @ApiOperation(value = "unregisterNewBlockEvent",
            notes = "unregister NewBlockEvent")
    @ApiImplicitParam(name = "ReqNewBlockEventRegister", value = "注册出块通知所需配置与数据表的id值",
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
            Pageable pageable = new PageRequest(pageNumber - 1, pageSize,
                    new Sort(Sort.Direction.DESC, "createTime"));
            resList = eventService.getContractEventInfoList(groupId, pageable);
        }
        log.debug("end getContractEventInfo resList count. {}", resList.size());
        return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
    }

    @ApiOperation(value = "unregisterContractEvent",
            notes = "unregister contract event")
    @ApiImplicitParam(name = "ReqContractEventRegister", value = "注册出块通知所需配置与数据表的id值",
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

}
