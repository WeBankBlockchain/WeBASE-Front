/*
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.front.cert;

import com.webank.webase.front.base.enums.GMStatus;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.enums.CertTypes;
import com.webank.webase.front.base.exception.FrontException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 1. get Front's sdk's certs(.crt) in /resource
 * 2. get ca.crt/node.crt of the node which linked with Front
 *
 * guomi chain use double cert mechanism including:
 * 1. sign cert(gmca.crt, gmnode.crt, gmnode.key),
 * 2. encrypt cert(gmennode.crt, gmennode.key
 * 3. as well as original cert for sdk's TLS, same as standard chain
 */
@Slf4j
@Service
public class FrontCertService {
    private static final String crtContentHead = "-----BEGIN CERTIFICATE-----\n";
    private static final String crtContentTail = "-----END CERTIFICATE-----\n";
    private static final String nodeCrtPath = "/conf/node.crt";
    private static final String caCrtPath = "/conf/ca.crt";
    // 国密双证书模式
    // 国密证书
    private static final String gmNodeCrtPath = "/conf/gmnode.crt";
    private static final String gmCaCrtPath = "/conf/gmca.crt";
    // 国密加密证书
    private static final String gmEncryptCrtPath = "/conf/gmennode.crt";

    private static final String frontSdkCaCrt = "ca.crt";
    private static final String frontSdkNodeCrt = "node.crt";

    @Autowired
    Constants constants;
    /**
     * 设置了front对应的节点的目录，如/data/fisco/nodes/127.0.0.1/node0
     * 则获取 ${path}/conf 中的ca.crt, node.crt
     * 无需填agency.crt的读取，因为node.crt会包含node和agency的证书
     * @return List<String> 或者 String
     * 2019/12: support guomi: add encrypt node cert in resultList
     */
    // 0 is node ca, 1 is agency ca
    public List<String> getNodeCerts() {
        List<String> resList = new ArrayList<>();
        String nodePath = constants.getNodePath();
        log.debug("start getNodeCerts in {}" + nodePath);
        getCertListByPathAndType(nodePath, CertTypes.NODE.getValue(), resList);
        // gm cert added to resList
        if (EncryptType.encryptType == GMStatus.GUOMI.getValue()) {
            getCertListByPathAndType(nodePath, CertTypes.OTHERS.getValue(), resList);
        }
        log.debug("end getNodeCerts in {}" + nodePath);
        return resList;
    }


    public String getChainCert() {
        List<String> resList = new ArrayList<>();
        String nodePath = constants.getNodePath();
        log.debug("start getChainCert in {}" + nodePath);
        getCertListByPathAndType(nodePath, CertTypes.CHAIN.getValue(), resList);
        log.debug("end getChainCert in {}" + nodePath);
        if (resList.isEmpty()) {
            return "";
        }
        return resList.get(0);
    }

    /**
     * get Cert from crt file through nodePath and certType
     * @param nodePath
     * @param certType
     * @param targetList
     */
    private void getCertListByPathAndType(String nodePath, int certType, List<String> targetList) {
        log.debug("start tools: getCertList in nodePath:{},certType:{}", nodePath, certType);
        Path certPath = getCertPath(nodePath, certType);
        loadCrtContentByPath(certPath, targetList);
    }

    /**
     * get SDK crts in directory ~/resource/
     * including agency's crt, node.crt
     * excluding ca.crt because node's certs already including ca.crt
     * @return
     */
    public List<String> getSDKNodeCert() {
        List<String> sdkCertStrList = new ArrayList<>();
        log.debug("start getSDKNodeCerts.");
        // add sdk cert: node.crt
        loadCrtContentByStringPath(frontSdkNodeCrt, sdkCertStrList);
        loadCrtContentByStringPath(frontSdkCaCrt, sdkCertStrList);
        log.debug("end getSDKNodeCerts sdkCertStrt []" + sdkCertStrList);
        return sdkCertStrList;
    }

