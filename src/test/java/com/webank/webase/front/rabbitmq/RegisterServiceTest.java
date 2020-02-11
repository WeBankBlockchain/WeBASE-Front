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

import com.webank.webase.front.util.RabbitMQUtils;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class RegisterServiceTest extends BaseTest{

    @Autowired
    private EventLogPushRegisterService eventLogPushRegisterService;

    @Test
    public void testRegisterEventLogPush() {
        String fromBlock = "latest";
        String toBlock = "latest";
        // empty list means all
        String address = "";
        List<Object> topics = new ArrayList<>();
        EventLogUserParams params = RabbitMQUtils.initEventLogUserParams(fromBlock, toBlock, address, topics);
        String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"string\"}],\"name\":\"SetName\",\"type\":\"event\"}]";
        String exchangeName = "event_exchange";
        eventLogPushRegisterService.registerEventLogPush(1, abi, params, exchangeName, "");
    }
}
