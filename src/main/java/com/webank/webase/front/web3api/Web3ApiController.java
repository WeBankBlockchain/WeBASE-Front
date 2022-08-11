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

import com.webank.webase.front.web3api.entity.NodeStatusInfo;
import com.webank.webase.front.web3api.entity.RspStatBlock;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.math.BigInteger;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupInfo.GroupInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupNodeInfo.GroupNodeInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.ConsensusStatus.ConsensusStatusInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.Peers;
import org.fisco.bcos.sdk.v3.client.protocol.response.SealerList.Sealer;
import org.fisco.bcos.sdk.v3.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.TotalTransactionCount.TransactionCountInfo;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@ApiImplicitParams(@ApiImplicitParam(name = "groupId", value = "groupId",required = true,dataType = "String", paramType = "path"))
public class Web3ApiController {

    @Autowired
    private Web3ApiService web3ApiService;

    @ApiOperation(value = "getBlockNumber", notes = "Get the latest block height of the node")
    @GetMapping("/blockNumber")
    public BigInteger getBlockNumber(@PathVariable String groupId) {
        return web3ApiService.getBlockNumber(groupId);
    }

    @ApiOperation(value = "getBlockByNumber", notes = "Get block information based on block height")
    @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
            dataType = "BigInteger", paramType = "path")
    @GetMapping("/blockByNumber/{blockNumber}")
    public BcosBlock.Block getBlockByNumber(@PathVariable String groupId,
        @PathVariable BigInteger blockNumber,
        @RequestParam(value = "fullTrans", required = false, defaultValue = "false") boolean fullTrans) {
        return web3ApiService.getBlockByNumber(groupId, blockNumber, fullTrans);
    }

    @ApiOperation(value = "getBlockByHash", notes = "Get block information based on block hash")
    @ApiImplicitParam(name = "blockHash", value = "blockHash", required = true, dataType = "String",
            paramType = "path")
    @GetMapping("/blockByHash/{blockHash}")
    public BcosBlock.Block getBlockByHash(
        @PathVariable String groupId,
        @PathVariable String blockHash,
        @RequestParam(value = "fullTrans", required = false, defaultValue = "false") boolean fullTrans) {
        return web3ApiService.getBlockByHash(groupId, blockHash, fullTrans);
    }

    @ApiOperation(value = "getBlockTransCntByNumber",
            notes = "Get the number of transactions in the block based on the block height")
    @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
            dataType = "BigInteger", paramType = "path")
    @GetMapping("/blockTransCnt/{blockNumber}")
    public int getBlockTransCntByNumber(@PathVariable String groupId,
            @PathVariable BigInteger blockNumber) {
        return web3ApiService.getBlockTransCntByNumber(groupId, blockNumber);
    }



    @ApiOperation(value = "getPbftView", notes = "Get PbftView")
    @GetMapping("/pbftView")
    public BigInteger getPbftView(@PathVariable String groupId) {
        return web3ApiService.getPbftView(groupId);
    }

    @ApiOperation(value = "getTransactionReceipt",
            notes = "Get a transaction receipt based on the transaction hash")
    @ApiImplicitParam(name = "transHash", value = "transHash", required = true, dataType = "String",
            paramType = "path")
    @GetMapping("/transactionReceipt/{transHash}")
    public TransactionReceipt getTransactionReceipt(@PathVariable String groupId,
            @PathVariable String transHash) {
        return web3ApiService.getTransactionReceipt(groupId, transHash);
    }

    @ApiOperation(value = "getTransactionByHash",
            notes = "Get transaction information based on transaction hash")
    @ApiImplicitParam(name = "transHash", value = "transHash", required = true, dataType = "String",
            paramType = "path")
    @GetMapping("/transaction/{transHash}")
    public JsonTransactionResponse getTransactionByHash(
        @PathVariable String groupId,
        @PathVariable String transHash,
        @RequestParam(value = "withProof", required = false, defaultValue = "false") boolean withProof) {
        return web3ApiService.getTransactionByHash(groupId, transHash, withProof);
    }

    @ApiOperation(value = "getCode",
            notes = "Get the binary code of the specified contract for the specified block")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "address", required = true,
                    dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
                    dataType = "BigInteger", paramType = "path")})
    @GetMapping("/code/{address}/{blockNumber}")
    public String getCode(@PathVariable String groupId, @PathVariable String address,
            @PathVariable BigInteger blockNumber) {
        return web3ApiService.getCode(groupId, address, blockNumber);
    }

    @ApiOperation(value = "getCode",
        notes = "Get the binary code of the specified contract for the specified block")
    @GetMapping("/code")
    public String getCodeWasm(@PathVariable String groupId, @RequestParam String address,
                          @RequestParam BigInteger blockNumber) {
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
                    dataType = "String", paramType = "path"))
    @GetMapping("/transaction-total")
    public TransactionCountInfo getTransTotalCnt(@PathVariable String groupId) {
        return web3ApiService.getTransCnt(groupId);
    }

    @ApiOperation(value = "getNodeStatusList", notes = "get list of node status info")
    @GetMapping("/nodeStatusList")
    public List<NodeStatusInfo> getNodeStatusList(@PathVariable String groupId) {
        return web3ApiService.getNodeStatusList(groupId);
    }

    /**
     * same as /web3/nodeIdList in fisco 2.0
     * @param groupId
     * @return
     */
    @ApiOperation(value = "getGroupPeers", notes = "get list of group peers, include removed nodes")
    @GetMapping("/groupPeers")
    public List<String> getGroupPeers(@PathVariable String groupId) {
        return web3ApiService.getGroupPeers(groupId);
    }


    @ApiOperation(value = "getGroupInfo", notes = "get group info")
    @GetMapping("/groupInfo")
    public GroupInfo getGroupInfo(@PathVariable String groupId) {
        return web3ApiService.getGroupInfo(groupId);
    }

    @ApiOperation(value = "getGroupInfo", notes = "get group info")
    @GetMapping("/groupNodeInfo")
    public List<GroupNodeInfo> getGroupNodeInfo(@PathVariable String groupId) {
        return web3ApiService.getGroupNodeInfo(groupId);
    }

    @ApiOperation(value = "getGroupList", notes = "get list of group id")
    @GetMapping("/groupList")
    public List<String> getGroupList() {
        return web3ApiService.getGroupList();
    }


    @ApiOperation(value = "getPeers", notes = "get list of peers")
    @GetMapping("/peers")
    public Peers.PeersInfo getPeers(@PathVariable String groupId) {
        return web3ApiService.getPeers(groupId);
    }

    @ApiOperation(value = "getPendingTransactionCount", notes = "get count of pending transactions count")
    @GetMapping("/pending-transactions-count")
    public int getPendingTransactionCount(@PathVariable String groupId) {
        return web3ApiService.getPendingTransactionsSize(groupId);
    }

    @ApiOperation(value = "getConsensusStatus", notes = "get consensus status of group")
    @GetMapping("/consensusStatus")
    public ConsensusStatusInfo getConsensusStatus(@PathVariable String groupId) {
        return web3ApiService.getConsensusStatus(groupId);
    }

    @ApiOperation(value = "getSyncStatus", notes = "get sync status of group")
    @GetMapping("/syncStatus")
    public SyncStatusInfo getSyncStatus(@PathVariable String groupId) {
        return web3ApiService.getSyncStatus(groupId);
    }

    @ApiOperation(value = "getSystemConfigByKey", notes = "get system config value by key")
    @ApiImplicitParam(name = "key", value = "key of system config", required = true, dataType = "String")
    @GetMapping("/systemConfigByKey/{key}")
    public String getSystemConfigByKey(@PathVariable String groupId, @PathVariable String key) {
        return web3ApiService.getSystemConfigByKey(groupId, key);
    }
