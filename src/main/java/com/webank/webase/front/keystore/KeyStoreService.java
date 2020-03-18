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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.KeyTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.keystore.entity.EncodeInfo;
import com.webank.webase.front.keystore.entity.KeyStoreInfo;
import com.webank.webase.front.keystore.entity.RspUserInfo;
import com.webank.webase.front.keystore.entity.SignInfo;
import com.webank.webase.front.util.AesUtils;
import com.webank.webase.front.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.util.*;


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
     * getLocalKeyStores.
     * v1.3.0+: no private key stored
     */
    public List<KeyStoreInfo> getLocalKeyStoreList() {
        Sort sort = new Sort(Sort.Direction.ASC, "userName");
        List<KeyStoreInfo> keyStores = keystoreRepository.findAll(
                (Root<KeyStoreInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
                    Predicate predicate = criteriaBuilder.equal(root.get("type"), KeyTypes.LOCALUSER.getValue());
                    return criteriaBuilder.and(predicate);
                }, sort);
        // make sure not transport private key
        keyStores.forEach(keyStoreInfo -> keyStoreInfo.setPrivateKey(null));
        return keyStores;
    }

    /**
     * create key store locally and save
     */
    public KeyStoreInfo createKeyStoreLocally(String userName) {
        log.info("start createKeyStore. userName:{}", userName);
        // check keyStoreInfoLocal
        KeyStoreInfo keyStoreInfoLocal = keystoreRepository.findByUserNameAndType(userName,
                KeyTypes.LOCALUSER.getValue());
        if (Objects.nonNull(keyStoreInfoLocal)) {
            log.error("fail createKeyStore. user name already exists.");
            throw new FrontException(ConstantCode.USER_NAME_EXISTS);
        }
        // create keyPair(support guomi)
        KeyStoreInfo keyStoreInfo;
        try {
            ECKeyPair keyPair = GenCredential.createKeyPair();
            keyStoreInfo = keyPair2KeyStoreInfo(keyPair, userName);
            keyStoreInfo.setType(KeyTypes.LOCALUSER.getValue());
        } catch (Exception e) {
            log.error("fail createKeyStore.", e);
            throw new FrontException("create keyInfo failed");
        }
        return keystoreRepository.save(keyStoreInfo);
    }


    /**
     * create keystore by webase-sign and not save
     * @param signUserId
     * @param appId
     * @return
     */
    public KeyStoreInfo createKeyStoreWithSign(String signUserId, String appId) {
        // String signUserId = UUID.randomUUID().toString().replaceAll("-","");
        // String appId = UUID.randomUUID().toString().replaceAll("-","");
        RspUserInfo rspUserInfo = getSignUserEntity(signUserId, appId);
        String address = rspUserInfo.getAddress();
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        keyStoreInfo.setAddress(address);
        keyStoreInfo.setPublicKey(rspUserInfo.getPublicKey());
        keyStoreInfo.setSignUserId(rspUserInfo.getSignUserId());
        keyStoreInfo.setAppId(rspUserInfo.getAppId());
        keyStoreInfo.setType(KeyTypes.EXTERNALUSER.getValue());
        return keystoreRepository.save(keyStoreInfo);
    }

    /**
     * deleteKeyStore.
     */
    public void deleteKeyStore(String address) {
        keystoreRepository.delete(address);
    }

    /**
     * convert ECKeyPair to KeyStoreInfo.
     * default aes true
     */
    private KeyStoreInfo keyPair2KeyStoreInfo(ECKeyPair keyPair, String userName) {
        String publicKey = Numeric
                .toHexStringWithPrefixZeroPadded(keyPair.getPublicKey(), PUBLIC_KEY_LENGTH_IN_HEX);
        String privateKey = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
        String address = "0x" + Keys.getAddress(keyPair.getPublicKey());
        log.debug("publicKey:{} privateKey:{} address:{}", publicKey, privateKey, address);
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        keyStoreInfo.setPublicKey(publicKey);
        keyStoreInfo.setAddress(address);
        keyStoreInfo.setPrivateKey(aesUtils.aesEncrypt(privateKey));
        keyStoreInfo.setUserName(userName);
        return keyStoreInfo;
    }

    /**
     * get random credential to call transaction(not execute)
     * 2019/11/26 support guomi
     */
    public Credentials getCredentialsForQuery() {
        log.debug("start getCredentialsForQuery. ");
        // create keyPair(support guomi)
        KeyStoreInfo keyStoreInfo;
        try {
            ECKeyPair keyPair = GenCredential.createKeyPair();
            keyStoreInfo = keyPair2KeyStoreInfo(keyPair, "");
        } catch (Exception e) {
            log.error("fail getCredentialsForQuery.", e);
            throw new FrontException("create random Credentials for query failed");
        }
        return GenCredential.create(keyStoreInfo.getPrivateKey());
    }

    public KeyStoreInfo getKeyStoreInfoForQuery() {
        log.debug("start getKeyStoreInfoForQuery. ");
        // create keyPair(support guomi)
        KeyStoreInfo keyStoreInfo;
        try {
            ECKeyPair keyPair = GenCredential.createKeyPair();
            return keyPair2KeyStoreInfo(keyPair, "");
        } catch (Exception e) {
            log.error("fail getKeyStoreInfoForQuery.", e);
            throw new FrontException("create random keyInfo for query failed");
        }
    }

    /**
     * getSignData from sign service. (webase-sign)
     * @param params params
     * @return
     */
    public String getSignData(EncodeInfo params) {
        try {
            SignInfo signInfo = new SignInfo();
            String url = String.format(Constants.WEBASE_SIGN_URI, constants.getKeyServer());
            log.info("getSignData url:{}", url);
            HttpHeaders headers = CommonUtils.buildHeaders();
            HttpEntity<String> formEntity =
                    new HttpEntity<String>(JSON.toJSONString(params), headers);
            BaseResponse response =
                    restTemplate.postForObject(url, formEntity, BaseResponse.class);
            log.info("getSignData response:{}", JSON.toJSONString(response));
            if (response.getCode() == 0) {
                signInfo = CommonUtils.object2JavaBean(response.getData(), SignInfo.class);
            }
            return signInfo.getSignDataStr();
        } catch (Exception e) {
            log.error("getSignData exception", e);
        }

        return null;
    }

    /**
     * get user from webase-sign api(v1.3.0+)
     * @param signUserId unique user id to call webase-sign
     * @return
     */
    public RspUserInfo getSignUserEntity(String signUserId, String appId) {
        try {
            // webase-sign api(v1.3.0) support
            RspUserInfo rspUserInfo = new RspUserInfo();
            String url = String.format(Constants.WEBASE_SIGN_USER_URI, constants.getKeyServer(),
                    EncryptType.encryptType, signUserId, appId);
            log.info("getSignUserEntity url:{}", url);
            HttpHeaders headers = CommonUtils.buildHeaders();
            HttpEntity<String> formEntity =
                    new HttpEntity<String>(null, headers);
            ResponseEntity<BaseResponse> response = restTemplate.exchange(url, HttpMethod.GET, formEntity, BaseResponse.class);
            BaseResponse baseResponse = response.getBody();
            log.info("getSignUserEntity response:{}", JSON.toJSONString(baseResponse));
            if (baseResponse.getCode() == 0) {
                rspUserInfo = CommonUtils.object2JavaBean(baseResponse.getData(), RspUserInfo.class);
            }
            return rspUserInfo;
        } catch (ResourceAccessException ex) {
            log.error("fail restTemplateExchange", ex);
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        } catch (HttpStatusCodeException e) {
            JSONObject error = JSONObject.parseObject(e.getResponseBodyAsString());
            log.error("http request fail. error:{}", JSON.toJSONString(error));
            throw new FrontException(error.getInteger("code"),
                    error.getString("errorMessage"));
        } catch (Exception e) {
            log.error("getSignUserEntity exception", e);
            throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
        }
    }

    /**
     * get signUserId by address
     * @param address
     * @return signUserId
     */
    public String getSignUserIdByAddress(String address) {
        KeyStoreInfo keyStoreInfo = keystoreRepository.findByAddress(address);
        if (Objects.isNull(keyStoreInfo)) {
            throw new FrontException(ConstantCode.KEYSTORE_NOT_EXIST);
        }
        return keyStoreInfo.getSignUserId();
    }

    /**
     * get credential to send transaction
     * 2019/11/26 support guomi
     */
    public Credentials getCredentials(String user) throws FrontException {
        String privateKey = getPrivateKey(user);
        return GenCredential.create(privateKey);
    }
    /**
     * get PrivateKey.
     * default use aes encrypt
     * @param user userId or userAddress.
     */
    public String getPrivateKey(String user) {

        // get from local db
        KeyStoreInfo keyStoreInfoLocal = keystoreRepository.findByAddress(user);
        if (Objects.isNull(keyStoreInfoLocal)) {
            log.warn("fail getPrivateKey. user:{} privateKey is null", user);
            throw new FrontException(ConstantCode.PRIVATEKEY_IS_NULL);
        }
        //get privateKey by address
        return aesUtils.aesDecrypt(keyStoreInfoLocal.getPrivateKey());

    }

    /**
     * import keystore info from pem file's content
     * @param pemContent
     * @param userName
     * @return
     */
    public KeyStoreInfo importKeyStoreFromPem(String pemContent, String userName) {
        PEMManager pemManager = new PEMManager();
        String privateKey;
        try {
            pemManager.load(new ByteArrayInputStream(pemContent.getBytes()));
            privateKey = Numeric.toHexStringNoPrefix(pemManager.getECKeyPair().getPrivateKey());
        }catch (Exception e) {
            log.error("importKeyStoreFromPem error:[]", e);
            throw new FrontException(ConstantCode.PEM_CONTENT_ERROR);
        }
        // to store
        return importFromPrivateKey(privateKey, userName);
    }

    /**
     * save LOCAL_USER key store by private key
     * @param privateKey
     * @param userName
     * @return KeyStoreInfo local user
     */
    public KeyStoreInfo importFromPrivateKey(String privateKey, String userName) {
        // to store
        ECKeyPair keyPair = GenCredential.createKeyPair(privateKey);
        KeyStoreInfo keyStoreInfo = keyPair2KeyStoreInfo(keyPair, userName);
        keyStoreInfo.setType(KeyTypes.LOCALUSER.getValue());
        return keystoreRepository.save(keyStoreInfo);
    }

}

