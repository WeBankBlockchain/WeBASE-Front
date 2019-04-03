package com.webank.webase.front.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;

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
 * CommonContract.
 *
 */
public final class CommonContract extends Contract {

    private CommonContract(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super("", contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CommonContract(String contractAddress, Web3j web3j, Credentials credentials,
                       ContractGasProvider gasProvider) {
    super("",contractAddress,web3j,credentials,gasProvider);
    }

    public TransactionReceipt execTransaction(Function function) throws IOException, TransactionException {
        return executeTransaction(function);
    }

    public List<Type> execCall(Function function) throws IOException {
        return executeCallMultipleValueReturn(function);
    }

    public static RemoteCall<CommonContract> deploy(Web3j web3j, Credentials credentials,
                                                    BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue,
                                                    String contractBin, String encodedConstructor) {
        return deployRemoteCall(CommonContract.class, web3j, credentials, gasPrice, gasLimit,
                contractBin, encodedConstructor, initialWeiValue);
    }

    public static CommonContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CommonContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }


    public static CommonContract loadByName(String contractName, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CommonContract(contractName, web3j, credentials, gasPrice, gasLimit);
    }

}
