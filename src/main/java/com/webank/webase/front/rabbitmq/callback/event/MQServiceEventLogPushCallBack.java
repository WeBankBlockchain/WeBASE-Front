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

package com.webank.webase.front.rabbitmq.callback.event;

import com.webank.webase.front.rabbitmq.RabbitMQPublisher;
import lombok.Setter;
import org.fisco.bcos.channel.event.filter.EventLogPushWithDecodeCallback;
import org.fisco.bcos.channel.event.filter.ServiceEventLogPushCallback;
import org.fisco.bcos.web3j.tx.txdecode.LogResult;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * 与EventLogPushWithDecodedCallBack类似，ServiceEventLogPushCallback未对log字段解码
 * @author marsli
 */
public class MQServiceEventLogPushCallBack extends ServiceEventLogPushCallback {
    private static final Logger logger =
            LoggerFactory.getLogger(MQServiceEventLogPushCallBack.class);

    private RabbitMQPublisher rabbitMQPublisher;
    @Setter
    private String eventExchange = "event_exchange";
    @Setter
    private String routingKey = "";

    public MQServiceEventLogPushCallBack(RabbitMQPublisher rabbitMQPublisher,
                                             String eventExchange, String routingKey) {
        this.rabbitMQPublisher = rabbitMQPublisher;
        this.eventExchange = eventExchange;
        this.routingKey = routingKey;
    }

    @Override
    public void onPushEventLog(int status, List<LogResult> logs) {
        logger.info(
                "MQEventLogFilterCallBack onPushEventLog, params: {}, status: {}, logs: {}",
                getFilter().getParams(),
                status,
                logs);
        // push to mq
    }

}
