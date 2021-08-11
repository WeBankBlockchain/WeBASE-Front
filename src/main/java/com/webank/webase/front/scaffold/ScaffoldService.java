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

import com.webank.scaffold.artifact.NewMainResourceDir.ContractInfo;
import com.webank.scaffold.factory.WebaseProjectFactory;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.NodeConfig;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.cert.FrontCertService;
import com.webank.webase.front.contract.ContractService;
import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.scaffold.entity.ReqProject;
import com.webank.webase.front.scaffold.entity.RspFile;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.NetUtils;
import com.webank.webase.front.util.ZipUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.CryptoType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * use webank-blockchain-smart-dev-scaffold
 * to generate demo project of contract
 * @author marsli
 */
@Slf4j
@Service
public class ScaffoldService {
    @Autowired
    private ContractService contractService;
    @Qualifier("common")
    @Autowired
    private CryptoSuite cryptoSuite;
    @Autowired
    private FrontCertService certService;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private NodeConfig nodeConfig;

    private static final String GRADLE_WRAPPER_DIR = "gradle";

    private static final String OUTPUT_DIR = "output";
    private static final String ZIP_SUFFIX = ".zip";
    private static final String OUTPUT_ZIP_DIR = OUTPUT_DIR + File.separator + "zip";

    /**
     * generate by contract with sol
      */
    public RspFile exportProject(ReqProject reqProject) {
        String artifactName = reqProject.getArtifactName();
        // check dir exist
        File checkProjectDir = new File(OUTPUT_DIR + File.separator + artifactName);
        if (checkProjectDir.exists()) {
            boolean result = CommonUtils.deleteDir(checkProjectDir);
            log.warn("exportProject dir exist: {}, now delete it result:{}", artifactName, result);
        }
        // get contract info list
        List<Integer> contractIdList = reqProject.getContractIdList();
        List<Contract> tbContractList = new ArrayList<>();
        for (Integer id : contractIdList) {
            Contract contract = contractService.findById(id.longValue());
            // if contract abi is null, not compile
            // if abi is [](empty list), compile already
            if (contract == null || contract.getContractAbi() == null) {
                log.error("exportProject contract not exist or abi empty, id:{}", id);
                throw new FrontException(ConstantCode.INVALID_CONTRACT_ID);
            }
            tbContractList.add(contract);
        }
        // get front's p2p ip and channel port
        NodeConfig thisConfig = new NodeConfig();
        BeanUtils.copyProperties(nodeConfig, thisConfig);
        thisConfig.setP2pip(reqProject.getChannelIp());
        // set port, if param not null, set as param's port
        // if param null, response from front not null, set as response's port
        // else all null, set as 20200 default value
        if (reqProject.getChannelPort() != null) {
            thisConfig.setChannelPort(String.valueOf(reqProject.getChannelPort()));
        } else if (StringUtils.isBlank(nodeConfig.getChannelPort())) {
            thisConfig.setChannelPort("20200");
        }
        log.info("exportProject get thisConfig:{}", thisConfig);
        // get front's sdk key cert
        Map<String, String> sdkMap = certService.getSDKCertKeyMap();
        log.info("exportProject get sdkMap size:{}", sdkMap.size());
        // get user private key if set
        List<String> userAddressList = reqProject.getUserAddressList();
        String hexPrivateKeyListStr = "";
        if (userAddressList != null && !userAddressList.isEmpty()) {
            hexPrivateKeyListStr = this.handleUserList(userAddressList);
            //hexPrivateKeyListStr = keyStoreService.getPrivateKey(userAddressList.get(0));
        }
        // generate
        String projectPath = this.generateProject(thisConfig, reqProject.getGroup(), reqProject.getArtifactName(),
            tbContractList, reqProject.getGroupId(), hexPrivateKeyListStr, sdkMap);
        String zipFileName = artifactName + ZIP_SUFFIX;
        try {
            ZipUtils.generateZipFile(projectPath, OUTPUT_ZIP_DIR, artifactName, zipFileName);
        } catch (Exception e) {
            log.error("exportProject generateZipFile failed:[]", e);
            // if failed, delete project dir
            boolean result = checkProjectDir.delete();
            log.error("zip failed, now delete project dir, result:{}", result);
        }
        String zipFileFullPath = OUTPUT_ZIP_DIR + File.separator + zipFileName;
        log.info("exportProject zipFileName:{}, zipFileFullPath{}", zipFileName, zipFileFullPath);
        RspFile rspFile = new RspFile();
        rspFile.setFileName(zipFileName);
        rspFile.setFileStreamBase64(CommonUtils.fileToBase64(zipFileFullPath));
        return rspFile;
    }

