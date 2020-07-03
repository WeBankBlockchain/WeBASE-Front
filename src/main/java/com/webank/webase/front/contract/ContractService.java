/*
 * Copyright 2014-2020 the original author or authors.
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

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.MySecurityManagerConfig;
import com.webank.webase.front.base.enums.ContractStatus;
import com.webank.webase.front.base.enums.GMStatus;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.contract.entity.ContractPath;
import com.webank.webase.front.contract.entity.ContractPathKey;
import com.webank.webase.front.contract.entity.FileContentHandle;
import com.webank.webase.front.contract.entity.ReqContractPath;
import com.webank.webase.front.contract.entity.ReqContractSave;
import com.webank.webase.front.contract.entity.ReqDeploy;
import com.webank.webase.front.contract.entity.ReqMultiContractCompile;
import com.webank.webase.front.contract.entity.ReqPageContract;
import com.webank.webase.front.contract.entity.ReqSendAbi;
import com.webank.webase.front.contract.entity.RspContractCompile;
import com.webank.webase.front.contract.entity.RspMultiContractCompile;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.AbiUtil;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;
import com.webank.webase.front.util.FrontUtils;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import com.webank.webase.front.precompiledapi.permission.PermissionManageService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.webank.webase.front.base.code.ConstantCode.GROUPID_NOT_EXIST;
import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.solc.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;
import static org.fisco.solc.compiler.SolidityCompiler.Options.INTERFACE;

/**
 * contract management.
 */
@Slf4j
@Service
public class ContractService {
    private static final String BASE_FILE_PATH = "./temp" + File.separator;
    private static final String CONTRACT_FILE_TEMP = BASE_FILE_PATH + "%1s.sol";

    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ContractPathRepository contractPathRepository;
    @Autowired
    private TransService transService;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private Constants constants;
    @Autowired
    private PermissionManageService permissionManageService;

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
            log.error("fail addressIsValid. bytecodeBin is empty");
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
     * case deploy type: deploy by abi or deploy by local contract
     *
     * @param: ReqDeploy'scontractId
     * @param: doLocally deploy contract locally or through webase-sign
     */
    public String caseDeploy(ReqDeploy req, boolean doLocally) throws Exception {
        if (Objects.nonNull(req.getContractId())) {
            return deployByLocalContract(req, doLocally);
        } else {
            if (doLocally) {
                return deployLocally(req);
            } else {
                return deployWithSign(req);
            }
        }
    }

    /**
     * check contract exists before deploy
     * 
     * @param req
     * @param doLocally deploy contract locally or through webase-sign
     */
    private String deployByLocalContract(ReqDeploy req, boolean doLocally) throws Exception {
        // check contract status
        Contract contract = verifyContractIdExist(req.getContractId(), req.getGroupId());

        // deploy
        String contractAddress;
        // deploy locally or webase-sign
        if (doLocally) {
            // check deploy permission
            checkDeployPermission(req.getGroupId(), req.getUser());
            contractAddress = deployLocally(req);
        } else {
            // check deploy permission
            String userAddress = keyStoreService.getAddressBySignUserId(req.getSignUserId());
            if (StringUtils.isNotBlank(userAddress)) {
                checkDeployPermission(req.getGroupId(), userAddress);
            }
            contractAddress = deployWithSign(req);
        }
        if (StringUtils.isNotBlank(contractAddress)) {
            // save address
            BeanUtils.copyProperties(req, contract);
            contract.setContractAddress(contractAddress);
            contract.setContractStatus(ContractStatus.DEPLOYED.getValue());
            contract.setDeployTime(LocalDateTime.now());
            contract.setModifyTime(contract.getDeployTime());
            contractRepository.save(contract);
        }
        return contractAddress;
    }

