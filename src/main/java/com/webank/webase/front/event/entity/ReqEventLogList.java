/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.event.entity;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import javax.validation.constraints.NotEmpty;

/**
 * sync get event logs of exactly block
 */
@Data
public class ReqEventLogList {
    /**
     * group id
     */
    @NotNull(message = "groupId cannot be null")
    private Integer groupId;

    /**
     * contract abi for decoder
     */
    @NotEmpty(message = "contractAbi cannot be empty")
    private List<Object> contractAbi;

    /**
     * event log push info below
     */

    @NotNull(message = "fromBlock cannot be empty")
    private Integer fromBlock;

    @NotNull(message = "toBlock cannot be empty")
    private Integer toBlock;

    /**
     * single contract address
     */
    private String contractAddress;

    /**
     * List of topics
     */
    private EventTopicParam topics;

}
