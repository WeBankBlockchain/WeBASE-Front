/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.contract;

import static com.webank.webase.front.base.ConstantCode.GROUPID_NOT_EXIST;
import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.*;
import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.METADATA;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.webank.webase.front.contract.entity.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.solidity.compiler.CompilationResult;
import org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.FrontUtils;
import com.webank.webase.front.base.enums.ContractStatus;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.file.FileContent;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.AbiUtil;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;
import com.webank.webase.front.web3api.Web3ApiService;
import lombok.extern.slf4j.Slf4j;

/**
 * contract management.
 */
@Slf4j
@Service
public class ContractService {
    private static final String BASE_FILE_PATH = File.separator + "temp" + File.separator;
    private static final String CONTRACT_FILE_TEMP = BASE_FILE_PATH + "%1s.sol";

    @Autowired
    private Map<Integer, Web3j> web3jMap;
    @Autowired
    private Map<String, String> cnsMap;
    @Autowired
    private HashMap<Integer, CnsService> cnsServiceMap;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private TransService transService;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private Constants constants;

    /**
     * sendAbi.
     *
     * @param req request data
     */
    public void sendAbi(ReqSendAbi req) {

        String contractName = req.getContractName();
        String address = req.getAddress();
        List<AbiDefinition> abiInfos = req.getAbiInfo();

        // check address is valid
        addressIsValid(req.getGroupId(), req.getAddress(), req.getContractBin());
        // Check if it has been deployed based on the contract name and version number
        checkContractAbiExistedAndSave(contractName, address.substring(2), abiInfos);
        try {
            cnsServiceMap.get(req.getGroupId()).registerCns(contractName, address.substring(2),
                    address, JSON.toJSONString(abiInfos));
        } catch (Exception ex) {
            log.error("fail sendAbi.", ex);
            throw new FrontException(ConstantCode.SEND_ABI_INFO_FAIL);
        }

        log.info("sendAbi end. contractname:{} ,version:{}", contractName, address);
    }

