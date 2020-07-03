/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.transaction;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.contract.CommonContract;
import com.webank.webase.front.contract.ContractRepository;
import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.keystore.entity.EncodeInfo;
import com.webank.webase.front.keystore.entity.KeyStoreInfo;
import com.webank.webase.front.precompiledapi.PrecompiledCommonInfo;
import com.webank.webase.front.precompiledapi.PrecompiledService;
import com.webank.webase.front.transaction.entity.ContractFunction;
import com.webank.webase.front.transaction.entity.ContractOfTrans;
import com.webank.webase.front.transaction.entity.ReqTransHandle;
import com.webank.webase.front.transaction.entity.ReqTransHandleWithSign;
import com.webank.webase.front.util.AbiUtil;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.Utils;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.*;
import org.fisco.bcos.web3j.crypto.Sign.SignatureData;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.fisco.bcos.web3j.protocol.core.Request;
import org.fisco.bcos.web3j.protocol.core.methods.request.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.SendTransaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tx.exceptions.ContractCallException;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.fisco.bcos.web3j.tx.txdecode.ConstantProperties;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import static com.webank.webase.front.base.code.ConstantCode.GROUPID_NOT_EXIST;
import static com.webank.webase.front.base.code.ConstantCode.IN_FUNCTION_ERROR;
import static com.webank.webase.front.base.code.ConstantCode.TRANSACTION_FAILED;

/**
 * TransService. handle transactions of deploy/call contract
 */
@Slf4j
@Service
public class TransService {

    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private Constants constants;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private PrecompiledService precompiledService;

    /**
     * transHandleWithSign.
     *
     * @param req request
     */
    public Object transHandleWithSign(ReqTransHandleWithSign req) throws Exception {
        // get signUserId
        String signUserId = req.getSignUserId();
        ContractOfTrans contractOfTrans = new ContractOfTrans(req);
        // check param get function of abi
        ContractFunction contractFunction = buildContractFunction(contractOfTrans);
        // check groupId
        int groupId = contractOfTrans.getGroupId();
        Web3j web3j = web3ApiService.getWeb3j(groupId);
        // check contractAddress
        String contractAddress = contractOfTrans.getContractAddress();
        // encode function
        Function function = new Function(req.getFuncName(),
                contractFunction.getFinalInputs(), contractFunction.getFinalOutputs());

        return handleTransByFunction(groupId, web3j, signUserId, contractAddress, function, contractFunction);
    }

    /**
     * send tx with sign for precomnpiled contract
     * 
     * @param precompiledType enum of precompiled contract
     * @param funcName precompiled contract function name
     */
    public Object transHandleWithSignForPrecompile(int groupId, String signUserId,
            PrecompiledTypes precompiledType, String funcName, List<Object> funcParams)
            throws Exception {
        // check groupId
        Web3j web3j = web3ApiService.getWeb3j(groupId);
        // get address and abi of precompiled contract
        String contractAddress = PrecompiledCommonInfo.getAddress(precompiledType);
        String abiStr = PrecompiledCommonInfo.getAbi(precompiledType);
        List<Object> contractAbi = JsonUtils.toJavaObjectList(abiStr, Object.class);
        // check function param and get function param from abi
        ContractFunction contractFunction =
                buildContractFunctionWithAbi(contractAbi, funcName, funcParams);
        // encode function
        Function function = new Function(funcName, contractFunction.getFinalInputs(),
                contractFunction.getFinalOutputs());
        // trans handle
        return handleTransByFunction(groupId, web3j, signUserId, contractAddress, function,
                contractFunction);
    }