    /**
     * generate project
     * @param nodeConfig ip channel port
     * @param projectGroup
     * @param artifactName
     * @param tbContractList
     * @param groupId
     * @param hexPrivateKeyListStr
     * @param sdkMap
     * @return path string of project
     */
    public String generateProject(NodeConfig nodeConfig, String projectGroup, String artifactName,
        List<Contract> tbContractList, int groupId, String hexPrivateKeyListStr, Map<String, String> sdkMap) {
        log.info("generateProject sdkMap size:{}", sdkMap.size());
        List<ContractInfo> contractInfoList = this.handleContractList(tbContractList);
        String frontChannelIpPort = nodeConfig.getP2pip() + ":" + nodeConfig.getChannelPort();
        WebaseProjectFactory projectFactory = new WebaseProjectFactory();
        log.info("generateProject projectGroup:{},artifactName:{},OUTPUT_DIR:{},frontChannelIpPort:{},groupId:{}",
            projectGroup, artifactName, OUTPUT_DIR, frontChannelIpPort, groupId);
        try {
            projectFactory.buildProjectDirWebase(contractInfoList,
                projectGroup, artifactName, OUTPUT_DIR, GRADLE_WRAPPER_DIR,
                frontChannelIpPort, groupId, hexPrivateKeyListStr, sdkMap);
        } catch (Exception e) {
            log.error("generateProject error:[]", e);
            throw new FrontException(ConstantCode.GENERATE_CONTRACT_PROJECT_FAIL);
        }
        String projectDir = OUTPUT_DIR + File.separator + artifactName;
        log.info("generateProject result:{}", projectDir);
        return projectDir;
    }

    private List<ContractInfo> handleContractList(List<Contract> contractList) {
        log.info("handleContractList contractList size:{}", contractList.size());
        List<ContractInfo> contractInfoList = new ArrayList<>();
        for (Contract contract : contractList) {
            String sourceCodeBase64 = contract.getContractSource();
            String solSourceCode = new String(Base64.getDecoder().decode(sourceCodeBase64));
            String contractName = contract.getContractName();
            String contractAddress = contract.getContractAddress();
            String contractAbi = contract.getContractAbi();
            String bytecodeBin = contract.getBytecodeBin();

            ContractInfo contractInfo = new ContractInfo();
            contractInfo.setSolRawString(solSourceCode);
            contractInfo.setContractName(contractName);
            contractInfo.setContractAddress(contractAddress);
            contractInfo.setAbiStr(contractAbi);
            if (cryptoSuite.getCryptoTypeConfig() == CryptoType.SM_TYPE) {
                contractInfo.setBinStr("");
                contractInfo.setSmBinStr(bytecodeBin);
            } else {
                contractInfo.setBinStr(bytecodeBin);
                contractInfo.setSmBinStr("");
            }
            contractInfoList.add(contractInfo);
        }
        log.info("handleContractList result contractInfoList size:{}", contractInfoList.size());
        return contractInfoList;
    }

    /**
     * userP12PathList
     * @return: String of list, e.g. 123,123,123
     */
    private String handleUserList(List<String> userAddressList) {
        List<String> keyList = new ArrayList<>();
        for (String address : userAddressList) {
            String hexPrivateKey = "";
            if (StringUtils.isNotBlank(address)) {
                hexPrivateKey = keyStoreService.getPrivateKey(address);
            }
            log.info("exportProject get hexPrivateKey length:{}", hexPrivateKey.length());
            keyList.add(hexPrivateKey);
        }
        return StringUtils.join(keyList, ",");
    }

    /**
     * telnet channel port to check reachable
     * @param nodeIp
     * @param channelPort
     * @return
     */
    public Boolean telnetChannelPort(String nodeIp, int channelPort) {
        Pair<Boolean, Integer> telnetResult = NetUtils.checkPorts(nodeIp, 2000, channelPort);
        // if true, telnet success, port is in use, which means node's channelPort is correct
        log.info("telnet {}:{} result:{}", nodeIp, channelPort, telnetResult);
        return telnetResult.getLeft();
    }
}
