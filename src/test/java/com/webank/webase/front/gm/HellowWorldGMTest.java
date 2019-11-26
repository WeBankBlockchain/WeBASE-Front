/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.gm;

import com.webank.webase.front.gm.basic.HelloWorldGM;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class HellowWorldGMTest {
//    public static ApplicationContext context = null;
//    // 初始化交易签名私钥 国密的create方法
//    public static Credentials credentials =
//            GenCredential.create("b83261efa42895c38c6c2364ca878f43e77f3cddbc922bf57d0d48070f79feb6");
//    protected static Web3j web3j;
    public static BigInteger gasPrice = new BigInteger("300000000");
    public static BigInteger gasLimit = new BigInteger("300000000");

    protected static String address;
    protected static BigInteger blockNumber;
    protected static String blockHash;
    protected static String txHash;

    @Test
    public void testHello() throws Exception{
      // EncryptType encryptType = new EncryptType(1);
        String groupId = "1";
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        service.run();
        System.out.println("===================================================================");

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        channelEthereumService.setTimeout(10000);
        Web3j web3 = Web3j.build(channelEthereumService, Integer.parseInt(groupId));
        BigInteger gasPrice = new BigInteger("300000000");
        BigInteger gasLimit = new BigInteger("3000000000");
        ContractGasProvider contractGasProvider = new StaticGasProvider(gasPrice, gasLimit);

        Credentials credentials1 =
                GenCredential.create(
                        "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f6");
                // address 0x1a3fc157bd47c3fc2e260b34abbf481730d0f80f
        // 测试合约部署
        HelloWorldGM test = HelloWorldGM.deploy(web3, credentials1, new StaticGasProvider(gasPrice, gasLimit)).send();
        address = test.getContractAddress();
        blockNumber = test.getTransactionReceipt().get().getBlockNumber();
        blockHash = test.getTransactionReceipt().get().getBlockHash();
        txHash = test.getTransactionReceipt().get().getTransactionHash();

        System.out.println("contract address: " + address);
        System.out.println("contract blockNumber: " + blockNumber);
        System.out.println("contract blockHash: " + blockHash);
        System.out.println("contract txHash: " + txHash);
        // send tx
        System.out.println("HelloWorld address is: " + test.getContractAddress());
        // call set function
        test.set("Hello, World!").send();
        // call get function
        String result = test.get().send();
        System.out.println(result);
        assertTrue("Hello, World!".equals(result));
    }
}
