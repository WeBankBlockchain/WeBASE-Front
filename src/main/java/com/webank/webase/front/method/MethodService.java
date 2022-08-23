/**
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.front.method;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.method.entity.Method;
import com.webank.webase.front.method.entity.MethodHandle;
import com.webank.webase.front.method.entity.MethodKey;
import com.webank.webase.front.method.entity.NewMethodInputParamHandle;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Method is used in transaction data's analysis
 * Store contract's method in db
 */
@Slf4j
@Service
public class MethodService {

    @Autowired
    private MethodRepository methodRepository;
    @Autowired
    private Web3ApiService web3ApiService;

    /**
     * save method info.
     */
    public void saveMethod(NewMethodInputParamHandle newMethodInputParamHandle) {
        List<MethodHandle> methodHandleList = newMethodInputParamHandle.getMethodHandleList();
        Method method = new Method();
        method.setGroupId(newMethodInputParamHandle.getGroupId());
        
        //save each method
        for (MethodHandle methodHandle : methodHandleList) {
            BeanUtils.copyProperties(methodHandle, method);
            methodRepository.save(method);
        }
    }

    /**
     * query by methodId.
     */
    public Method getByMethodId(String methodId, String groupId) {
        MethodKey pram = new MethodKey();
        pram.setMethodId(methodId);
        pram.setGroupId(groupId);
        return methodRepository.findById(pram).orElse(null);
    }

    /**
     * save method by abi string
     * @param groupId
     * @param abiStr
     */
    public void saveMethod(String groupId, String abiStr) {
        List<MethodHandle> methodList = new ArrayList<>(getMethodFromAbi(abiStr, web3ApiService.getCryptoSuite(groupId)));
        NewMethodInputParamHandle newMethodInputParam = new NewMethodInputParamHandle();
        newMethodInputParam.setGroupId(groupId);
        newMethodInputParam.setMethodHandleList(methodList);
        this.saveMethod(newMethodInputParam);
    }
    /**
     * get method from abi
     */
    public static List<MethodHandle> getMethodFromAbi(String abi, CryptoSuite cryptoSuite) {
        List<ABIDefinition> abiList = JsonUtils.toJavaObjectList(abi, ABIDefinition.class);
        if (abiList == null){
            throw new FrontException(ConstantCode.PARAM_FAIL_ABI_INVALID);
        }
        List<MethodHandle> methodList = new ArrayList<>();
        for (ABIDefinition abiDefinition : abiList) {
            MethodHandle method = new MethodHandle();
            method.setMethodType(abiDefinition.getType());
            method.setAbiInfo(JsonUtils.toJSONString(abiDefinition));
            method.setMethodId(buildMethodId(abiDefinition, cryptoSuite));
            methodList.add(method);
        }
        return methodList;
    }

    /**
     * get methodId after hash
     */
    public static String buildMethodId(ABIDefinition abiDefinition, CryptoSuite cryptoSuite) {
        byte[] inputs = getMethodIdBytes(abiDefinition);
        // 2019/11/27 support guomi
        byte[] hash = cryptoSuite.hash(inputs);
        if ("function".equals(abiDefinition.getType())) {
            return Hex.toHexString(hash).substring(0, 10);
        }
        // event save whole topic hash
        return Hex.toHexString(hash);
    }
    /**
     * get methodId bytes from ABIDefinition
     * @return byte[]
     */
    public static byte[] getMethodIdBytes(ABIDefinition abiDefinition) {
        StringBuilder result = new StringBuilder();
        result.append(abiDefinition.getName());
        result.append("(");
        String params = abiDefinition.getInputs().stream()
            .map(ABIDefinition.NamedType::getType)
            .collect(Collectors.joining(","));
        result.append(params);
        result.append(")");

        byte[] inputs = result.toString().getBytes();
        return inputs;
    }
}
