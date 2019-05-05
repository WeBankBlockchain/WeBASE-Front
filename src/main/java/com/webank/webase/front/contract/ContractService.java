package com.webank.webase.front.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.file.FileContent;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.JsonRpc2_0Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 
 */
@Slf4j
@Service
public class ContractService {
//    @Autowired
//    Web3j web3j;
    @Autowired
    Map<Integer,Web3j> web3jMap;
    @Autowired
    TransService transService;
    @Autowired
    HashMap<Integer, CnsService> cnsServiceMap;
    @Autowired
    ContractRepository contractRepository;
    /**
     * sendAbi.
     * 
     * @param req request data
     * @return
     */
    public BaseResponse sendAbi(ReqSendAbi req) throws FrontException {
        BaseResponse baseRsp = new BaseResponse(ConstantCode.RET_SUCCEED);

        String contractName = req.getContractName();
        String version = req.getVersion();
        List<AbiDefinition> abiInfos = req.getAbiInfo();

        // Check if it has been deployed based on the contract name and version number
        checkContractAbiExistedAndSave(contractName, version, abiInfos);

        log.info("sendAbi end. contractname:{} ,version:{}", contractName, version);
        return baseRsp;
    }

    /**
     * contract deploy.
     * 
     * @param req request data
     * @return
     */
    public String deploy(ReqDeploy req) throws Exception {

        int userId = req.getUserId();
        String contractName = req.getContractName();
        String version = req.getVersion();
        List<AbiDefinition> abiInfos = req.getAbiInfo();
        String bytecodeBin = req.getBytecodeBin();
        List<Object> params = req.getFuncParam();
        int groupId = req.getGroupId();
        // Check if contractAbi existed
        ContractAbiUtil.setContractWithAbi(contractName, version,abiInfos,false);
        String encodedConstructor = constructorEncoded(contractName, version, params);

        // get privateKey
        Credentials credentials =  transService.getCredentials(userId);

        // contract deploy
        String contractAddress = deployContract(groupId, bytecodeBin, encodedConstructor, credentials);

        checkContractAbiExistedAndSave(contractName, version, abiInfos);

        cnsServiceMap.get(groupId).registerCns(contractName ,version, contractAddress,JSON.toJSONString(abiInfos));

        return contractAddress;
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


    private void checkContractAbiExistedAndSave(String contractName, String version, List<AbiDefinition> abiInfos) throws FrontException {
        boolean ifExisted = ContractAbiUtil.ifContractAbiExisted(contractName, version);
        if (!ifExisted) {

            ContractAbiUtil.setContractWithAbi(contractName, version, abiInfos, true);
        }
    }

    private String deployContract(int groupId, String bytecodeBin, String encodedConstructor, Credentials credentials) throws FrontException {
        CommonContract commonContract = null;
        try {
            Web3j web3j = web3jMap.get(groupId);
            commonContract = CommonContract.deploy(web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT,
                                    Constants.INITIAL_WEI_VALUE, bytecodeBin, encodedConstructor).send();
        } catch (Exception e) {
            log.error("commonContract deploy failed.",e);
            throw new FrontException(ConstantCode.CONTRACT_DEPLOY_ERROR);
        }
        log.info("commonContract deploy success. contractAddress:{}", commonContract.getContractAddress());
        return commonContract.getContractAddress();

    }

    /**
     * deleteAbi.
     * 
     * @param contractName not null
     * @param version not null
     * @return
     */
    public BaseResponse deleteAbi(String contractName, String version) throws FrontException {
        BaseResponse baseRsp = new BaseResponse(ConstantCode.RET_SUCCEED);
        boolean result = CommonUtils.deleteFile(
                Constants.ABI_DIR + Constants.DIAGONAL + contractName + Constants.SEP + version);
        if (!result) {
            log.warn("deleteAbi fail. contractname:{} ,version:{}", contractName, version);
            throw new FrontException(ConstantCode.FILE_IS_NOT_EXIST);
        }
        return baseRsp;
    }

    public String getAddressByContractNameAndVersion(int groupId, String name, String version) {
        return cnsServiceMap.get(groupId).getAddressByContractNameAndVersion(name+":"+version);
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


    public List<Contract> findByCriteria(int groupId, String contractName, String version, String address) {
        if (address != null) {
            return contractRepository.findByGroupIdAndContractAddress(groupId,address);
        }
        return contractRepository.findByGroupIdAndContractNameAndVersion(groupId,contractName, version);
    }
}
