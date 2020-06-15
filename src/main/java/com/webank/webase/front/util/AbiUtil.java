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

package com.webank.webase.front.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.webase.front.base.code.ConstantCode;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.abi.EventValues;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Event;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition.NamedType;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.txdecode.ConstantProperties;
import com.webank.webase.front.base.exception.FrontException;

/**
 * ContractAbiUtil.
 * format abi types from String
 */
@Slf4j
public class AbiUtil {

    /**
     * get constructor abi info.
     * 
     * @param contractAbi contractAbi
     * @return
     */
    public static AbiDefinition getAbiDefinition(String contractAbi) {
        List<AbiDefinition> abiArr = JsonUtils.toJavaObjectList(contractAbi, AbiDefinition.class);
        AbiDefinition result = null;
        for (AbiDefinition abiDefinition : abiArr) {
            if (ConstantProperties.TYPE_CONSTRUCTOR.equals(abiDefinition.getType())) {
                result = abiDefinition;
                break;
            }
        }
        return result;
    }

    /**
     * get function abi info.
     * 
     * @param name name
     * @param contractAbi contractAbi
     * @return
     */
    public static AbiDefinition getAbiDefinition(String name, String contractAbi) {
        List<AbiDefinition> abiArr = JsonUtils.toJavaObjectList(contractAbi, AbiDefinition.class);
        AbiDefinition result = null;
        for (AbiDefinition abiDefinition : abiArr) {
            if (ConstantProperties.TYPE_FUNCTION.equals(abiDefinition.getType())
                    && name.equals(abiDefinition.getName())) {
                result = abiDefinition;
                break;
            } 
        }
        return result;
    }
    
    /**
     * get event abi info.
     * 
     * @param contractAbi contractAbi
     * @return
     */
    public static List<AbiDefinition> getEventAbiDefinitions(String contractAbi) {
        List<AbiDefinition> abiArr = JsonUtils.toJavaObjectList(contractAbi, AbiDefinition.class);
        List<AbiDefinition> result = new ArrayList<>();
        for (AbiDefinition abiDefinition : abiArr) {
            if (ConstantProperties.TYPE_EVENT.equals(abiDefinition.getType())) {
                result.add(abiDefinition);
            } 
        }
        return result;
    }

    /**
     * getFuncInputType.
     * 
     * @param abiDefinition abiDefinition
     * @return
     */
    public static List<String> getFuncInputType(AbiDefinition abiDefinition) {
        List<String> inputList = new ArrayList<>();
        if (abiDefinition != null) {
            List<NamedType> inputs = abiDefinition.getInputs();
            for (NamedType input : inputs) {
                inputList.add(input.getType());
            }
        }
        return inputList;
    }

    /**
     * getFuncOutputType.
     * 
     * @param abiDefinition abiDefinition
     * @return
     */
    public static List<String> getFuncOutputType(AbiDefinition abiDefinition) {
        List<String> outputList = new ArrayList<>();
        List<NamedType> outputs = abiDefinition.getOutputs();
        for (NamedType output : outputs) {
            outputList.add(output.getType());
        }
        return outputList;
    }

    /**
     * input parameter format.
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
            if (funcInputTypes.get(i).contains("[")
                    && funcInputTypes.get(i).contains("]")) {
                List<Object> arrList;
                try {
                    arrList = (List<Object>) params.get(i);
                } catch (ClassCastException e) {
                    log.error("params of index {} parse List error: {}", i, params.get(i));
                    throw new FrontException(ConstantCode.PARAM_ERROR);
                }
                List<Type> arrParams = new ArrayList<>();
                for (int j = 0; j < arrList.size(); j++) {
                    inputType = AbiTypes.getType(
                            funcInputTypes.get(i).substring(0, funcInputTypes.get(i).indexOf("[")));
                    input = ContractTypeUtil.parseByType(
                            funcInputTypes.get(i).substring(0, funcInputTypes.get(i).indexOf("[")),
                            arrList.get(j).toString());
                    arrParams.add(ContractTypeUtil.generateClassFromInput(input.toString(), inputType));
                }
                finalInputs.add(new DynamicArray<>(arrParams));
            } else {
                inputType = AbiTypes.getType(funcInputTypes.get(i));
                input = ContractTypeUtil.parseByType(funcInputTypes.get(i),
                        params.get(i).toString());
                finalInputs.add(ContractTypeUtil.generateClassFromInput(input.toString(), inputType));
            }
        }
        return finalInputs;
    }

    /**
     * output parameter format.
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
            if (funOutputTypes.get(i).contains("[")
                    && funOutputTypes.get(i).contains("]")) {
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
     * @return
     */
    public static Object callResultParse(List<String> funOutputTypes, List<Type> typeList)
        throws FrontException {
        if (funOutputTypes.size() == typeList.size()) {
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < funOutputTypes.size(); i++) {
                Class<? extends Type> outputType = null;
                Object value = null;
                if (funOutputTypes.get(i).contains("[")
                        && funOutputTypes.get(i).contains("]")) {
                    List<Object> values = new ArrayList<>();
                    List<Type> results = (List<Type>) typeList.get(i).getValue();
                    for (int j = 0; j < results.size(); j++) {
                        outputType = AbiTypes.getType(funOutputTypes.get(i).substring(0,
                                funOutputTypes.get(i).indexOf("[")));
                        value = ContractTypeUtil.decodeResult(results.get(j), outputType);
                        values.add(value);
                    }
                    result.add(values);
                } else {
                    outputType = AbiTypes.getType(funOutputTypes.get(i));
                    value = ContractTypeUtil.decodeResult(typeList.get(i), outputType);
                    result.add(value);
                }
            }
            return JsonUtils.toJavaObject(JsonUtils.toJSONString(result), Object.class);
        }
        throw new FrontException("output parameter not match");
    }
    
    /**
     * receiptParse.
     * 
     * @param receipt info
     * @param abiList info
     * @return
     */
    public static Object receiptParse(TransactionReceipt receipt, List<AbiDefinition> abiList)
        throws FrontException {
        Map<String, Object> resultMap = new HashMap<>();
        List<Log> logList = receipt.getLogs();
        for (AbiDefinition abiDefinition : abiList) {
            String eventName = abiDefinition.getName();
            List<String> funcInputTypes = getFuncInputType(abiDefinition);
            List<TypeReference<?>> finalOutputs = outputFormat(funcInputTypes);
            Event event = new Event(eventName,finalOutputs);
            Object result = null;
            for (Log logInfo : logList) {
                EventValues eventValues = Contract.staticExtractEventParameters(event, logInfo);
                if (eventValues != null) {
                    result = callResultParse(funcInputTypes, eventValues.getNonIndexedValues());
                    break;
                }
            }
            if (result != null) {
                resultMap.put(eventName, result);
            }
        }
        return resultMap;
    }

    /**
     * check abi valid
     * @param contractAbi
     */
    public static List<AbiDefinition> checkAbi(String contractAbi) {
        try {
            List<AbiDefinition> abiArr = JsonUtils.toJavaObjectList(contractAbi, AbiDefinition.class);
            return abiArr;
        } catch (Exception ex) {
            throw new FrontException(ConstantCode.PARAM_FAIL_ABI_INVALID);
        }
    }
}
