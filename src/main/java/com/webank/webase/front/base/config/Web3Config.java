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


import com.webank.webase.front.configapi.ConfigService;
import com.webank.webase.front.util.JsonUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.eventsub.EventSubscribe;
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
//    private boolean loadFromDb;

    private String threadPoolSize;
    private String certPath;
    private String useSmSsl;
    private List<String> peers;


    @Bean
    public ConfigOption getConfigOptionFromFile() throws ConfigException {
        log.info("start init ConfigProperty");
        // cert config, encrypt type
        Map<String, Object> cryptoMaterial = new HashMap<>();
        cryptoMaterial.put("certPath", certPath);
        cryptoMaterial.put("useSMCrypto", useSmSsl);
        log.info("init cert cryptoMaterial:{}, (using conf as cert path)", JsonUtils.objToString(cryptoMaterial));

        // peers, default one node in front
        Map<String, Object> network = new HashMap<>();
        network.put("peers", peers);
        log.info("init node network property :{}", JsonUtils.objToString(peers));

        // thread pool config
        log.info("init thread pool property");
        Map<String, Object> threadPool = new HashMap<>();
        threadPool.put("threadPoolSize", threadPoolSize);
        log.info("init thread pool property:{}", JsonUtils.objToString(threadPool));

        // init property
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setCryptoMaterial(cryptoMaterial);
        configProperty.setNetwork(network);
        configProperty.setThreadPool(threadPool);
        // init config option
        ConfigOption configOption = new ConfigOption(configProperty);
        log.info("initConfigOptionFromFile init configOption :{}", configOption);
        return configOption;
    }

    /**
     * only used to get groupList
     * @throws JniException
     */
    @Bean(name = "rpcClient")
    public Client getRpcWeb3j(ConfigOption configOption) throws JniException {

        Client rpcWeb3j = Client.build(configOption);
        // Client rpcWeb3j = bcosSDK.getClient();
        log.info("get rpcWeb3j(only support groupList) client:{}", rpcWeb3j);
        return rpcWeb3j;
    }

    @Bean(name = "clientMap")
    public Map<String, Client> getClientMap(ConfigOption configOption) throws JniException {
        Map<String, Client> clientMap = new HashMap<>();
        Client rpcClient = getRpcWeb3j(configOption);
        List<String> groupList = rpcClient.getGroupList().getResult().getGroupList();
        log.info("getClientMap groupList:{}", groupList);
        if (groupList.isEmpty()) {
            log.error("**** getClientMap group list is empty!");
            return clientMap;
        }
        for (String groupId : groupList) {
            Client client = Client.build(groupId, configOption);
            clientMap.put(groupId, client);
        }
        log.info("getClientMap success, size:{}", clientMap.size());

        return clientMap;
    }
//        log.info("getClientMap loadFromDb:{}", loadFromDb);
//        if (loadFromDb) {
//            ConfigOption configOption = null;
//            try {
//                configOption = configService.initConfigOptionFromDb();
//            } catch (FrontException e) {
//                log.error("getClientMap not config in db:[]", e);
//            } catch (Exception e) {
//                log.error("getClientMap from db failed:[]", e);
//            }
//            // if db empty config, return empty map and wait to init
//            if (configOption == null) {
//                log.error("getClientMap from db failed, configOption is null");
//                return clientMap;
//            }
//            try {
//                clientMap = this.initMap(configOption);
//            } catch (JniException e) {
//                log.error("getClientMap from db failed, initMap configOption not match:[]", e);
//            } catch (Exception e) {
//                log.error("getClientMap from db failed, initMap:[]", e);
//            }
//        } else { // load from file


    @Bean(name = "eventSubMap")
    public Map<String, EventSubscribe> getEventSubMap(ConfigOption configOption) throws JniException {
        Map<String, EventSubscribe> eventSubscribeMap = new HashMap<>();
        Client rpcClient = getRpcWeb3j(configOption);
        List<String> groupList = rpcClient.getGroupList().getResult().getGroupList();
        log.info("getEventSubMap groupList:{}", groupList);
        if (groupList.isEmpty()) {
            log.error("**** getEventSubMap group list is empty!");
            return eventSubscribeMap;
        }
        for (String groupId : groupList) {
            EventSubscribe eventSubscribe = EventSubscribe.build(groupId, configOption);
            eventSubscribeMap.put(groupId, eventSubscribe);
        }
        log.info("getEventSubMap success, size:{}", eventSubscribeMap.size());

        return eventSubscribeMap;
    }

//    private Map<String, Client> initMap(ConfigOption configOption) throws JniException {
//        Map<String, Client> clientMap = new HashMap<>();
//        Client rpcClient = getRpcWeb3j(configOption);
//        List<String> groupList = rpcClient.getGroupList().getResult().getGroupList();
//        log.info("getClientMap groupList:{}", groupList);
//        if (groupList.isEmpty()) {
//            log.warn("**** getClientMap group list is empty!");
//            return clientMap;
//        }
//        for (String groupId : groupList) {
//            Client client = Client.build(groupId, configOption);
//            clientMap.put(groupId, client);
//        }
//        return clientMap;
//    }

//    @Bean
//    public Stack<BcosSDK> getBcosSDK() throws ConfigException {
//        Stack<BcosSDK> bcosSDKs = new Stack<>();
//        log.info("getBcosSDK loadFromDb:{}", loadFromDb);
//        if (loadFromDb) {
//            ConfigOption configOption = null;
//            try {
//                configOption = configService.initConfigOptionFromDb();
//            } catch (FrontException e) {
//                log.error("Config of sdk not config:[]", e);
//            } catch (Exception e) {
//                log.error("Init bcosSDK from db failed:[]", e);
//            }
//            if (configOption == null) {
//                log.error("Init bcosSDK configOption from db failed, configOption is null");
//                return bcosSDKs;
//            }
//            BcosSDK bcosSDK = new BcosSDK(configOption);
//            log.info("getBcosSDK bcosSDK:{}", JsonUtils.objToString(bcosSDK));
//            bcosSDKs.push(bcosSDK);
//        } else {
//            ConfigOption configOption = this.initConfigOptionFromFile();
//            BcosSDK bcosSDK = new BcosSDK(configOption);
//            log.info("getBcosSDK bcosSDK:{}", JsonUtils.objToString(bcosSDK));
//            bcosSDKs.push(bcosSDK);
//        }
//        log.info("getBcosSDK bcosSDKs:{}", JsonUtils.objToString(bcosSDKs));
//
//        return bcosSDKs;
//    }


}
