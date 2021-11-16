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
import com.webank.webase.front.util.JsonUtils;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.jni.common.JniException;
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

    // deprecated org name, use agency from /web3/nodeInfo api instead
    public static String orgName = "fisco";
    private List<Integer> groupIdList;
    private String threadPoolSize;
    private List<String> peers; // ["127.0.0.1:20200","127.0.0.1:20201"]
    private boolean useSmSsl = false;
    public String certPath = "conf";


    @Bean
    public Stack<BcosSDK> getBcosSDK() throws ConfigException, JniException {
        Stack<BcosSDK> bcosSDKs = new Stack<>();
        // pass peers to init sdk
        BcosSDK bcosSDK = this.buildBcosSDK(this.peers);
        // check block height
//        Client groupListClient = this.getRpcWeb3j(bcosSDK);
//        log.info("====== getBcosSDK groupListClient:{}", JsonUtils.objToString(groupListClient));
//        List<String> groupList = groupListClient.getGroupList().getResult().getGroupList();
//        this.getClientMap(bcosSDK, groupList);
//        Client client = bcosSDK.getClient(groupList.iterator().next());
//        log.info("====== getBcosSDK groupList:{}", groupList);
//        log.info("====== blockHeight client:{}", JsonUtils.objToString(client));
//        BigInteger blockHeight = client.getBlockNumber().getBlockNumber();
//        log.info("====== getBcosSDK blockHeight:{}", blockHeight);
//        Constants.chainId = client.getChainId();

        bcosSDKs.push(bcosSDK);
        log.info("====== getBcosSDK stack bcosSDKs:{}", bcosSDKs);

        return bcosSDKs;
    }

//
//    /**
//     * todo rm this
//     * @return
//     * @throws JniException
//     */
//    @Bean
//    public Map<String, Client> getClientMap(BcosSDK bcosSDK, List<String> groupList) {
//        Map<String, Client> clientMap = new ConcurrentHashMap<>();
//        log.info("init getClientMap bcosSDK:{},groupList:{}", JsonUtils.objToString(bcosSDK), groupList);
//        for (String groupId : groupList) {
//            Client client = null;
//            try {
//                client = Client.build(groupId, bcosSDK.getConfig());
//                log.info("getClientMap client:{}", JsonUtils.objToString(client));
//            } catch (JniException e) {
//                log.error("getClientMap build client error:[]", e);
//            };
//            log.info("getClientMap client:{}", JsonUtils.objToString(client));
//            clientMap.put(groupId, client);
//        }
//        log.info("finish getClientMap:{}", clientMap.size());
//        return clientMap;
//    }

    /**
     * todo init one-bean and set ConfigOption by api
     * 启动至少配置证书、一个节点的IP PORT
     * todo DB保存ip配置，定时刷新
     * @return
     * @throws ConfigException
     */
    public BcosSDK buildBcosSDK(List<String> newPeers) throws ConfigException, JniException {
        log.info("start init ConfigProperty");
        log.info("=========getBcosSDK :{}", peers);
        // cert config, encrypt type
        Map<String, Object> cryptoMaterial = new HashMap<>();
        // cert use conf
        cryptoMaterial.put("certPath", certPath);
        cryptoMaterial.put("useSMCrypto", useSmSsl);
        // user no need set this:cryptoMaterial.put("sslCryptoType", encryptType);
        log.info("init cert cryptoMaterial:{}, (using conf as cert path)", JsonUtils.objToString(cryptoMaterial));

        // peers, default one node in front
        Map<String, Object> network = new HashMap<>();
        network.put("peers", newPeers);
        log.info("init node network property :{}", JsonUtils.objToString(newPeers));

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
        log.info("init configOption from configProperty");
        ConfigOption configOption = new ConfigOption(configProperty);
        // init bcosSDK
        BcosSDK bcosSDK = new BcosSDK(configOption);
        log.info("finish init bcos sdk instance, check sdk.log for detail");
        try {
            this.getRpcWeb3j(bcosSDK);
            this.getClient(bcosSDK);
        } catch (JniException e) {
            log.error("getBcosSDK error:[]", e);
            throw e;
        }
//        ClientVersion version = bcosSDK.getGroupManagerService().getNodeVersion(ip + ":" + channelPort)
//            .getNodeVersion();
//        Constants.version = version.getVersion();
//        Constants.chainId = bcosSDK.getChainId();

        return bcosSDK;
    }


    /**
     * only used to get groupList
     * @throws JniException
     */
    @Bean(name = "rpcClient")
    public Client getRpcWeb3j(BcosSDK bcosSDK) throws JniException {

        Client rpcWeb3j = Client.build(bcosSDK.getConfig());
        // Client rpcWeb3j = bcosSDK.getClient();
        log.info("get rpcWeb3j(only support groupList) client:{}", rpcWeb3j);
        return rpcWeb3j;
    }

    @Bean(name = "singleClient")
    public Client getClient(BcosSDK bcosSDK) {

        Client client = bcosSDK.getClient("group");
        // Client rpcWeb3j = bcosSDK.getClient();
        log.info("get client:{}", client);
        return client;
    }
}
