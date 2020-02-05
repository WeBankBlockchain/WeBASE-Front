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

/**
 * ex: exchange1.queue1
 * exchangeName: exchange1, queueName: exchange1.queue1
 * @author marsli
 */
@Data
@NoArgsConstructor
public class ReqRegister {


    private String exchangeName;
    private String queueName;
    private String routingKey;

//    @Override
//    public String toString() {
//        return exchangeName + "." + routingKey;
//    }
}
