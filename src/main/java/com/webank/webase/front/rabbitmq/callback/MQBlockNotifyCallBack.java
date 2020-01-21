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
import com.webank.webase.front.rabbitmq.entity.BlockPushMessage;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.BlockNotifyCallBack;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;

/**
 * @author marsli
 */
@Slf4j
public class MQBlockNotifyCallBack implements BlockNotifyCallBack {

    @Autowired
    RabbitMQPublisher rabbitMQPublisher;

    @Override
    public void onBlockNotify(int groupID, BigInteger blockNumber) {
        log.info("MQBlockNotifyCallBack groupID:{}, blockNumber:{}",
                groupID, blockNumber);
        // TODO pull block data or pull message and enqueue in MQ
        BlockPushMessage blockPushMessage = new BlockPushMessage();
        blockPushMessage.setBlockNumber(blockNumber);
        blockPushMessage.setGroupId(groupID);
        rabbitMQPublisher.sendToTradeFinishedByString(blockPushMessage.toString());

        // TODO 确认需要哪些类型的消息/数据
        // TODO 获取节点heartbeat等等的message，see in ChannelMessageType.java
    }


}
