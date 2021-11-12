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
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.ClientImpl;
import org.fisco.bcos.sdk.client.protocol.response.BcosGroupList;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.jni.rpc.Rpc;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
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
    private List<Integer> groupIdList;
    private String threadPoolSize;
//    private String ip = "127.0.0.1";
//    private String channelPort = "20200";
    private List<String> peers; // ["127.0.0.1:20200","127.0.0.1:20201"]
    private boolean useSmSsl = false;
    public String certPath = "conf";

    /**
     * todo init one-bean and set ConfigOption by api 或,支持启动就连接
     * todo 保存证书文件/保存ip配置(toml文件)
     * @return
     * @throws ConfigException
     */
    @Bean
    public BcosSDK getBcosSDK() throws ConfigException {
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
        log.info("init configOption from configProperty");
        ConfigOption configOption = new ConfigOption(configProperty);
        // init bcosSDK
        log.info("init bcos sdk instance, check sdk.log for detail");

        BcosSDK bcosSDK = new BcosSDK(configOption);

        log.info("init client version");
//        ClientVersion version = bcosSDK.getGroupManagerService().getNodeVersion(ip + ":" + channelPort)
//            .getNodeVersion();
//        Constants.version = version.getVersion();
//        Constants.chainId = version.getChainId();

        return bcosSDK;
    }


    @Bean(name = "rpcClient")
    public Client getRpcWeb3j(BcosSDK bcosSDK) throws JniException {
        // init rpc client(web3j)
        Client rpcWeb3j = Client.build(bcosSDK.getConfig());
        log.info("get rpcWeb3j(only support rpc) client:{}", rpcWeb3j);
        return rpcWeb3j;
    }
//
//    @Bean(name = "common") todo 通过group判断
//    @DependsOn("rpcClient")
//    public CryptoSuite getCommonSuite(Client rpcClient) {
//        int encryptType = rpcClient.getCryptoType();
//        log.info("getCommonSuite init encrypt type:{}", encryptType);
//        return new CryptoSuite(encryptType);
//    }
}
