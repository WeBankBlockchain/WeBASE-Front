/**
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
package com.webank.webase.front.transaction;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.contract.CommonContract;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.util.AbiTypes;
import com.webank.webase.front.util.ContractAbiUtil;
import com.webank.webase.front.util.ContractTypeUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tx.exceptions.ContractCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.webank.webase.front.base.ConstantCode.GROUPID_NOT_EXIST;


/**
 * TransService.
 */
@Slf4j
@Service
public class TransService {

    @Autowired
    private Map<Integer, Web3j> web3jMap;
    @Autowired
    private Map<Integer, CnsService> cnsServiceMap;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private Map<String, String> cnsMap;


    /**
     * transHandle.
     *
     * @param req request
     */
    public Object transHandle(ReqTransHandle req) throws Exception {

        boolean ifExisted;
        // Check if contractAbi existed
        if (req.getVersion() != null) {
            ifExisted = ContractAbiUtil
                .ifContractAbiExisted(req.getContractName(), req.getVersion());
        } else {
            ifExisted = ContractAbiUtil
                .ifContractAbiExisted(req.getContractName(), req.getContractAddress().substring(2));
        }

        if (!ifExisted) {
            // check and save abi
            checkAndSaveAbiFromCns(req);
        }

        Object baseRsp = dealWithtrans(req);
        log.info("transHandle end. name:{} func:{} baseRsp:{}", req.getContractName(),
            req.getFuncName(), JSON.toJSONString(baseRsp));
        return baseRsp;
    }

    /**
     * checkAndSaveAbiFromCns.
     *
     * @param req request
     */
    public void checkAndSaveAbiFromCns(ReqTransHandle req) throws Exception {
        List<CnsInfo> cnsInfoList = null;
         CnsService cnsService = cnsServiceMap.get(req.getGroupId());
         if(cnsService==null) {
             throw new FrontException(GROUPID_NOT_EXIST);
         }
        if(req.getVersion()!=null) {
             cnsInfoList = cnsService
                    .queryCnsByNameAndVersion(req.getContractName(), req.getVersion());
        }
        else {
            cnsInfoList = cnsService.queryCnsByNameAndVersion(req.getContractName(),req.getContractAddress().substring(2));
        }
        // transaction request
        if(cnsInfoList==null) {
            throw new FrontException("can not get cns information from chain!");
        }
        log.info("cnsinfo" + cnsInfoList.get(0).getAddress());
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        List abiDefinitionList = objectMapper.readValue(cnsInfoList.get(0).getAbi(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, AbiDefinition.class));
        // check if contract has been deployed
        if (StringUtils.isBlank(cnsInfoList.get(0).getAbi())) {
            throw new FrontException(ConstantCode.CONTRACT_NOT_DEPLOY_ERROR);
        }

        // save abi
        ContractAbiUtil
            .setContractWithAbi(req.getContractName(), req.getVersion() == null ? req.getContractAddress().substring(2):req.getVersion() , abiDefinitionList, true);
    }

    /**
     * transaction request.
     *
     * @param req request
     */
    public Object dealWithtrans(ReqTransHandle req) throws FrontException {
        log.info("dealWithtrans start. ReqTransHandle:[{}]", JSON.toJSONString(req));
        Object result = null;
        String contractName = req.getContractName();
        String version = req.getVersion();
        String address = req.getContractAddress();
        String funcName = req.getFuncName();
        List<Object> params = req.getFuncParam();
        int groupId = req.getGroupId();
        if (StringUtils.isBlank(version) && StringUtils.isNotBlank(address)) {
            version = address.substring(2);
        }

        // if function is constant
        String constant = ContractAbiUtil.ifConstantFunc(contractName, funcName, version);
        if (constant == null) {
            log.warn("dealWithtrans fail. contract name:{} func:{} version:{} is not existed", contractName,
                funcName,version);
            throw new FrontException(ConstantCode.IN_FUNCTION_ERROR);
        }

        // inputs format
        List<String> funcInputTypes = ContractAbiUtil
            .getFuncInputType(contractName, funcName, version);
        if (funcInputTypes == null || funcInputTypes.size() != params.size()) {
            log.warn("dealWithtrans fail. funcInputTypes:{}, params:{}", funcInputTypes, params);
            throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
        }
        List<Type> finalInputs = inputFormat(funcInputTypes, params);

        // outputs format
        List<String> funOutputTypes = ContractAbiUtil
            .getFuncOutputType(contractName, funcName, version);
        List<TypeReference<?>> finalOutputs = outputFormat(funOutputTypes);

        // get privateKey
        Credentials credentials = keyStoreService.getCredentials(req.getUser(), req.getUseAes());

        // contract load
        CommonContract commonContract;
        Web3j web3j = web3jMap.get(groupId);
        if(web3j == null ) {
            new FrontException(GROUPID_NOT_EXIST);
        }
        if(address == null ) {
            address = cnsMap.get(contractName+":"+version);
        }

        if (address != null) {
            commonContract = CommonContract.load(address, web3j,
                    credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT);

        } else {
            commonContract = CommonContract
                    .loadByName(contractName + Constants.SYMPOL + version, web3j,
                            credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT);
        }
        // request
        Function function = new Function(funcName, finalInputs, finalOutputs);
        if ("true".equals(constant)) {
            result = execCall(funOutputTypes, function, commonContract);
        } else {
            result = execTransaction(function, commonContract);
        }
        return result;
    }

