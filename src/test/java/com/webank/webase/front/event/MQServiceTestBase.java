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

import com.webank.webase.front.base.SpringTestBase;
import org.junit.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;

public class MQServiceTestBase extends SpringTestBase {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private String queueName = "test1";
    private String exchangeName = "test_exchange";
    private String routingKey = "test1_routing_key";
    @Test
    public void testDeclareExchange() {
        rabbitAdmin.declareExchange(new DirectExchange(exchangeName));
    }

    @Test
    public void testDeclareQueue() {
        rabbitAdmin.declareQueue(new Queue(queueName));
    }

    @Test
    public void testDeclareBind() {
        Binding bind = BindingBuilder.bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);
        rabbitAdmin.declareBinding(bind);
    }
}
