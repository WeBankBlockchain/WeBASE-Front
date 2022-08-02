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

import static com.webank.webase.front.util.ContractAbiUtil.contractEventMap;
import static org.junit.Assert.assertEquals;

import com.webank.webase.front.base.TestBase;
import com.webank.webase.front.contract.CommonContract;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodec;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.junit.Test;

public class ContractAbiUtilTest extends TestBase {


    @Test
    public void testDepolyContract() throws Exception {

        String contractName = "hello";
        String version = "2.0";
        List<ABIDefinition> abiList = ContractAbiUtil.loadContractDefinition(new File("src/test/resources/solidity/Ok.abi"));
        ContractAbiUtil.setContractWithAbi(contractName, version, abiList, false);
        ContractCodec abiCodec = new ContractCodec(cryptoSuite, false);

        String bytecodeBin = "608060405234801561001057600080fd5b5060016000800160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506402540be40060006001018190555060028060000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060006002600101819055506103bf806100c26000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100515780636d4ce63c1461007e575b600080fd5b34801561005d57600080fd5b5061007c600480360381019080803590602001909291905050506100a9565b005b34801561008a57600080fd5b506100936102e1565b6040518082815260200191505060405180910390f35b8060006001015410806100c757506002600101548160026001015401105b156100d1576102de565b8060006001015403600060010181905550806002600101600082825401925050819055507fc77b710b83d1dc3f3fafeccd08a6c469beb873b2f0975b50d1698e46b3ee5b4c816040518082815260200191505060405180910390a160046080604051908101604052806040805190810160405280600881526020017f323031373034313300000000000000000000000000000000000000000000000081525081526020016000800160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001838152509080600181540180825580915050906001820390600052602060002090600402016000909192909190915060008201518160000190805190602001906102419291906102ee565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b6000600260010154905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061032f57805160ff191683800117855561035d565b8280016001018555821561035d579182015b8281111561035c578251825591602001919060010190610341565b5b50905061036a919061036e565b5090565b61039091905b8082111561038c576000816000905550600101610374565b5090565b905600a165627a7a72305820044e82d74a0d492f9f764e8bbf8eeca940eee670c9ab7bba9861db1d522ab6400029";
        byte[] encodedConstructor = abiCodec
            .encodeConstructor(JsonUtils.objToString(abiList), bytecodeBin, new ArrayList<>());

        CommonContract commonContract = null;
            commonContract = CommonContract.deploy(web3j,
                cryptoKeyPair, bytecodeBin, encodedConstructor);
        System.out.println(commonContract.getContractAddress());
        BFSService cnsService = new BFSService(web3j, cryptoKeyPair);
        RetCode result =  cnsService.link(contractName ,version,commonContract.getContractAddress(),"[{\"constant\":false,\"inputs\":[{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"trans\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"TransEvent\",\"type\":\"event\"}]");
        System.out.println(result);
    }

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
        List<Type> finalInputs = ContractAbiUtil.inputFormat(funcInputTypes, params);
        List<String> funOutputTypes = ContractAbiUtil.getFuncOutputType(contractName, "trans", version);
        List<TypeReference<?>> finalOutputs = ContractAbiUtil.outputFormat(funOutputTypes);
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
        List<Type> finalInputs = ContractAbiUtil.inputFormat(funcInputTypes, params);
        List<String> funOutputTypes = ContractAbiUtil.getFuncOutputType(contractName, "set", version);
        List<TypeReference<?>> finalOutputs = ContractAbiUtil.outputFormat(funOutputTypes);
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

