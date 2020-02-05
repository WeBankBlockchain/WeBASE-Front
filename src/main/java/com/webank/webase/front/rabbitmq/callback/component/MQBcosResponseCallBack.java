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

package com.webank.webase.front.rabbitmq.callback.component;

import com.webank.webase.front.rabbitmq.entity.message.BcosResponseMessage;
import com.webank.webase.front.rabbitmq.mqservice.RabbitMQPublisher;
import lombok.Setter;
import org.fisco.bcos.channel.client.BcosResponseCallback;
import org.fisco.bcos.channel.dto.BcosResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author marsli
 */
@Component
public class MQBcosResponseCallBack extends BcosResponseCallback {

    private static final Logger logger =
            LoggerFactory.getLogger(MQBcosResponseCallBack.class);
    @Setter
    private String exchangeName = "bcos_exchange";

    @Autowired
    private RabbitMQPublisher rabbitMQPublisher;

    @Override
    public void onResponse(BcosResponse response) {
        logger.info(
                "MQBcosResponseCallBack " +
                        "onResponse, response: {}", response);

        pushMessage2MQ(response);
    }

    private void pushMessage2MQ(BcosResponse response) {
        BcosResponseMessage bcosResponseMessage = new BcosResponseMessage(response);
        // sendX(exchangeName, routingKey, ..);
        rabbitMQPublisher.sendToTradeFinishedByString(exchangeName, "",
                bcosResponseMessage.toString());
    }


}
