/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.scaffold;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.SpringTestBase;
import com.webank.webase.front.base.config.NodeConfig;
import com.webank.webase.front.contract.entity.Contract;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ScaffoldServiceTest extends SpringTestBase {

    @Autowired
    private ScaffoldService scaffoldService;

    @Test
    public void testGenerate() throws JsonProcessingException {
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setP2pip("127.0.0.1");
        nodeConfig.setChannelPort("25210");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> sdkMap = mapper.readValue(ScaffoldBuildTest.sdkMapStr, Map.class);
        String groupName = "org.webank";
        String artifact = "demo";
        Contract contract = new Contract();
        contract.setContractAddress(ScaffoldBuildTest.contractAddress);
        contract.setContractSource(ScaffoldBuildTest.helloWorldSolBase64Str);
        contract.setContractAbi(ScaffoldBuildTest.abiStr);
        contract.setBytecodeBin(ScaffoldBuildTest.binStr);
        contract.setContractName(ScaffoldBuildTest.contractName);
        List<Contract> tbContractList = Collections.singletonList(contract);

        scaffoldService.generateProject(nodeConfig, groupName, artifact,
            tbContractList, 1, "", sdkMap);
    }

    @Test
    public void testTelnetPort() {
        scaffoldService.telnetChannelPort("127.0.0.1", 5002);
    }
}
