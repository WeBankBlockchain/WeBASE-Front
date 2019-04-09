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

package com.webank.webase.front.contract;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.tx.Contract;

/**
 * common contract handle class.
 *
 */
public final class CommonContract extends Contract {

    /**
     * constructor.
     * 
     * @param contractAddress
     * @param web3j
     * @param credentials
     * @param gasPrice
     * @param gasLimit
     */
    private CommonContract(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super("", contractAddress, web3j, credentials, gasPrice, gasLimit, false);
    }

    /**
     * constructor.
     * 
     * @param contractAddress
     * @param web3j
     * @param credentials
     * @param gasPrice
     * @param gasLimit
     * @param isInitByName
     */
    private CommonContract(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, Boolean isInitByName) {
        super("", contractAddress, web3j, credentials, gasPrice, gasLimit, isInitByName);
    }

    /**
     * send transaction to block chain.
     * 
     * @param function
     * @return
     */
    public Future<TransactionReceipt> execTransaction(Function function) {
        return executeTransactionAsync(function);
    }

    /**
     * query transaction from block chain.
     * 
     * @param function
     * @return
     */
    public Future<List<Type>> execCall(Function function) {
        return executeCallMultipleValueReturnAsync(function);
    }

    /**
     * contract deploy.
     * 
     * @param web3j
     * @param credentials
     * @param gasPrice
     * @param gasLimit
     * @param initialWeiValue
     * @param contractBin
     * @param encodedConstructor
     * @return
     */
    public static Future<CommonContract> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue,
            String contractBin, String encodedConstructor) {
        return deployAsync(CommonContract.class, web3j, credentials, gasPrice, gasLimit,
                contractBin, encodedConstructor, initialWeiValue);
    }

    /**
     * load contract by contract address.
     * 
     * @param contractAddress
     * @param web3j
     * @param credentials
     * @param gasPrice
     * @param gasLimit
     * @return
     */
    public static CommonContract load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new CommonContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    /**
     * load contract by contract name.
     * 
     * @param contractName
     * @param web3j
     * @param credentials
     * @param gasPrice
     * @param gasLimit
     * @return
     */
    public static CommonContract loadByName(String contractName, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CommonContract(contractName, web3j, credentials, gasPrice, gasLimit, true);
    }
}
