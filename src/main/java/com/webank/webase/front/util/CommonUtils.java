/*
 * Copyright 2012-2019 the original author or authors.
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
package com.webank.webase.front.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.Sign.SignatureData;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.exception.FrontException;
import lombok.extern.slf4j.Slf4j;

/**
 * CommonUtils.
 *
 */
@Slf4j
public class CommonUtils {

    private CommonUtils() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * stringToSignatureData.
     * 
     * @param signatureData signatureData
     * @return
     */
    public static SignatureData stringToSignatureData(String signatureData) {
        byte[] byteArr = Numeric.hexStringToByteArray(signatureData);
        byte[] signR = new byte[32];
        System.arraycopy(byteArr, 1, signR, 0, signR.length);
        byte[] signS = new byte[32];
        System.arraycopy(byteArr, 1 + signR.length, signS, 0, signS.length);
        return new SignatureData(byteArr[0], signR, signS);
    }

    /**
     * signatureDataToString.
     * 
     * @param signatureData signatureData
     * @return
     */
    public static String signatureDataToString(SignatureData signatureData) {
        byte[] byteArr = new byte[1 + signatureData.getR().length + signatureData.getS().length];
        byteArr[0] = signatureData.getV();
        System.arraycopy(signatureData.getR(), 0, byteArr, 1, signatureData.getR().length);
        System.arraycopy(signatureData.getS(), 0, byteArr, signatureData.getR().length + 1,
                signatureData.getS().length);
        return Numeric.toHexString(byteArr, 0, byteArr.length, false);
    }
    
    /**
     * parse Byte to HexStr.
     * 
     * @param buf byte
     * @return
     */
    public static String parseByte2HexStr(byte[] buf) {
        log.info("parseByte2HexStr start...");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        log.info("parseByte2HexStr end...");
        return sb.toString();
    }

    /**
     * parse String to HexStr.
     * 
     * @param str String
     * @return
     */
    public static String parseStr2HexStr(String str) {
        if (StringUtils.isBlank(str)) {
            return "0x0";
        }
        return  "0x" + Integer.toHexString(Integer.valueOf(str));
    }

    /**
     * base64Decode.
     * 
     * @param str String
     * @return
     */
    public static byte[] base64Decode(String str) {
        if (str == null) {
          return  new byte[0];
        }
        return Base64.getDecoder().decode(str);
    }

    /**
     * read File.
     * 
     * @param filePath filePath
     * @return
     */
    public static String readFile(String filePath) throws IOException {
        log.info("readFile dir:{}", filePath);
        File dirFile = new File(filePath);
        if (!dirFile.exists()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        try(
                InputStream inputStream = new FileInputStream(dirFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            throw new FrontException(e.getMessage());
        }
        return result.toString();
    }
    
    /**
     * read File.
     * 
     * @param filePath filePath
     * @return
     */
    public static List<String> readFileToList(String filePath) throws IOException {
    	log.info("readFile dir:{}", filePath);
    	File dirFile = new File(filePath);
    	if (!dirFile.exists()) {
    		return null;
    	}
    	List<String> result = new ArrayList<String>();
        try (
                InputStream inputStream = new FileInputStream(dirFile);
                BufferedReader  reader = new BufferedReader(new InputStreamReader(inputStream));
        )
        {
            String line ;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (Exception e) {
            throw new FrontException(e.getMessage());
        }
    	return result;
    }

    /**
     * delete single File.
     * 
     * @param filePath filePath
     * @return
     */
    public static boolean deleteFile(String filePath) {
        boolean flag = false;
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * delete Files.
     * 
     * @param path path
     * @return
     */
    public static boolean deleteFiles(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        if (files == null) {
            return false;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                flag = deleteFiles(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
     return flag;
    }

    /**
     * set HttpHeaders.
     * 
     * @return
     */
    public static HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        return headers;
    }

    /**
     * Object to JavaBean.
     * 
     * @param obj obj
     * @param clazz clazz
     * @return
     */
    public static <T> T object2JavaBean(Object obj, Class<T> clazz) {
        if (obj == null || clazz == null) {
            log.warn("Object2JavaBean. obj or clazz null");
            return null;
        }
        String jsonStr = JSON.toJSONString(obj);
        return JSON.parseObject(jsonStr, clazz);
    }

    /**
     * get server ip.
     * 
     * @return
     */
    public static String getCurrentIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni =  networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia =  nias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address) {
                        return ia.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.error("getCurrentIp error.");
        }
        return null;
    }
}
