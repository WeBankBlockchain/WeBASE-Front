package com.webank.webase.front.contract;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.exception.FileException;
import com.webank.webase.front.file.FileContent;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

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
 * contract management.
 */
@Slf4j
@Service
public class ContractService {
    @Autowired
    Web3j web3j;
    @Autowired
    TransService transService;
    @Autowired
    CnsService cnsService;
    @Autowired
    ContractRepository contractRepository;

    /**
     * saveAbi.
     *
     * @param req request data
     * @return
     */
    public BaseResponse saveAbi(ReqSendAbi req) throws FrontException {
        BaseResponse baseRsp = new BaseResponse(ConstantCode.RET_SUCCEED);

        String contractName = req.getContractName();
        String version = req.getVersion();
        List<AbiDefinition> abiInfos = req.getAbiInfo();
        String binary = req.getBinaryCode() == null ? "" : req.getBinaryCode();

        // Check if it has been deployed based on the contract name and version number
        checkContractAbiExistedAndSave(contractName, version, abiInfos);
        saveBinary(contractName, version, binary);
        return baseRsp;
    }

    private void saveBinary(String contractName, String version, String binary) throws FrontException {
        try {
            File file = new File(Constants.BIN_DIR + Constants.DIAGONAL + contractName + Constants.SEP + version);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileUtils.writeStringToFile(file, binary);
            //todo
        } catch (IOException e) {
            log.error("saveAbiFile failed.");
            throw new FrontException(ConstantCode.ABI_SAVE_ERROR);
        }
    }

    /**
     * contract deploy.
     *
     * @param req request data
     * @return
     */
    public BaseResponse deploy(ReqDeploy req) throws Exception {

        int userId = req.getUserId();
        String contractName = req.getContractName();
        String version = req.getVersion();
        List<AbiDefinition> abiInfos = req.getAbiInfo();
        String bytecodeBin = req.getBytecodeBin();
        List<Object> params = req.getFuncParam();

        // Check if contractAbi existed
        checkContractAbiExistedAndSave(contractName, version, abiInfos);
        String encodedConstructor = constructorEncoded(contractName, version, params);

        // get privateKey
        Credentials credentials = getCredentials(userId);

        // contract deploy
        String contractAddress = deployContract(bytecodeBin, encodedConstructor, credentials);


//        // cns Params
//        List<Object> cnsParams = new ArrayList<>();
//        cnsParams.add(contractName + Constants.DIAGONAL + version);
//        cnsParams.add(contractName);
//        cnsParams.add(version);
//        cnsParams.add(JSON.toJSONString(abiInfos));
//        cnsParams.add(contractAddress);

        String result = cnsService.registerCns(contractName, version, contractAddress, JSON.toJSONString(abiInfos));

        // trans Params
//        ReqTransHandle reqTransHandle = new ReqTransHandle();
//        reqTransHandle.setUserId(userId);
//        reqTransHandle.setContractName(Constants.CNS_CONTRAC_TNAME);
//        reqTransHandle.setVersion("");
//        reqTransHandle.setFuncName(Constants.CNS_FUNCTION_ADDABI);
//        reqTransHandle.setFuncParam(cnsParams);

        // cns add
//        BaseResponse baseRsp = transService.transRequest(reqTransHandle);

        // result
        BaseResponse baseRsp = new BaseResponse(ConstantCode.RET_SUCCEED);
        baseRsp.setData(contractAddress);
        log.info("contract deploy end. baseRsp:{}", JSON.toJSONString(baseRsp));
        return baseRsp;
    }

    public static String constructorEncoded(String contractName, String version, List<Object> params) throws FrontException {
        // Constructor encoded
        String encodedConstructor = "";
        String functionName = contractName;
        // input handle
        List<String> funcInputTypes = ContractAbiUtil.getFuncInputType(contractName, functionName, version);
        if (funcInputTypes != null && funcInputTypes.size() > 0) {
            if (funcInputTypes.size() == params.size()) {
                List<Type> finalInputs = TransService.inputFormat(funcInputTypes, params);
                encodedConstructor = FunctionEncoder.encodeConstructor(finalInputs);
                log.info("deploy encodedConstructor:{}", encodedConstructor);
            } else {
                log.warn("deploy fail. funcInputTypes:{}, params:{}", funcInputTypes, params);
                throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
            }
        }
        return encodedConstructor;
    }

