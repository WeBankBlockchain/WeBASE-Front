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

import com.webank.scaffold.artifact.ProjectArtifact;
import com.webank.scaffold.artifact.single.NewMainDir.SolInfo;
import com.webank.scaffold.artifact.single.NewMainResourceDir.ContractInfo;
import com.webank.scaffold.factory.ProjectFactory;
import com.webank.webase.front.util.CleanPathUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import org.junit.Test;

public class ScaffoldServiceTest {
    // hello world
    String solSourceCodeBase64Str = "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IEhlbGxvV29ybGR7DQogICAgc3RyaW5nIG5hbWU7DQogICAgZXZlbnQgU2V0TmFtZShzdHJpbmcgbmFtZSk7DQogICAgZnVuY3Rpb24gZ2V0KCljb25zdGFudCByZXR1cm5zKHN0cmluZyl7DQogICAgICAgIHJldHVybiBuYW1lOw0KICAgIH0NCiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nIG4pew0KICAgICAgICBlbWl0IFNldE5hbWUobik7DQogICAgICAgIG5hbWU9bjsNCiAgICB9DQp9";
    String contractName = "HelloWorld";
    String abiStr = "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"string\"}],\"name\":\"SetName\",\"type\":\"event\"}]";
    String binStr = "608060405234801561001057600080fd5b50610373806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100515780636d4ce63c146100ba575b600080fd5b34801561005d57600080fd5b506100b8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061014a565b005b3480156100c657600080fd5b506100cf610200565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010f5780820151818401526020810190506100f4565b50505050905090810190601f16801561013c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b7f4df9dcd34ae35f40f2c756fd8ac83210ed0b76d065543ee73d868aec7c7fcf02816040518080602001828103825283818151815260200191508051906020019080838360005b838110156101ac578082015181840152602081019050610191565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906101fc9291906102a2565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102985780601f1061026d57610100808354040283529160200191610298565b820191906000526020600020905b81548152906001019060200180831161027b57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e357805160ff1916838001178555610311565b82800160010185558215610311579182015b828111156103105782518255916020019190600101906102f5565b5b50905061031e9190610322565b5090565b61034491905b80821115610340576000816000905550600101610328565b5090565b905600a165627a7a72305820baa9e2a7ab055843a8a3de62b50fba49e6309323fb92358b598491fa5a76b9e90029";
    String smBinStr = "";

    // build params
    String group = "org.webase.example";
    String artifactName = "demo_" + contractName.toLowerCase();
    String outputDir = "output";

    String solDir = outputDir + File.separator + "contracts";
    String confDir = outputDir + File.separator + artifactName
        + File.separator + "src" + File.separator + "resources" + File.separator + "conf";
    /**
     * 生成sol文件到指定目录
     */
    @Test
    public void writeSolFile() {
        String solSourceCode = new String(Base64.getDecoder().decode(solSourceCodeBase64Str));
        String solFileName = "HelloWorld.sol";
        System.out.println("solSourceCode: " + solSourceCode);
        File solDirCheck = new File(solDir);
        if (!solDirCheck.exists()) {
            boolean createSolDir = solDirCheck.mkdirs();
            System.out.println("createSolDir: " + createSolDir);
        }
        Path solFilePath = Paths
            .get(CleanPathUtil.cleanString(solDir + File.separator + solFileName));
        System.out.println("solFilePath: " + solFilePath);
        try (BufferedWriter writer = Files
            .newBufferedWriter(solFilePath, StandardCharsets.UTF_8)) {
            // write to relative path
            writer.write(solSourceCode);
        } catch (IOException e) {
            System.out.println("error: " + Arrays.toString(e.getStackTrace()));
        }

    }

    /**
     * 根据sol文件生成项目
     */
    @Test
    public void testFactory() {
        ProjectFactory projectFactory = new ProjectFactory();
        ProjectArtifact artifact = projectFactory.buildProjectDir(solDir, group, artifactName, outputDir, contractName);
        System.out.println("artifact: " + artifact);
    }

    /**
     * 根据入参生成项目
     */
    @Test
    public void testBuildByParam() {
        String solSourceCode = new String(Base64.getDecoder().decode(solSourceCodeBase64Str));
        SolInfo solInfo = new SolInfo();
        solInfo.setContractName(contractName);
        solInfo.setSolRawString(solSourceCode);
        System.out.println("solInfo: ");
        System.out.println(solInfo);

        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setContractName(contractName);
        contractInfo.setAbiStr(abiStr);
        contractInfo.setBinStr(binStr);
        System.out.println("contractInfo: ");
        System.out.println(contractInfo);

        ProjectFactory projectFactory = new ProjectFactory();
        ProjectArtifact result = projectFactory.buildProjectDir(Collections.singletonList(solInfo),
            Collections.singletonList(contractInfo), group, artifactName, outputDir);
        System.out.println("result: ");
        System.out.println(result);
    }

    /**
     * 项目中添加sdk证书：./output/webaseDemo/src/resources/conf
     * 修改.properties：./output/webaseDemo/src/resources/application.properties
     *  system.peers=127.0.0.1:20200,127.0.0.1:20201
     *  system.groupId=1
     *  system.hexPrivateKey=
     *  system.contract.helloWorldAddress=
     *  载入合约地址可能有多个
     */
    @Test
    public void testAddSdk() {

    }

    @Test
    public void testUpdateProperties() {
        //
    }
}
