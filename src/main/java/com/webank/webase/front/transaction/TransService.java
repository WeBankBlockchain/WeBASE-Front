package com.webank.webase.front.transaction;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.contract.CommonContract;
import com.webank.webase.front.contract.KeyStoreInfo;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;
import com.webank.webase.front.util.ContractTypeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.DynamicArray;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
 * TransService.
 */
@Slf4j
@Service
public class TransService {
    @Autowired
    Web3j web3j;
    @Autowired
    Constants constants;
    @Autowired
    RestTemplate restTemplate;

    Map<Integer, KeyStoreInfo> keyMap = new HashMap<>();

    /**
     * transHandle.
     * 
     * @param req request
     * @return
     */
    public BaseResponse transHandle(ReqTransHandle req) throws FrontException {

        // Check if contractAbi existed
        boolean ifExisted =
                ContractAbiUtil.ifContractAbiExisted(req.getContractName(), req.getVersion());
        if (!ifExisted) {
            // check and save abi
            checkAndSaveAbiFromCns(req);
        }

        BaseResponse baseRsp = transRequest(req);
        log.info("transHandle end. name:{} func:{} baseRsp:{}", req.getContractName(),
                req.getFuncName(), JSON.toJSONString(baseRsp));
        return baseRsp;
    }

    /**
     * checkAndSaveAbiFromCns.
     * 
     * @param req request
     */
    public void checkAndSaveAbiFromCns(ReqTransHandle req) throws FrontException {
        // cns Params
        List<Object> cnsParams = new ArrayList<>();
        cnsParams.add(req.getContractName() + Constants.DIAGONAL + req.getVersion());

        // transaction Params
        ReqTransHandle reqTransHandle = new ReqTransHandle();
        reqTransHandle.setUserId(req.getUserId());
        reqTransHandle.setContractName(Constants.CNS_CONTRAC_TNAME);
        reqTransHandle.setVersion("");
        reqTransHandle.setFuncName(Constants.CNS_FUNCTION_GETABI);
        reqTransHandle.setFuncParam(cnsParams);

        // transaction request
        BaseResponse baseRsp = transRequest(reqTransHandle);
        if (baseRsp.getCode() != 0) {
            throw new FrontException(ConstantCode.ABI_GET_ERROR);
        }
        String abiInfo = (String) JSONArray.parseArray(baseRsp.getData().toString()).get(0);

        // check if contract has been deployed
        if (StringUtils.isBlank(abiInfo)) {
            throw new FrontException(ConstantCode.CONTRACT_NOT_DEPLOY_ERROR);
        }

        // save abi
        ContractAbiUtil.setContractWithAbi(req.getContractName(), req.getVersion(),
                JSON.parseArray(abiInfo), true);
    }

    /**
     * transaction request.
     * 
     * @param req request
     * @return
     */
    public BaseResponse transRequest(ReqTransHandle req) throws FrontException {
        log.info("transRequest start. ReqTransHandle:[{}]", JSON.toJSONString(req));
        BaseResponse baseRsp = new BaseResponse(ConstantCode.RET_SUCCEED);

        int userId = req.getUserId();
        String contractName = req.getContractName();
        String version = req.getVersion();
        String funcName = req.getFuncName();
        List<Object> params = req.getFuncParam();

        // if function is constant
        String constant = ContractAbiUtil.ifConstantFunc(contractName, funcName, version);
        if (constant == null) {
            log.warn("transRequest fail. contract name:{} func:{} is not existed", contractName,
                    funcName);
            throw new FrontException(ConstantCode.IN_FUNCTION_ERROR);
        }

        // inputs format
        List<String> funcInputTypes =
                ContractAbiUtil.getFuncInputType(contractName, funcName, version);
        if (funcInputTypes == null || funcInputTypes.size() != params.size()) {
            log.warn("transRequest fail. funcInputTypes:{}, params:{}", funcInputTypes, params);
            throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
        }
        List<Type> finalInputs = inputFormat(funcInputTypes, params);

        // outputs format
        List<String> funOutputTypes =
                ContractAbiUtil.getFuncOutputType(contractName, funcName, version);
        List<TypeReference<?>> finalOutputs = outputFormat(funOutputTypes);

        // get privateKey
        String privateKey = Optional.ofNullable(getPrivateKey(userId))
                .map(info -> info.getPrivateKey()).orElse(null);
        if (privateKey == null) {
            log.warn("transRequest userId:{} privateKey is null", userId);
            throw new FrontException(ConstantCode.PRIVATEKEY_IS_NULL);
        }
        Credentials credentials = Credentials.create(privateKey);

        // contract load
        CommonContract commonContract =
                CommonContract.loadByName(contractName + Constants.SYMPOL + version, web3j,
                        credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT);

        // request
        Function function = new Function(funcName, finalInputs, finalOutputs);
        if ("true".equals(constant)) {
            baseRsp = execCall(funOutputTypes, function, commonContract, baseRsp);
        } else {
            baseRsp = execTransaction(function, commonContract, baseRsp);
        }
        return baseRsp;
    }

    /**
     * execCall.
     * 
     * @param funOutputTypes list
     * @param function function
     * @param commonContract contract
     * @param baseRsp response
     * @return
     */
    static BaseResponse execCall(List<String> funOutputTypes, Function function,
            CommonContract commonContract, BaseResponse baseRsp) throws FrontException {
        try {
            List<Type> typeList = commonContract.execCall(function).get();
            if (typeList.size() > 0) {
                baseRsp = ethCallResultParse(funOutputTypes, typeList, baseRsp);
            } else {
                baseRsp.setData(typeList);
            }
            return baseRsp;
        } catch (InterruptedException | ExecutionException e) {
            log.error("execCall failed.");
            throw new FrontException(ConstantCode.TRANSACTION_QUERY_FAILED);
        }
    }

