/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.rabbitmq;

import com.webank.webase.front.rabbitmq.entity.ReqRegister;
import com.webank.webase.front.util.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.RabbitUtils;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化RabbitMQ实例
 * @author marsli
 */
@Slf4j
public class RabbitMQService {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    public void declareNewQueue(ReqRegister reqRegister) {
        FanoutExchange fanoutExchange = new FanoutExchange(reqRegister.getExchangeName());

        Binding binding = BindingBuilder.bind(new Queue(reqRegister.getQueueName())).to(fanoutExchange);
        rabbitAdmin.declareQueue(new Queue(reqRegister.getQueueName()));
        rabbitAdmin.declareExchange(fanoutExchange);
        rabbitAdmin.declareBinding(binding);


    }
}
