package com.webank.webase.front.rabbitmq;

import com.webank.webase.front.rabbitmq.MqObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String BLOCK_EXCHANGE= "block_exchange";

    public void sendToTradeFinished(MqObject mqObject) {
        rabbitTemplate.convertAndSend(BLOCK_EXCHANGE,"", mqObject);
    }
}
