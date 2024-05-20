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
package com.webank.webase.front.precntauth.precompiled.consensus;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.precntauth.authmanager.base.AuthMgrBaseService;
import com.webank.webase.front.precntauth.authmanager.committee.CommitteeService;
import com.webank.webase.front.precntauth.precompiled.base.PrecompiledCommonInfo;
import com.webank.webase.front.precntauth.precompiled.base.PrecompiledUtils;
import com.webank.webase.front.precntauth.precompiled.consensus.entity.NodeInfo;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.web3api.Web3ApiService;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.client.protocol.response.SealerList.Sealer;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.webank.webase.front.util.PrecompiledUtils.*;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.Committee.FUNC_SETWEIGHT;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager.FUNC_CREATERMNODEPROPOSAL;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager.FUNC_CREATESETCONSENSUSWEIGHTPROPOSAL;
import static org.fisco.bcos.sdk.v3.contract.precompiled.consensus.ConsensusPrecompiled.*;

/**
 *  Node consensus status service;
 *  Handle transaction through webase-sign.
 */
@Slf4j
@Service
public class ConsensusServiceInWebase {

  @Autowired
  private Web3ApiService web3ApiService;
  @Autowired
  private TransService transService;
  @Autowired
  private AuthMgrBaseService authMgrBaseService;

  /**
   * consensus: add sealer through webase-sign v1.5.0 增加校验群组文件是否存在，P2P连接存在
   */
  public String addSealer(String groupId, String signUserId, String nodeId, BigInteger weight) {
    // check node id
    if (!isValidNodeID(nodeId, groupId)) {
      return ConstantCode.PEERS_NOT_CONNECTED.toString();
    }

    return this.addSealerHandle(groupId, signUserId, nodeId, weight);
  }

