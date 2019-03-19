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

package com.webank.webase.front.contract;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import lombok.extern.slf4j.Slf4j;
import org.bcos.web3j.crypto.ECKeyPair;
import org.bcos.web3j.crypto.Keys;
import org.bcos.web3j.utils.Numeric;
import org.springframework.stereotype.Service;

/**
 * KeyStoreService.
 *
 */
@Slf4j
@Service
public class KeyStoreService {
    static final int PUBLIC_KEY_LENGTH_IN_HEX = 128;

    /**
     * getPrivateKey.
     * 
     * @return
     */
    public BaseResponse getPrivateKey() {
        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);

        try {
            ECKeyPair keyPair = Keys.createEcKeyPair();
            String publicKey = Numeric.toHexStringWithPrefixZeroPadded(keyPair.getPublicKey(),
                    PUBLIC_KEY_LENGTH_IN_HEX);
            String privateKey = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
            String address = "0x" + Keys.getAddress(publicKey);

            KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
            keyStoreInfo.setPublicKey(publicKey);
            keyStoreInfo.setPrivateKey(privateKey);
            keyStoreInfo.setAddress(address);
            baseRspEntity.setData(keyStoreInfo);

            log.info("getPrivateKey finish. baseRspEntity[{}]", JSON.toJSONString(baseRspEntity));
            return baseRspEntity;
        } catch (Exception e) {
            log.error("createEcKeyPair fail.");
            baseRspEntity = new BaseResponse(ConstantCode.SYSTEM_ERROR);
            return baseRspEntity;
        }
    }
}
