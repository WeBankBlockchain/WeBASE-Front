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
package com.webank.webase.front.base;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.GroupList;
import org.fisco.bcos.sdk.client.protocol.response.GroupPeers;
import org.fisco.bcos.sdk.client.protocol.response.Peers;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.junit.Ignore;
import org.junit.Test;

public class BasicTest extends TestBase {

  @Ignore
  @Test
  public void pbftViewTest() {
    int i = web3j.getPbftView().getPbftView().intValue();
    System.out.println(i);
    assertNotNull(i > 0);
  }

  @Test
  public void consensusStatusTest() {
    System.out.println(web3j.getConsensusStatus().getConsensusStatus());
    assertNotNull(web3j.getConsensusStatus().getConsensusStatus());
  }

  @Test
  public void getBlockByNumber() {
    BigInteger blockNumber = web3j.getBlockNumber().getBlockNumber();
    BcosBlock bcosBlock = web3j.getBlockByNumber(blockNumber,true);
    System.out.println(bcosBlock.getBlock());
  }

  @Test
  public void syncTest() {
    System.out.println(web3j.getSyncStatus().getSyncStatus());
    assertNotNull(web3j.getSyncStatus().getSyncStatus());
  }

  @Test
  public void versionTest() {
    ClientVersion web3ClientVersion = web3j.getNodeVersion().getNodeVersion();
    System.out.println(web3ClientVersion);
    assertNotNull(web3ClientVersion);
  }

  // getPeers
  @Ignore
  @Test
  public void peersTest() {
    Peers ethPeers = web3j.getPeers();
    System.out.println(ethPeers.getPeers().get(0).getNodeID());
    assertNotNull(ethPeers);
  }

  @Test
  public void groupPeersTest() {
    GroupPeers groupPeers = web3j.getGroupPeers();
    groupPeers.getGroupPeers().forEach(System.out::println);
    assertNotNull(groupPeers.getResult());
  }

  @Test
  public void groupListTest() {
    GroupList groupList = web3j.getGroupList();
    groupList.getGroupList().stream().forEach(System.out::println);
    assertTrue((groupList.getGroupList().size() > 0));
  }

  @Ignore
  @Test
  public void getTransactionByBlockNumberAndIndexTest() {
    BigInteger blockNumber = web3j.getBlockNumber().getBlockNumber();
    JsonTransactionResponse transaction =
        web3j.getTransactionByBlockNumberAndIndex(blockNumber, new BigInteger("0")).getTransaction().get();
    System.out.println(transaction.calculateHash(web3j.getCryptoSuite()));
  }

//  @Test
//  public void basicTest() {
//    try {
//      testDeployContract(web3j, credentials);
//    } catch (Exception e) {
//      e.printStackTrace();
//      throw new Exception("Execute basic test failed");
//    }
//  }
//
//  private void testDeployContract(Web3j web3j, Credentials credentials) {
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
