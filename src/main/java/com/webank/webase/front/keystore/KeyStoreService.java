/**
 * Copyright 2014-2019 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.webank.webase.front.keystore.entity.EncodeInfo;
import com.webank.webase.front.keystore.entity.KeyStoreInfo;
import com.webank.webase.front.keystore.entity.SignInfo;
import com.webank.webase.front.util.CredentialUtils;
import com.webank.webase.front.util.PemUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSON;
import com.webank.webase.front.util.AesUtils;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.enums.KeyTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;


/**
 * KeyStoreService.
 * 2019/11/26 support guomi： create keyPair, useAes=>aes or sm4 encrypt
 */
@Slf4j
@Service
public class KeyStoreService {

    @Autowired
    private AesUtils aesUtils;
    @Autowired
    Constants constants;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    KeystoreRepository keystoreRepository;
    static final int PUBLIC_KEY_LENGTH_IN_HEX = 128;
    private static Map<String, String> PRIVATE_KEY_MAP = new HashMap<>();


    /**
     * createKeyStore
     */
    public KeyStoreInfo createKeyStore(boolean useAes, int type, String userName) {
        log.info("start createKeyStore. useAes:{} type:{} userName:{}", useAes, type, userName);
        // check keyStoreInfoLocal
        if (type == KeyTypes.LOCALUSER.getValue()) {
            if (StringUtils.isBlank(userName)) {
                log.error("fail createKeyStore. user name is null.");
                throw new FrontException(ConstantCode.USER_NAME_NULL);
            }
            KeyStoreInfo keyStoreInfoLocal = keystoreRepository.findByUserNameAndType(userName, type);
            if (keyStoreInfoLocal != null) {
                log.error("fail createKeyStore. user name already exists.");
                throw new FrontException(ConstantCode.USER_NAME_EXISTS);
            }
        }
        // create keyPair(support guomi)
        try {
            // TODO upgrade in web3sdk 2.1.3+
            ECKeyPair keyPair = CredentialUtils.createKeyPair();
            return keyPair2KeyStoreInfo(keyPair, useAes, type, userName);
        } catch (Exception e) {
            log.error("fail createKeyStore.", e);
            throw new FrontException("create keyInfo failed");
        }
    }

    /**
     * get KeyStoreInfo by privateKey to store keyStoreInfo
     */
    public KeyStoreInfo getKeyStoreFromPrivateKey(String privateKey, boolean useAes, int type, String userName) {
        log.info("start getKeyStoreFromPrivateKey. privateKey:{} userName:{}", privateKey, userName);
        if (StringUtils.isBlank(privateKey)) {
            log.error("fail getKeyStoreFromPrivateKey. private key is null");
            throw new FrontException(ConstantCode.PRIVATEKEY_IS_NULL);
        }
        if (StringUtils.isBlank(userName)) {
            log.error("fail createKeyStore. user name is null.");
            throw new FrontException(ConstantCode.USER_NAME_NULL);
        }
        KeyStoreInfo keyStoreInfoLocal = keystoreRepository.findByPrivateKey(privateKey);
        if (keyStoreInfoLocal != null) {
            log.error("fail getKeyStoreFromPrivateKey. private key already exists");
            throw new FrontException(ConstantCode.PRIVATEKEY_EXISTS);
        }
        KeyStoreInfo userInfo = keystoreRepository.findByUserNameAndType(userName, type);
        if (userInfo != null) {
            log.error("fail getKeyStoreFromPrivateKey. user name already exists.");
            throw new FrontException(ConstantCode.USER_NAME_EXISTS);
        }
        // support guomi
        ECKeyPair keyPair = CredentialUtils.createKeyPair(privateKey);
        return keyPair2KeyStoreInfo(keyPair, useAes, type, userName);
    }
    
    /**
     * getLocalKeyStores.
     */
    public List<KeyStoreInfo> getLocalKeyStores() {
        Sort sort = new Sort(Sort.Direction.ASC, "userName");
        List<KeyStoreInfo> keyStores = keystoreRepository.findAll(
                (Root<KeyStoreInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
                    Predicate predicate = criteriaBuilder.equal(root.get("type"), KeyTypes.LOCALUSER.getValue());
                    return criteriaBuilder.and(predicate);
                }, sort);
        for (KeyStoreInfo keyStoreInfo : keyStores) {
            keyStoreInfo.setPrivateKey(aesUtils.aesDecrypt(keyStoreInfo.getPrivateKey()));
        }
        return keyStores;
    }
    
    /**
     * deleteKeyStore.
     */
    public void deleteKeyStore(String address) {
        keystoreRepository.delete(address);;
    }

