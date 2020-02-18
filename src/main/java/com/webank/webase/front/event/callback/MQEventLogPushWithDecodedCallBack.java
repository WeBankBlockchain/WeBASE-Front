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

package com.webank.webase.front.event.callback;

import com.webank.webase.front.event.RabbitMQPublisher;
import com.webank.webase.front.event.entity.message.EventLogPushMessage;
import org.fisco.bcos.channel.event.filter.EventLogPushWithDecodeCallback;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.tx.txdecode.BaseException;
import org.fisco.bcos.web3j.tx.txdecode.LogResult;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 指定exchangeName和routingKey, callback后直接push到对应的mq中
 * routingKey是exchange推送到queue的唯一标志, ex: username_event, username_block
 * @author marsli
 */
public class MQEventLogPushWithDecodedCallBack extends EventLogPushWithDecodeCallback {

    private static final Logger logger =
            LoggerFactory.getLogger(MQEventLogPushWithDecodedCallBack.class);

    private RabbitMQPublisher rabbitMQPublisher;
    private String exchangeName;
    private String routingKey;
    private int groupId;
    private String appId;

    public MQEventLogPushWithDecodedCallBack(RabbitMQPublisher rabbitMQPublisher,
                                             String exchangeName, String routingKey,
                                             TransactionDecoder decoder, int groupId, String appId) {
        this.rabbitMQPublisher = rabbitMQPublisher;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        // onPush will call father class's decoder, init EventLogPushWithDecodeCallback's decoder
        this.setDecoder(decoder);
        this.groupId = groupId;
        this.appId = appId;
    }


    /**
     * 根据Log对象中的blockNumber，transactionIndex，logIndex进行去重
     * @param status
     * @param logs
     */
    @Override
    public void onPushEventLog(int status, List<LogResult> logs) {
        logger.info(
                "MQEventLogPushWithDecodedCallBack " +
                "onPushEventLog, params: {}, status: {}, logs: {}",
                getFilter().getParams(),
                status,
                logs);
        // 推送到指定的MQ中
        pushMessage2MQ(groupId, status, logs);
    }

    private void pushMessage2MQ(int groupId,
                                int status, List<LogResult> logs) {
        EventLogPushMessage eventLogPushMessage = new EventLogPushMessage();
        eventLogPushMessage.setGroupId(groupId);
        eventLogPushMessage.setStatus(status);
        eventLogPushMessage.setLogs(logs);
        eventLogPushMessage.setAppId(appId);
        rabbitMQPublisher.sendToTradeFinishedByString(exchangeName, routingKey,
                eventLogPushMessage.toString());
    }

    @Override
    public LogResult transferLogToLogResult(Log log) {
        try {
            LogResult logResult = getDecoder().decodeEventLogReturnObject(log);
            return logResult;
        } catch (BaseException e) {
            logger.warn(" event log decode failed, log: {}", log);
            return null;
        }
    }

}
