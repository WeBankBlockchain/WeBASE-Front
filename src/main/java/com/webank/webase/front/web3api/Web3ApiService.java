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
import com.webank.webase.front.base.config.NodeConfig;
import com.webank.webase.front.base.config.Web3Config;
import com.webank.webase.front.base.enums.DataStatus;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.entity.GenerateGroupInfo;
import com.webank.webase.front.web3api.entity.NodeStatusInfo;
import com.webank.webase.front.web3api.entity.RspSearchTransaction;
import com.webank.webase.front.web3api.entity.RspStatBlock;
import com.webank.webase.front.web3api.entity.RspTransCountInfo;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.BcosSDKException;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.model.GroupStatus;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock.Block;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader.BlockHeader;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceiptsDecoder;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus.ConsensusInfo;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus.ViewInfo;
import org.fisco.bcos.sdk.client.protocol.response.GroupPeers;
import org.fisco.bcos.sdk.client.protocol.response.NodeInfo.NodeInformation;
import org.fisco.bcos.sdk.client.protocol.response.Peers;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.PeersInfo;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.fisco.bcos.sdk.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * Web3Api manage.
 */
@Slf4j
@Service
public class Web3ApiService {

    @Autowired
    private NodeConfig nodeConfig;
    @Autowired
    private Constants constants;
    @Autowired
    private BcosSDK bcosSDK;
    @Autowired
    @Qualifier("rpcClient")
    private Client rpcWeb3j;
    @Autowired
    private Web3Config web3ConfigConstants;

    private static Map<Integer, List<NodeStatusInfo>> nodeStatusMap = new HashMap<>();
    private static final Long CHECK_NODE_WAIT_MIN_MILLIS = 5000L;
    private static final int HASH_OF_TRANSACTION_LENGTH = 66;


    /**
     * getBlockNumber.
     */
    public BigInteger getBlockNumber(int groupId) {

        BigInteger blockNumber;
        blockNumber = getWeb3j(groupId).getBlockNumber().getBlockNumber();
        return blockNumber;
    }

