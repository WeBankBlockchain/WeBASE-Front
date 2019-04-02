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
    Map<Integer, Web3j> web3jMap;
    @Autowired
    NodeConfig nodeConfig;

    private static BigInteger blockNumber = new BigInteger("0");
    private static BigInteger pbftView = new BigInteger("0");

    /**
     * getBlockNumber.
     * 
     * @return
     */
    public BigInteger getBlockNumber(int groupId)  {

        BigInteger blockNumber;
        try {
             blockNumber = web3jMap.get(groupId).getBlockNumber().send().getBlockNumber();
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
    public BcosBlock.Block getBlockByNumber(int groupId,BigInteger blockNumber)  {
        BcosBlock.Block block;
        try {
            if (blockNumberCheck(groupId,blockNumber)) {
                throw new FrontException("requst blockNumber is greater than latest");
            }
             block = web3jMap.get(groupId).getBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
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
    public BcosBlock.Block getBlockByHash(int groupId , String blockHash)  {
        BcosBlock.Block block;
        try {
            block = web3jMap.get(groupId).getBlockByHash(blockHash, true).send().getBlock();

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
    public int getBlockTransCntByNumber(int groupId,BigInteger blockNumber)  {
        int transCnt;
        try {
            if (blockNumberCheck(groupId,blockNumber)) {
                throw new FrontException("requst blockNumber is greater than latest");
            }
            BcosBlock.Block block = web3jMap.get(groupId).getBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
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
    public BigInteger getPbftView(int groupId)  {

       BigInteger result;
        try {
            result = web3jMap.get(groupId).getPbftView().send().getPbftView();
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
    public TransactionReceipt getTransactionReceipt(int groupId,String transHash)  {

        TransactionReceipt transactionReceipt =null;
        try {
            Optional<TransactionReceipt> opt = web3jMap.get(groupId).getTransactionReceipt(transHash).send().getTransactionReceipt();
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
    public Transaction getTransactionByHash(int groupId, String transHash)  {

        Transaction transaction= null;
        try {
            Optional<Transaction> opt = web3jMap.get(groupId).getTransactionByHash(transHash).send().getTransaction();
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
    public String getClientVersion()  {
        String version;
        try {
             version = web3jMap.get(1).getNodeVersion().send().getNodeVersion().getVersion();
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
        public String  getCode(int groupId, String address, BigInteger blockNumber)  {
            String code;
            try {
            if (blockNumberCheck(groupId,blockNumber)) {
                throw new FrontException("requst blockNumber is greater than latest");
            }
             code = web3jMap.get(groupId).getCode(address, DefaultBlockParameter.valueOf(blockNumber)).send().getCode();
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
    public TotalTransactionCount.TransactionCount getTransCnt(int groupId)  {
        TotalTransactionCount.TransactionCount transactionCount;
        try {
             transactionCount = web3jMap.get(groupId).getTotalTransactionCount().send().getTotalTransactionCount();
        } catch (IOException e) {
            log.error("getTransCnt fail.");
            throw new FrontException(ConstantCode.NODE_REQUEST_FAILED);
        }
        return transactionCount;
    }

    /**
     * getTransByBlockHashAndIndex.
     * 
     * @param blockHash blockHash
     * @param transactionIndex index
     * @return
     */
    public Transaction getTransByBlockHashAndIndex(int groupId, String blockHash, BigInteger transactionIndex)
             {

       Transaction transaction= null;
        try {
            Optional<Transaction> opt = web3jMap.get(groupId).getTransactionByBlockHashAndIndex(blockHash, transactionIndex).send().getTransaction();
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
    public Transaction getTransByBlockNumberAndIndex(int groupId, BigInteger blockNumber, BigInteger transactionIndex)  {
        Transaction transaction= null;
        try {
            if (blockNumberCheck(groupId,blockNumber)) {
               throw new FrontException("requst blockNumber is greater than latest");
            }
            Optional<Transaction> opt = web3jMap.get(groupId)
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

    private boolean blockNumberCheck(int groupId, BigInteger blockNumber)  {
        BigInteger currentNumber = null;
        try {
            currentNumber = web3jMap.get(groupId).getBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("****" + currentNumber);
        if (blockNumber.compareTo(currentNumber) == 1) {
            return true;
        }
        return false;
    }


    /**
     * nodeHeartBeat.
     * 
     * @return
     */
    public Map<String, BigInteger> nodeHeartBeat(int groupId)  {
        try {
            BigInteger currentBlockNumber = web3jMap.get(groupId).getBlockNumber().send().getBlockNumber();
            BigInteger currentPbftView = web3jMap.get(groupId).getPbftView().send().getPbftView();
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

    public List<String> getGroupPeers(int groupId)  {
        GroupPeers groupPeers = null;
        try {
            groupPeers = web3jMap.get(groupId).getGroupPeers().send();
        } catch (IOException e) {
            throw new FrontException(e.getMessage());
        }
        return groupPeers.getGroupPeers() ;
    }

    public List<String> getGroupList()  {
        try {
            return web3jMap.get(1).getGroupList().send().getGroupList();
        } catch (IOException e) {
            throw new FrontException(e.getMessage());
        }
    }

    // get all peers of chain
    public List<Peers.Peer> getPeers(int groupId)  {
        try {
            return web3jMap.get(groupId).getPeers().send().getPeers();
        } catch (IOException e) {
            throw new FrontException(e.getMessage());
        }
    }

    public  String getConsensusStatus(int groupId) {
        try {
            return web3jMap.get(groupId).getConsensusStatus().sendForReturnString();
        } catch (IOException e) {
            throw new FrontException(e.getMessage());
        }
    }

    public String getSyncStatus(int groupId)  {
        try {
            return web3jMap.get(groupId).getSyncStatus().sendForReturnString();
        } catch (IOException e) {
            throw new FrontException(e.getMessage());
        }
    }

    public String getSystemConfigByKey(int groupId, String key)   {
        try {
            return web3jMap.get(groupId).getSystemConfigByKey(key).send().getSystemConfigByKey();
        } catch (IOException e) {
           throw new FrontException(e.getMessage());
        }
    }

}
