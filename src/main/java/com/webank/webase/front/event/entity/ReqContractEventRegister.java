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

package com.webank.webase.front.event.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Based on EventLogUserParams
 * handle request for registering contract event log push notify
 * @author marsli
 */
@Data
@NoArgsConstructor
public class ReqContractEventRegister {

    /**
     * id in database
     */
    private String infoId;

    /**
     * application which register contract event
     */
    @NotEmpty(message = "appId cannot be empty")
    private String appId;

    /**
     * group id
     */
    @NotNull(message = "groupId cannot be null")
    private Integer groupId;

    /**
     * MQ info: exchange name
     */
    @NotEmpty(message = "exchangeName cannot be empty")
    private String exchangeName;

    /**
     * appId as queue name
     */
    @NotEmpty(message = "queueName cannot be empty(usually use appId as queueName)")
    private String queueName;

    /**
     * contract abi for decoder
     */
    @NotEmpty(message = "contractAbi cannot be empty")
    private List<Object> contractAbi;

    /**
     * event log push info below
     */

    @NotEmpty(message = "fromBlock cannot be empty")
    private String fromBlock;

    @NotEmpty(message = "toBlock cannot be empty")
    private String toBlock;

    /**
     * single contract address
     */
    private String contractAddress;

    /**
     * event name list
     */
    private List<String> topicList;

}
