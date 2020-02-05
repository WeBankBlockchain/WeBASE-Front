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
package com.webank.webase.front.base.config;

import com.webank.webase.front.base.properties.RabbitMQProperties;
import com.webank.webase.front.util.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化RabbitMQ实例
 * @author marsli
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    /**
     * 用于发送消息到队列
     * 指定 messageConverter为Json
     * @param connectionFactory 包含了本地连接rabbitmq服务器的配置信息
     * @return
     */
    @Bean(name = "rabbitTemplate")
    public RabbitTemplate getRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * 新建yml中rabbitmq-queue默认的队列
     * @param connectionFactory spring的yml中rabbitmq项配置
     * @param config 单独的rabbitmq-queue的配置
     * @return
     */
    @Bean(name = "rabbitAdmin")
    public RabbitAdmin initRabbitAdmin(ConnectionFactory connectionFactory, RabbitMQProperties config) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        // bind queue which configured in yml
        RabbitMQUtils.declareQueues(rabbitAdmin, config.getQueueNames());
        return rabbitAdmin;
    }

}
