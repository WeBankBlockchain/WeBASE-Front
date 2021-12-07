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
import com.webank.webase.front.base.enums.NodeStatus;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.entity.NodeStatusInfo;
import com.webank.webase.front.web3api.entity.RspStatBlock;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock.Block;
import org.fisco.bcos.sdk.client.protocol.response.BcosGroupInfo.GroupInfo;
import org.fisco.bcos.sdk.client.protocol.response.BcosGroupNodeInfo.GroupNodeInfo;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus.ConsensusStatusInfo;
import org.fisco.bcos.sdk.client.protocol.response.Peers;
import org.fisco.bcos.sdk.client.protocol.response.SealerList.Sealer;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.PeersInfo;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.fisco.bcos.sdk.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.client.protocol.response.TotalTransactionCount.TransactionCountInfo;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Web3Api manage.
 */
@Slf4j
@Service
public class Web3ApiService {

//    @Autowired
//    private Stack<BcosSDK> bcosSDKs;
    @Autowired
    @Qualifier("rpcClient")
    private Client rpcWeb3j;
    @Autowired
    @Qualifier("clientMap")
    private Map<String, Client> clientMap;
    @Autowired
    private Web3Config web3ConfigConstants;

    private static Map<String, String> NODE_ID_2_NODE_NAME = new ConcurrentHashMap<>();
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
        BcosBlock.Block block = getWeb3j(groupId).getBlockByNumber(blockNumber, false, fullTrans)
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

