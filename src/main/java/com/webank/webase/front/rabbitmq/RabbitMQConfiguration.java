package com.webank.webase.front.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfiguration {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AmqpAdmin rabbitAdmin;

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, RabbitMQProperties config) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        binding(config.getQueueNames());
        return rabbitTemplate;
    }

    private Map<String,List<String>> distinctList(List<String> list){
        Map<String,List<String>> map = new HashMap<>();
        for (String string :list){
            if(!string.contains(".")){
                continue;
            }
            List<String> list1 = new ArrayList<>();
            String key =string.split("\\.")[0];
            if(map.keySet().contains(key)){
                map.get(key).add(string);
            }else{
                list1.add(string);
                map.put(key,list1);
            }
        }
        return map;
    }


    private void  binding(List<String> queueNames){
        Map<String ,List<String>> map = distinctList(queueNames);
        for (String string : map.keySet()){
            FanoutExchange fanoutExchange = new FanoutExchange(string);
            for(String string1 : map.get(string)){
                Binding binding = BindingBuilder.bind(new Queue(string1)).to(fanoutExchange);
                rabbitAdmin.declareQueue(new Queue(string1));
                rabbitAdmin.declareExchange(fanoutExchange);
                rabbitAdmin.declareBinding(binding);
            }
        }
    }

}
