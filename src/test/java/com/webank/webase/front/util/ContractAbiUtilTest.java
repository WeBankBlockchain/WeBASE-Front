/**
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.util;

import static com.webank.webase.front.util.AbiUtil.outputFormat;
import static com.webank.webase.front.util.ContractAbiUtil.contractEventMap;
import static org.junit.Assert.assertEquals;

import com.webank.webase.front.base.TestBase;
import com.webank.webase.front.contract.CommonContract;
import com.webank.webase.front.contract.ContractService;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.model.RetCode;
import org.junit.Test;

public class ContractAbiUtilTest extends TestBase {



    @Test
    public void testBuildType() {

        HashMap web3jMap= new HashMap<Integer, Client>();
        web3jMap.put("1", web3j);
        String s = ContractAbiUtil.buildTypeName("address[]").toString();
        String s1 = ContractAbiUtil.buildTypeName("address[4]").toString();
        assertEquals(s, "org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Address>");
        assertEquals(s1, "org.fisco.bcos.web3j.abi.datatypes.generated.StaticArray4<org.fisco.bcos.web3j.abi.datatypes.Address>");
    }

    @Test
    public void testSetFunctionFromAbi() throws Exception {
        String contractName = "hello";
        String version = "1.0";
        List<ABIDefinition> abiList = ContractAbiUtil.loadContractDefinition(new File("src/test/resources/solidity/Ok.abi"));
        ContractAbiUtil.setContractWithAbi(contractName, version, abiList, false);
        List<ContractAbiUtil.VersionEvent> versionEvents = contractEventMap.get("hello");
        String funcName = "trans";
        List<String> funcInputTypes = versionEvents.get(0).getFuncInputs().get(funcName);
        ArrayList a = new ArrayList();
        a.add("123");
        List<Object> params = a;
        List<Type> finalInputs = AbiUtil.inputFormat(funcInputTypes, params);
        List<String> funOutputTypes = ContractAbiUtil.getFuncOutputType(contractName, "trans", version);
        List<TypeReference<?>> finalOutputs = outputFormat(funOutputTypes);
        Function function = new Function(funcName, finalInputs, finalOutputs);

//        Ok okDemo = Ok.deploy(web3j, credentials, gasPrice, gasLimit).send();
//        ContractGasProvider contractGasProvider = new StaticGasProvider(Constants.GAS_PRICE, Constants.GAS_LIMIT);
//        CommonContract commonContract = CommonContract.load(okDemo.getContractAddress(), web3j, credentials, contractGasProvider);
//
//        TransactionReceipt t  = TransService.execTransaction(function, commonContract);
//        System.out.println("***********************");
//        System.out.println(t);
//
//        //invoke get function
//        String funcName1 = "get";
//        List<String> funcInputTypes1 = versionEvents.get(0).getFuncInputs().get(funcName1);
//        ArrayList a1 = new ArrayList();
//        List<Object> params1 = a1;
//        List<Type> finalInputs1 = AbiUtil.inputFormat(funcInputTypes1, params1);
//
//        List<String> funOutputTypes1 = ContractAbiUtil.getFuncOutputType(contractName, funcName1, version);
//        List<TypeReference<?>> finalOutputs1 = outputFormat(funOutputTypes1);
//        Function function1 = new Function(funcName1, finalInputs1, finalOutputs1);
//        Object o  = TransService.execCall(funOutputTypes1, function1, commonContract);
    }
 @Test
    public void testInvokeSetFunctionFromAbi() throws Exception {
        String contractName = "hello";
        String version = "1.0";
        List<ABIDefinition> abiList = ContractAbiUtil.loadContractDefinition(new File("src/test/resources/solidity/HelloWorld.abi"));
        ContractAbiUtil.setContractWithAbi(contractName, version, abiList, false);
        List<ContractAbiUtil.VersionEvent> versionEvents = contractEventMap.get("hello");
        String funcName = "set";
        List<String> funcInputTypes = versionEvents.get(0).getFuncInputs().get(funcName);
        ArrayList a = new ArrayList();
//        a.add("123");
//        a.add("12345");
        a.add("12,23,34,45");
        List<Object> params = a;
        List<Type> finalInputs = AbiUtil.inputFormat(funcInputTypes, params);
        List<String> funOutputTypes = ContractAbiUtil.getFuncOutputType(contractName, "set", version);
        List<TypeReference<?>> finalOutputs = outputFormat(funOutputTypes);
        Function function = new Function(funcName, finalInputs, finalOutputs);

//        HelloWorld okDemo = HelloWorld.deploy(web3j, credentials, gasPrice, gasLimit).send();
//        ContractGasProvider contractGasProvider = new StaticGasProvider(Constants.GAS_PRICE, Constants.GAS_LIMIT);
//        CommonContract commonContract = CommonContract.load(okDemo.getContractAddress(), web3j, credentials, contractGasProvider);
//
//        TransactionReceipt t  = TransService.execTransaction(function, commonContract);
//        System.out.println(t);
//       List ilist =  okDemo.get().send();
//        System.out.println("*****************");
//        ilist.stream().forEach(System.out::println);
//       //invoke get function
//        String funcName1 = "get";
//        List<String> funcInputTypes1 = versionEvents.get(0).getFuncInputs().get(funcName1);
//        ArrayList a1 = new ArrayList();
//        List<Object> params1 = a1;
//        List<Type> finalInputs1 = AbiUtil.inputFormat(funcInputTypes1, params1);
//
//        List<String> funOutputTypes1 = ContractAbiUtil.getFuncOutputType(contractName, funcName1, version);
//        List<TypeReference<?>> finalOutputs1 = outputFormat(funOutputTypes1);
//        Function function1 = new Function(funcName1, finalInputs1, finalOutputs1);
//        Object o  = TransService.execCall(funOutputTypes1, function1, commonContract);
//        System.out.println(o);

    }


}