    /**
     * get crt file content in dir(String)
     * @param certPath
     * @param targetList
     */
    private void loadCrtContentByPath(Path certPath, List<String> targetList) {
        try(InputStream nodeCrtInput = Files.newInputStream(certPath)){
            String nodeCrtStr = inputStream2String(nodeCrtInput);
            String[] nodeCrtStrArray = nodeCrtStr.split(crtContentHead);
            for(String nodeCrtStrNoHead: nodeCrtStrArray) { //i=0时为空，跳过，i=1时进入第二次spilt，去除tail
                String[] nodeCrtStrArray2 = nodeCrtStrNoHead.split(crtContentTail); // i=1时，j=0是string, 因为\n去除了换行符，不包含j=1的情况
                for(String nodeCrtStrNoTail: nodeCrtStrArray2) {
                    // 去头尾的ca证书内容
                    String ca = nodeCrtStrNoTail;
                    if(ca.length() != 0) {
                        targetList.add(formatStr(ca));
                        log.debug("tools: loadCrtContentByPath add one:{}", formatStr(ca));
                    }
                }
            }
            log.debug("end tools: loadCrtContentByPath resultList:{}", targetList);
        } catch (IOException e) {
            log.error("FrontCertService loadCrtContentByPath, cert(.crt) path prefix error, Exception:[]", e);
            throw new FrontException("loadCrtContentByPath error:" + e.getMessage());
        } catch (Exception e) {
            log.error("FrontCertService loadCrtContentByPath error:[]", e);
            throw new FrontException("loadCrtContentByPath error:" + e.getMessage());
        }
    }

    /**
     * get crt file content in dir: /resource
     * @param crtFilePath
     * @return
     */
    public void loadCrtContentByStringPath(String crtFilePath, List<String> targetList) {
        try(InputStream nodeCrtInput = new ClassPathResource(crtFilePath).getInputStream()){
            String nodeCrtStr = inputStream2String(nodeCrtInput);
            String[] nodeCrtStrArray = nodeCrtStr.split(crtContentHead);
            for(String nodeCrtStrNoHead: nodeCrtStrArray) { //i=0时为空，跳过，i=1时进入第二次spilt，去除tail
                String[] nodeCrtStrArray2 = nodeCrtStrNoHead.split(crtContentTail); // i=1时，j=0是string, 因为\n去除了换行符，不包含j=1的情况
                for(String nodeCrtStrNoTail: nodeCrtStrArray2) {
                    // 去头尾的ca证书内容
                    String ca = nodeCrtStrNoTail;
                    if(ca.length() != 0) {
                        targetList.add(formatStr(ca));
                        log.debug("tools: loadCrtContentByPath add one:{}", formatStr(ca));
                    }
                }
            }
            log.debug("end tools: loadCrtContentByPath resultList:{}", targetList);
        } catch (IOException e) {
            log.error("FrontCertService loadCrtContentByStringPath, cert(.crt) path prefix error, Exception:[]", e);
            throw new FrontException("loadCrtContentByStringPath error:" + e.getMessage());
        } catch (Exception e) {
            log.error("FrontCertService loadCrtContentByStringPath error:[]", e);
            throw new FrontException("loadCrtContentByStringPath error:" + e.getMessage());
        }
    }

    public String inputStream2String(InputStream inputStream) throws IOException {
        return IOUtils.toString(inputStream,"UTF-8");
    }

    /**
     * get cert file's path through concatting nodePath with certType
     * @param nodePath
     * @param certType
     * @return
     * 2019/12 support guomi
     */
    public Path getCertPath(String nodePath, int certType) {
        if (certType == CertTypes.CHAIN.getValue()) {
            if (EncryptType.encryptType == 1){
                return Paths.get(nodePath.concat(gmCaCrtPath));
            }
            return Paths.get(nodePath.concat(caCrtPath));
        } else if (certType == CertTypes.NODE.getValue()) {
            if (EncryptType.encryptType == 1){
                return Paths.get(nodePath.concat(gmNodeCrtPath));
            }
            return Paths.get(nodePath.concat(nodeCrtPath));
        } else if(certType == CertTypes.OTHERS.getValue()){
            return getEncrytCertPath(nodePath);
        }
        return null;
    }

    public Path getEncrytCertPath(String nodePath) {
        if (EncryptType.encryptType == 1){
            return Paths.get(nodePath.concat(gmEncryptCrtPath));
        } else {
            return null;
        }
    }

    /**
     * remove the last character: "\n"
     * @param input
     */
    public String formatStr(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        return input.substring(0, input.length() - 1);
    }
}
