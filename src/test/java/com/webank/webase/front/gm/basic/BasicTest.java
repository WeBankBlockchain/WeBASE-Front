/*
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
package com.webank.webase.front.gm.basic;

import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.contract.CommonContract;
import com.webank.webase.front.contract.ContractService;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.AbiUtil;
import com.webank.webase.front.util.ContractAbiUtil;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.config.SystemConfigService;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.webank.webase.front.util.AbiUtil.outputFormat;
import static com.webank.webase.front.util.ContractAbiUtil.contractEventMap;
import static org.junit.Assert.assertTrue;

/**
 * 国密GenCredential与web3j在HelloWorldGM TestBase中static变量初始化
 * 测试get与发交易
 */
public class BasicTest extends TestBase {

  public static Credentials credentials =
          GenCredential.create("b83261efa42895c38c6c2364ca878f43e77f3cddbc922bf57d0d48070f79feb6");
  @Test
  public void testAddressZero() {
      System.out.println(Address.DEFAULT.toString());
  }

  @Test
  public void getBlockNumber() throws IOException {
    BigInteger blockNumber = web3j.getBlockNumber().send().getBlockNumber();
    assertTrue(blockNumber.compareTo(new BigInteger("0")) >= 0);
  }

  /**
   * 测试precompiled api
   * 国密credential
   * @throws Exception
   */
  @Test
  public void testSystemConfigService() throws Exception {

    SystemConfigService systemConfigSerivce = new SystemConfigService(web3j, credentials);
    systemConfigSerivce.setValueByKey("tx_count_limit", "2000");
    String value = web3j.getSystemConfigByKey("tx_count_limit").send().getSystemConfigByKey();
    System.out.println(value);
    assertTrue("2000".equals(value));
  }


  /**
   * 测试向国密链发交易 测试国密sm2的sign
   * 对部署的HelloWorldGM合约进行调用
   * @param *contractAddress: 0x57bb53e57ef30e207e1cc5be053c536950ec2243(部署后自行替换)
   * @throws Exception
   */
  @Test
  public void testSendTx() throws Exception {
    String contractAddress = "0x83a06fea6eba30c4f9f045279d95a02415b45614";
    HelloWorldGM helloWorldGM =
            HelloWorldGM.load(contractAddress, web3j, credentials,
                    new StaticGasProvider(gasPrice, gasLimit));
    TransactionReceipt receipt = helloWorldGM.set("test gm send tx").send();
    System.out.println("======receipt==========");

    System.out.println(receipt.getTransactionHash());
    String result = helloWorldGM.get().send();
    System.out.println(result);
    assertTrue("test gm send tx".equals(result));

//      System.out.println(web3j.getCode("0xec14ed13e3f4dfe2874f5dd13ba7d8363518bc41", DefaultBlockParameter.valueOf(new BigInteger("117"))));

  }


  /**
   * 调用合约
   * @throws Exception
   * contract address:
   *    js: 0xcab16429dc93ed4e394503719d7cd5ad2bc35d8a
   *  java: 0xedabfc904ab2ff2b69fc04bc0ab13365f9c5e9ee
   *  win:  0xa5a35ba945b52285c50c666c674f15751cef98f5
   * linux: 0x0d3fc8b3210a379022a3bc6f9b7f202c8b9e3aca
   */
  @Test
  public void deployAndCallHelloWorld() throws Exception {

    // deploy contract
    HelloWorldGM helloWorldGM =
            HelloWorldGM.deploy(
                    web3j,
                    credentials,
                    new StaticGasProvider(gasPrice, gasLimit))
                    .send();
    if (helloWorldGM != null) {
      System.out.println("HelloWorld address is: " + helloWorldGM.getContractAddress());
      // call set function
      helloWorldGM.set("Hello, World!").send();
      // call get function
      String result = helloWorldGM.get().send();
      System.out.println(result);
      assertTrue("Hello, World!".equals(result));
    }
  }

