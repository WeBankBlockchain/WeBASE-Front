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
import static com.webank.webase.front.base.properties.Constants.RECEIPT_STATUS_0X0;
import static com.webank.webase.front.util.ContractAbiUtil.STATE_MUTABILITY_PURE;
import static com.webank.webase.front.util.ContractAbiUtil.STATE_MUTABILITY_VIEW;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.ClassName;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.contract.CommonContract;
import com.webank.webase.front.contract.ContractRepository;
import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.keystore.entity.EncodeInfo;
import com.webank.webase.front.keystore.entity.KeyStoreInfo;
import com.webank.webase.front.keystore.entity.RspMessageHashSignature;
import com.webank.webase.front.keystore.entity.RspUserInfo;
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
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.Utils;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.AbiTypes;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.wrapper.ABICodecJsonWrapper;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition.NamedType;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.abi.wrapper.ABIObject;
import org.fisco.bcos.sdk.abi.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.client.protocol.response.Call.CallOutput;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.crypto.signature.SM2SignatureResult;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderInterface;
import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderService;
import org.fisco.bcos.sdk.transaction.codec.decode.RevertMessageParser;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderInterface;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.transaction.model.gas.DefaultGasProvider;
import org.fisco.bcos.sdk.transaction.model.po.RawTransaction;
import org.fisco.bcos.sdk.transaction.pusher.TransactionPusherService;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import springfox.documentation.spring.web.json.Json;

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

    public Object transHandleWithSign(ReqTransHandleWithSign req) throws FrontException {
        int groupId = req.getGroupId();
        String signUserId = req.getSignUserId();
        String userAddress = keyStoreService.getAddressBySignUserId(signUserId);
        if (StringUtils.isBlank(userAddress)) {
            log.warn("transHandleWithSign this signUser [{}] not record in webase-front", signUserId);
            userAddress = keyStoreService.getCredentialsForQuery().getAddress();
        }
        String abiStr = JsonUtils.objToString(req.getContractAbi());
        String funcName = req.getFuncName();
        List<String> funcParam = req.getFuncParam() == null ? new ArrayList<>() : req.getFuncParam();
        String contractAddress = req.getContractAddress();
        // handle cns
        if (req.isUseCns()) {
            try {
                List<CnsInfo> cnsList = precompiledService.queryCnsByNameAndVersion(req.getGroupId(),
                        req.getCnsName(), req.getVersion());
                if (CollectionUtils.isEmpty(cnsList)) {
                    throw new FrontException(VERSION_NOT_EXISTS);
                }
                contractAddress = cnsList.iterator().next().getAddress();
                log.info("transHandleWithSign cns contractAddress:{}", contractAddress);
            } catch (ContractException e) {
                log.error("queryCnsByNameAndVersion ContractException fail:[]", e);
                throw new FrontException(ConstantCode.CNS_QUERY_FAIL);
            }
        }
        return this.transHandleWithSign(groupId, signUserId, contractAddress, abiStr, funcName, funcParam);
    }


    /**
     * send tx with sign (support precomnpiled contract)
     */
    public Object transHandleWithSign(int groupId, String signUserId,
        String contractAddress, String abiStr, String funcName, List<String> funcParam)
        throws FrontException {
        // check groupId
        Client client = web3ApiService.getWeb3j(groupId);

        String encodeFunction = this.encodeFunction2Str(abiStr, funcName, funcParam);
        String userAddress = keyStoreService.getAddressBySignUserId(signUserId);
        if (StringUtils.isBlank(userAddress)) {
            log.warn("transHandleWithSign this signUser [{}] not record in webase-front", signUserId);
            userAddress = keyStoreService.getCredentialsForQuery().getAddress();
        }

        boolean isTxConstant = this.getABIDefinition(abiStr, funcName).isConstant();
        if (isTxConstant) {
            return this.handleCall(groupId, userAddress, contractAddress, encodeFunction, abiStr, funcName);
        } else {
            return this.handleTransaction(client, signUserId, contractAddress, encodeFunction);
        }

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
     * signMessage to create raw transaction and encode data
     *
     * @param groupId id
     * @param contractAddress info
     * @param data info
     * @return
     */
    public String signMessage(int groupId, Client client, String signUserId, String contractAddress,
            String data) {
        Random r = new SecureRandom();
        BigInteger randomid = new BigInteger(250, r);

        BigInteger blockLimit = client.getBlockLimit();

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

        SignatureResult signData = this.requestSignForSign(encodedTransaction, signUserId);
        byte[] signedMessage = encoderService.encode(rawTransaction, signData);
        return Numeric.toHexString(signedMessage);

    }


    /**
     * send message to node.
     *
     * @param signMsg signMsg
     */
    public TransactionReceipt sendMessage(Client client, String signMsg) {
        TransactionPusherService txPusher = new TransactionPusherService(client);
        TransactionReceipt receipt = txPusher.push(signMsg);
        this.decodeReceipt(receipt);
        return receipt;

    }


    /**
     * send transaction locally
     */
    public Object transHandleLocal(ReqTransHandle req) {
        int groupId = req.getGroupId();
        String abiStr = JsonUtils.objToString(req.getContractAbi());
        String funcName = req.getFuncName();
        List<String> funcParam = req.getFuncParam() == null ? new ArrayList<>() : req.getFuncParam();
        String userAddress = req.getUser();

        String contractAddress = req.getContractAddress();
        if (req.isUseCns()) {
            try {
                List<CnsInfo> cnsList = precompiledService.queryCnsByNameAndVersion(groupId,
                    req.getCnsName(), req.getVersion());
                if (CollectionUtils.isEmpty(cnsList)) {
                    throw new FrontException(VERSION_NOT_EXISTS);
                }
                contractAddress = cnsList.iterator().next().getAddress();
                log.info("transHandleLocal cns contractAddress:{}", contractAddress);
            } catch (ContractException e) {
                log.error("queryCnsByNameAndVersion ContractException fail:[]", e);
                throw new FrontException(ConstantCode.CNS_QUERY_FAIL);
            }
        }

        String encodeFunction = this.encodeFunction2Str(abiStr, funcName, funcParam);

        boolean isTxConstant = this.getABIDefinition(abiStr, funcName).isConstant();
        // get privateKey
        CryptoKeyPair cryptoKeyPair = getCredentials(isTxConstant, userAddress);

        Client client = web3ApiService.getWeb3j(groupId);

        if (isTxConstant) {
            if (StringUtils.isBlank(userAddress)) {
                userAddress = cryptoKeyPair.getAddress();
            }
            return this.handleCall(groupId, userAddress, contractAddress, encodeFunction, abiStr, funcName);
        } else {
            return this.handleTransaction(client, cryptoKeyPair, contractAddress, encodeFunction);
        }
    }


    public TransactionReceipt sendSignedTransaction(String signedStr, Boolean sync, int groupId) {

        Client client = web3ApiService.getWeb3j(groupId);
        if (sync) {
            TransactionReceipt receipt = sendMessage(client, signedStr);
            return receipt;
        } else {
            TransactionPusherService txPusher = new TransactionPusherService(client);
            txPusher.pushOnly(signedStr);
            TransactionReceipt transactionReceipt = new TransactionReceipt();
            transactionReceipt.setTransactionHash(cryptoSuite.hash(signedStr));
            return transactionReceipt;
        }
    }


    public Object sendQueryTransaction(String encodeStr, String contractAddress, String funcName,
            List<Object> contractAbi, int groupId, String userAddress) {

        Client client = web3ApiService.getWeb3j(groupId);
        String callOutput = client
            .call(new Transaction(userAddress, contractAddress,
                        encodeStr))
            .getCallResult().getOutput();

        ABIDefinition abiDefinition = getFunctionAbiDefinition(funcName, JsonUtils.toJSONString(contractAbi));
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

    public SignatureResult signMessageHashByType(String messageHash, CryptoKeyPair cryptoKeyPair, int encryptType) {
        try {
            if (encryptType == CryptoType.SM_TYPE) {
                return new CryptoSuite(CryptoType.SM_TYPE).sign(messageHash, cryptoKeyPair);
            } else {
                return new CryptoSuite(CryptoType.ECDSA_TYPE).sign(messageHash, cryptoKeyPair);
            }
        } catch (Exception e) {
            log.error("signMessageHashByType failed:[]", e);
            throw new FrontException(ConstantCode.GET_MESSAGE_HASH, e.getMessage());
        }
    }

    /**
     * signMessageLocal
     * @return SignatureResult
     */
    public Object signMessageLocal(ReqSignMessageHash req) {
        log.info("transHandle start. ReqSignMessageHash:[{}]", JsonUtils.toJSONString(req));

        CryptoKeyPair cryptoKeyPair = this.getCredentials(false, req.getUser());

        SignatureResult signResult = signMessageHashByType(
                Numeric.cleanHexPrefix(req.getHash()), cryptoKeyPair,
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
     * signMessageLocalExternal
     */
    public Object signMessageLocalExternal(ReqSignMessageHash req) {
        log.info("transHandle start. ReqSignMessageHash:[{}]", JsonUtils.toJSONString(req));
        RspUserInfo rspUserInfo = keyStoreService.getUserInfoWithSign(req.getSignUserId(),true);
        String privateKeyRaw = new String(Base64.getDecoder().decode(rspUserInfo.getPrivateKey()));
        CryptoKeyPair cryptoKeyPair = cryptoSuite.getKeyPairFactory().createKeyPair(privateKeyRaw);
        SignatureResult signResult = signMessageHashByType(
                org.fisco.bcos.sdk.utils.Numeric.cleanHexPrefix(req.getHash()),cryptoKeyPair,
                cryptoSuite.cryptoTypeConfig
        );

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
     * @param user  if user not blank, return signed raw tx, else return encoded tx(not sign)
     * @param isLocal  if false, user is signUserId, else, user is userAddress local
     */
    public String createRawTxEncoded(boolean isLocal, String user,
        int groupId, String contractAddress, List<Object> contractAbi,
        boolean isUseCns, String cnsName, String cnsVersion,
        String funcName, List<String> funcParam) throws Exception {

        if (isUseCns) {
            List<CnsInfo> cnsList = precompiledService.queryCnsByNameAndVersion(groupId, cnsName, cnsVersion);
            if (CollectionUtils.isEmpty(cnsList)) {
                throw new FrontException(VERSION_NOT_EXISTS);
            }
            contractAddress = cnsList.iterator().next().getAddress();
            log.info("transHandleWithSign cns contractAddress:{}", contractAddress);
        }
        // encode function
        String encodeFunction = this.encodeFunction2Str(JsonUtils.objToString(contractAbi), funcName, funcParam);
        // check groupId
        Client client = web3ApiService.getWeb3j(groupId);
        // isLocal:
        // true: user is userAddress locally
        // false: user is signUserId in webase-sign
        return this.convertRawTx2Str(client, contractAddress, encodeFunction, user, isLocal);
    }


    /**
     * get encoded function for /trans/query-transaction
     * @param abiStr
     * @param funcName
     * @param funcParam
     * @return
     */
    public String encodeFunction2Str(String abiStr, String funcName, List<String> funcParam) {

        funcParam = funcParam == null ? new ArrayList<>() : funcParam;
        ABICodec abiCodec = new ABICodec(cryptoSuite, true);
        String encodeFunction;
        try {
            encodeFunction = abiCodec.encodeMethodFromString(abiStr, funcName, funcParam);
        } catch (ABICodecException e) {
            log.error("deployWithSign encode fail:[]", e);
            throw new FrontException(ConstantCode.CONTRACT_TYPE_ENCODED_ERROR.getCode(), e.getMessage());
        }
        log.info("encodeFunction2Str encodeFunction:{}", encodeFunction);
        return encodeFunction;
    }

    /**
     * get encoded raw transaction
     * handleTransByFunction by whether is constant
     * if use signed data to send tx, call @send-signed-transaction api
     * @case1 if @userAddress is blank, return not signed raw tx encoded str
     * @case2 if @userAddress not blank, return signed str
     */
    private String convertRawTx2Str(Client client, String contractAddress,
        String encodeFunction, String user, boolean isLocal) {

        // to encode raw tx
        Pair<String, Integer> chainIdAndGroupId = TransactionProcessorFactory.getChainIdAndGroupId(client);
        TransactionBuilderInterface transactionBuilder = new TransactionBuilderService(client);
        RawTransaction rawTransaction = transactionBuilder.createTransaction(DefaultGasProvider.GAS_PRICE,
            DefaultGasProvider.GAS_LIMIT, contractAddress, encodeFunction,
            BigInteger.ZERO, new BigInteger(chainIdAndGroupId.getLeft()),
            BigInteger.valueOf(chainIdAndGroupId.getRight()), "");

        TransactionEncoderService encoderService = new TransactionEncoderService(cryptoSuite);
        byte[] encodedTransaction = encoderService.encode(rawTransaction, null);
        // if user not null: sign, else, not sign
        if (StringUtils.isBlank(user)) {
            // return unsigned raw tx encoded str
            String unsignedResultStr = Numeric.toHexString(encodedTransaction);
            log.info("createRawTxEncoded unsignedResultStr:{}", unsignedResultStr);
            return unsignedResultStr;
        } else {
            log.info("createRawTxEncoded use key of address [{}] to sign", user);
            // hash encoded, to sign locally
            byte[] hashMessage = cryptoSuite.hash(encodedTransaction);
            String hashMessageStr = Numeric.toHexString(hashMessage);
            log.info("createRawTxEncoded encoded tx of hex str:{}", hashMessageStr);
            // if local, sign locally
            log.info("createRawTxEncoded isLocal:{}", isLocal);
            String signResultStr;
            if (isLocal) {
                CryptoKeyPair cryptoKeyPair = this.getCredentials(false, user);
                SignatureResult userSignResult = signMessageHashByType(hashMessageStr,
                    cryptoKeyPair, cryptoSuite.cryptoTypeConfig);
                // encode again
                byte[] signedMessage = encoderService.encode(rawTransaction, userSignResult);
                signResultStr = Numeric.toHexString(signedMessage);
            } else {
                // sign by webase-sign
                // convert encoded to hex string (no need to hash then toHex)
                hashMessageStr = Numeric.toHexString(encodedTransaction);
                EncodeInfo encodeInfo = new EncodeInfo(user, hashMessageStr);
                String signDataStr = keyStoreService.getSignData(encodeInfo);
                SignatureResult signData = CommonUtils.stringToSignatureData(signDataStr, cryptoSuite.cryptoTypeConfig);
                byte[] signedMessage = encoderService.encode(rawTransaction, signData);
                signResultStr = Numeric.toHexString(signedMessage);
            }
            log.info("createRawTxEncoded signResultStr:{}", signResultStr);
            return signResultStr;
        }
        // trans hash is cryptoSuite.hash(signedStr)
    }

    private ABIDefinition getABIDefinition(String abiStr, String functionName) {
        ABIDefinitionFactory factory = new ABIDefinitionFactory(cryptoSuite);

        ContractABIDefinition contractABIDefinition = factory.loadABI(abiStr);
        List<ABIDefinition> abiDefinitionList = contractABIDefinition.getFunctions()
            .get(functionName);
        if (abiDefinitionList.isEmpty()) {
            throw new FrontException(ConstantCode.IN_FUNCTION_ERROR);
        }
        // abi only contain one function, so get first one
        ABIDefinition abiDefinition = abiDefinitionList.get(0);
        if (abiDefinition.getStateMutability().equals("pure")
            || abiDefinition.getStateMutability().equals("constant")
            || abiDefinition.getStateMutability().equals("view")) {
            abiDefinition.setConstant(true);
        }
        return abiDefinition;
    }

    public Object handleCall(int groupId, String userAddress, String contractAddress,
        String encodedFunction, String abiStr, String funcName) {

        TransactionProcessor transactionProcessor = new TransactionProcessor(
            web3ApiService.getWeb3j(groupId), keyStoreService.getCredentialsForQuery(),
            groupId, Constants.chainId);
        CallOutput callOutput = transactionProcessor
            .executeCall(userAddress, contractAddress, encodedFunction)
            .getCallResult();
        // if error
        if (!RECEIPT_STATUS_0X0.equals(callOutput.getStatus())) {
            Tuple2<Boolean, String> parseResult =
                RevertMessageParser.tryResolveRevertMessage(callOutput.getStatus(), callOutput.getOutput());
            log.error("call contract error:{}", parseResult);
            String parseResultStr = parseResult.getValue1() ? parseResult.getValue2() : "call contract error of status: " + callOutput.getStatus();
            return Collections.singletonList("Call contract return error: " + parseResultStr);
        } else {
            ABICodec abiCodec = new ABICodec(cryptoSuite, true);
            try {
                List<String> res = abiCodec.decodeMethodToString(abiStr, funcName, callOutput.getOutput());
                // list object会出现bytes32乱码（因为是二进制）
                //  List<Object> res = abiCodec.decodeMethodAndGetOutputObject(abiStr, funcName, callOutput.getOutput()).getLeft();
                log.info("call contract res before decode:{}", callOutput.getOutput());
                log.info("call contract res:{}", res);
                return res;
            } catch (ABICodecException e) {
                log.error("handleCall decode call output fail:[]", e);
                throw new FrontException(ConstantCode.CONTRACT_TYPE_DECODED_ERROR);
            }
        }
    }

    /**
     * handle tx locally
     * @param client
     * @param cryptoKeyPair
     * @param contractAddress
     * @param encodeFunction
     * @return
     */
    public TransactionReceipt handleTransaction(Client client, CryptoKeyPair cryptoKeyPair, String contractAddress,
        String encodeFunction) {
        Instant startTime = Instant.now();
        log.info("handleTransaction start startTime:{}", startTime.toEpochMilli());
        // send tx
        TransactionProcessor txProcessor = TransactionProcessorFactory.createTransactionProcessor(client, cryptoKeyPair);
        TransactionReceipt receipt = txProcessor.sendTransactionAndGetReceipt(contractAddress, encodeFunction, cryptoKeyPair);
        // cover null message through statusCode
        this.decodeReceipt(receipt);
        log.info("execTransaction end  useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
        return receipt;
    }


    /**
     * handle tx with sign
     * @param client
     * @param signUserId
     * @param contractAddress
     * @param encodeFunction
     * @return
     */
    public TransactionReceipt handleTransaction(Client client, String signUserId, String contractAddress, String encodeFunction) {
        log.debug("handleTransaction signUserId:{},contractAddress:{},encodeFunction:{}",signUserId,contractAddress, encodeFunction);
        // raw tx
        Pair<String, Integer> chainIdAndGroupId = TransactionProcessorFactory.getChainIdAndGroupId(client);
        TransactionBuilderInterface transactionBuilder = new TransactionBuilderService(client);
        RawTransaction rawTransaction = transactionBuilder.createTransaction(DefaultGasProvider.GAS_PRICE,
            DefaultGasProvider.GAS_LIMIT, contractAddress, encodeFunction,
            BigInteger.ZERO, new BigInteger(chainIdAndGroupId.getLeft()),
            BigInteger.valueOf(chainIdAndGroupId.getRight()), "");
        // encode
        TransactionEncoderInterface transactionEncoder = new TransactionEncoderService(cryptoSuite);
        byte[] encodedTransaction = transactionEncoder.encode(rawTransaction, null);
        // sign
        SignatureResult signResult = this.requestSignForSign(encodedTransaction, signUserId);
        byte[] signedMessage = transactionEncoder.encode(rawTransaction, signResult);
        String signedMessageStr = Numeric.toHexString(signedMessage);

        Instant nodeStartTime = Instant.now();
        // send transaction
        TransactionReceipt receipt = sendMessage(client, signedMessageStr);
        log.info("***node cost time***: {}",
            Duration.between(nodeStartTime, Instant.now()).toMillis());
        return receipt;

    }

    /**
     * sign by
     * @param encodedTransaction
     * @param signUserId
     * @return
     */
    public SignatureResult requestSignForSign(byte[] encodedTransaction, String signUserId) {
        String encodedDataStr = Numeric.toHexString(encodedTransaction);
        EncodeInfo encodeInfo = new EncodeInfo();
        encodeInfo.setSignUserId(signUserId);
        encodeInfo.setEncodedDataStr(encodedDataStr);

        Instant startTime = Instant.now();
        String signDataStr = keyStoreService.getSignData(encodeInfo);
        log.info("get requestSignForSign cost time: {}",
            Duration.between(startTime, Instant.now()).toMillis());
        SignatureResult signData = CommonUtils.stringToSignatureData(signDataStr, cryptoSuite.cryptoTypeConfig);
        return signData;
    }

    public void decodeReceipt(TransactionReceipt receipt) {
        // decode receipt
        TransactionDecoderService txDecoder = new TransactionDecoderService(cryptoSuite);
        String receiptMsg = txDecoder.decodeReceiptStatus(receipt).getReceiptMessages();
        receipt.setMessage(receiptMsg);
        CommonUtils.processReceiptHexNumber(receipt);
    }


//    private void validFuncParam(String contractAbiStr, String funcName, List<String> funcParam) {
//        ABIDefinition abiDefinition = this.getABIDefinition(contractAbiStr, funcName);
//        List<NamedType> inputTypeList = abiDefinition.getInputs();
//        if (inputTypeList.size() != funcParam.size()) {
//            log.error("validFuncParam param not match");
//            throw new FrontException(ConstantCode.FUNC_PARAM_SIZE_NOT_MATCH);
//        }
//        for (int i = 0; i < inputTypeList.size(); i++) {
//            String type = inputTypeList.get(i).getType();
//            if (type.startsWith("bytes")) {
//                if (type.contains("[][]")) {
//                    // todo bytes[][]
//                    log.warn("validFuncParam param, not support bytes 2d array or more");
//                    throw new FrontException(ConstantCode.FUNC_PARAM_BYTES_NOT_SUPPORT_HIGH_D);
//                }
//                // if not bytes[], bytes or bytesN
//                if (!type.endsWith("[]")) {
//                    // update funcParam
//                    String bytesHexStr = funcParam.get(i);
//                    byte[] inputArray = Numeric.hexStringToByteArray(bytesHexStr);
//                    // bytesN: bytes1, bytes32 etc.
//                    if (type.length() > "bytes".length()) {
//                        int bytesNLength = Integer.parseInt(type.substring("bytes".length()));
//                        if (inputArray.length != bytesNLength) {
//                            log.error("validFuncParam param of bytesN size not match");
//                            throw new FrontException(ConstantCode.FUNC_PARAM_BYTES_SIZE_NOT_MATCH);
//                        }
//                    }
//                    // replace hexString with array
//                    funcParam.set(i, bytesHexStr);
//                } else {
//                    // if bytes[] or bytes32[]
//                    List<String> hexStrArray = (List<String>) (funcParam.get(i));
//                    List<byte[]> bytesArray = new ArrayList<>(hexStrArray.size());
//                    for (int j = 0; j < hexStrArray.size(); j++) {
//                        String bytesHexStr = hexStrArray.get(j);
//                        byte[] inputArray = Numeric.hexStringToByteArray(bytesHexStr);
//                        // check: bytesN: bytes1, bytes32 etc.
//                        if (type.length() > "bytes[]".length()) {
//                            // bytes32[] => 32[]
//                            String temp = type.substring("bytes".length());
//                            // 32[] => 32
//                            int bytesNLength = Integer
//                                .parseInt(temp.substring(0, temp.length() - 2));
//                            if (inputArray.length != bytesNLength) {
//                                log.error("validFuncParam param of bytesN size not match");
//                                throw new FrontException(
//                                    ConstantCode.FUNC_PARAM_BYTES_SIZE_NOT_MATCH);
//                            }
//                        }
//                        bytesArray.add(inputArray);
//                    }
//                    // replace hexString with array
//                    funcParam.set(i, bytesArray);
//                }
//            }
//        }
//    }

//    /**
//     * parse bytes, bytesN, bytesN[], bytes[] from base64 string to hex string
//     * @param abiStr
//     * @param funcName
//     * @param outputValues
//     */
//    private void handleFuncOutput(String abiStr, String funcName, List<String> outputValues) {
//        ABIDefinition abiDefinition = this.getABIDefinition(abiStr, funcName);
//        ABICodecJsonWrapper jsonWrapper = new ABICodecJsonWrapper();
//        List<NamedType> outputTypeList = abiDefinition.getOutputs();
//        for (int i = 0; i < outputTypeList.size(); i++) {
//            String type = outputTypeList.get(i).getType();
//            if (type.startsWith("bytes")) {
//                if (type.contains("[][]")) { // todo bytes[][]
//                    log.warn("validFuncParam param, not support bytes 2d array or more");
//                    continue;
//                }
//                // if not bytes[], bytes or bytesN
//                if (!type.endsWith("[]")) {
//                    // update funcParam
//                    String bytesBase64Str = outputValues.get(i);
//                    if (bytesBase64Str.startsWith("base64://")) {
//                        bytesBase64Str = bytesBase64Str.substring("base64://".length());
//                    }
//                    byte[] inputArray = Base64.getDecoder().decode(bytesBase64Str);
//                    // replace hexString with array
//                    outputValues.set(i, Numeric.toHexString(inputArray));
//                } else {
//                    // if bytes[] or bytes32[]
//                    List<String> base64StrArray = JsonUtils.toJavaObject(outputValues.get(i), List.class);
//                    List<String> bytesArray = new ArrayList<>(base64StrArray.size());
//                    for (int j = 0; j < base64StrArray.size(); j++) {
//                        String bytesBase64Str = base64StrArray.get(j);
//                        if (bytesBase64Str.startsWith("base64://")) {
//                            bytesBase64Str = bytesBase64Str.substring("base64://".length());
//                        }
//                        byte[] inputArray = Base64.getDecoder().decode(bytesBase64Str);
//                        bytesArray.add(Numeric.toHexString(inputArray));
//                    }
//                    // replace hexString with array
//                    outputValues.set(i, JsonUtils.objToString(bytesArray));
//                }
//            }
//        }
//    }

}


