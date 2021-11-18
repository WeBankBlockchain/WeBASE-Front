/*
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
package com.webank.webase.front.base.config;


import com.webank.webase.front.configapi.ConfigInfoRepository;
import com.webank.webase.front.configapi.ConfigService;
import com.webank.webase.front.configapi.entity.ConfigInfo;
import com.webank.webase.front.util.JsonUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.config.model.CryptoMaterialConfig;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * init web3sdk getService.
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "sdk")
public class Web3Config {
    @Autowired
    private ConfigService configService;

//    public String certPath = "conf";
    private String threadPoolSize;

    public static List<String> peers; // ["127.0.0.1:20200","127.0.0.1:20201"]
    public static boolean useSmSsl = false;


    @Bean
    public Stack<BcosSDK> getBcosSDK() {
        Stack<BcosSDK> bcosSDKs = new Stack<>();
        return bcosSDKs;

    }

    /**
     * todo init one-bean and set ConfigOption by api
     * 启动至少配置证书、一个节点的IP PORT
     * todo DB保存ip配置，定时刷新
     * @return
     * @throws ConfigException
     */
//    public BcosSDK buildBcosSDK(List<String> newPeers) throws ConfigException, JniException {
//
//        // init config option
//        log.info("init configOption from configProperty");
//        ConfigOption configOption = initConfigOption;
//        // init bcosSDK
//        BcosSDK bcosSDK = new BcosSDK(configOption);
//        log.info("finish init bcos sdk instance, check sdk.log for detail");
//        try {
//            this.getRpcWeb3j(bcosSDK);
//            this.getClient(bcosSDK);
//        } catch (JniException e) {
//            log.error("getBcosSDK error:[]", e);
//            throw e;
//        }
////        ClientVersion version = bcosSDK.getGroupManagerService().getNodeVersion(ip + ":" + channelPort)
////            .getNodeVersion();
////        Constants.version = version.getVersion();
////        Constants.chainId = bcosSDK.getChainId();
//
//        return bcosSDK;
//    }


//    /**
//     * only used to get groupList
//     * @throws JniException
//     */
//    @Bean(name = "rpcClient")
//    public Client getRpcWeb3j(BcosSDK bcosSDK) throws JniException {
//
//        Client rpcWeb3j = Client.build(bcosSDK.getConfig());
//        // Client rpcWeb3j = bcosSDK.getClient();
//        log.info("get rpcWeb3j(only support groupList) client:{}", rpcWeb3j);
//        return rpcWeb3j;
//    }
//
//    @Bean(name = "singleClient")
//    public Client getClient(BcosSDK bcosSDK) {
//
//        Client client = bcosSDK.getClient("group");
//        // Client rpcWeb3j = bcosSDK.getClient();
//        log.info("get client:{}", client);
//        return client;
//    }
}
