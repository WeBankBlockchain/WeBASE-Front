package com.webank.webase.front.web3api;

import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.exception.FrontException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.math.BigInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Copyright 2012-2019 the original author or authors.
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

/**
 * Web3ApiController.
 *
 */
@Api(value = "/web3", tags = "web3j interface")
@RestController
@RequestMapping(value = "/web3")
public class Web3ApiController {

    @Autowired
    Web3ApiService web3ApiService;

    @ApiOperation(value = "getBlockNumber", notes = "Get the latest block height of the node")
    @GetMapping("/blockNumber")
    public BaseResponse getBlockNumber() throws FrontException {
        return web3ApiService.getBlockNumber();
    }

    @ApiOperation(value = "getBlockByNumber", notes = "Get block information based on block height")
    @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
            dataType = "BigInteger", paramType = "path")
    @GetMapping("/blockByNumber/{blockNumber}")
    public BaseResponse getBlockByNumber(@PathVariable BigInteger blockNumber)
            throws FrontException {
        return web3ApiService.getBlockByNumber(blockNumber);
    }

    @ApiOperation(value = "getBlockByHash", notes = "Get block information based on block hash")
    @ApiImplicitParam(name = "blockHash", value = "blockHash", required = true, dataType = "String",
            paramType = "path")
    @GetMapping("/blockByHash/{blockHash}")
    public BaseResponse getBlockByHash(@PathVariable String blockHash) throws FrontException {
        return web3ApiService.getBlockByHash(blockHash);
    }

    @ApiOperation(value = "getBlockTransCntByNumber",
            notes = "Get the number of transactions in the block based on the block height")
    @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
            dataType = "BigInteger", paramType = "path")
    @GetMapping("/blockTransCnt/{blockNumber}")
    public BaseResponse getBlockTransCntByNumber(@PathVariable BigInteger blockNumber)
            throws FrontException {
        return web3ApiService.getBlockTransCntByNumber(blockNumber);
    }

    @ApiOperation(value = "getPbftView", notes = "Get PbftView")
    @GetMapping("/pbftView")
    public BaseResponse getPbftView() throws FrontException {
        return web3ApiService.getPbftView();
    }

    @ApiOperation(value = "getTransactionReceipt",
            notes = "Get a transaction receipt based on the transaction hash")
    @ApiImplicitParam(name = "transHash", value = "transHash", required = true, dataType = "String",
            paramType = "path")
    @GetMapping("/transactionReceipt/{transHash}")
    public BaseResponse getTransactionReceipt(@PathVariable String transHash)
            throws FrontException {
        return web3ApiService.getTransactionReceipt(transHash);
    }

    @ApiOperation(value = "getTransactionByHash",
            notes = "Get transaction information based on transaction hash")
    @ApiImplicitParam(name = "transHash", value = "transHash", required = true, dataType = "String",
            paramType = "path")
    @GetMapping("/transaction/{transHash}")
    public BaseResponse getTransactionByHash(@PathVariable String transHash) throws FrontException {
        return web3ApiService.getTransactionByHash(transHash);
    }

    @ApiOperation(value = "getClientVersion", notes = "Get the web3j version")
    @GetMapping("/clientVersion")
    public BaseResponse getClientVersion() throws FrontException {
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
    public BaseResponse getCode(@PathVariable String address, @PathVariable BigInteger blockNumber)
            throws FrontException {
        return web3ApiService.getCode(address, blockNumber);
    }

// todo
//    @ApiOperation(value = "getTransCnt",
//            notes = "Get the number of execution transactions for the specified "
//                    + "account at the specified block height")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "address", value = "address", required = true,
//                    dataType = "String", paramType = "path"),
//            @ApiImplicitParam(name = "blockNumber", value = "blockNumber", required = true,
//                    dataType = "BigInteger", paramType = "path")})
//    @GetMapping("/transCnt/{address}/{blockNumber}")
//    public BaseResponse getTransCnt(@PathVariable String address,
//            @PathVariable BigInteger blockNumber) throws FrontException {
//        return web3ApiService.getTransCnt(address, blockNumber);
//    }

    @ApiOperation(value = "getTransByBlockHashAndIndex",
            notes = "Gets the transaction information for the specified "
                    + "location of the specified block")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "blockHash", value = "blockHash", required = true,
                    dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "transactionIndex", value = "transactionIndex",
                    required = true, dataType = "BigInteger", paramType = "path")})
    @GetMapping("/transByBlockHashAndIndex/{blockHash}/{transactionIndex}")
    public BaseResponse getTransByBlockHashAndIndex(@PathVariable String blockHash,
            @PathVariable BigInteger transactionIndex) throws FrontException {
        return web3ApiService.getTransByBlockHashAndIndex(blockHash, transactionIndex);
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
    public BaseResponse getTransByBlockNumberAndIndex(@PathVariable BigInteger blockNumber,
            @PathVariable BigInteger transactionIndex) throws FrontException {
        return web3ApiService.getTransByBlockNumberAndIndex(blockNumber, transactionIndex);
    }


    @ApiOperation(value = "nodeHeartBeat", notes = "Verify that the node is alive")
    @GetMapping("/nodeHeartBeat")
    public BaseResponse nodeHeartBeat() throws FrontException {
        return web3ApiService.nodeHeartBeat();
    }
}
