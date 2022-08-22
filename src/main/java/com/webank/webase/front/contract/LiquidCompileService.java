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

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.contract.entity.wasm.AbiBinInfo;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.cmd.ExecuteResult;
import com.webank.webase.front.util.cmd.JavaCommandExecutor;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * use command line to compile liquid contract and return abi & bin
 * node-mgr执行编译的时候，可以选择front（选择有环境的front）
 */
@Slf4j
@Service
public class LiquidCompileService {

    public static final String LIQUID_DIR = "liquid";
    @Autowired
    private Constants constants;


    public void checkLiquidEnv() {
        String commandCargo = "cargo -V";
        // cargo -V
        //cargo 1.56.0 (4ed5d137b 2021-10-04)
        String commandRustc = "rustc -V";
        // rustc -V
        //rustc 1.56.1 (59eed8a2a 2021-11-01)

        ExecuteResult result = JavaCommandExecutor.executeCommand(commandCargo, constants.getCommandLineTimeout());
        if (result.failed()) {
            throw new FrontException(ConstantCode.LIQUID_ENV_NOT_CONFIG.attach(result.getExecuteOut()));
        }

       ExecuteResult resultRustc = JavaCommandExecutor.executeCommand(commandRustc, constants.getCommandLineTimeout());
        if (resultRustc.failed()) {
            throw new FrontException(ConstantCode.LIQUID_ENV_NOT_CONFIG.attach(resultRustc.getExecuteOut()));
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
     * groupId + _ + contractPath + _ + contractName
     * @param groupId
     * @param contractPath
     * @param contractName
     * @param contractSourceBase64 base64 encoded, required decode before write to file
     */
    public void execLiquidNewContract(String groupId, String contractPath, String contractName, String contractSourceBase64) {
        this.mkdirIfNotExist();
        String contractDir = getContractDir(groupId, contractPath, contractName);
        String contractLiquidPath = getLiquidContractPath(contractDir);

        // check contractDir exist, if exist, delete
        File check = new File(contractLiquidPath);
        if (check.exists()) {
            log.warn("execLiquidNewContract [{}] already exist, skip new contract", contractLiquidPath);
            boolean result = check.delete();
            log.info("execLiquidNewContract delete old contract, result:{}", result);
        }

        // new liquid project, cargo liquid new contract XXX
        String command = String.format("cd %s && cargo liquid new contract %s", LIQUID_DIR, contractDir);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constants.getCommandLineTimeout());
        log.info("execNewContract new contract Result:{}", result);
        if (result.failed()) {
            throw new FrontException(ConstantCode.LIQUID_NEW_PROJECT_FAILED.attach(result.getExecuteOut()));
        }

        // cd $contractDir
        // sed github as gitee
        // mac add "", linux not need
        String osName = System.getProperty("os.name");
        log.info("now os is [{}]", osName);
        boolean isMacOS = osName.contains("Mac OS");
        String sed = String.format("sed -i %s \"s-https://github.com/WeBankBlockchain-https://gitee.com/WeBankBlockchain-g\" %s/Cargo.toml", isMacOS ? "\"\"" : "", contractLiquidPath);
        String sed2 = String.format("sed -i %s \"s-https://github.com/WeBankBlockchain-https://gitee.com/WeBankBlockchain-g\" %s/.liquid/abi_gen/Cargo.toml", isMacOS ? "\"\"" : "", contractLiquidPath);
        ExecuteResult sedResult = JavaCommandExecutor.executeCommand(sed, constants.getCommandLineTimeout());
        ExecuteResult sedResult2 = JavaCommandExecutor.executeCommand(sed2, constants.getCommandLineTimeout());
        log.info("execNewContract sedResult:{},sedResult2:{}", sedResult, sedResult2);
        if (sedResult.failed()||sedResult2.failed()) {
            throw new FrontException(ConstantCode.LIQUID_NEW_PROJECT_SED_GITEE_FAILED.attach(sedResult.getExecuteOut()));
        }

        //  write contract source: lib.rs
        File contractFile = Paths.get(LIQUID_DIR, contractDir, "src", "lib.rs").toFile();
        try {
            FileUtils.write(contractFile, CommonUtils.base64Decode(contractSourceBase64), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("execNewContract save contract source code error:", e);
            throw new FrontException(ConstantCode.WRITE_CONTRACT_SOURCE_FAILED.attach(e.getMessage()));
        }

    }

//    public void execCargoTest(String groupId, String contractName, int compileTimeout) {
//        String contractDir = groupId + "_" + contractName;
//        String testCommand = "cargo test";
//        String command = String.format("cd %s && %s", getLiquidContractPath(contractDir), testCommand);
//        ExecuteResult result = JavaCommandExecutor.executeCommand(command, compileTimeout);
//        log.info("execTest result:{}", result);
//        if (result.failed()) {
//            throw new FrontException(ConstantCode.EXEC_JAVA_COMMAND_RETURN_FAILED.attach(result.getExecuteOut()));
//        }
//    }

    /**
     * cargo liquid build
     * @param contractDir
     */
    public void execLiquidCompile(boolean useSm2, String contractDir, int compileTimeout) {
        // cargo liquid build
        String testCommand = "cargo liquid build --skip-analysis";
        if (useSm2) {
            testCommand = "cargo liquid build -g --skip-analysis";
        }
        String command = String.format("cd %s && %s", getLiquidContractPath(contractDir), testCommand);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, compileTimeout);
        log.info("execCompile result:{}", result);
        if (result.failed()) {
            throw new FrontException(ConstantCode.LIQUID_COMPILE_FAILED.attach(result.getExecuteOut()));
        }
    }

    /**
     * check contract project dir exist before build
     * @param groupId
     * @param contractName
     * @return
     */
    public AbiBinInfo compileAndReturn(String groupId, String contractPath,
                                       String contractName, int compileTimeout, boolean useSm2) {
        String contractDir = getContractDir(groupId, contractPath, contractName);
        // compile
        this.execLiquidCompile(useSm2, contractDir, compileTimeout);
        // check target dir exist, 如果已存在则删除，因为此时要重新编译
        String targetPath = Paths.get(LIQUID_DIR, contractDir, "target").toString();
        File targetDirFile = new File(targetPath);
        if (!targetDirFile.exists()) {
            log.error("compileAndReturn target directory not exist, compile failed!");
            throw new FrontException(ConstantCode.LIQUID_TARGET_FILE_NOT_EXIST);
        }

        // read .abi and .wasm(byte to hexString)
        String abiPath = Paths.get(LIQUID_DIR, contractDir, "target", contractDir + ".abi").toString();
        String wasmBinPath = Paths.get(LIQUID_DIR, contractDir, "target", contractDir + ".wasm").toString();
        if (useSm2) {
            // xxx + _gm.wasm
            wasmBinPath = Paths.get(LIQUID_DIR, contractDir, "target", contractDir + "_gm.wasm").toString();
        }
        File abiFile = new File(abiPath);
        File binFile = new File(wasmBinPath);
        try {
            byte[] bin = CommonUtils.readBytes(binFile);
            String binStr = Hex.toHexString(bin);
            String abi = FileUtils.readFileToString(abiFile);
            AbiBinInfo abiBinInfo = new AbiBinInfo(abi, binStr);
            log.info("compileAndReturn abiBin:{}", abiBinInfo);
            return abiBinInfo;
        } catch (IOException e) {
            log.error("compileAndReturn get abi and bin error:", e);
            throw new FrontException(ConstantCode.LIQUID_READ_ABI_BIN_FAILED.attach(e.getMessage()));
        }
    }


    private static String getContractDir(String groupId, String contractPath, String contractName) {
        // if contractPath is "/"
        if ("/".equals(contractPath)) {
            contractPath = "";
        }
        return groupId + "_" + contractPath + "_" + contractName;
    }

    public static String getLiquidContractPath(String contractDir) {
        String path = Paths.get(LIQUID_DIR, contractDir).toString();
        log.info("getLiquidContractPath path:{}", path);
        return path;
    }

    public static String getLiquidTargetPath(String groupId, String contractPath, String contractName) {
        String contractDir = getContractDir(groupId, contractPath, contractName);
        // check target dir exist, 如果已存在则删除，因为此时要重新编译
        return Paths.get(LIQUID_DIR, contractDir, "target").toString();
    }
}