    private Credentials getCredentials(int userId) throws FrontException {
        String privateKey = Optional.ofNullable(transService.getPrivateKey(userId))
                .map(info -> info.getPrivateKey()).orElse(null);
        if (privateKey == null) {
            log.error("userId:{} privateKey is null.", userId);
            throw new FrontException(ConstantCode.PRIVATEKEY_IS_NULL);
        }
        return Credentials.create(privateKey);
    }

    private void checkContractAbiExistedAndSave(String contractName, String version, List<AbiDefinition> abiInfos) throws FrontException {
        boolean ifExisted = ContractAbiUtil.ifContractAbiExisted(contractName, version);
        if (!ifExisted) {

//            List<AbiDefinition> ilist = new ArrayList<>();
//            for (Object o : abiInfos) {
//                ilist.add((AbiDefinition) o);
//            }
            //  JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(abiInfos));
            ContractAbiUtil.addAbiToCacheMapAndSaveToFile(contractName, version, abiInfos, true);
        }
    }

    private String deployContract(String bytecodeBin, String encodedConstructor, Credentials credentials) throws FrontException {
        CommonContract commonContract = null;
        try {
            commonContract = CommonContract.deploy(web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT,
                    Constants.INITIAL_WEI_VALUE, bytecodeBin, encodedConstructor).send();
        } catch (Exception e) {
            log.error("commonContract deploy failed.");
            throw new FrontException(ConstantCode.CONTRACT_DEPLOY_ERROR);
        }
        log.info("commonContract deploy success. contractAddress:{}", commonContract.getContractAddress());
        return commonContract.getContractAddress();

    }

    /**
     * deleteAbi.
     *
     * @param contractName not null
     * @param version      not null
     * @return
     */
    public BaseResponse deleteAbi(String contractName, String version) throws FrontException {
        BaseResponse baseRsp = new BaseResponse(ConstantCode.RET_SUCCEED);
        boolean result = CommonUtils.deleteFile(Constants.ABI_DIR + Constants.DIAGONAL + contractName + Constants.SEP + version);
        if (!result) {
            log.warn("deleteAbi fail. contractname:{} ,version:{}", contractName, version);
            throw new FrontException(ConstantCode.FILE_IS_NOT_EXIST);
        }
        return baseRsp;
    }

    public static FileContent compileToJavaFile(String contractName, List<AbiDefinition> abiInfo, String binaryCode, String packageName) throws IOException {

        File abiFile = new File(Constants.ABI_DIR + Constants.DIAGONAL + contractName + ".abi");
        FileUtils.writeStringToFile(abiFile, JSON.toJSONString(abiInfo));
        File binFile = new File(Constants.BIN_DIR + Constants.DIAGONAL + contractName + ".bin");
        FileUtils.writeStringToFile(binFile, JSON.toJSONString(binaryCode));

        SolidityFunctionWrapperGenerator.main(
                Arrays.asList(
                        "-a", abiFile.getPath(),
                        "-b", binFile.getPath(),
                        "-p", packageName,
                        "-o", Constants.JAVA_DIR)
                        .toArray(new String[0]));

        String outputDirectory="" ;
        if (!packageName.isEmpty()) {
              outputDirectory= packageName.replace(".",File.separator);
        }
        File file = new File(Constants.JAVA_DIR+ File.separator+ outputDirectory+ File.separator+ contractName+".java");
        InputStream targetStream = new FileInputStream(file);
        return new FileContent(contractName+".java",targetStream);
    }


    public void saveContract(Contract contract) {
        contractRepository.save(contract);
    }
}
