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
import com.webank.webase.front.keystore.entity.EncodeInfo;
import com.webank.webase.front.keystore.entity.KeyStoreInfo;
import com.webank.webase.front.keystore.entity.MessageHashInfo;
import com.webank.webase.front.keystore.entity.RspMessageHashSignature;
import com.webank.webase.front.keystore.entity.RspUserInfo;
import com.webank.webase.front.keystore.entity.SignInfo;
import com.webank.webase.front.util.AesUtils;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.exceptions.LoadKeyStoreException;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.keystore.KeyTool;
import org.fisco.bcos.sdk.crypto.keystore.P12KeyStore;
import org.fisco.bcos.sdk.crypto.keystore.PEMKeyStore;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.crypto.signature.SM2SignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private KeystoreRepository keystoreRepository;
    @Autowired
    @Qualifier(value = "common")
    private CryptoSuite cryptoSuite;

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
        CryptoKeyPair keyPair = cryptoSuite.createKeyPair();
        if (keyPair == null) {
            log.error("fail createKeyStore for null key pair");
            throw new FrontException(ConstantCode.WEB3J_CREATE_KEY_PAIR_NULL);
        }
        keyStoreInfo = keyPair2KeyStoreInfo(keyPair, userName);
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
    public KeyStoreInfo createKeyStoreWithSign(String signUserId, String appId, boolean returnPrivateKey) {
        // get from sign
        RspUserInfo rspUserInfo = getSignUserEntity(signUserId, appId, returnPrivateKey);
        KeyStoreInfo keyStoreInfo = saveSignKeyStore(rspUserInfo);
        if (returnPrivateKey == true) {
            keyStoreInfo.setPrivateKey(Base64.getEncoder().encodeToString(
                    aesUtils.aesDecrypt(rspUserInfo.getPrivateKey()).getBytes()));
        }
        return keyStoreInfo;
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
    private KeyStoreInfo keyPair2KeyStoreInfo(CryptoKeyPair keyPair, String userName) {
        String publicKey = keyPair.getHexPublicKey();
        String privateKey = keyPair.getHexPrivateKey();
        String address = keyPair.getAddress();
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
    public CryptoKeyPair getCredentialsForQuery() {
        log.debug("start getCredentialsForQuery. ");
        // create keyPair(support guomi)
        CryptoKeyPair keyPair = cryptoSuite.createKeyPair();
        if (keyPair == null) {
            log.error("create random Credentials for query failed for null key pair");
            throw new FrontException(ConstantCode.WEB3J_CREATE_KEY_PAIR_NULL);
        }
        // keyPair2KeyStoreInfo(keyPair, "");
        return keyPair;
    }

    public KeyStoreInfo getKeyStoreInfoForQuery() {
        log.debug("start getKeyStoreInfoForQuery. ");
        // create keyPair(support guomi)
        CryptoKeyPair keyPair = this.getCredentialsForQuery();
        return keyPair2KeyStoreInfo(keyPair, "");
    }

    /**
     * getSignData from sign service. (webase-sign)
     * @param params params
     * @return
     */
    public String getSignData(EncodeInfo params) throws FrontException {
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
            } else {
                log.error("getSignData fail for error response:{}", response);
                throw new FrontException(response.getCode(), response.getMessage());
            }
            String signDataStr = signInfo.getSignDataStr();
            if (StringUtils.isBlank(signDataStr)) {
                log.warn("get sign data error and get blank string.");
                throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
            }
            return signDataStr;
        } catch (ResourceAccessException ex) {
            log.error("getSignData fail restTemplateExchange", ex);
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        } catch (HttpStatusCodeException e) {
            JsonNode error = JsonUtils.stringToJsonNode(e.getResponseBodyAsString());
            if (error == null) {
                throw e;
            }
            log.error("getSignData http request fail. error:{}", JsonUtils.toJSONString(error));
            // if return 404, no code or errorMessage
            int code = error.get("code").intValue();
            String errorMessage = error.get("errorMessage").asText();
            throw new FrontException(code, errorMessage);
        }
    }

    /**
     * getMessageHashSignData from sign service. (webase-sign)
     * @param params params
     * @return RspMessageHashSignature
     */
    public RspMessageHashSignature getMessageHashSignData(MessageHashInfo params) throws FrontException {
        try {
            String url = String.format(Constants.WEBASE_SIGN_URI, constants.getKeyServer());
            log.info("getSignData url:{}", url);
            HttpHeaders headers = CommonUtils.buildHeaders();
            HttpEntity<String> formEntity =
                    new HttpEntity<String>(JsonUtils.toJSONString(params), headers);
            BaseResponse response =
                    restTemplate.postForObject(url, formEntity, BaseResponse.class);
            log.info("getSignData response:{}", JsonUtils.toJSONString(response));
            SignInfo signInfo = new SignInfo();
            if (response.getCode() == 0) {
                signInfo = CommonUtils.object2JavaBean(response.getData(), SignInfo.class);
            } else {
                log.error("getSignData fail for error response:{}", response);
                throw new FrontException(response.getCode(), response.getMessage());
            }
            String signDataStr = signInfo.getSignDataStr();

            if (StringUtils.isBlank(signDataStr)) {
                log.warn("get sign data error and get blank string.");
                throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
            }
            RspMessageHashSignature rspMessageHashSignature = new RspMessageHashSignature();

            if (cryptoSuite.cryptoTypeConfig == CryptoType.SM_TYPE) {
                SM2SignatureResult signData = (SM2SignatureResult) CommonUtils.stringToSignatureData(signDataStr,
                    cryptoSuite.cryptoTypeConfig);
                // SM2SignatureResult signData = CommonUtils.stringToSM2SignatureData(signDataStr);
                rspMessageHashSignature.setR(Numeric.toHexString(signData.getR()));
                rspMessageHashSignature.setS(Numeric.toHexString(signData.getS()));
                rspMessageHashSignature.setV(new Byte("0"));
                rspMessageHashSignature.setP(Numeric.toHexString(signData.getPub()));
            } else {
                ECDSASignatureResult signData = (ECDSASignatureResult) CommonUtils.stringToSignatureData(signDataStr,
                    cryptoSuite.cryptoTypeConfig);
                // ECDSASignatureResult signData = CommonUtils.stringToECDSASignatureData(signDataStr);
                rspMessageHashSignature.setR(Numeric.toHexString(signData.getR()));
                rspMessageHashSignature.setS(Numeric.toHexString(signData.getS()));
                rspMessageHashSignature.setV(signData.getV());
                rspMessageHashSignature.setP(null);
            }
            return rspMessageHashSignature;
        } catch (ResourceAccessException ex) {
            log.error("getSignData fail restTemplateExchange", ex);
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        } catch (HttpStatusCodeException e) {
            JsonNode error = JsonUtils.stringToJsonNode(e.getResponseBodyAsString());
            if (error == null) {
                throw e;
            }
            log.error("getSignData http request fail. error:{}", JsonUtils.toJSONString(error));
            // if return 404, no code or errorMessage
            int code = error.get("code").intValue();
            String errorMessage = error.get("errorMessage").asText();
            throw new FrontException(code, errorMessage);
        }
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
    public CryptoKeyPair getCredentials(String user) throws FrontException {
        String privateKey = getPrivateKey(user);
        return cryptoSuite.createKeyPair(privateKey);
    }

    /**
     * get PrivateKey.
     * default use aes encrypt
     * @param user  userAddress.
     */
    public String getPrivateKey(String user) {

        // get from local db
        KeyStoreInfo keyStoreInfoLocal = keystoreRepository.findByAddress(user);
        if (Objects.isNull(keyStoreInfoLocal) || StringUtils.isBlank(keyStoreInfoLocal.getPrivateKey())) {
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
        PEMKeyStore pemManager = new PEMKeyStore(new ByteArrayInputStream(pemContent.getBytes()));
        String privateKey = KeyTool.getHexedPrivateKey(pemManager.getKeyPair().getPrivate());
        // throw new FrontException(ConstantCode.PEM_CONTENT_ERROR);
        // to store local
        return importFromPrivateKey(privateKey, userName);
    }

    /**
     * import keystore info from p12 file input stream and its password
     * @param file
     * @param p12PasswordEncoded
     * @param userName
     * @return KeyStoreInfo
     */
    public KeyStoreInfo importKeyStoreFromP12(MultipartFile file, String p12PasswordEncoded, String userName) {
        // decode p12 password
        String password;
        try {
            password = new String(Base64.getDecoder().decode(p12PasswordEncoded));
        } catch (Exception e) {
            log.error("decode password error:[]", e);
            throw new FrontException(ConstantCode.PRIVATE_KEY_DECODE_FAIL);
        }

        String privateKey;
        try {
            // manually set password and load
            P12KeyStore p12Manager = new P12KeyStore(file.getInputStream(), password);
            privateKey = KeyTool.getHexedPrivateKey(p12Manager.getKeyPair().getPrivate());
        }  catch (IOException e) {
            log.error("importKeyStoreFromP12 file not found error:[]", e);
            throw new FrontException(ConstantCode.P12_FILE_ERROR);
        } catch (LoadKeyStoreException e) {
            log.error("importKeyStoreFromP12 error:[]", e);
            if (e.getMessage().contains("password")) {
                throw new FrontException(ConstantCode.P12_PASSWORD_ERROR);
            }
            throw new FrontException(ConstantCode.P12_FILE_ERROR);
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
        CryptoKeyPair keyPair = cryptoSuite.createKeyPair(privateKey);
        if (keyPair == null) {
            log.error("importFromPrivateKey get null keyPair");
            throw new FrontException(ConstantCode.PRIVATE_KEY_DECODE_FAIL);
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
        RspUserInfo rspUserInfo = postForSignUserEntity(privateKeyEncoded, signUserId, appId);
        // save in local as external
        KeyStoreInfo keyStoreInfo = saveSignKeyStore(rspUserInfo);
        return keyStoreInfo;
    }


    /**
     * request (get) user from webase-sign api(v1.3.0+)
     * @param signUserId unique user id to call webase-sign
     * @return
     */
    public RspUserInfo getSignUserEntity(String signUserId, String appId, boolean returnPrivateKey) {
        // webase-sign api(v1.3.0) support
        String url = String.format(Constants.WEBASE_SIGN_USER_URI, constants.getKeyServer(),
                cryptoSuite.cryptoTypeConfig, signUserId, appId, returnPrivateKey);
        log.info("getSignUserEntity url:{}", url);
        BaseResponse baseResponse = getForEntity(url);
        log.info("getSignUserEntity response:{}", JsonUtils.toJSONString(baseResponse));
        RspUserInfo rspUserInfo;
        if (baseResponse.getCode() == 0) {
            rspUserInfo = CommonUtils.object2JavaBean(baseResponse.getData(), RspUserInfo.class);
        } else {
            log.error("getSignUserEntity fail for:{}", baseResponse.getMessage());
            throw new FrontException(baseResponse.getCode(), baseResponse.getMessage());
        }
        return rspUserInfo;
    }
    
    /**
     * request (get) user info from webase-sign api(v1.5.0+)
     * @param signUserId unique user id to call webase-sign
     * @return
     */
    public RspUserInfo getUserInfoWithSign(String signUserId, boolean returnPrivateKey) {
        // webase-sign api(v1.4.4) support
        String url = String.format(Constants.WEBASE_SIGN_USER_INFO_URI, constants.getKeyServer(),
                signUserId, returnPrivateKey);
        log.info("getUserInfoWithSign url:{}", url);
        BaseResponse baseResponse = getForEntity(url);
        log.info("getUserInfoWithSign response:{}", JsonUtils.toJSONString(baseResponse));
        RspUserInfo rspUserInfo;
        if (baseResponse.getCode() == 0) {
            rspUserInfo = CommonUtils.object2JavaBean(baseResponse.getData(), RspUserInfo.class);
        } else {
            log.error("getUserInfoWithSign fail for:{}", baseResponse.getMessage());
            throw new FrontException(baseResponse.getCode(), baseResponse.getMessage());
        }
        rspUserInfo.setPrivateKey(Base64.getEncoder().encodeToString(
                    aesUtils.aesDecrypt(rspUserInfo.getPrivateKey()).getBytes()));
        return rspUserInfo;
    }

    /**
     * request(post) sign to import private key
     * @param signUserId
     * @param appId
     * @param privateKeyEncoded base64 encoded
     * @return RspUserInfo
     */
    public RspUserInfo postForSignUserEntity(String privateKeyEncoded, String signUserId, String appId) {
        String urlSpilt = Constants.WEBASE_SIGN_USER_URI.split("\\?")[0];
        String url = String.format(urlSpilt, constants.getKeyServer());
        log.info("getSignUserEntity url:{}", url);
        Map<String, Object> params = new HashMap<>();
        params.put("privateKey", privateKeyEncoded);
        params.put("signUserId", signUserId);
        params.put("appId", appId);
        params.put("encryptType", cryptoSuite.cryptoTypeConfig);

        BaseResponse baseResponse = postForEntity(url, params);

        log.info("postSignUserEntity response:{}", JsonUtils.toJSONString(baseResponse));
        RspUserInfo rspUserInfo;
        if (baseResponse.getCode() == 0) {
            rspUserInfo = CommonUtils.object2JavaBean(baseResponse.getData(), RspUserInfo.class);
        } else {
            log.error("getSignUserEntity fail for:{}", baseResponse.getMessage());
            throw new FrontException(baseResponse.getCode(), baseResponse.getMessage());
        }
        return rspUserInfo;

    }

    /**
     * post from front for entity.
     */
    private BaseResponse getForEntity(String url) {
        BaseResponse response = restTemplateExchange(url, HttpMethod.GET, null, BaseResponse.class);
        // check return null
        if (response == null) {
            log.error("getForEntity fail, return null");
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        }
        return response;
    }

    /**
     * post from front for entity.
     */
    private BaseResponse postForEntity(String url, Object params) {
        BaseResponse response = restTemplateExchange(url, HttpMethod.POST, params, BaseResponse.class);
        // check return null
        if (response == null) {
            log.error("postForEntity fail, return null");
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        }
        return response;
    }

    private <T> T restTemplateExchange(String url, HttpMethod method, Object param, Class<T> clazz) {
        try{
            log.info("restTemplateExchange url:{}, param:{}", url, param);
            HttpEntity entity = CommonUtils.buildHttpEntity(param);
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
            return response.getBody();
        } catch (ResourceAccessException ex) {
            log.error("getSignUserEntity fail restTemplateExchange", ex);
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        } catch (HttpStatusCodeException e) {
            JsonNode error = JsonUtils.stringToJsonNode(e.getResponseBodyAsString());
            if (error == null) {
                throw e;
            }
            log.error("getSignUserEntity http request fail. error:{}", JsonUtils.toJSONString(error));
            // if return 404, no code or errorMessage
            int code = error.get("code").intValue();
            String errorMessage = error.get("errorMessage").asText();
            throw new FrontException(code, errorMessage);
        }
    }
}

