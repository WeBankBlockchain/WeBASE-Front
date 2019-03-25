package com.webank.webase.front.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import lombok.Data;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * init web3sdk service.
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sdk")
public class Web3Config {
    @Autowired
    NodeConfig nodeConfig;
    private String orgName;
    private int groupId;
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    private int keepAlive;

    /**
     * init service.
     * 
     * @return
     */
    @Bean
    public Service service() {
        List<String> connectionsList = new ArrayList<>();
        connectionsList.add(nodeConfig.getListenip()+ ":" + nodeConfig.getChannelPort());

        ChannelConnections channelConnections = new ChannelConnections();
        channelConnections.setConnectionsStr(connectionsList);
        channelConnections.setGroupId(groupId);
        List<ChannelConnections> channelConnectionsList = new ArrayList<>();
        channelConnectionsList.add(channelConnections);

        GroupChannelConnectionsConfig groupChannelConnectionsConfig = new GroupChannelConnectionsConfig();
        groupChannelConnectionsConfig.setAllChannelConnections(channelConnectionsList);

        nodeConfig.setOrgName(orgName);
        Service service = new Service();
        service.setOrgID(orgName);
        service.setGroupId(groupId);
        service.setThreadPool(sdkThreadPool());
        service.setAllChannelConnections(groupChannelConnectionsConfig);
        return service;
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
     * init web3j.
     * 
     * @return
     */
    @Bean
    public Web3j web3j() throws Exception {
        service().run();
        Thread.sleep(1000);
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setTimeout(3000);
        channelEthereumService.setChannelService(service());
        return Web3j.build(channelEthereumService, groupId);
    }

    @Bean
    public CnsService getCnsService(Web3j web3j) {
         Credentials credentials =
                Credentials.create("b83261efa42895c38c6c2364ca878f43e77f3cddbc922bf57d0d48070f79feb6");
        return new CnsService(web3j, credentials);
    }
}
