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
package com.webank.webase.front.precompiledapi;

import static org.junit.Assert.assertNotNull;

import com.webank.webase.front.base.TestBase;
import org.fisco.bcos.sdk.contract.precompiled.sysconfig.SystemConfigService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * system config api test
 */
public class PrecompiledSysConfigServiceTest extends TestBase {

    //String setValueByKey(String key, String value)：
    // 根据键设置对应的值（查询键对应的值，参考Web3j API中的getSystemConfigByKey接口）。
//    目前支持tx_count_limit和tx_gas_limit属性的设置
    public static ApplicationContext context = null;
    public static String key;
    public static String value;

    @Test
    public void testSystemConfig() throws Exception {

        key = "tx_count_limit"; // key: tx_count_limit, tx_gas_limit
        value = "300000001";

        SystemConfigService systemConfigService = new SystemConfigService(web3j, cryptoKeyPair);

        System.out.println(web3j.getSystemConfigByKey(key).getResult());
        System.out.println(systemConfigService.setValueByKey(key, value));
        assertNotNull(systemConfigService.setValueByKey(key, value));
        System.out.println(web3j.getSystemConfigByKey(key).getResult());
        assertNotNull(web3j.getSystemConfigByKey(key));
    }


}
