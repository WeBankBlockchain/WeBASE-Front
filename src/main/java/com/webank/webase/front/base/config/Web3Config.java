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


import com.webank.webase.front.base.properties.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * init web3sdk getService.
 *
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "sdk")
public class Web3Config {

    public static String orgName;
    public String certPath = "conf";
    private List<Integer> groupIdList;
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    public int timeout = 30000;
    private int keepAlive;
    private String ip = "127.0.0.1";
    private String channelPort = "20200";
    private int encryptType;

    private int independentGroupId = Integer.MAX_VALUE;


    /**
     * 覆盖EncryptType构造函数
     * @return
     */
    @Bean(name = "common")
    public CryptoSuite getCommonSuite() {
        log.info("init encrypt type:{}", encryptType);
        return new CryptoSuite(encryptType);
    }


    /**
     * singleton instance of config
     * @case1: if add new web3j in web3jmap, add connection to channelConnectionsList of this bean
     * @case2: if create a brand new web3j of new connections config,
     * use getGroupChannelConnectionsConfig method to create new config
     * @return
     */
//    @Bean
//    public GroupChannelConnectionsConfig initGroupChannelConnectionsConfig() {
//        return getGroupChannelConnectionsConfig();
//    }

    /**
     * get a new config instance
     * @return
     */
//    private GroupChannelConnectionsConfig getGroupChannelConnectionsConfig() {
//        List<ChannelConnections> channelConnectionsList = new ArrayList<>();
//
//        List<String> connectionsList = new ArrayList<>();
//        connectionsList.add(ip + ":" + channelPort);
//        log.info("*****" + ip + ":" + channelPort);
//        ChannelConnections channelConnections = new ChannelConnections();
//        channelConnections.setConnectionsStr(connectionsList);
//        channelConnections.setGroupId(independentGroupId);
//        channelConnectionsList.add(channelConnections);
//
//        GroupChannelConnectionsConfig groupChannelConnectionsConfig =
//            new GroupChannelConnectionsConfig();
//        groupChannelConnectionsConfig.setAllChannelConnections(channelConnectionsList);
//        return groupChannelConnectionsConfig;
//    }

    /**
     * init Web3j of default group id 1
     */
//    @Bean
//    public Web3j getWeb3j(GroupChannelConnectionsConfig groupChannelConnectionsConfig)
//            throws Exception {
//        Service service = new Service();
//        service.setOrgID(orgName);
//        service.setGroupId(independentGroupId);
//        service.setThreadPool(sdkThreadPool());
//        service.setAllChannelConnections(groupChannelConnectionsConfig);
//        service.run();
//        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
//        channelEthereumService.setTimeout(timeout);
//        channelEthereumService.setChannelService(service);
//        Web3j web3j = Web3j.build(channelEthereumService, service.getGroupId());
//        // init node version
//        NodeVersion version = web3j.getNodeVersion().send();
//        Constants.version = version.getNodeVersion().getVersion();
//        Constants.chainId = version.getNodeVersion().getChainID();
//        log.info("Chain's clientVersion:{}", Constants.version);
//        return web3j;
//    }

    /**
     * set sdk threadPool.
     *
     * @return
     */
//    @Bean
//    public ThreadPoolTaskExecutor sdkThreadPool() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(corePoolSize);
//        executor.setMaxPoolSize(maxPoolSize);
//        executor.setQueueCapacity(queueCapacity);
//        executor.setKeepAliveSeconds(keepAlive);
//        executor.setRejectedExecutionHandler(new AbortPolicy());
//        executor.setThreadNamePrefix("sdkThreadPool-");
//        executor.initialize();
//        return executor;
//    }

