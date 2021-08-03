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
package com.webank.webase.front.web3api;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.util.Address;
import com.webank.webase.front.web3api.entity.GenerateGroupInfo;
import com.webank.webase.front.web3api.entity.NodeStatusInfo;
import com.webank.webase.front.web3api.entity.ReqGroupStatus;
import com.webank.webase.front.web3api.entity.RspStatBlock;
import com.webank.webase.front.web3api.entity.RspTransCountInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.math.BigInteger;
import java.util.List;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader.BlockHeader;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus.ConsensusInfo;
import org.fisco.bcos.sdk.client.protocol.response.NodeInfo.NodeInformation;
import org.fisco.bcos.sdk.client.protocol.response.Peers;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web3ApiController.
 *
 */
@Api(value = "/web3", tags = "web3j interface")
@RestController
@RequestMapping(value = "/{groupId}/web3")
@ApiImplicitParams(@ApiImplicitParam(name = "groupId", value = "groupId",required = true,dataType = "Integer", paramType = "path"))
public class Web3ApiController {

    @Autowired
    Web3ApiService web3ApiService;

    @ApiOperation(value = "getBlockNumber", notes = "Get the latest block height of the node")
    @GetMapping("/blockNumber")
    public BigInteger getBlockNumber(@PathVariable int groupId) {
        return web3ApiService.getBlockNumber(groupId);
    }

