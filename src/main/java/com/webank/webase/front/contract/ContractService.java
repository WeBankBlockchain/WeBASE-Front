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

import static com.webank.webase.front.base.code.ConstantCode.GROUPID_NOT_EXIST;
import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.solc.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.solc.compiler.SolidityCompiler.Options.INTERFACE;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.MySecurityManagerConfig;
import com.webank.webase.front.base.enums.ContractStatus;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.contract.entity.Cns;
import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.contract.entity.ContractPath;
import com.webank.webase.front.contract.entity.ContractPathKey;
import com.webank.webase.front.contract.entity.FileContentHandle;
import com.webank.webase.front.contract.entity.ReqContractPath;
import com.webank.webase.front.contract.entity.ReqContractSave;
import com.webank.webase.front.contract.entity.ReqCopyContracts;
import com.webank.webase.front.contract.entity.ReqDeploy;
import com.webank.webase.front.contract.entity.ReqListContract;
import com.webank.webase.front.contract.entity.ReqMultiContractCompile;
import com.webank.webase.front.contract.entity.ReqPageContract;
import com.webank.webase.front.contract.entity.ReqQueryCns;
import com.webank.webase.front.contract.entity.ReqRegisterCns;
import com.webank.webase.front.contract.entity.ReqSendAbi;
import com.webank.webase.front.contract.entity.RspContractCompile;
import com.webank.webase.front.contract.entity.RspContractNoAbi;
import com.webank.webase.front.contract.entity.RspMultiContractCompile;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precompiledapi.PrecompiledService;
import com.webank.webase.front.precompiledapi.PrecompiledWithSignService;
import com.webank.webase.front.precompiledapi.permission.PermissionManageService;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.AbiUtil;
import com.webank.webase.front.util.CleanPathUtil;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;
import com.webank.webase.front.util.ErrorCodeHandleUtils;
import com.webank.webase.front.util.FrontUtils;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.codegen.SolidityContractGenerator;
import org.fisco.bcos.sdk.codegen.exceptions.CodeGenException;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionInfo;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    private CnsRepository cnsRepository;
    @Autowired
    private TransService transService;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private PermissionManageService permissionManageService;
    @Autowired
    private PrecompiledWithSignService precompiledWithSignService;
    @Autowired
    private PrecompiledService precompiledService;
    @Autowired
    @Qualifier(value = "common")
    private CryptoSuite cryptoSuite;

    /**
     * sendAbi.
     *
     * @param req request data
     */
    public void sendAbi(ReqSendAbi req) {

        String contractName = req.getContractName();
        String address = req.getAddress();
        List<ABIDefinition> abiInfos = req.getAbiInfo();

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
        String binOnChain = web3ApiService.getCode(groupId, contractAddress, BigInteger.ZERO);

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
    public String caseDeploy(ReqDeploy req, boolean doLocally) {
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
    private String deployByLocalContract(ReqDeploy req, boolean doLocally) {
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
        if (StringUtils.isNotBlank(contractAddress)
                || !Address.DEFAULT.getValue().equals(contractAddress)) {
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
    public String deployWithSign(ReqDeploy req) {
        int groupId = req.getGroupId();
        String signUserId = req.getSignUserId();
        List<ABIDefinition> abiInfos = req.getAbiInfo();
        String bytecodeBin = req.getBytecodeBin();
        List<Object> params = req.getFuncParam();
        String contractName = req.getContractName();

        // check groupId
        Client web3j = web3ApiService.getWeb3j(groupId);

        if (web3j == null) {
            throw new FrontException(GROUPID_NOT_EXIST);
        }

        // check deploy permission
        String userAddress = keyStoreService.getAddressBySignUserId(req.getSignUserId());
        if (StringUtils.isNotBlank(userAddress)) {
            checkDeployPermission(req.getGroupId(), userAddress);
        }

        ContractAbiUtil.VersionEvent versionEvent =
                ContractAbiUtil.getVersionEventFromAbi(contractName, abiInfos);
        String encodedConstructor = constructorEncoded(contractName, versionEvent, params);


        // data sign
        String data = bytecodeBin + encodedConstructor;
        String signMsg = transService.signMessage(groupId, web3j, signUserId, "", data);
        // send transaction
        TransactionReceipt receipt = transService.sendMessage(web3j, signMsg);
        String contractAddress = receipt.getContractAddress();

        log.info("success deployWithSign. contractAddress:{}", contractAddress);
        return contractAddress;
    }

    /**
     * deploy locally, not through webase-sign
     */
    public String deployLocally(ReqDeploy req) {
        int groupId = req.getGroupId();
        String userAddress = req.getUser();
        // check deploy permission
        checkDeployPermission(groupId, userAddress);
        String contractName = req.getContractName();
        List<ABIDefinition> abiInfos = req.getAbiInfo();
        String bytecodeBin = req.getBytecodeBin();
        List<Object> params = req.getFuncParam();

        ContractAbiUtil.VersionEvent versionEvent =
                ContractAbiUtil.getVersionEventFromAbi(contractName, abiInfos);
        String encodedConstructor = constructorEncoded(contractName, versionEvent, params);

        // get privateKey
        CryptoKeyPair credentials = keyStoreService.getCredentials(userAddress);
        // contract deploy
        String contractAddress =
                deployContract(groupId, bytecodeBin, encodedConstructor, credentials);

        log.info("success deployLocally. contractAddress:{}", contractAddress);
        return contractAddress;
    }

    /**
     * registerCns.
     */
    public void registerCns(ReqRegisterCns req) throws Exception {
        int groupId = req.getGroupId();
        String cnsName = req.getCnsName();
        String version = req.getVersion();
        String contractAddress = req.getContractAddress();
        String abiInfo = JsonUtils.toJSONString(req.getAbiInfo());
        List<CnsInfo> cnsList =
                precompiledService.queryCnsByNameAndVersion(groupId, cnsName, version);
        if (!CollectionUtils.isEmpty(cnsList)) {
            log.error("registerCns. cnsName:{} version:{} exists", cnsName, version);
            throw new FrontException(ErrorCodeHandleUtils.PRECOMPILED_CONTRACT_NAME_VERSION_EXIST);
        }
        // locally
        if (req.isSaveEnabled()) {
            if (StringUtils.isBlank(req.getContractPath())) {
                throw new FrontException(ConstantCode.PARAM_FAIL_CONTRACT_PATH_IS_EMPTY_STRING);
            }
            if (StringUtils.isBlank(req.getUserAddress())) {
                throw new FrontException(ConstantCode.PARAM_FAIL_USER_IS_EMPTY);
            }
            CryptoKeyPair credentials = keyStoreService.getCredentials(req.getUserAddress());
            CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId), credentials);
            try {
                cnsService.registerCNS(cnsName, version, contractAddress, abiInfo);
            } catch (Exception e) {
                log.error("fail registerCns. cnsName:{}", cnsName);
                throw new FrontException(ConstantCode.CNS_REGISTER_FAIL);
            }
            Cns cns = new Cns();
            cns.setContractAbi(abiInfo);
            BeanUtils.copyProperties(req, cns);
            cns.setCreateTime(LocalDateTime.now());
            cns.setModifyTime(LocalDateTime.now());
            cnsRepository.save(cns);
        } else {
            // from node mgr
            if (StringUtils.isBlank(req.getSignUserId())) {
                throw new FrontException(ConstantCode.PARAM_FAIL_SIGN_USER_ID_IS_EMPTY);
            }
            precompiledWithSignService.registerCns(groupId, req.getSignUserId(), cnsName,
                    req.getVersion(), contractAddress, abiInfo);
        }
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
            List<ABIDefinition> abiInfos) throws FrontException {
        boolean ifExisted = ContractAbiUtil.ifContractAbiExisted(contractName, version);
        if (!ifExisted) {
            ContractAbiUtil.setContractWithAbi(contractName, version, abiInfos, true);
        }
    }

    private String deployContract(int groupId, String bytecodeBin, String encodedConstructor,
            CryptoKeyPair credentials) throws FrontException {
        CommonContract commonContract = null;
        Client web3j = web3ApiService.getWeb3j(groupId);
        if (web3j == null) {
            throw new FrontException(ConstantCode.GROUPID_NOT_EXIST);
        }
        try {
            commonContract =
                    CommonContract.deploy(web3j, credentials, bytecodeBin, encodedConstructor);
        } catch (ContractException e) {
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
            List<ABIDefinition> abiInfo, String contractBin, String packageName)
            throws IOException {

        File abiFile = new File(CleanPathUtil
                .cleanString(Constants.ABI_DIR + Constants.DIAGONAL + contractName + ".abi"));
        FrontUtils.createFileIfNotExist(abiFile, true);
        FileUtils.writeStringToFile(abiFile, JsonUtils.toJSONString(abiInfo));
        File binFile = new File(CleanPathUtil
                .cleanString(Constants.BIN_DIR + Constants.DIAGONAL + contractName + ".bin"));
        FrontUtils.createFileIfNotExist(binFile, true);
        FileUtils.writeStringToFile(binFile, contractBin);

        String outputDirectory = "";
        if (StringUtils.isNotBlank(packageName)) {
            outputDirectory = packageName.replace(".", File.separator);
        }
        if (contractName.length() > 1) {
            contractName = contractName.substring(0, 1).toUpperCase() + contractName.substring(1);
        }
        File outputDir = new File(CleanPathUtil.cleanString(Constants.JAVA_DIR));

        generateJavaFile(packageName, abiFile, binFile, outputDir);

        // generated java file is in outputDir/xxx.java
        String filePath = Constants.JAVA_DIR + File.separator + outputDirectory + File.separator
                + contractName + ".java";
        File file = new File(CleanPathUtil.cleanString(filePath));
        FrontUtils.createFileIfNotExist(file, true);
        InputStream targetStream = new FileInputStream(file);
        return new FileContentHandle(contractName + ".java", targetStream);
    }

    private static synchronized void generateJavaFile(String packageName, File abiFile,
            File binFile, File outputDir) {
        try {
            MySecurityManagerConfig.forbidSystemExitCall();
            // sm bin use same bin
            SolidityContractGenerator generator = new SolidityContractGenerator(binFile, binFile,
                    abiFile, outputDir, packageName);
            generator.generateJavaFiles();
        } catch (IOException | ClassNotFoundException e) {
            log.error("generateJavaFile error for io error/file not found:[]", e);
        } catch (CodeGenException e) {
            log.error("generateJavaFile error code gen:[]", e);
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
     * copyContracts
     */
    @Transactional
    public void copyContracts(ReqCopyContracts reqCopyContracts) {
        log.debug("start saveContractBatch ReqContractList:{}",
                JsonUtils.toJSONString(reqCopyContracts));
        reqCopyContracts.getContractItems().forEach(c -> {
            ReqContractSave reqContractSave = new ReqContractSave();
            reqContractSave.setContractName(c.getContractName());
            reqContractSave.setContractSource(c.getContractSource());
            reqContractSave.setContractPath(reqCopyContracts.getContractPath());
            reqContractSave.setGroupId(reqCopyContracts.getGroupId());
            newContract(reqContractSave);
        });
    }


    /**
     * save new contract.
     */
    @Transactional
    public Contract newContract(ReqContractSave contractReq) {
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
    public Contract updateContract(ReqContractSave contractReq) {
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
     * find contract by page with contract content
     */
    @Transactional
    public Page<Contract> findContractByPage(ReqPageContract param) throws IOException {
        // init templates
        initDefaultContract(param.getGroupId());
        // findContractByPage
        // page start from index 1 instead of 0
        int pageNumber = param.getPageNumber() - 1;
        Pageable pageable =
                new PageRequest(pageNumber, param.getPageSize(), Direction.DESC, "modifyTime");
        Page<Contract> contractPage = contractRepository.findAll(
                (Root<Contract> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
                    // v1.4.2, param add contractPath to filter
                    Predicate predicate = FrontUtils.buildPredicate(root, criteriaBuilder, param);
                    query.where(predicate);
                    return query.getRestriction();
                }, pageable);
        return contractPage;
    }

    /**
     * find all contract without contract content
     */
    public List<RspContractNoAbi> findAllContractNoAbi(int groupId, int contractStatus)
            throws IOException {
        // init templates
        initDefaultContract(groupId);
        // find all
        List<Contract> contractList =
                contractRepository.findByGroupIdAndContractStatus(groupId, contractStatus);
        List<RspContractNoAbi> resultList = new ArrayList<>();
        contractList.forEach(c -> {
            RspContractNoAbi rsp = new RspContractNoAbi();
            BeanUtils.copyProperties(c, rsp);
            resultList.add(rsp);
        });
        return resultList;
    }


    /**
     * save default contract in path '/template' to db
     * 
     * @param groupId
     * @throws IOException
     */
    private void initDefaultContract(Integer groupId) throws IOException {
        String contractPath = "template";
        List<Contract> contracts =
                contractRepository.findByGroupIdAndContractPath(groupId, contractPath);
        // if no template contracts in db, load contract file in template; else, not load
        List<String> templates = null;
        if (contracts.isEmpty()) {
            templates = CommonUtils.readFileToList(Constants.TEMPLATE);
        }
        if ((contracts.isEmpty() && !Objects.isNull(templates)) || (!contracts.isEmpty()
                && !Objects.isNull(templates) && templates.size() != contracts.size())) {
            for (String template : templates) {
                Contract localContract =
                        contractRepository.findByGroupIdAndContractPathAndContractName(groupId,
                                contractPath, template.split(",")[0]);
                if (Objects.isNull(localContract)) {
                    log.info("init template contract:{}", template.split(",")[0]);
                    Contract contract = new Contract();
                    contract.setGroupId(groupId);
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
            contractPathVo.setGroupId(groupId);
            contractPathVo.setContractPath(contractPath);
            contractPathVo.setCreateTime(LocalDateTime.now());
            contractPathVo.setModifyTime(contractPathVo.getCreateTime());
            contractPathRepository.save(contractPathVo);
        }
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
        if (!Objects.isNull(contract) && contract.getContractStatus() == 2
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
            boolean useSM2 = cryptoSuite.cryptoTypeConfig == CryptoType.SM_TYPE;
            // decode
            byte[] contractSourceByteArr = Base64.getDecoder().decode(sourceBase64);
            String contractFilePath = String.format(CONTRACT_FILE_TEMP, contractName);
            // save contract to file
            contractFile = new File(CleanPathUtil.cleanString(contractFilePath));
            FileUtils.writeByteArrayToFile(contractFile, contractSourceByteArr);
            // compile
            SolidityCompiler.Result res = SolidityCompiler.compile(contractFile, useSM2, true, ABI,
                    BIN, INTERFACE, METADATA);
            if ("".equals(res.getOutput())) {
                log.error("contractCompile error", res.getErrors());
                throw new FrontException(ConstantCode.CONTRACT_COMPILE_FAIL.getCode(),
                        res.getErrors());
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
        boolean useSM2 = cryptoSuite.cryptoTypeConfig == CryptoType.SM_TYPE;

        List<RspMultiContractCompile> compileInfos = new ArrayList<>();
        for (File solFile : solFiles) {
            String contractName =
                    solFile.getName().substring(0, solFile.getName().lastIndexOf("."));
            // compile
            SolidityCompiler.Result res = SolidityCompiler.compile(solFile, useSM2, true, ABI,
                    SolidityCompiler.Options.BIN);
            // check result
            if (res.isFailed()) {
                log.error("multiContractCompile fail. contract:{} compile error. {}", contractName,
                        res.getErrors());
                throw new FrontException(ConstantCode.CONTRACT_COMPILE_FAIL.getCode(),
                        res.getErrors());
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
        ContractPathKey pathKey = new ContractPathKey(req.getGroupId(), req.getContractPath());
        ContractPath check = contractPathRepository.findOne(pathKey);
        if (check != null) {
            log.error("addContractPath fail, path exists check:{}", check);
            throw new FrontException(ConstantCode.CONTRACT_PATH_IS_EXISTS);
        }
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
    public List<ContractPath> findPathList(Integer groupId) throws IOException {
        // init default contracts and dir
        initDefaultContract(groupId);
        // get from database
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
        List<PermissionInfo> deployUserList =
                permissionManageService.listDeployAndCreateManager(groupId);

        // check user in the list,
        if (deployUserList.isEmpty()) {
            return;
        } else {
            long count = 0;
            count = deployUserList.stream().filter(admin -> admin.getAddress().equals(userAddress))
                    .count();
            // if not in the list, permission denied
            if (count == 0) {
                log.error("checkDeployPermission permission denied for user:{}", userAddress);
                throw new FrontException(ConstantCode.PERMISSION_DENIED);
            }
        }

    }

    /**
     * batch delete contract by path if path contain deployed
     * 
     * @param groupId
     * @param contractPath
     * @return
     */
    public void batchDeleteByPath(int groupId, String contractPath) {
        log.debug("start batchDeleteByPath groupId:{},contractPath:{}", groupId, contractPath);
        List<Contract> contractList =
                contractRepository.findByGroupIdAndContractPath(groupId, contractPath);
        log.debug("batchDeleteByPath delete contracts");
        contractList.forEach(c -> contractRepository.delete(c.getId()));
        log.debug("batchDeleteByPath delete contracts");
        contractPathRepository.delete(new ContractPathKey(groupId, contractPath));
        log.debug("batchDeleteByPath delete contract path");
    }

    public Contract findById(Long contractId) {
        Contract contract = contractRepository.findOne(contractId);
        if (Objects.isNull(contract)) {
            throw new FrontException(ConstantCode.INVALID_CONTRACT_ID);
        }
        return contract;
    }

    public Contract findByGroupIdAndAddress(int groupId, String contractAddress) {
        Contract contract =
                contractRepository.findByGroupIdAndContractAddress(groupId, contractAddress);
        if (Objects.isNull(contract)) {
            throw new FrontException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
        return contract;
    }

    /**
     * list contract by path list
     * 
     * @param param
     * @return
     */
    public List<Contract> listContractByMultiPath(ReqListContract param) {
        log.debug("start listContractByMultiPath ReqListContract:{},", param);
        List<Contract> resultList = new ArrayList<>();
        int groupId = param.getGroupId();
        List<String> contractPathList = param.getContractPathList();
        for (String contractPath : contractPathList) {
            List<Contract> contractList =
                    contractRepository.findByGroupIdAndContractPath(groupId, contractPath);
            resultList.addAll(contractList);
        }
        log.debug("end listContractByMultiPath result size:{},", resultList.size());
        return resultList;
    }

    /**
     * findCns.
     */
    public Cns findCns(ReqQueryCns req) {
        // get from database
        return cnsRepository.findByAddressLimitOne(req.getGroupId(), req.getContractAddress());
    }
}
