/*
 * Copyright 2014-2019 the original author or authors.
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

import com.webank.webase.front.channel.test.TestBase;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.config.SystemConfigService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SysConfigDbTest extends TestBase {


    public static ApplicationContext context = null;
    public static String key;
    public static String value;

    /**
     * System config related
     */
    @Test
    public void setSysConfigValueByKey() throws Exception {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //链管理员私钥加载
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
        SystemConfigService systemConfigService = new SystemConfigService(web3j, credentialsPEM);

        systemConfigService.setValueByKey(key, value);
    }

}
