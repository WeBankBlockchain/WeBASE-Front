/**
 * Copyright 2014-2019 the original author or authors.
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

package com.webank.webase.front.rabbitmq.callback;

import com.webank.webase.front.rabbitmq.RabbitMQPublisher;
import com.webank.webase.front.rabbitmq.entity.message.BlockPushMessage;
import lombok.Setter;
import org.fisco.bcos.channel.client.BlockNotifyCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * 出块后将Push一个信息到RabbitMQ
 * @author marsli
 */
@Component
public class MQBlockNotifyCallBack implements BlockNotifyCallBack {

    private static Logger logger = LoggerFactory.getLogger(MQBlockNotifyCallBack.class);

    @Setter
    private String blockExchange= "block_exchange";

    @Autowired
    private RabbitMQPublisher rabbitMQPublisher;

    @Override
    public void onBlockNotify(int groupID, BigInteger blockNumber) {
        logger.info("MQBlockNotifyCallBack groupID:{}, blockNumber:{}",
                groupID, blockNumber);
        pushMessage2MQ(blockExchange, groupID, blockNumber);
    }

    /**
     * push message to mq
     * @param exchangeName
     * @param groupID
     * @param blockNumber
     */
    private void pushMessage2MQ(String exchangeName,
                                int groupID, BigInteger blockNumber) {
        // TODO pull block data or and enqueue message in RabbitMQ
        BlockPushMessage blockPushMessage = new BlockPushMessage();
        blockPushMessage.setBlockNumber(blockNumber);
        blockPushMessage.setGroupId(groupID);
        logger.info("MQBlockNotifyCallBack pushMessage2MQ blockPushMessage:{}",
                blockPushMessage.toString());
        // TODO routing key设为groupId
        rabbitMQPublisher.sendToTradeFinishedByString(exchangeName, "",
                blockPushMessage.toString());
    }


}