    /**
     * handleTransByFunction by whether is constant
     */
    private Object handleTransByFunction(int groupId, Web3j web3j, String signUserId,
            String contractAddress, Function function, ContractFunction contractFunction)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {

        String encodedFunction = FunctionEncoder.encode(function);
        Object response;
        Instant startTime = Instant.now();
        // if constant, signUserId can be ""
        if (contractFunction.getConstant()) {
            KeyStoreInfo keyStoreInfo = keyStoreService.getKeyStoreInfoForQuery();
            String callOutput = web3j
                    .call(Transaction.createEthCallTransaction(keyStoreInfo.getAddress(),
                            contractAddress, encodedFunction), DefaultBlockParameterName.LATEST)
                    .send().getValue().getOutput();
            List<Type> typeList =
                    FunctionReturnDecoder.decode(callOutput, function.getOutputParameters());
            if (typeList.size() > 0) {
                response = AbiUtil.callResultParse(contractFunction.getOutputList(), typeList);
            } else {
                response = typeList;
            }
        } else {
            // data sign
            String signMsg =
                    signMessage(groupId, web3j, signUserId, contractAddress, encodedFunction);
            if (StringUtils.isBlank(signMsg)) {
                throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
            }
            Instant nodeStartTime = Instant.now();
            // send transaction
            final CompletableFuture<TransactionReceipt> transFuture = new CompletableFuture<>();
            sendMessage(web3j, signMsg, transFuture);
            TransactionReceipt receipt =
                    transFuture.get(constants.getTransMaxWait(), TimeUnit.SECONDS);
            response = receipt;
            log.info("***node cost time***: {}",
                    Duration.between(nodeStartTime, Instant.now()).toMillis());
        }
        log.info("***transaction total cost time***: {}",
                Duration.between(startTime, Instant.now()).toMillis());
        log.info("transHandleWithSign end. func:{} baseRsp:{}", contractFunction.getFuncName(),
                JsonUtils.toJSONString(response));
        return response;
    }

