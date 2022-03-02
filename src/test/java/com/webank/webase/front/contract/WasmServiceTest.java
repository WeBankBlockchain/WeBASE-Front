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
import com.webank.webase.front.contract.entity.ReqContractSave;
import com.webank.webase.front.contract.entity.wasm.AbiBinInfo;
import com.webank.webase.front.contract.entity.wasm.CompileTask;
import com.webank.webase.front.util.JsonUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class WasmServiceTest extends SpringTestBase {

    @Autowired
    LiquidCompileService liquidCompileService;
    @Autowired
    ContractService contractService;

    private static final String groupId = "group";
    private static final String contractPath = "/";
    private static final String contractName = "hello";
    private static final String source = "IyFbY2ZnX2F0dHIobm90KGZlYXR1cmUgPSAic3RkIiksIG5vX3N0ZCldCgp1c2UgbGlxdWlkOjpzdG9yYWdlOwp1c2UgbGlxdWlkX2xhbmcgYXMgbGlxdWlkOwoKI1tsaXF1aWQ6OmNvbnRyYWN0XQptb2QgaGVsbG9fd29ybGQgewogICAgdXNlIHN1cGVyOjoqOwoKICAgICNbbGlxdWlkKHN0b3JhZ2UpXQogICAgc3RydWN0IEhlbGxvV29ybGQgewogICAgICAgIG5hbWU6IHN0b3JhZ2U6OlZhbHVlPFN0cmluZz4sCiAgICB9CgogICAgI1tsaXF1aWQobWV0aG9kcyldCiAgICBpbXBsIEhlbGxvV29ybGQgewogICAgICAgIHB1YiBmbiBuZXcoJm11dCBzZWxmKSB7CiAgICAgICAgICAgIHNlbGYubmFtZS5pbml0aWFsaXplKFN0cmluZzo6ZnJvbSgiQWxpY2UiKSk7CiAgICAgICAgfQoKICAgICAgICBwdWIgZm4gZ2V0KCZzZWxmKSAtPiBTdHJpbmcgewogICAgICAgICAgICBzZWxmLm5hbWUuY2xvbmUoKQogICAgICAgIH0KCiAgICAgICAgcHViIGZuIHNldCgmbXV0IHNlbGYsIG5hbWU6IFN0cmluZykgewogICAgICAgICAgICBzZWxmLm5hbWUuc2V0KG5hbWUpCiAgICAgICAgfQogICAgfQoKICAgICNbY2ZnKHRlc3QpXQogICAgbW9kIHRlc3RzIHsKICAgICAgICB1c2Ugc3VwZXI6Oio7CgogICAgICAgICNbdGVzdF0KICAgICAgICBmbiBnZXRfd29ya3MoKSB7CiAgICAgICAgICAgIGxldCBjb250cmFjdCA9IEhlbGxvV29ybGQ6Om5ldygpOwogICAgICAgICAgICBhc3NlcnRfZXEhKGNvbnRyYWN0LmdldCgpLCAiQWxpY2UiKTsKICAgICAgICB9CgogICAgICAgICNbdGVzdF0KICAgICAgICBmbiBzZXRfd29ya3MoKSB7CiAgICAgICAgICAgIGxldCBtdXQgY29udHJhY3QgPSBIZWxsb1dvcmxkOjpuZXcoKTsKCiAgICAgICAgICAgIGxldCBuZXdfbmFtZSA9IFN0cmluZzo6ZnJvbSgiQm9iIik7CiAgICAgICAgICAgIGNvbnRyYWN0LnNldChuZXdfbmFtZS5jbG9uZSgpKTsKICAgICAgICAgICAgYXNzZXJ0X2VxIShjb250cmFjdC5nZXQoKSwgIkJvYiIpOwogICAgICAgIH0KICAgIH0KfQ==";


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
        boolean ifDone = contractService.newAndCompileLiquidContract(reqContractSave);
        System.out.println(ifDone); // todo 如果异步里失败里，具体错误无法捕获
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
        assert compileTask != null;
        System.out.println("compileTask " + JsonUtils.objToString(compileTask));
    }

}
