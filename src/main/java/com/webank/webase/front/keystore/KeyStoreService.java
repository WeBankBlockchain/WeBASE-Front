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
import com.webank.webase.front.base.AesUtils;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keyServer.KeyServerRestTools;
import com.webank.webase.front.keyServer.KeyServerService;
import com.webank.webase.front.keyServer.entity.ReqNewUserDto;
import com.webank.webase.front.keyServer.entity.RspUserInfoDto;
import com.webank.webase.front.util.CommonUtils;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;


/**
 * KeyStoreService.
 */
@Slf4j
@Service
public class KeyStoreService {

    @Autowired
    private AesUtils aesUtils;
    @Autowired
    Constants constants;
    @Autowired
    private KeyServerService keyServerService;
    static final int PUBLIC_KEY_LENGTH_IN_HEX = 128;

    public static HashMap<String, String> keyMap = new HashMap();
    private static final String KEY_SERVER_NAME = "webase-sign";

    /**
     * createPrivateKey.
     */
    public KeyStoreInfo createPrivateKey(boolean useAes) {
        try {
            ECKeyPair keyPair = Keys.createEcKeyPair();
            return keyPair2KeyStoreInfo(keyPair, useAes);
        } catch (Exception e) {
            log.error("fail createPrivateKey.", e);
            throw new FrontException("create keyInfo failed");
        }
    }

    /**
     * new user.
     */
    public KeyStoreInfo newUser(boolean useAes, String userName) {
        if (constants.getKeyServerUrl().contains(KEY_SERVER_NAME)) {
            ReqNewUserDto param = new ReqNewUserDto(userName);
            final KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
            RspUserInfoDto rspUser = keyServerService.newUser(param);
            Optional.ofNullable(rspUser).ifPresent(u -> BeanUtils.copyProperties(u, keyStoreInfo));
            if (useAes) {
                keyStoreInfo.setPrivateKey(aesUtils.aesEncrypt(rspUser.getPrivateKey()));
            }
            return keyStoreInfo;
        } else {
            return createPrivateKey(useAes);
        }

    }

    /**
     * get KeyStoreInfo by privateKey.
     */
    public KeyStoreInfo getKeyStoreFromPrivateKey(String privateKey, boolean useAes) {
        log.debug("start getKeyStoreFromPrivateKey. privateKey:{} useAes", privateKey, useAes);
        if (StringUtils.isBlank(privateKey)) {
            log.error("fail getKeyStoreFromPrivateKey. private key is null");
            throw new FrontException(ConstantCode.PRIVATEKEY_IS_NULL);
        }
        byte[] base64decodedBytes = Base64.getDecoder().decode(privateKey);
        String decodeKey = null;
        try {
            decodeKey = new String(base64decodedBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("fail getKeyStoreFromPrivateKey", e);
            throw new FrontException(ConstantCode.PRIVATE_KEY_DECODE_FAIL);
        }
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(decodeKey));
        return keyPair2KeyStoreInfo(keyPair, useAes);
    }

    /**
     * convert ECKeyPair to KeyStoreInfo.
     */
    private KeyStoreInfo keyPair2KeyStoreInfo(ECKeyPair keyPair, boolean useAes) {
        String publicKey = Numeric
            .toHexStringWithPrefixZeroPadded(keyPair.getPublicKey(), PUBLIC_KEY_LENGTH_IN_HEX);
        String privateKey = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
        String address = "0x" + Keys.getAddress(keyPair.getPublicKey());
        log.debug("publicKey:{} privateKey:{} address:{}", publicKey, privateKey, address);
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        keyStoreInfo.setPublicKey(publicKey);
        keyStoreInfo.setPrivateKey(privateKey);
        keyStoreInfo.setAddress(address);

        keyMap.put(address, privateKey);

        if (useAes) {
            keyStoreInfo.setPrivateKey(aesUtils.aesEncrypt(keyStoreInfo.getPrivateKey()));
        } else {
            keyStoreInfo.setPrivateKey(privateKey);
        }
        log.info("keyPair2KeyStoreInfo=======================keyMap:{}", JSON.toJSONString(keyMap));
        return keyStoreInfo;
    }


    /**
     * get credential.
     */
    public Credentials getCredentials(String user, boolean useAes) throws FrontException {
        String privateKey = Optional.ofNullable(getPrivateKey(user, useAes)).orElse(null);
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
    public String getPrivateKey(String user, boolean useAes) {
        log.info("getPrivateKey=======================keyMap:{}", JSON.toJSONString(keyMap));
        if (KeyStoreService.keyMap != null && KeyStoreService.keyMap.get(user) != null) {
            //get privateKey by address
            return keyMap.get(user);
        }

        //get privateKey by user
        RspUserInfoDto rspUser = keyServerService.getUser(user);
        String privateKey = Optional.ofNullable(rspUser).map(u -> u.getPrivateKey())
            .orElseThrow(() -> new FrontException(ConstantCode.PRIVATEKEY_IS_NULL));

        keyMap.put(user, privateKey);

        if (useAes && StringUtils.isNotBlank(privateKey)&& !constants.getKeyServerUrl().contains(KEY_SERVER_NAME)) {
            return aesUtils.aesDecrypt(privateKey);
        }

        return privateKey;
    }
}