//
//    @ApiOperation(value = "getNodeConfig", notes = "Get node config info")
//    @GetMapping("/nodeConfig")
//    public Object getNodeConfig() {
//        return web3ApiService.getNodeConfig();
//    }


    @ApiOperation(value = "getSealerList", notes = "get list of group's sealers")
    @GetMapping("/sealerList/weight")
    public List<Sealer> getSealerListWithWeight(@PathVariable String groupId) {
        return web3ApiService.getSealerList(groupId);
    }


    @ApiOperation(value = "getSealerList", notes = "get list of group's sealers")
    @GetMapping("/sealerList")
    public List<String> getSealerList(@PathVariable String groupId) {
        return web3ApiService.getSealerStrList(groupId);
    }

    @ApiOperation(value = "getObserverList", notes = "get list of group's observers")
    @GetMapping("/observerList")
    public List<String> getObserverList(@PathVariable String groupId) {
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
    public Object searchByCriteria(@PathVariable String groupId, @RequestParam String input) {
        return web3ApiService.searchByCriteria(groupId, input);
    }

    @ApiOperation(value = "getBlockTransCntByNumber",
        notes = "Get the number of transactions in the block based on the block height")
    @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
        dataType = "BigInteger", paramType = "path")
    @GetMapping("/blockStat/{blockNumber}")
    public RspStatBlock getBlockStatByNumber(@PathVariable String groupId,
        @PathVariable BigInteger blockNumber) {
        return web3ApiService.getBlockStatisticByNumber(groupId, blockNumber);
    }

    @GetMapping("encrypt")
    public Integer getEncryptType(@PathVariable("groupId") String groupId) {
        int encrypt = web3ApiService.getCryptoType(groupId);
        return encrypt;
    }

    @GetMapping("isWasm")
    public Boolean getIsWasmApi(@PathVariable("groupId") String groupId) {
        return web3ApiService.getIsWasm(groupId);
    }

}