    /**
     * init channel service. set setBlockNotifyCallBack
     * todo event callback setter
     * @return
     */
//    @Bean(name = "serviceMap")
//    @DependsOn("encryptType")
//    public Map<Integer, Service> serviceMap(Web3j web3j, NewBlockEventCallback newBlockEventCallBack)
//        throws Exception {
//        List<String> groupIdList = web3j.getGroupList().send().getGroupList();
//        GroupChannelConnectionsConfig groupChannelConnectionsConfig = getGroupChannelConnectionsConfig();
//        List<ChannelConnections> channelConnectionsList  = groupChannelConnectionsConfig.getAllChannelConnections();
//        channelConnectionsList.clear();
//        for (int i = 0; i < groupIdList.size(); i++) {
//            List<String> connectionsList = new ArrayList<>();
//            connectionsList.add(ip + ":" + channelPort);
//            ChannelConnections channelConnections = new ChannelConnections();
//            channelConnections.setConnectionsStr(connectionsList);
//            channelConnections.setGroupId(Integer.parseInt(groupIdList.get(i)));
//            log.info("***** groupId:{}", groupIdList.get(i));
//            channelConnectionsList.add(channelConnections);
//        }
//        Map serviceMap = new ConcurrentHashMap<Integer, Service>(groupIdList.size());
//        for (int i = 0; i < groupIdList.size(); i++) {
//            Service service = new Service();
//            service.setOrgID(orgName);
//            service.setGroupId(Integer.parseInt(groupIdList.get(i)));
//            service.setThreadPool(sdkThreadPool());
//            service.setAllChannelConnections(groupChannelConnectionsConfig);
//            // newBlockEventCallBack message enqueues in MQ
//            service.setBlockNotifyCallBack(newBlockEventCallBack);
//            service.run();
//            serviceMap.put(Integer.valueOf(groupIdList.get(i)), service);
//        }
//        return serviceMap;
//    }

    /**
     * init Web3j
     * 
     * @return
     */
//    @Bean
//    @DependsOn("encryptType")
//    public Map<Integer, Web3j> web3jMap(Map<Integer, Service> serviceMap) {
//        Map web3jMap = new ConcurrentHashMap<Integer, Web3j>(serviceMap.size());
//        for (Integer i : serviceMap.keySet()) {
//            Service service = serviceMap.get(i);
//            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
//            channelEthereumService.setTimeout(timeout);
//            channelEthereumService.setChannelService(service);
//            Web3j web3jSync = Web3j.build(channelEthereumService, service.getGroupId());
//            // for getClockNumber local
//            web3jSync.getBlockNumberCache();
//            web3jMap.put(i, web3jSync);
//        }
//        return web3jMap;
//    }


    @Bean
    public org.fisco.bcos.sdk.config.model.ConfigProperty getConfigProperty() {
        log.info("start init ConfigProperty");
        // cert config, encrypt type
        Map<String, Object> cryptoMaterial = new HashMap<>();
        // cert use conf
        cryptoMaterial.put("certPath", certPath);
        //cryptoMaterial.put("sslCryptoType", encryptType);
        log.info("init cert cryptoMaterial:{}, (using conf as cert path)", cryptoMaterial);

        // peers, default one node in front
        Map<String, Object> network = new HashMap<>();
        List<String> peers = new ArrayList<>();
        peers.add(ip + ":" + channelPort);
        network.put("peers", peers);
        log.info("init node network property :{}", peers);

        // thread pool config
        log.info("init thread pool property");
        Map<String, Object> threadPool = new HashMap<>();
        threadPool.put("channelProcessorThreadSize", corePoolSize);
        threadPool.put("receiptProcessorThreadSize", corePoolSize);
        threadPool.put("maxBlockingQueueSize", queueCapacity);
        log.info("init thread pool property:{}", threadPool);

        // init property
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setCryptoMaterial(cryptoMaterial);
        configProperty.setNetwork(network);
        configProperty.setThreadPool(threadPool);
        return configProperty;
    }

    @Bean
    public org.fisco.bcos.sdk.config.ConfigOption getConfig(ConfigProperty configProperty)
        throws ConfigException {
        log.info("init ConfigOption encrypt type:{}", encryptType);
        return new ConfigOption(configProperty, encryptType);
    }

    @Bean
    public org.fisco.bcos.sdk.BcosSDK getBcosSDK(ConfigOption configOption) {
        log.info("init bcos sdk instance, please check sdk.log");
        BcosSDK bcosSDK = new BcosSDK(configOption);

        return bcosSDK;
    }

    @Bean
    public Client getIndependentWeb3j(BcosSDK bcosSDK) {
        Client iClient = Client.build(bcosSDK.getChannel());
        // init node version
        ClientVersion version = iClient.getNodeVersion().getNodeVersion();
        Constants.version = version.getVersion();
        Constants.chainId = version.getChainId();
        return iClient;
    }


}
