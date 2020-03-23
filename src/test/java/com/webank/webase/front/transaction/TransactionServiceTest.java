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

package com.webank.webase.front.transaction;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.Application;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.precompiledapi.PrecompiledCommonInfo;
import com.webank.webase.front.transaction.entity.ReqTransHandleWithSign;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TransactionServiceTest {

    @Autowired
    TransService transService;

//    @Test
//    public void testSignMessage() throws Exception {
//        String contractAddr = "0x76b52c5c0d5c4a2cf77e557bdd909d748eb95a4d";
////        transService.signMessage(1, web3jHashMap.get(1), 100001, contractAddr, );
//        ReqTransHandleWithSign param = new ReqTransHandleWithSign();
//        String abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
//        List<Object> abiList = JSON.parseArray(abi, Object.class);
//        param.setContractAbi(abiList);
//        param.setFuncName("set");
//        String funParam = " [\"22\"]";
//        List<Object> funParamList = JSON.parseArray(funParam, Object.class);
//        param.setFuncParam(funParamList);
//        param.setContractAddress(contractAddr);
//        Object res = transService.transHandleWithSign(param);
//        System.out.println(res);
//    }

}
