package com.webank.webase.front.config;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

import com.webank.webase.front.base.Constants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
 * init web3sdk getService.
 *
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "sdk")
public class Web3Config {
//    @Autowired
//    NodeConfig nodeConfig;
    public   static String orgName;
    private List<Integer> groupIdList;
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    public static int timeout=30000;
    private int keepAlive;
    private String ip= "127.0.0.1";
    private String channelPort= "20200";

    @Bean
    public GroupChannelConnectionsConfig getGroupChannelConnectionsConfig(){
        List<ChannelConnections> channelConnectionsList = new ArrayList<>();

            List<String> connectionsList = new ArrayList<>();
            connectionsList.add(ip + ":" + channelPort);
            log.info("*********" + ip +  ":" + channelPort);
            ChannelConnections channelConnections = new ChannelConnections();
            channelConnections.setConnectionsStr(connectionsList);
            channelConnections.setGroupId(1);
            channelConnectionsList.add(channelConnections);

        GroupChannelConnectionsConfig groupChannelConnectionsConfig = new GroupChannelConnectionsConfig();
        groupChannelConnectionsConfig.setAllChannelConnections(channelConnectionsList);
     return groupChannelConnectionsConfig;
    }

    /**
     * init getService.
     * 
     * @return
     */
    @Bean
    public Web3j getService(GroupChannelConnectionsConfig groupChannelConnectionsConfig) throws Exception {

        Service service = new Service();
        service.setOrgID(orgName);
        service.setGroupId(1);
        service.setThreadPool(sdkThreadPool());
        service.setAllChannelConnections(groupChannelConnectionsConfig);
        service.run();
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setTimeout(timeout);
        channelEthereumService.setChannelService(service);
        Web3j web3j = Web3j.build(channelEthereumService);
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
     * init web3j.
     * 
     * @return
     */
    @Bean
    public HashMap<Integer, Web3j> web3j(Web3j web3j, GroupChannelConnectionsConfig groupChannelConnectionsConfig) throws Exception {
        List<String> groupIdList = web3j.getGroupList().send().getGroupList();
        List<ChannelConnections> channelConnectionsList = groupChannelConnectionsConfig.getAllChannelConnections();
        channelConnectionsList.clear();
        for (int i = 0; i < groupIdList.size(); i++) {
            List<String> connectionsList = new ArrayList<>();
            connectionsList.add( ip + ":" + channelPort);
            ChannelConnections channelConnections = new ChannelConnections();
            channelConnections.setConnectionsStr(connectionsList);
            channelConnections.setGroupId(Integer.valueOf(groupIdList.get(i)));
            log.info("*** groupId " +  groupIdList.get(i));
            channelConnectionsList.add(channelConnections);
        }
        HashMap web3jMap= new HashMap<Integer,Web3j>();
        for (int i = 0; i < groupIdList.size(); i++) {
            Service service1 = new Service();
            service1.setOrgID(orgName);
            service1.setGroupId(Integer.valueOf(groupIdList.get(i)));
            service1.setThreadPool(sdkThreadPool());
            service1.setAllChannelConnections(groupChannelConnectionsConfig);
            service1.run();
            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
            channelEthereumService.setTimeout(timeout);
            channelEthereumService.setChannelService(service1);
            Web3j web3j1= Web3j.build(channelEthereumService, service1.getGroupId() );
            //for getClockNumber local
            web3j1.getBlockNumberCache();
            web3jMap.put(Integer.valueOf(groupIdList.get(i)) ,web3j1 );
        }
        return web3jMap;
    }

    @Bean
    public HashMap<Integer, CnsService> getCnsService(HashMap<Integer,Web3j> web3jMap) {
        Credentials credentials = Credentials.create("3bed914595c159cbce70ec5fb6aff3d6797e0c5ee5a7a9224a21cae8932d84a4");
        HashMap cnsServiceMap = new HashMap<Integer, CnsService>();
        Iterator entries = web3jMap.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Integer key =  (Integer)entry.getKey();
            Web3j value = (Web3j) entry.getValue();

            cnsServiceMap.put(key, new CnsService(value, credentials));

        }
        return cnsServiceMap;
    }

    @Bean
    public HashMap<String, String> getCnsMap() {
        HashMap cnsMap = new HashMap<String, String>();
        return cnsMap;
    }
}
