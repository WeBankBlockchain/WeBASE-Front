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
import com.webank.webase.front.rabbitmq.mqservice.RabbitMQPublisher;
import com.webank.webase.front.rabbitmq.entity.ReqEventLogPushRegister;
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

    @ApiOperation(value = "registerEventLogPush",
            notes = "register eventLogPushCallBack and register exchange in mq")
    @ApiImplicitParam(name = "ReqEventLogPushRegister", value = "EventLogUserParams",
            required = true, dataType = "ReqEventLogPushRegister")
    @PostMapping("consensus")
    public Object registerEventLogPush(
            @Valid @RequestBody ReqEventLogPushRegister reqEventLogPushRegister)
            throws Exception {
        log.info("start registerEventLogPush. reqEventLogPushRegister:{}", reqEventLogPushRegister);
        EventLogUserParams params = eventLogPushRegisterService.setEventLogUserParams(
                reqEventLogPushRegister.getFromBlock(), reqEventLogPushRegister.getToBlock(),
                reqEventLogPushRegister.getAddresses(), reqEventLogPushRegister.getTopics());

        eventLogPushRegisterService.registerEventLogPush(params,
                reqEventLogPushRegister.getExchangeName(), reqEventLogPushRegister.getRoutingKey());
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }



}
