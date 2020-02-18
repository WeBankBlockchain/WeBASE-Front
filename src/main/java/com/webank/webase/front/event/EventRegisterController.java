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
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.event.entity.ReqRegisterEvent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static com.webank.webase.front.util.RabbitMQUtils.*;

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
    private MQRegisterService mqRegisterService;

    @ApiOperation(value = "registerEventLogPush",
            notes = "register eventLogPushCallBack and push message to mq")
    @ApiImplicitParam(name = "ReqEventLogPushRegister", value = "EventLogUserParams与消息队列名",
            required = true, dataType = "ReqEventLogPushRegister")
    @PostMapping("")
    public Object registerEventLogPush(
            @Valid @RequestBody ReqRegisterEvent reqRegisterEvent) {
        log.info("start registerEventLogPush. reqEventLogPushRegister:{}", reqRegisterEvent);
        int eventType = reqRegisterEvent.getEventType();
        int groupId = reqRegisterEvent.getGroupId();
        String fromBlock = reqRegisterEvent.getFromBlock();
        String toBlock = reqRegisterEvent.getToBlock();
        String contractAddress = reqRegisterEvent.getContractAddress();
        List<String> topicList = reqRegisterEvent.getTopicList();
        String contractAbi = reqRegisterEvent.getContractAbi();
        String exchangeName = reqRegisterEvent.getExchangeName();
        // username as queue name
        String queueName = reqRegisterEvent.getQueueName();

        // block notify
        if(eventType == 1) {
            String blockRoutingKey = queueName + "_" + ROUTING_KEY_BLOCK;
            mqRegisterService.bindQueue2Exchange(exchangeName, queueName, blockRoutingKey);
            BLOCK_ROUTING_KEY_MAP.put(queueName, blockRoutingKey);
            // save to db
        } else if (eventType == 2) {
            // event log push
            // bind queue to exchange by routing key "event"
            String eventRoutingKey = queueName + "_" + ROUTING_KEY_EVENT;
            mqRegisterService.bindQueue2Exchange(exchangeName, queueName, eventRoutingKey);
            // register event log push in service
            eventLogPushRegisterService.registerDecodedEventLogPush(groupId, exchangeName, queueName, eventRoutingKey,
                    contractAbi, fromBlock, toBlock, contractAddress, topicList);
        }

        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }



}