    /**
     * execTransaction.
     * 
     * @param function function
     * @param commonContract contract
     * @param baseRsp response
     * @return
     */
    static BaseResponse execTransaction(Function function, CommonContract commonContract,
            BaseResponse baseRsp) throws FrontException {
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = commonContract.execTransaction(function).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("execTransaction failed.");
            throw new FrontException(ConstantCode.TRANSACTION_SEND_FAILED);
        }
        baseRsp.setData(JSON.parse(JSON.toJSONString(transactionReceipt)));
        return baseRsp;
    }

    /**
     * input params format.
     * 
     * @param funcInputTypes list
     * @param params list
     * @return
     */
    public static List<Type> inputFormat(List<String> funcInputTypes, List<Object> params)
            throws FrontException {
        List<Type> finalInputs = new ArrayList<>();
        for (int i = 0; i < funcInputTypes.size(); i++) {
            Class<? extends Type> inputType = null;
            Object input = null;
            if (funcInputTypes.get(i).indexOf("[") != -1
                    && funcInputTypes.get(i).indexOf("]") != -1) {
                List<Object> arrList =
                        new ArrayList<>(Arrays.asList(params.get(i).toString().split(",")));
                List<Type> arrParams = new ArrayList<>();
                for (int j = 0; j < arrList.size(); j++) {
                    inputType = ContractTypeUtil.getType(
                            funcInputTypes.get(i).substring(0, funcInputTypes.get(i).indexOf("[")));
                    input = ContractTypeUtil.parseByType(
                            funcInputTypes.get(i).substring(0, funcInputTypes.get(i).indexOf("[")),
                            arrList.get(j).toString());
                    arrParams.add(ContractTypeUtil.encodeString(input.toString(), inputType));
                }
                finalInputs.add(new DynamicArray<>(arrParams));
            } else {
                inputType = ContractTypeUtil.getType(funcInputTypes.get(i));
                input = ContractTypeUtil.parseByType(funcInputTypes.get(i),
                        params.get(i).toString());
                finalInputs.add(ContractTypeUtil.encodeString(input.toString(), inputType));
            }
        }
        return finalInputs;
    }

    /**
     * output params format.
     * 
     * @param funOutputTypes list
     * @return
     */
    public static List<TypeReference<?>> outputFormat(List<String> funOutputTypes)
            throws FrontException {
        List<TypeReference<?>> finalOutputs = new ArrayList<>();
        for (int i = 0; i < funOutputTypes.size(); i++) {
            Class<? extends Type> outputType = null;
            TypeReference<?> typeReference = null;
            if (funOutputTypes.get(i).indexOf("[") != -1
                    && funOutputTypes.get(i).indexOf("]") != -1) {
                typeReference = ContractTypeUtil.getArrayType(
                        funOutputTypes.get(i).substring(0, funOutputTypes.get(i).indexOf("[")));
            } else {
                outputType = ContractTypeUtil.getType(funOutputTypes.get(i));
                typeReference = TypeReference.create(outputType);
            }
            finalOutputs.add(typeReference);
        }
        return finalOutputs;
    }

    /**
     * ethCall Result Parse.
     * 
     * @param funOutputTypes list
     * @param typeList list
     * @param baseRsp response
     * @return
     */
    static BaseResponse ethCallResultParse(List<String> funOutputTypes, List<Type> typeList,
            BaseResponse baseRsp) throws FrontException {
        if (funOutputTypes.size() == typeList.size()) {
            List<Object> ressult = new ArrayList<>();
            for (int i = 0; i < funOutputTypes.size(); i++) {
                Class<? extends Type> outputType = null;
                Object value = null;
                if (funOutputTypes.get(i).indexOf("[") != -1
                        && funOutputTypes.get(i).indexOf("]") != -1) {
                    List<Object> values = new ArrayList<>();
                    List<Type> results = (List<Type>) typeList.get(i).getValue();
                    for (int j = 0; j < results.size(); j++) {
                        outputType = ContractTypeUtil.getType(funOutputTypes.get(i).substring(0,
                                funOutputTypes.get(i).indexOf("[")));
                        value = ContractTypeUtil.decodeResult(results.get(j), outputType);
                        values.add(value);
                    }
                    ressult.add(values);
                } else {
                    outputType = ContractTypeUtil.getType(funOutputTypes.get(i));
                    value = ContractTypeUtil.decodeResult(typeList.get(i), outputType);
                    ressult.add(value);
                }
            }
            baseRsp.setData(JSON.parse(JSON.toJSONString(ressult)));
        }
        return baseRsp;
    }

    /**
     * get PrivateKey.
     * 
     * @param userId userId
     * @return
     */
    public KeyStoreInfo getPrivateKey(int userId) {
        if (this.keyMap.get(userId) != null && this.keyMap.get(userId).getPrivateKey() != null) {
            return this.keyMap.get(userId);
        }

        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        String[] ipPortArr = constants.getMgrIpPorts().split(",");
        for (String ipPort : ipPortArr) {
            try {
                String url = String.format(Constants.MGR_PRIVATE_KEY_URI, ipPort, userId);
                log.info("getPrivateKey url:{}", url);
                BaseResponse response = restTemplate.getForObject(url, BaseResponse.class);
                log.info("getPrivateKey response:{}", JSON.toJSONString(response));
                if (response.getCode() == 0) {
                    keyStoreInfo =
                            CommonUtils.object2JavaBean(response.getData(), KeyStoreInfo.class);
                    break;
                }
            } catch (Exception e) {
                log.warn("userId:{} getPrivateKey from ipPort:{} exception", userId, ipPort, e);
                continue;
            }
        }
        this.keyMap.put(userId, keyStoreInfo);
        return keyStoreInfo;
    }
}