    /**
     * test abi bin in helloWorld
     * deploy through common contract
     * @throws Exception
     */
    @Test
    public void testDepolyContract() throws Exception {

        String contractName = "HelloWorld";
        String version = "2.0";
        List<AbiDefinition> abiList = ContractAbiUtil.loadContractDefinition(new File("src/test/resources/solidity/HelloWorldGM.abi"));
        ContractAbiUtil.setContractWithAbi(contractName, version, abiList, false);
        String bytecodeBin = "608060405234801561001057600080fd5b506040805190810160405280600d81526020017f48656c6c6f2c20576f726c6421000000000000000000000000000000000000008152506000908051906020019061005c929190610062565b50610107565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100a357805160ff19168380011785556100d1565b828001600101855582156100d1579182015b828111156100d05782518255916020019190600101906100b5565b5b5090506100de91906100e2565b5090565b61010491905b808211156101005760008160009055506001016100e8565b5090565b90565b6102d7806101166000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063299f7f9d146100515780633590b49f146100e1575b600080fd5b34801561005d57600080fd5b5061006661014a565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156100a657808201518184015260208101905061008b565b50505050905090810190601f1680156100d35780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b3480156100ed57600080fd5b50610148600480360381019080803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091929192905050506101ec565b005b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156101e25780601f106101b7576101008083540402835291602001916101e2565b820191906000526020600020905b8154815290600101906020018083116101c557829003601f168201915b5050505050905090565b8060009080519060200190610202929190610206565b5050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061024757805160ff1916838001178555610275565b82800160010185558215610275579182015b82811115610274578251825591602001919060010190610259565b5b5090506102829190610286565b5090565b6102a891905b808211156102a457600081600090555060010161028c565b5090565b905600a165627a7a72305820149885ffb94e77aa5ef86c3e347ff2d32491bd694173e82b03b08f67da4cebc00029";
        String encodedConstructor = ContractService.constructorEncodedByContractNameAndVersion(contractName, version, new ArrayList<>());

        CommonContract commonContract = null;
        commonContract = CommonContract.deploy(web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT,
                Constants.INITIAL_WEI_VALUE, bytecodeBin, encodedConstructor).send();
        System.out.println(commonContract.getContractAddress());

//        CnsService cnsService = new CnsService(web3j, credentials);
//        String result =  cnsService.registerCns(contractName ,version,commonContract.getContractAddress(),"[{\"constant\":false,\"inputs\":[{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"trans\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"TransEvent\",\"type\":\"event\"}]");
//        System.out.println(result);
    }

    /**
     * test call HelloWorldGM
     */
    @Test
    public void testInvokeSetFunctionFromAbi() throws Exception {
        String contractName = "hello";
        String version = "1.0";
        List<AbiDefinition> abiList = ContractAbiUtil.loadContractDefinition(new File("src/test/resources/solidity/HelloWorldGM.abi"));
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

        HelloWorldGM okDemo = HelloWorldGM.deploy(web3j, credentials, gasPrice, gasLimit).send();
        ContractGasProvider contractGasProvider = new StaticGasProvider(Constants.GAS_PRICE, Constants.GAS_LIMIT);
        CommonContract commonContract = CommonContract.load(okDemo.getContractAddress(), web3j, credentials, contractGasProvider);

        TransactionReceipt t  = TransService.execTransaction(function, commonContract);
        System.out.println(t);
        String res =  okDemo.get().send();
        System.out.println("*****************");
        System.out.println(res);
        //invoke get function
        String funcName1 = "get";
        List<String> funcInputTypes1 = versionEvents.get(0).getFuncInputs().get(funcName1);
        ArrayList a1 = new ArrayList();
        List<Object> params1 = a1;
        List<Type> finalInputs1 = AbiUtil.inputFormat(funcInputTypes1, params1);

        List<String> funOutputTypes1 = ContractAbiUtil.getFuncOutputType(contractName, funcName1, version);
        List<TypeReference<?>> finalOutputs1 = outputFormat(funOutputTypes1);
        Function function1 = new Function(funcName1, finalInputs1, finalOutputs1);
        Object o  = TransService.execCall(funOutputTypes1, function1, commonContract);
        System.out.println(o);

    }

    /**
     * get node version
     * api: rpc's getClientVersion
     */
    @Test
    public void getNodeVersion() throws IOException {
        String clientVersion = web3j.getNodeVersion().send().getNodeVersion().getVersion();
        System.out.println(clientVersion.contains("gm"));
        System.out.println(web3j.getNodeVersion().send().getNodeVersion().getVersion());
        System.out.println("============");
    }


}
