package com.webank.webase.front.rabbitmq;

import com.webank.webase.front.rabbitmq.entity.MqObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * MQ publisher to send message @param: MqObject
 * @author marsli
 */
@Component
public class RabbitMQPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String BLOCK_EXCHANGE= "block_exchange";

    public void sendToTradeFinished(MqObject mqObject) {
        // 设置mqObject.toString().getBytes()
        Message message = MessageBuilder.withBody(mqObject.toString().getBytes())
                .setHeader("type", "block")
                .setMessageId(UUID.randomUUID()+"")
                .build();
        rabbitTemplate.convertAndSend(BLOCK_EXCHANGE,"", message);
    }

    public void sendToTradeFinishedByString(String messageStr) {
        rabbitTemplate.convertAndSend(BLOCK_EXCHANGE,"", messageStr);
    }
}
