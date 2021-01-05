/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.front.event.callback;

import com.webank.webase.front.base.enums.EventTypes;
import com.webank.webase.front.event.MQPublisher;
import com.webank.webase.front.event.entity.PublisherHelper;
import com.webank.webase.front.event.entity.message.BlockPushMessage;
import org.fisco.bcos.channel.client.BlockNotifyCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

import static com.webank.webase.front.util.RabbitMQUtils.BLOCK_ROUTING_KEY_MAP;

/**
 * 出块后将Push一个信息到RabbitMQ，并广播到订阅者的队列中
 * @author marsli
 */
@Component
public class NewBlockEventCallback implements BlockNotifyCallBack {

    private static Logger logger = LoggerFactory.getLogger(NewBlockEventCallback.class);

    @Autowired
    private MQPublisher MQPublisher;

    @Override
    public void onBlockNotify(int groupID, BigInteger blockNumber) {
        logger.info("NewBlockEventCallBack groupID:{}, blockNumber:{}",
                groupID, blockNumber);
        // register map
        if (BLOCK_ROUTING_KEY_MAP.isEmpty()) {
            logger.debug("block notify register list is empty. ");
            return;
        }
        BlockPushMessage blockPushMessage = new BlockPushMessage();
        blockPushMessage.setBlockNumber(blockNumber);
        blockPushMessage.setGroupId(groupID);
        blockPushMessage.setEventType(EventTypes.BLOCK_NOTIFY.getValue());
        for (String appId: BLOCK_ROUTING_KEY_MAP.keySet()) {
            blockPushMessage.setAppId(appId);
            PublisherHelper blockPublishInfo = BLOCK_ROUTING_KEY_MAP.get(appId);
            if (groupID == blockPublishInfo.getGroupId()) {
                pushMessage2MQ(blockPublishInfo.getExchangeName(),
                        blockPublishInfo.getRoutingKey(), blockPushMessage);
            }
        }


    }

    /**
     * push message to mq
     * @param exchangeName
     * @param routingKey
     * @param blockPushMessage
     */
    private void pushMessage2MQ(String exchangeName, String routingKey,
                                BlockPushMessage blockPushMessage) {
        logger.debug("NewBlockEventCallBack pushMessage2MQ blockPushMessage:{}",
                blockPushMessage.toString());
        MQPublisher.sendToTradeFinishedByString(exchangeName, routingKey,
                blockPushMessage.toString());
    }


}
