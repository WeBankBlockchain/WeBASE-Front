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

package com.webank.webase.front.web3api;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.config.NodeConfig;
import com.webank.webase.front.contract.KeyStoreInfo;
import com.webank.webase.front.transaction.TransService;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bcos.contract.source.SystemProxy;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.bcos.web3j.protocol.core.methods.response.EthBlock.Block;
import org.bcos.web3j.protocol.core.methods.response.Transaction;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Web3Api manage.
 *
 */
@Slf4j
@Service
public class Web3ApiService {

    @Autowired
    Web3j web3j;
    @Autowired
    NodeConfig nodeConfig;
    @Autowired
    TransService transService;

    private BigInteger blockNumber = new BigInteger("0");
    private BigInteger pbftView = new BigInteger("0");

    /**
     * getBlockNumber.
     * 
     * @return
     */
    public BaseResponse getBlockNumber() throws FrontException {
        log.info("getBlockNumber start.");

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        Map<String, BigInteger> map = new HashMap<String, BigInteger>();
        try {
            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            map.put("blockNumber", blockNumber);
            baseRspEntity.setData(map);
        } catch (IOException e) {
            log.error("getBlockNumber fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getBlockByNumber.
     * 
     * @param blockNumber blockNumber
     * @return
     */
    public BaseResponse getBlockByNumber(BigInteger blockNumber) throws FrontException {
        log.info("getBlockByNumber start. blockNumber:{}", blockNumber);

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        try {
            if (blockNumberCheck(blockNumber)) {
                baseRspEntity = new BaseResponse(ConstantCode.BLOCK_NUMBER_ERROR);
                return baseRspEntity;
            }
            Block block =
                    web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
                            .send().getBlock();
            baseRspEntity.setData(block);
        } catch (IOException e) {
            log.error("getBlockByNumber fail. blockNumber:{} ", blockNumber);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getBlockByHash.
     * 
     * @param blockHash blockHash
     * @return
     */
    public BaseResponse getBlockByHash(String blockHash) throws FrontException {
        log.info("getBlockByHash start. blockHash:{}", blockHash);

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        try {
            Block block = web3j.ethGetBlockByHash(blockHash, true).send().getBlock();
            baseRspEntity.setData(block);
        } catch (IOException e) {
            log.error("getBlockByHash fail. blockHash:{} ", blockHash);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getBlockTransCntByNumber.
     * 
     * @param blockNumber blockNumber
     * @return
     */
    public BaseResponse getBlockTransCntByNumber(BigInteger blockNumber) throws FrontException {
        log.info("getBlockTransCntByNumber start. blockNumber:{}", blockNumber);

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        Map<String, BigInteger> map = new HashMap<String, BigInteger>();
        try {
            if (blockNumberCheck(blockNumber)) {
                baseRspEntity = new BaseResponse(ConstantCode.BLOCK_NUMBER_ERROR);
                return baseRspEntity;
            }
            BigInteger transCnt = web3j
                    .ethGetBlockTransactionCountByNumber(DefaultBlockParameter.valueOf(blockNumber))
                    .send().getTransactionCount();
            map.put("transactionCount", transCnt);
            baseRspEntity.setData(map);
        } catch (IOException e) {
            log.error("getBlockTransCntByNumber fail. blockNumber:{} ", blockNumber);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getPbftView.
     * 
     * @return
     */
    public BaseResponse getPbftView() throws FrontException {
        log.info("getPbftView start.");

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        Map<String, BigInteger> map = new HashMap<String, BigInteger>();
        try {
            BigInteger pbftView = web3j.ethPbftView().send().getEthPbftView();
            map.put("pbftView", pbftView);
            baseRspEntity.setData(map);
        } catch (IOException e) {
            log.error("getPbftView fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getTransactionReceipt.
     * 
     * @param transHash transHash
     * @return
     */
    public BaseResponse getTransactionReceipt(String transHash) throws FrontException {
        log.info("getTransactionReceipt start. transHash:{}", transHash);

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        try {
            Optional<TransactionReceipt> opt =
                    web3j.ethGetTransactionReceipt(transHash).send().getTransactionReceipt();
            if (opt.isPresent()) {
                baseRspEntity.setData(opt.get());
            }
        } catch (IOException e) {
            log.error("getTransactionReceipt fail. transHash:{} ", transHash);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getTransactionByHash.
     * 
     * @param transHash transHash
     * @return
     */
    public BaseResponse getTransactionByHash(String transHash) throws FrontException {
        log.info("getTransactionByHash start. transHash:{}", transHash);

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        try {
            Optional<Transaction> opt =
                    web3j.ethGetTransactionByHash(transHash).send().getTransaction();
            if (opt.isPresent()) {
                baseRspEntity.setData(opt.get());
            }
        } catch (IOException e) {
            log.error("getTransactionByHash fail. transHash:{} ", transHash);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getClientVersion.
     * 
     * @return
     */
    public BaseResponse getClientVersion() throws FrontException {
        log.info("getClientVersion start.");

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        Map<String, String> map = new HashMap<String, String>();
        try {
            String version = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            map.put("version", version);
            baseRspEntity.setData(map);
        } catch (IOException e) {
            log.error("getClientVersion fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getCode.
     * 
     * @param address address
     * @param blockNumber blockNumber
     * @return
     */
    public BaseResponse getCode(String address, BigInteger blockNumber) throws FrontException {
        log.info("getCode start. address:{} blockNumber:{}", address, blockNumber);

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        Map<String, String> map = new HashMap<String, String>();
        try {
            if (blockNumberCheck(blockNumber)) {
                baseRspEntity = new BaseResponse(ConstantCode.BLOCK_NUMBER_ERROR);
                return baseRspEntity;
            }
            String code = web3j.ethGetCode(address, DefaultBlockParameter.valueOf(blockNumber))
                    .send().getCode();
            map.put("code", code);
            baseRspEntity.setData(map);
        } catch (IOException e) {
            log.error("getCode fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * get transaction counts.
     * @param address address
     * @param blockNumber blockNumber
     * @return
     */
    public BaseResponse getTransCnt(String address, BigInteger blockNumber) throws FrontException {
        log.info("getTransCnt start. address:{} blockNumber:{}", address, blockNumber);

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        Map<String, BigInteger> map = new HashMap<String, BigInteger>();
        try {
            if (blockNumberCheck(blockNumber)) {
                baseRspEntity = new BaseResponse(ConstantCode.BLOCK_NUMBER_ERROR);
                return baseRspEntity;
            }
            BigInteger transactionCount = web3j
                    .ethGetTransactionCount(address, DefaultBlockParameter.valueOf(blockNumber))
                    .send().getTransactionCount();
            map.put("transactionCount", transactionCount);
            baseRspEntity.setData(map);
        } catch (IOException e) {
            log.error("getTransCnt fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getTransByBlockHashAndIndex.
     * 
     * @param blockHash blockHash
     * @param transactionIndex index
     * @return
     */
    public BaseResponse getTransByBlockHashAndIndex(String blockHash, BigInteger transactionIndex)
            throws FrontException {
        log.info("getTransByBlockHashAndIndex start. blockHash:{} transactionIndex:{}", blockHash,
                transactionIndex);

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        try {
            Optional<Transaction> opt =
                    web3j.ethGetTransactionByBlockHashAndIndex(blockHash, transactionIndex).send()
                            .getTransaction();
            if (opt.isPresent()) {
                baseRspEntity.setData(opt.get());
            }
        } catch (IOException e) {
            log.error("getTransByBlockHashAndIndex fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    /**
     * getTransByBlockNumberAndIndex.
     * 
     * @param blockNumber blockNumber
     * @param transactionIndex index
     * @return
     */
    public BaseResponse getTransByBlockNumberAndIndex(BigInteger blockNumber,
            BigInteger transactionIndex) throws FrontException {
        log.info("getTransByBlockNumberAndIndex start. blockNumber:{} transactionIndex:{}",
                blockNumber, transactionIndex);

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        try {
            if (blockNumberCheck(blockNumber)) {
                baseRspEntity = new BaseResponse(ConstantCode.BLOCK_NUMBER_ERROR);
                return baseRspEntity;
            }
            Optional<Transaction> opt = web3j
                    .ethGetTransactionByBlockNumberAndIndex(
                            DefaultBlockParameter.valueOf(blockNumber), transactionIndex)
                    .send().getTransaction();
            if (opt.isPresent()) {
                baseRspEntity.setData(opt.get());
            }
        } catch (IOException e) {
            log.error("getTransByBlockNumberAndIndex fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }

    private boolean blockNumberCheck(BigInteger blockNumber) throws IOException {
        BigInteger currentNumber = web3j.ethBlockNumber().send().getBlockNumber();
        if (blockNumber.compareTo(currentNumber) == 1) {
            return true;
        }
        return false;
    }

    /**
     * getNodeInfo.
     * 
     * @return
     */
    public BaseResponse getNodeInfo() {
        log.info("getNodeInfo start.");

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        log.info("getNodeInfo data:{}", nodeConfig.toString());
        baseRspEntity.setData(JSON.parse(nodeConfig.toString()));
        return baseRspEntity;
    }

    /**
     * getSystemProxyInfo.
     * 
     * @param userId userId
     * @return
     */
    public BaseResponse getSystemProxyInfo(Integer userId) throws FrontException {
        log.info("getSystemProxyInfo start.");

        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        try {
            // getPrivateKey
            KeyStoreInfo keyStoreInfo = transService.getPrivateKey(userId);
            String privateKey = Optional.ofNullable(keyStoreInfo).map(info -> info.getPrivateKey())
                    .orElse(null);
            if (privateKey == null) {
                log.error("userId:{} privateKey is null", userId);
                baseRspEntity = new BaseResponse(ConstantCode.PRIVATEKEY_IS_NULL);
                return baseRspEntity;
            }
            // SystemProxy load
            Credentials credentials = Credentials.create(privateKey);
            SystemProxy systemProxy = SystemProxy.load(nodeConfig.getSystemproxyaddress(), web3j,
                    credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT);
            Uint256 routelength = systemProxy.getRouteSize().get();
            // baseRspEntity data set
            List<RspSystemProxy> rspList = new ArrayList<>();
            for (int i = 0; i < routelength.getValue().intValue(); i++) {
                Utf8String key = systemProxy.getRouteNameByIndex(new Uint256(i)).get();
                List<Type> route = systemProxy.getRoute(key).get();

                RspSystemProxy rspSystemProxy = new RspSystemProxy();
                rspSystemProxy.setName(key.getValue());
                rspSystemProxy.setAddress(route.get(0).toString());
                rspSystemProxy.setCache(route.get(1).getValue().toString());
                rspSystemProxy.setBlockNumber(((BigInteger) (route.get(2).getValue())).intValue());
                rspList.add(rspSystemProxy);
            }
            baseRspEntity.setData(rspList);
        } catch (Exception e) {
            log.error("getSystemProxyInfo fail.");
            throw new FrontException(ConstantCode.SYSTEM_ERROR);
        }
        return baseRspEntity;
    }

    /**
     * nodeHeartBeat.
     * 
     * @return
     */
    public BaseResponse nodeHeartBeat() throws FrontException {
        log.info("nodeHeartBeat start.");
        BaseResponse baseRspEntity = new BaseResponse(ConstantCode.RET_SUCCEED);
        try {
            BigInteger currentBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            BigInteger currentPbftView = web3j.ethPbftView().send().getEthPbftView();
            log.info("nodeHeartBeat blockNumber:{} current:{} pbftView:{} current:{}",
                    this.blockNumber, currentBlockNumber, this.pbftView, currentPbftView);
            if (currentBlockNumber.equals(this.blockNumber)
                    && currentPbftView.equals(this.pbftView)) {
                baseRspEntity = new BaseResponse(ConstantCode.BLOCKNUMBER_AND_PBFTVIEW_UNCHANGED);
                return baseRspEntity;
            } else {
                this.blockNumber = currentBlockNumber;
                this.pbftView = currentPbftView;
            }
            Map<String, BigInteger> map = new HashMap<String, BigInteger>();
            map.put("blockNumber", currentBlockNumber);
            map.put("pbftView", currentPbftView);
            baseRspEntity.setData(map);
        } catch (IOException e) {
            log.error("nodeHeartBeat Exception.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return baseRspEntity;
    }
}
