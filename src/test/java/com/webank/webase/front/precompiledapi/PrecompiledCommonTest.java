/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.precompiledapi;


import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.util.AbiUtil;
import com.webank.webase.front.util.JsonUtils;
import java.util.List;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionPrecompiled;
import org.junit.Assert;
import org.junit.Test;


public class PrecompiledCommonTest {

    @Test
    public void testCommonInfo() {
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.PERMISSION);
        List<Object> contractAbi = JsonUtils.toJavaObjectList(abiStr, Object.class);
        String funcName = PermissionPrecompiled.FUNC_INSERT;
        ABIDefinition abiDefinition = AbiUtil.getAbiDefinition(funcName, JsonUtils.toJSONString(contractAbi));
        Assert.assertFalse(contractAbi.isEmpty());

    }


}