  public String addSealerHandle(String groupId, String signUserId, String nodeId,
      BigInteger weight) {
    List<String> sealerList = web3ApiService.getSealerStrList(groupId);
    boolean existed = sealerList.contains(nodeId);
    log.info("addSealerHandle sealer existed is:{}|{}", nodeId, existed);
    List<String> funcParams = new ArrayList<>();
    funcParams.add(nodeId);
    funcParams.add(weight.toString(10));
    String contractAddress;
    boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
    if (isWasm) {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS_LIQUID);
    } else {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS);
    }
    // 如果启用了权限，则一定是solidity且要走Committee合约
    if (authMgrBaseService.chainHasAuth(groupId)) {
      // 开启权限后，用committee预编译合约
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.COMMITTEE_MANAGER);
      log.info("addSealerHandle enable auth, now proposal groupId:{},signUserId:{},nodeId:{}", groupId, signUserId, nodeId);
      // addFlag
      // 如果不存在，就是true，用于add；
      // 如果已存在，则是false，用于更新weight
      funcParams.add(String.valueOf(!existed));
      // blockInterval
      funcParams.add(CommitteeService.DEFAULT_BLOCK_NUMBER_INTERVAL.toString(10));
      String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.COMMITTEE_MANAGER);
      TransactionReceipt receipt =
          (TransactionReceipt) transService.transHandleWithSign(groupId,
              signUserId, contractAddress, abiStr, FUNC_CREATESETCONSENSUSWEIGHTPROPOSAL, funcParams, isWasm);
      return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
    } else {
      String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CONSENSUS);
      // 如果已存在，就是setWeight，如果不存在，就是addSealer
      TransactionReceipt receipt;
      if (existed) {
        receipt =
            (TransactionReceipt) transService.transHandleWithSign(groupId,
                signUserId, contractAddress, abiStr, FUNC_SETWEIGHT, funcParams, isWasm);
      } else {
        receipt =
            (TransactionReceipt) transService.transHandleWithSign(groupId,
                signUserId, contractAddress, abiStr, FUNC_ADDSEALER, funcParams, isWasm);
      }
      return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
    }
  }

  private boolean isValidNodeID(String _nodeID, String groupId) {
    boolean flag = false;
    List<String> nodeList = web3ApiService.getGroupPeers(groupId);
    for (String s : nodeList
    ) {
      if (s.equals(_nodeID)) {
        flag = true;
      }
    }
    return flag;
  }


  public String addObserver(String groupId, String signUserId, String nodeId) {
    // check node id
    if (!isValidNodeID(nodeId, groupId)) {
      return PrecompiledRetCode.CODE_INVALID_NODEID.toString();
    }
    List<String> observerList = web3ApiService.getObserverList(groupId);
    if (observerList.contains(nodeId)) {
      return ConstantCode.ALREADY_EXISTS_IN_OBSERVER_LIST.toString();
    }

    return this.addObserverHandle(groupId, signUserId, nodeId);
  }

  private String addObserverHandle(String groupId, String signUserId, String nodeId) {
    // trans
    List<String> funcParams = new ArrayList<>();
    funcParams.add(nodeId);
    String contractAddress;
    boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
    if (isWasm) {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS_LIQUID);
    } else {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS);
    }
    // 如果启用了权限，则一定是solidity且要走Committee合约
    if (authMgrBaseService.chainHasAuth(groupId)) {
      // 开启权限后，用committee预编译合约
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.COMMITTEE_MANAGER);
      log.info("addObserverHandle enable auth, now proposal groupId:{},signUserId:{},nodeId:{}", groupId, signUserId, nodeId);
      // 观察节点weight是 0
      funcParams.add("0");
      // addFlag
      funcParams.add("true");
      // blockInterval
      funcParams.add(CommitteeService.DEFAULT_BLOCK_NUMBER_INTERVAL.toString(10));
      String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.COMMITTEE_MANAGER);
      TransactionReceipt receipt =
          (TransactionReceipt) transService.transHandleWithSign(groupId,
              signUserId, contractAddress, abiStr, FUNC_CREATESETCONSENSUSWEIGHTPROPOSAL, funcParams, isWasm);
      return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
    } else {
      String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CONSENSUS);
      TransactionReceipt receipt =
          (TransactionReceipt) transService.transHandleWithSign(groupId,
              signUserId, contractAddress, abiStr, FUNC_ADDOBSERVER, funcParams, isWasm);
      return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
    }
  }

  public String removeNode(String groupId, String signUserId, String nodeId) {
    List<String> groupPeers = web3ApiService.getGroupPeers(groupId);
    if (!groupPeers.contains(nodeId)) {
      return ConstantCode.ALREADY_REMOVED_FROM_THE_GROUP.toString();
    }
    // trans
    List<String> funcParams = new ArrayList<>();
    funcParams.add(nodeId);
    String contractAddress;
    boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
    if (isWasm) {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS_LIQUID);
    } else {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS);
    }
    // 如果启用了权限，则一定是solidity且要走Committee合约
    if (authMgrBaseService.chainHasAuth(groupId)) {
      // 开启权限后，用committee预编译合约
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.COMMITTEE_MANAGER);
      log.info("removeNode enable auth, now proposal groupId:{},signUserId:{},nodeId:{}", groupId, signUserId, nodeId);
      // blockInterval
      funcParams.add(CommitteeService.DEFAULT_BLOCK_NUMBER_INTERVAL.toString(10));
      String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.COMMITTEE_MANAGER);
      TransactionReceipt receipt =
          (TransactionReceipt) transService.transHandleWithSign(groupId,
              signUserId, contractAddress, abiStr, FUNC_CREATERMNODEPROPOSAL, funcParams, isWasm);
      return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
    } else {
      String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CONSENSUS);
      TransactionReceipt receipt;
      try {
        receipt = (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_REMOVE, funcParams, isWasm);
      } catch (RuntimeException e) {
        // firstly remove node that sdk connected to the node, return the request that present
        // susscces
        // because the exception is throwed by getTransactionReceipt, we need ignore it.
        if (e.getMessage().contains("Don't send requests to this group")) {
          return ConstantCode.ALREADY_REMOVED_FROM_THE_GROUP.toString();
        } else {
          throw e;
        }
      }
      return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
    }
  }


  public List<NodeInfo> getNodeList(String groupId) {
    // nodeListWithType 组合多个带有类型的nodeid list
    List<Sealer> sealerList = web3ApiService.getSealerList(groupId);

    List<String> sealerStrList = sealerList.stream().map(Sealer::getNodeID)
        .collect(Collectors.toList());

    List<String> observerList = web3ApiService.getObserverList(groupId);
    List<String> peerList = web3ApiService.getGroupPeers(groupId);
    // process nodeList
    List<NodeInfo> nodeListWithType = new ArrayList<>();

    // add all sealer and observer in List
    sealerList.forEach(sealer -> nodeListWithType.add(
        new NodeInfo(sealer.getNodeID(), NODE_TYPE_SEALER, sealer.getWeight())));
    observerList.forEach(
        observer -> nodeListWithType.add(new NodeInfo(observer, NODE_TYPE_OBSERVER)));
    // peer not in sealer/observer but connected is remove node(游离节点)
    peerList.stream()
        .filter(peer -> !sealerStrList.contains(peer) && !observerList.contains(peer))
        .forEach(peerToAdd -> nodeListWithType
            .add(new NodeInfo(peerToAdd, NODE_TYPE_REMOVE)));

    return nodeListWithType;
  }

  public String setWeight(String groupId, String signUserId, String nodeId, BigInteger weight) {
    // check node id
    if (!isValidNodeID(nodeId, groupId)) {
      return PrecompiledRetCode.CODE_INVALID_NODEID.toString();
    }
    List<String> nodeIdList = web3ApiService.getGroupPeers(groupId);
    if (!nodeIdList.contains(nodeId)) {
      log.error("nodeId is not connected with others, cannot set weight");
      return ConstantCode.PEERS_NOT_CONNECTED.toString();
    }
//    if (!containsGroupFile(groupId)) {
//      throw new FrontException(ConstantCode.GENESIS_CONF_NOT_FOUND);
//    }
    return this.setWeightHandle(groupId, signUserId, nodeId, weight);
  }

  public String setWeightHandle(String groupId, String signUserId, String nodeId,
                                BigInteger weight) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(nodeId);
    funcParams.add(weight.toString(10));
    String contractAddress;
    boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
    if (isWasm) {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS_LIQUID);
    } else {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS);
    }
    String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CONSENSUS);
    TransactionReceipt receipt =
            (TransactionReceipt) transService.transHandleWithSign(groupId,
                    signUserId, contractAddress, abiStr, FUNC_SETWEIGHT, funcParams, isWasm);
    return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
  }

}