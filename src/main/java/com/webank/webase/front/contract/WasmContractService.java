/**
 * Copyright 2014-2021 the original author or authors.
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

package com.webank.webase.front.contract;

import static java.io.File.separator;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.contract.entity.wasm.AbiBinInfo;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.cmd.ExecuteResult;
import com.webank.webase.front.util.cmd.JavaCommandExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.utils.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * use command line to compile liquid contract and return abi & bin
 * node-mgr执行编译的时候，可以选择front（选择有环境的front）
 */
@Slf4j
@Service
public class WasmContractService {

    private static final String LIQUID_DIR = "liquid";
    @Autowired
    private Constants constants;

    public static String getContractPath(String contractDir) {
        String path = Paths.get(LIQUID_DIR, contractDir).toString();
        log.info("getContractPath path:{}", path);
        return path;
    }

    public void checkLiquidEnv() {
        String commandCargo = "cargo -V";
        // cargo -V
        //cargo 1.56.0 (4ed5d137b 2021-10-04)
        String commandRustc = "cargo -V";
        // rustc -V
        //rustc 1.56.1 (59eed8a2a 2021-11-01)

//        String commandLiquid = "cargo liquid -V";
        // cargo liquid -V
        //error: no such subcommand: `liquid`

        ExecuteResult result = JavaCommandExecutor.executeCommand(commandCargo, constants.getLiquidCompileTimeout());
        if (result.failed()) {
            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED.attach(result.getExecuteOut()));
        }

       ExecuteResult resultRustc = JavaCommandExecutor.executeCommand(commandRustc, constants.getLiquidCompileTimeout());
        if (resultRustc.failed()) {
            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED.attach(resultRustc.getExecuteOut()));
        }
        this.mkdirIfNotExist();

    }

    private void mkdirIfNotExist() {
        // if liquid dir not exist, build dir
        File targetDirFile = Paths.get(LIQUID_DIR).toFile();
        if (!targetDirFile.exists()) {
            boolean mkdirResult = targetDirFile.mkdir();
            log.info("mkdir of liquid, result:{{}", mkdirResult);
        }

    }


    /**
     * 检查目录是否已存在，然后再创建
     * groupId + _ + contractName
     * @param contractName
     * @param contractSource base64 encoded, required decode before write to file
     */
    public void execLiquidNewContract(String groupId, String contractName, String contractSource) {
        this.mkdirIfNotExist();

        String contractDir = groupId + "_" + contractName;
        // todo check dir exist
        String command = String.format("cd %s && cargo liquid new contract %s", LIQUID_DIR, contractDir);
        // todo cargo liquid new contract XXX, sed -i gitee
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constants.getLiquidCompileTimeout());
        log.info("execNewContract new contract Result:{}", result);
        if (result.failed()) {
            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED.attach(result.getExecuteOut()));
        }

        // cd $contractDir
        // sed github as gitee
        // todo mac add "", linux might not
        String sed = String.format("sed -i \"\" \"s-https://github.com/WeBankBlockchain-https://gitee.com/WeBankBlockchain-g\" %s/Cargo.toml", getContractPath(contractDir));
        String sed2 = String.format("sed -i \"\" \"s-https://github.com/WeBankBlockchain-https://gitee.com/WeBankBlockchain-g\" %s/.liquid/abi_gen/Cargo.toml", getContractPath(contractDir));
        ExecuteResult sedResult = JavaCommandExecutor.executeCommand(sed, constants.getLiquidCompileTimeout());
        ExecuteResult sedResult2 = JavaCommandExecutor.executeCommand(sed2, constants.getLiquidCompileTimeout());
        log.info("execNewContract sedResult:{},sedResult2:{}", sedResult, sedResult2);
        if (sedResult.failed()||sedResult2.failed()) {
            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED.attach(sedResult.getExecuteOut()));
        }

        //  write contract source: lib.rs
        File contractFile = Paths.get(LIQUID_DIR, contractDir, "src", "lib.rs").toFile();
        try {
            FileUtils.write(contractFile, contractSource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("execNewContract save contract source code error:", e);
            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED);
        }

    }

    public void execCargoTest(String groupId, String contractName) {
        String contractDir = groupId + "_" + contractName;
        String testCommand = "cargo test";
        String command = String.format("cd %s && %s", getContractPath(contractDir), testCommand);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constants.getLiquidCompileTimeout());
        log.info("execTest result:{}", result);
        if (result.failed()) {
            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED.attach(result.getExecuteOut()));
        }
    }

    /**
     * cargo liquid build
     * todo 换成异步，在合约状态中更新是否1已编译
     * @param contractDir
     */
    public void execLiquidCompile(String contractDir) {
        // cargo liquid build
        String testCommand = "cargo liquid build";
        String command = String.format("cd %s && %s", getContractPath(contractDir), testCommand);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constants.getLiquidCompileTimeout());
        log.info("execCompile result:{}", result);
        if (result.failed()) {
            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED.attach(result.getExecuteOut()));
        }
    }

    /**
     * check contract project dir exist before build
     * @param groupId
     * @param contractName
     * @return
     */
    public AbiBinInfo compileAndReturn(String groupId, String contractName) {
        String contractDir = groupId + "_" + contractName;
        // compile
        this.execLiquidCompile(contractDir);
        // check target dir exist, or compile failed
        String targetPath = Paths.get(LIQUID_DIR, contractDir, "target").toString();
        File targetDirFile = new File(targetPath);
        if (!targetDirFile.exists()) {
            log.error("compileAndReturn target directory not exist, compile failed!");
            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED);
        }

        // read .abi and .wasm(byte to hexString)
        String abiPath = Paths.get(LIQUID_DIR, contractDir, "target", contractDir + ".abi").toString();
        String wasmBinPath = Paths.get(LIQUID_DIR, contractDir, "target", contractDir + ".wasm").toString();
        File abiFile = new File(abiPath);
        File binFile = new File(wasmBinPath);
        try {
            byte[] bin = CommonUtils.readBytes(binFile);
            String binStr = Hex.toHexString(bin);
            String abi = FileUtils.readFileToString(abiFile);
            return new AbiBinInfo(abi, binStr);
        } catch (IOException e) {
            log.error("compileAndReturn get abi and bin error:", e);
            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED);
        }

    }


}
