/**
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.keystore;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreInfo;
import com.webank.webase.front.util.CommonUtils;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import org.springframework.web.client.RestTemplate;


/**
 * KeyStoreService.
 */
@Slf4j
@Service
public class KeyStoreService {

    @Autowired
    Constants constants;
    @Autowired
    RestTemplate restTemplate;
    static final int PUBLIC_KEY_LENGTH_IN_HEX = 128;

    public static HashMap<String, String> keyMap = new HashMap();

    /**
     * createPrivateKey.
     */
    public KeyStoreInfo createPrivateKey() {
        try {
            ECKeyPair keyPair = Keys.createEcKeyPair();
            return keyPair2KeyStoreInfo(keyPair);
        } catch (Exception e) {
            throw new FrontException("create keyinfo failed");
        }
    }

    /**
     * get KeyStoreInfo by privateKey.
     */
    public KeyStoreInfo getKeyStoreFromPrivateKey(String privateKey) {
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        return keyPair2KeyStoreInfo(keyPair);
    }

    /**
     * convert ECKeyPair to KeyStoreInfo.
     */
    private KeyStoreInfo keyPair2KeyStoreInfo(ECKeyPair keyPair) {

        String publicKey = Numeric
            .toHexStringWithPrefixZeroPadded(keyPair.getPublicKey(), PUBLIC_KEY_LENGTH_IN_HEX);
        String privateKey = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
        String address = "0x" + Keys.getAddress(keyPair.getPublicKey());
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        keyStoreInfo.setPublicKey(publicKey);
        keyStoreInfo.setPrivateKey(privateKey);
        keyStoreInfo.setAddress(address);
        keyMap.put(address, privateKey);

        return keyStoreInfo;
    }


    /**
     * get credential.
     */
    public Credentials getCredentials(String user) throws FrontException {
        String privateKey = Optional.ofNullable(getPrivateKey(user)).orElse(null);
        if (StringUtils.isBlank(privateKey)) {
            log.warn("fail getCredentials. user:{} privateKey is null", user);
            throw new FrontException(ConstantCode.PRIVATEKEY_IS_NULL);
        }
        return Credentials.create(privateKey);
    }


    /**
     * get PrivateKey.
     *
     * @param user userId or userAddress.
     */
    public String getPrivateKey(String user) {

        if (KeyStoreService.keyMap != null && KeyStoreService.keyMap.get(user) != null) {
            //get privateKey by address
            return KeyStoreService.keyMap.get(user);
        }

        //get privateKey by userId
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        String[] ipPortArr = constants.getMgrIpPorts().split(",");
        for (String ipPort : ipPortArr) {
            try {
                String url = String.format(Constants.MGR_PRIVATE_KEY_URI, ipPort, user);
                log.info("getPrivateKey url:{}", url);
                BaseResponse response = restTemplate.getForObject(url, BaseResponse.class);
                log.info("getPrivateKey response:{}", JSON.toJSONString(response));
                if (response.getCode() == 0) {
                    keyStoreInfo =
                        CommonUtils.object2JavaBean(response.getData(), KeyStoreInfo.class);
                    break;
                }
            } catch (Exception e) {
                log.warn("user:{} getPrivateKey from ipPort:{} exception", user, ipPort, e);
                continue;
            }
        }

        return keyStoreInfo.getPrivateKey();
    }
}

