/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.abi;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigInteger;

public class TestBase {
  public static ApplicationContext context = null;
  // 初始化交易签名私钥
  public static  Credentials credentials =
      Credentials.create("b83261efa42895c38c6c2364ca878f43e77f3cddbc922bf57d0d48070f79feb6");
  protected static Web3j web3j;
  public static BigInteger gasPrice = new BigInteger("300000000");
  public static BigInteger gasLimit = new BigInteger("300000000");

  protected static String address;
  protected static BigInteger blockNumber;
  protected static String blockHash;
  protected static String txHash;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // 获取spring配置文件，生成上下文
    context = new ClassPathXmlApplicationContext("applicationContext.xml");
    //   ((ClassPathXmlApplicationContext) context).start();

    Service service = context.getBean(Service.class);
    service.run();

    System.out.println("start...");
    System.out.println("===================================================================");

    ChannelEthereumService channelEthereumService = new ChannelEthereumService();
    channelEthereumService.setChannelService(service);

    web3j = Web3j.build(channelEthereumService, service.getGroupId());
    // EthBlockNumber ethBlockNumber = web3.ethBlockNumber().send();

  }

  @AfterClass
  public static void setUpAfterClass() throws Exception {
    ((ClassPathXmlApplicationContext) context).destroy();
  }
}
