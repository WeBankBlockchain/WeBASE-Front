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
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.fisco.bcos.sdk.jni.BlockNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 出块后将Push一个信息到RabbitMQ，并广播到订阅者的队列中
 * @author marsli
 */
@Data
@AllArgsConstructor
public class NewBlockEventCallback implements BlockNotifier {

    private static Logger logger = LoggerFactory.getLogger(NewBlockEventCallback.class);
    private MQPublisher MQPublisher;
    private String groupId;
    private PublisherHelper blockPublishInfo;
    @Setter
    private Boolean running = true;

    @Override
    public void onResponse(String groupId, BigInteger blockNumber) {
        logger.info("NewBlockEventCallBack groupId:{}, blockNumber:{}",
            groupId, blockNumber);
        if (!running) {
            logger.warn("NewBlockEventCallBack skip because already unregister blockPublishInfo:{}", blockPublishInfo);
            return;
        }
        BlockPushMessage blockPushMessage = new BlockPushMessage();
        blockPushMessage.setBlockNumber(blockNumber);
        blockPushMessage.setGroupId(groupId);
        blockPushMessage.setEventType(EventTypes.BLOCK_NOTIFY.getValue());
        if (groupId.equals(this.groupId)) {
            pushMessage2MQ(blockPublishInfo.getExchangeName(),
                blockPublishInfo.getRoutingKey(), blockPushMessage);
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
        logger.info("NewBlockEventCallBack pushMessage2MQ blockPushMessage:{}",
                blockPushMessage.toString());
        MQPublisher.sendToTradeFinishedByString(exchangeName, routingKey,
                blockPushMessage.toString());
    }


}
