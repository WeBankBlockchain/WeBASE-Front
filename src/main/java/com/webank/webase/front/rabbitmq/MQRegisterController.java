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

package com.webank.webase.front.rabbitmq;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.rabbitmq.entity.ReqEventLogPushRegister;
import com.webank.webase.front.rabbitmq.entity.ReqRegister;
import com.webank.webase.front.util.RabbitMQUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author marsli
 */
@Api(value = "/precompiled", tags = "precompiled manage interface")
@Slf4j
@RestController
@RequestMapping(value = "register")
public class MQRegisterController {

    @Autowired
    private EventLogPushRegisterService eventLogPushRegisterService;
    @Autowired
    private RabbitMQPublisher rabbitMQPublisher;
    @Autowired
    private MQRegisterService MQRegisterService;

    @ApiOperation(value = "registerEventLogPush",
            notes = "register eventLogPushCallBack and push message to mq")
    @ApiImplicitParam(name = "ReqEventLogPushRegister", value = "EventLogUserParams与消息队列名",
            required = true, dataType = "ReqEventLogPushRegister")
    @PostMapping("eventLogPush")
    public Object registerEventLogPush(
            @Valid @RequestBody ReqEventLogPushRegister reqEventLogPushRegister)
            throws Exception {
        log.info("start registerEventLogPush. reqEventLogPushRegister:{}", reqEventLogPushRegister);
        String fromBlock = reqEventLogPushRegister.getFromBlock();
        String toBlock = reqEventLogPushRegister.getToBlock();
        String contractAddress = reqEventLogPushRegister.getAddress();
        List<String> topicList = reqEventLogPushRegister.getTopicList();
        int groupId = reqEventLogPushRegister.getGroupId();
        String contractAbi = reqEventLogPushRegister.getContractAbi();
        String exchangeName = reqEventLogPushRegister.getExchangeName();
        String routingKey = reqEventLogPushRegister.getRoutingKey();
        eventLogPushRegisterService.registerDecodedEventLogPush(groupId, contractAbi,
                fromBlock, toBlock, contractAddress, topicList, exchangeName, routingKey);
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }

    @ApiOperation(value = "createQueue",
            notes = "declare message queue in rabbit mq")
    @ApiImplicitParam(name = "ReqRegister", value = "创建消息队列的队列名与exchangeName",
            required = true, dataType = "ReqRegister")
    @PostMapping("queue")
    public Object createQueue(
            @Valid @RequestBody ReqRegister reqRegister)
            throws Exception {
        log.info("start createQueue. reqRegister:{}", reqRegister);
        MQRegisterService.declareNewQueue(reqRegister);
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }

}
