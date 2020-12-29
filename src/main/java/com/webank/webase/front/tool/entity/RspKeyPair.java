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

package com.webank.webase.front.tool.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.utils.Numeric;

@Data
@NoArgsConstructor
public class RspKeyPair {
    // all hex string
    private String privateKey;
    private String publicKey;
    private String address;
    private Integer encryptType;

    public RspKeyPair(ECKeyPair ecKeyPair, Integer encryptType) {
        // no prefix
        this.privateKey = Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey());
        // with prefix
        this.publicKey = Numeric.toHexStringWithPrefix(ecKeyPair.getPublicKey());
        this.address = "0x" + Keys.getAddress(ecKeyPair.getPublicKey());
        this.encryptType = encryptType;
    }
}
