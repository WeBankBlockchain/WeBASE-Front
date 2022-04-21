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

/**
 * used to unregister
 * @author marsli
 */
@Data
@NoArgsConstructor
public class ReqUnregister {
    /**
     * id in database
     */
    private String id;

    /**
     * application which register new block event
     */
    @NotEmpty(message = "appId cannot be empty")
    private String appId;

    /**
     * group id
     */
    @NotNull(message = "groupId cannot be empty")
    private Integer groupId;

    /**
     * MQ info: exchange name
     */
    @NotEmpty(message = "exchangeName cannot be empty")
    private String exchangeName;

    /**
     * username as queue name
     */
    @NotEmpty(message = "queueName cannot be empty, usually use username")
    private String queueName;

}