    /**
     * convert ECKeyPair to KeyStoreInfo.
     */
    private KeyStoreInfo keyPair2KeyStoreInfo(ECKeyPair keyPair, boolean useAes, int type, String userName) {
        String publicKey = Numeric
            .toHexStringWithPrefixZeroPadded(keyPair.getPublicKey(), PUBLIC_KEY_LENGTH_IN_HEX);
        String privateKey = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
        String address = "0x" + Keys.getAddress(keyPair.getPublicKey());
        log.debug("publicKey:{} privateKey:{} address:{}", publicKey, privateKey, address);
        
        String realPrivateKey = privateKey;
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        keyStoreInfo.setPublicKey(publicKey);
        keyStoreInfo.setAddress(address);
        keyStoreInfo.setPrivateKey(aesUtils.aesEncrypt(privateKey));
        keyStoreInfo.setUserName(userName);
        keyStoreInfo.setType(type);
        if (type != KeyTypes.LOCALRANDOM.getValue()) {
            keystoreRepository.save(keyStoreInfo);
        }

        if (!useAes) {
            keyStoreInfo.setPrivateKey(realPrivateKey);
        }
        return keyStoreInfo;
    }


    /**
     * get credential to send transaction
     * 2019/11/26 support guomi
     */
    public Credentials getCredentials(String user, boolean useAes) throws FrontException {
        String privateKey = Optional.ofNullable(getPrivateKey(user, useAes)).orElse(null);
        if (StringUtils.isBlank(privateKey)) {
            log.warn("fail getCredentials. user:{} privateKey is null", user);
            throw new FrontException(ConstantCode.PRIVATEKEY_IS_NULL);
        }
        return GenCredential.create(privateKey);
    }

    /**
     * get credential to send transaction
     * 2019/11/26 support guomi
     * @return
     */
    // 直接用一个固定的credential，不用每次都新建
    public Credentials getCredentialsForQuery() {
        log.debug("start getCredentialsForQuery. ");
        KeyStoreInfo keyStoreInfo = createKeyStore(false, KeyTypes.LOCALRANDOM.getValue(), "");
        return GenCredential.create(keyStoreInfo.getPrivateKey());
    }

    /**
     * get PrivateKey.
     *
     * @param user userId or userAddress.
     */
    public String getPrivateKey(String user, boolean useAes) {
        KeyStoreInfo keyStoreInfoLocal = keystoreRepository.findByAddress(user);

        if (keyStoreInfoLocal != null) {
            //get privateKey by address
            return aesUtils.aesDecrypt(keyStoreInfoLocal.getPrivateKey());
        }

        String key_of_user = user + "_" + useAes;
        if (PRIVATE_KEY_MAP.containsKey(key_of_user)) {
            return PRIVATE_KEY_MAP.get(key_of_user);
        }

        //get privateKey by userId from nodemgr
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        String[] ipPortArr = constants.getKeyServer().split(",");
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

        String private_key;
        if (useAes) {
            private_key = aesUtils.aesDecrypt(keyStoreInfo.getPrivateKey());
        } else {
            private_key = keyStoreInfo.getPrivateKey();
        }

        if (StringUtils.isNotBlank(private_key)) {
            PRIVATE_KEY_MAP.put(key_of_user, private_key);
        }

        return private_key;
    }

    /**
     * getSignDate from sign service. (webase-sign)
     * @param params params
     * @return
     */
    public String getSignDate(EncodeInfo params) {
        try {
            SignInfo signInfo = new SignInfo();
            String url = String.format(Constants.WEBASE_SIGN_URI, constants.getKeyServer());
            log.info("getSignDate url:{}", url);
            HttpHeaders headers = CommonUtils.buildHeaders();
            HttpEntity<String> formEntity =
                    new HttpEntity<String>(JSON.toJSONString(params), headers);
            BaseResponse response =
                    restTemplate.postForObject(url, formEntity, BaseResponse.class);
            log.info("getSignDate response:{}", JSON.toJSONString(response));
            if (response.getCode() == 0) {
                signInfo = CommonUtils.object2JavaBean(response.getData(), SignInfo.class);
            }
            return signInfo.getSignDataStr();
        } catch (Exception e) {
            log.error("getSignDate exception", e);
        }
        return null;
    }

    /**
     * import keystore info from pem file's content
     * @param pemContent
     * @param useAes false
     * @param userType local
     * @param userName
     * @return
     */
    public KeyStoreInfo importKeyStoreFromPem(String pemContent, boolean useAes, int userType,  String userName) {
        PEMManager pemManager = new PEMManager();
        String privateKey;
        try {
            pemManager.load(new ByteArrayInputStream(pemContent.getBytes()));
            privateKey = Numeric.toHexStringNoPrefix(pemManager.getECKeyPair().getPrivateKey());
        }catch (Exception e) {
            log.error("importKeyStoreFromPem error:[]", e);
            throw new FrontException(ConstantCode.PEM_CONTENT_ERROR);
        }
        getKeyStoreFromPrivateKey(privateKey, useAes, userType, userName);
        return null;
    }

}

