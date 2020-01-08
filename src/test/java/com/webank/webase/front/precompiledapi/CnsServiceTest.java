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
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 *  config cns api
 */
public class CnsServiceTest extends TestBase {

    public static ApplicationContext context = null;
    public static String contractName;
    public static String version;
    public static String address;
    public static String abi;
    public static String contractNameAndVersion;
    //
    @Test
    public void testRegCns() throws Exception {
        contractName = "Evidence1";
        version = "1.0";
        address = "0x8acf30e511c885163b8b9d85f34b806c216da6cc";
        abi = "{\"constant\":true,\"inputs\":[{\"name\":\"id\",\"type\":\"bytes32\"}],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"id\",\"type\":\"bytes32\"},{\"name\":\"decription\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"evidence\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";
        contractNameAndVersion = "Evidencee:1.0";

        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //链管理员私钥加载
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));

        CnsService cnsService = new CnsService(web3j, credentialsPEM);
//        System.out.println(cnsService.registerCns(contractName, version, address, abi));
//        assertNotNull(cnsService.registerCns(contractName, version, address, abi));

        // 默认获取最新版本
//        String res = cnsService.getAddressByContractNameAndVersion("contractNameAndVersion");
        List<CnsInfo> list = new ArrayList<>();
                list = cnsService.queryCnsByName(contractName);
        System.out.println(contractNameAndVersion);
//        System.out.println(cnsService.getAddressByContractNameAndVersion(contractName));
//        assertNotNull(cnsService.getAddressByContractNameAndVersion(contractName));
//        System.out.println(cnsService.queryCnsByName(contractName).size());
//        System.out.println(cnsService.queryCnsByName(contractName).get(0).getAddress());
//        System.out.println(cnsService.queryCnsByName(contractName).get(1).getAddress());
//        assertTrue(cnsService.queryCnsByName(contractName).size() != 0);

    }
}
