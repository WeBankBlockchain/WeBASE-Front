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
import com.webank.webase.front.web3api.entity.GroupOperateStatus;
import com.webank.webase.front.web3api.entity.NodeStatusInfo;
import com.webank.webase.front.web3api.entity.PeerOfConsensusStatus;
import com.webank.webase.front.web3api.entity.PeerOfSyncStatus;
import com.webank.webase.front.web3api.entity.SyncStatus;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.GroupPeers;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion.Version;
import org.fisco.bcos.web3j.protocol.core.methods.response.Peers;
import org.fisco.bcos.web3j.protocol.core.methods.response.TotalTransactionCount;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * Web3Api manage.
 */
@Slf4j
@Service
public class Web3ApiService {

    @Autowired
    Map<Integer, Web3j> web3jMap;
    @Autowired
    NodeConfig nodeConfig;
    @Autowired
    GroupChannelConnectionsConfig groupChannelConnectionsConfig;
    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    Constants constants;
    @Autowired
    Web3Config web3Config;
    @Autowired
    Web3j independentWeb3j;

    private static Map<Integer, List<NodeStatusInfo>> nodeStatusMap = new HashMap<>();
    private static final Long CHECK_NODE_WAIT_MIN_MILLIS = 5000L;
    private static final int HASH_OF_TRANSACTION_LENGTH = 66;

