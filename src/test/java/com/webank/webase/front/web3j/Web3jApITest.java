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
package com.webank.webase.front.web3j;


import static org.junit.Assert.assertNotNull;

import com.webank.webase.front.base.TestBase;
import java.math.BigInteger;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceipt;
import org.fisco.bcos.sdk.client.protocol.response.BlockHash;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.client.protocol.response.Code;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus;
import org.fisco.bcos.sdk.client.protocol.response.GroupList;
import org.fisco.bcos.sdk.client.protocol.response.GroupPeers;
import org.fisco.bcos.sdk.client.protocol.response.NodeIDList;
import org.fisco.bcos.sdk.client.protocol.response.ObserverList;
import org.fisco.bcos.sdk.client.protocol.response.PbftView;
import org.fisco.bcos.sdk.client.protocol.response.Peers;
import org.fisco.bcos.sdk.client.protocol.response.PendingTransactions;
import org.fisco.bcos.sdk.client.protocol.response.PendingTxSize;
import org.fisco.bcos.sdk.client.protocol.response.SealerList;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus;
import org.fisco.bcos.sdk.client.protocol.response.SystemConfig;
import org.fisco.bcos.sdk.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.junit.Ignore;
import org.junit.Test;

public class Web3jApITest extends TestBase {

  @Test
  public void getNodeVersion() { 
      ClientVersion nodeVersion = web3j.getNodeVersion().getNodeVersion();
      assertNotNull(nodeVersion);
  }

  @Test
  public void getBlockNumber() {
      BlockNumber blockNumber = web3j.getBlockNumber();
      assertNotNull(blockNumber.getBlockNumber());
  }

  @Test
  public void getBlockNumberCache() {
      BigInteger blockNumberCache = web3j.getBlockLimit();
      assertNotNull(blockNumberCache);
  }

  @Test
  public void pbftView() {
    PbftView pbftView = web3j.getPbftView();
    assertNotNull(pbftView.getPbftView());
  }

  @Test
  public void getConsensusStatus() { 
      ConsensusStatus consensusStatus = web3j.getConsensusStatus();
      assertNotNull(consensusStatus);
  }

  @Test
  public void getSyncStatus() {
      SyncStatus syncStatus = web3j.getSyncStatus();
      assertNotNull(syncStatus);
  }

  @Test
  public void peers() {
    Peers peers = web3j.getPeers();
    assertNotNull(peers.getPeers());
  }

  @Test
  public void groupPeers() {
    GroupPeers groupPeers = web3j.getGroupPeers();
    assertNotNull(groupPeers.getGroupPeers());
  }

  @Test
  public void groupList() {
    GroupList groupList = web3j.getGroupList();
    assertNotNull(groupList.getGroupList());
  }

  @Test
  public void getSealerList() {
      SealerList sealerList = web3j.getSealerList();
      assertNotNull(sealerList.getSealerList());
  }

  @Test
  public void getObserverList() {
      ObserverList observerList = web3j.getObserverList();
      assertNotNull(observerList.getObserverList());
  }

  @Test
  public void getNodeIDList() {
      NodeIDList nodeIDList = web3j.getNodeIDList();
      assertNotNull(nodeIDList.getNodeIDList());
  }

  @Test
  public void getSystemConfigByKey() {
      SystemConfig txCountLimit = web3j.getSystemConfigByKey("tx_count_limit");
      SystemConfig txGasLimit = web3j.getSystemConfigByKey("tx_gas_limit");
      assertNotNull(txCountLimit.getSystemConfig());
      assertNotNull(txGasLimit.getSystemConfig());
  }

  @Test
  public void getCode() {
      Code code = web3j.getCode(address);
      assertNotNull(code.getCode());
  }

  @Test
  public void getTotalTransactionCount() {
      TotalTransactionCount count = web3j.getTotalTransactionCount();
      assertNotNull(count.getTotalTransactionCount());
  }

    @Ignore
  @Test
  public void getBlockByHash() {
      BcosBlock bcosBlock = web3j.getBlockByHash(blockHash, true);
      assertNotNull(bcosBlock.getBlock());
  }

  @Ignore
  @Test
  public void getBlockByNumber() {
      BcosBlock bcosBlock = web3j.getBlockByNumber(blockNumber, true);
      assertNotNull(bcosBlock.getBlock());
  }

  @Ignore
  @Test
  public void getBlockHashByNumber() {
      BlockHash blockHash = web3j.getBlockHashByNumber(blockNumber);
      assertNotNull(blockHash.getBlockHashByNumber());
  }

  @Ignore
  @Test
  public void getTransactionByHash() {
      BcosTransaction bcosTransaction = web3j.getTransactionByHash(blockHash);
      assertNotNull(bcosTransaction.getTransaction());
  }

    @Ignore
    @Test
    public void getTransactionByBlockNumberAndIndex() {
      BcosTransaction bcosTransaction = web3j.getTransactionByBlockNumberAndIndex(blockNumber, new BigInteger("0"));
      JsonTransactionResponse transaction = bcosTransaction.getTransaction().get();
      assertNotNull(transaction);
    }

    @Ignore
    @Test
    public void getTransactionByBlockHashAndIndex() {
      BcosTransaction bcosTransaction = web3j.getTransactionByBlockHashAndIndex(blockHash, new BigInteger("0"));
      JsonTransactionResponse transaction = bcosTransaction.getTransaction().get();
      assertNotNull(transaction);
    }

  @Ignore
  @Test
  public void getTransactionReceipt() {
      BcosTransactionReceipt bcosTransactionReceipt = web3j.getTransactionReceipt(blockHash);
      TransactionReceipt transactionReceipt = bcosTransactionReceipt.getTransactionReceipt().get();
        assertNotNull(transactionReceipt);
  }

  @Test
  public void getPendingTransaction() {
      PendingTransactions pendingTransactions = web3j.getPendingTransaction();
      assertNotNull(pendingTransactions.getPendingTransactions());
  }

  @Test
  public void getPendingTxSize() {
      PendingTxSize pendingTxSize = web3j.getPendingTxSize();
      assertNotNull(pendingTxSize.getPendingTxSize());
  }

}