    /**
     * getBlockByNumber.
     *
     * @param blockNumber blockNumber
     */
    public BcosBlock.Block getBlockByNumber(int groupId, BigInteger blockNumber) {
        if (blockNumberCheck(groupId, blockNumber)) {
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        BcosBlock.Block block;
        block = getWeb3j(groupId)
                .getBlockByNumber(blockNumber, true)
                .getBlock();
        CommonUtils.processBlockHexNumber(block);
        return block;
    }

    /**
     * getBlockByHash.
     *
     * @param blockHash blockHash
     */
    public BcosBlock.Block getBlockByHash(int groupId, String blockHash) {
        BcosBlock.Block block = getWeb3j(groupId).getBlockByHash(blockHash, true)
                .getBlock();
        CommonUtils.processBlockHexNumber(block);
        return block;
    }

    /**
     * getBlockTransCntByNumber.
     *
     * @param blockNumber blockNumber
     */
    public int getBlockTransCntByNumber(int groupId, BigInteger blockNumber) {
        int transCnt;
        if (blockNumberCheck(groupId, blockNumber)) {
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        Block block = getWeb3j(groupId)
                .getBlockByNumber(blockNumber, false)
                .getBlock();
        transCnt = block.getTransactions().size();
        return transCnt;
    }

    /**
     * getPbftView.
     */
    public BigInteger getPbftView(int groupId) {

        BigInteger result;
        result = getWeb3j(groupId).getPbftView().getPbftView();
        return result;
    }

    /**
     * getTransactionReceipt.
     *
     * @param transHash transHash
     */
    public TransactionReceipt getTransactionReceipt(int groupId, String transHash) {

        TransactionReceipt transactionReceipt = null;
        Optional<TransactionReceipt> opt = getWeb3j(groupId)
                .getTransactionReceipt(transHash).getTransactionReceipt();
        if (opt.isPresent()) {
            transactionReceipt = opt.get();
        }
        CommonUtils.decodeReceipt(transactionReceipt, getWeb3j(groupId).getCryptoSuite());
        CommonUtils.processReceiptHexNumber(transactionReceipt);
        return transactionReceipt;
    }

    /**
     * getTransactionByHash.
     *
     * @param transHash transHash
     */
    public JsonTransactionResponse getTransactionByHash(int groupId, String transHash) {

        JsonTransactionResponse transaction = null;
        Optional<JsonTransactionResponse> opt =
                getWeb3j(groupId).getTransactionByHash(transHash).getTransaction();
        if (opt.isPresent()) {
            transaction = opt.get();
        }
        CommonUtils.processTransHexNumber(transaction);
        return transaction;
    }

    /**
     * getClientVersion.
     */
    public ClientVersion getClientVersion() {
        ClientVersion version = getWeb3j().getNodeVersion(getNodeIpPort()).getNodeVersion();
        return version;
    }

    /**
     * getCode.
     *
     * @param address address
     * @param blockNumber blockNumber
     */
    public String getCode(int groupId, String address, BigInteger blockNumber) {
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
    public RspTransCountInfo getTransCnt(int groupId) {
        TotalTransactionCount.TransactionCountInfo transactionCount;
        transactionCount = getWeb3j(groupId)
                .getTotalTransactionCount()
                .getTotalTransactionCount();
        String txSumHex = transactionCount.getTxSum();
        String blockNumberHex = transactionCount.getBlockNumber();
        String failedTxSumHex = transactionCount.getFailedTxSum();
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
    public JsonTransactionResponse getTransByBlockHashAndIndex(int groupId, String blockHash,
                                                   BigInteger transactionIndex) {

        JsonTransactionResponse transaction = null;
        Optional<JsonTransactionResponse> opt = getWeb3j(groupId)
                .getTransactionByBlockHashAndIndex(blockHash, transactionIndex)
                .getTransaction();
        if (opt.isPresent()) {
            transaction = opt.get();
        }
        CommonUtils.processTransHexNumber(transaction);
        return transaction;
    }

    /**
     * getTransByBlockNumberAndIndex.
     *
     * @param blockNumber blockNumber
     * @param transactionIndex index
     */
    public JsonTransactionResponse getTransByBlockNumberAndIndex(int groupId, BigInteger blockNumber,
                                                     BigInteger transactionIndex) {
        JsonTransactionResponse transaction = null;
        if (blockNumberCheck(groupId, blockNumber)) {
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        Optional<JsonTransactionResponse> opt =
                getWeb3j(groupId)
                        .getTransactionByBlockNumberAndIndex(blockNumber, transactionIndex)
                        .getTransaction();
        if (opt.isPresent()) {
            transaction = opt.get();
        }
        CommonUtils.processTransHexNumber(transaction);
        return transaction;
    }

    private boolean blockNumberCheck(int groupId, BigInteger blockNumber) {
        BigInteger currentNumber = null;
        currentNumber = getWeb3j(groupId).getBlockNumber().getBlockNumber();
        log.debug("**** currentNumber:{}", currentNumber);
        return (blockNumber.compareTo(currentNumber) > 0);

    }


    /**
     * nodeHeartBeat.
     */
    public List<NodeStatusInfo> getNodeStatusList(int groupId) {
        log.info("start getNodeStatusList. groupId:{}", groupId);
        try {
            List<NodeStatusInfo> statusList = new ArrayList<>();
            List<String> peerStrList = getGroupPeers(groupId);
            List<String> observerList = getObserverList(groupId);
            SyncStatusInfo syncStatusInfo = this.getSyncStatus(groupId);
            List<ViewInfo> viewInfoList = getPeerOfConsensusStatus(groupId);
            if (Objects.isNull(peerStrList) || peerStrList.isEmpty()) {
                log.info("end getNodeStatusList. peerStrList is empty");
                return Collections.emptyList();
            }
            for (String peer : peerStrList) {
                // 0-consensus;1-observer
                int nodeType = 0;
                if (observerList != null) {
                    nodeType = observerList.stream().filter(peer::equals)
                            .map(c -> 1).findFirst().orElse(0);
                }
                BigInteger blockNumberOnChain = getBlockNumberOfNodeOnChain(syncStatusInfo, peer);
                String latestView =
                    viewInfoList.stream().filter(cl -> peer.equals(cl.getNodeId()))
                                .map(ViewInfo::getView).findFirst().orElse("0");// pbftView
                // check node status
                statusList.add(
                        checkNodeStatus(groupId, peer, blockNumberOnChain, new BigInteger(latestView), nodeType));
            }

            nodeStatusMap.put(groupId, statusList);
            log.info("end getNodeStatusList. groupId:{} statusList:{}", groupId,
                    JsonUtils.toJSONString(statusList));
            return statusList;
        } catch (Exception e) {
            log.error("nodeHeartBeat Exception.", e);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
    }

    /**
     * check node status.
     */
    private NodeStatusInfo checkNodeStatus(int groupId, String nodeId, BigInteger chainBlockNumber,
                                           BigInteger chainView, int nodeType) {
        log.info("start checkNodeStatus. groupId:{} nodeId:{} blockNumber:{} chainView:{}", groupId,
                nodeId, chainBlockNumber, chainView);

        if (Objects.isNull(nodeStatusMap.get(groupId))) {
            log.info("end checkNodeStatus. no cache group:{}", groupId);
            return new NodeStatusInfo(nodeId, chainBlockNumber, chainView,
                    DataStatus.NORMAL.getValue(), LocalDateTime.now());
        } else {
            List<NodeStatusInfo> statusList = nodeStatusMap.get(groupId);
            NodeStatusInfo localNodeStatus = statusList.stream()
                    .filter(s -> nodeId.equals(s.getNodeId())).findFirst().orElse(null);
            if (Objects.isNull(localNodeStatus)) {
                log.info("end checkNodeStatus. no cache node:{}", nodeId);
                return new NodeStatusInfo(nodeId, chainBlockNumber, chainView,
                        DataStatus.NORMAL.getValue(), LocalDateTime.now());
            }

            LocalDateTime latestUpdate = localNodeStatus.getLatestStatusUpdateTime();
            Long subTime = Duration.between(latestUpdate, LocalDateTime.now()).toMillis();
            if (subTime < CHECK_NODE_WAIT_MIN_MILLIS) {
                log.info("checkNodeStatus jump over. nodeId:{} subTime:{}", nodeId, subTime);
                return localNodeStatus;
            }

            BigInteger localBlockNumber = localNodeStatus.getBlockNumber();
            BigInteger localPbftView = localNodeStatus.getPbftView();
            // 0-consensus;1-observer
            if (nodeType == 0) {
                if (localBlockNumber.equals(chainBlockNumber) && localPbftView.equals(chainView)) {
                    log.warn(
                            "node[{}] is invalid. localNumber:{} chainNumber:{} localView:{} chainView:{}",
                            nodeId, localBlockNumber, chainBlockNumber, localPbftView, chainView);
                    localNodeStatus.setStatus(DataStatus.INVALID.getValue());
                } else {
                    localNodeStatus.setBlockNumber(chainBlockNumber);
                    localNodeStatus.setPbftView(chainView);
                    localNodeStatus.setStatus(DataStatus.NORMAL.getValue());
                }
            } else {
                if (!chainBlockNumber.equals(getBlockNumber(groupId))) {
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
    private BigInteger getBlockNumberOfNodeOnChain(SyncStatusInfo syncStatus, String nodeId) {
        if (Objects.isNull(syncStatus)) {
            log.warn("fail getBlockNumberOfNodeOnChain. SyncStatus is null");
            return BigInteger.ZERO;
        }
        if (StringUtils.isBlank(nodeId)) {
            log.warn("fail getBlockNumberOfNodeOnChain. nodeId is null");
            return BigInteger.ZERO;
        }
        if (nodeId.equals(syncStatus.getNodeId())) {
            return new BigInteger(syncStatus.getBlockNumber());
        }
        List<PeersInfo> peerList = syncStatus.getPeers();
        // blockNumber
        String latestNumber = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
                .map(PeersInfo::getBlockNumber).findFirst().orElse("0");
        return new BigInteger(latestNumber);
    }


    /**
     * get peer of consensusStatus
     */
    private List<ViewInfo> getPeerOfConsensusStatus(int groupId) {
        ConsensusInfo consensusStatus = getConsensusStatus(groupId);
        return consensusStatus.getViewInfos();
//        if (StringUtils.isBlank(consensusStatusJson)) {
//            return Collections.emptyList();
//        }
//        List jsonArr = JsonUtils.toJavaObject(consensusStatusJson, List.class);
//        if (jsonArr == null) {
//            log.error("getPeerOfConsensusStatus error");
//            throw new FrontException(ConstantCode.FAIL_PARSE_JSON);
//        }
//        List<PeerOfConsensusStatus> dataIsList = new ArrayList<>();
//        for (int i = 0; i < jsonArr.size(); i++ ) {
//            if (jsonArr.get(i) instanceof List) {
//                List<PeerOfConsensusStatus> tempList = ;
//                if (tempList != null) {
//                    dataIsList.addAll(tempList);
//                } else {
//                    throw new FrontException(ConstantCode.FAIL_PARSE_JSON);
//                }
//            }
//        }
//        return dataIsList;
    }


    public List<String> getGroupPeers(int groupId) {
        GroupPeers groupPeers = null;
        groupPeers = getWeb3j(groupId)
                .getGroupPeers();
        return groupPeers.getGroupPeers();
    }

    /**
     * get group list and refresh web3j map
     * @return
     */
    public List<String> getGroupList() {
        log.info("getGroupList. ");
        List<String> groupIdList = getWeb3j().getGroupList(getNodeIpPort()).getGroupList();
        // check web3jMap, if not match groupIdList, refresh web3jMap in front
        refreshWeb3jMap(groupIdList);
        return groupIdList;
    }

    public List<String> getNodeIdList() {
        return getWeb3j()
                .getNodeIDList(getNodeIpPort())
                .getNodeIDList();
    }

    /**
     * add web3j from chain and remove web3j not in chain
     * todo use getClient init new web3j, remove old one
     * @param groupIdList
     * @throws FrontException
     */
    @DependsOn("encryptType")
    public void refreshWeb3jMap(List<String> groupIdList) throws FrontException {
        // todo whether need to init new client
        log.debug("refreshWeb3jMap groupIdList:{}", groupIdList);
        // if localGroupIdList not contain group in groupList from chain, add it
        groupIdList.forEach(group2Init ->
                bcosSDK.getClient(Integer.parseInt(group2Init)));
                //initWeb3j(Integer.parseInt(group2Init)));

        // Set<Integer> localGroupIdList = web3jMap.keySet();
        // todo whether need to stop client manually, 需要catch get的异常
        //log.debug("refreshWeb3jMap localGroupList:{}", localGroupIdList);
        // if local web3j map contains group that not in groupList from chain
        // remove it from local web3j map
//        localGroupIdList.stream()
//            // not contains in groupList from chain
//            .filter(groupId ->
//                !groupIdList.contains(String.valueOf(groupId)))
//            .forEach(group2Remove ->
//                bcosSDK.getClient(group2Remove).stop());
    }

    // get all peers of chain
    public List<Peers.PeerInfo> getPeers(int groupId) {
        return getWeb3j(groupId)
                .getPeers()
                .getPeers();
    }

    /**
     * get BasicConsensusInfo and List of ViewInfo
     * @param groupId
     * @return
     */
    public ConsensusInfo getConsensusStatus(int groupId) {
        return getWeb3j(groupId).getConsensusStatus().getConsensusStatus();
    }

    public SyncStatusInfo getSyncStatus(int groupId) {
        return getWeb3j(groupId).getSyncStatus().getSyncStatus();
    }

    /**
     * get getSystemConfigByKey of tx_count_limit/tx_gas_limit
     * @param groupId
     * @param key
     * @return value for config, ex: return 1000
     */
    public String getSystemConfigByKey(int groupId, String key) {
        return getWeb3j(groupId)
                .getSystemConfigByKey(key)
                .getSystemConfig();
    }

    /**
     * getNodeInfo.
     */
    public NodeInformation getNodeInfo() {
        return getWeb3j().getNodeInfo(getNodeIpPort()).getNodeInfo();
    }

    /**
     * get node config info
     * @return
     */
    public Object getNodeConfig() {
        return JsonUtils.toJavaObject(nodeConfig.toString(), Object.class);
    }

    public int getPendingTransactions(int groupId) {
        return getWeb3j(groupId)
                .getPendingTransaction()
                .getPendingTransactions().size();
    }

    public BigInteger getPendingTransactionsSize(int groupId) {
        return getWeb3j(groupId).getPendingTxSize().getPendingTxSize();
    }

    public List<String> getSealerList(int groupId) {
        return getWeb3j(groupId).getSealerList().getSealerList();
    }

    public List<String> getObserverList(int groupId) {
        return getWeb3j(groupId).getObserverList().getObserverList();
    }

    /**
     * search By Criteria
     */
    public Object searchByCriteria(int groupId, String input) {
        if (StringUtils.isBlank(input)) {
            log.warn("fail searchByCriteria. input is null");
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
        if (StringUtils.isNumeric(input)) {
            return getBlockByNumber(groupId, new BigInteger(input));
        } else if (input.length() == HASH_OF_TRANSACTION_LENGTH) {
            JsonTransactionResponse txResponse = getTransactionByHash(groupId, input);
            BlockHeader blockHeader = getBlockHeaderByNumber(groupId, txResponse.getBlockNumber(), false);
            RspSearchTransaction rspSearchTransaction = new RspSearchTransaction(blockHeader.getTimestamp(), txResponse);
            return rspSearchTransaction;
        }
        return null;
    }

    /**
     * dynamic group management
     */

    public Object generateGroup(GenerateGroupInfo generateGroupInfo) {
        log.debug("start generateGroup. groupId:{}", generateGroupInfo.getGenerateGroupId());
        GroupStatus status = getWeb3j()
            .generateGroup(generateGroupInfo.getGenerateGroupId(),
                generateGroupInfo.getTimestamp().longValue(), true,
                generateGroupInfo.getNodeList(),
                bcosSDK.getConfig().getNetworkConfig().getPeers().get(0))
            .getGroupStatus();
        log.info("generateGroup. groupId:{} status:{}", generateGroupInfo.getGenerateGroupId(),
                status);
        if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCEED);
        } else {
            log.error("generateGroup failed:{}", status.getMessage());
            throw classifyGroupOperateException(status);
        }
    }

    public Object operateGroup(int groupId, String type) {
        log.debug("start operateGroup. groupId:{} type:{}", groupId, type);
        switch (type) {
            case Constants.OPERATE_GROUP_START:
                return startGroup(groupId);
            case Constants.OPERATE_GROUP_STOP:
                return stopGroup(groupId);
            case Constants.OPERATE_GROUP_REMOVE:
                return removeGroup(groupId);
            case Constants.OPERATE_GROUP_RECOVER:
                return recoverGroup(groupId);
            case Constants.OPERATE_GROUP_GET_STATUS:
                return querySingleGroupStatus(groupId);
            default:
                log.error("end operateGroup. invalid operate type");
                throw new FrontException(ConstantCode.INVALID_GROUP_OPERATE_TYPE);
        }
    }

    private Object startGroup(int groupId) {
        GroupStatus status = getWeb3j()
            .startGroup(groupId, bcosSDK.getConfig().getNetworkConfig().getPeers().get(0))
            .getGroupStatus();
        log.info("startGroup. groupId:{} status:{}", groupId, status);
        if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCEED);
        } else {
            log.error("startGroup fail:{}", status.getMessage());
            throw classifyGroupOperateException(status);
        }
    }

    private Object stopGroup(int groupId) {
        GroupStatus status = getWeb3j()
            .stopGroup(groupId, bcosSDK.getConfig().getNetworkConfig().getPeers().get(0))
            .getGroupStatus();
        log.info("stopGroup. groupId:{} status:{}", groupId, status);
        if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCEED);
        } else {
            log.error("stopGroup fail:{}", status.getMessage());
            throw classifyGroupOperateException(status);
        }
    }

    private Object removeGroup(int groupId) {
        GroupStatus status = getWeb3j()
            .removeGroup(groupId, bcosSDK.getConfig().getNetworkConfig().getPeers().get(0))
            .getGroupStatus();
        log.info("removeGroup. groupId:{} status:{}", groupId, status);
        if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCEED);
        } else {
            log.error("removeGroup fail:{}", status.getMessage());
            throw classifyGroupOperateException(status);
        }
    }

    private Object recoverGroup(int groupId) {
        GroupStatus status = getWeb3j()
            .recoverGroup(groupId, bcosSDK.getConfig().getNetworkConfig().getPeers().get(0))
            .getGroupStatus();
        log.info("recoverGroup. groupId:{} status:{}", groupId, status);
        if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCEED);
        } else {
            log.error("recoverGroup fail:{}", status.getMessage());
            throw classifyGroupOperateException(status);
        }
    }

    /**
     * classify group operate exception code
     * @param status
     * @return FrontException
     */
    private FrontException classifyGroupOperateException(GroupStatus status) {
        int groupOperateStatusCode = CommonUtils.parseHexStr2Int(status.getCode());
        switch (groupOperateStatusCode) {
            case 1:
                return new FrontException(ConstantCode.NODE_INTERNAL_ERROR);
            case 2:
                return new FrontException(ConstantCode.GROUP_ALREADY_EXISTS);
            case 3:
                return new FrontException(ConstantCode.GROUP_ALREADY_RUNNING);
            case 4:
                return new FrontException(ConstantCode.GROUP_ALREADY_STOPPED);
            case 5:
                return new FrontException(ConstantCode.GROUP_ALREADY_DELETED);
            case 6:
                return new FrontException(ConstantCode.GROUP_NOT_FOUND);
            case 7:
                return new FrontException(ConstantCode.GROUP_OPERATE_INVALID_PARAMS);
            case 8:
                return new FrontException(ConstantCode.PEERS_NOT_CONNECTED);
            case 9:
                return new FrontException(ConstantCode.GENESIS_CONF_ALREADY_EXISTS);
            case 10:
                return new FrontException(ConstantCode.GROUP_CONF_ALREADY_EXIST);
            case 11:
                return new FrontException(ConstantCode.GENESIS_CONF_NOT_FOUND);
            case 12:
                return new FrontException(ConstantCode.GROUP_CONF_NOT_FOUND);
            case 13:
                return new FrontException(ConstantCode.GROUP_IS_STOPPING);
            case 14:
                return new FrontException(ConstantCode.GROUP_NOT_DELETED);
            default:
                return new FrontException(ConstantCode.GROUP_OPERATE_FAIL);
        }
    }

    public BaseResponse querySingleGroupStatus(int groupId) {
        GroupStatus status = getWeb3j()
            .queryGroupStatus(groupId, bcosSDK.getConfig().getNetworkConfig().getPeers().get(0))
            .getGroupStatus();
        log.info("queryGroupStatus. groupId:{} status:{}", groupId, status);
        if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
            BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
            response.setData(status.getStatus());
            return response;
        } else {
            log.error("queryGroupStatus fail:{}", status.getMessage());
            throw classifyGroupOperateException(status);
        }
    }

