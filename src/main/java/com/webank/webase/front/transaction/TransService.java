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


import static com.webank.webase.front.base.code.ConstantCode.IN_FUNCTION_ERROR;
import static com.webank.webase.front.base.code.ConstantCode.VERSION_NOT_EXISTS;
import static com.webank.webase.front.util.ContractAbiUtil.STATE_MUTABILITY_PURE;
import static com.webank.webase.front.util.ContractAbiUtil.STATE_MUTABILITY_VIEW;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.webank.webase.front.keystore.entity.RspMessageHashSignature;
import com.webank.webase.front.precompiledapi.PrecompiledCommonInfo;
import com.webank.webase.front.precompiledapi.PrecompiledService;
import com.webank.webase.front.transaction.entity.ContractFunction;
import com.webank.webase.front.transaction.entity.ReqSignMessageHash;
import com.webank.webase.front.transaction.entity.ReqTransHandle;
import com.webank.webase.front.transaction.entity.ReqTransHandleWithSign;
import com.webank.webase.front.util.AbiUtil;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.Utils;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.crypto.signature.SM2SignatureResult;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessor;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.transaction.model.po.RawTransaction;
import org.fisco.bcos.sdk.transaction.pusher.TransactionPusherService;
import org.fisco.bcos.sdk.utils.ByteUtils;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    @Qualifier(value = "sm")
    private CryptoSuite smCryptoSuite;
    @Autowired
    @Qualifier(value = "ecdsa")
    private CryptoSuite ecdsaCryptoSuite;
    @Autowired
    @Qualifier(value = "common")
    private CryptoSuite cryptoSuite;
    @Autowired
    private PrecompiledService precompiledService;
    @Autowired
    private BcosSDK bcosSDK;

    /**
     * transHandleWithSign.
     *
     * @param req request
     */
    public Object transHandleWithSign(ReqTransHandleWithSign req) throws Exception {
        // get signUserId
        String signUserId = req.getSignUserId();
        // check param get function of abi
        ContractFunction contractFunction = buildContractFunctionWithAbi(req.getContractAbi(),
            req.getFuncName(), req.getFuncParam());
        // check groupId
        int groupId = req.getGroupId();
        Client web3j = web3ApiService.getWeb3j(groupId);
        // check contractAddress
        String contractAddress = req.getContractAddress();
        if (req.isUseCns()) {
            List<CnsInfo> cnsList = precompiledService.queryCnsByNameAndVersion(req.getGroupId(),
                    req.getCnsName(), req.getVersion());
            if (CollectionUtils.isEmpty(cnsList)) {
                throw new FrontException(VERSION_NOT_EXISTS);
            }
            contractAddress = cnsList.iterator().next().getAddress();
            log.info("transHandleWithSign cns contractAddress:{}", contractAddress);
        }
        // encode function
        Function function = new Function(req.getFuncName(), contractFunction.getFinalInputs(),
                contractFunction.getFinalOutputs());

        return handleTransByFunction(groupId, web3j, signUserId, contractAddress, function,
                contractFunction);
    }

    /**
     * send tx with sign for precomnpiled contract
     * 
     * @param precompiledType enum of precompiled contract
     * @param funcName precompiled contract function name
     */
    public Object transHandleWithSignForPrecompile(int groupId, String signUserId,
            PrecompiledTypes precompiledType, String funcName, List<Object> funcParams) {
        // check groupId
        Client web3j = web3ApiService.getWeb3j(groupId);
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
    private Object handleTransByFunction(int groupId, Client web3j, String signUserId,
            String contractAddress, Function function, ContractFunction contractFunction) {

        FunctionEncoder functionEncoder = new FunctionEncoder(cryptoSuite);
        String encodedFunction = functionEncoder.encode(function);
        Object response;
        Instant startTime = Instant.now();
        // if constant, signUserId can be ""
        if (contractFunction.getConstant()) {
            KeyStoreInfo keyStoreInfo = keyStoreService.getKeyStoreInfoForQuery();
            TransactionProcessor transactionProcessor = new TransactionProcessor(
                web3ApiService.getWeb3j(groupId), keyStoreService.getCredentialsForQuery(),
                groupId, Constants.chainId);
            String callOutput = transactionProcessor
                .executeCall(keyStoreInfo.getAddress(),
                            contractAddress, encodedFunction)
                .getCallResult().getOutput();
            // throw new FrontException(ConstantCode.CALL_CONTRACT_ERROR, e.getMessage());

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
            Instant nodeStartTime = Instant.now();
            TransactionDecoderService txDecoder = new TransactionDecoderService(cryptoSuite);
            // send transaction
            TransactionReceipt responseReceipt = sendMessage(web3j, signMsg);
            String receiptMsg = txDecoder.decodeReceiptStatus(responseReceipt).getReceiptMessages();
            responseReceipt.setMessage(receiptMsg);
            response = responseReceipt;
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
    public boolean checkAndSaveAbiFromDb(ReqTransHandle req) {
        Contract contract = contractRepository.findByGroupIdAndContractPathAndContractName(
                req.getGroupId(), req.getContractPath(), req.getContractName());
        log.info("checkAndSaveAbiFromDb contract:{}", contract);
        if (Objects.isNull(contract)) {
            log.info("checkAndSaveAbiFromDb contract is null");
            return false;
        }
        // save abi
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        List<ABIDefinition> abiDefinitionList;
        try {
            abiDefinitionList = objectMapper.readValue(contract.getContractAbi(), objectMapper
                    .getTypeFactory().constructCollectionType(List.class, ABIDefinition.class));
        } catch (JsonProcessingException e) {
            log.error("parse abi to json error:[]", e);
            throw new FrontException(ConstantCode.CONTRACT_ABI_PARSE_JSON_ERROR);
        }
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
        } catch (ContractException e) {
            log.error("execCall failed of ContractException:{}", e);
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
            CommonContract commonContract, TransactionDecoderService txDecoder) throws FrontException {
        Instant startTime = Instant.now();
        log.info("execTransaction start startTime:{}", startTime.toEpochMilli());
        TransactionReceipt transactionReceipt = commonContract.execTransaction(function);
        // cover null message through statusCode
        // String receiptMsg = FrontUtils.handleReceiptMsg(transactionReceipt);
        String receiptMsg = txDecoder.decodeReceiptStatus(transactionReceipt).getReceiptMessages();
        transactionReceipt.setMessage(receiptMsg);
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
    public String signMessage(int groupId, Client web3j, String signUserId, String contractAddress,
            String data) {
        Random r = new SecureRandom();
        BigInteger randomid = new BigInteger(250, r);

        BigInteger blockLimit = web3j.getBlockLimit();

        // v1.5.0 no longer support 2.0.0-rc1 or 2.0.1
        // if (versionContent.contains("2.0.0-rc1") || versionContent.contains("release-2.0.1")) {

        // to encode raw tx
        TransactionEncoderService encoderService = new TransactionEncoderService(cryptoSuite);
        String chainId = Constants.chainId;
        RawTransaction rawTransaction =
            RawTransaction.createTransaction(randomid, Constants.GAS_PRICE,
                Constants.GAS_LIMIT, blockLimit, contractAddress, BigInteger.ZERO, data,
                new BigInteger(chainId), BigInteger.valueOf(groupId), "");

        byte[] encodedTransaction = encoderService.encode(rawTransaction, null);
        String encodedDataStr = Numeric.toHexString(encodedTransaction);

        EncodeInfo encodeInfo = new EncodeInfo();
        encodeInfo.setSignUserId(signUserId);
        encodeInfo.setEncodedDataStr(encodedDataStr);

        Instant startTime = Instant.now();
        String signDataStr = keyStoreService.getSignData(encodeInfo);
        log.info("get signdatastr cost time: {}",
                Duration.between(startTime, Instant.now()).toMillis());
        SignatureResult signData = CommonUtils.stringToSignatureData(signDataStr, cryptoSuite.cryptoTypeConfig);
        byte[] signedMessage = encoderService.encode(rawTransaction, signData);
        return Numeric.toHexString(signedMessage);

    }


    /**
     * send message to node.
     *
     * @param signMsg signMsg
     */
    public TransactionReceipt sendMessage(Client web3j, String signMsg) {
        TransactionPusherService txPusher = new TransactionPusherService(web3j);
        TransactionReceipt receipt = txPusher.push(signMsg);
        CommonUtils.processReceiptHexNumber(receipt);
        return receipt;

    }

    /**
     * build Function with cns.
     */
    private ContractFunction buildContractFunctionWithCns(String contractName, String funcName,
            String version, List<Object> params) {
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
        ABIDefinition abiDefinition =
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

        // fit in solidity 0.6
        boolean isConstant = (STATE_MUTABILITY_VIEW.equals(abiDefinition.getStateMutability())
                || STATE_MUTABILITY_PURE.equals(abiDefinition.getStateMutability()));
        // build ContractFunction
        ContractFunction cf = ContractFunction.builder().funcName(funcName).constant(isConstant)
                // .constant(abiDefinition.isConstant())
                .inputList(funcInputTypes).outputList(funOutputTypes).finalInputs(finalInputs)
                .finalOutputs(finalOutputs).build();
        return cf;
    }

    /**
     * does abi exist in cns or db.
     * @Deprecated: request body contains abi list
     */
    private void checkContractAbiInCnsOrDb(ReqTransHandle req) {
        log.debug("start checkContractAbiInCnsOrDb");
        String version = req.getVersion();
        String contractName = req.getContractName();
        String contractAddress = req.getContractAddress();
        String contractPath = req.getContractPath();
        boolean ifExisted;
        // check if contractAbi existed in cache
        if (version != null) {
            ifExisted = ContractAbiUtil.ifContractAbiExisted(contractName, version);
        } else {
            ifExisted = ContractAbiUtil.ifContractAbiExisted(contractName, contractAddress.substring(2));
            req.setVersion(contractAddress.substring(2));
        }
        // deprecated cns in front
        // check if contractAbi existed in cns
        // if (!ifExisted) {
        // ifExisted = checkAndSaveAbiFromCns(contract);
        // }
        // check if contractAbi existed in db
        if (!ifExisted) {
            ifExisted = checkAndSaveAbiFromDb(req);
            req.setVersion(contractPath);
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
        checkContractAbiInCnsOrDb(req);
        // check param and build function
        ContractFunction contractFunction = buildContractFunctionWithAbi(req.getContractAbi(),
            req.getFuncName(), req.getFuncParam());

        // address
        String address = req.getContractAddress();
        if (req.isUseCns()) {
            List<CnsInfo> cnsList = precompiledService.queryCnsByNameAndVersion(req.getGroupId(),
                    req.getCnsName(), req.getVersion());
            if (CollectionUtils.isEmpty(cnsList)) {
                throw new FrontException(VERSION_NOT_EXISTS);
            }
            address = cnsList.iterator().next().getAddress();
            log.info("transHandleLocal cns contractAddress:{}", address);
        }

        // web3j
        Client web3j = web3ApiService.getWeb3j(req.getGroupId());
        // get privateKey
        CryptoKeyPair credentials = getCredentials(contractFunction.getConstant(), req.getUser());
        CommonContract commonContract =
                CommonContract.load(address, web3j, credentials);
        // tx decoder
        TransactionDecoderService txDecoder = new TransactionDecoderService(cryptoSuite);
        // request
        Object result;
        Function function = new Function(req.getFuncName(), contractFunction.getFinalInputs(),
                contractFunction.getFinalOutputs());
        if (contractFunction.getConstant()) {
            result = execCall(contractFunction.getOutputList(), function, commonContract);
        } else {
            result = execTransaction(function, commonContract, txDecoder);
        }

        log.info("transHandle end. name:{} func:{} result:{}", req.getContractName(),
            req.getFuncName(), JsonUtils.toJSONString(result));
        return result;
    }


    public TransactionReceipt sendSignedTransaction(String signedStr, Boolean sync, int groupId) {

        Client web3j = web3ApiService.getWeb3j(groupId);
        if (sync) {
            TransactionReceipt receipt = sendMessage(web3j, signedStr);
            CommonUtils.processReceiptHexNumber(receipt);
            return receipt;
        } else {
            TransactionPusherService txPusher = new TransactionPusherService(web3j);
            txPusher.pushOnly(signedStr);
            TransactionReceipt transactionReceipt = new TransactionReceipt();
            transactionReceipt.setTransactionHash(cryptoSuite.hash(signedStr));
            return transactionReceipt;
        }
    }


    public Object sendQueryTransaction(String encodeStr, String contractAddress, String funcName,
            String contractAbi, int groupId, String userAddress) {

        Client web3j = web3ApiService.getWeb3j(groupId);
        String callOutput = web3j
            .call(new Transaction(userAddress, contractAddress,
                        encodeStr))
            .getCallResult().getOutput();

        ABIDefinition abiDefinition = getFunctionAbiDefinition(funcName, contractAbi);
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

    public static ABIDefinition getFunctionAbiDefinition(String functionName, String contractAbi) {
        if (functionName == null) {
            throw new FrontException(IN_FUNCTION_ERROR);
        }
        List<ABIDefinition> abiDefinitionList =
                JsonUtils.toJavaObjectList(contractAbi, ABIDefinition.class);
        if (abiDefinitionList == null) {
            throw new FrontException(ConstantCode.FAIL_PARSE_JSON);
        }
        ABIDefinition result = null;
        for (ABIDefinition abiDefinition : abiDefinitionList) {
            if (abiDefinition == null) {
                throw new FrontException(IN_FUNCTION_ERROR);
            }
            if (Constants.TYPE_FUNCTION.equals(abiDefinition.getType())
                    && functionName.equals(abiDefinition.getName())) {
                result = abiDefinition;
                break;
            }
        }
        return result;
    }

    /**
     * get CryptoKeyPair by keyUser locally
     */
    private CryptoKeyPair getCredentials(boolean constant, String keyUser) {
        // get privateKey
        CryptoKeyPair credentials;
        if (constant) {
            credentials = keyStoreService.getCredentialsForQuery();
        } else {
            credentials = keyStoreService.getCredentials(keyUser);
        }
        return credentials;
    }

    public SignatureResult signMessageHashByType(String messageHash, CryptoKeyPair cryptoKeyPair,
            int encryptType) {
        try {
            if (encryptType == CryptoType.SM_TYPE) {
                return smCryptoSuite.sign(messageHash, cryptoKeyPair);
            } else {
                return ecdsaCryptoSuite.sign(messageHash, cryptoKeyPair);
            }
        } catch (Exception e) {
            throw new FrontException(ConstantCode.GET_MESSAGE_HASH, e.getMessage());
        }
    }

    /**
     * signMessageLocal
     */
    public Object signMessageLocal(ReqSignMessageHash req) {
        log.info("transHandle start. ReqSignMessageHash:[{}]", JsonUtils.toJSONString(req));

        CryptoKeyPair cryptoKeyPair = keyStoreService.getCredentials(req.getUser());


        SignatureResult signResult = signMessageHashByType(
                org.fisco.bcos.sdk.utils.Numeric.cleanHexPrefix(req.getHash()), cryptoKeyPair,
                cryptoSuite.cryptoTypeConfig);
        if (cryptoSuite.cryptoTypeConfig == CryptoType.SM_TYPE) {
            SM2SignatureResult sm2SignatureResult = (SM2SignatureResult) signResult;
            RspMessageHashSignature rspMessageHashSignature = new RspMessageHashSignature();
            rspMessageHashSignature.setP(Numeric.toHexString(sm2SignatureResult.getPub()));
            rspMessageHashSignature.setR(Numeric.toHexString(sm2SignatureResult.getR()));
            rspMessageHashSignature.setS(Numeric.toHexString(sm2SignatureResult.getS()));
            rspMessageHashSignature.setV((byte) 0);
            return rspMessageHashSignature;
        } else {
            ECDSASignatureResult sm2SignatureResult = (ECDSASignatureResult) signResult;
            RspMessageHashSignature rspMessageHashSignature = new RspMessageHashSignature();
            rspMessageHashSignature.setP("0x");
            rspMessageHashSignature.setR(Numeric.toHexString(sm2SignatureResult.getR()));
            rspMessageHashSignature.setS(Numeric.toHexString(sm2SignatureResult.getS()));
            rspMessageHashSignature.setV((byte) (sm2SignatureResult.getV()+27));
            return rspMessageHashSignature;
        }
    }

    /**
     * get encoded raw transaction
     * @param req req.user userAddress, if not null, return signed raw tx
     */
    public String transToRawTxStr(ReqTransHandle req) throws Exception {
        // get signUserId
        String user = req.getUser();
        // check param get function of abi
        ContractFunction contractFunction = buildContractFunctionWithAbi(req.getContractAbi(),
            req.getFuncName(), req.getFuncParam());
        // check groupId
        int groupId = req.getGroupId();
        Client web3j = web3ApiService.getWeb3j(groupId);
        // check contractAddress
        String contractAddress = req.getContractAddress();
        if (req.isUseCns()) {
            List<CnsInfo> cnsList = precompiledService.queryCnsByNameAndVersion(req.getGroupId(),
                req.getCnsName(), req.getVersion());
            if (CollectionUtils.isEmpty(cnsList)) {
                throw new FrontException(VERSION_NOT_EXISTS);
            }
            contractAddress = cnsList.iterator().next().getAddress();
            log.info("transHandleWithSign cns contractAddress:{}", contractAddress);
        }
        // encode function
        Function function = new Function(req.getFuncName(), contractFunction.getFinalInputs(),
            contractFunction.getFinalOutputs());

        return createRawTxEncoded(groupId, web3j, contractAddress, function, user);
    }

    /**
     * handleTransByFunction by whether is constant
     * if use signed data to send tx, call @send-signed-transaction api
     */
    private String createRawTxEncoded(int groupId, Client web3j, String contractAddress,
        Function function, String userAddress) {

        // to encode raw tx
        BigInteger randomId = new BigInteger(250, new SecureRandom());
        BigInteger blockLimit = web3j.getBlockLimit();
        FunctionEncoder functionEncoder = new FunctionEncoder(cryptoSuite);
        String encodedFunction = functionEncoder.encode(function);
        log.info("createRawTxEncoded encodedFunction:{}", encodedFunction);
        TransactionEncoderService encoderService = new TransactionEncoderService(cryptoSuite);
        String chainId = Constants.chainId;
        RawTransaction rawTransaction =
            RawTransaction.createTransaction(randomId, Constants.GAS_PRICE,
                Constants.GAS_LIMIT, blockLimit, contractAddress, BigInteger.ZERO, encodedFunction,
                new BigInteger(chainId), BigInteger.valueOf(groupId), "");

        byte[] encodedTransaction = encoderService.encode(rawTransaction, null);
        String encodedDataStr = Numeric.toHexString(encodedTransaction);
        log.info("createRawTxEncoded encodedDataStr:{}", encodedDataStr);
        // if user not null: sign, else, not sign
        SignatureResult userSignResult = null;
        if (!StringUtils.isBlank(userAddress)) {
            log.info("createRawTxEncoded use key of {} to sign message", userAddress);
            byte[] hashMessage = cryptoSuite.hash(ByteUtils.hexStringToBytes(encodedDataStr));
            String hashMessageStr = Numeric.toHexString(hashMessage);
            log.info("createRawTxEncoded hashMessageStr:{}", hashMessageStr);
            userSignResult = signMessageHashByType(hashMessageStr,
                getCredentials(false, userAddress),
                cryptoSuite.cryptoTypeConfig);
        }
        // encode again
        byte[] signedMessage = encoderService.encode(rawTransaction, userSignResult);
        return Numeric.toHexString(signedMessage);
    }

}


