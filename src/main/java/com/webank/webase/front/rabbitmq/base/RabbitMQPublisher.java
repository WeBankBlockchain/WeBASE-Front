package com.webank.webase.front.rabbitmq.base;

import com.webank.webase.front.rabbitmq.entity.MqObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 将RabbitTemplate方法进行简单封装，将message发送到指定的exchange
 * MQ publisher to send message @param: MqObject
 * @author marsli
 */
@Component
public class RabbitMQPublisher {

    @Autowired
    RabbitTemplate rabbitTemplate;
//    private RabbitTemplate rabbitTemplate;
//
//    public RabbitMQPublisher(RabbitTemplate rabbitTemplate) {
//        this.rabbitTemplate = rabbitTemplate;
//    }

    /**
     * use string to send message
     * @param exchangeName
     * @param messageStr
     */
    public void sendToTradeFinishedByString(String exchangeName, String messageStr) {
        rabbitTemplate.convertAndSend(exchangeName, "", messageStr);
    }

//    public void sendToTradeFinished(String exchangeName, MqObject mqObject) {
//        // 设置mqObject.toString().getBytes()
//        Message message = MessageBuilder.withBody(mqObject.toString().getBytes())
//                .setHeader("type", "block")
//                .setMessageId(UUID.randomUUID()+"")
//                .build();
//        rabbitTemplate.convertAndSend(exchangeName,"", message);
//    }

}