    /**
     * Map of <groupId, status>
     * @param groupIdList
     * @return status: "INEXISTENT"、"STOPPING"、"RUNNING"、"STOPPED"、"DELETED"
     * @throws IOException
     */
    public BaseResponse getGroupStatus(List<Integer> groupIdList) {
        Map<Integer, String> groupIdStatusMap = new HashMap<>(groupIdList.size());
        for (Integer groupId: groupIdList) {
            BaseResponse res = querySingleGroupStatus(groupId);
            groupIdStatusMap.put(groupId, (String)res.getData());
        }
        return new BaseResponse(ConstantCode.RET_SUCCESS, groupIdStatusMap);
    }

    /* above v2.6.1*/
    public BlockHeader getBlockHeaderByHash(Integer groupId, String blockHash,
        boolean returnSealers) {
        BlockHeader blockHeader = getWeb3j(groupId).getBlockHeaderByHash(blockHash, returnSealers).getBlockHeader();
        CommonUtils.processBlockHeaderHexNumber(blockHeader);
        return blockHeader;
    }

    public BlockHeader getBlockHeaderByNumber(Integer groupId, BigInteger blockNumber,
        boolean returnSealers) {
        BlockHeader blockHeader = getWeb3j(groupId).getBlockHeaderByNumber(blockNumber, returnSealers).getBlockHeader();
        CommonUtils.processBlockHeaderHexNumber(blockHeader);
        return blockHeader;
    }
    /* above v2.6.1*/

