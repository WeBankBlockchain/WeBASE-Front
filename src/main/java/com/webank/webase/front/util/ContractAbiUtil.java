/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.codec.datatypes.DynamicArray;
import org.fisco.bcos.sdk.codec.datatypes.StaticArray;
import org.fisco.bcos.sdk.codec.datatypes.Type;
import org.fisco.bcos.sdk.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinition.NamedType;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;

/**
 * ContractAbiUtil.
 */
@Slf4j
public class ContractAbiUtil {

    private ContractAbiUtil() {
        throw new IllegalStateException("Utility class");
    }

    protected static HashMap<String, List<VersionEvent>> contractEventMap = new HashMap<>();
    private static final String REGEX = "(\\w+)(?:\\[(.*?)\\])(?:\\[(.*?)\\])?";
    private static final Pattern PATTERN = Pattern.compile(REGEX);
    public static final String STATE_MUTABILITY_VIEW = "view";
    public static final String STATE_MUTABILITY_PURE = "pure";

    @Data
    public static class VersionEvent {

        String version;
        HashMap<String, List<Class<? extends Type>>> events;
        HashMap<String, Boolean> functions;// constant or not
        HashMap<String, List<String>> funcInputs;
        HashMap<String, List<String>> funcOutputs;

        /**
         * VersionEvent.
         *
         * @param version     contract version
         * @param events      events
         * @param functions   functions // constant or not
         * @param funcInputs  funcInputs
         * @param funcOutputs funcOutputs
         */
        public VersionEvent(String version, HashMap<String, List<Class<? extends Type>>> events,
                            HashMap<String, Boolean> functions, HashMap<String, List<String>> funcInputs,
                            HashMap<String, List<String>> funcOutputs) {
            this.version = version;
            this.events = events;
            this.functions = functions;
            this.funcInputs = funcInputs;
            this.funcOutputs = funcOutputs;
        }
    }

    static {
        try {
            initAllContractAbi();
        } catch (Exception ex) {
            log.error("initAllContractAbi fail");
        }
    }