        BigInteger result = getWeb3j(groupId).getPbftView().getPbftView();
        return result;
    }

    /**
     * @param groupId
     * @return
     */
    public BigInteger getPbftView(String groupId, String nodeName) {

        BigInteger result = getWeb3j(groupId).getPbftView().getPbftView();
        return result;
    }

    /**
     * getTransactionReceipt.
     *
     * @param transHash transHash
     */
    public TransactionReceipt getTransactionReceipt(String groupId, String transHash) {

        TransactionReceipt transactionReceipt = getWeb3j(groupId)
                .getTransactionReceipt(transHash,false).getTransactionReceipt();
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
     * getCode.
     *
     * @param address address
     * @param blockNumber blockNumber
     */
    public String getCode(String groupId, String address, BigInteger blockNumber) {
        if (blockNumberCheck(groupId, blockNumber)) {
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        String code = getWeb3j(groupId)
                .getCode(address).getCode();
        log.debug("getCode code:{}", code);
        return code;
    }

    /**
     * get transaction counts.
     */
    public TransactionCountInfo getTransCnt(String groupId) {
        TotalTransactionCount.TransactionCountInfo transactionCount = getWeb3j(groupId)
            .getTotalTransactionCount()
            .getTotalTransactionCount();

        return transactionCount;
    }

    private boolean blockNumberCheck(String groupId, BigInteger blockNumber) {
        BigInteger currentNumber = getWeb3j(groupId).getBlockNumber().getBlockNumber();
        log.debug("**** currentNumber:{}", currentNumber);
        return (blockNumber.compareTo(currentNumber) > 0);

    }

    /**
     * node status list of sealer and observer
     */
    public List<NodeStatusInfo> getNodeStatusList(String groupId) {
        log.info("start getNodeStatusList. groupId:{}", groupId);
        List<NodeStatusInfo> statusList = new ArrayList<>();

        // include observer, sealer, exclude removed nodes
        this.refreshNodeNameMap(groupId);
        log.info("getNodeStatusList NODE_ID_2_NODE_NAME:{}", JsonUtils.objToString(NODE_ID_2_NODE_NAME));
        // get local node
        for (String nodeId : NODE_ID_2_NODE_NAME.keySet()) {
            String nodeName = NODE_ID_2_NODE_NAME.get(nodeId);
            log.info("getNodeStatusList nodeId:{},nodeName:{}", nodeId, nodeName);
            // check nodeType if observer or sealer

            ConsensusStatusInfo consensusStatusInfo = this.getConsensusStatus(groupId, nodeName);
            log.info("getNodeStatusList consensusStatusInfo{}", consensusStatusInfo);
            int blockNumber = consensusStatusInfo.getBlockNumber();
            // if timeout true, view increase
            int view = consensusStatusInfo.getView();

            // normal => timeout false, else true
            boolean ifTimeout = consensusStatusInfo.getTimeout();
            // if timeout, check if node is syncing;
            // if syncing, always timeout until syncing finish
            NodeStatus status = NodeStatus.NORMAL;
            if (ifTimeout) {
                status = NodeStatus.INVALID;
                SyncStatusInfo syncStatusInfo = this.getSyncStatus(groupId, nodeName);
                if (syncStatusInfo.getIsSyncing()) {
                    status = NodeStatus.SYNCING;
                }
            }
            // check node status
            statusList.add(new NodeStatusInfo(nodeId, status, blockNumber, view));
        }

        SyncStatusInfo syncStatusInfo = this.getSyncStatus(groupId);
        long highestBlockNum = syncStatusInfo.getKnownHighestNumber();
        for (PeersInfo peersInfo : syncStatusInfo.getPeers()) {
            if (!NODE_ID_2_NODE_NAME.containsKey(peersInfo.getNodeId())) {
                // todo syncStatusInfo check not connected node's block number
                // todo check block number is changing to set normal or invalid
                long peerBlockNum = peersInfo.getBlockNumber();
                NodeStatus status = peerBlockNum == highestBlockNum ? NodeStatus.NORMAL: NodeStatus.INVALID;
                statusList.add(new NodeStatusInfo(peersInfo.getNodeId(), status, peerBlockNum, 0));
            }
        }

        log.info("end getNodeStatusList. groupId:{} statusList:{}", groupId,
                JsonUtils.toJSONString(statusList));
        return statusList;

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


    public List<GroupNodeInfo> getGroupNodeInfo(String groupId) {
        List<GroupNodeInfo> groupNodeInfos = getWeb3j(groupId).getGroupInfo().getResult().getNodeList();
        return groupNodeInfos;
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
     * get BasicConsensusInfo, include block height, timeout, view
     * @param groupId
     * @return
     */
    public ConsensusStatusInfo getConsensusStatus(String groupId) {
        return getWeb3j(groupId).getConsensusStatus().getConsensusStatus();
    }

    /**
     * getConsensusStatus by node name
     * @param groupId
     * @param nodeName
     * @return
     */
    public ConsensusStatusInfo getConsensusStatus(String groupId, String nodeName) {
        log.info("getConsensusStatus groupId{},nodeName:{}", groupId, nodeName);
        return getWeb3j(groupId).getConsensusStatus(nodeName).getConsensusStatus();
    }

    public SyncStatusInfo getSyncStatus(String groupId) {
        return getWeb3j(groupId).getSyncStatus().getSyncStatus();
    }

    public SyncStatusInfo getSyncStatus(String groupId, String nodeName) {
        return getWeb3j(groupId).getSyncStatus(nodeName).getSyncStatus();
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
                .getSystemConfig().getValue();
    }


    public int getPendingTransactionsSize(String groupId) {
        return getWeb3j(groupId).getPendingTxSize().getPendingTxSize().intValue();
    }

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
     * search By Criteria
     */
    public Object searchByCriteria(String groupId, String input) {
        if (StringUtils.isBlank(input)) {
            log.warn("fail searchByCriteria. input is null");
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
        if (StringUtils.isNumeric(input)) {
            return getBlockByNumber(groupId, new BigInteger(input),false);
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
        return rpcWeb3j;
    }

    /**
     * get target group's web3j
     * @param groupId
     * @return
     */
    public Client getWeb3j(String groupId) {
        Client client = clientMap.get(groupId);
        if (client == null) {
            List<String> groupList = this.getGroupList();
            if (!groupList.contains(groupId)) {
                log.error("getClient group id not exist! groupId:{}", groupId);
                throw new FrontException(ConstantCode.GROUPID_NOT_EXIST);
            }
            // else, groupList contains this groupId, try to build new client
            try {
                Client clientNew = Client.build(groupId, web3ConfigConstants.getConfigOptionFromFile());
                log.info("getClient clientNew:{}", clientNew);
                clientMap.put(groupId, clientNew);
                return clientNew;
            } catch (ConfigException | JniException e) {
                log.error("build new client of groupId:{} failed:{}", groupId, e);
                throw new FrontException(ConstantCode.BUILD_NEW_CLIENT_FAILED);
            }
        }
        return client;
    }

    public CryptoSuite getCryptoSuite(String groupId) {
        return this.getWeb3j(groupId).getCryptoSuite();
    }

    public Integer getCryptoType(String groupId) {
        return this.getWeb3j(groupId).getCryptoType();
    }

    private void refreshNodeNameMap(String groupId) {
        log.info("refreshNodeNameMap groupId:{}", groupId);
        // add all node into map, and set nodeId as its default nodeName
        List<GroupNodeInfo> nodeInfoList = this.getGroupInfo(groupId).getNodeList();
        for (GroupNodeInfo node : nodeInfoList) {
            NODE_ID_2_NODE_NAME.put(node.getIniConfig().getNodeID(), node.getName());
        }
        log.info("end refreshNodeNameMap groupId:{}", groupId);
    }
}
