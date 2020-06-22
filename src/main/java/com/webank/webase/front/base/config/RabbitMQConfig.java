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
package com.webank.webase.front.base.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * init mq instance, add config in spring configuration
 * ********mq config*******
 *  spring:
 *    ...
 *    rabbitmq:
 *     host: 127.0.0.1
 *     port: 5672
 *     username: youraccount
 *     password: yourpassword
 *     virtual-host: yourvirtualhost
 *     publisher-confirm: true
 *     ssl:
 *       enabled: false
 *   ...
 * *************************
 * @author marsli
 */
@Slf4j
@Configuration
public class RabbitMQConfig {


    /**
     * 新建yml中rabbitmq-queue默认的队列
     * @param connectionFactory spring的yml中rabbitmq项配置
     * @return
     */
    @Bean(name = "rabbitAdmin")
    public RabbitAdmin initRabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        return rabbitAdmin;
    }

    /**
     * 用于发送消息到队列
     * 传输的message消息体在Message实体类中使用jackson进行序列化
     * @param rabbitAdmin
     * @return
     */
    @Bean(name = "rabbitTemplate")
    public RabbitTemplate getRabbitTemplate(RabbitAdmin rabbitAdmin) {
        RabbitTemplate rabbitTemplate = rabbitAdmin.getRabbitTemplate();
        return rabbitTemplate;
    }


}
