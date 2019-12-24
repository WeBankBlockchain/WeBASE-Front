package com.webank.webase.front.gm.basic;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.contract.ContractService;
import com.webank.webase.front.contract.entity.RspContractCompile;
import com.webank.webase.front.gm.basic.HelloWorldGM;
import com.webank.webase.front.keystore.KeyStoreService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.solidity.compiler.CompilationResult;
import org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Base64;
import java.util.HashMap;

import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.*;
import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.METADATA;
import static org.junit.Assert.*;

public class ContractTest {


    private static final String BASE_FILE_PATH = File.separator + "temp" + File.separator;
    private static final String CONTRACT_FILE_TEMP = BASE_FILE_PATH + "%1s.sol";

    public static BigInteger gasPrice = new BigInteger("3000000");
    public static BigInteger gasLimit = new BigInteger("300000000");

    @Test
    public void compileTest() throws Exception {

        String solidityName = "HelloWorldGM";
        // HelloWorldGM.sol encoded in base64
        InputStream is = new ClassPathResource("/contract/HelloWorldGM.sol").getInputStream();
        byte[] solHelloGM = IOUtils.toByteArray(is);
        String sourceBase64 = Base64.getEncoder().encodeToString(solHelloGM);
//        String sourceBase64 = "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjQ7DQoNCmNvbnRyYWN0IEhlbGxvV29ybGRHTXsNCiAgIHN0cmluZyBuYW1lOw0KDQogICBjb25zdHJ1Y3RvcigpIHB1YmxpY3sNCiAgICAgIG5hbWUgPSAiSGVsbG8sIFdvcmxkISI7DQogICB9DQogIGZ1bmN0aW9uIGdldCgpIGNvbnN0YW50IHB1YmxpYyByZXR1cm5zKHN0cmluZyl7DQogICAgICByZXR1cm4gbmFtZTsNCiAgfQ0KDQogIGZ1bmN0aW9uIHNldChzdHJpbmcgbikgcHVibGljew0KICAgICAgbmFtZSA9IG47DQogIH0NCn0";
        System.out.println(sourceBase64);
        RspContractCompile res = contractCompile(solidityName, sourceBase64);
        System.out.println(res.getContractName());
        System.out.println(res.getContractBin());
        System.out.println(res.getContractAbi());
        System.out.println(res.getErrors());
        assertNotNull(res.getContractBin());
    }


    public RspContractCompile contractCompile(String contractName, String sourceBase64) {
        File contractFile = null;
        try {
            // decode
            byte[] contractSourceByteArr = Base64.getDecoder().decode(sourceBase64);
            String contractFilePath = String.format(CONTRACT_FILE_TEMP, contractName);
            // save contract to file
            contractFile = new File(contractFilePath);
            FileUtils.writeByteArrayToFile(contractFile, contractSourceByteArr);
            //compile
            SolidityCompiler.Result res = SolidityCompiler.compile(contractFile, true, ABI, BIN, INTERFACE, METADATA);
            if ("".equals(res.output)) {
                throw new FrontException(ConstantCode.CONTRACT_COMPILE_FAIL.getCode(), res.errors);
            }
            // compile result
            CompilationResult result = CompilationResult.parse(res.output);
            CompilationResult.ContractMetadata meta = result.getContract(contractName);
            RspContractCompile compileResult = new RspContractCompile(contractName, meta.abi, meta.bin, res.errors);
            return compileResult;
        } catch (Exception ex) {
            throw new FrontException(ConstantCode.CONTRACT_COMPILE_FAIL.getCode(), ex.getMessage());
        } finally {
            if (contractFile != null) {
                contractFile.deleteOnExit();
            }
        }

    }
}