    /**
     * check address is valid.
     */
    private void addressIsValid(int groupId, String contractAddress, String contractBin) {
        if (StringUtils.isBlank(contractAddress)) {
            log.error("fail addressIsValid. contractAddress is empty");
            throw new FrontException(ConstantCode.CONTRACT_ADDRESS_NULL);
        }
        if (StringUtils.isBlank(contractBin)) {
            log.error("fail addressIsValid. contractBin is empty");
            throw new FrontException(ConstantCode.CONTRACT_BIN_NULL);
        }
        String binOnChain;
        try {
            binOnChain = web3ApiService.getCode(groupId, contractAddress, BigInteger.ZERO);
        } catch (Exception e) {
            log.error("fail addressIsValid.", e);
            throw new FrontException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
        log.debug("addressIsValid address:{} binOnChain:{}", contractAddress, binOnChain);
        if (StringUtils.isBlank(binOnChain)) {
            log.error("fail addressIsValid. binOnChain is null, address:{}", contractAddress);
            throw new FrontException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }

        String subChainAddress = FrontUtils.removeBinFirstAndLast(binOnChain, 68);
        String subInputBin = FrontUtils.removeFirstStr(contractBin, "0x");
        log.info("address:{} subBinOnChain:{} subInputBin:{}", contractAddress, subChainAddress,
                subInputBin);
        if (!subInputBin.contains(subChainAddress)) {
            log.error("fail addressIsValid contractAddress:{}", contractAddress);
            throw new FrontException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
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
        // check contract status
        Contract contract = verifyContractIdExist(req.getContractId(), req.getGroupId());

        // deploy
        String contractAddress = deploy(req);
        if (StringUtils.isNotBlank(contractAddress)) {
            // save address
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

        ContractAbiUtil.VersionEvent versionEvent =
                ContractAbiUtil.getVersionEventFromAbi(contractName, abiInfos);
        String encodedConstructor = constructorEncoded(contractName, versionEvent, params);

        // get privateKey
        Credentials credentials = keyStoreService.getCredentials(req.getUser(), req.getUseAes());
        // contract deploy
        String contractAddress =
                deployContract(groupId, bytecodeBin, encodedConstructor, credentials);


        if (version != null) {
            checkContractAbiExistedAndSave(contractName, version, abiInfos);
            cnsServiceMap.get(groupId).registerCns(contractName, version, contractAddress,
                    JSON.toJSONString(abiInfos));
            cnsMap.put(contractName + ":" + version, contractAddress);
        } else {
            checkContractAbiExistedAndSave(contractName, contractAddress.substring(2), abiInfos);
            cnsServiceMap.get(groupId).registerCns(contractName, contractAddress.substring(2),
                    contractAddress, JSON.toJSONString(abiInfos));
            cnsMap.put(contractName + ":" + contractAddress.substring(2), contractAddress);
        }
        log.info("success deploy. contractAddress:{}", contractAddress);
        return contractAddress;
    }

    /**
     * contract deploy.
     */
    public String deployWithSign(ReqDeployWithSign req) throws Exception {
        int groupId = req.getGroupId();
        String contractAbi = JSON.toJSONString(req.getContractAbi());
        String contractBin = req.getContractBin();
        List<Object> params = req.getFuncParam();

        // check groupId
        Web3j web3j = web3jMap.get(groupId);
        if (web3j == null) {
            new FrontException(GROUPID_NOT_EXIST);
        }

        // check parameters
        AbiDefinition abiDefinition = AbiUtil.getAbiDefinition(contractAbi);
        List<String> funcInputTypes = AbiUtil.getFuncInputType(abiDefinition);
        if (funcInputTypes.size() != params.size()) {
            log.warn("deployWithSign fail. funcInputTypes:{}, params:{}", funcInputTypes, params);
            throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
        }

        // Constructor encode
        String encodedConstructor = "";
        if (funcInputTypes.size() > 0) {
            List<Type> finalInputs = AbiUtil.inputFormat(funcInputTypes, params);
            encodedConstructor = FunctionEncoder.encodeConstructor(finalInputs);
        }

        // data sign
        String data = contractBin + encodedConstructor;
        String signMsg = transService.signMessage(groupId, web3j, req.getSignUserId(), "", data);
        if (StringUtils.isBlank(signMsg)) {
            throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
        }
        // send transaction
        final CompletableFuture<TransactionReceipt> transFuture = new CompletableFuture<>();
        transService.sendMessage(web3j, signMsg, transFuture);
        TransactionReceipt receipt = transFuture.get(constants.getTransMaxWait(), TimeUnit.SECONDS);
        String contractAddress = receipt.getContractAddress();

        log.info("success deploy. contractAddress:{}", contractAddress);
        return contractAddress;
    }


    public static String constructorEncodedByContractNameAndVersion(String contractName,
            String version, List<Object> params) throws FrontException {
        // Constructor encoded
        String encodedConstructor = "";
        String functionName = contractName;
        // input handle
        List<String> funcInputTypes =
                ContractAbiUtil.getFuncInputType(contractName, functionName, version);
        if (funcInputTypes != null && funcInputTypes.size() > 0) {
            if (funcInputTypes.size() == params.size()) {
                List<Type> finalInputs = AbiUtil.inputFormat(funcInputTypes, params);
                encodedConstructor = FunctionEncoder.encodeConstructor(finalInputs);
                log.info("deploy encodedConstructor:{}", encodedConstructor);
            } else {
                log.warn("deploy fail. funcInputTypes:{}, params:{}", funcInputTypes, params);
                throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
            }
        }
        return encodedConstructor;
    }

    public static String constructorEncoded(String contractName,
            ContractAbiUtil.VersionEvent versionEvent, List<Object> params) throws FrontException {
        // Constructor encoded
        String encodedConstructor = "";
        String functionName = contractName;
        // input handle
        List<String> funcInputTypes = versionEvent.getFuncInputs().get(functionName);

        if (funcInputTypes != null && funcInputTypes.size() > 0) {
            if (funcInputTypes.size() == params.size()) {
                List<Type> finalInputs = AbiUtil.inputFormat(funcInputTypes, params);
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
        Web3j web3j = web3jMap.get(groupId);
        if (web3j == null) {
            new FrontException(ConstantCode.GROUPID_NOT_EXIST);
        }
        try {
            commonContract =
                    CommonContract
                            .deploy(web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT,
                                    Constants.INITIAL_WEI_VALUE, bytecodeBin, encodedConstructor)
                            .send();
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
            String contractBin, String packageName) throws IOException {

        File abiFile = new File(Constants.ABI_DIR + Constants.DIAGONAL + contractName + ".abi");
        FrontUtils.createFileIfNotExist(abiFile, true);
        FileUtils.writeStringToFile(abiFile, JSON.toJSONString(abiInfo));
        File binFile = new File(Constants.BIN_DIR + Constants.DIAGONAL + contractName + ".bin");
        FrontUtils.createFileIfNotExist(binFile, true);
        FileUtils.writeStringToFile(binFile, contractBin);

        SolidityFunctionWrapperGenerator
                .main(Arrays.asList("-a", abiFile.getPath(), "-b", binFile.getPath(), "-p",
                        packageName, "-o", Constants.JAVA_DIR).toArray(new String[0]));

        String outputDirectory = "";
        if (!packageName.isEmpty()) {
            outputDirectory = packageName.replace(".", File.separator);
        }
        if (contractName.length() > 1) {
            contractName = contractName.substring(0, 1).toUpperCase() + contractName.substring(1);
        }
        File file = new File(Constants.JAVA_DIR + File.separator + outputDirectory + File.separator
                + contractName + ".java");
        FrontUtils.createFileIfNotExist(file, true);
        InputStream targetStream = new FileInputStream(file);
        return new FileContent(contractName + ".java", targetStream);
    }


    /**
     * delete contract by contractId.
     */
    public void deleteContract(Long contractId, int groupId) {
        log.debug("start deleteContract contractId:{} groupId:{}", contractId, groupId);
        // check contract id
        verifyContractIdExist(contractId, groupId);
        // remove
        contractRepository.delete(contractId);
        log.debug("end deleteContract");
    }

    /**
     * save contract data.
     */
    public Contract saveContract(ReqContractSave contractReq) {
        log.debug("start saveContract contractReq:{}", JSON.toJSONString(contractReq));
        if (contractReq.getContractId() == null) {
            return newContract(contractReq);// new
        } else {
            return updateContract(contractReq);// update
        }
    }


    /**
     * save new contract.
     */
    private Contract newContract(ReqContractSave contractReq) {
        // check contract not exist.
        verifyContractNotExist(contractReq.getGroupId(), contractReq.getContractPath(),
                contractReq.getContractName());

        // add to database.
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
        // check contract exist
        Contract contract =
                verifyContractIdExist(contractReq.getContractId(), contractReq.getGroupId());
        // check contractName
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
        Contract contract =
                contractRepository.findByGroupIdAndContractPathAndContractName(groupId, path, name);
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
     * verify that if the contract changed.
     */
    public boolean verifyContractChange(Long contractId, int groupId) {
        Contract contract = contractRepository.findByGroupIdAndId(groupId, contractId);
        log.debug("verifyContractChange contract:{}", contract);
        if (!Objects.isNull(contract) && contract.getContractStatus().intValue() == 2
                && !contract.getDeployTime().isEqual(contract.getModifyTime())) {
            return true;
        }
        return false;
    }

    /**
     * contract name can not be repeated.
     */
    private void verifyContractNameNotExist(int groupId, String path, String name,
            Long contractId) {
        Contract localContract =
                contractRepository.findByGroupIdAndContractPathAndContractName(groupId, path, name);
        if (Objects.isNull(localContract)) {
            return;
        }
        if (Objects.isNull(contractId)) {
            log.warn("fail verifyContractNameNotExist. contractId is null");
            throw new FrontException(ConstantCode.INVALID_CONTRACT_ID);
        }
        Long localId = localContract.getId();
        if (contractId.longValue() != localId.longValue()) {
            log.info("contract name repeat. groupId:{} path:{} name:{} contractId:{} localId:{}",
                    groupId, path, name, contractId, localId);
            throw new FrontException(ConstantCode.CONTRACT_NAME_REPEAT);
        }
    }



    /**
     * compile contract.
     */
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

            // compile result
            CompilationResult result = CompilationResult.parse(res.output);
            CompilationResult.ContractMetadata meta = result.getContract(contractName);
            RspContractCompile compileResult = new RspContractCompile(contractName, meta.abi, meta.bin);
            return compileResult;
        } catch (Exception ex) {
            log.error("contractCompile error", ex);
            throw new FrontException(ConstantCode.CONTRACT_COMPILE_FAIL.getCode(), ex.getMessage());
        } finally {
            if (contractFile != null) {
                contractFile.deleteOnExit();
            }
        }

    }
}
