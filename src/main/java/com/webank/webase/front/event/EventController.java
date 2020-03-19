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
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.event.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static com.webank.webase.front.util.RabbitMQUtils.*;

/**
 * 虽然mq-server限制了用户读取队列权限，但是用户可以无节制发送订阅请求
 * 待添加鉴权： 需要限制指定的appId订阅对应的queue
 * @author marsli
 */
@Api(value = "/event", tags = "event notify of contract event log push and new block event")
@Slf4j
@RestController
@RequestMapping(value = "event")
public class EventController {

    @Autowired
    private EventService eventService;

    @ApiOperation(value = "registerNewBlockEvent",
            notes = "register registerNewBlockEvent and push message to mq")
    @ApiImplicitParam(name = "ReqNewBlockEventRegister", value = "注册出块通知所需配置",
            required = true, dataType = "ReqNewBlockEventRegister")
    @PostMapping("newBlockEvent")
    public BaseResponse registerNewBlockEvent(
            @Valid @RequestBody ReqNewBlockEventRegister reqNewBlockEventRegister) {
        log.debug("start registerNewBlockEvent. {}", reqNewBlockEventRegister);
        String appId = reqNewBlockEventRegister.getAppId();
        int groupId = reqNewBlockEventRegister.getGroupId();
        String exchangeName = reqNewBlockEventRegister.getExchangeName();
        // username as queue name
        String queueName = reqNewBlockEventRegister.getQueueName();

        // String blockRoutingKey = queueName + "_" + ROUTING_KEY_BLOCK + "_" + appId;
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 4);
        String blockRoutingKey = appId + "_" + ROUTING_KEY_BLOCK + "_" + randomStr;
        eventService.registerNewBlockEvent(appId, groupId,
                exchangeName, queueName, blockRoutingKey);
        log.debug("end registerNewBlockEvent. ");
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }


    @ApiOperation(value = "registerContractEvent",
            notes = "register contract event callback and push message to mq")
    @ApiImplicitParam(name = "ReqContractEventRegister", value = "EventLogUserParams与消息队列所需配置",
            required = true, dataType = "ReqContractEventRegister")
    @PostMapping("contractEvent")
    public BaseResponse registerContractEvent(
            @Valid @RequestBody ReqContractEventRegister reqContractEventRegister) {
        log.debug("start registerContractEvent. {}", reqContractEventRegister);
        int groupId = reqContractEventRegister.getGroupId();
        String appId = reqContractEventRegister.getAppId();
        String fromBlock = reqContractEventRegister.getFromBlock();
        String toBlock = reqContractEventRegister.getToBlock();
        String contractAddress = reqContractEventRegister.getContractAddress();
        List<String> topicList = reqContractEventRegister.getTopicList();
        String contractAbi = reqContractEventRegister.getContractAbi();
        String exchangeName = reqContractEventRegister.getExchangeName();
        // username as queue name
        String queueName = reqContractEventRegister.getQueueName();

        // String eventRoutingKey = queueName + "_" + ROUTING_KEY_EVENT + "_" + appId;
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 4);
        String eventRoutingKey = appId + "_" + ROUTING_KEY_EVENT + "_" + randomStr;
        // register contract event log push in service
        eventService.registerContractEvent(appId, groupId,
                exchangeName, queueName, eventRoutingKey,
                contractAbi, fromBlock, toBlock, contractAddress, topicList);
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
        // "username_routingKey"
        String blockRoutingKey = queueName + "_" + ROUTING_KEY_BLOCK + "_" + appId;
        eventService.unregisterNewBlock(infoId, appId, groupId,
                exchangeName, queueName, blockRoutingKey);
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
        // "username_routingKey"
        String blockRoutingKey = queueName + "_" + ROUTING_KEY_EVENT + "_" + appId;
        eventService.unregisterContractEvent(infoId, appId, groupId,
                exchangeName, queueName, blockRoutingKey);
        log.debug("end unregisterContractEvent. ");
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }

}
