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
 * CommonContract.
 *
 */
public final class CommonContract extends Contract {

    private CommonContract(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super("", contractAddress, web3j, credentials, gasPrice, gasLimit, false);
    }

    // private CommonContract(String contractAddress, Web3j web3j, TransactionManager
    // transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
    // super("", contractAddress, web3j, transactionManager, gasPrice, gasLimit, false);
    // }

    private CommonContract(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, Boolean isInitByName) {
        super("", contractAddress, web3j, credentials, gasPrice, gasLimit, isInitByName);
    }

    // private CommonContract(String contractAddress, Web3j web3j, TransactionManager
    // transactionManager, BigInteger gasPrice, BigInteger gasLimit, Boolean isInitByName) {
    // super("", contractAddress, web3j, transactionManager, gasPrice, gasLimit, isInitByName);
    // }

    public Future<TransactionReceipt> execTransaction(Function function) {
        return executeTransactionAsync(function);
    }

    public Future<List<Type>> execCall(Function function) {
        return executeCallMultipleValueReturnAsync(function);
    }

    public static Future<CommonContract> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue,
            String contractBin, String encodedConstructor) {
        return deployAsync(CommonContract.class, web3j, credentials, gasPrice, gasLimit,
                contractBin, encodedConstructor, initialWeiValue);
    }

    // public static Future<CommonContract> deploy(Web3j web3j, TransactionManager
    // transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
    // return deployAsync(CommonContract.class, web3j, transactionManager, gasPrice, gasLimit,
    // BINARY, "", initialWeiValue);
    // }

    public static CommonContract load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new CommonContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    // public static CommonContract load(String contractAddress, Web3j web3j, TransactionManager
    // transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
    // return new CommonContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    // }

    public static CommonContract loadByName(String contractName, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CommonContract(contractName, web3j, credentials, gasPrice, gasLimit, true);
    }

    // public static CommonContract loadByName(String contractName, Web3j web3j, TransactionManager
    // transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
    // return new CommonContract(contractName, web3j, transactionManager, gasPrice, gasLimit, true);
    // }
}