    /**
     * checkAndSaveAbiFromDb.
     *
     * @param req request
     */
    public boolean checkAndSaveAbiFromDb(ContractOfTrans req) throws Exception {
        Contract contract = contractRepository.findByGroupIdAndContractPathAndContractName(
                req.getGroupId(), req.getContractPath(), req.getContractName());
        log.info("checkAndSaveAbiFromDb contract:{}", contract);
        if (Objects.isNull(contract)) {
            log.info("checkAndSaveAbiFromDb contract is null");
            return false;
        }
        // save abi
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        List<AbiDefinition> abiDefinitionList =
                objectMapper.readValue(contract.getContractAbi(), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, AbiDefinition.class));
        ContractAbiUtil.setFunctionFromAbi(req.getContractName(), req.getContractPath(),
                abiDefinitionList, new ArrayList<>());
        return true;
    }


    /**
     * execCall through common contract
     *
     * @param funOutputTypes list
     * @param function function
     * @param commonContract contract
     */
    public static Object execCall(List<String> funOutputTypes, Function function,
            CommonContract commonContract) throws FrontException {
        try {
            List<Type> typeList = commonContract.execCall(function);
            Object result = null;
            if (typeList.size() > 0) {
                result = AbiUtil.callResultParse(funOutputTypes, typeList);
            }
            return result;
        } catch (IOException | ContractCallException e) {
            log.error("execCall failed.", e);
            throw new FrontException(ConstantCode.TRANSACTION_QUERY_FAILED.getCode(),
                    e.getMessage());
        }
    }

    /**
     * execTransaction through common contract
     *
     * @param function function
     * @param commonContract contract
     */
    public static TransactionReceipt execTransaction(Function function,
            CommonContract commonContract) throws FrontException {
        TransactionReceipt transactionReceipt = null;
        Instant startTime = Instant.now();
        log.info("execTransaction start startTime:{}", startTime.toEpochMilli());
        try {
            transactionReceipt = commonContract.execTransaction(function);
        } catch (IOException | TransactionException | ContractCallException e) {
            log.error("execTransaction failed.", e);
            throw new FrontException(ConstantCode.TRANSACTION_SEND_FAILED.getCode(),
                    e.getMessage());
        }
        log.info("execTransaction end  useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return transactionReceipt;
    }

    /**
     * signMessage to create raw transaction and encode data
     *
     * @param groupId id
     * @param contractAddress info
     * @param data info
     * @return
     */
    public String signMessage(int groupId, Web3j web3j, String signUserId, String contractAddress,
            String data) throws FrontException {
        Random r = new Random();
        BigInteger randomid = new BigInteger(250, r);

        BigInteger blockLimit = web3j.getBlockNumberCache();
        String versionContent = Constants.version;
        // get user's signUserId
        // String signUserId = keyStoreService.getSignUserIdByAddress(address);
        String signMsg;
        if (versionContent.contains("2.0.0-rc1") || versionContent.contains("release-2.0.1")) {
            RawTransaction rawTransaction = RawTransaction.createTransaction(randomid,
                    Constants.GAS_PRICE, Constants.GAS_LIMIT, blockLimit, contractAddress,
                    BigInteger.ZERO, data);
            byte[] encodedTransaction = TransactionEncoder.encode(rawTransaction);
            String encodedDataStr = Numeric.toHexString(encodedTransaction);

            EncodeInfo encodeInfo = new EncodeInfo();
            encodeInfo.setSignUserId(signUserId);
            encodeInfo.setEncodedDataStr(encodedDataStr);
            String signDataStr = keyStoreService.getSignData(encodeInfo);
            if (StringUtils.isBlank(signDataStr)) {
                log.warn("deploySend get sign data error.");
                return null;
            }

            SignatureData signData = CommonUtils.stringToSignatureData(signDataStr);
            byte[] signedMessage = TransactionEncoder.encode(rawTransaction, signData);
            signMsg = Numeric.toHexString(signedMessage);
        } else {
            // String chainId = versionContent.get("Chain Id");
            String chainId = Constants.chainId;
            ExtendedRawTransaction extendedRawTransaction =
                    ExtendedRawTransaction.createTransaction(randomid, Constants.GAS_PRICE,
                            Constants.GAS_LIMIT, blockLimit, contractAddress, BigInteger.ZERO, data,
                            new BigInteger(chainId), BigInteger.valueOf(groupId), "");
            byte[] encodedTransaction = ExtendedTransactionEncoder.encode(extendedRawTransaction);
            String encodedDataStr = Numeric.toHexString(encodedTransaction);

            EncodeInfo encodeInfo = new EncodeInfo();
            encodeInfo.setSignUserId(signUserId);
            encodeInfo.setEncodedDataStr(encodedDataStr);

            Instant startTime = Instant.now();

            String signDataStr = keyStoreService.getSignData(encodeInfo);

            log.info("get signdatastr cost time: {}",
                    Duration.between(startTime, Instant.now()).toMillis());

            if (StringUtils.isBlank(signDataStr)) {
                log.warn("deploySend get sign data error.");
                return null;
            }

            SignatureData signData = CommonUtils.stringToSignatureData(signDataStr);
            byte[] signedMessage =
                    ExtendedTransactionEncoder.encode(extendedRawTransaction, signData);
            signMsg = Numeric.toHexString(signedMessage);
        }
        return signMsg;

    }


    /**
     * send message to node.
     *
     * @param signMsg signMsg
     * @param future future
     */
    public void sendMessage(Web3j web3j, String signMsg,
            final CompletableFuture<TransactionReceipt> future) throws IOException {
        Request<?, SendTransaction> request = web3j.sendRawTransaction(signMsg);
        request.setNeedTransCallback(true);
        request.setTransactionSucCallback(new TransactionSucCallback() {
            @Override
            public void onResponse(TransactionReceipt receipt) {
                log.info("onResponse receipt:{}", receipt);
                future.complete(receipt);
                return;
            }
        });
        request.send();
    }


    /**
     * build ContractFunction if abi is empty, check in db or file: conf/*.abi else build directly
     */
    private ContractFunction buildContractFunction(ContractOfTrans cot) throws Exception {
        log.debug("start buildContractFunction");
        if (CollectionUtils.isEmpty(cot.getContractAbi())) {
            checkContractAbiInCnsOrDb(cot);
            return buildContractFunctionWithCns(cot.getContractName(), cot.getFuncName(),
                    cot.getVersion(), cot.getFuncParam());
        }
        return buildContractFunctionWithAbi(cot.getContractAbi(), cot.getFuncName(),
                cot.getFuncParam());
    }

    /**
     * build Function with cns.
     */
    private ContractFunction buildContractFunctionWithCns(String contractName, String funcName,
            String version, List<Object> params) throws Exception {
        log.debug("start buildContractFunctionWithCns");
        // if function is constant
        boolean constant = ContractAbiUtil.getConstant(contractName, funcName, version);
        // inputs format
        List<String> funcInputTypes =
                ContractAbiUtil.getFuncInputType(contractName, funcName, version);
        // check param match inputs
        if (funcInputTypes.size() != params.size()) {
            log.error("load contract function error for function params not fit");
            throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
        }
        List<Type> finalInputs = AbiUtil.inputFormat(funcInputTypes, params);
        // outputs format
        List<String> funOutputTypes =
                ContractAbiUtil.getFuncOutputType(contractName, funcName, version);
        List<TypeReference<?>> finalOutputs = AbiUtil.outputFormat(funOutputTypes);

        // build ContractFunction
        ContractFunction cf = ContractFunction.builder().funcName(funcName).constant(constant)
                .inputList(funcInputTypes).outputList(funOutputTypes).finalInputs(finalInputs)
                .finalOutputs(finalOutputs).build();
        return cf;
    }

    /**
     * build Function with abi.
     */
    private ContractFunction buildContractFunctionWithAbi(List<Object> contractAbi, String funcName,
            List<Object> params) {
        log.debug("start buildContractFunctionWithAbi");
        // check function name
        AbiDefinition abiDefinition =
                AbiUtil.getAbiDefinition(funcName, JsonUtils.toJSONString(contractAbi));
        if (Objects.isNull(abiDefinition)) {
            log.warn("transaction fail. func:{} is not existed", funcName);
            throw new FrontException(IN_FUNCTION_ERROR);
        }

        // input format
        List<String> funcInputTypes = AbiUtil.getFuncInputType(abiDefinition);
        // check param match inputs
        if (funcInputTypes.size() != params.size()) {
            log.error("load contract function error for function params not fit");
            throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
        }
        List<Type> finalInputs = AbiUtil.inputFormat(funcInputTypes, params);
        // output format
        List<String> funOutputTypes = AbiUtil.getFuncOutputType(abiDefinition);
        List<TypeReference<?>> finalOutputs = AbiUtil.outputFormat(funOutputTypes);

        // build ContractFunction
        ContractFunction cf =
                ContractFunction.builder().funcName(funcName).constant(abiDefinition.isConstant())
                        .inputList(funcInputTypes).outputList(funOutputTypes)
                        .finalInputs(finalInputs).finalOutputs(finalOutputs).build();
        return cf;
    }

    /**
     * does abi exist in cns or db.
     */
    private void checkContractAbiInCnsOrDb(ContractOfTrans contract) throws Exception {
        log.debug("start checkContractAbiInCnsOrDb");
        boolean ifExisted;
        // check if contractAbi existed in cache
        if (contract.getVersion() != null) {
            ifExisted = ContractAbiUtil.ifContractAbiExisted(contract.getContractName(),
                    contract.getVersion());
        } else {
            ifExisted = ContractAbiUtil.ifContractAbiExisted(contract.getContractName(),
                    contract.getContractAddress().substring(2));
            contract.setVersion(contract.getContractAddress().substring(2));
        }
        // deprecated cns in front
        // check if contractAbi existed in cns
//        if (!ifExisted) {
//            ifExisted = checkAndSaveAbiFromCns(contract);
//        }
        // check if contractAbi existed in db
        if (!ifExisted) {
            ifExisted = checkAndSaveAbiFromDb(contract);
            contract.setVersion(contract.getContractPath());
        }

        if (!ifExisted) {
            throw new FrontException(ConstantCode.ABI_GET_ERROR);
        }

    }

    /**
     * send transaction locally
     */
    public Object transHandleLocal(ReqTransHandle req) throws Exception {
        log.info("transHandle start. ReqTransHandle:[{}]", JsonUtils.toJSONString(req));

        // init contract params
        ContractOfTrans cof = new ContractOfTrans(req);
        // check param and build function
        ContractFunction contractFunction = buildContractFunction(cof);

        // address
        String address = cof.getContractAddress();

        // web3j
        Web3j web3j = web3ApiService.getWeb3j(cof.getGroupId());
        // get privateKey
        Credentials credentials = getCredentials(contractFunction.getConstant(), req.getUser());
        // contract load
        ContractGasProvider contractGasProvider =
                new StaticGasProvider(Constants.GAS_PRICE, Constants.GAS_LIMIT);
        CommonContract commonContract = CommonContract.load(address, web3j, credentials, contractGasProvider);

        // request
        Object result;
        Function function = new Function(cof.getFuncName(), contractFunction.getFinalInputs(),
                contractFunction.getFinalOutputs());
        if (contractFunction.getConstant()) {
            result = execCall(contractFunction.getOutputList(), function, commonContract);
        } else {
            result = execTransaction(function, commonContract);
        }

        log.info("transHandle end. name:{} func:{} result:{}", cof.getContractName(),
                cof.getFuncName(), JsonUtils.toJSONString(result));
        return result;
    }


    public TransactionReceipt sendSignedTransaction(String signedStr, Boolean sync, int groupId)  {

        Web3j web3j = web3ApiService.getWeb3j(groupId);
        if (sync) {
            final CompletableFuture<TransactionReceipt> transFuture = new CompletableFuture<>();
            TransactionReceipt receipt;
            try {
                sendMessage(web3j, signedStr, transFuture);
                receipt = transFuture.get(constants.getTransMaxWait(), TimeUnit.SECONDS);
            } catch (Exception e ) {
                throw new FrontException(TRANSACTION_FAILED.getMessage() +e.getMessage());
            }
             return receipt;
        } else {
            TransactionReceipt transactionReceipt = new TransactionReceipt();
            web3j.sendRawTransaction(signedStr).sendAsync();
            transactionReceipt.setTransactionHash(Hash.sha3(signedStr));
            return transactionReceipt;
        }
    }



    public Object sendQueryTransaction(String encodeStr, String contractAddress, String funcName, String contractAbi, int groupId, String userAddress) {

        Web3j web3j = web3ApiService.getWeb3j(groupId);
        String callOutput ;
        try {
           callOutput = web3j.call(Transaction.createEthCallTransaction(userAddress, contractAddress, encodeStr), DefaultBlockParameterName.LATEST)
                    .send().getValue().getOutput();
        } catch (IOException e) {
            throw new FrontException(TRANSACTION_FAILED);
        }

        AbiDefinition abiDefinition = getFunctionAbiDefinition(funcName, contractAbi);
        if (Objects.isNull(abiDefinition)) {
            throw new FrontException(IN_FUNCTION_ERROR);
        }
        List<String> funOutputTypes = AbiUtil.getFuncOutputType(abiDefinition);
        List<TypeReference<?>> finalOutputs = AbiUtil.outputFormat(funOutputTypes);

        List<Type> typeList = FunctionReturnDecoder.decode(callOutput, Utils.convert(finalOutputs));
        Object response;
        if (typeList.size() > 0) {
            response = AbiUtil.callResultParse(funOutputTypes, typeList);
        } else {
            response = typeList;
        }
        return response;
    }

    public static AbiDefinition getFunctionAbiDefinition(String functionName, String contractAbi) {
        if(functionName == null) {
            throw new FrontException(IN_FUNCTION_ERROR);
        }
        List<AbiDefinition> abiDefinitionList = JsonUtils.toJavaObjectList(contractAbi, AbiDefinition.class);
        if (abiDefinitionList == null) {
            throw new FrontException(ConstantCode.FAIL_PARSE_JSON);
        }
        AbiDefinition result = null;
        for (AbiDefinition abiDefinition : abiDefinitionList) {
            if (abiDefinition == null) {
                throw new FrontException(IN_FUNCTION_ERROR);
            }
            if (ConstantProperties.TYPE_FUNCTION.equals(abiDefinition.getType())
                    && functionName.equals(abiDefinition.getName())) {
                result = abiDefinition;
                break;
            }
        }
        return result;
    }
    /**
     * get Credentials by keyUser locally
     */
    private Credentials getCredentials(boolean constant, String keyUser) {
        // get privateKey
        Credentials credentials;
        if (constant) {
            credentials = keyStoreService.getCredentialsForQuery();
        } else {
            credentials = keyStoreService.getCredentials(keyUser);
        }
        return credentials;
    }

    /**
     * @Deprecated old transHandleWithSign.
     * @param req request
     */

    // public Object transHandleWithSign(ReqTransHandleWithSign req) throws Exception {
    // //get function of abi
    // ContractFunction cf = buildContractFunction(new ContractOfTrans(req));
    // //check param
    // checkParamOfTransaction(cf, req.getFuncParam());
    //
    // // check contractAddress
    // String contractAddress = req.getContractAddress();
    // if (StringUtils.isBlank(contractAddress)) {
    // log.error("transHandleWithSign. contractAddress is empty");
    // throw new FrontException(ConstantCode.CONTRACT_ADDRESS_NULL);
    // }
    //
    // // check groupId
    // Web3j web3j = getWeb3j(req.getGroupId());
    //
    // // encode function
    // Function function = new Function(req.getFuncName(), cf.getFinalInputs(),
    // cf.getFinalOutputs());
    // String encodedFunction = FunctionEncoder.encode(function);
    //
    // // trans handle
    // Object response = "";
    // Instant startTime = Instant.now();
    // if (cf.getConstant()) {
    // KeyStoreInfo keyStoreInfo = keyStoreService.getKeyStoreInfoForQuery();
    // String callOutput = web3j
    // .call(Transaction.createEthCallTransaction(keyStoreInfo.getAddress(),
    // contractAddress, encodedFunction), DefaultBlockParameterName.LATEST)
    // .send().getValue().getOutput();
    // List<Type> typeList =
    // FunctionReturnDecoder.decode(callOutput, function.getOutputParameters());
    // if (typeList.size() > 0) {
    // response = AbiUtil.callResultParse(cf.getOutputList(), typeList);
    // } else {
    // response = typeList;
    // }
    // } else {
    // // data sign
    // String signMsg = signMessage(req.getGroupId(), web3j, req.getSignAddress(), contractAddress,
    // encodedFunction);
    // if (StringUtils.isBlank(signMsg)) {
    // throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
    // }
    // Instant nodeStartTime = Instant.now();
    // // send transaction
    // final CompletableFuture<TransactionReceipt> transFuture = new CompletableFuture<>();
    // sendMessage(web3j, signMsg, transFuture);
    // TransactionReceipt receipt =
    // transFuture.get(constants.getTransMaxWait(), TimeUnit.SECONDS);
    // response = receipt;
    // log.info("***node cost time***: {}", Duration.between(nodeStartTime,
    // Instant.now()).toMillis());
    //
    // }
    // log.info("***transaction total cost time***: {}", Duration.between(startTime,
    // Instant.now()).toMillis());
    // log.info("transHandleWithSign end. func:{} baseRsp:{}", req.getFuncName(),
    // JsonUtils.toJSONString(response));
    // return response;
    // }
}


