/**
 * Copyright 2014-2019 the original author or authors.
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

package com.webank.webase.front.gm.runtime;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

/**
 * Cns.register returns invalid signature from chain
 */
public class CnsServiceTest extends BaseTest{

    @Autowired
    private HashMap<Integer, CnsService> cnsServiceMap;
    @Autowired
    HashMap<Integer, Web3j> web3jMap;

    @Test
    public void testCnsRegister() throws Exception {
//        Credentials credentialsTest =
//                GenCredential.create("3bed914595c159cbce70ec5fb6aff3d6797e0c5ee5a7a9224a21cae8932d84a4");

        String contractName = "HelloWorldGM";
        String version = "1.0";
        String address = "0xde002b342fe940dacbfee25c54278d5d22804c1e";
        String abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
        String contractNameAndVersion = "HelloWorldGM:1.0";

        int groupId = 1;

//        CnsService cnsService = new CnsService(web3jMap.get(groupId), credentialsTest);
//        cnsService.registerCns(contractName, version, address, abi);
//        String res0 = cnsService.getAddressByContractNameAndVersion(contractNameAndVersion);
//        Assert.assertNotNull(res0);

        cnsServiceMap.get(groupId).registerCns(contractName, version, address, abi);
        String res = cnsServiceMap.get(groupId).getAddressByContractNameAndVersion(contractNameAndVersion);
        Assert.assertNotNull(res);

    }
}
