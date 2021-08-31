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
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.Message;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.fisco.bcos.sdk.network.MsgHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

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
    public String certPath = "conf";
    private List<Integer> groupIdList;
    /* use String in java sdk*/
    private String corePoolSize;
    private String maxPoolSize;
    private String queueCapacity;
    /* use String in java sdk*/
    private String ip = "127.0.0.1";
    private String channelPort = "20200";

    // add channel disconnect
    public static boolean PEER_CONNECTED = true;
    static class Web3ChannelMsg implements MsgHandler {
        @Override
        public void onConnect(ChannelHandlerContext ctx) {
            PEER_CONNECTED = true;
            log.info("Web3ChannelMsg onConnect:{}, status:{}", ctx.channel().remoteAddress(), PEER_CONNECTED);
        }

        @Override
        public void onMessage(ChannelHandlerContext ctx, Message msg) {
            // not added in message handler, ignore this override
            log.info("Web3ChannelMsg onMessage:{}, status:{}", ctx.channel().remoteAddress(), PEER_CONNECTED);
        }

        @Override
        public void onDisconnect(ChannelHandlerContext ctx) {
            PEER_CONNECTED = false;
            log.error("Web3ChannelMsg onDisconnect:{}, status:{}", ctx.channel().remoteAddress(), PEER_CONNECTED);
        }
    }

    @Bean
    public BcosSDK getBcosSDK() throws ConfigException {
        log.info("start init ConfigProperty");
        // cert config, encrypt type
        Map<String, Object> cryptoMaterial = new HashMap<>();
        // cert use conf
        cryptoMaterial.put("certPath", certPath);
        // user no need set this:cryptoMaterial.put("sslCryptoType", encryptType);
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
        // init config option
        log.info("init configOption from configProperty");
        ConfigOption configOption = new ConfigOption(configProperty);
        // init bcosSDK
        log.info("init bcos sdk instance, please check sdk.log");
        BcosSDK bcosSDK = new BcosSDK(configOption);

        log.info("init client version");
        ClientVersion version = bcosSDK.getGroupManagerService().getNodeVersion(ip + ":" + channelPort)
            .getNodeVersion();
        Constants.version = version.getVersion();
        Constants.chainId = version.getChainId();

        Web3ChannelMsg disconnectMsg = new Web3ChannelMsg();
        bcosSDK.getChannel().addConnectHandler(disconnectMsg);
        bcosSDK.getChannel().addDisconnectHandler(disconnectMsg);

        return bcosSDK;
    }


    /**
     * 覆盖EncryptType构造函数
     * @return
     */
    @Bean(name = "common")
    public CryptoSuite getCommonSuite(BcosSDK bcosSDK) {
        int encryptType = bcosSDK.getGroupManagerService().getCryptoType(ip + ":" + channelPort);
        log.info("getCommonSuite init encrypt type:{}", encryptType);
        return new CryptoSuite(encryptType);
    }

    @Bean(name = "rpcClient")
    public Client getRpcWeb3j(BcosSDK bcosSDK) {
        // init rpc client(web3j)
        Client rpcWeb3j = Client.build(bcosSDK.getChannel());
        log.info("get rpcWeb3j(only support rpc) client:{}", rpcWeb3j);
        return rpcWeb3j;
    }

}
