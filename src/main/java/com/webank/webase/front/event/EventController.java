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

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.event.entity.NewBlockEventInfo;
import com.webank.webase.front.event.entity.ContractEventInfo;
import com.webank.webase.front.event.entity.ReqNewBlockEventRegister;
import com.webank.webase.front.event.entity.ReqContractEventRegister;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
        int groupId = reqNewBlockEventRegister.getGroupId();
        String exchangeName = reqNewBlockEventRegister.getExchangeName();
        // username as queue name
        String queueName = reqNewBlockEventRegister.getQueueName();

        // "username_routingKey"
        String blockRoutingKey = queueName + "_" + ROUTING_KEY_BLOCK + "_" + appId;
        List<NewBlockEventInfo> resList = eventService.registerNewBlockEvent(appId, groupId,
                exchangeName, queueName, blockRoutingKey);

        return new BaseResponse(ConstantCode.RET_SUCCESS, resList);
    }


    @ApiOperation(value = "registerContractEvent",
            notes = "register contract event callback and push message to mq")
    @ApiImplicitParam(name = "ReqContractEventRegister", value = "EventLogUserParams与消息队列所需配置",
            required = true, dataType = "ReqContractEventRegister")
    @PostMapping("contractEvent")
    public BaseResponse registerContractEvent(
            @Valid @RequestBody ReqContractEventRegister reqContractEventRegister, BindingResult result) {
        log.debug("start registerContractEvent. {}", reqContractEventRegister);
        checkParamResult(result);
        int groupId = reqContractEventRegister.getGroupId();
        String appId = reqContractEventRegister.getAppId();
        String fromBlock = reqContractEventRegister.getFromBlock();
        String toBlock = reqContractEventRegister.getToBlock();
        if("0".equals(fromBlock) || "0".equals(toBlock)
                || Integer.parseInt(toBlock) <= Integer.parseInt(fromBlock)) {
            return new BaseResponse(ConstantCode.BLOCK_RANGE_PARAM_INVALID);
        }
        String contractAddress = reqContractEventRegister.getContractAddress();
        List<String> topicList = reqContractEventRegister.getTopicList();
        List<AbiDefinition> contractAbi = reqContractEventRegister.getContractAbi();
        String abiStr = JSON.toJSONString(contractAbi);
        String exchangeName = reqContractEventRegister.getExchangeName();
        // username as queue name
        String queueName = reqContractEventRegister.getQueueName();

        // bind queue to exchange by routing key "username_event"
        String eventRoutingKey = queueName + "_" + ROUTING_KEY_EVENT + "_" + appId;
        // register contract event log push in service
        List<ContractEventInfo> resList = eventService.registerContractEvent(appId, groupId,
                exchangeName, queueName, eventRoutingKey,
                abiStr, fromBlock, toBlock, contractAddress, topicList);

        return new BaseResponse(ConstantCode.RET_SUCCESS, resList);
    }

}
