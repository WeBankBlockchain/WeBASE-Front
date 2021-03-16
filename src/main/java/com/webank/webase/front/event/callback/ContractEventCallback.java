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
import com.webank.webase.front.event.entity.message.EventLogPushMessage;
import java.util.List;
import lombok.Data;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.eventsub.EventCallback;
import org.fisco.bcos.sdk.model.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 指定exchangeName和routingKey, callback后直接push到对应的mq中
 * routingKey是exchange推送到queue的唯一标志, ex: username_event, username_block
 * to start/stop this callback pushing message to mq, set id sth. / empty("")
 * @author marsli
 */
@Data
public class ContractEventCallback implements EventCallback {

    private static final Logger logger =
            LoggerFactory.getLogger(ContractEventCallback.class);

    private MQPublisher MQPublisher;
    private String exchangeName;
    private String routingKey;
    private int groupId;
    private String appId;
//    @Setter
//    private boolean running = false;
    private ABICodec abiCodec;
    private String contractAbi;
    private List<String> eventNameList;

    public ContractEventCallback(MQPublisher mqPublisher,
        String exchangeName, String routingKey, int groupId, String appId,
        ABICodec abiCodec, String contractAbi, List<String> eventNameList) {
        this.MQPublisher = mqPublisher;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.groupId = groupId;
        this.appId = appId;
        this.abiCodec = abiCodec;
        this.contractAbi = contractAbi;
        this.eventNameList = eventNameList;
    }


    /**
     * 根据Log对象中的blockNumber，transactionIndex，logIndex进行去重
     * @param status
     * @param logs
     */
    @Override
    public void onReceiveLog(int status, List<EventLog> logs) {
        logger.info(
                "ContractEventCallback onPushEventLog" +
                        " status: {}, logs: {}", status, logs);
        // decode event
        // todo event decode result entity
//        List<Object> decodeResList = new ArrayList<>();
//        if (logs != null) {
//            for (EventLog log : logs) {
//                logger.debug(
//                    " blockNumber:" + log.getBlockNumber()
//                        + ",txIndex:" + log.getTransactionIndex()
//                        + " data:" + log.getData());
//                for (String eventName: eventNameList) {
//                    try {
//                        List<Object> list = abiCodec.decodeEvent(contractAbi, eventName, log);
//                        logger.debug("decode event of :{}, log content:{} ", eventName, list);
//                    } catch (ABICodecException e) {
//                        logger.error("decode event log error:{} ", e.getMessage());
//                    }
//                }
//            }
//        }
        // 推送到指定的MQ中
        pushMessage2MQ(groupId, status, logs);
    }

    private void pushMessage2MQ(int groupId,
                                int status, List<EventLog> logs) {
        EventLogPushMessage eventLogPushMessage = new EventLogPushMessage();
        eventLogPushMessage.setEventType(EventTypes.EVENT_LOG_PUSH.getValue());
        eventLogPushMessage.setGroupId(groupId);
        eventLogPushMessage.setStatus(status);
        eventLogPushMessage.setLogs(logs);
        eventLogPushMessage.setAppId(appId);
        MQPublisher.sendToTradeFinishedByString(exchangeName, routingKey,
                eventLogPushMessage.toString());
    }

}
