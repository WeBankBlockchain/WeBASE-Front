/**
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
package com.webank.webase.front.web3api;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.Web3Config;
import com.webank.webase.front.base.enums.DataStatus;
import com.webank.webase.front.base.enums.NodeTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.web3api.entity.NodeStatusInfo;
import com.webank.webase.front.web3api.entity.RspStatBlock;
import com.webank.webase.front.web3api.entity.RspTransCountInfo;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.BcosSDKException;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock.Block;
import org.fisco.bcos.sdk.client.protocol.response.BcosGroupInfo.GroupInfo;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus.ConsensusStatusInfo;
import org.fisco.bcos.sdk.client.protocol.response.Peers;
import org.fisco.bcos.sdk.client.protocol.response.SealerList.Sealer;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.PeersInfo;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.fisco.bcos.sdk.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * Web3Api manage.
 */
@Slf4j
@Service
public class Web3ApiService {

    @Autowired
    private Constants constants;
    @Autowired
    private Stack<BcosSDK> bcosSDKs;
    @Autowired
    private Client rpcWeb3j;
    @Autowired
    private Web3Config web3ConfigConstants;

    private static Map<String, List<NodeStatusInfo>> nodeStatusMap = new HashMap<>();
    private static final Long CHECK_NODE_WAIT_MIN_MILLIS = 5000L;
    private static final int HASH_OF_TRANSACTION_LENGTH = 66;


    /**
     * getBlockNumber.
     */
    public BigInteger getBlockNumber(String groupId) {

        BigInteger blockNumber = getWeb3j(groupId).getBlockNumber().getBlockNumber();
        return blockNumber;
    }

