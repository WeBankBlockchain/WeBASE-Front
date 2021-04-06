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

package com.webank.webase.front.web3api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RspSearchTransaction extends JsonTransactionResponse {
    private String timestamp;

    public RspSearchTransaction(String timestamp, JsonTransactionResponse response) {
        this.timestamp = timestamp;
        this.setGas(response.getGas());
        this.setGasPrice(response.getGasPrice());
        this.setGroupId(response.getGroupId());
        this.setBlockNumber(response.getBlockNumber() == null ? "0" : response.getBlockNumber().toString());
        this.setHash(response.getHash());
        this.setInput(response.getInput());
        this.setValue(response.getValue());
        this.setBlockHash(response.getBlockHash());
        this.setBlockLimit(response.getBlockLimit());
        this.setChainId(response.getChainId());
        this.setExtraData(response.getExtraData());
        this.setFrom(response.getFrom());
        this.setNonce(response.getNonce());
        this.setTo(response.getTo());
        this.setTransactionIndex(response.getTransactionIndex());
        this.setSignature(response.getSignature());
    }
}
