package com.webank.webase.front.web3api;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.config.NodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.methods.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private static BigInteger blockNumber = new BigInteger("0");
    private static BigInteger pbftView = new BigInteger("0");

    /**
     * getBlockNumber.
     * 
     * @return
     */
    public BigInteger getBlockNumber() throws FrontException {

        BigInteger blockNumber;
        try {
             blockNumber = web3j.getBlockNumber().send().getBlockNumber();

        } catch (IOException e) {
            log.error("getBlockNumber fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return blockNumber;
    }

    /**
     * getBlockByNumber.
     * 
     * @param blockNumber blockNumber
     * @return
     */
    public BcosBlock.Block getBlockByNumber(BigInteger blockNumber) throws FrontException {
        BcosBlock.Block block;
        try {
            if (blockNumberCheck(blockNumber)) {
                throw new FrontException("requst blockNumber is greater than latest");
            }
             block = web3j.getBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
                            .send().getBlock();
        } catch (IOException e) {
            log.error("getBlockByNumber fail. blockNumber:{} ", blockNumber);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return block;
    }

    /**
     * getBlockByHash.
     * 
     * @param blockHash blockHash
     * @return
     */
    public BcosBlock.Block getBlockByHash(String blockHash) throws FrontException {
        BcosBlock.Block block;
        try {
            block = web3j.getBlockByHash(blockHash, true).send().getBlock();

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
     * @return
     */
    public int getBlockTransCntByNumber(BigInteger blockNumber) throws FrontException {
        int transCnt;
        try {
            if (blockNumberCheck(blockNumber)) {
                throw new FrontException("requst blockNumber is greater than latest");
            }
            BcosBlock.Block block = web3j.getBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
                            .send().getBlock();
             transCnt = block.getTransactions().size();
//            BigInteger transCnt = web3j
//                    .ethGetBlockTransactionCountByNumber(DefaultBlockParameter.valueOf(blockNumber))
//                    .send().getTransactionCount();

        } catch (IOException e) {
            log.error("getBlockTransCntByNumber fail. blockNumber:{} ", blockNumber);
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transCnt;
    }

    /**
     * getPbftView.
     * 
     * @return
     */
    public BigInteger getPbftView() throws FrontException {

       BigInteger result;
        try {
            result = web3j.getPbftView().send().getPbftView();
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
     * @return
     */
    public TransactionReceipt getTransactionReceipt(String transHash) throws FrontException {

        TransactionReceipt transactionReceipt =null;
        try {
            Optional<TransactionReceipt> opt =
                    web3j.getTransactionReceipt(transHash).send().getTransactionReceipt();
            if (opt.isPresent()) {
                transactionReceipt= opt.get();
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
     * @return
     */
    public Transaction getTransactionByHash(String transHash) throws FrontException {

        Transaction transaction= null;
        try {
            Optional<Transaction> opt = web3j.getTransactionByHash(transHash).send().getTransaction();
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
     * 
     * @return
     */
    public String getClientVersion() throws FrontException {
        String version;
        try {
             version = web3j.getNodeVersion().send().getNodeVersion().getVersion();
        } catch (IOException e) {
            log.error("getClientVersion fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return version;
    }

    /**
     * getCode.
     * 
     * @param address address
     * @param blockNumber blockNumber
     * @return
     */
        public String  getCode(String address, BigInteger blockNumber) throws FrontException {
            String code;
            try {
            if (blockNumberCheck(blockNumber)) {
                throw new FrontException("requst blockNumber is greater than latest");
            }
             code = web3j.getCode(address, DefaultBlockParameter.valueOf(blockNumber)).send().getCode();
        } catch (IOException e) {
            log.error("getCode fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return code;
    }

    /**
     * get transaction counts.
     * @return
     */
    public Map<String, BigInteger> getTransCnt() throws FrontException {

        Map<String, BigInteger> map = new HashMap<String, BigInteger>();
        try {

            TotalTransactionCount.TransactionCount transactionCount = web3j
                    .getTotalTransactionCount().send().getTotalTransactionCount();
            map.put("transactionCount", transactionCount.getTxSum());
            map.put("blockNumber", transactionCount.getBlockNumber());
        } catch (IOException e) {
            log.error("getTransCnt fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return map;
    }

    /**
     * getTransByBlockHashAndIndex.
     * 
     * @param blockHash blockHash
     * @param transactionIndex index
     * @return
     */
    public Transaction getTransByBlockHashAndIndex(String blockHash, BigInteger transactionIndex)
            throws FrontException {

       Transaction transaction= null;
        try {
            Optional<Transaction> opt = web3j.getTransactionByBlockHashAndIndex(blockHash, transactionIndex).send().getTransaction();
            if (opt.isPresent()) {
                transaction= opt.get();
            }
        } catch (IOException e) {
            log.error("getTransByBlockHashAndIndex fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transaction;
    }

    /**
     * getTransByBlockNumberAndIndex.
     * 
     * @param blockNumber blockNumber
     * @param transactionIndex index
     * @return
     */
    public Transaction getTransByBlockNumberAndIndex(BigInteger blockNumber, BigInteger transactionIndex) throws FrontException {
        Transaction transaction= null;
        try {
            if (blockNumberCheck(blockNumber)) {
               throw new FrontException("requst blockNumber is greater than latest");
            }
            Optional<Transaction> opt = web3j
                    .getTransactionByBlockNumberAndIndex(DefaultBlockParameter.valueOf(blockNumber), transactionIndex).send().getTransaction();
            if (opt.isPresent()) {
                transaction= opt.get();
            }
        } catch (IOException e) {
            log.error("getTransByBlockNumberAndIndex fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transaction;
    }

    private boolean blockNumberCheck(BigInteger blockNumber) throws IOException {
        BigInteger currentNumber = web3j.getBlockNumber().send().getBlockNumber();
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
     * nodeHeartBeat.
     * 
     * @return
     */
    public Map<String, BigInteger> nodeHeartBeat() throws FrontException {
        try {
            BigInteger currentBlockNumber = web3j.getBlockNumber().send().getBlockNumber();
            BigInteger currentPbftView = web3j.getPbftView().send().getPbftView();
            log.info("nodeHeartBeat blockNumber:{} current:{} pbftView:{} current:{}",
                    this.blockNumber, currentBlockNumber, this.pbftView, currentPbftView);
            if (currentBlockNumber.equals(this.blockNumber) && currentPbftView.equals(this.pbftView)) {
                throw new FrontException("blockNumber and pbftView unchanged");
            } else {
                this.blockNumber = currentBlockNumber;
                this.pbftView = currentPbftView;
            }
            Map<String, BigInteger> map = new HashMap<String, BigInteger>();
            map.put("blockNumber", currentBlockNumber);
            map.put("pbftView", currentPbftView);
            return map;
        } catch (IOException e) {
            log.error("nodeHeartBeat Exception.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }

    }

    public List<String> getGroupPeers() throws IOException {
        GroupPeers groupPeers = web3j.getGroupPeers().send();
        return groupPeers.getGroupPeers() ;
    }

    public List<String> getGroupList() throws IOException {
        return web3j.getGroupList().send().getGroupList();
    }

    // get all peers of chain
    public List<Peers.Peer> getPeers() throws IOException {
        return web3j.getPeers().send().getPeers();
    }

    public  String getConsensusStatus() throws IOException {
        return web3j.getConsensusStatus().sendForReturnString();
    }

    public String getSyncStatus() throws IOException {
        return web3j.getSyncStatus().sendForReturnString();
    }

    public String getSystemConfigByKey(String key) throws IOException {
        return web3j.getSystemConfigByKey(key).send().getSystemConfigByKey();
    }

}
