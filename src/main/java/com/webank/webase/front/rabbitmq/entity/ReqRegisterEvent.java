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

package com.webank.webase.front.rabbitmq.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Based on EventLogUserParams
 * @author marsli
 */
@Data
@NoArgsConstructor
public class ReqRegisterEvent {

    /**
     * event type: 1: blockNotify, 2: eventLogPush, 3: others
     */
    private int eventType;

    /**
     * group id
     */
    private int groupId;
    /**
     * MQ info: exchange name
     */
    private String exchangeName;

    /**
     * username as queue name
     */
    private String queueName;

	/**
	 * contract abi for decoder
	 */
	private String contractAbi;

    /**
     * event log push info below
     */

    private String fromBlock;

    private String toBlock;

    /**
     * single contract address
     */
    private String contractAddress;

    /**
     * List of topics
     */
    private List<String> topicList;

}
