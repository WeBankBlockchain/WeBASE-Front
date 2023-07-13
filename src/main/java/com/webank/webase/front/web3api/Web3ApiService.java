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
import com.webank.webase.front.web3api.entity.FrontNodeConfig;
import com.webank.webase.front.web3api.entity.NodeStatusInfo;
import com.webank.webase.front.web3api.entity.RspStatBlock;
import com.webank.webase.front.web3api.entity.TransactionInfo;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
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
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.exceptions.ClientException;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock.Block;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupInfo.GroupInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupNodeInfo.GroupNodeInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.ConsensusStatus.ConsensusStatusInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.GroupPeers;
import org.fisco.bcos.sdk.v3.client.protocol.response.Peers;
import org.fisco.bcos.sdk.v3.client.protocol.response.SealerList.Sealer;
import org.fisco.bcos.sdk.v3.client.protocol.response.SyncStatus.PeersInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.v3.client.protocol.response.TotalTransactionCount.TransactionCountInfo;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Web3Api manage.
 */
@Slf4j
@Service
public class Web3ApiService {

    @Autowired
    @Qualifier("rpcClient")
    private Client rpcWeb3j;
    @Autowired
    private BcosSDK bcosSDK;
    @Autowired
    private Web3Config web3Config;

    /**
     * nodes connected with front, key:nodeId, value:nodeName
     */
    private static final int HASH_OF_TRANSACTION_LENGTH = 66;
    /**
     * key: nodeId, value: nodeStatusInfo cached
     */
    private static Map<String, NodeStatusInfo> NODE_STATUS_MAP = new ConcurrentHashMap<>();
    private static final int CHECK_NODE_STATUS_INTERVAL_TIME = 3500; //ms
    /**
     * key: groupId, value, boolean of whether available
     */
    private static Map<String, Boolean> GROUP_AVAILABLE_MAP = new ConcurrentHashMap<>();


    /**
     * get first web3j in web3jMap
     * @return
     */
    public Client getWeb3j() {
        return rpcWeb3j;
    }

    /**
     * check client connected with group and get target group's web3j
     * @param groupId
     * @return
     */
    public Client getWeb3j(String groupId) {
        // check group avaible
        if (GROUP_AVAILABLE_MAP.get(groupId) != null && !GROUP_AVAILABLE_MAP.get(groupId)) {
            log.error("getWeb3j peers of this groupId:[{}] not connected", groupId);
            throw new FrontException(ConstantCode.CLIENT_NOT_CONNECTED_WITH_THIS_GROUP);
        }
//        return bcosSDK.getClient(groupId);
        return getWeb3jRaw(groupId);
    }

