/*
 * Copyright 2014-2019 the original author or authors.
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

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
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
    public static int timeout = 30000;
    private int keepAlive;
    private String ip = "127.0.0.1";
    private String channelPort = "20200";
    // 0:standard, 1:guomi
    private int encryptType;

    /**
     * 覆盖EncryptType构造函数，不能写getEncrytType() 放在web3sdk初始化前，否则当前类里的CnsServiceMap的credential为非国密的
     * 
     * @return
     */
    @Bean(name = "encryptType")
    public EncryptType EncryptType() {
        log.info("*****init EncrytType:" + encryptType);
        return new EncryptType(encryptType);
    }

    @Bean
    public GroupChannelConnectionsConfig getGroupChannelConnectionsConfig() {
        List<ChannelConnections> channelConnectionsList = new ArrayList<>();

        List<String> connectionsList = new ArrayList<>();
        connectionsList.add(ip + ":" + channelPort);
        log.info("*********" + ip + ":" + channelPort);
        ChannelConnections channelConnections = new ChannelConnections();
        channelConnections.setConnectionsStr(connectionsList);
        channelConnections.setGroupId(1);
        channelConnectionsList.add(channelConnections);

        GroupChannelConnectionsConfig groupChannelConnectionsConfig =
                new GroupChannelConnectionsConfig();
        groupChannelConnectionsConfig.setAllChannelConnections(channelConnectionsList);
        return groupChannelConnectionsConfig;
    }

    /**
     * init getService.
     * 
     * @return
     */
    @Bean
    public Web3j getService(GroupChannelConnectionsConfig groupChannelConnectionsConfig)
            throws Exception {

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
    @DependsOn("encryptType")
    public Map<Integer, Web3j> web3jMap(Web3j web3j,
            GroupChannelConnectionsConfig groupChannelConnectionsConfig) throws Exception {
        // whether front' encrypt type matches with chain's
        isMatchEncryptType(web3j);
        List<String> groupIdList = web3j.getGroupList().send().getGroupList();
        List<ChannelConnections> channelConnectionsList =
                groupChannelConnectionsConfig.getAllChannelConnections();
        channelConnectionsList.clear();
        for (int i = 0; i < groupIdList.size(); i++) {
            List<String> connectionsList = new ArrayList<>();
            connectionsList.add(ip + ":" + channelPort);
            ChannelConnections channelConnections = new ChannelConnections();
            channelConnections.setConnectionsStr(connectionsList);
            channelConnections.setGroupId(Integer.valueOf(groupIdList.get(i)));
            log.info("*** groupId " + groupIdList.get(i));
            channelConnectionsList.add(channelConnections);
        }
        Map web3jMap = new ConcurrentHashMap<Integer, Web3j>();
        for (int i = 0; i < groupIdList.size(); i++) {
            Service service = new Service();
            service.setOrgID(orgName);
            service.setGroupId(Integer.valueOf(groupIdList.get(i)));
            service.setThreadPool(sdkThreadPool());
            service.setAllChannelConnections(groupChannelConnectionsConfig);
            service.run();
            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
            channelEthereumService.setTimeout(timeout);
            channelEthereumService.setChannelService(service);
            Web3j web3jSync = Web3j.build(channelEthereumService, service.getGroupId());
            // for getClockNumber local
            web3jSync.getBlockNumberCache();
            web3jMap.put(Integer.valueOf(groupIdList.get(i)), web3jSync);
        }
        return web3jMap;
    }

    public void isMatchEncryptType(Web3j web3j) throws IOException {
        boolean isMatch = true;
        // 1: guomi, 0: standard
        String clientVersion = web3j.getNodeVersion().send().getNodeVersion().getVersion();
        log.info("Chain's clientVersion:{}", clientVersion);
        if (clientVersion.contains("gm")) {
            isMatch = EncryptType.encryptType == 1;
        } else {
            isMatch = EncryptType.encryptType == 0;
        }
        if (!isMatch) {
            log.error("Chain's version not matches with Front's  encryptType:{}",
                    EncryptType.encryptType);
            throw new FrontException(ConstantCode.SYSTEM_ERROR.getCode(),
                    "Chain's version not matches with Front's" + " encryptType: "
                            + EncryptType.encryptType);
        }
    }

    @Bean
    @DependsOn("encryptType")
    public Map<Integer, CnsService> cnsServiceMap(Map<Integer, Web3j> web3jMap) {
        // support guomi
        Credentials credentials = GenCredential.create();
        Map cnsServiceMap = new ConcurrentHashMap<Integer, CnsService>();
        Iterator entries = web3jMap.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Integer key = (Integer) entry.getKey();
            Web3j value = (Web3j) entry.getValue();
            cnsServiceMap.put(key, new CnsService(value, credentials));
        }
        return cnsServiceMap;
    }

    @Bean
    public HashMap<String, String> cnsMap() {
        HashMap cnsMap = new HashMap<String, String>();
        return cnsMap;
    }
}
