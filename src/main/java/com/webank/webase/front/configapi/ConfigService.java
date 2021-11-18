/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.configapi;

import static com.webank.webase.front.cert.FrontCertService.*;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.Web3Config;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.configapi.entity.ConfigInfo;
import com.webank.webase.front.configapi.entity.ReqSdkConfig;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.config.model.CryptoMaterialConfig;
import org.fisco.bcos.sdk.config.model.NetworkConfig;
import org.fisco.bcos.sdk.config.model.ThreadPoolConfig;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * handle config of sdk instance to build BcosSDK
 */
@Slf4j
@Service
public class ConfigService {
    @Autowired
    private Web3Config web3Config;
    @Autowired
    private Stack<BcosSDK> bcosSDKs;
    @Autowired
    private ConfigInfoRepository configInfoRepository;

    public synchronized void updateBcosSDKPeers(ReqSdkConfig param) {
        log.info("updateBcosSDKPeers param:{}", param);
        List<String> newPeers = param.getPeers();
        if (newPeers == null || newPeers.isEmpty()) {
            throw new FrontException(ConstantCode.PARAM_ERROR_EMPTY_PEERS);
        }
        List<String> oldPeers = this.getSdkPeers();
        log.info("updateBcosSDKPeers oldPeers in db:{}", oldPeers);

        boolean isSame = CommonUtils.compareStrList(oldPeers, newPeers);
        if (isSame) {
            log.warn("same peers with old peers in BcosSDK!");
            throw new FrontException(ConstantCode.SAME_SDK_PEERS_ERROR);
        }
        // check peers connected or not
        for (String nPeer : newPeers) {
            String[] ipPortArr = nPeer.split(":");
            if (ipPortArr.length != 2) {
                log.error("new peer format not match [ip:port], nPeer:{}", nPeer);
                throw new FrontException(ConstantCode.PARAM_ERROR_EMPTY_PEERS);
            }
            boolean connected = CommonUtils.checkConnect(ipPortArr[0], Integer.parseInt(ipPortArr[1]));
            if (!connected) {
                log.error("try to connect to new peer failed, nPeer:{}", nPeer);
                throw new FrontException(ConstantCode.CONNECT_TO_NEW_PEERS_FAILED);
            }
        }

        // todo check certificate

        log.info("updateBcosSDKPeers newPeers:{},oldPeers:{}", newPeers, oldPeers);
        try {
            this.buildBcosSDK();
        } catch (ConfigException e) {
            log.error("buildBcosSDK failed:[]", e);
            throw new FrontException(ConstantCode.BUILD_SDK_WITH_NEW_PEERS_FAILED);
        }
        // todo save config to db
        this.updatePeersConfig(newPeers);

    }


    /**
     * update and save
     * @param peers
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfigInfo updatePeersConfig(List<String> peers) {
        log.info("savePeersConfig peers:{}", peers);
        ConfigInfo oldConfig = configInfoRepository.findByTypeAndKey("sdk", "peer");
        oldConfig.setValue(JsonUtils.objToString(peers));
        log.info("savePeersConfig configInfo:{}", oldConfig);
        return configInfoRepository.save(oldConfig);
    }

    public static final String TYPE_SDK = "sdk";
    public static final String SDK_PEERS = "peers";
    public static final String SDK_USE_SM_SSL = "useSmSsl";
    public boolean getSdkUseSmSsl() {
        ConfigInfo configInfo = configInfoRepository.findByTypeAndKey(TYPE_SDK, SDK_USE_SM_SSL);
        boolean useSmSsl = Boolean.parseBoolean(configInfo.getValue());
        log.info("getSdkUseSmSsl :{}", useSmSsl);
        return useSmSsl;
    }

    /**
     * todo check null configInfo
     * @return
     */
    public List<String> getSdkPeers() {
        ConfigInfo configInfo = configInfoRepository.findByTypeAndKey(TYPE_SDK, SDK_PEERS);
        if (configInfo == null) {
            throw new FrontException(ConstantCode.PARAM_ERROR_EMPTY_PEERS);
        }
        List<String> dbPeers = JsonUtils.toJavaObjectList(configInfo.getValue(), String.class);
        log.info("getSdkPeers :{}", dbPeers);
        return dbPeers;
    }

    public String getSdkCertStr(String certName) {
        ConfigInfo configInfo = configInfoRepository.findByTypeAndKey(TYPE_SDK, certName);
        String caCertBase64 = configInfo.getValue();
        log.info("getSdkCertStr :{}", caCertBase64);
        return caCertBase64;
    }

    public void buildBcosSDK() throws ConfigException {
        ConfigOption configOption = initConfigOption();
        // init bcosSDK
        BcosSDK bcosSDK = new BcosSDK(configOption);
//        try{
//
//        } catch (Exception e) {
//            log.error("buildBcosSDK failed:[]", e);
//            throw new FrontException(ConstantCode.BUILD_SDK_WITH_NEW_PEERS_FAILED);
//        }
        if (bcosSDKs.isEmpty()) {
            bcosSDKs.push(bcosSDK);
        } else {
            bcosSDKs.pop();
            bcosSDKs.push(bcosSDK);
        }
    }

    public ConfigOption initConfigOption() throws ConfigException {
        // network
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setPeers(this.getSdkPeers());
        networkConfig.setDefaultGroup("group");
        networkConfig.setTimeout(30000); // 30s
        // thread pool
        String threadPoolSize = web3Config.getThreadPoolSize();
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig(Integer.parseInt(threadPoolSize));


        // set peers, thread pool

        // ssl type, cert config
        CryptoMaterialConfig cryptoMaterialConfig = new CryptoMaterialConfig();
        boolean useSmSsl = this.getSdkUseSmSsl();
        cryptoMaterialConfig.setUseSmCrypto(useSmSsl);
        if (useSmSsl) {
            cryptoMaterialConfig.setCaCert(this.getSdkCertStr(frontGmSdkCaCrt));
            cryptoMaterialConfig.setSdkCert(this.getSdkCertStr(frontGmSdkNodeCrt));
            cryptoMaterialConfig.setSdkPrivateKey(this.getSdkCertStr(frontGmSdkNodeKey));
            cryptoMaterialConfig.setEnSdkCert(this.getSdkCertStr(frontGmEnSdkNodeCrt));
            cryptoMaterialConfig.setEnSdkPrivateKey(this.getSdkCertStr(frontGmEnSdkNodeKey));
        } else {
            cryptoMaterialConfig.setCaCert(this.getSdkCertStr(frontSdkCaCrt));
            cryptoMaterialConfig.setSdkCert(this.getSdkCertStr(frontSdkNodeCrt));
            cryptoMaterialConfig.setSdkPrivateKey(this.getSdkCertStr(frontSdkNodeKey));
        }
        // set crypto
        ConfigOption configOption = new ConfigOption();
        configOption.setCryptoMaterialConfig(cryptoMaterialConfig);
        configOption.setThreadPoolConfig(threadPoolConfig);
        configOption.setNetworkConfig(networkConfig);
        return configOption;
    }

}
