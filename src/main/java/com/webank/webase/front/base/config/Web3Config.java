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
import com.webank.webase.front.event.callback.NewBlockEventCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
    @Bean(name = "encryptType")
    public EncryptType EncryptType() {
        // 1: guomi, 0: standard
        log.info("*****init EncrytType:" + encryptType);
        return new EncryptType(encryptType);
    }

    /**
     * singleton instance of config
     * @case1: if add new web3j in web3jmap, add connection to channelConnectionsList of this bean
     * @case2: if create a brand new web3j of new connections config,
     * use getGroupChannelConnectionsConfig method to create new config
     * @return
     */
    @Bean
    public GroupChannelConnectionsConfig initGroupChannelConnectionsConfig() {
        return getGroupChannelConnectionsConfig();
    }

    /**
     * get a new config instance
     * @return
     */
    private GroupChannelConnectionsConfig getGroupChannelConnectionsConfig() {
        List<ChannelConnections> channelConnectionsList = new ArrayList<>();

        List<String> connectionsList = new ArrayList<>();
        connectionsList.add(ip + ":" + channelPort);
        log.info("*****" + ip + ":" + channelPort);
        ChannelConnections channelConnections = new ChannelConnections();
        channelConnections.setConnectionsStr(connectionsList);
        channelConnections.setGroupId(independentGroupId);
        channelConnectionsList.add(channelConnections);

        GroupChannelConnectionsConfig groupChannelConnectionsConfig =
            new GroupChannelConnectionsConfig();
        groupChannelConnectionsConfig.setAllChannelConnections(channelConnectionsList);
        return groupChannelConnectionsConfig;
    }

    /**
     * init Web3j of default group id 1
     */
    @Bean
    public Web3j getWeb3j(GroupChannelConnectionsConfig groupChannelConnectionsConfig)
            throws Exception {
        Service service = new Service();
        service.setOrgID(orgName);
        service.setGroupId(independentGroupId);
        service.setThreadPool(sdkThreadPool());
        service.setAllChannelConnections(groupChannelConnectionsConfig);
        service.run();
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setTimeout(timeout);
        channelEthereumService.setChannelService(service);
        Web3j web3j = Web3j.build(channelEthereumService, service.getGroupId());
        // init node version
        NodeVersion version = web3j.getNodeVersion().send();
        Constants.version = version.getNodeVersion().getVersion();
        Constants.chainId = version.getNodeVersion().getChainID();
        log.info("Chain's clientVersion:{}", Constants.version);
        return web3j;
    }

    /**
     * set sdk threadPool.
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor sdkThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAlive);
        executor.setRejectedExecutionHandler(new AbortPolicy());
        executor.setThreadNamePrefix("sdkThreadPool-");
        executor.initialize();
        return executor;
    }

    /**
     * init channel service. set setBlockNotifyCallBack
     * 
     * @return
     */
    @Bean(name = "serviceMap")
    @DependsOn("encryptType")
    public Map<Integer, Service> serviceMap(Web3j web3j, NewBlockEventCallback newBlockEventCallBack)
        throws Exception {
        List<String> groupIdList = web3j.getGroupList().send().getGroupList();
        GroupChannelConnectionsConfig groupChannelConnectionsConfig = getGroupChannelConnectionsConfig();
        List<ChannelConnections> channelConnectionsList  = groupChannelConnectionsConfig.getAllChannelConnections();
        channelConnectionsList.clear();
        for (int i = 0; i < groupIdList.size(); i++) {
            List<String> connectionsList = new ArrayList<>();
            connectionsList.add(ip + ":" + channelPort);
            ChannelConnections channelConnections = new ChannelConnections();
            channelConnections.setConnectionsStr(connectionsList);
            channelConnections.setGroupId(Integer.parseInt(groupIdList.get(i)));
            log.info("***** groupId:{}", groupIdList.get(i));
            channelConnectionsList.add(channelConnections);
        }
        Map serviceMap = new ConcurrentHashMap<Integer, Service>(groupIdList.size());
        for (int i = 0; i < groupIdList.size(); i++) {
            Service service = new Service();
            service.setOrgID(orgName);
            service.setGroupId(Integer.parseInt(groupIdList.get(i)));
            service.setThreadPool(sdkThreadPool());
            service.setAllChannelConnections(groupChannelConnectionsConfig);
            // newBlockEventCallBack message enqueues in MQ
            service.setBlockNotifyCallBack(newBlockEventCallBack);
            service.run();
            serviceMap.put(Integer.valueOf(groupIdList.get(i)), service);
        }
        return serviceMap;
    }

    /**
     * init Web3j
     * 
     * @param serviceMap
     * @return
     */
    @Bean
    @DependsOn("encryptType")
    public Map<Integer, Web3j> web3jMap(Map<Integer, Service> serviceMap) {
        Map web3jMap = new ConcurrentHashMap<Integer, Web3j>(serviceMap.size());
        for (Integer i : serviceMap.keySet()) {
            Service service = serviceMap.get(i);
            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
            channelEthereumService.setTimeout(timeout);
            channelEthereumService.setChannelService(service);
            Web3j web3jSync = Web3j.build(channelEthereumService, service.getGroupId());
            // for getClockNumber local
            web3jSync.getBlockNumberCache();
            web3jMap.put(i, web3jSync);
        }
        return web3jMap;
    }

}
