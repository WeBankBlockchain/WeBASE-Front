package com.webank.webase.front.keystore;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreInfo;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.stereotype.Service;

/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * KeyStoreService.
 *
 */
@Slf4j
@Service
public class KeyStoreService {
    static final int PUBLIC_KEY_LENGTH_IN_HEX = 128;

    /**
     * createPrivateKey.
     *
     * @return
     */
    public KeyStoreInfo createPrivateKey() {
        try {
            ECKeyPair keyPair = Keys.createEcKeyPair();
            String publicKey = Numeric.toHexStringWithPrefixZeroPadded(keyPair.getPublicKey(), PUBLIC_KEY_LENGTH_IN_HEX);
            String privateKey = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
            String address = "0x" + Keys.getAddress(publicKey);

            KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
            keyStoreInfo.setPublicKey(publicKey);
            keyStoreInfo.setPrivateKey(privateKey);
            keyStoreInfo.setAddress(address);

            return keyStoreInfo;
        } catch (Exception e) {
            throw new FrontException("create keyinfo failed");
        }
    }

    public KeyStoreInfo getKeyStoreFromPrivateKey(String privateKey) {
        try {
            ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
            String publicKey = Numeric.toHexStringWithPrefixZeroPadded(keyPair.getPublicKey(), PUBLIC_KEY_LENGTH_IN_HEX);
            String address = "0x" + Keys.getAddress(keyPair.getPublicKey());
            KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
            keyStoreInfo.setPublicKey(publicKey);
            keyStoreInfo.setPrivateKey(privateKey);
            keyStoreInfo.setAddress(address);

            return keyStoreInfo;
        } catch (Exception e) {
            throw new FrontException("create keyinfo failed");
        }
    }
}