    /**
     * getBlockTransCntByNumber.
     *
     * @param blockNumber blockNumber
     */
    public RspStatBlock getBlockStatisticByNumber(int groupId, BigInteger blockNumber) {
        if (blockNumberCheck(groupId, blockNumber)) {
            throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
        }
        Block block = getWeb3j(groupId)
            .getBlockByNumber(blockNumber, false)
            .getBlock();
        CommonUtils.processBlockHexNumber(block);
        int transCnt = block.getTransactions().size();
        String timestamp = block.getTimestamp();
        return new RspStatBlock(blockNumber, Long.parseLong(timestamp), transCnt);
    }

    /**
     * get batch receipt in one block
     * @param groupId
     * @param blockNumber
     * @param start start index
     * @param count cursor, if -1, return all
     */
    public List<TransactionReceipt> getBatchReceiptByBlockNumber(int groupId, BigInteger blockNumber, int start, int count) {
        BcosTransactionReceiptsDecoder batchReceipts = getWeb3j(groupId)
            .getBatchReceiptsByBlockNumberAndRange(blockNumber, String.valueOf(start), String.valueOf(count));
        return batchReceipts.decodeTransactionReceiptsInfo().getTransactionReceipts();
    }

    public List<TransactionReceipt> getBatchReceiptByBlockHash(int groupId, String blockHash, int start, int count) {
        BcosTransactionReceiptsDecoder batchReceipts = getWeb3j(groupId)
            .getBatchReceiptsByBlockHashAndRange(blockHash, String.valueOf(start), String.valueOf(count));
        return batchReceipts.decodeTransactionReceiptsInfo().getTransactionReceipts();
    }


    /**
     * get first web3j in web3jMap
     *
     * @return
     */
    public Client getWeb3j() {
        this.checkConnection();
        Set<Integer> groupIdSet = bcosSDK.getGroupManagerService().getGroupList(); //1
        if (groupIdSet.isEmpty()) {
            log.error("web3jMap is empty, groupList empty! please check your node status");
            // get default web3j of integer max value
            return rpcWeb3j;
        }
        // get random index to get web3j
        Integer index = groupIdSet.iterator().next();
        return bcosSDK.getClient(index);
    }

    /**
     * get target group's web3j
     * @param groupId
     * @return
     */
    public Client getWeb3j(Integer groupId) {
        this.checkConnection();
        Client web3j;
        try {
            web3j= bcosSDK.getClient(groupId);
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
            // refresh group list
            // getGroupList();
        }
        return web3j;
    }

    private void checkConnection() {
        if (!Web3Config.PEER_CONNECTED) {
            throw new FrontException(ConstantCode.SYSTEM_ERROR_NODE_INACTIVE);
        }
    }

    private String getNodeIpPort() {
        return web3ConfigConstants.getIp() + ":" + web3ConfigConstants.getChannelPort();
    }
}
