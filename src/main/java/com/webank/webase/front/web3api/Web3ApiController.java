/*
 * Copyright 2014-2019 the original author or authors.
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

import com.webank.webase.front.web3api.entity.GenerateGroupInfo;
import com.webank.webase.front.web3api.entity.NodeStatusInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion.Version;
import org.fisco.bcos.web3j.protocol.core.methods.response.Peers;
import org.fisco.bcos.web3j.protocol.core.methods.response.TotalTransactionCount;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
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
    public BcosBlock.Block getBlockByHash(@PathVariable int groupId,
            @PathVariable String blockHash) {
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
    public Transaction getTransactionByHash(@PathVariable int groupId,
            @PathVariable String transHash) {
        return web3ApiService.getTransactionByHash(groupId, transHash);
    }

    @ApiOperation(value = "getClientVersion", notes = "Get the web3j version")
    @GetMapping("/clientVersion")
    public Version getClientVersion() {
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
        return web3ApiService.getCode(groupId, address, blockNumber);
    }

    // todo
    @ApiOperation(value = "getTotalTransactionCount",
            notes = "Get the  total number of execution transactions count ")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "address", required = true,
                    dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
                    dataType = "BigInteger", paramType = "path")})
    @GetMapping("/transaction-total")
    public TotalTransactionCount.TransactionCount getTransTotalCnt(@PathVariable int groupId) {
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
    public Transaction getTransByBlockHashAndIndex(@PathVariable int groupId,
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
    public Transaction getTransByBlockNumberAndIndex(@PathVariable int groupId,
            @PathVariable BigInteger blockNumber, @PathVariable BigInteger transactionIndex) {
        return web3ApiService.getTransByBlockNumberAndIndex(groupId, blockNumber, transactionIndex);
    }

    @ApiOperation(value = "getNodeStatusList", notes = "get list of node status info")
    @GetMapping("/getNodeStatusList")
    public List<NodeStatusInfo> getNodeStatusList(@PathVariable int groupId) {
        return web3ApiService.getNodeStatusList(groupId);
    }

    @GetMapping("/groupPeers")
    public List<String> getGroupPeers(@PathVariable int groupId) {
        return web3ApiService.getGroupPeers(groupId);
    }

    @GetMapping("/groupList")
    public List<String> getGroupList() throws IOException {
        return web3ApiService.getGroupList();
    }

    @GetMapping("/nodeIdList")
    public List<String> getNodeIDList() throws IOException {
        return web3ApiService.getNodeIDList();
    }

    @GetMapping("/peers")
    public List<Peers.Peer> getPeers(@PathVariable int groupId) {
        return web3ApiService.getPeers(groupId);
    }

    @GetMapping("/pending-transactions-count")
    public int getPendingTransactions(@PathVariable int groupId) throws IOException {
        return web3ApiService.getPendingTransactions(groupId);
    }

    @GetMapping("/consensusStatus")
    public String getConsensusStatus(@PathVariable int groupId) {
        return web3ApiService.getConsensusStatus(groupId);
    }

    @GetMapping("/syncStatus")
    public String getSyncStatus(@PathVariable int groupId) {
        return web3ApiService.getSyncStatus(groupId);
    }

    @GetMapping("/systemConfigByKey/{key}")
    public String getSystemConfigByKey(@PathVariable int groupId, @PathVariable String key) {
        return web3ApiService.getSystemConfigByKey(groupId, key);
    }

    @ApiOperation(value = "getNodeInfo", notes = "Get node information")
    @GetMapping("/nodeInfo")
    public Object getNodeInfo() {
        return web3ApiService.getNodeInfo();
    }

    @GetMapping("/sealerList")
    public List<String> getSealerList(@PathVariable int groupId) throws IOException {
        return web3ApiService.getSealerList(groupId);
    }

    @GetMapping("/observerList")
    public List<String> getObserverList(@PathVariable int groupId) throws IOException {
        return web3ApiService.getObserverList(groupId);
    }

    @GetMapping("/refresh")
    public void refresh(@PathVariable int groupId) {
        web3ApiService.refreshServiceMap();
    }

    @GetMapping("/search")
    public Object searchByCriteria(@PathVariable int groupId, @RequestParam String input) {
        return web3ApiService.searchByCriteria(groupId, input);
    }

    @PostMapping("/generateGroup")
    public Object generateGroup(@RequestBody GenerateGroupInfo req)
            throws IOException {
        return web3ApiService.generateGroup(req);
    }

    @GetMapping("/startGroup/{startGroupId}")
    public Object startGroup(@PathVariable int startGroupId)
            throws IOException {
        return web3ApiService.startGroup(startGroupId);
    }
}
