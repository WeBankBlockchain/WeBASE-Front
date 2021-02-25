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

import com.webank.webase.front.base.TestBase;
import java.util.List;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 *  config cns api
 */
public class CnsServiceTest extends TestBase {

    public static ApplicationContext context = null;
    public static String contractName;
    public static String version;
    public static String address;
    public static String abi;
    //
    @Test
    public void testRegCns() throws ContractException {
        contractName = "Evidence1";
        version = "1.0";
        address = "0x8acf30e511c885163b8b9d85f34b806c216da6cc";
        abi = "{\"constant\":true,\"inputs\":[{\"name\":\"id\",\"type\":\"bytes32\"}],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"id\",\"type\":\"bytes32\"},{\"name\":\"decription\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"evidence\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

        CnsService cnsService = new CnsService(web3j, cryptoKeyPair);
        System.out.println(cnsService.registerCNS(contractName, version, address, abi));

        // 默认获取最新版本
        List<CnsInfo> res = cnsService.selectByNameAndVersion(contractName, version);
        List<CnsInfo> list = cnsService.selectByName(contractName);
        System.out.println(res);
        System.out.println(list);

    }
}
