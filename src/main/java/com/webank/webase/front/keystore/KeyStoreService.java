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
        keyStores.forEach(keyStoreInfo -> keyStoreInfo.setPrivateKey(""));
        return keyStores;
    }

    /**
     * create key store and save
     */
    public KeyStoreInfo createKeyStore(String userName) {
        RspUserInfo rspUserInfo = getSignUserEntity();
        String address = rspUserInfo.getAddress();
        KeyStoreInfo checkAddressExist = keystoreRepository.findByAddress(address);
        if(Objects.nonNull(checkAddressExist)) {
            throw new FrontException(ConstantCode.KEYSTORE_EXISTS);
        }
        KeyStoreInfo checkUserNameExist = keystoreRepository.findByUserName(userName);
        if(Objects.nonNull(checkUserNameExist)) {
            throw new FrontException(ConstantCode.USER_NAME_EXISTS);
        }
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        keyStoreInfo.setAddress(address);
        keyStoreInfo.setPublicKey(rspUserInfo.getPublicKey());
        keyStoreInfo.setUserName(userName);
        keyStoreInfo.setType(KeyTypes.LOCALUSER.getValue());
        return keystoreRepository.save(keyStoreInfo);
    }

    /**
     * deleteKeyStore.
     */
    public void deleteKeyStore(String address) {
        keystoreRepository.delete(address);;
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
        keyStoreInfo.setUserName(userName);
        keyStoreInfo.setPrivateKey(privateKey);
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
            // webase-sign api(v1.3.0) support
            params.setEncryptType(EncryptType.encryptType);
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
     * @return
     */
    public RspUserInfo getSignUser() {
        try {
            RspUserInfo rspUserInfo = new RspUserInfo();
            String url = String.format(Constants.WEBASE_SIGN_USER_URI, constants.getKeyServer(),
                    EncryptType.encryptType);
            log.info("getSignData url:{}", url);
            BaseResponse response =
                    restTemplate.getForObject(url, BaseResponse.class);
            log.info("getSignData response:{}", JSON.toJSONString(response));
            if (response.getCode() == 0) {
                rspUserInfo = CommonUtils.object2JavaBean(response.getData(), RspUserInfo.class);
            }
            return rspUserInfo;
        } catch (Exception e) {
            log.error("getSignData exception", e);
            throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
        }
    }

    public RspUserInfo getSignUserEntity() {
        try {
            // webase-sign api(v1.3.0) support
            RspUserInfo rspUserInfo = new RspUserInfo();
            String url = String.format(Constants.WEBASE_SIGN_USER_URI, constants.getKeyServer(), EncryptType.encryptType);
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
     * ===================== deprecated ==============
     */

    /**
     * get credential to send transaction
     * 2019/11/26 support guomi
     */
    @Deprecated
    public Credentials getCredentials(String user) throws FrontException {
        String privateKey = Optional.ofNullable(getPrivateKey(user)).orElse(null);
        if (StringUtils.isBlank(privateKey)) {
            log.warn("fail getCredentials. user:{} privateKey is null", user);
            throw new FrontException(ConstantCode.PRIVATEKEY_IS_NULL);
        }
        return GenCredential.create(privateKey);
    }
    /**
     * get PrivateKey.
     * default use aes encrypt
     * @param user userId or userAddress.
     */
    @Deprecated
    public String getPrivateKey(String user) {
        boolean useAes = true;
        // get privateKey from map (in memory, not db)
        String key_of_user = user + "_" + useAes;
        if (PRIVATE_KEY_MAP.containsKey(key_of_user)) {
            return PRIVATE_KEY_MAP.get(key_of_user);
        }

        // get from local db
        KeyStoreInfo keyStoreInfoLocal = keystoreRepository.findByAddress(user);
        // no need for if(useAes), default aesDecrypt before saving in front db
        if (keyStoreInfoLocal != null) {
            //get privateKey by address
            return aesUtils.aesDecrypt(keyStoreInfoLocal.getPrivateKey());
        }

        //get privateKey by userId from nodemgr or webase-sign
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        String[] ipPortArr = constants.getKeyServer().split(",");
        for (String ipPort : ipPortArr) {
            try {
                String url = String.format(Constants.MGR_PRIVATE_KEY_URI, ipPort.trim(), user);
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

        String private_key = aesUtils.aesDecrypt(keyStoreInfo.getPrivateKey());

        if (StringUtils.isNotBlank(private_key)) {
            PRIVATE_KEY_MAP.put(key_of_user, private_key);
        }

        return private_key;
    }

    /**
     * import keystore info from pem file's content
     * @param pemContent
     * @param userType local
     * @param userName
     * @return
     */
//    @Deprecated
//    public KeyStoreInfo importKeyStoreFromPem(String pemContent, int userType,  String userName) {
//        PEMManager pemManager = new PEMManager();
//        String privateKey;
//        try {
//            pemManager.load(new ByteArrayInputStream(pemContent.getBytes()));
//            privateKey = Numeric.toHexStringNoPrefix(pemManager.getECKeyPair().getPrivateKey());
//        }catch (Exception e) {
//            log.error("importKeyStoreFromPem error:[]", e);
//            throw new FrontException(ConstantCode.PEM_CONTENT_ERROR);
//        }
//        // to store
//        getKeyStoreFromPrivateKey(privateKey, userType, userName);
//        return null;
//    }

}

