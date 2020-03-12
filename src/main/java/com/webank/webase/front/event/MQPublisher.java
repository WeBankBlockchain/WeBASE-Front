package com.webank.webase.front.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 将RabbitTemplate方法进行简单封装，将message发送到指定的exchange
 * MQ publisher to send message @param: MqObject
 * @author marsli
 */
@Slf4j
@Component
public class MQPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * use string to send message
     * @param exchangeName
     * @param messageStr
     */
    public void sendToTradeFinishedByString(String exchangeName, String routingKey,
                                            String messageStr) {
        log.debug("sendToTradeFinishedByString exchangeName:{}, routingKey:{}, messageStr:{}",
                exchangeName, routingKey, messageStr);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, messageStr);
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
