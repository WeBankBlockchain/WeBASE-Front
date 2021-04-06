package com.webank.webase.front.contract;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.contract.entity.RspContractCompile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Base64;

import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.solc.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;
import static org.fisco.solc.compiler.SolidityCompiler.Options.INTERFACE;
import static org.junit.Assert.*;

public class ContractTest{


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
        // use guomi to compile
        RspContractCompile res = contractCompile(solidityName, sourceBase64, true);
        assertNotNull(res.getBytecodeBin());
        System.out.println("GUOMI SM2:=======");
        System.out.println(res.getContractName());
        System.out.println(res.getBytecodeBin());
        System.out.println(res.getContractAbi());
        // not use guomi
        RspContractCompile resFalse = contractCompile(solidityName, sourceBase64, false);
        assertNotNull(resFalse.getBytecodeBin());
        System.out.println("ECDSA:=======");
        System.out.println(resFalse.getContractName());
        System.out.println(resFalse.getBytecodeBin());
        System.out.println(resFalse.getContractAbi());
    }

    /**
     *
     * @param contractName
     * @param sourceBase64
     * @param useSM2 true-guomi, false-ecdsa
     * @return
     */
    public RspContractCompile contractCompile(String contractName, String sourceBase64, boolean useSM2) {
        File contractFile = null;
        try {
            // decode
            byte[] contractSourceByteArr = Base64.getDecoder().decode(sourceBase64);
            String contractFilePath = String.format(CONTRACT_FILE_TEMP, contractName);
            // save contract to file
            contractFile = new File(contractFilePath);
            FileUtils.writeByteArrayToFile(contractFile, contractSourceByteArr);
            //compile
            SolidityCompiler.Result res = SolidityCompiler.compile(contractFile, useSM2, true, ABI, BIN, INTERFACE, METADATA);
            if ("".equals(res.getOutput())) {
                throw new FrontException(ConstantCode.CONTRACT_COMPILE_FAIL.getCode(), res.getErrors());
            }
            // compile result
            CompilationResult result = CompilationResult.parse(res.getOutput());
            CompilationResult.ContractMetadata meta = result.getContract(contractName);
            RspContractCompile compileResult = new RspContractCompile(contractName, meta.abi, meta.bin, res.getErrors());
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