    /**
     * execCall.
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
                result = ethCallResultParse(funOutputTypes, typeList);
            }
            return result;
        } catch (IOException | ContractCallException e) {
            log.error("execCall failed.", e);
            throw new FrontException(ConstantCode.TRANSACTION_QUERY_FAILED.getCode(),
                e.getMessage());
        }
    }

    /**
     * execTransaction.
     *
     * @param function function
     * @param commonContract contract
     */
    public static TransactionReceipt execTransaction(Function function,
        CommonContract commonContract) throws FrontException {
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = commonContract.execTransaction(function);
        } catch (IOException | TransactionException | ContractCallException e) {
            log.error("execTransaction failed.", e);
            throw new FrontException(ConstantCode.TRANSACTION_SEND_FAILED.getCode(),
                e.getMessage());
        }
        return transactionReceipt;
    }

    /**
     * input params format.
     *
     * @param funcInputTypes list
     * @param params list
     */
    // todo 拼接动态数组
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
                inputType = AbiTypes.getType(
                    funcInputTypes.get(i).substring(0, funcInputTypes.get(i).indexOf("[")));

                for (int j = 0; j < arrList.size(); j++) {
                    input = ContractTypeUtil.parseByType(
                        funcInputTypes.get(i).substring(0, funcInputTypes.get(i).indexOf("[")),
                        arrList.get(j).toString());
                    arrParams
                        .add(ContractTypeUtil.generateClassFromInput(input.toString(), inputType));
                }
                finalInputs.add(new DynamicArray<>(arrParams));
            } else {
                inputType = AbiTypes.getType(funcInputTypes.get(i));
                input = ContractTypeUtil.parseByType(funcInputTypes.get(i),
                    params.get(i).toString());
                finalInputs
                    .add(ContractTypeUtil.generateClassFromInput(input.toString(), inputType));
            }
        }
        return finalInputs;
    }

    /**
     * output params format.
     *
     * @param funOutputTypes list
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
                outputType = AbiTypes.getType(funOutputTypes.get(i));
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
     */
    static Object ethCallResultParse(List<String> funOutputTypes, List<Type> typeList)
        throws FrontException {
        if (funOutputTypes.size() == typeList.size()) {
            List<Object> ressult = new ArrayList<>();
            for (int i = 0; i < funOutputTypes.size(); i++) {
                Class<?> outputType = null;
                Object value = null;
                if (funOutputTypes.get(i).indexOf("[") != -1
                    && funOutputTypes.get(i).indexOf("]") != -1) {
                    List<Object> values = new ArrayList<>();
                    List<Type> results = (List<Type>) typeList.get(i).getValue();
                    for (int j = 0; j < results.size(); j++) {
                        outputType = AbiTypes.getType(funOutputTypes.get(i).substring(0,
                            funOutputTypes.get(i).indexOf("[")));
                        value = ContractTypeUtil.decodeResult(results.get(j), outputType);
                        values.add(value);
                    }
                    ressult.add(values);
                } else {
                    outputType = AbiTypes.getType(funOutputTypes.get(i));
                    value = ContractTypeUtil.decodeResult(typeList.get(i), outputType);
                    ressult.add(value);
                }
            }
            return JSON.parse(JSON.toJSONString(ressult));
        }
        throw new FrontException("output parameter not match");

    }
}
