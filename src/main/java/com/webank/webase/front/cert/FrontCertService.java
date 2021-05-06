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

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.CertTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.contract.entity.FileContentHandle;
import com.webank.webase.front.util.CleanPathUtil;
import com.webank.webase.front.util.ZipUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.CryptoType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

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
    // 前置配置的节点目录下的证书
    private static final String nodeCrtPath = "/conf/node.crt";
    private static final String caCrtPath = "/conf/ca.crt";
    // 国密双证书模式
    // 国密证书
    private static final String gmNodeCrtPath = "/conf/gmnode.crt";
    private static final String gmCaCrtPath = "/conf/gmca.crt";
    // 国密加密证书
    private static final String gmEncryptCrtPath = "/conf/gmennode.crt";

    // 前置的sdk证书
    private static final String frontSdkCaCrt = "ca.crt";
    private static final String frontSdkNodeCrt = "sdk.crt";
    // add in v1.5.0
    private static final String frontSdkNodeKey = "sdk.key";
    private static final String frontGmSdkCaCrt = "gm/gmca.crt";
    private static final String frontGmSdkNodeCrt = "gm/gmsdk.crt";
    private static final String frontGmSdkNodeKey = "gm/gmsdk.key";
    private static final String frontGmEnSdkNodeCrt = "gm/gmensdk.crt";
    private static final String frontGmEnSdkNodeKey = "gm/gmensdk.key";
    // v1.5.0 add sdk key
    private final static String TEMP_SDK_DIR = "sdk";
    private final static String TEMP_ZIP_DIR = "tempZip";
    private final static String TEMP_ZIP_FILE_NAME = "conf.zip";
    private final static String TEMP_ZIP_FILE_PATH = TEMP_ZIP_DIR + File.separator + TEMP_ZIP_FILE_NAME;


    @Autowired
    Constants constants;
    @Autowired
    @Qualifier("common")
    private CryptoSuite cryptoSuite;
    @Autowired
    private BcosSDK bcosSDK;

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
        if (cryptoSuite.cryptoTypeConfig == CryptoType.SM_TYPE) {
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
     * get SDK crts in directory ~/resource/
     * including agency's crt, node.crt
     * excluding ca.crt because node's certs already including ca.crt
     * @return
     */
    public List<String> getSDKNodeCert() {
        List<String> sdkCertMap = new ArrayList<>();
        log.debug("start getSDKNodeCerts.");
        // add sdk cert: node.crt
        // v1.5.0 change node.crt to sdk.crt
        loadCrtContentByStringPath(frontSdkNodeCrt, sdkCertMap);
        loadCrtContentByStringPath(frontSdkCaCrt, sdkCertMap);
        log.debug("end getSDKNodeCerts sdkCertStr []" + sdkCertMap);
        return sdkCertMap;
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
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * get cert file's path through concat nodePath with certType
     * @param nodePath the fisco node front connecting with, ex: /data/fisco/nodes/127.0.0.1/node0
     * @param certType
     * @return
     * 2019/12 support guomi
     */
    public Path getCertPath(String nodePath, int certType) {
        if (certType == CertTypes.CHAIN.getValue()) {
            if (cryptoSuite.cryptoTypeConfig == CryptoType.SM_TYPE) {
                return Paths.get(CleanPathUtil.cleanString(nodePath.concat(gmCaCrtPath)));
            }
            return Paths.get(CleanPathUtil.cleanString(nodePath.concat(caCrtPath)));
        } else if (certType == CertTypes.NODE.getValue()) {
            if (cryptoSuite.cryptoTypeConfig == CryptoType.SM_TYPE) {
                return Paths.get(CleanPathUtil.cleanString(nodePath.concat(gmNodeCrtPath)));
            }
            return Paths.get(CleanPathUtil.cleanString(nodePath.concat(nodeCrtPath)));
        } else if(certType == CertTypes.OTHERS.getValue()) {
            if (cryptoSuite.cryptoTypeConfig == CryptoType.SM_TYPE) {
                return Paths.get(CleanPathUtil.cleanString(nodePath.concat(gmEncryptCrtPath)));
            } else {
                return null;
            }
        }
        return null;
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

    /* v1.5.0: add read sdk cert and key file for node-manager */

    /**
     * read sdk key and cert file (ca.crt, sdk.key, sdk.crt)
     * get SDK crts in directory ~/resource/
     * including agency's crt, node.crt
     * excluding ca.crt because node's certs already including ca.crt
     * @return
     */
    public Map<String, String> getSDKCertKeyMap() {
        Map<String, String> sdkCertMap = new HashMap<>();
        log.info("start getSDKCertKeyMap sslType:{}.", bcosSDK.getSSLCryptoType() );
        // add sdk cert: node.crt
        loadBareSdkContent(frontSdkNodeCrt, sdkCertMap);
        loadBareSdkContent(frontSdkNodeKey, sdkCertMap);
        loadBareSdkContent(frontSdkCaCrt, sdkCertMap);
        // if use gm ssl
        if (bcosSDK.getSSLCryptoType() == CryptoType.SM_TYPE) {
            loadBareSdkContent(frontGmSdkCaCrt, sdkCertMap);
            loadBareSdkContent(frontGmSdkNodeCrt, sdkCertMap);
            loadBareSdkContent(frontGmSdkNodeKey, sdkCertMap);
            loadBareSdkContent(frontGmEnSdkNodeCrt, sdkCertMap);
            loadBareSdkContent(frontGmEnSdkNodeKey, sdkCertMap);
        }
        log.debug("end getSDKCertKeyMap sdkCertStr:{}", sdkCertMap);
        return sdkCertMap;
    }

    /**
     * get crt file content in dir(String)
     * @param sdkFileStr
     * @param targetMap as return result
     */
    private void loadBareSdkContent(String sdkFileStr, Map<String, String> targetMap) {
        log.debug("start loadBareSdkContent sdkFileStr:{}", sdkFileStr);
        try(InputStream nodeCrtInput = new ClassPathResource(sdkFileStr).getInputStream()){
            String nodeCrtStr = inputStream2String(nodeCrtInput);
            String fileName = sdkFileStr;
            // if gm/gmca.crt, then fileName=gmca.crt
            if (sdkFileStr.contains("/")) {
                String[] gmCertStrArr = sdkFileStr.split("/");
                if (gmCertStrArr.length >= 2) {
                    fileName = gmCertStrArr[gmCertStrArr.length - 1];
                }
            }
            targetMap.put(fileName, nodeCrtStr);
            log.debug("end tools: loadBareSdkContent resultList:{}", targetMap);
        } catch (IOException e) {
            log.error("FrontCertService loadBareSdkContent, cert(.crt) path prefix error, Exception:[]", e);
            throw new FrontException(ConstantCode.CERT_FILE_NOT_FOUND.getCode(),
                "loadBareSdkContent error:" + e.getMessage());
        } catch (Exception e) {
            log.error("FrontCertService loadBareSdkContent error:[]", e);
            throw new FrontException(ConstantCode.CERT_FILE_NOT_FOUND.getCode(),
                "loadBareSdkContent error:" + e.getMessage());
        }
    }


    /**
     * get sdk cert key files' zip
     * @return
     */
    public synchronized FileContentHandle getFrontSdkZipFile() {
        // get sdk cert content
        Map<String, String> sdkContentMap = this.getSDKCertKeyMap();
        if (sdkContentMap.isEmpty()) {
            throw new FrontException(ConstantCode.SDK_KEY_FILE_NOT_FOUND);
        }
        // get if guomi sdk
        String key = sdkContentMap.keySet().iterator().next();
        boolean useGm = key.contains("gm");

        // if guomi, create conf/gm, else create conf/
        File sdkDir;
        if (useGm) {
            sdkDir = new File(TEMP_SDK_DIR + File.separator + "gm");
        } else {
            sdkDir = new File(TEMP_SDK_DIR);
        }
        log.info("writeSdkAsFile sdkDir:{}", sdkDir);
        // create dir and zip
        writeSdkFilesAndZip(sdkContentMap, sdkDir, useGm);
        try {
            // FileInputStream would be closed by web
            return new FileContentHandle(TEMP_ZIP_FILE_NAME, new FileInputStream(TEMP_ZIP_FILE_PATH));
        } catch (IOException e) {
            log.error("getFrontSdkFiles fail:[]", e);
            throw new FrontException(ConstantCode.WRITE_SDK_CRT_KEY_FILE_FAIL);
        }
    }

    private void writeSdkFilesAndZip(Map<String, String> sdkContentMap, File sdkDir, boolean useGm) {
        this.writeSdkAsFile(sdkContentMap, sdkDir);
        // zip the directory of conf(guomi: conf/gm)
        String gmDirInZip = useGm ? "gm" : "";
        try {
            ZipUtils.generateZipFile(sdkDir.getPath(), TEMP_ZIP_DIR, gmDirInZip, TEMP_ZIP_FILE_NAME);
        } catch (Exception e) {
            log.error("writeSdkAsFile generateZipFile fail:[]", e);
            throw new FrontException(ConstantCode.WRITE_SDK_CRT_KEY_FILE_FAIL);
        }

        // rm conf dir
        boolean resultDel = sdkDir.delete();
        log.info("delete for temp sdk file, result:{}", resultDel);
    }

    /**
     * write sdk file of ca.crt, sdk.crt, sdk.key
     * @param sdkContentMap
     * @param sdkDir sdk file output directory
     * @return
     */
    public void writeSdkAsFile(Map<String, String> sdkContentMap, File sdkDir) {

        // create sdk dir
        if (sdkDir.exists()) {
            boolean result = sdkDir.delete();
            log.info("delete existed gm dir, result:{}", result);
        }
        boolean result = sdkDir.mkdirs();
        log.info("mkdir for temp sdk file, result:{}", result);

        // write each content to each file in conf/ or conf/gm/
        // gm: gmca.crt, gmsdk.crt, gmsdk.key
        // else: ca.crt, sdk.crt, sdk.key
        for (String fileName : sdkContentMap.keySet()) {
            Path sdkFilePath = Paths
                .get(CleanPathUtil.cleanString(sdkDir.getPath() + File.separator + fileName));
            String fileContent = sdkContentMap.get(fileName);
            log.info("writeSdkAsFile sdkPath:{}, content:{}", sdkFilePath, fileContent);
            try (BufferedWriter writer = Files
                .newBufferedWriter(sdkFilePath, StandardCharsets.UTF_8)) {
                // write to relative path
                writer.write(fileContent);
            } catch (IOException e) {
                log.error("writeSdkAsFile fail:[]", e);
                throw new FrontException(ConstantCode.WRITE_SDK_CRT_KEY_FILE_FAIL);
            }
        }
    }


}
