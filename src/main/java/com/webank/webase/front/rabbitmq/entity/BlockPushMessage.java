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

import java.math.BigInteger;
import java.util.List;

/**
 * @author marsli
 */
@Data
@NoArgsConstructor
public class BlockPushMessage implements MqObject {
    /**
     * group id
     */
    private Integer groupId;
    /**
     * block height
     */
    private BigInteger blockNumber;

    @Override
    public String toString() {
        return "BlockPushMessage:{" + "groupId:" + groupId.toString() + ","
                + "blockNumber:" + blockNumber.toString() + "}";
    }
//    private String hash;
//    private String parentHash;
//    private String nonce;
//    private String sealer;
//    private String logsBloom;
//    private String transactionsRoot;
//    private String stateRoot;
//    private int difficulty;
//    private int totalDifficulty;
//    private List<Object> extraData;
//    private int size;
//    private Long gasLimit;
//    private Long gasUsed;
//    private String timestamp;
//    private String gasLimitRaw;
//    private String timestampRaw;
//    private String gasUsedRaw;
//    private String numberRaw;
//    /**
//     * list of tx_hash
//     */
//    private List<String> transactions;

}
