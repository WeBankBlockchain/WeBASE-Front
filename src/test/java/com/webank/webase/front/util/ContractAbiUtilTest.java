package com.webank.webase.front.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.channel.test.Ok;
import com.webank.webase.front.channel.test.TestBase;
import com.webank.webase.front.contract.CommonContract;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.ContractAbiUtil;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.webank.webase.front.transaction.TransService.outputFormat;
import static com.webank.webase.front.util.ContractAbiUtil.contractEventMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ContractAbiUtilTest extends TestBase {


    @Test
    public  void testSetFunctionFromAbi() throws Exception {
        String contractName= "hello";
        String version="1.0";
        List<AbiDefinition> abiList = ContractAbiUtil.loadContractDefinition(new File("src/test/resources/solidity/Ok.abi"));
        ContractAbiUtil.setContractWithAbi(contractName,version, JSONArray.parseArray(JSON.toJSONString(abiList)),false);
        List<ContractAbiUtil.VersionEvent> versionEvents = contractEventMap.get("hello");
        String funcName = "trans";
        List<String> funcInputTypes   = versionEvents.get(0).getFuncInputs().get(funcName);
        ArrayList a = new ArrayList();
        a.add(123);
        List<Object> params = a;
        List<Type> finalInputs = TransService.inputFormat(funcInputTypes, params);
        List<String> funOutputTypes = ContractAbiUtil.getFuncOutputType(contractName, "trans", version);
        List<TypeReference<?>> finalOutputs = outputFormat(funOutputTypes);
        Function function = new Function(funcName, finalInputs, finalOutputs);

        Ok okDemo = Ok.deploy(web3j, credentials, gasPrice, gasLimit).send();
        CommonContract commonContract = CommonContract.load(okDemo.getContractAddress(), web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT);

        BaseResponse baseRsp = new BaseResponse(ConstantCode.RET_SUCCEED);
        baseRsp = TransService.execTransaction(function, commonContract, baseRsp);
        System.out.println(  baseRsp.getData());

    //invoke get function
        String funcName1 = "get";
        List<String> funcInputTypes1   = versionEvents.get(0).getFuncInputs().get(funcName1);
        ArrayList a1 = new ArrayList();
        List<Object> params1 = a1;
        List<Type> finalInputs1 = TransService.inputFormat(funcInputTypes1, params1);

        List<String> funOutputTypes1 = ContractAbiUtil.getFuncOutputType(contractName, funcName1, version);
        List<TypeReference<?>> finalOutputs1 = outputFormat(funOutputTypes1);
        Function function1 = new Function(funcName1, finalInputs1, finalOutputs1);
        BaseResponse baseRsp1 = new BaseResponse(ConstantCode.RET_SUCCEED);
        baseRsp1 = TransService.execCall(funOutputTypes1, function1, commonContract, baseRsp1);
        System.out.println(  baseRsp1.getData());
        assertEquals(baseRsp1.getData().toString(),"[123]");
    }


    @Test
    public void testBuildType(){
        String s =ContractAbiUtil.buildTypeName("address[]").toString();
        String s1 =ContractAbiUtil.buildTypeName("address[4]").toString();
        assertEquals(s,"org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Address>");
        assertEquals(s1,"org.fisco.bcos.web3j.abi.datatypes.generated.StaticArray4<org.fisco.bcos.web3j.abi.datatypes.Address>");
    }
}
