package com.webank.webase.front.util;

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
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.StaticArray;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.generated.AbiTypes;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition.NamedType;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.FrontUtils;
import com.webank.webase.front.base.exception.FrontException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
 * ContractAbiUtil.
 */
@Slf4j
public class ContractAbiUtil {

    private ContractAbiUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static HashMap<String, List<VersionEvent>> contractEventMap = new HashMap<>();
    private static final String regex = "(\\w+)(?:\\[(.*?)\\])(?:\\[(.*?)\\])?";
    private static final Pattern pattern = Pattern.compile(regex);

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
         * @param version contract version
         * @param events events
         * @param functions functions
         * @param funcInputs funcInputs
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
            List<AbiDefinition> abiList = loadContractDefinition(file);
            setContractWithAbi(arr[0], version, abiList, false);
        }
    }

    /**
     * loadContractDefinition.
     *
     * @param abiFile file
     */
    public static List<AbiDefinition> loadContractDefinition(File abiFile) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition = objectMapper.readValue(abiFile, AbiDefinition[].class);
        return Arrays.asList(abiDefinition);
    }

    /**
     * set contract in map and save file.
     *
     * @param contractName contractName
     * @param version version
     * @param abiDefinitionList abi info
     */
    public static void setContractWithAbi(String contractName, String version,
        List<AbiDefinition> abiDefinitionList, boolean ifSaveFile) throws FrontException {

        List<VersionEvent> versionEventList = getAbiVersionList(contractName, version);
        setFunctionFromAbi(contractName, version, abiDefinitionList, versionEventList);

        if (ifSaveFile) {
            saveAbiFile(contractName, version, abiDefinitionList);
        }
    }

    public static void setFunctionFromAbi(String contractName, String version,
        List<AbiDefinition> abiDefinitionList, List<VersionEvent> versionEventList) {
        HashMap<String, List<Class<? extends Type>>> events = new HashMap<>();
        HashMap<String, Boolean> functions = new HashMap<>();
        HashMap<String, List<String>> funcInputs = new HashMap<>();
        HashMap<String, List<String>> funcOutputs = new HashMap<>();

        // todo fill with solidity type  only need the type
        for (AbiDefinition abiDefinition : abiDefinitionList) {

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

                functions.put(abiDefinition.getName(), abiDefinition.isConstant());
                funcInputs.put(abiDefinition.getName(), inputList);
                funcOutputs.put(abiDefinition.getName(), outputList);
            }
        }

        versionEventList.add(new VersionEvent(version, events, functions, funcInputs, funcOutputs));
        contractEventMap.put(contractName, versionEventList);
    }


    public static VersionEvent getVersionEventFromAbi(String contractName, List<AbiDefinition> abiDefinitionList) {
        HashMap<String, List<Class<? extends Type>>> events = new HashMap<>();
        HashMap<String, Boolean> functions = new HashMap<>();
        HashMap<String, List<String>> funcInputs = new HashMap<>();
        HashMap<String, List<String>> funcOutputs = new HashMap<>();

        // todo fill with solidity type  only need the type
        for (AbiDefinition abiDefinition : abiDefinitionList) {

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

                functions.put(abiDefinition.getName(), abiDefinition.isConstant());
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
     * @param version version
     */
    public static void saveAbiFile(String contractName, String version,
        List<AbiDefinition> abiDefinitionList)
        throws FrontException {
        FileOutputStream outputStream = null;
        try {
            File file = new File(
                Constants.ABI_DIR + Constants.DIAGONAL + contractName + Constants.SEP + version);
            FrontUtils.createFileIfNotExist(file,true);
            outputStream = new FileOutputStream(file);

            //todo
            outputStream.write(JSON.toJSONString(abiDefinitionList).getBytes());
            outputStream.flush();
            log.info(file.getName()+"file create successfully");
        } catch (IOException e) {
            log.error("saveAbiFile failed.", e);
            throw new FrontException(ConstantCode.ABI_SAVE_ERROR);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                throw new FrontException(ConstantCode.ABI_SAVE_ERROR);
            }
        }
        return;
    }

    /**
     * check if the contract has been deployed.
     *
     * @param contractName contractName
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
     * @param funcName funcName
     * @param version version
     */
    public static String ifConstantFunc(String contractName, String funcName, String version) {
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
        if (target == null) {
            return null;
        }
        return target.getFunctions().get(funcName).toString();
    }

    /**
     * get input types.
     *
     * @param contractName contractName
     * @param funcName funcName
     * @param version version
     */
    public static List<String> getFuncInputType(String contractName, String funcName,
        String version) {
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
        if (target == null) {
            return null;
        }
        return target.getFuncInputs().get(funcName);
    }

    /**
     * get output types.
     *
     * @param contractName contractName
     * @param funcName funcName
     * @param version version
     */
    public static List<String> getFuncOutputType(String contractName, String funcName,
        String version) {
        if (!contractEventMap.containsKey(contractName)) {
            return   Collections.emptyList();
        }

        List<VersionEvent> versionEventList = contractEventMap.get(contractName);
        VersionEvent target = null;
        for (VersionEvent versionEvent : versionEventList) {
            if (versionEvent.getVersion().equals(version)) {
                target = versionEvent;
                break;
            }
        }
        if (target == null) {
            return   Collections.emptyList();
        }
        return target.getFuncOutputs().get(funcName);
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
        Matcher matcher = pattern.matcher(type);
        if (matcher.find()) {
            Class<?> baseType = org.fisco.bcos.web3j.abi.datatypes.generated.AbiTypes
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
}
