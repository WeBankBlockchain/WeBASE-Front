/**
 * Copyright 2014-2021 the original author or authors.
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
package com.webank.webase.front.contract;

import com.webank.webase.front.base.SpringTestBase;
import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.contract.entity.ReqContractSave;
import com.webank.webase.front.contract.entity.ReqDeploy;
import com.webank.webase.front.contract.entity.wasm.AbiBinInfo;
import com.webank.webase.front.contract.entity.wasm.CompileTask;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.transaction.entity.ReqTransHandle;
import com.webank.webase.front.util.JsonUtils;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class WasmServiceTest extends SpringTestBase {

    @Autowired
    LiquidCompileService liquidCompileService;
    @Autowired
    ContractService contractService;
    @Autowired
    ContractRepository contractRepository;
    @Autowired
    TransService transService;

    private static final String groupId = "group0";
    private static final String contractPath = "/";
    private static final String contractName = "hello";
    private static final String source = "IyFbY2ZnX2F0dHIobm90KGZlYXR1cmUgPSAic3RkIiksIG5vX3N0ZCldCgp1c2UgbGlxdWlkOjpzdG9yYWdlOwp1c2UgbGlxdWlkX2xhbmcgYXMgbGlxdWlkOwoKI1tsaXF1aWQ6OmNvbnRyYWN0XQptb2QgaGVsbG9fd29ybGQgewogICAgdXNlIHN1cGVyOjoqOwoKICAgICNbbGlxdWlkKHN0b3JhZ2UpXQogICAgc3RydWN0IEhlbGxvV29ybGQgewogICAgICAgIG5hbWU6IHN0b3JhZ2U6OlZhbHVlPFN0cmluZz4sCiAgICB9CgogICAgI1tsaXF1aWQobWV0aG9kcyldCiAgICBpbXBsIEhlbGxvV29ybGQgewogICAgICAgIHB1YiBmbiBuZXcoJm11dCBzZWxmKSB7CiAgICAgICAgICAgIHNlbGYubmFtZS5pbml0aWFsaXplKFN0cmluZzo6ZnJvbSgiQWxpY2UiKSk7CiAgICAgICAgfQoKICAgICAgICBwdWIgZm4gZ2V0KCZzZWxmKSAtPiBTdHJpbmcgewogICAgICAgICAgICBzZWxmLm5hbWUuY2xvbmUoKQogICAgICAgIH0KCiAgICAgICAgcHViIGZuIHNldCgmbXV0IHNlbGYsIG5hbWU6IFN0cmluZykgewogICAgICAgICAgICBzZWxmLm5hbWUuc2V0KG5hbWUpCiAgICAgICAgfQogICAgfQoKICAgICNbY2ZnKHRlc3QpXQogICAgbW9kIHRlc3RzIHsKICAgICAgICB1c2Ugc3VwZXI6Oio7CgogICAgICAgICNbdGVzdF0KICAgICAgICBmbiBnZXRfd29ya3MoKSB7CiAgICAgICAgICAgIGxldCBjb250cmFjdCA9IEhlbGxvV29ybGQ6Om5ldygpOwogICAgICAgICAgICBhc3NlcnRfZXEhKGNvbnRyYWN0LmdldCgpLCAiQWxpY2UiKTsKICAgICAgICB9CgogICAgICAgICNbdGVzdF0KICAgICAgICBmbiBzZXRfd29ya3MoKSB7CiAgICAgICAgICAgIGxldCBtdXQgY29udHJhY3QgPSBIZWxsb1dvcmxkOjpuZXcoKTsKCiAgICAgICAgICAgIGxldCBuZXdfbmFtZSA9IFN0cmluZzo6ZnJvbSgiQm9iIik7CiAgICAgICAgICAgIGNvbnRyYWN0LnNldChuZXdfbmFtZS5jbG9uZSgpKTsKICAgICAgICAgICAgYXNzZXJ0X2VxIShjb250cmFjdC5nZXQoKSwgIkJvYiIpOwogICAgICAgIH0KICAgIH0KfQ==";
    private static final String userAddress = "0x90c647e80d787f4a8763073077224915501f1c92";

    @Test
    public void testMacOrNot() {
        String osName = System.getProperty("os.name");
        System.out.println("now os is " + osName);
        boolean isMacOS = osName.contains("Mac OS");
        System.out.println(isMacOS);
    }

    @Test
    public void testCheck() {
        liquidCompileService.checkLiquidEnv();
    }

    @Test
    public void testNewContract() {
        liquidCompileService.execLiquidNewContract(groupId, contractPath, contractName, source);
    }

    @Test
    public void testCompileLiquid() {
        AbiBinInfo abiBinInfo = liquidCompileService.compileAndReturn(groupId, contractPath, contractName, 120000);
        System.out.println(JsonUtils.objToString(abiBinInfo));
    }

    @Test
    public void testContractNewAndCompile() {
        ReqContractSave reqContractSave = new ReqContractSave();
        reqContractSave.setGroupId(groupId);
        reqContractSave.setContractName(contractName);
        reqContractSave.setContractPath(contractPath);
        reqContractSave.setContractSource(source);
        CompileTask compileTaskRes = contractService.newAndCompileLiquidContract(reqContractSave);
        System.out.println(compileTaskRes); // todo 如果异步里失败里，具体错误无法捕获
        try {
            Thread.sleep(120000L);
            // todo 查看contract的数据库保存成功了没
            CompileTask compileTask = contractService.getLiquidContract(groupId, contractPath, contractName);
            System.out.println("compileTask " + JsonUtils.objToString(compileTask));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetTaskInfo() {
        CompileTask compileTask = contractService.getLiquidContract(groupId, contractPath, contractName);
        System.out.println("compileTask " + JsonUtils.objToString(compileTask));

    }

    @Test
    public void testSaveContract() {
        CompileTask compileTask = contractService.getLiquidContract(groupId, contractPath, contractName);
        ReqContractSave reqContractSave = new ReqContractSave();
        reqContractSave.setGroupId(groupId);
        reqContractSave.setContractName(contractName);
        reqContractSave.setContractPath(contractPath);
        reqContractSave.setContractSource(source);
        reqContractSave.setContractAbi(compileTask.getAbi());
        reqContractSave.setBytecodeBin(compileTask.getBin());
        contractService.saveContract(reqContractSave);

    }

    @Test
    public void testDeploy() throws ContractException {
        Contract contract = contractRepository.findByGroupIdAndContractPathAndContractName(groupId, contractPath, contractName);
        System.out.println("contract " + contract);
        ReqDeploy reqDeploy = new ReqDeploy();
        reqDeploy.setContractId(contract.getId());
        reqDeploy.setContractAddress("/test_2"); // 每次部署的合约地址都必须不一样
        reqDeploy.setGroupId(groupId);
        reqDeploy.setContractName(contractName);
        reqDeploy.setContractPath(contractPath);
        reqDeploy.setContractSource(source);
        reqDeploy.setAbiInfo(JsonUtils.toList(contract.getContractAbi()));
        reqDeploy.setBytecodeBin(contract.getBytecodeBin());
        reqDeploy.setIsWasm(true);
        reqDeploy.setUser(userAddress);

        String address = contractService.deployByLocalContract(reqDeploy, true);
        System.out.println("deploy " + address);

    }

    @Test
    public void testCallLiquid() {
        Contract contract = contractRepository.findByGroupIdAndContractPathAndContractName(groupId, contractPath, contractName);
        ReqTransHandle transHandle = new ReqTransHandle();
        transHandle.setContractAbi(JsonUtils.toList(contract.getContractAbi()));
        transHandle.setContractAddress("/test_2");
        transHandle.setUser(userAddress);
        transHandle.setGroupId(groupId);
        transHandle.setIsWasm(true);

        // todo test call test_2 group0 contract
        transHandle.setFuncName("get");
        transHandle.setFuncParam(new ArrayList<>());
        Object res1 = transService.transHandleLocal(transHandle);
        System.out.println("res1 " + JsonUtils.objToString(res1));

    }


    @Test
    public void testExecLiquid() {
        Contract contract = contractRepository.findByGroupIdAndContractPathAndContractName(groupId, contractPath, contractName);
        ReqTransHandle transHandle = new ReqTransHandle();
        transHandle.setContractAbi(JsonUtils.toList(contract.getContractAbi()));
        transHandle.setContractAddress("/test_2");
        transHandle.setUser(userAddress);
        transHandle.setGroupId(groupId);
        transHandle.setIsWasm(true);

        // todo test set
        transHandle.setFuncName("set");
        List<Object> param = new ArrayList<>();
        param.add("Bob");

        transHandle.setFuncParam(param);
        Object res2 = transService.transHandleLocal(transHandle);
        System.out.println("res2 " + JsonUtils.objToString(res2));

        // todo test call test_2 group0 contract
        transHandle.setFuncName("get");
        transHandle.setFuncParam(new ArrayList<>());
        Object res1 = transService.transHandleLocal(transHandle);
        System.out.println("res1 " + JsonUtils.objToString(res1));

    }
}
