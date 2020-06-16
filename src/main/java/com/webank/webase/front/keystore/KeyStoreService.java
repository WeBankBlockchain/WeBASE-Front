/**
 * Copyright 2014-2020 the original author or authors.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.KeyTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.keystore.entity.*;
import com.webank.webase.front.util.AesUtils;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.P12Manager;
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
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
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
     * get local user KeyStores with privateKey
     * without external user
     */
    public List<KeyStoreInfo> getLocalKeyStoreList() {
        Sort sort = new Sort(Sort.Direction.ASC, "userName");
        List<KeyStoreInfo> keyStores = keystoreRepository.findAll(
                (Root<KeyStoreInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
                    Predicate predicate = criteriaBuilder.equal(root.get("type"), KeyTypes.LOCALUSER.getValue());
                    return criteriaBuilder.and(predicate);
                }, sort);
        // return local KeyStore with decrypted privateKey
        keyStores.forEach(info -> {
            String realPrivateKey = aesUtils.aesDecrypt(info.getPrivateKey());
            info.setPrivateKey(realPrivateKey);
        });
        return keyStores;
    }

    /**
     * create key store locally and save
     * with private key
     */
    public KeyStoreInfo createKeyStoreLocally(String userName) {
        log.info("start createKeyStore. userName:{}", userName);
        checkUserNameAndTypeNotExist(userName, KeyTypes.LOCALUSER.getValue());
        // create keyPair(support guomi)
        KeyStoreInfo keyStoreInfo;
        try {
            ECKeyPair keyPair = GenCredential.createKeyPair();
            keyStoreInfo = keyPair2KeyStoreInfo(keyPair, userName);
        } catch (Exception e) {
            log.error("fail createKeyStore.", e);
            throw new FrontException("create keyInfo failed");
        }
        keyStoreInfo.setType(KeyTypes.LOCALUSER.getValue());
        String realPrivateKey = keyStoreInfo.getPrivateKey();
        keyStoreInfo.setPrivateKey(aesUtils.aesEncrypt(realPrivateKey));
        return keystoreRepository.save(keyStoreInfo);
    }


    /**
     * create keystore by webase-sign
     * without private key
     * @param signUserId
     * @param appId
     * @return KeyStoreInfo
     */
    public KeyStoreInfo createKeyStoreWithSign(String signUserId, String appId) {
        // get from sign
        RspUserInfo rspUserInfo = getSignUserEntity(signUserId, appId);
        return saveSignKeyStore(rspUserInfo);
    }

    /**
     * save rspUserInfo as KeyStoreInfo
     * @param rspUserInfo
     * @return KeyStoreInfo
     */
    private KeyStoreInfo saveSignKeyStore(RspUserInfo rspUserInfo) {
        String address = rspUserInfo.getAddress();
        if (StringUtils.isEmpty(address)) {
            throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
        }
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
        keyStoreInfo.setPrivateKey(privateKey);
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
                    new HttpEntity<String>(JsonUtils.toJSONString(params), headers);
            BaseResponse response =
                    restTemplate.postForObject(url, formEntity, BaseResponse.class);
            log.info("getSignData response:{}", JsonUtils.toJSONString(response));
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
     * get signUserId by address
     * @param signUserId
     * @return user address
     */
    public String getAddressBySignUserId(String signUserId) {
        KeyStoreInfo keyStoreInfo = keystoreRepository.findBySignUserId(signUserId);
        if (Objects.isNull(keyStoreInfo)) {
            return null;
        }
        return keyStoreInfo.getAddress();
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
     * @param user  userAddress.
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
     * check userName and userType not exist
     */
    private void checkUserNameAndTypeNotExist(String userName, Integer userType) {
        // check keyStoreInfoLocal
        KeyStoreInfo keyStoreInfoLocal = keystoreRepository.findByUserNameAndType(userName,
                userType);
        if (Objects.nonNull(keyStoreInfoLocal)) {
            log.error("fail checkUserNameAndTypeNotExist. user name already exists.");
            throw new FrontException(ConstantCode.USER_NAME_EXISTS);
        }
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
        // to store local
        return importFromPrivateKey(privateKey, userName);
    }

    /**
     * import keystore info from p12 file input stream and its password
     * @param file
     * @param password
     * @param userName
     * @return KeyStoreInfo
     */
    public KeyStoreInfo importKeyStoreFromP12(MultipartFile file, String password, String userName) {
        P12Manager p12Manager = new P12Manager();
        String privateKey;
        try {
            // manually set password and load
            p12Manager.setPassword(password);
            p12Manager.load(file.getInputStream(), password);
            privateKey = Numeric.toHexStringNoPrefix(p12Manager.getECKeyPair().getPrivateKey());
        } catch (IOException e) {
            log.error("importKeyStoreFromP12 error:[]", e);
            if (e.getMessage().contains("password")) {
                throw new FrontException(ConstantCode.P12_PASSWORD_ERROR);
            }
            throw new FrontException(ConstantCode.P12_FILE_ERROR);
        } catch (Exception e) {
            log.error("importKeyStoreFromP12 error:[]", e);
            throw new FrontException(ConstantCode.P12_FILE_ERROR.getCode(), e.getMessage());
        }
        // to store local
        return importFromPrivateKey(privateKey, userName);
    }

    /**
     * save LOCAL_USER key store by private key
     * @param privateKey
     * @param userName
     * @return KeyStoreInfo local user
     */
    public KeyStoreInfo importFromPrivateKey(String privateKey, String userName) {
        // check name
        checkUserNameAndTypeNotExist(userName, KeyTypes.LOCALUSER.getValue());
        // to store locally
        ECKeyPair keyPair = GenCredential.createKeyPair(privateKey);
        if (keyPair == null) {
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
        KeyStoreInfo keyStoreInfo = keyPair2KeyStoreInfo(keyPair, userName);
        keyStoreInfo.setType(KeyTypes.LOCALUSER.getValue());
        String realPrivateKey = keyStoreInfo.getPrivateKey();
        keyStoreInfo.setPrivateKey(aesUtils.aesEncrypt(realPrivateKey));
        return keystoreRepository.save(keyStoreInfo);
    }

    /**
     * import private key to sign
     * @param privateKeyEncoded
     * @param signUserId
     * @param appId
     * @return KeyStoreInfo
     */
    public KeyStoreInfo importPrivateKeyToSign(String privateKeyEncoded, String signUserId, String appId) {
        // post private and save in sign
        RspUserInfo rspUserInfo = getSignUserEntity(privateKeyEncoded, signUserId, appId);
        // save in local as external
        KeyStoreInfo keyStoreInfo = saveSignKeyStore(rspUserInfo);
        return keyStoreInfo;
    }


    /**
     * request (get) user from webase-sign api(v1.3.0+)
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
            log.info("getSignUserEntity response:{}", JsonUtils.toJSONString(baseResponse));
            if (baseResponse.getCode() == 0) {
                rspUserInfo = CommonUtils.object2JavaBean(baseResponse.getData(), RspUserInfo.class);
            }
            return rspUserInfo;
        } catch (ResourceAccessException ex) {
            log.error("fail restTemplateExchange", ex);
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        } catch (HttpStatusCodeException e) {
            JsonNode error = JsonUtils.stringToJsonNode(e.getResponseBodyAsString());
            log.error("http request fail. error:{}", JsonUtils.toJSONString(error));
            try {
                // if return 404, no code or errorMessage
                int code = error.get("code").intValue();
                String errorMessage = error.get("errorMessage").asText();
                throw new FrontException(code, errorMessage);
            } catch (Exception ex) {
                throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
            }
        } catch (Exception e) {
            log.error("getSignUserEntity exception", e);
            throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
        }
    }

    /**
     * request(post) sign to import private key
     * @param signUserId
     * @param appId
     * @param privateKeyEncoded base64 encoded
     * @return RspUserInfo
     */
    public RspUserInfo getSignUserEntity(String privateKeyEncoded, String signUserId, String appId) {
        try {
            RspUserInfo rspUserInfo = new RspUserInfo();
            String urlSpilt = constants.WEBASE_SIGN_USER_URI.split("\\?")[0];
            String url = String.format(urlSpilt, constants.getKeyServer());
            log.info("getSignUserEntity url:{}", url);
            Map<String, Object> params = new HashMap<>();
            params.put("privateKey", privateKeyEncoded);
            params.put("signUserId", signUserId);
            params.put("appId", appId);
            params.put("encryptType", EncryptType.encryptType);
            HttpEntity entity = CommonUtils.buildHttpEntity(params);

            ResponseEntity<BaseResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, BaseResponse.class);
            BaseResponse baseResponse = response.getBody();
            log.info("getSignUserEntity response:{}", JsonUtils.toJSONString(baseResponse));
            if (baseResponse.getCode() == 0) {
                rspUserInfo = CommonUtils.object2JavaBean(baseResponse.getData(), RspUserInfo.class);
            } else {
                log.error("getSignUserEntity fail for:{}", baseResponse.getMessage());
                throw new FrontException(ConstantCode.PRIVATE_KEY_DECODE_FAIL.getCode(), baseResponse.getMessage());
            }
            return rspUserInfo;
        } catch (ResourceAccessException ex) {
            log.error("getSignUserEntity fail restTemplateExchange", ex);
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        } catch (HttpStatusCodeException e) {
            JsonNode error = JsonUtils.stringToJsonNode(e.getResponseBodyAsString());
            log.error("getSignUserEntity http request fail. error:{}", JsonUtils.toJSONString(error));
            try {
                // if return 404, no code or errorMessage
                int code = error.get("code").intValue();
                String errorMessage = error.get("errorMessage").asText();
                throw new FrontException(code, errorMessage);
            } catch (NullPointerException ex) {
                throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
            }
        } catch (FrontException e) {
            throw e;
        } catch (Exception e) {
            log.error("getSignUserEntity exception", e);
            throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
        }
    }
}

