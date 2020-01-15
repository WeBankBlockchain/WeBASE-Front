package com.webank.webase.front.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("rabbitmq")
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
