package com.webank.webase.front.contract;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.FrontUtils;
import com.webank.webase.front.base.enums.ContractStatus;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.contract.entity.ReqContractSave;
import com.webank.webase.front.contract.entity.ReqPageContract;
import com.webank.webase.front.contract.entity.ReqDeploy;
import com.webank.webase.front.contract.entity.ReqSendAbi;
import com.webank.webase.front.file.FileContent;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
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
    private Map<Integer, Web3j> web3jMap;
    @Autowired
    private HashMap<Integer, CnsService> cnsServiceMap;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private KeyStoreService keyStoreService;

    /**
     * sendAbi.
     *
     * @param req request data
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
     * case deploy type.
     */
    public String caseDeploy(ReqDeploy req) throws Exception {
        if (Objects.nonNull(req.getContractId())) {
            return deployLocalContract(req);
        } else {
            return deploy(req);
        }
    }

    private String deployLocalContract(ReqDeploy req) throws Exception {
        //check contractId
        verifyContractIdExist(req.getContractId(), req.getGroupId());
        req.setVersion(Instant.now().toEpochMilli() + "");
        Contract contract = verifyContractNotDeploy(req.getContractId(), req.getGroupId());

        //deploy
        String contractAddress = deploy(req);
        if (StringUtils.isNotBlank(contractAddress)) {
            //save address
            BeanUtils.copyProperties(req, contract);
            contract.setContractAddress(contractAddress);
            contract.setContractStatus(ContractStatus.DEPLOYED.getValue());
            contract.setModifyTime(LocalDateTime.now());
            contract.setDeployTime(LocalDateTime.now());
            contractRepository.save(contract);
        }
        return contractAddress;
    }

    /**
     * contract deploy.
     */
    public String deploy(ReqDeploy req) throws Exception {
        String contractName = req.getContractName();
        String version = req.getVersion();
        List<AbiDefinition> abiInfos = req.getAbiInfo();
        String bytecodeBin = req.getBytecodeBin();
        List<Object> params = req.getFuncParam();
        int groupId = req.getGroupId();
        // Check if contractAbi existed
        ContractAbiUtil.setContractWithAbi(contractName, version, abiInfos, false);
        String encodedConstructor = constructorEncoded(contractName, version, params);

        // get privateKey
        Credentials credentials = keyStoreService.getCredentials(req.getUser());
        // contract deploy
        String contractAddress = deployContract(groupId, bytecodeBin, encodedConstructor,
            credentials);

        checkContractAbiExistedAndSave(contractName, version, abiInfos);

        cnsServiceMap.get(groupId)
            .registerCns(contractName, version, contractAddress, JSON.toJSONString(abiInfos));

        log.info("success deploy. contractAddress:{}", contractAddress);
        return contractAddress;
    }


    public static String constructorEncoded(String contractName, String version,
        List<Object> params) throws FrontException {
        // Constructor encoded
        String encodedConstructor = "";
        String functionName = contractName;
        // input handle
        List<String> funcInputTypes = ContractAbiUtil
            .getFuncInputType(contractName, functionName, version);
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


    private void checkContractAbiExistedAndSave(String contractName, String version,
        List<AbiDefinition> abiInfos) throws FrontException {
        boolean ifExisted = ContractAbiUtil.ifContractAbiExisted(contractName, version);
        if (!ifExisted) {

            ContractAbiUtil.setContractWithAbi(contractName, version, abiInfos, true);
        }
    }

    private String deployContract(int groupId, String bytecodeBin, String encodedConstructor,
        Credentials credentials) throws FrontException {
        CommonContract commonContract = null;
        try {
            Web3j web3j = web3jMap.get(groupId);
            commonContract = CommonContract
                .deploy(web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT,
                    Constants.INITIAL_WEI_VALUE, bytecodeBin, encodedConstructor).send();
        } catch (Exception e) {
            log.error("commonContract deploy failed.", e);
            throw new FrontException(ConstantCode.CONTRACT_DEPLOY_ERROR);
        }
        log.info("commonContract deploy success. contractAddress:{}",
            commonContract.getContractAddress());
        return commonContract.getContractAddress();

    }

    /**
     * deleteAbi.
     *
     * @param contractName not null
     * @param version not null
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
        return cnsServiceMap.get(groupId).getAddressByContractNameAndVersion(name + ":" + version);
    }

    public static FileContent compileToJavaFile(String contractName, List<AbiDefinition> abiInfo,
        String binaryCode, String packageName) throws IOException {

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

        String outputDirectory = "";
        if (!packageName.isEmpty()) {
            outputDirectory = packageName.replace(".", File.separator);
        }
        File file = new File(
            Constants.JAVA_DIR + File.separator + outputDirectory + File.separator + contractName
                + ".java");
        InputStream targetStream = new FileInputStream(file);
        return new FileContent(contractName + ".java", targetStream);
    }


    /**
     * delete contract by contractId.
     */
    public void deleteContract(Long contractId, int groupId) {
        log.debug("start deleteContract contractId:{} groupId:{}", contractId, groupId);
        // check contract id
        verifyContractNotDeploy(contractId, groupId);
        //remove
        contractRepository.delete(contractId);
        log.debug("end deleteContract");
    }

    /**
     * save contract data.
     */
    public Contract saveContract(ReqContractSave contractReq) {
        log.debug("start saveContract contractReq:{}", JSON.toJSONString(contractReq));
        if (contractReq.getContractId() == null) {
            return newContract(contractReq);//new
        } else {
            return updateContract(contractReq);//update
        }
    }


    /**
     * save new contract.
     */
    private Contract newContract(ReqContractSave contractReq) {
        //check contract not exist.
        verifyContractNotExist(contractReq.getGroupId(), contractReq.getContractPath(),
            contractReq.getContractName());

        //add to database.
        Contract contract = new Contract();
        BeanUtils.copyProperties(contractReq, contract);
        contract.setContractStatus(ContractStatus.NOTDEPLOYED.getValue());
        contract.setModifyTime(LocalDateTime.now());
        contract.setCreateTime(LocalDateTime.now());
        contractRepository.save(contract);
        return contract;
    }


    /**
     * update contract.
     */
    private Contract updateContract(ReqContractSave contractReq) {
        //check not deploy
        Contract contract = verifyContractNotDeploy(contractReq.getContractId(),
            contractReq.getGroupId());
        //check contractName
        verifyContractNameNotExist(contractReq.getGroupId(), contractReq.getContractPath(),
            contractReq.getContractName(), contractReq.getContractId());
        BeanUtils.copyProperties(contractReq, contract);
        contract.setModifyTime(LocalDateTime.now());
        contractRepository.save(contract);
        return contract;
    }

    /**
     * find contract by page.
     */
    public Page<Contract> findContractByPage(ReqPageContract param) {
        Pageable pageable = new PageRequest(param.getPageNumber(), param.getPageSize(),
            Direction.DESC, "modifyTime");
        Page<Contract> contractPage = contractRepository.findAll(
            (Root<Contract> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
                Predicate predicate = FrontUtils.buildPredicate(root, criteriaBuilder, param);
                query.where(predicate);
                return query.getRestriction();
            }, pageable);
        return contractPage;

    }


    /**
     * verify contract not exist.
     */
    private void verifyContractNotExist(int groupId, String path, String name) {
        Contract contract = contractRepository
            .findByGroupIdAndContractPathAndContractName(groupId, path, name);
        if (Objects.nonNull(contract)) {
            log.warn("contract is exist. groupId:{} name:{} path:{}", groupId, name, path);
            throw new FrontException(ConstantCode.CONTRACT_EXISTS);
        }
    }

    /**
     * verify that the contract had not deployed.
     */
    private Contract verifyContractNotDeploy(Long contractId, int groupId) {
        Contract contract = verifyContractIdExist(contractId, groupId);
        if (ContractStatus.DEPLOYED.getValue() == contract.getContractStatus()) {
            log.info("contract had bean deployed contract", contractId);
            throw new FrontException(ConstantCode.CONTRACT_HAS_BEAN_DEPLOYED);
        }
        return contract;
    }

    /**
     * verify that the contractId is exist.
     */
    private Contract verifyContractIdExist(Long contractId, int groupId) {
        Contract contract = contractRepository.findByGroupIdAndId(groupId, contractId);
        if (Objects.isNull(contract)) {
            log.info("contractId is invalid. contractId:{}", contractId);
            throw new FrontException(ConstantCode.INVALID_CONTRACT_ID);
        }
        return contract;
    }

    /**
     * contract name can not be repeated.
     */
    private void verifyContractNameNotExist(int groupId, String path, String name,
        Long contractId) {
        Contract localContract = contractRepository
            .findByGroupIdAndContractPathAndContractName(groupId, path, name);
        if (Objects.isNull(localContract)) {
            return;
        }
        if (contractId != localContract.getId()) {
            throw new FrontException(ConstantCode.CONTRACT_NAME_REPEAT);
        }
    }
}