    @ApiOperation(value = "getBlockByNumber", notes = "Get block information based on block height")
    @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
            dataType = "BigInteger", paramType = "path")
    @GetMapping("/blockByNumber/{blockNumber}")
    public BcosBlock.Block getBlockByNumber(@PathVariable int groupId,
        @PathVariable BigInteger blockNumber) {
        return web3ApiService.getBlockByNumber(groupId, blockNumber);
    }

    @ApiOperation(value = "getBlockByHash", notes = "Get block information based on block hash")
    @ApiImplicitParam(name = "blockHash", value = "blockHash", required = true, dataType = "String",
            paramType = "path")
    @GetMapping("/blockByHash/{blockHash}")
    public BcosBlock.Block getBlockByHash(@PathVariable int groupId, @PathVariable String blockHash) {
        return web3ApiService.getBlockByHash(groupId, blockHash);
    }

    @ApiOperation(value = "getBlockTransCntByNumber",
            notes = "Get the number of transactions in the block based on the block height")
    @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
            dataType = "BigInteger", paramType = "path")
    @GetMapping("/blockTransCnt/{blockNumber}")
    public int getBlockTransCntByNumber(@PathVariable int groupId,
            @PathVariable BigInteger blockNumber) {
        return web3ApiService.getBlockTransCntByNumber(groupId, blockNumber);
    }



    @ApiOperation(value = "getPbftView", notes = "Get PbftView")
    @GetMapping("/pbftView")
    public BigInteger getPbftView(@PathVariable int groupId) {
        return web3ApiService.getPbftView(groupId);
    }

    @ApiOperation(value = "getTransactionReceipt",
            notes = "Get a transaction receipt based on the transaction hash")
    @ApiImplicitParam(name = "transHash", value = "transHash", required = true, dataType = "String",
            paramType = "path")
    @GetMapping("/transactionReceipt/{transHash}")
    public TransactionReceipt getTransactionReceipt(@PathVariable int groupId,
            @PathVariable String transHash) {
        return web3ApiService.getTransactionReceipt(groupId, transHash);
    }

    @ApiOperation(value = "getTransactionByHash",
            notes = "Get transaction information based on transaction hash")
    @ApiImplicitParam(name = "transHash", value = "transHash", required = true, dataType = "String",
            paramType = "path")
    @GetMapping("/transaction/{transHash}")
    public JsonTransactionResponse getTransactionByHash(@PathVariable int groupId,
            @PathVariable String transHash) {
        return web3ApiService.getTransactionByHash(groupId, transHash);
    }

    @ApiOperation(value = "getClientVersion", notes = "Get the web3j version")
    @GetMapping("/clientVersion")
    public ClientVersion getClientVersion() {
        return web3ApiService.getClientVersion();
    }

    @ApiOperation(value = "getCode",
            notes = "Get the binary code of the specified contract for the specified block")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "address", required = true,
                    dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
                    dataType = "BigInteger", paramType = "path")})
    @GetMapping("/code/{address}/{blockNumber}")
    public String getCode(@PathVariable int groupId, @PathVariable String address,
            @PathVariable BigInteger blockNumber) {
        if (address.length() != Address.ValidLen) {
            throw new FrontException(ConstantCode.PARAM_ADDRESS_IS_INVALID);
        }
        return web3ApiService.getCode(groupId, address, blockNumber);
    }

    /**
     * getTotalTransactionCount
     * @param groupId
     * @return
     */
    @ApiOperation(value = "getTotalTransactionCount",
            notes = "Get the  total number of execution transactions count ")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "groupId", value = "groupId", required = true,
                    dataType = "int", paramType = "path"))
    @GetMapping("/transaction-total")
    public RspTransCountInfo getTransTotalCnt(@PathVariable int groupId) {
        return web3ApiService.getTransCnt(groupId);
    }

    @ApiOperation(value = "getTransByBlockHashAndIndex",
            notes = "Gets the transaction information for the specified "
                    + "location of the specified block")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "blockHash", value = "blockHash", required = true,
                    dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "transactionIndex", value = "transactionIndex",
                    required = true, dataType = "BigInteger", paramType = "path")})
    @GetMapping("/transByBlockHashAndIndex/{blockHash}/{transactionIndex}")
    public JsonTransactionResponse getTransByBlockHashAndIndex(@PathVariable int groupId,
            @PathVariable String blockHash, @PathVariable BigInteger transactionIndex) {
        return web3ApiService.getTransByBlockHashAndIndex(groupId, blockHash, transactionIndex);
    }

    @ApiOperation(value = "getTransByBlockNumberAndIndex",
            notes = "Gets the transaction information for the specified "
                    + "location of the specified block")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
                    dataType = "BigInteger", paramType = "path"),
            @ApiImplicitParam(name = "transactionIndex", value = "transactionIndex",
                    required = true, dataType = "BigInteger", paramType = "path")})
    @GetMapping("/transByBlockNumberAndIndex/{blockNumber}/{transactionIndex}")
    public JsonTransactionResponse getTransByBlockNumberAndIndex(@PathVariable int groupId,
            @PathVariable BigInteger blockNumber, @PathVariable BigInteger transactionIndex) {
        return web3ApiService.getTransByBlockNumberAndIndex(groupId, blockNumber, transactionIndex);
    }

    @ApiOperation(value = "getNodeStatusList", notes = "get list of node status info")
    @GetMapping("/getNodeStatusList")
    public List<NodeStatusInfo> getNodeStatusList(@PathVariable int groupId) {
        return web3ApiService.getNodeStatusList(groupId);
    }

    @ApiOperation(value = "getGroupPeers", notes = "get list of group peers")
    @GetMapping("/groupPeers")
    public List<String> getGroupPeers(@PathVariable int groupId) {
        return web3ApiService.getGroupPeers(groupId);
    }

    @ApiOperation(value = "getGroupList", notes = "get list of group id")
    @GetMapping("/groupList")
    public List<String> getGroupList() {
        return web3ApiService.getGroupList();
    }

    @ApiOperation(value = "getNodeIDList", notes = "get list of node id")
    @GetMapping("/nodeIdList")
    public List<String> getNodeIDList() {
        return web3ApiService.getNodeIdList();
    }

    @ApiOperation(value = "getPeers", notes = "get list of peers")
    @GetMapping("/peers")
    public List<Peers.PeerInfo> getPeers(@PathVariable int groupId) {
        return web3ApiService.getPeers(groupId);
    }

    @ApiOperation(value = "getPendingTransactionCount", notes = "get count of pending transactions count")
    @GetMapping("/pending-transactions-count")
    public int getPendingTransactionCount(@PathVariable int groupId) {
        return web3ApiService.getPendingTransactions(groupId);
    }

    @ApiOperation(value = "getConsensusStatus", notes = "get consensus status of group")
    @GetMapping("/consensusStatus")
    public ConsensusInfo getConsensusStatus(@PathVariable int groupId) {
        return web3ApiService.getConsensusStatus(groupId);
    }

    @ApiOperation(value = "getSyncStatus", notes = "get sync status of group")
    @GetMapping("/syncStatus")
    public SyncStatusInfo getSyncStatus(@PathVariable int groupId) {
        return web3ApiService.getSyncStatus(groupId);
    }

    @ApiOperation(value = "getSystemConfigByKey", notes = "get system config value by key")
    @ApiImplicitParam(name = "key", value = "key of system config", required = true, dataType = "String")
    @GetMapping("/systemConfigByKey/{key}")
    public String getSystemConfigByKey(@PathVariable int groupId, @PathVariable String key) {
        return web3ApiService.getSystemConfigByKey(groupId, key);
    }

    @ApiOperation(value = "getNodeConfig", notes = "Get node config info")
    @GetMapping("/nodeConfig")
    public Object getNodeConfig() {
        return web3ApiService.getNodeConfig();
    }

    @ApiOperation(value = "getSealerList", notes = "get list of group's sealers")
    @GetMapping("/sealerList")
    public List<String> getSealerList(@PathVariable int groupId) {
        return web3ApiService.getSealerList(groupId);
    }

    @ApiOperation(value = "getObserverList", notes = "get list of group's observers")
    @GetMapping("/observerList")
    public List<String> getObserverList(@PathVariable int groupId) {
        return web3ApiService.getObserverList(groupId);
    }

    @ApiOperation(value = "refresh", notes = "refresh all groups")
    @GetMapping("/refresh")
    public void refresh() {
        // Service of getGroupList will refresh web3jMap
        web3ApiService.getGroupList();
    }

    @ApiOperation(value = "searchByCriteria", notes = "get list of group id")
    @ApiImplicitParam(name = "input", value = "input of criteria", required = true, dataType = "String")
    @GetMapping("/search")
    public Object searchByCriteria(@PathVariable int groupId, @RequestParam String input) {
        return web3ApiService.searchByCriteria(groupId, input);
    }

    @ApiOperation(value = "generateGroup", notes = "generate a new group")
    @PostMapping("/generateGroup")
    public Object generateGroup(@RequestBody GenerateGroupInfo req) {
        return web3ApiService.generateGroup(req);
    }

    @ApiOperation(value = "operateGroup", notes = "start/stop/recover/remove/getStatus the group")
    @ApiImplicitParam(name = "type", value = "group operation type", required = true, dataType = "String")
    @GetMapping("/operateGroup/{type}")
    public Object operateGroup(@PathVariable int groupId, @PathVariable String type) {
        return web3ApiService.operateGroup(groupId, type);
    }

    /**
     * get group status of front's node
     * @param groupIdList
     * @return map of <groupId, status>
     *     status: "INEXISTENT"、"STOPPING"、"RUNNING"、"STOPPED"、"DELETED"
     */
    @ApiOperation(value = "getGroupStatus", notes = "getStatus of the group id in the list")
    @ApiImplicitParam(name = "groupIdList", value = "group id list of string", required = true, dataType = "ReqGroupStatus")
    @PostMapping("/queryGroupStatus")
    public BaseResponse getGroupStatus(@RequestBody ReqGroupStatus groupIdList) {
        if (groupIdList.getGroupIdList().isEmpty()) {
            throw new FrontException(ConstantCode.PARAM_FAIL_GROUP_ID_IS_EMPTY);
        }
        return web3ApiService.getGroupStatus(groupIdList.getGroupIdList());
    }

    /* after fisco-bcos v2.5.x */

    /* above 2.7.0 */
    @ApiOperation(value = "getBlockHeaderByHash", notes = "Get block header with sealers based on block hash")
    @ApiImplicitParam(name = "blockHash", value = "blockHash", required = true,
        dataType = "String", paramType = "path")
    @GetMapping("/blockHeaderByHash/{blockHash}")
    public BlockHeader getBlockHeaderByHash(@PathVariable int groupId,
        @PathVariable String blockHash) {
        return web3ApiService.getBlockHeaderByHash(groupId, blockHash, true);
    }

    @ApiOperation(value = "getBlockHeaderByNumber", notes = "Get block header with sealers based on block height")
    @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
        dataType = "BigInteger", paramType = "path")
    @GetMapping("/blockHeaderByNumber/{blockNumber}")
    public BlockHeader getBlockHeaderByNumber(@PathVariable int groupId,
        @PathVariable BigInteger blockNumber) {
        return web3ApiService.getBlockHeaderByNumber(groupId, blockNumber, true);
    }
    /* above 2.7.0 */

    @ApiOperation(value = "getBlockTransCntByNumber",
        notes = "Get the number of transactions in the block based on the block height")
    @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
        dataType = "BigInteger", paramType = "path")
    @GetMapping("/blockStat/{blockNumber}")
    public RspStatBlock getBlockStatByNumber(@PathVariable int groupId,
        @PathVariable BigInteger blockNumber) {
        return web3ApiService.getBlockStatisticByNumber(groupId, blockNumber);
    }

    /* above 2.7.0 */
    @ApiOperation(value = "getBatchReceiptByBlockNumber",
        notes = "Get the number of transactions in the block based on the block height")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
            dataType = "BigInteger", paramType = "path"),
        @ApiImplicitParam(name = "start", value = "start", required = true,
            dataType = "int"),
        @ApiImplicitParam(name = "count", value = "count", required = true,
            dataType = "int")
    })
    @GetMapping("/transReceipt/batchByNumber/{blockNumber}")
    public List<TransactionReceipt> getBatchReceiptByBlockNumber(@PathVariable int groupId,
        @PathVariable BigInteger blockNumber,
        @RequestParam(value = "start", defaultValue = "0") int start,
        @RequestParam(value = "count", defaultValue = "-1") int count) {
        return web3ApiService.getBatchReceiptByBlockNumber(groupId, blockNumber, start, count);
    }

    @ApiOperation(value = "getBatchReceiptByBlockHash",
        notes = "Get the number of transactions in the block based on the block height")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "blockHash", value = "blockHash", required = true,
            dataType = "String", paramType = "path"),
        @ApiImplicitParam(name = "start", value = "start", required = true,
            dataType = "int"),
        @ApiImplicitParam(name = "count", value = "count", required = true,
            dataType = "int")
    })
    @GetMapping("/transReceipt/batchByHash/{blockHash}")
    public List<TransactionReceipt> getBatchReceiptByBlockHash(@PathVariable int groupId,
        @PathVariable String blockHash,
        @RequestParam(value = "start", defaultValue = "0") int start,
        @RequestParam(value = "count", defaultValue = "-1") int count) {
        return web3ApiService.getBatchReceiptByBlockHash(groupId, blockHash, start, count);
    }

    @ApiOperation(value = "getNodeInfo", notes = "Get node information")
    @GetMapping("/nodeInfo")
    public NodeInformation getNodeInfo() {
        return web3ApiService.getNodeInfo();
    }

    /* above 2.7.0 */
}