    /**
     * getBlockNumber.
     */
    public BigInteger getBlockNumber(int groupId) {

        BigInteger blockNumber;
        try {
            blockNumber = getWeb3j(groupId).getBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            log.error("getBlockNumber fail.", e);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
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
        try {
            block = getWeb3j(groupId)
                    .getBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
                    .send()
                    .getBlock();
        } catch (IOException e) {
            log.info("get blocknumber failed" + e.getMessage());
            log.error("getBlAockByNumber fail. blockNumber:{} , groupID: {}", blockNumber, groupId);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return block;
    }

    /**
     * getBlockByHash.
     *
     * @param blockHash blockHash
     */
    public BcosBlock.Block getBlockByHash(int groupId, String blockHash) {
        BcosBlock.Block block;
        try {

            block = getWeb3j(groupId).getBlockByHash(blockHash, true)
                    .send()
                    .getBlock();
        } catch (IOException e) {
            log.error("getBlockByHash fail. blockHash:{} ", blockHash);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return block;
    }

    /**
     * getBlockTransCntByNumber.
     *
     * @param blockNumber blockNumber
     */
    public int getBlockTransCntByNumber(int groupId, BigInteger blockNumber) {
        int transCnt;
        try {
            if (blockNumberCheck(groupId, blockNumber)) {
                throw new FrontException("ConstantCode.NODE_REQUEST_FAILED");
            }
            BcosBlock.Block block = getWeb3j(groupId)
                    .getBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
                    .send()
                    .getBlock();
            transCnt = block.getTransactions().size();
        } catch (IOException e) {
            log.error("getBlockTransCntByNumber fail. blockNumber:{} ", blockNumber);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transCnt;
    }

    /**
     * getPbftView.
     */
    public BigInteger getPbftView(int groupId) {

        BigInteger result;
        try {
            result = getWeb3j(groupId).getPbftView().send().getPbftView();
        } catch (IOException e) {
            log.error("getPbftView fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return result;
    }

    /**
     * getTransactionReceipt.
     *
     * @param transHash transHash
     */
    public TransactionReceipt getTransactionReceipt(int groupId, String transHash) {

        TransactionReceipt transactionReceipt = null;
        try {
            Optional<TransactionReceipt> opt = getWeb3j(groupId)
                    .getTransactionReceipt(transHash).send().getTransactionReceipt();
            if (opt.isPresent()) {
                transactionReceipt = opt.get();
            }
        } catch (IOException e) {
            log.error("getTransactionReceipt fail. transHash:{} ", transHash);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transactionReceipt;
    }

    /**
     * getTransactionByHash.
     *
     * @param transHash transHash
     */
    public Transaction getTransactionByHash(int groupId, String transHash) {

        Transaction transaction = null;
        try {
            Optional<Transaction> opt =
                    getWeb3j(groupId).getTransactionByHash(transHash).send().getTransaction();
            if (opt.isPresent()) {
                transaction = opt.get();
            }
        } catch (IOException e) {
            log.error("getTransactionByHash fail. transHash:{} ", transHash);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transaction;
    }

    /**
     * getClientVersion.
     */
    public Version getClientVersion() {
        Version version;
        try {
            version = getWeb3j().getNodeVersion().send().getNodeVersion();
        } catch (IOException e) {
            log.error("getClientVersion fail.", e);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
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
        try {
            if (blockNumberCheck(groupId, blockNumber)) {
                throw new FrontException(ConstantCode.BLOCK_NUMBER_ERROR);
            }
            code = getWeb3j(groupId)
                    .getCode(address, DefaultBlockParameter.valueOf(blockNumber)).send().getCode();
        } catch (IOException e) {
            log.error("getCode fail.", e);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return code;
    }

    /**
     * get transaction counts.
     */
    public TotalTransactionCount.TransactionCount getTransCnt(int groupId) {
        TotalTransactionCount.TransactionCount transactionCount;
        try {
            transactionCount = getWeb3j(groupId)
                    .getTotalTransactionCount().send()
                    .getTotalTransactionCount();
        } catch (IOException e) {
            log.error("getTransCnt fail.", e);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transactionCount;
    }

    /**
     * getTransByBlockHashAndIndex.
     *
     * @param blockHash blockHash
     * @param transactionIndex index
     */
    public Transaction getTransByBlockHashAndIndex(int groupId, String blockHash,
                                                   BigInteger transactionIndex) {

        Transaction transaction = null;
        try {
            Optional<Transaction> opt = getWeb3j(groupId)
                    .getTransactionByBlockHashAndIndex(blockHash, transactionIndex).send()
                    .getTransaction();
            if (opt.isPresent()) {
                transaction = opt.get();
            }
        } catch (IOException e) {
            log.error("getTransByBlockHashAndIndex fail.", e);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transaction;
    }

    /**
     * getTransByBlockNumberAndIndex.
     *
     * @param blockNumber blockNumber
     * @param transactionIndex index
     */
    public Transaction getTransByBlockNumberAndIndex(int groupId, BigInteger blockNumber,
                                                     BigInteger transactionIndex) {
        Transaction transaction = null;
        try {
            if (blockNumberCheck(groupId, blockNumber)) {
                throw new FrontException("ConstantCode.NODE_REQUEST_FAILED");
            }
            Optional<Transaction> opt =
                    getWeb3j(groupId)
                            .getTransactionByBlockNumberAndIndex(
                                    DefaultBlockParameter.valueOf(blockNumber), transactionIndex)
                            .send().getTransaction();
            if (opt.isPresent()) {
                transaction = opt.get();
            }
        } catch (IOException e) {
            log.error("getTransByBlockNumberAndIndex fail.", e);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transaction;
    }

    private boolean blockNumberCheck(int groupId, BigInteger blockNumber) {
        BigInteger currentNumber = null;
        try {
            currentNumber = getWeb3j(groupId).getBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            log.error("blockNumberCheck error:{}", e.getMessage());
        }
        log.info("**** currentNumber:{}", currentNumber);
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
            SyncStatus syncStatus = JsonUtils.toJavaObject(getSyncStatus(groupId), SyncStatus.class);
            List<PeerOfConsensusStatus> consensusList = getPeerOfConsensusStatus(groupId);
            if (Objects.isNull(peerStrList) || peerStrList.isEmpty() || consensusList == null) {
                log.info("end getNodeStatusList. peerStrList is empty");
                return Collections.emptyList();
            }
            for (String peer : peerStrList) {
                int nodeType = 0; // 0-consensus;1-observer
                if (observerList != null) {
                    nodeType = observerList.stream().filter(observer -> peer.equals(observer))
                            .map(c -> 1).findFirst().orElse(0);
                }
                BigInteger blockNumberOnChain = getBlockNumberOfNodeOnChain(syncStatus, peer);
                BigInteger latestView =
                        consensusList.stream().filter(cl -> peer.equals(cl.getNodeId()))
                                .map(c -> c.getView()).findFirst().orElse(BigInteger.ZERO);// pbftView
                // check node status
                statusList.add(
                        checkNodeStatus(groupId, peer, blockNumberOnChain, latestView, nodeType));
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
    private BigInteger getBlockNumberOfNodeOnChain(SyncStatus syncStatus, String nodeId) {
        if (Objects.isNull(syncStatus)) {
            log.warn("fail getBlockNumberOfNodeOnChain. SyncStatus is null");
            return BigInteger.ZERO;
        }
        if (StringUtils.isBlank(nodeId)) {
            log.warn("fail getBlockNumberOfNodeOnChain. nodeId is null");
            return BigInteger.ZERO;
        }
        if (nodeId.equals(syncStatus.getNodeId())) {
            return syncStatus.getBlockNumber();
        }
        List<PeerOfSyncStatus> peerList = syncStatus.getPeers();
        // blockNumber
        BigInteger latestNumber = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
                .map(s -> s.getBlockNumber()).findFirst().orElse(BigInteger.ZERO);
        return latestNumber;
    }


    /**
     * get peer of consensusStatus
     */
    private List<PeerOfConsensusStatus> getPeerOfConsensusStatus(int groupId) {
        String consensusStatusJson = getConsensusStatus(groupId);
        if (StringUtils.isBlank(consensusStatusJson)) {
            return Collections.emptyList();
        }
        List jsonArr = JsonUtils.toJavaObject(consensusStatusJson, List.class);
        if (jsonArr == null) {
            log.error("getPeerOfConsensusStatus error");
            throw new FrontException(ConstantCode.FAIL_PARSE_JSON);
        }
        List<PeerOfConsensusStatus> dataIsList = new ArrayList<>();
        for (int i = 0; i < jsonArr.size(); i++ ) {
            if (jsonArr.get(i) instanceof List) {
                List<PeerOfConsensusStatus> tempList = JsonUtils.toJavaObjectList(
                    JsonUtils.toJSONString(jsonArr.get(i)), PeerOfConsensusStatus.class);
                if (tempList != null) {
                    dataIsList.addAll(tempList);
                } else {
                    throw new FrontException(ConstantCode.FAIL_PARSE_JSON);
                }
            }
        }
        return dataIsList;
    }


    public List<String> getGroupPeers(int groupId) {
        GroupPeers groupPeers = null;
        try {
            groupPeers = getWeb3j(groupId)
                    .getGroupPeers().send();
        } catch (IOException e) {
            log.error("getGroupPeers error:[]", e);
            throw new FrontException(e.getMessage());
        }
        return groupPeers.getGroupPeers();
    }

    /**
     * get group list and refresh web3j map
     * @return
     */
    public List<String> getGroupList() {
        log.debug("getGroupList. ");
        try {
            List<String> groupIdList = getWeb3j().getGroupList().send().getGroupList();
            // check web3jMap, if not match groupIdList, refresh web3jMap in front
            refreshWeb3jMap(groupIdList);
            return groupIdList;
        } catch (IOException e) {
            log.error("getGroupList error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    public List<String> getNodeIdList() {
        try {
            return getWeb3j()
                    .getNodeIDList().send()
                    .getNodeIDList();
        } catch (IOException e) {
            log.error("getNodeIDList error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    /**
     * add web3j from chain and remove web3j not in chain
     * @param groupIdList
     * @throws FrontException
     */
    @DependsOn("encryptType")
    public void refreshWeb3jMap(List<String> groupIdList) throws FrontException {
        log.debug("refreshWeb3jMap groupIdList:{}", groupIdList);
        // if localGroupIdList not contain group in groupList from chain, add it
        groupIdList.stream()
            .filter(groupId ->
                web3jMap.get(Integer.parseInt(groupId)) == null)
            .forEach(group2Init ->
                initWeb3j(Integer.parseInt(group2Init)));

        Set<Integer> localGroupIdList = web3jMap.keySet();
        log.debug("refreshWeb3jMap localGroupList:{}", localGroupIdList);
        // if local web3j map contains group that not in groupList from chain
        // remove it from local web3j map
        localGroupIdList.stream()
            // not contains in groupList from chain
            .filter(groupId ->
                !groupIdList.contains(String.valueOf(groupId)))
            .forEach(group2Remove ->
                web3jMap.remove(group2Remove));
    }

    /**
     * init a new web3j of group id, add in groupChannelConnectionsConfig's connections
     * @param groupId
     * @return
     */
    private synchronized Web3j initWeb3j(int groupId) {
        log.info("initWeb3j of groupId:{}", groupId);
        List<ChannelConnections> channelConnectionsList =
                groupChannelConnectionsConfig.getAllChannelConnections();
        ChannelConnections channelConnections = new ChannelConnections();
        channelConnections.setConnectionsStr(channelConnectionsList.get(0).getConnectionsStr());
        channelConnections.setGroupId(groupId);
        channelConnectionsList.add(channelConnections);
        org.fisco.bcos.channel.client.Service service = new org.fisco.bcos.channel.client.Service();
        service.setOrgID(Web3Config.orgName);
        service.setGroupId(groupId);
        service.setThreadPool(threadPoolTaskExecutor);
        service.setAllChannelConnections(groupChannelConnectionsConfig);
        try {
            service.run();
        } catch (Exception e) {
            log.error("initWeb3j fail. groupId:{} error:[]", groupId, e);
            throw new FrontException("refresh web3j failed");
        }
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setTimeout(web3Config.getTimeout());
        channelEthereumService.setChannelService(service);
        Web3j web3j = Web3j.build(channelEthereumService, service.getGroupId());
        web3jMap.put(groupId, web3j);
        return web3j;
    }

    // get all peers of chain
    public List<Peers.Peer> getPeers(int groupId) {
        try {
            return getWeb3j(groupId)
                    .getPeers().send()
                    .getPeers();
        } catch (IOException e) {
            log.error("getPeers error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    public String getConsensusStatus(int groupId) {
        try {
            return getWeb3j(groupId)
                    .getConsensusStatus().sendForReturnString();
        } catch (IOException e) {
            log.error("getConsensusStatus error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    public String getSyncStatus(int groupId) {
        try {
            return getWeb3j(groupId)
                    .getSyncStatus().sendForReturnString();
        } catch (IOException e) {
            log.error("getSyncStatus error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    public String getSystemConfigByKey(int groupId, String key) {
        try {
            return getWeb3j(groupId)
                    .getSystemConfigByKey(key).send()
                    .getSystemConfigByKey();
        } catch (IOException e) {
            log.error("getSystemConfigByKey error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    /**
     * getNodeInfo.
     */
    public Object getNodeInfo() {
        return JsonUtils.toJavaObject(nodeConfig.toString(), Object.class);
    }

    public int getPendingTransactions(int groupId) throws IOException {
        try {
            return getWeb3j(groupId)
                    .getPendingTransaction().send()
                    .getPendingTransactions().size();
        } catch (IOException e) {
            log.error("getPendingTransactions error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    public BigInteger getPendingTransactionsSize(int groupId) throws IOException {
        try {
            return getWeb3j(groupId).getPendingTxSize().send().getPendingTxSize();
        } catch (IOException e) {
            log.error("getPendingTransactionsSize error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    public List<String> getSealerList(int groupId) throws IOException {
        try {
            return getWeb3j(groupId).getSealerList().send().getSealerList();
        } catch (IOException e) {
            log.error("getSealerList error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    public List<String> getObserverList(int groupId) throws IOException {
        try {
            return getWeb3j(groupId).getObserverList().send().getObserverList();
        } catch (IOException e) {
            log.error("getObserverList error:[]", e);
            throw new FrontException(e.getMessage());
        }
    }

    /**
     * search By Criteria
     */
    public Object searchByCriteria(int groupId, String input) {
        if (StringUtils.isBlank(input)) {
            log.warn("fail searchByCriteria. input is null");
            return null;
        }
        if (StringUtils.isNumeric(input)) {
            return getBlockByNumber(groupId, new BigInteger(input));
        } else if (input.length() == HASH_OF_TRANSACTION_LENGTH) {
            return getTransactionByHash(groupId, input);
        }

        return null;
    }

    /**
     * dynamic group management
     */

    public Object generateGroup(GenerateGroupInfo generateGroupInfo) {
        log.debug("start generateGroup. groupId:{}", generateGroupInfo.getGenerateGroupId());
        try {
            GroupOperateStatus status = CommonUtils.object2JavaBean(
                    getWeb3j().generateGroup(generateGroupInfo.getGenerateGroupId(),
                            generateGroupInfo.getTimestamp().longValue(), true,
                            generateGroupInfo.getNodeList()).send().getStatus(),
                    GroupOperateStatus.class);
            log.info("generateGroup. groupId:{} status:{}", generateGroupInfo.getGenerateGroupId(),
                    status);
            if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
                return new BaseResponse(ConstantCode.RET_SUCCEED);
            } else {
                log.error("generateGroup failed:{}", status.getMessage());
                throw classifyGroupOperateException(status);
            }
        } catch (IOException e) {
            log.error("generateGroup fail:[]", e);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
    }

    public Object operateGroup(int groupId, String type) {
        log.debug("start operateGroup. groupId:{} type:{}", groupId, type);
        try {
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
        } catch (IOException e) {
            log.error("operateGroup fail:[]", e);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
    }

    private Object startGroup(int groupId) throws IOException {
        GroupOperateStatus status = CommonUtils.object2JavaBean(
                getWeb3j().startGroup(groupId).send().getStatus(), GroupOperateStatus.class);
        log.info("startGroup. groupId:{} status:{}", groupId, status);
        if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
            initWeb3j(groupId);
            return new BaseResponse(ConstantCode.RET_SUCCEED);
        } else {
            log.error("startGroup fail:{}", status.getMessage());
            throw classifyGroupOperateException(status);
        }
    }

    private Object stopGroup(int groupId) throws IOException {
        GroupOperateStatus status = CommonUtils.object2JavaBean(
                getWeb3j().stopGroup(groupId).send().getStatus(), GroupOperateStatus.class);
        log.info("stopGroup. groupId:{} status:{}", groupId, status);
        if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
            web3jMap.remove(groupId);
            return new BaseResponse(ConstantCode.RET_SUCCEED);
        } else {
            log.error("stopGroup fail:{}", status.getMessage());
            throw classifyGroupOperateException(status);
        }
    }

    private Object removeGroup(int groupId) throws IOException {
        GroupOperateStatus status = CommonUtils.object2JavaBean(
                getWeb3j().removeGroup(groupId).send().getStatus(), GroupOperateStatus.class);
        log.info("removeGroup. groupId:{} status:{}", groupId, status);
        if (CommonUtils.parseHexStr2Int(status.getCode()) == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCEED);
        } else {
            log.error("removeGroup fail:{}", status.getMessage());
            throw classifyGroupOperateException(status);
        }
    }

    private Object recoverGroup(int groupId) throws IOException {
        GroupOperateStatus status = CommonUtils.object2JavaBean(
                getWeb3j().recoverGroup(groupId).send().getStatus(), GroupOperateStatus.class);
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
    private FrontException classifyGroupOperateException(GroupOperateStatus status) {
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

    private BaseResponse querySingleGroupStatus(int groupId) throws IOException {
        GroupOperateStatus status = CommonUtils.object2JavaBean(
                getWeb3j().queryGroupStatus(groupId).send().getStatus(), GroupOperateStatus.class);
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
    public BaseResponse getGroupStatus(List<Integer> groupIdList) throws IOException {
        Map<Integer, String> groupIdStatusMap = new HashMap<>(groupIdList.size());
        for (Integer groupId: groupIdList) {
            BaseResponse res = querySingleGroupStatus(groupId);
            groupIdStatusMap.put(groupId, (String)res.getData());
        }
        return new BaseResponse(ConstantCode.RET_SUCCESS, groupIdStatusMap);
    }

    /**
     * get first web3j in web3jMap
     * 
     * @return
     */
    public Web3j getWeb3j() {
        Set<Integer> iSet = web3jMap.keySet();
        if (iSet.isEmpty()) {
            log.error("web3jMap is empty, groupList empty! please check your node status");
            // get default web3j of integer max value
            return independentWeb3j;
        }
        // get random index to get web3j
        Integer index = iSet.iterator().next();
        return web3jMap.get(index);
    }

    /**
     * get target group's web3j
     * @param groupId
     * @return
     */
    public Web3j getWeb3j(Integer groupId) {
        if (web3jMap.isEmpty()) {
            // refresh group list
            getGroupList();
            log.error("web3jMap is empty, groupList empty! please check your node status");
            throw new FrontException(ConstantCode.SYSTEM_ERROR_GROUP_LIST_EMPTY);
        }
        Web3j web3j = web3jMap.get(groupId);
        if (Objects.isNull(web3j)) {
            log.error("web3j of {} is null, please call /{}/web3/refresh to refresh", groupId, groupId);
            // refresh group list
            getGroupList();
            throw new FrontException(ConstantCode.SYSTEM_ERROR_WEB3J_NULL.getCode(),
                    "web3j of " + groupId + " is null");
        }
        return web3j;
    }
}
