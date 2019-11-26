/*
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.front.gm.basic;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.config.SystemConfigService;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

/**
 * 国密GenCredential与web3j在HelloWorldGMTestBase中static变量初始化
 * 因为此处都是GET操作，未对链发起交易
 */
public class BasicTestGM extends TestBaseGM {

  public static Credentials credentials =
          GenCredential.create("b83261efa42895c38c6c2364ca878f43e77f3cddbc922bf57d0d48070f79feb6");
  @Test
  public void getBlockNumber() throws IOException {
    BigInteger blockNumber = web3j.getBlockNumber().send().getBlockNumber();
    assertTrue(blockNumber.compareTo(new BigInteger("0")) >= 0);
  }

  /**
   * 测试precompiled api
   * 国密credential
   * @throws Exception
   */
  @Test
  public void testSystemConfigService() throws Exception {

    SystemConfigService systemConfigSerivce = new SystemConfigService(web3j, credentials);
    systemConfigSerivce.setValueByKey("tx_count_limit", "2000");
    String value = web3j.getSystemConfigByKey("tx_count_limit").send().getSystemConfigByKey();
    System.out.println(value);
    assertTrue("2000".equals(value));
  }


  /**
   * 测试向国密链发交易 测试国密sm2的sign
   * 对部署的HelloWorldGM合约进行调用
   * @param *contractAddress: 0x57bb53e57ef30e207e1cc5be053c536950ec2243(部署后自行替换)
   * @throws Exception
   */
  @Test
  public void testSendTx() throws Exception {
    String contractAddress = "0x57bb53e57ef30e207e1cc5be053c536950ec2243";
    HelloWorldGM helloWorldGM =
            HelloWorldGM.load(contractAddress, web3j, credentials,
                    new StaticGasProvider(gasPrice, gasLimit));
    helloWorldGM.set("test gm send tx").send();
    String result = helloWorldGM.get().send();
    System.out.println(result);
    assertTrue("test gm send tx".equals(result));

  }


  /**
   * 调用合约
   * @throws Exception
   */
  @Test
  public void deployAndCallHelloWorld() throws Exception {

    // deploy contract
    HelloWorldGM helloWorldGM =
            HelloWorldGM.deploy(
                    web3j,
                    credentials,
                    new StaticGasProvider(gasPrice, gasLimit))
                    .send();
    if (helloWorldGM != null) {
      System.out.println("HelloWorld address is: " + helloWorldGM.getContractAddress());
      // call set function
      helloWorldGM.set("Hello, World!").send();
      // call get function
      String result = helloWorldGM.get().send();
      System.out.println(result);
      assertTrue("Hello, World!".equals(result));
    }
  }







  // test gm deploy contract: change sol's bin, sign, hash
//  @Test
//  public void basicTest() throws Exception {
//    try {
//      testDeployContract(web3j, credentials);
//    } catch (Exception e) {
//      e.printStackTrace();
//      throw new Exception("Execute basic test failed");
//    }
//  }
//
//  private void testDeployContract(Web3j web3j, Credentials credentials) throws Exception {
//    Ok okDemo = Ok.deploy(web3j, credentials, gasPrice, gasLimit).send();
//    if (okDemo != null) {
//      System.out.println(
//          "####get nonce from Block: "
//              + web3j
//                  .getBlockByNumber(DefaultBlockParameter.valueOf(new BigInteger("0")), true)
//                  .send()
//                  .getBlock()
//                  .getNonce());
//      System.out.println(
//          "####get block number by index from Block: "
//              + web3j
//                  .getBlockByNumber(DefaultBlockParameter.valueOf(new BigInteger("1")), true)
//                  .send()
//                  .getBlock()
//                  .getNumber());
//
//      System.out.println("####contract address is: " + okDemo.getContractAddress());
//      // TransactionReceipt receipt = okDemo.trans(new
//      // BigInteger("4")).sendAsync().get(60000, TimeUnit.MILLISECONDS);
//      TransactionReceipt receipt = okDemo.trans(new BigInteger("4")).send();
//      List<Ok.TransEventEventResponse> events = okDemo.getTransEventEvents(receipt);
//      events.stream().forEach(System.out::println);
//
//      System.out.println("###callback trans success");
//
//      System.out.println(
//          "####get block number from TransactionReceipt: " + receipt.getBlockNumber());
//      System.out.println(
//          "####get transaction index from TransactionReceipt: " + receipt.getTransactionIndex());
//      System.out.println("####get gas used from TransactionReceipt: " + receipt.getGasUsed());
//      // System.out.println("####get cumulative gas used from TransactionReceipt: " +
//      // receipt.getCumulativeGasUsed());
//
//      BigInteger toBalance = okDemo.get().send();
//      System.out.println("============to balance:" + toBalance.intValue());
//    }
//  }
}
