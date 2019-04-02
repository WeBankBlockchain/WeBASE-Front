package com.webank.webase.front.util;

import com.webank.webase.front.base.Constants;
import com.webank.webase.front.channel.test.Ok;
import com.webank.webase.front.channel.test.TestBase;
import com.webank.webase.front.contract.CommonContract;
import com.webank.webase.front.contract.ContractService;
import com.webank.webase.front.transaction.TransService;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.webank.webase.front.transaction.TransService.outputFormat;
import static com.webank.webase.front.util.ContractAbiUtil.contractEventMap;
import static org.junit.Assert.assertEquals;

public class ContractAbiUtilTest extends TestBase {


    @Test
    public void testDepolyContract() throws Exception {

        String contractName = "hello";
        String version = "1.0";
        List<AbiDefinition> abiList = ContractAbiUtil.loadContractDefinition(new File("src/test/resources/solidity/Ok.abi"));
        ContractAbiUtil.addAbiToCacheMapAndSaveToFile(contractName, version, abiList, false);

        String bytecodeBin = "608060405234801561001057600080fd5b5060016000800160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506402540be40060006001018190555060028060000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060006002600101819055506103bf806100c26000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100515780636d4ce63c1461007e575b600080fd5b34801561005d57600080fd5b5061007c600480360381019080803590602001909291905050506100a9565b005b34801561008a57600080fd5b506100936102e1565b6040518082815260200191505060405180910390f35b8060006001015410806100c757506002600101548160026001015401105b156100d1576102de565b8060006001015403600060010181905550806002600101600082825401925050819055507fc77b710b83d1dc3f3fafeccd08a6c469beb873b2f0975b50d1698e46b3ee5b4c816040518082815260200191505060405180910390a160046080604051908101604052806040805190810160405280600881526020017f323031373034313300000000000000000000000000000000000000000000000081525081526020016000800160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001838152509080600181540180825580915050906001820390600052602060002090600402016000909192909190915060008201518160000190805190602001906102419291906102ee565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b6000600260010154905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061032f57805160ff191683800117855561035d565b8280016001018555821561035d579182015b8281111561035c578251825591602001919060010190610341565b5b50905061036a919061036e565b5090565b61039091905b8082111561038c576000816000905550600101610374565b5090565b905600a165627a7a72305820044e82d74a0d492f9f764e8bbf8eeca940eee670c9ab7bba9861db1d522ab6400029";
        String encodedConstructor = ContractService.constructorEncoded(contractName, version, new ArrayList<>());

        CommonContract commonContract = null;
            commonContract = CommonContract.deploy(web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT,
                    Constants.INITIAL_WEI_VALUE, bytecodeBin, encodedConstructor).send();
        System.out.println(commonContract.getContractAddress());
        CnsService cnsService = new CnsService(web3j, credentials);
        String result =  cnsService.registerCns(contractName ,version,commonContract.getContractAddress(),"[{\"constant\":false,\"inputs\":[{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"trans\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"TransEvent\",\"type\":\"event\"}]");
        System.out.println(result);
    }

    @Test
    public void testSetFunctionFromAbi() throws Exception {
        String contractName = "hello";
        String version = "1.0";
        List<AbiDefinition> abiList = ContractAbiUtil.loadContractDefinition(new File("src/test/resources/solidity/Ok.abi"));
        ContractAbiUtil.addAbiToCacheMapAndSaveToFile(contractName, version, abiList, false);
        List<ContractAbiUtil.VersionEvent> versionEvents = contractEventMap.get("hello");
        String funcName = "trans";
        List<String> funcInputTypes = versionEvents.get(0).getFuncInputs().get(funcName);
        ArrayList a = new ArrayList();
        a.add(123);
        List<Object> params = a;
        List<Type> finalInputs = TransService.inputFormat(funcInputTypes, params);
        List<String> funOutputTypes = ContractAbiUtil.getFuncOutputType(contractName, "trans", version);
        List<TypeReference<?>> finalOutputs = outputFormat(funOutputTypes);
        Function function = new Function(funcName, finalInputs, finalOutputs);

        Ok okDemo = Ok.deploy(web3j, credentials, gasPrice, gasLimit).send();
        CommonContract commonContract = CommonContract.load(okDemo.getContractAddress(), web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT);

        TransactionReceipt t  = TransService.execTransaction(function, commonContract);
        System.out.println(t);

        //invoke get function
        String funcName1 = "get";
        List<String> funcInputTypes1 = versionEvents.get(0).getFuncInputs().get(funcName1);
        ArrayList a1 = new ArrayList();
        List<Object> params1 = a1;
        List<Type> finalInputs1 = TransService.inputFormat(funcInputTypes1, params1);

        List<String> funOutputTypes1 = ContractAbiUtil.getFuncOutputType(contractName, funcName1, version);
        List<TypeReference<?>> finalOutputs1 = outputFormat(funOutputTypes1);
        Function function1 = new Function(funcName1, finalInputs1, finalOutputs1);
        Object o  = TransService.execCall(funOutputTypes1, function1, commonContract);
    }


    @Test
    public void testBuildType() {

        HashMap web3jMap= new HashMap<Integer, Web3j>();
        web3jMap.put("1", web3j);
        String s = ContractAbiUtil.buildTypeName("address[]").toString();
        String s1 = ContractAbiUtil.buildTypeName("address[4]").toString();
        assertEquals(s, "org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Address>");
        assertEquals(s1, "org.fisco.bcos.web3j.abi.datatypes.generated.StaticArray4<org.fisco.bcos.web3j.abi.datatypes.Address>");
    }

    @Test
    public void testCompileJava() throws IOException {
        List<AbiDefinition> abiList = ContractAbiUtil.loadContractDefinition(new File("src/test/resources/solidity/Ok.abi"));
        String bin = "60606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100465780636d4ce63c14610066575bfe5b341561004e57fe5b610064600480803590602001909190505061008c565b005b341561006e57fe5b610076610264565b6040518082815260200191505060405180910390f35b806000600101540360006001018190555080600260010160008282540192505081905550600480548060010182816100c49190610272565b916000526020600020906004020160005b608060405190810160405280604060405190810160405280600881526020017f32303137303431330000000000000000000000000000000000000000000000008152508152602001600060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200185815250909190915060008201518160000190805190602001906101c49291906102a4565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b600060026001015490505b90565b81548183558181151161029f5760040281600402836000526020600020918201910161029e9190610324565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e557805160ff1916838001178555610313565b82800160010185558215610313579182015b828111156103125782518255916020019190600101906102f7565b5b50905061032091906103aa565b5090565b6103a791905b808211156103a357600060008201600061034491906103cf565b6001820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556002820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff021916905560038201600090555060040161032a565b5090565b90565b6103cc91905b808211156103c85760008160009055506001016103b0565b5090565b90565b50805460018160011615610100020316600290046000825580601f106103f55750610414565b601f01602090049060005260206000209081019061041391906103aa565b5b505600a165627a7a72305820d453cb481a312519166e409e7248d76d8c2672458c08b9500945a4004a1b69020029";
        ContractService.compileToJavaFile("Ok",abiList,bin,"");
    }
}

