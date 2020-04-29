/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.event;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * to bind queue with routing key to exchange
 * @author marsli
 */
@Slf4j
@Service
public class MQService {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    /**
     * bind new queue to existed exchange
     * @param exchangeName
     * @param queueName
     * @param routingKey
     */
    public void bindQueue2Exchange(String exchangeName, String queueName, String routingKey){
        log.info("bindQueue2Exchange exchangeName:{},queueName:{},routingKey:{}",
                exchangeName, queueName, routingKey);
        Binding binding = BindingBuilder
                .bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);
        try {
            rabbitAdmin.declareBinding(binding);
        } catch (AmqpException ex) {
            log.error("Exchange or message queue not exists. ex:[]", ex);
            throw new FrontException(ConstantCode.EXCHANGE_OR_QUEUE_NOT_EXIST_ERROR);
        }
    }

    public void unbindQueueFromExchange(String exchangeName, String queueName, String routingKey){
        log.info("unbindQueueFromExchange exchangeName:{},queueName:{},routingKey:{}",
                exchangeName, queueName, routingKey);
        Binding binding = BindingBuilder
                .bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);
        try {
            rabbitAdmin.removeBinding(binding);
        } catch (AmqpException ex) {
            log.error("Exchange or message queue not exists. ex:[]", ex);
            throw new FrontException(ConstantCode.EXCHANGE_OR_QUEUE_NOT_EXIST_ERROR);
        }
    }

}