    /**
     * get target group's web3j directly
     * @param groupId
     * @return
     */
    private Client getWeb3jRaw(String groupId) throws FrontException {
        Client client = bcosSDK.getClient(groupId);
//        Client client = clientMap.get(groupId);
//        if (client == null) {
//            List<String> groupList = this.getGroupList();
//            if (!groupList.contains(groupId)) {
//                log.error("getClient group id not exist! groupId:{}", groupId);
//                throw new FrontException(ConstantCode.GROUPID_NOT_EXIST);
//            }
//            // else, groupList contains this groupId, try to build new client
//            try {
//                Client clientNew = Client.build(groupId, web3ConfigConstants.getConfigOptionFromFile());
//                log.info("getClient clientNew:{}", clientNew);
//                clientMap.put(groupId, clientNew);
//                return clientNew;
//            } catch (ConfigException | JniException e) {
//                log.error("build new client of groupId:{} failed:{}", groupId, e);
//                throw new FrontException(ConstantCode.BUILD_NEW_CLIENT_FAILED);
//            }
//        }
        return client;
    }

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
        try {
            BcosBlock.Block block = getWeb3j(groupId).getBlockByHash(blockHash,
                false, fullTrans)
                .getBlock();
            return block;
        } catch (ClientException ex) {
            throw new FrontException(ConstantCode.BLOCK_NOT_EXIST_ERROR);
        }
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
     * getTransactionReceipt.
     *
     * @param transHash transHash
     */
    public TransactionReceipt getTransactionReceipt(String groupId, String transHash) {
        try {
            TransactionReceipt transactionReceipt = getWeb3j(groupId)
                .getTransactionReceipt(transHash, false).getTransactionReceipt();
            return transactionReceipt;
        } catch (ClientException ex) {
            throw new FrontException(ConstantCode.TX_RECEIPT_NOT_EXIST_ERROR);
        }
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
        return code == null ? "" : code;
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
        // include observer, sealer, exclude removed nodes
        Map<String, String> nodeIdNameMap = this.getNodeIdNameMap(groupId);

        LocalDateTime now = LocalDateTime.now();
        log.debug("getNodeStatusList NODE_ID_2_NODE_NAME:{},now:{}", JsonUtils.objToString(nodeIdNameMap), now);
        // 1. get nodes connected with front
        for (String nodeId : nodeIdNameMap.keySet()) {
            String nodeName = nodeIdNameMap.get(nodeId);
            log.debug("getNodeStatusList nodeId:{},nodeName:{}", nodeId, nodeName);

            ConsensusStatusInfo consensusStatusInfo = this.getConsensusStatus(groupId, nodeName);
            log.debug("getNodeStatusList consensusStatusInfo{}", consensusStatusInfo);
            int blockNumber = consensusStatusInfo.getBlockNumber();

            // normal => timeout false, else true
            boolean ifTimeout = consensusStatusInfo.getTimeout();
            // if timeout, check if node is syncing;
            // if syncing, always timeout until syncing finish
            NodeStatus status = NodeStatus.NORMAL;
            if (ifTimeout) {
                SyncStatusInfo syncStatusInfo = this.getSyncStatus(groupId, nodeName);
                status = syncStatusInfo.getIsSyncing() ? NodeStatus.SYNCING : NodeStatus.INVALID ;
            }
            // check node status
            NODE_STATUS_MAP.put(nodeId, new NodeStatusInfo(nodeId, status, blockNumber, now));
        }

        // 2. check nodes not connected with front
        SyncStatusInfo syncStatusInfo = this.getSyncStatus(groupId);
        long highestBlockNum = syncStatusInfo.getKnownHighestNumber();
        for (PeersInfo peersInfo : syncStatusInfo.getPeers()) {
            String nodeId = peersInfo.getNodeId();
            long peerBlockNum = peersInfo.getBlockNumber();
            // to check nodes that not connected with front
            if (!nodeIdNameMap.containsKey(peersInfo.getNodeId())) {
                // check block number is changing to set normal or invalid
                if (peerBlockNum == highestBlockNum) {
                    NODE_STATUS_MAP.put(nodeId, new NodeStatusInfo(nodeId, NodeStatus.NORMAL, peerBlockNum, now));
                } else {
                    // if not equal highest block number, check by caching
                    // if cache is null, default normal and set as INVALID for block num not equal highest
                    if (NODE_STATUS_MAP.get(nodeId) == null) {
                        NODE_STATUS_MAP.put(nodeId, new NodeStatusInfo(nodeId, NodeStatus.INVALID, peerBlockNum, now));
                    } else {
                        // if not equal highest block num, check whether over 3000ms interval,
                        // if within interval: only update block num, and not update status,
                        // else beyond interval:  check block number whether changing and update status
                        NodeStatusInfo oldStatusInfo = NODE_STATUS_MAP.get(nodeId);
                        Long subTime = Duration.between(oldStatusInfo.getModifyTime(), now).toMillis();
                        if (subTime < (NODE_STATUS_MAP.size() * 500 + CHECK_NODE_STATUS_INTERVAL_TIME)) {
                            log.warn("checkNodeStatus skip for time internal subTime:{}", subTime);
                            // only update block num, not update time and status
                            oldStatusInfo.setBlockNumber(peerBlockNum);
                        } else {
                            // if block num not changing ,invalid
                            if (peerBlockNum == oldStatusInfo.getBlockNumber()) {
                                oldStatusInfo.setBlockNumber(peerBlockNum);
                                oldStatusInfo.setStatus(NodeStatus.INVALID.getValue());
                            } else {
                                oldStatusInfo.setBlockNumber(peerBlockNum);
                                oldStatusInfo.setStatus(NodeStatus.NORMAL.getValue());
                            }
                        }
                        // update cache
                        NODE_STATUS_MAP.put(nodeId, oldStatusInfo);
                    }
                }
            }
        }
        List<NodeStatusInfo> statusList = new ArrayList<>();
        List<String> groupPeers = this.getGroupPeers(groupId);
        for (String nodeId : NODE_STATUS_MAP.keySet()) {
            if (groupPeers.contains(nodeId)) {
                statusList.add(NODE_STATUS_MAP.get(nodeId));
            }
        }
        log.info("end getNodeStatusList. groupId:{} statusList:{}", groupId, JsonUtils.objToString(statusList));
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

    /**
     * get group list and refresh web3j map
     * @return
     */
    public List<String> getGroupList() {
        log.debug("getGroupList. ");
        List<String> groupIdList = getWeb3j()
            .getGroupList().getResult()
            .getGroupList();
        log.debug("end getGroupList :{}", groupIdList);
        return groupIdList;
    }

    /**
     * node id list
     */
    public List<String> getGroupPeers(String groupId) {
        Instant startTime = Instant.now();
        List<String> groupPeers = getWeb3j(groupId)
            .getGroupPeers().getGroupPeers();
        log.info("getGroupPeers groupId:{},{}", groupId, Duration.between(startTime, Instant.now()).toMillis());
        return groupPeers;
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
     * @mark： only support query from connected node
     * @param groupId
     * @param nodeName
     * @return
     */
    private ConsensusStatusInfo getConsensusStatus(String groupId, String nodeName) {
        Instant startTime = Instant.now();
        log.info("getConsensusStatus groupId{},nodeName:{}", groupId, nodeName);
        ConsensusStatusInfo consensusStatusInfo = getWeb3j(groupId).getConsensusStatus(nodeName).getConsensusStatus();
        log.info("getConsensusStatus groupId{},nodeName:{},{}", groupId, nodeName, Duration.between(startTime, Instant.now()).toMillis());
        return consensusStatusInfo;
    }

    public SyncStatusInfo getSyncStatus(String groupId) {
        Instant startTime = Instant.now();
        SyncStatusInfo syncStatusInfo = getWeb3j(groupId).getSyncStatus().getSyncStatus();
        log.info("getSyncStatus groupId{},{}", groupId, Duration.between(startTime, Instant.now()).toMillis());
        return syncStatusInfo;
    }

    /**
     * @mark： only support query from connected node
     * @param groupId
     * @param nodeName
     * @return
     */
    private SyncStatusInfo getSyncStatus(String groupId, String nodeName) {
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
        Instant startTime = Instant.now();
        List<Sealer> sealerList = getWeb3j(groupId).getSealerList().getSealerList();
        log.info("getSealerList groupId:{},{}", groupId, Duration.between(startTime, Instant.now()).toMillis());
        return sealerList;
    }

    public List<String> getSealerStrList(String groupId) {
        List<Sealer> sealers = this.getSealerList(groupId);
        return sealers.stream().map(Sealer::getNodeID).collect(Collectors.toList());

    }

    public List<String> getObserverList(String groupId) {
        Instant startTime = Instant.now();
        List<String> observerList = getWeb3j(groupId).getObserverList().getObserverList();
        log.info("getObserverList groupId:{},{}", groupId, Duration.between(startTime, Instant.now()).toMillis());
        return observerList;
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
            // get block number
            TransactionReceipt receipt = getTransactionReceipt(groupId, input);

            TransactionInfo transactionInfo = new TransactionInfo(txResponse,
                receipt.getBlockNumber().toString(10));
            return transactionInfo;
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


    public CryptoSuite getCryptoSuite(String groupId) {
        return this.getWeb3j(groupId).getCryptoSuite();
    }

    public Integer getCryptoType(String groupId) {
        return this.getWeb3j(groupId).getCryptoType();
    }

    public GroupInfo getGroupInfo(String groupId) {
        GroupInfo groupInfo = getWeb3j(groupId).getGroupInfo().getResult();
        return groupInfo;
    }

    public List<GroupNodeInfo> getGroupNodeInfo(String groupId) {
        List<GroupNodeInfo> groupNodeInfos = getWeb3j(groupId).getGroupInfo().getResult().getNodeList();
        return groupNodeInfos;
    }

    public List<String> getNodeList(String groupId) {
        GroupPeers groupPeers = getWeb3j(groupId).getGroupPeers();
        List<String> nodeList =  groupPeers.getGroupPeers();
        return nodeList;
    }

    public Map<String, String> getNodeIdNameMap(String groupId) {
        Instant startTime = Instant.now();
        log.debug("refreshAndGetNodeNameMap groupId:{}", groupId);
        Map<String, String> nodeIdNameMap = new HashMap<>();
        List<GroupNodeInfo> nodeInfoList = this.getGroupNodeInfo(groupId);
        for (GroupNodeInfo node : nodeInfoList) {
            String nodeId = node.getIniConfig().getNodeID();
            nodeIdNameMap.put(nodeId, node.getName());
        }
        log.debug("end refreshAndGetNodeNameMap groupId:{},nodeIdNameMap:{}", groupId, nodeIdNameMap);
        log.info("refreshAndGetNodeNameMap groupId{},{}", groupId, Duration.between(startTime, Instant.now()).toMillis());
        return nodeIdNameMap;
    }

    /**
     * refresh available group map
     */
    public void refreshAvailableGroupMap() {
        List<String> groupList = this.getGroupList();
        if (groupList == null || groupList.isEmpty()) {
            log.warn("refreshAvailableGroupMap group list empty!");
            return;
        }
        for (String groupId : groupList) {
            // group info get from raw client
            List<GroupNodeInfo> nodeInfoList;
            try {
                nodeInfoList = this.getWeb3j(groupId).getGroupInfo().getResult().getNodeList();
            } catch (FrontException e) {
                log.error("refreshAvailableGroupMap get nodeInfoList failed:[]", e);
                continue;
            }
            // if not empty, available true
            GROUP_AVAILABLE_MAP.put(groupId, !nodeInfoList.isEmpty());
        }
    }

    public boolean getIsWasm(String groupId) {
        return this.getWeb3j(groupId).isWASM();
    }

    public List<String> getPeersConfig() {
        List<String> peers = web3Config.getPeers();
        log.info("getPeersConfig peers:{}", peers);
        return peers;
    }

    public FrontNodeConfig getNodeConfig() {
        FrontNodeConfig nodeConfig = new FrontNodeConfig();
        List<String> peers = this.getPeersConfig();
        if (peers == null || peers.isEmpty()) {
            throw new FrontException(ConstantCode.SYSTEM_ERROR_WEB3J_NULL);
        }
        String[] ipPort = peers.get(0).split(":");
        nodeConfig.setP2pip(ipPort[0]);
        nodeConfig.setChannelPort(Integer.parseInt(ipPort[1]));

        log.info("getNodeConfig peers:{},nodeConfig:{}", peers, nodeConfig);
        return nodeConfig;
    }
}