    /**
     * deploy through webase-sign
     */
    public String deployWithSign(ReqDeploy req) throws Exception {
        int groupId = req.getGroupId();
        String signUserId = req.getSignUserId();
        List<AbiDefinition> abiInfos = req.getAbiInfo();
        String bytecodeBin = req.getBytecodeBin();
        List<Object> params = req.getFuncParam();

        // check groupId
        Web3j web3j = web3ApiService.getWeb3j(groupId);

        if (web3j == null) {
            throw new FrontException(GROUPID_NOT_EXIST);
        }

        // check deploy permission
        String userAddress = keyStoreService.getAddressBySignUserId(req.getSignUserId());
        if (StringUtils.isNotBlank(userAddress)) {
            checkDeployPermission(req.getGroupId(), userAddress);
        }

        String contractName = req.getContractName();
        ContractAbiUtil.VersionEvent versionEvent =
                ContractAbiUtil.getVersionEventFromAbi(contractName, abiInfos);
        String encodedConstructor = constructorEncoded(contractName, versionEvent, params);


        // data sign
        String data = bytecodeBin + encodedConstructor;
        String signMsg = transService.signMessage(groupId, web3j, signUserId, "", data);
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

    /**
     * deploy locally, not through webase-sign
     */
    public String deployLocally(ReqDeploy req) throws Exception {
        int groupId = req.getGroupId();
        String userAddress = req.getUser();
        // check deploy permission
        checkDeployPermission(groupId, userAddress);
        String contractName = req.getContractName();
        List<AbiDefinition> abiInfos = req.getAbiInfo();
        String bytecodeBin = req.getBytecodeBin();
        List<Object> params = req.getFuncParam();

        ContractAbiUtil.VersionEvent versionEvent =
                ContractAbiUtil.getVersionEventFromAbi(contractName, abiInfos);
        String encodedConstructor = constructorEncoded(contractName, versionEvent, params);

        // get privateKey
        Credentials credentials = keyStoreService.getCredentials(userAddress);
        // contract deploy
        String contractAddress =
                deployContract(groupId, bytecodeBin, encodedConstructor, credentials);

        log.info("success deployLocally. contractAddress:{}", contractAddress);
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

    /**
     * encode constructor function
     */
    private static String constructorEncoded(String contractName,
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
        Web3j web3j = web3ApiService.getWeb3j(groupId);
        if (web3j == null) {
            throw new FrontException(ConstantCode.GROUPID_NOT_EXIST);
        }
        try {
            commonContract =
                    CommonContract
                            .deploy(web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT,
                                    Constants.INITIAL_WEI_VALUE, bytecodeBin, encodedConstructor)
                            .send();
        } catch (TransactionException e) {
            log.error("commonContract deploy failed.", e);
            throw new FrontException(ConstantCode.TRANSACTION_SEND_FAILED, e.getMessage());
        } catch (Exception e) {
            log.error("commonContract deploy failed.", e);
            throw new FrontException(ConstantCode.CONTRACT_DEPLOY_ERROR, e.getMessage());
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

    public static FileContentHandle compileToJavaFile(String contractName,
            List<AbiDefinition> abiInfo, String contractBin, String packageName)
            throws IOException {


        File abiFile = new File(Constants.ABI_DIR + Constants.DIAGONAL + contractName + ".abi");
        FrontUtils.createFileIfNotExist(abiFile, true);
        FileUtils.writeStringToFile(abiFile, JsonUtils.toJSONString(abiInfo));
        File binFile = new File(Constants.BIN_DIR + Constants.DIAGONAL + contractName + ".bin");
        FrontUtils.createFileIfNotExist(binFile, true);
        FileUtils.writeStringToFile(binFile, contractBin);

        generateJavaFile(packageName, abiFile, binFile);

        String outputDirectory = "";
        if (StringUtils.isNotBlank(packageName)) {
            outputDirectory = packageName.replace(".", File.separator);
        }
        if (contractName.length() > 1) {
            contractName = contractName.substring(0, 1).toUpperCase() + contractName.substring(1);
        }
        File file = new File(Constants.JAVA_DIR + File.separator + outputDirectory + File.separator
                + contractName + ".java");
        FrontUtils.createFileIfNotExist(file, true);
        InputStream targetStream = new FileInputStream(file);
        return new FileContentHandle(contractName + ".java", targetStream);
    }

    private static synchronized void generateJavaFile(String packageName, File abiFile, File binFile) {
        try {
            MySecurityManagerConfig.forbidSystemExitCall();
            SolidityFunctionWrapperGenerator
                    .main(Arrays.asList("-a", abiFile.getPath(), "-b", binFile.getPath(), "-p",
                            packageName, "-o", Constants.JAVA_DIR).toArray(new String[0]));
        } finally {
            MySecurityManagerConfig.enableSystemExitCall();
        }
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
        log.debug("start saveContract contractReq:{}", JsonUtils.toJSONString(contractReq));
        if (contractReq.getContractId() == null) {
            // new
            return newContract(contractReq);
        } else {
            // update
            return updateContract(contractReq);
        }
    }


    /**
     * save new contract.
     */
    @Transactional
    private Contract newContract(ReqContractSave contractReq) {
        // check contract not exist.
        verifyContractNotExist(contractReq.getGroupId(), contractReq.getContractPath(),
                contractReq.getContractName());

        // add to database.
        Contract contract = new Contract();
        BeanUtils.copyProperties(contractReq, contract);
        contract.setContractStatus(ContractStatus.NOTDEPLOYED.getValue());
        contract.setCreateTime(LocalDateTime.now());
        contract.setModifyTime(contract.getCreateTime());
        contractRepository.save(contract);
        // update time
        ContractPath contractPathVo = new ContractPath();
        contractPathVo.setGroupId(contractReq.getGroupId());
        contractPathVo.setContractPath(contractReq.getContractPath());
        contractPathVo.setModifyTime(LocalDateTime.now());
        contractPathRepository.save(contractPathVo);

        return contract;
    }


    /**
     * update contract.
     */
    @Transactional
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
        // update time
        ContractPath contractPathVo = new ContractPath();
        contractPathVo.setGroupId(contractReq.getGroupId());
        contractPathVo.setContractPath(contractReq.getContractPath());
        contractPathVo.setModifyTime(LocalDateTime.now());
        contractPathRepository.save(contractPathVo);

        return contract;
    }

    /**
     * find contract by page.
     */
    @Transactional
    public Page<Contract> findContractByPage(ReqPageContract param) throws IOException {
        // init templates
        List<String> templates = CommonUtils.readFileToList(Constants.TEMPLATE);
        String contractPath = "template";
        List<Contract> contracts =
                contractRepository.findByGroupIdAndContractPath(param.getGroupId(), contractPath);
        if ((contracts.isEmpty() && !Objects.isNull(templates)) || (!contracts.isEmpty()
                && !Objects.isNull(templates) && templates.size() != contracts.size())) {
            for (String template : templates) {
                Contract localContract =
                        contractRepository.findByGroupIdAndContractPathAndContractName(
                                param.getGroupId(), contractPath, template.split(",")[0]);
                if (Objects.isNull(localContract)) {
                    log.info("init template contract:{}", template.split(",")[0]);
                    Contract contract = new Contract();
                    contract.setGroupId(param.getGroupId());
                    contract.setContractName(template.split(",")[0]);
                    contract.setContractSource(template.split(",")[1]);
                    contract.setContractPath(contractPath);
                    contract.setContractStatus(ContractStatus.NOTDEPLOYED.getValue());
                    contract.setCreateTime(LocalDateTime.now());
                    contract.setModifyTime(contract.getCreateTime());
                    contractRepository.save(contract);
                }
            }
            ContractPath contractPathVo = new ContractPath();
            contractPathVo.setGroupId(param.getGroupId());
            contractPathVo.setContractPath(contractPath);
            contractPathVo.setCreateTime(LocalDateTime.now());
            contractPathVo.setModifyTime(contractPathVo.getCreateTime());
            contractPathRepository.save(contractPathVo);
        }
        // findContractByPage
       // page start from index 1 instead of 0
        int pageNumber = param.getPageNumber() - 1;
        Pageable pageable = new PageRequest(pageNumber, param.getPageSize(),
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
            // whether use guomi to compile
            boolean useSM2 = EncryptType.encryptType == GMStatus.GUOMI.getValue();
            // decode
            byte[] contractSourceByteArr = Base64.getDecoder().decode(sourceBase64);
            String contractFilePath = String.format(CONTRACT_FILE_TEMP, contractName);
            // save contract to file
            contractFile = new File(contractFilePath);
            FileUtils.writeByteArrayToFile(contractFile, contractSourceByteArr);
            // compile
            SolidityCompiler.Result res =
                    SolidityCompiler.compile(contractFile, useSM2, true, ABI, BIN, INTERFACE, METADATA);
            if ("".equals(res.getOutput())) {
                log.error("contractCompile error", res.getErrors());
                throw new FrontException(ConstantCode.CONTRACT_COMPILE_FAIL.getCode(), res.getErrors());
            }
            // compile result
            CompilationResult result = CompilationResult.parse(res.getOutput());
            CompilationResult.ContractMetadata meta = result.getContract(contractName);
            RspContractCompile compileResult =
                    new RspContractCompile(contractName, meta.abi, meta.bin, res.getErrors());
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

    public List<RspMultiContractCompile> multiContractCompile(ReqMultiContractCompile inputParam)
            throws IOException {
        // clear temp folder
        CommonUtils.deleteFiles(BASE_FILE_PATH);

        // unzip
        CommonUtils.zipBase64ToFile(inputParam.getContractZipBase64(), BASE_FILE_PATH);

        // get sol files
        File solFileList = new File(BASE_FILE_PATH);
        File[] solFiles = solFileList.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                if (!fileName.toLowerCase().endsWith(".sol")) {
                    return false;
                }
                return true;
            }
        });
        if (solFiles == null || solFiles.length == 0) {
            log.error("There is no sol files in source.");
            throw new FrontException(ConstantCode.NO_SOL_FILES);
        }
        // whether use guomi to compile
        boolean useSM2 = EncryptType.encryptType == GMStatus.GUOMI.getValue();

        List<RspMultiContractCompile> compileInfos = new ArrayList<>();
        for (File solFile : solFiles) {
            String contractName =
                    solFile.getName().substring(0, solFile.getName().lastIndexOf("."));
            // compile
            SolidityCompiler.Result res =
                    SolidityCompiler.compile(solFile, useSM2, true, ABI, SolidityCompiler.Options.BIN);
            // check result
            if (res.isFailed()) {
                log.error("multiContractCompile fail. contract:{} compile error. {}", contractName,
                        res.getErrors());
                throw new FrontException(ConstantCode.CONTRACT_COMPILE_FAIL.getCode(), res.getErrors());
            }
            // parse result
            CompilationResult result = CompilationResult.parse(res.getOutput());
            List<CompilationResult.ContractMetadata> contracts = result.getContracts();
            if (contracts.size() > 0) {
                RspMultiContractCompile compileInfo = new RspMultiContractCompile();
                compileInfo.setContractName(contractName);
                compileInfo.setBytecodeBin(result.getContract(contractName).bin);
                compileInfo.setContractAbi(result.getContract(contractName).abi);
                compileInfo.setContractSource(
                        CommonUtils.fileToBase64(BASE_FILE_PATH + solFile.getName()));
                compileInfos.add(compileInfo);
            }
        }

        log.debug("end multiContractCompile.");
        return compileInfos;
    }

    /**
     * addContractPath.
     */
    public ContractPath addContractPath(ReqContractPath req) {
        // add to database.
        ContractPath contractPath = new ContractPath();
        BeanUtils.copyProperties(req, contractPath);
        contractPath.setCreateTime(LocalDateTime.now());
        contractPath.setModifyTime(contractPath.getCreateTime());
        contractPathRepository.save(contractPath);
        return contractPath;
    }

    /**
     * addContractPath.
     */
    public List<ContractPath> findPathList(Integer groupId) {
        // get from database.
        Sort sort = new Sort(Sort.Direction.DESC, "modifyTime");
        List<ContractPath> contractPaths = contractPathRepository.findAll((Root<ContractPath> root,
                CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("groupId"), groupId);
            return criteriaBuilder.and(predicate);
        }, sort);
        return contractPaths;
    }

    /**
     * deletePath.
     */
    public void deletePath(Integer groupId, String contractPath) {
        ContractPathKey contractPathKey = new ContractPathKey();
        contractPathKey.setGroupId(groupId);
        contractPathKey.setContractPath(contractPath);
        contractPathRepository.delete(contractPathKey);
    }

    /**
     * check user deploy permission
     */
    private void checkDeployPermission(int groupId, String userAddress) {
        // get deploy permission list
        try {
            List<PermissionInfo> deployUserList = permissionManageService.listPermissionManager(groupId);
            // check user in the list,
            if (deployUserList.isEmpty()) {
                return;
            } else {
                long count = 0;
                count = deployUserList.stream().filter( admin -> admin.getAddress().equals(userAddress)).count();
                // if not in the list, permission denied
                if (count == 0) {
                    log.error("checkDeployPermission permission denied for user:{}", userAddress);
                    throw new FrontException(ConstantCode.PERMISSION_DENIED);
                }
            }
        } catch (Exception e) {
            log.error("checkDeployPermission get list error:{}", e.getMessage());
        }
    }

}