    /**
     * getBlockByNumber.
     *
     * @param blockNumber blockNumber
     */
    public BcosBlock.Block getBlockByNumber(String groupId, BigInteger blockNumber, boolean fullTrans) {
        if (blockNumberCheck(groupId, blockNumber)) {
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        BcosBlock.Block block;
        block = getWeb3j(groupId)
                .getBlockByNumber(blockNumber, false, fullTrans)
                .getBlock();
        return block;
    }

    /**
     * getBlockByHash.
     *
     * @param blockHash blockHash
     */
    public BcosBlock.Block getBlockByHash(String groupId, String blockHash, boolean fullTrans) {
        BcosBlock.Block block = getWeb3j(groupId).getBlockByHash(blockHash,
            false, fullTrans)
                .getBlock();
        return block;
    }

    /**
     * getBlockTransCntByNumber.
     *
     * @param blockNumber blockNumber
     */
    public int getBlockTransCntByNumber(String groupId, BigInteger blockNumber) {
        int transCnt;
        if (blockNumberCheck(groupId, blockNumber)) {
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        Block block = getWeb3j(groupId)
                .getBlockByNumber(blockNumber, false, false)
                .getBlock();
        transCnt = block.getTransactions().size();
        return transCnt;
    }

    /**
     * getPbftView.
     */
    public BigInteger getPbftView(String groupId) {

        BigInteger result;
        result = getWeb3j(groupId).getPbftView().getPbftView();
        return result;
    }

    /**
     * getTransactionReceipt.
     *
     * @param transHash transHash
     */
    public TransactionReceipt getTransactionReceipt(String groupId, String transHash) {

        TransactionReceipt transactionReceipt = null;
        Optional<TransactionReceipt> opt = getWeb3j(groupId)
                .getTransactionReceipt(transHash,false).getTransactionReceipt();
        if (opt.isPresent()) {
            transactionReceipt = opt.get();
        }
        CommonUtils.processReceiptHexNumber(transactionReceipt);
        return transactionReceipt;
    }

    /**
     * getTransactionByHash.
     *
     * @param transHash transHash
     */
    public JsonTransactionResponse getTransactionByHash(String groupId, String transHash, boolean withProof) {

        JsonTransactionResponse transaction = null;
        Optional<JsonTransactionResponse> opt =
                getWeb3j(groupId).getTransaction(transHash, withProof).getTransaction();
        if (opt.isPresent()) {
            transaction = opt.get();
        }
        return transaction;
    }

    /**
     * getClientVersion. todo
//     */
//    public ClientVersion getClientVersion(String groupId) {
//        ClientVersion version = getWeb3j(groupId).get()).getNodeVersion();
//        return version;
//    }

    /**
     * getCode.
     *
     * @param address address
     * @param blockNumber blockNumber
     */
    public String getCode(String groupId, String address, BigInteger blockNumber) {
        String code;
        if (blockNumberCheck(groupId, blockNumber)) {
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        code = getWeb3j(groupId)
                .getCode(address).getCode();
        return code;
    }

    /**
     * get transaction counts.
     */
    public RspTransCountInfo getTransCnt(String groupId) {
        TotalTransactionCount.TransactionCountInfo transactionCount;
        transactionCount = getWeb3j(groupId)
                .getTotalTransactionCount()
                .getTotalTransactionCount();
        String txSumHex = transactionCount.getTransactionCount();
        String blockNumberHex = transactionCount.getBlockNumber();
        String failedTxSumHex = transactionCount.getFailedTransactionCount();
        RspTransCountInfo txCountResult = new RspTransCountInfo(Numeric.toBigInt(txSumHex),
            Numeric.toBigInt(blockNumberHex), Numeric.toBigInt(failedTxSumHex));
        return txCountResult;
    }

    /**
     * getTransByBlockHashAndIndex.
     *
     * @param blockHash blockHash
     * @param transactionIndex index
     */
//    public JsonTransactionResponse getTransByBlockHashAndIndex(String groupId, String blockHash,
//                                                   BigInteger transactionIndex) {
//
//        JsonTransactionResponse transaction = null;
//        Optional<JsonTransactionResponse> opt = getWeb3j(groupId)
//                .getTransactionByBlockHashAndIndex(blockHash, transactionIndex)
//                .getTransaction();
//        if (opt.isPresent()) {
//            transaction = opt.get();
//        }
//        CommonUtils.processTransHexNumber(transaction);
//        return transaction;
//    }

    /**
     * getTransByBlockNumberAndIndex.
     *
     * @param blockNumber blockNumber
     */
//    public JsonTransactionResponse getTransByBlockNumberAndIndex(String groupId, BigInteger blockNumber,
//                                                     BigInteger transactionIndex) {
//        JsonTransactionResponse transaction = null;
//        if (blockNumberCheck(groupId, blockNumber)) {
//            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
//        }
//        Optional<JsonTransactionResponse> opt =
//                getWeb3j(groupId)
//                        .getTransactionByBlockNumberAndIndex(blockNumber, transactionIndex)
//                        .getTransaction();
//        if (opt.isPresent()) {
//            transaction = opt.get();
//        }
//        CommonUtils.processTransHexNumber(transaction);
//        return transaction;
//    }

    private boolean blockNumberCheck(String groupId, BigInteger blockNumber) {
        BigInteger currentNumber = getWeb3j(groupId).getBlockNumber().getBlockNumber();
        log.debug("**** currentNumber:{}", currentNumber);
        return (blockNumber.compareTo(currentNumber) > 0);

    }


    /**
     * nodeHeartBeat.
     */
//    public List<NodeStatusInfo> getNodeStatusList(String groupId) {
//        log.info("start getNodeStatusList. groupId:{}", groupId);
//        try {
//            List<NodeStatusInfo> statusList = new ArrayList<>();
//            SyncStatusInfo syncStatusInfo = this.getSyncStatus(groupId);
//            // include observer and sealer, exclude removed nodes
//            List<String> nodeListInGroup = syncStatusInfo.getPeers().stream()
//                .map(PeersInfo::getNodeId)
//                .collect(Collectors.toList());
//            List<String> observerList = getObserverList(groupId);
//            if (nodeListInGroup.isEmpty()) {
//                log.info("end getNodeStatusList. nodeListInGroup is empty");
//                return Collections.emptyList();
//            }
//            for (String peer : nodeListInGroup) {
//                int nodeType = NodeTypes.SEALER.getValue();
//                // check nodeType if observer or sealer
//                if (observerList != null) {
//                    nodeType = observerList.stream()
//                        .filter(peer::equals)
//                        .map(c -> NodeTypes.OBSERVER.getValue()).findFirst()
//                        .orElse(NodeTypes.SEALER.getValue());
//                }
//                long blockNumberOnChain = getBlockNumberOfNodeOnChain(syncStatusInfo, peer);
//                // check timeout
//                // check syncing
//                String latestView =
//                    viewInfoList.stream().filter(cl -> peer.equals(cl.getNodeId()))
//                                .map(ViewInfo::getView).findFirst().orElse("0");// pbftView
//                // check node status
//                statusList.add(
//                        checkNodeStatus(groupId, peer, blockNumberOnChain, latestView, nodeType));
//            }
//
//            nodeStatusMap.put(groupId, statusList);
//            log.info("end getNodeStatusList. groupId:{} statusList:{}", groupId,
//                    JsonUtils.toJSONString(statusList));
//            return statusList;
//        } catch (Exception e) {
//            log.error("nodeHeartBeat Exception.", e);
//            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
//        }
//    }


    /**
     * check node status.
     */
    private NodeStatusInfo checkNodeStatus(String groupId, String nodeId, long chainBlockNumber,
        long chainView, int nodeType) {
        log.info("start checkNodeStatus. groupId:{} nodeId:{} blockNumber:{} chainView:{}", groupId,
                nodeId, chainBlockNumber, chainView);

        if (Objects.isNull(nodeStatusMap.get(groupId))) {
            log.info("end checkNodeStatus. no cache group:{}", groupId);
            return new NodeStatusInfo(nodeId, chainBlockNumber, chainView);
        } else {
            List<NodeStatusInfo> statusList = nodeStatusMap.get(groupId);
            NodeStatusInfo localNodeStatus = statusList.stream()
                    .filter(s -> nodeId.equals(s.getNodeId())).findFirst().orElse(null);
            if (Objects.isNull(localNodeStatus)) {
                log.info("end checkNodeStatus. no cache node:{}", nodeId);
                return new NodeStatusInfo(nodeId, chainBlockNumber, chainView);
            }

            LocalDateTime latestUpdate = localNodeStatus.getLatestStatusUpdateTime();
            Long subTime = Duration.between(latestUpdate, LocalDateTime.now()).toMillis();
            if (subTime < CHECK_NODE_WAIT_MIN_MILLIS) {
                log.info("checkNodeStatus jump over. nodeId:{} subTime:{}", nodeId, subTime);
                return localNodeStatus;
            }

            long localBlockNumber = localNodeStatus.getBlockNumber();
            long localPbftView = localNodeStatus.getPbftView();
            // 0-consensus;1-observer
            if (nodeType == NodeTypes.SEALER.getValue()) {
                if (localBlockNumber == chainBlockNumber && localPbftView == chainView) {
                    log.warn("node[{}] is invalid. localNumber:{} chainNumber:{} localView:{} chainView:{}",
                            nodeId, localBlockNumber, chainBlockNumber, localPbftView, chainView);
                    localNodeStatus.setStatus(DataStatus.INVALID.getValue());
                } else {
                    localNodeStatus.setBlockNumber(chainBlockNumber);
                    localNodeStatus.setPbftView(chainView);
                    localNodeStatus.setStatus(DataStatus.NORMAL.getValue());
                }
            } else {
                if (!(chainBlockNumber == getBlockNumber(groupId).longValue())) {
                    log.warn(
                            "node[{}] is invalid. localNumber:{} chainNumber:{} localView:{} chainView:{}",
                            nodeId, localBlockNumber, chainBlockNumber, localPbftView, chainView);
                    localNodeStatus.setStatus(DataStatus.INVALID.getValue());
                } else {
                    localNodeStatus.setBlockNumber(chainBlockNumber);
                    localNodeStatus.setPbftView(chainView);
                    localNodeStatus.setStatus(DataStatus.NORMAL.getValue());
                }
            }
            localNodeStatus.setLatestStatusUpdateTime(LocalDateTime.now());
            return localNodeStatus;
        }
    }


    /**
     * get latest number of peer on chain.
     */
    private long getBlockNumberOfNodeOnChain(SyncStatusInfo syncStatus, String nodeId) {
        if (Objects.isNull(syncStatus)) {
            log.warn("fail getBlockNumberOfNodeOnChain. SyncStatus is null");
            return 0L;
        }
        if (StringUtils.isBlank(nodeId)) {
            log.warn("fail getBlockNumberOfNodeOnChain. nodeId is null");
            return 0L;
        }
        if (nodeId.equals(syncStatus.getNodeId())) {
            return syncStatus.getBlockNumber();
        }
        List<PeersInfo> peerList = syncStatus.getPeers();
        // blockNumber
        long latestNumber = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
                .map(PeersInfo::getBlockNumber).findFirst().orElse(0L);
        return latestNumber;
    }


    public GroupInfo getGroupInfo(String groupId) {
        GroupInfo groupInfo = getWeb3j(groupId).getGroupInfo().getResult();
        return groupInfo;
    }

    /**
     * get group list and refresh web3j map
     * @return
     */
    public List<String> getGroupList() {
        log.debug("getGroupList. ");
        List<String> groupIdList = getWeb3j()
            .getGroupList().getResult()
            .getGroupList();
        return groupIdList;
    }

    /**
     * node id list
     */
    public List<String> getGroupPeers(String groupId) {
        return getWeb3j(groupId)
            .getGroupPeers().getGroupPeers();
    }

    // get all peers of chain
    public Peers.PeersInfo getPeers(String groupId) {
        Peers.PeersInfo peers = getWeb3j(groupId)
                .getPeers().getPeers();
        return peers;
    }

    /**
     * get BasicConsensusInfo and List of ViewInfo todo
     * @param groupId
     * @return
     */
    public ConsensusStatusInfo getConsensusStatus(String groupId) {
        return getWeb3j(groupId).getConsensusStatus().getConsensusStatus();
    }

    public SyncStatusInfo getSyncStatus(String groupId) {
        return getWeb3j(groupId).getSyncStatus().getSyncStatus();
    }

    /**
     * get getSystemConfigByKey of tx_count_limit/tx_gas_limit
     * @param groupId
     * @param key
     * @return value for config, ex: return 1000
     */
    public String getSystemConfigByKey(String groupId, String key) {
        return getWeb3j(groupId)
                .getSystemConfigByKey(key)
                .getSystemConfig().getValue(); // todo
    }

    /**
     * get node config info
     * @return todo
     */
//    public Object getNodeConfig() {
//        return JsonUtils.toJavaObject(nodeConfig.toString(), Object.class);
//    }

    public int getPendingTransactionsSize(String groupId) {
        return getWeb3j(groupId).getPendingTxSize().getPendingTxSize().intValue();
    }

    // todo sealer: nodeId and weight
    public List<Sealer> getSealerList(String groupId) {
        return getWeb3j(groupId).getSealerList().getSealerList();
    }

    public List<String> getSealerStrList(String groupId) {
        List<Sealer> sealers = this.getSealerList(groupId);
        return sealers.stream().map(Sealer::getNodeID).collect(Collectors.toList());

    }

    public List<String> getObserverList(String groupId) {
        return getWeb3j(groupId).getObserverList().getObserverList();
    }

    /**
     * search By Criteria todo add search
     */
    public Object searchByCriteria(String groupId, String input) {
        if (StringUtils.isBlank(input)) {
            log.warn("fail searchByCriteria. input is null");
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
        if (StringUtils.isNumeric(input)) {
            return getBlockByNumber(groupId, new BigInteger(input),true);
        } else if (input.length() == HASH_OF_TRANSACTION_LENGTH) {
            JsonTransactionResponse txResponse = getTransactionByHash(groupId, input, true);
            return txResponse;
        }
        return null;
    }


    /**
     * getBlockTransCntByNumber.
     *
     * @param blockNumber blockNumber
     */
    public RspStatBlock getBlockStatisticByNumber(String groupId, BigInteger blockNumber) {
        if (blockNumberCheck(groupId, blockNumber)) {
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        Block block = getWeb3j(groupId)
            .getBlockByNumber(blockNumber, false, false)
            .getBlock();
        int transCnt = block.getTransactions().size();
        Long timestamp = block.getTimestamp();
        return new RspStatBlock(blockNumber, timestamp, transCnt);
    }



    /**
     * get first web3j in web3jMap
     * @return
     */
    public Client getWeb3j() {
        List<String> groupIdList = rpcWeb3j.getGroupList().getResult().getGroupList(); //1
        if (groupIdList.isEmpty()) {
            log.error("Node's groupList empty! please check your node status");
            // get default web3j of integer max value
            throw new FrontException(ConstantCode.SYSTEM_ERROR_GROUP_LIST_EMPTY);
        }
        // get random index to get web3j
        String index = groupIdList.iterator().next();
        return bcosSDKs.peek().getClient(index);
    }

    /**
     * get target group's web3j
     * @param groupId
     * @return
     */
    public Client getWeb3j(String groupId) {
        Client web3j;
        try {
            web3j= bcosSDKs.peek().getClient(groupId);
        } catch (BcosSDKException e) {
            String errorMsg = e.getMessage();
            log.error("bcosSDK getClient failed: {}", errorMsg);
            // check client error type
            if (errorMsg.contains("available peers")) {
                log.error("no available node to connect to");
                throw new FrontException(ConstantCode.SYSTEM_ERROR_NODE_INACTIVE.getCode(),
                    "no available node to connect to");
            }
            if (errorMsg.contains("existence of group")) {
                log.error("group: {} of the connected node not exist!", groupId);
                throw new FrontException(ConstantCode.SYSTEM_ERROR_WEB3J_NULL.getCode(),
                    "group: " + groupId + " of the connected node not exist!");
            }
            if (errorMsg.contains("no peers set up the group")) {
                log.error("no peers belong to this group: {}!", groupId);
                throw new FrontException(ConstantCode.SYSTEM_ERROR_NO_NODE_IN_GROUP.getCode(),
                    "no peers belong to this group: " + groupId);
            }
            throw new FrontException(ConstantCode.WEB3J_CLIENT_IS_NULL);
        }
        return web3j;
    }

    public CryptoSuite getCryptoSuite(String groupId) {
        return this.getWeb3j(groupId).getCryptoSuite();
    }

    public Integer getCryptoType(String groupId) {
        return this.getWeb3j(groupId).getCryptoType();
    }

//    private void checkConnection() { todo
//        if (!Web3Config.PEER_CONNECTED) {
//            throw new FrontException(ConstantCode.SYSTEM_ERROR_NODE_INACTIVE);
//        }
//    }

}