    /**
     * init from config abi dir.
     */
    public static void initAllContractAbi() throws Exception {
        File f = new File(Constants.ABI_DIR);

        File[] files = f.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String[] arr = file.getName().split(Constants.SEP);
            String version = "";
            if (arr.length >= 2) {
                version = arr[1];
            }
            List<ABIDefinition> abiList = loadContractDefinition(file);
            setContractWithAbi(arr[0], version, abiList, false);
        }
    }

    /**
     * loadContractDefinition.
     *
     * @param abiFile file
     */
    public static List<ABIDefinition> loadContractDefinition(File abiFile) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        ABIDefinition[] abiDefinition = objectMapper.readValue(abiFile, ABIDefinition[].class);
        return Arrays.asList(abiDefinition);
    }

    /**
     * set contract in map and save file.
     *
     * @param contractName      contractName
     * @param version           version
     * @param abiDefinitionList abi info
     */
    public static void setContractWithAbi(String contractName, String version,
                                          List<ABIDefinition> abiDefinitionList, boolean ifSaveFile) throws FrontException {

        List<VersionEvent> versionEventList = getAbiVersionList(contractName, version);
        setFunctionFromAbi(contractName, version, abiDefinitionList, versionEventList);

        if (ifSaveFile) {
            saveAbiFile(contractName, version, abiDefinitionList);
        }
    }

    public static void setFunctionFromAbi(String contractName, String version,
                                          List<ABIDefinition> abiDefinitionList, List<VersionEvent> versionEventList) {
        HashMap<String, List<Class<? extends Type>>> events = new HashMap<>();
        HashMap<String, Boolean> functions = new HashMap<>();
        HashMap<String, List<String>> funcInputs = new HashMap<>();
        HashMap<String, List<String>> funcOutputs = new HashMap<>();

        // todo fill with solidity type  only need the type
        for (ABIDefinition abiDefinition : abiDefinitionList) {

            if (Constants.TYPE_CONSTRUCTOR.equals(abiDefinition.getType())) {
                List<NamedType> inputs = abiDefinition.getInputs();
                List<String> inputList = new ArrayList<>();

                for (NamedType input : inputs) {
                    inputList.add(input.getType());
                }
                functions.put(contractName, false);
                funcInputs.put(contractName, inputList);
            } else if (Constants.TYPE_FUNCTION.equals(abiDefinition.getType())) {
                List<NamedType> inputs = abiDefinition.getInputs();
                List<String> inputList = new ArrayList<>();
                List<NamedType> outputs = abiDefinition.getOutputs();
                List<String> outputList = new ArrayList<>();

                for (NamedType input : inputs) {
                    inputList.add(input.getType());
                }
                for (NamedType output : outputs) {
                    outputList.add(output.getType());
                }

                // fit in solidity 0.6
                boolean isConstant = STATE_MUTABILITY_VIEW.equals(abiDefinition.getStateMutability())
                    || STATE_MUTABILITY_PURE.equals(abiDefinition.getStateMutability());
                functions.put(abiDefinition.getName(), isConstant);
                // functions.put(abiDefinition.getName(), abiDefinition.isConstant());
                funcInputs.put(abiDefinition.getName(), inputList);
                funcOutputs.put(abiDefinition.getName(), outputList);
            }
        }

        versionEventList.add(new VersionEvent(version, events, functions, funcInputs, funcOutputs));
        contractEventMap.put(contractName, versionEventList);
    }


    public static VersionEvent getVersionEventFromAbi(String contractName, List<ABIDefinition> abiDefinitionList) {
        HashMap<String, List<Class<? extends Type>>> events = new HashMap<>();
        HashMap<String, Boolean> functions = new HashMap<>();
        HashMap<String, List<String>> funcInputs = new HashMap<>();
        HashMap<String, List<String>> funcOutputs = new HashMap<>();

        // todo fill with solidity type  only need the type
        for (ABIDefinition abiDefinition : abiDefinitionList) {

            if (Constants.TYPE_CONSTRUCTOR.equals(abiDefinition.getType())) {
                List<NamedType> inputs = abiDefinition.getInputs();
                List<String> inputList = new ArrayList<>();

                for (NamedType input : inputs) {
                    inputList.add(input.getType());
                }
                functions.put(contractName, false);
                funcInputs.put(contractName, inputList);
            } else if (Constants.TYPE_FUNCTION.equals(abiDefinition.getType())) {
                List<NamedType> inputs = abiDefinition.getInputs();
                List<String> inputList = new ArrayList<>();
                List<NamedType> outputs = abiDefinition.getOutputs();
                List<String> outputList = new ArrayList<>();

                for (NamedType input : inputs) {
                    inputList.add(input.getType());
                }
                for (NamedType output : outputs) {
                    outputList.add(output.getType());
                }

                // fit in solidity 0.6
                boolean isConstant = STATE_MUTABILITY_VIEW.equals(abiDefinition.getStateMutability())
                    || STATE_MUTABILITY_PURE.equals(abiDefinition.getStateMutability());
                functions.put(abiDefinition.getName(), isConstant);
                // functions.put(abiDefinition.getName(), abiDefinition.isConstant());
                funcInputs.put(abiDefinition.getName(), inputList);
                funcOutputs.put(abiDefinition.getName(), outputList);
            }
        }

        return new VersionEvent(null, events, functions, funcInputs, funcOutputs);

    }

    private static List<VersionEvent> getAbiVersionList(String contractName, String version) throws FrontException {
        List<VersionEvent> versionEventList = null;
        if (contractEventMap.containsKey(contractName)) {
            versionEventList = contractEventMap.get(contractName);
        } else {
            versionEventList = new ArrayList<>();
        }

        for (VersionEvent versionEvent : versionEventList) {
            if (version.equals(versionEvent.getVersion())) {
                log.error("contract:{} version:{} is existed.", contractName, version);
                throw new FrontException(ConstantCode.CONTRACT_DEPLOYED_ERROR);
            }
        }
        return versionEventList;
    }

    /**
     * save abi file to disk which dir declare in config.
     *
     * @param contractName contractName
     * @param version      version
     */
    public static void saveAbiFile(String contractName, String version,
                                   List<ABIDefinition> abiDefinitionList)
        throws FrontException {
        FileOutputStream outputStream = null;
        try {
            File file = new File(
                    Constants.ABI_DIR + Constants.DIAGONAL + contractName + Constants.SEP + version);
            FrontUtils.createFileIfNotExist(file, true);
            outputStream = new FileOutputStream(file);

            outputStream.write(JsonUtils.toJSONString(abiDefinitionList).getBytes());
            outputStream.flush();
            log.info(file.getName() + "file create successfully");
        } catch (IOException e) {
            log.error("saveAbiFile failed.", e);
            throw new FrontException(ConstantCode.ABI_SAVE_ERROR);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("saveAbiFile failed.", e);
            }
        }
        return;
    }

    /**
     * check if the contract has been deployed.
     *
     * @param contractName    contractName
     * @param contractVersion version
     */
    public static Boolean ifContractAbiExisted(String contractName, String contractVersion) {
        boolean ifExisted = false;
        Map<String, List<String>> contracts = getAllContract();
        for (String contract : contracts.keySet()) {
            if (contract.equals(contractName)) {
                List<String> versions = contracts.get(contract);
                if (versions.contains(contractVersion)) {
                    ifExisted = true;
                    break;
                }
            }
        }
        return ifExisted;
    }

    /**
     * check if the function is constant.
     *
     * @param contractName contractName
     * @param funcName     funcName
     * @param version      version
     */
    public static boolean getConstant(String contractName, String funcName, String version) {
        VersionEvent target = getVersionEvent(contractName, version);
        if (target == null) {
            log.warn("getConstant fail. contract name:{} func:{} version:{} is not existed",
                    contractName, funcName, version);
            throw new FrontException(ConstantCode.IN_FUNCTION_ERROR);
        }
        return target.getFunctions().get(funcName);
    }

    /**
     * get input types.
     *
     * @param contractName contractName
     * @param funcName     funcName
     * @param version      version
     */
    public static List<String> getFuncInputType(String contractName, String funcName,
                                                String version) {
        VersionEvent target = getVersionEvent(contractName, version);

        if (target == null) {
            return Collections.emptyList();
        }
        return target.getFuncInputs().get(funcName);
    }

    /**
     * get output types.
     *
     * @param contractName contractName
     * @param funcName     funcName
     * @param version      version
     */
    public static List<String> getFuncOutputType(String contractName, String funcName,
                                                 String version) {
        VersionEvent target = getVersionEvent(contractName, version);
        if (target == null) {
            return Collections.emptyList();
        }
        return target.getFuncOutputs().get(funcName);
    }


    /**
     * getFuncOutputType.
     *
     * @param abiDefinition abiDefinition
     * @return
     */
    public static List<String> getFuncOutputType(ABIDefinition abiDefinition) {
        List<String> outputList = new ArrayList<>();
        List<ABIDefinition.NamedType> outputs = abiDefinition.getOutputs();
        for (ABIDefinition.NamedType output : outputs) {
            outputList.add(output.getType());
        }
        return outputList;
    }

    /**
     * get all contract from contractEventMap.
     */
    private static Map<String, List<String>> getAllContract() {
        Map<String, List<String>> contracts = new HashMap<>();
        for (Map.Entry<String, List<VersionEvent>> entry : contractEventMap.entrySet()) {
            List<VersionEvent> versionEventList = entry.getValue();
            List<String> versions = new ArrayList<>();
            for (VersionEvent versionEvent : versionEventList) {
                versions.add(versionEvent.getVersion());
            }
            contracts.put(entry.getKey(), versions);
        }
        return contracts;
    }


    static TypeName buildTypeName(String typeDeclaration) {
        String type = trimStorageDeclaration(typeDeclaration);
        Matcher matcher = PATTERN.matcher(type);
        if (matcher.find()) {
            Class<?> baseType = org.fisco.bcos.sdk.codec.datatypes.AbiTypes
                    .getType(matcher.group(1));
            String firstArrayDimension = matcher.group(2);
            String secondArrayDimension = matcher.group(3);

            TypeName typeName;

            if ("".equals(firstArrayDimension)) {
                typeName = ParameterizedTypeName.get(DynamicArray.class, baseType);
            } else {
                Class<?> rawType = getStaticArrayTypeReferenceClass(firstArrayDimension);
                typeName = ParameterizedTypeName.get(rawType, baseType);
            }

            if (secondArrayDimension != null) {
                if ("".equals(secondArrayDimension)) {
                    return ParameterizedTypeName.get(ClassName.get(DynamicArray.class), typeName);
                } else {
                    Class<?> rawType = getStaticArrayTypeReferenceClass(secondArrayDimension);
                    return ParameterizedTypeName.get(ClassName.get(rawType), typeName);
                }
            }

            return typeName;
        } else {
            Class<?> cls = AbiTypes.getType(type);
            return ClassName.get(cls);
        }
    }


    private static Class<?> getStaticArrayTypeReferenceClass(String type) {
        try {
            return Class.forName("org.fisco.bcos.web3j.abi.datatypes.generated.StaticArray" + type);
        } catch (ClassNotFoundException e) {
            // Unfortunately we can't encode it's length as a type if it's > 32.
            return StaticArray.class;
        }
    }


    private static String trimStorageDeclaration(String type) {
        if (type.endsWith(" storage") || type.endsWith(" memory")) {
            return type.split(" ")[0];
        } else {
            return type;
        }
    }


    /**
     * get VersionEvent.
     */
    private static VersionEvent getVersionEvent(String contractName, String version) {
        if (!contractEventMap.containsKey(contractName)) {
            return null;
        }
        List<VersionEvent> versionEventList = contractEventMap.get(contractName);
        VersionEvent target = null;
        for (VersionEvent versionEvent : versionEventList) {
            if (versionEvent.getVersion().equals(version)) {
                target = versionEvent;
                break;
            }
        }
        return target;
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
                        outputType = ContractTypeUtil.getType(funOutputTypes.get(i).substring(0,
                            funOutputTypes.get(i).indexOf("[")));
                        value = ContractTypeUtil.decodeResult(results.get(j), outputType);
                        values.add(value);
                    }
                    result.add(values);
                } else {
                    outputType = ContractTypeUtil.getType(funOutputTypes.get(i));
                    value = ContractTypeUtil.decodeResult(typeList.get(i), outputType);
                    result.add(value);
                }
            }
            return JsonUtils.toJavaObject(JsonUtils.toJSONString(result), Object.class);
        }
        return null;
    }


    /**
     * check abi valid
     *
     * @param contractAbi
     */
    public static List<ABIDefinition> checkAbi(String contractAbi) {
        try {
            List<ABIDefinition> abiArr =
                JsonUtils.toJavaObjectList(contractAbi, ABIDefinition.class);
            return abiArr;
        } catch (Exception ex) {
            throw new FrontException(ConstantCode.PARAM_FAIL_ABI_INVALID);
        }
    }


    /**
     * input parameter format.
     *
     * @param funcInputTypes list
     * @param params list
     * @return
     */
    public static List<Type> inputFormat(List<String> funcInputTypes, List<Object> params) {
        List<Type> finalInputs = new ArrayList<>();
        for (int i = 0; i < funcInputTypes.size(); i++) {
            Class<? extends Type> inputType = null;
            Object input = null;
            if (funcInputTypes.get(i).contains("[")
                && funcInputTypes.get(i).contains("]")) {
                List<Object> arrList =
                    new ArrayList<>(Arrays.asList(params.get(i).toString().split(",")));
                inputType = ContractTypeUtil.getType(
                    funcInputTypes.get(i).substring(0, funcInputTypes.get(i).indexOf("[")));
                List<Type> arrParams = new ArrayList<>();
                for (int j = 0; j < arrList.size(); j++) {
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

}
