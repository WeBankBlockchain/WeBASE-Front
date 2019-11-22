/*
 * Copyright 2014-2019  the original author or authors.
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

import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.base.enums.CertTypes;
import com.webank.webase.front.base.exception.FrontException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
 */
@Slf4j
@Service
public class FrontCertService {
    private static final String crtContentHead = "-----BEGIN CERTIFICATE-----\n";
    private static final String crtContentTail = "-----END CERTIFICATE-----\n";
    private static final String nodeCrtPath = "/conf/node.crt";
    private static final String caCrtPath = "/conf/ca.crt";

    @Autowired
    Constants constants;
    /**
     * 设置了front对应的节点的目录，如/data/fisco/nodes/127.0.0.1/node0
     * 则获取 ${path}/conf 中的ca.crt, node.crt
     * 无需填agency.crt的读取，因为node.crt会包含node和agency的证书
     * @return List<String> 或者 String
     */
    // 0 is node ca, 1 is agency ca
    public List<String> getNodeCerts() {
        List<String> resList = new ArrayList<>();
        String nodePath = constants.getNodePath();
        log.debug("start getNodeCerts in {}" + nodePath);
        getCertList(nodePath, CertTypes.NODE.getValue(), resList);
        log.debug("end getNodeCerts in {}" + nodePath);
        return resList;
    }


    public String getChainCert() {
        List<String> resList = new ArrayList<>();
        String nodePath = constants.getNodePath();
        log.debug("start getChainCert in {}" + nodePath);
        getCertList(nodePath, CertTypes.CHAIN.getValue(), resList);
        log.debug("end getChainCert in {}" + nodePath);
        return resList.get(0);
    }

    /**
     * get Cert from crt file through nodePath and certType
     * @param nodePath
     * @param certType
     * @param resultList
     */
    private void getCertList(String nodePath, int certType, List<String> resultList) {
        log.debug("start tools: getCertList in nodePath:{},certType:{}", nodePath, certType);

        Path certPath = getCertPath(nodePath, certType);
        try(InputStream nodeCrtInput = Files.newInputStream(certPath)){
            String nodeCrtStr = inputStream2String(nodeCrtInput);
            String[] nodeCrtStrArray = nodeCrtStr.split(crtContentHead);
            for(String nodeCrtStrNoHead: nodeCrtStrArray) { //i=0时为空，跳过，i=1时进入第二次spilt，去除tail
                String[] nodeCrtStrArray2 = nodeCrtStrNoHead.split(crtContentTail); // i=1时，j=0是string, 因为\n去除了换行符，不包含j=1的情况
                for(String nodeCrtStrNoTail: nodeCrtStrArray2) {
                    // 去头尾的ca证书内容
                    String ca = nodeCrtStrNoTail;
                    if(ca.length() != 0) {
                        resultList.add(formatStr(ca));
                        log.debug("tools: getCertList add one:{}", formatStr(ca));
                    }
                }
            }
            log.debug("end tools: getCertList resultList:{}", resultList);

        }catch (IOException e) {
            log.error("FrontCertService getCertList, cert(.crt) path prefix error, Exception:[]", e);
            throw (FrontException)new FrontException("FileNotFound, cert(.crt) path error").initCause(e);
        }
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
        try(InputStream nodeCrtInput = new ClassPathResource("node.crt").getInputStream()){
            String nodeCrtStr = inputStream2String(nodeCrtInput);
            String[] nodeCrtStrArray = nodeCrtStr.split(crtContentHead);
            for(String nodeCrtStrNoHead: nodeCrtStrArray) { //i=0时为空，跳过，i=1时进入第二次spilt，去除tail
                String[] nodeCrtStrArray2 = nodeCrtStrNoHead.split(crtContentTail); // i=1时，j=0是string, 因为\n去除了换行符，不包含j=1的情况
                for(String nodeCrtStrNoTail: nodeCrtStrArray2) {
                    // 去头尾的ca证书内容
                    String ca = nodeCrtStrNoTail;
                    if(ca.length() != 0) {
                        sdkCertStrList.add(formatStr(ca));
                    }
                }
            }
        }catch (IOException e) {
            log.error("FrontCertService getCertList, cert(.crt) path prefix error, Exception:[]", e);
            throw (FrontException)new FrontException("FileNotFound, cert(.crt) path error").initCause(e);
        }
        log.debug("end getSDKNodeCerts sdkCertStrt {}" + sdkCertStrList);
        return sdkCertStrList;
    }


    public String inputStream2String(InputStream inputStream) throws IOException {
        String str = IOUtils.toString(inputStream,"UTF-8");
        return str;
    }

    /**
     * get cert file's path through concatting nodePath with certType
     * @param nodePath
     * @param certType
     * @return
     */
    public Path getCertPath(String nodePath, int certType) {
        if(certType == CertTypes.CHAIN.getValue()) {
            return Paths.get(nodePath.concat(caCrtPath));
        }else if(certType == CertTypes.NODE.getValue()) {
            return Paths.get(nodePath.concat(nodeCrtPath));
        }
        return null;
    }

    // remove the last character: "\n"
    public String formatStr(String string) {
        return string.substring(0, string.length() - 1);
    }
}
