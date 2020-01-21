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
package com.webank.webase.front.base.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * get rabbit mq queue's config in yml
 * TODO 默认连接配置中的mq，后续支持动态增加修改
 * @author marsli
 */
@ConfigurationProperties("rabbitmq.queue")
public class RabbitMQProperties {

    private String serviceName;

    private List<String> queueNames;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<String> getQueueNames() {
        return queueNames;
    }
}
