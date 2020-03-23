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

package com.webank.webase.front.gm.runtime;


import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.keystore.entity.EncodeInfo;
import com.webank.webase.front.keystore.entity.KeyStoreInfo;
import com.webank.webase.front.util.CommonUtils;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.Hash;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.utils.ByteUtil;
import org.fisco.bcos.web3j.utils.Numeric;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.SignatureException;

public class SignDataTest extends BaseTest {
    @Autowired
    KeyStoreService keyStoreService;
    @Autowired
    Constants constants;
    @Autowired
    RestTemplate restTemplate;


    private static final String rawData = "123";

    /**
     * test guomi webase-sign
     * needed: config keyServer in yml
     */
    @Test
    public void testSignData() throws SignatureException {
        EncodeInfo encodeInfo = new EncodeInfo();
        // uuid to set uuid of webase-sign's keystoreInfo
        encodeInfo.setSignUserId("uuid");
        encodeInfo.setEncodedDataStr(Hash.sha3String(rawData));
        String signedData = keyStoreService.getSignData(encodeInfo);// from webase-sign
        System.out.println(signedData); // 00fd8bbc86faa0cf9216886e15118862ef5469c5283ab43b727ebaee93d866ced346c6eea89aac11be598de5ad8b3711791594651a3a3e44df2f7d9a522da351e0
//        Assert.assertTrue(StringUtils.isNotBlank(signedData));

    }

    public String getPriFromSign(int userIdSign) {

        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        String[] ipPortArr = constants.getKeyServer().split(",");
        try {
            String url = "http://127.0.0.1:5004/WeBASE-Sign/user/100001/userInfo";
            BaseResponse response = restTemplate.getForObject(url, BaseResponse.class);
            if (response.getCode() == 0) {
                keyStoreInfo =
                        CommonUtils.object2JavaBean(response.getData(), KeyStoreInfo.class);
            }
        } catch (Exception e) {
            return "";
        }

        return keyStoreInfo.getPrivateKey();
    }

    public String getLocalSignedData(String pri) {
        Credentials credentials = GenCredential.create(pri);
        Sign.SignatureData signatureData = Sign.getSignInterface().signMessage(
                ByteUtil.hexStringToBytes(Numeric.toHexString(rawData.getBytes())), credentials.getEcKeyPair());
        return CommonUtils.signatureDataToString(signatureData);
    }


    /**
     * key pair from sign differs from local credential
     * front or sign change @param encryptType: 1 and 0 to switch from guomi and standard
     */
    @Test
    public void testKeyFromSign() {
        KeyStoreInfo keyStoreInfo = getKeyStoreInfoFromSign();
        Assert.assertNotNull(keyStoreInfo);
        System.out.println("keyStoreInfo: ");
        System.out.println(keyStoreInfo.getAddress());
        System.out.println(keyStoreInfo.getPublicKey());
        System.out.println(keyStoreInfo.getPrivateKey());
        // local guomi
        Credentials credentials = GenCredential.create(keyStoreInfo.getPrivateKey());
        String pub = Numeric.toHexStringWithPrefix(credentials.getEcKeyPair().getPublicKey());
        String addr = credentials.getAddress();
        System.out.println("local transfer: ");
        System.out.println(pub);
        System.out.println(addr);
        Assert.assertTrue(keyStoreInfo.getPublicKey().equals(pub));
        Assert.assertTrue(keyStoreInfo.getAddress().equals(addr));
    }

    public KeyStoreInfo getKeyStoreInfoFromSign() {

        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        try {
            String url = "http://127.0.0.1:5004/WeBASE-Sign/user/newUser";
            BaseResponse response = restTemplate.getForObject(url, BaseResponse.class);
            if (response.getCode() == 0) {
                keyStoreInfo =
                        CommonUtils.object2JavaBean(response.getData(), KeyStoreInfo.class);
            }
        } catch (Exception e) {
            return null;
        }

        return keyStoreInfo;
    }
}
