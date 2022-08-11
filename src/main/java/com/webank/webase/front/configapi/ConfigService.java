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

import static com.webank.webase.front.cert.FrontCertService.frontGmEnSdkNodeCrt;
import static com.webank.webase.front.cert.FrontCertService.frontGmEnSdkNodeKey;
import static com.webank.webase.front.cert.FrontCertService.frontGmSdkCaCrt;
import static com.webank.webase.front.cert.FrontCertService.frontGmSdkNodeCrt;
import static com.webank.webase.front.cert.FrontCertService.frontGmSdkNodeKey;
import static com.webank.webase.front.cert.FrontCertService.frontSdkCaCrt;
import static com.webank.webase.front.cert.FrontCertService.frontSdkNodeCrt;
import static com.webank.webase.front.cert.FrontCertService.frontSdkNodeKey;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.Web3Config;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.configapi.entity.ConfigInfo;
import com.webank.webase.front.configapi.entity.ReqSdkConfig;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.util.NetUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.config.model.CryptoMaterialConfig;
import org.fisco.bcos.sdk.v3.config.model.NetworkConfig;
import org.fisco.bcos.sdk.v3.config.model.ThreadPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
//    @Autowired
//    @Lazy
//    private Stack<BcosSDK> bcosSDKs;
    @Autowired
    private ConfigInfoRepository configInfoRepository;

    public static final String TYPE_SDK = "sdk";
    public static final String SDK_PEERS = "peers";
    public static final String SDK_USE_SM_SSL = "useSmSsl";

    public Map<String, ConfigInfo> getConfigInfoSdk() {
        List<ConfigInfo> oldConfig = configInfoRepository.findByType(TYPE_SDK);
        log.info("getConfigInfoSdk oldConfig:{}", oldConfig);
        Map<String, ConfigInfo> configInfoMap = new HashMap<>();
        for (ConfigInfo info : oldConfig) {
            configInfoMap.put(info.getKey(), info);
        }
        return configInfoMap;
    }


    public synchronized void configBcosSDK(ReqSdkConfig param) {
        log.info("updateBcosSDKPeers param:{}", param);
        List<String> newPeers = param.getPeers();
        if (newPeers == null || newPeers.isEmpty()) {
            throw new FrontException(ConstantCode.PARAM_ERROR_EMPTY_PEERS);
        }
        ConfigInfo peersConfig = configInfoRepository.findByTypeAndKey(TYPE_SDK, SDK_PEERS);
        List<String> oldPeers = peersConfig != null
            ? JsonUtils.toJavaObjectList(peersConfig.getValue(), String.class) : new ArrayList<>();
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
            String ip = ipPortArr[0];
            if (!NetUtils.ipv4Valid(ip)) {
                throw new FrontException(ConstantCode.IP_FORMAT_ERROR);
            }
            boolean connected = CommonUtils.checkConnect(ipPortArr[0], Integer.parseInt(ipPortArr[1]));
            if (!connected) {
                log.error("try to connect to new peer failed, nPeer:{}", nPeer);
                throw new FrontException(ConstantCode.CONNECT_TO_NEW_PEERS_FAILED);
            }
        }

        // todo check certificate

        log.info("updateBcosSDKPeers newPeers:{},oldPeers:{}", newPeers, oldPeers);
        this.refreshSDKStack(param); //todo
        // build成功再save， todo save后在初始化的时候尝试自动加载
        // todo save config to db
        this.saveConfig(param);

    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveConfig(ReqSdkConfig param) {
        log.info("saveConfig param:{}", param);
        // save peers
        this.updatePeersConfig(param.getPeers());
        // save certs
        this.updateCertConfig(param);
        // save gm
        this.updateSmSslConfig(param.getUseSmSsl());
        log.info("end saveConfig");
    }

    public void refreshSDKStack(ReqSdkConfig param) {
        ConfigOption configOption = initConfigOption(param);
        // init bcosSDK
        BcosSDK bcosSDK = new BcosSDK(configOption);
        log.info("refreshSDKStack bcosSDK:{}", bcosSDK);
//        if (bcosSDKs.isEmpty()) {
//            bcosSDKs.push(bcosSDK);
//        } else {
//            bcosSDKs.pop(); //todo 调用bcosSDK的stop
//            bcosSDKs.push(bcosSDK);
//        }
    }


    /**
     * update and save
     * @param peers
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfigInfo updatePeersConfig(List<String> peers) {
        log.info("savePeersConfig peers:{}", peers);
        ConfigInfo oldConfig = configInfoRepository.findByTypeAndKey(TYPE_SDK, SDK_PEERS);
        if (oldConfig == null) {
            oldConfig = new ConfigInfo(TYPE_SDK, SDK_PEERS, JsonUtils.objToString(peers));
        }
        log.info("savePeersConfig configInfo:{}", oldConfig);
        return configInfoRepository.save(oldConfig);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfigInfo updateSmSslConfig(boolean useSmSsl) {
        log.info("savePeersConfig useSmSsl:{}", useSmSsl);
        ConfigInfo oldConfig = configInfoRepository.findByTypeAndKey(TYPE_SDK, SDK_USE_SM_SSL);
        if (oldConfig == null) {
            oldConfig = new ConfigInfo(TYPE_SDK, SDK_USE_SM_SSL, JsonUtils.objToString(useSmSsl));
        }
        log.info("savePeersConfig configInfo:{}", oldConfig);
        return configInfoRepository.save(oldConfig);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateCertConfig(ReqSdkConfig param) {
        log.info("savePeersConfig param:{}", param);
        List<ConfigInfo> configInfos = new ArrayList<>();
        if (param.getUseSmSsl()) {
            ConfigInfo smcaConfig = Optional.ofNullable(configInfoRepository.findByTypeAndKey(TYPE_SDK, frontGmSdkCaCrt))
                .orElse(new ConfigInfo(TYPE_SDK, frontGmSdkCaCrt, JsonUtils.objToString(param.getSmCaCertStr())));
            ConfigInfo smsdkCrtConfig = Optional.ofNullable(configInfoRepository.findByTypeAndKey(TYPE_SDK, frontGmSdkNodeCrt))
                .orElse(new ConfigInfo(TYPE_SDK, frontGmSdkNodeCrt, JsonUtils.objToString(param.getSmSdkCertStr())));
            ConfigInfo smsdkKeyeyConfig = Optional.ofNullable(configInfoRepository.findByTypeAndKey(TYPE_SDK, frontGmSdkNodeKey))
                .orElse(new ConfigInfo(TYPE_SDK, frontGmSdkNodeKey, JsonUtils.objToString(param.getSmSdkKeyStr())));
            ConfigInfo smEnCrtConfig = Optional.ofNullable(configInfoRepository.findByTypeAndKey(TYPE_SDK, frontGmEnSdkNodeCrt))
                .orElse(new ConfigInfo(TYPE_SDK, frontGmEnSdkNodeCrt, JsonUtils.objToString(param.getSmEnSdkCertStr())));
            ConfigInfo smEnKeyConfig = Optional.ofNullable(configInfoRepository.findByTypeAndKey(TYPE_SDK, frontGmEnSdkNodeKey))
                .orElse(new ConfigInfo(TYPE_SDK, frontGmEnSdkNodeKey, JsonUtils.objToString(param.getSmSdkKeyStr())));
            configInfos.add(smcaConfig);
            configInfos.add(smsdkCrtConfig);
            configInfos.add(smsdkKeyeyConfig);
            configInfos.add(smEnCrtConfig);
            configInfos.add(smEnKeyConfig);
        } else {
            ConfigInfo caConfig = Optional.ofNullable(configInfoRepository.findByTypeAndKey(TYPE_SDK, frontSdkCaCrt))
                .orElse(new ConfigInfo(TYPE_SDK, frontSdkCaCrt, JsonUtils.objToString(param.getCaCertStr())));
            ConfigInfo sdkCrtConfig = Optional.ofNullable(configInfoRepository.findByTypeAndKey(TYPE_SDK, frontSdkNodeCrt))
                .orElse(new ConfigInfo(TYPE_SDK, frontSdkNodeCrt, JsonUtils.objToString(param.getSdkCertStr())));
            ConfigInfo sdkKeyConfig = Optional.ofNullable(configInfoRepository.findByTypeAndKey(TYPE_SDK, frontSdkNodeKey))
                .orElse(new ConfigInfo(TYPE_SDK, frontSdkNodeKey, JsonUtils.objToString(param.getSdkKeyStr())));
            configInfos.add(caConfig);
            configInfos.add(sdkCrtConfig);
            configInfos.add(sdkKeyConfig);
        }

        log.info("savePeersConfig configInfo:{}", configInfos);
        for (ConfigInfo info : configInfos) {
            configInfoRepository.save(info);
        }
    }


    public Boolean getSdkUseSmSsl() {
        ConfigInfo configInfo = configInfoRepository.findByTypeAndKey(TYPE_SDK, SDK_USE_SM_SSL);
        if (configInfo == null) {
            log.warn("getSdkUseSmSsl get null");
            throw new FrontException(ConstantCode.BCOS_SDK_EMPTY);
        }
        boolean useSmSsl = Boolean.parseBoolean(configInfo.getValue());
        log.info("getSdkUseSmSsl :{}", useSmSsl);
        return useSmSsl;
    }

    /**
     * @return
     */
    public List<String> getSdkPeers() {
        ConfigInfo configInfo = configInfoRepository.findByTypeAndKey(TYPE_SDK, SDK_PEERS);
        if (configInfo == null) {
            log.warn("getSdkPeers get null");
            throw new FrontException(ConstantCode.BCOS_SDK_EMPTY);
        }
        List<String> dbPeers = JsonUtils.toJavaObjectList(configInfo.getValue(), String.class);
        log.info("getSdkPeers :{}", dbPeers);
        return dbPeers;
    }

    public String getSdkCertStr(String certName) {
        ConfigInfo configInfo = configInfoRepository.findByTypeAndKey(TYPE_SDK, certName);
        if (configInfo == null) {
            log.warn("getSdkCertStr get null");
            throw new FrontException(ConstantCode.PARAM_ERROR_CERT_EMPTY);
        }
        String caCertBase64 = configInfo.getValue();
        log.info("getSdkCertStr :{}", caCertBase64);
        return caCertBase64;
    }

    public ConfigOption initConfigOption(ReqSdkConfig param) {
        // network
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setPeers(param.getPeers());
        networkConfig.setDefaultGroup("group");
        networkConfig.setTimeout(30000); // 30s
        // thread pool
        String threadPoolSize = web3Config.getThreadPoolSize();
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
        threadPoolConfig.setThreadPoolSize(Integer.parseInt(threadPoolSize));

        // ssl type, cert config
//        CryptoMaterialConfig cryptoMaterialConfig = this.getCryptoMaterialFromDb();
        CryptoMaterialConfig cryptoMaterialConfig = this.loadCryptoMaterial(param);
        this.checkCryptoCertMatch(cryptoMaterialConfig);

        // set crypto
        ConfigOption configOption = new ConfigOption();
        configOption.setCryptoMaterialConfig(cryptoMaterialConfig);
        configOption.setThreadPoolConfig(threadPoolConfig);
        configOption.setNetworkConfig(networkConfig);
        configOption.setJniConfig(configOption.generateJniConfig());
        log.info("initConfigOption configOption :{}", JsonUtils.objToString(configOption));
        return configOption;
    }

    private CryptoMaterialConfig loadCryptoMaterial(ReqSdkConfig param) {
        CryptoMaterialConfig cryptoMaterialConfig = new CryptoMaterialConfig();
        boolean useSmSsl = param.getUseSmSsl();
        cryptoMaterialConfig.setUseSmCrypto(useSmSsl);
        if (useSmSsl) {
            cryptoMaterialConfig.setCaCert(param.getCaCertStr());
            cryptoMaterialConfig.setSdkCert(param.getSdkCertStr());
            cryptoMaterialConfig.setSdkPrivateKey(param.getSdkKeyStr());
            cryptoMaterialConfig.setEnSdkCert(param.getSmEnSdkCertStr());
            cryptoMaterialConfig.setEnSdkPrivateKey(param.getSmEnSdkKeyStr());
        } else {
            cryptoMaterialConfig.setCaCert(param.getCaCertStr());
            cryptoMaterialConfig.setSdkCert(param.getSdkCertStr());
            cryptoMaterialConfig.setSdkPrivateKey(param.getSdkKeyStr());
        }
        return cryptoMaterialConfig;
    }


    /**
     * todo auto try load from db to init bcosSDK when start
     * todo if db's data is null, catch FrontException, skip init,
     * @return
     */
    public ConfigOption initConfigOptionFromDb() throws FrontException {
        // network
        List<String> peers = this.getSdkPeers();

        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setPeers(peers);
        networkConfig.setDefaultGroup("group");
        networkConfig.setTimeout(30000); // 30s
        // thread pool
        String threadPoolSize = web3Config.getThreadPoolSize();
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
        threadPoolConfig.setThreadPoolSize(Integer.parseInt(threadPoolSize));

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
        // todo
        this.checkCryptoCertMatch(cryptoMaterialConfig);

        // set crypto
        ConfigOption configOption = new ConfigOption();
        configOption.setCryptoMaterialConfig(cryptoMaterialConfig);
        configOption.setThreadPoolConfig(threadPoolConfig);
        configOption.setNetworkConfig(networkConfig);
        return configOption;
    }


    // TODO check cryptoMaterialConfig's cert match with each other
    private void checkCryptoCertMatch(CryptoMaterialConfig cryptoMaterialConfig) {

    }
}
