/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.util;

import com.webank.webase.front.base.exception.FrontException;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.Sign.SignatureData;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * CommonUtils.
 *
 */
@Slf4j
public class CommonUtils {

    public static final int PUBLIC_KEY_LENGTH_64 = 64;

    private CommonUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * stringToSignatureData. 19/12/24 support guomi： add byte[] pub in signatureData
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
        if (EncryptType.encryptType == 1) {
            byte[] pub = new byte[64];
            System.arraycopy(byteArr, 1 + signR.length + signS.length, pub, 0, pub.length);
            return new SignatureData(byteArr[0], signR, signS, pub);
        } else {
            return new SignatureData(byteArr[0], signR, signS);
        }
    }

    /**
     * signatureDataToString. 19/12/24 support guomi： add byte[] pub in signatureData
     * 
     * @param signatureData signatureData
     */
    public static String signatureDataToString(SignatureData signatureData) {
        byte[] byteArr;
        if (EncryptType.encryptType == 1) {
            byteArr = new byte[1 + signatureData.getR().length + signatureData.getS().length
                    + PUBLIC_KEY_LENGTH_64];
            byteArr[0] = signatureData.getV();
            System.arraycopy(signatureData.getR(), 0, byteArr, 1, signatureData.getR().length);
            System.arraycopy(signatureData.getS(), 0, byteArr, signatureData.getR().length + 1,
                    signatureData.getS().length);
            System.arraycopy(signatureData.getPub(), 0, byteArr,
                    signatureData.getS().length + signatureData.getR().length + 1,
                    signatureData.getPub().length);
        } else {
            byteArr = new byte[1 + signatureData.getR().length + signatureData.getS().length];
            byteArr[0] = signatureData.getV();
            System.arraycopy(signatureData.getR(), 0, byteArr, 1, signatureData.getR().length);
            System.arraycopy(signatureData.getS(), 0, byteArr, signatureData.getR().length + 1,
                    signatureData.getS().length);
        }
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
        return "0x" + Integer.toHexString(Integer.valueOf(str));
    }

    /**
     * base64Decode.
     *
     * @param str String
     * @return
     */
    public static byte[] base64Decode(String str) {
        if (str == null) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(str);
    }

    /**
     * read File by String
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
        try (InputStream inputStream = new FileInputStream(dirFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
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
        log.debug("readFile dir:{}", filePath);
        File dirFile = new File(filePath);
        if (!dirFile.exists()) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        try (InputStream inputStream = new FileInputStream(dirFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));) {
            String line;
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
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        return headers;
    }

    /**
     * build httpEntity.
     */
    public static HttpEntity buildHttpEntity(Object param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        String paramStr = null;
        if (Objects.nonNull(param)) {
            paramStr = JsonUtils.toJSONString(param);
        }
        HttpEntity requestEntity = new HttpEntity(paramStr, headers);
        return requestEntity;
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
        return JsonUtils.toJavaObject(obj, clazz);
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
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = nias.nextElement();
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

    /**
     * parseHexStr2Int.
     *
     * @param str str
     * @return
     */
    public static int parseHexStr2Int(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        return Integer.parseInt(str.substring(2), 16);
    }

    /**
     * 支持数字，字母与下划线"_"
     * 
     * @param input
     * @return
     */
    public static boolean isLetterDigit(String input) {
        String regex = "^[a-z0-9A-Z_]+$";
        return input.matches(regex);
    }

    /**
     * 不包含中文
     */
    public static boolean notContainsChinese(String input) {
        if (StringUtils.isBlank(input)) {
            return true;
        }
        String regex = "[^\\u4e00-\\u9fa5]+";
        return input.matches(regex);
    }

    /**
     * check connect.
     */
    public static boolean checkConnect(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.setReceiveBufferSize(8193);
            socket.setSoTimeout(500);
            SocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, 1000);
        } catch (Exception ex) {
            log.info("fail checkConnect.");
            return false;
        } finally {
            if (Objects.nonNull(socket)) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("fail close socket", e);
                }
            }
        }
        return true;
    }

    /**
     * extractFigureFromStr.
     * 
     * @param str
     * @return
     */
    public static int extractFigureFromStr(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return Integer.parseInt(m.replaceAll("").trim());
    }

    /**
     * getFolderSize.
     * 
     * @param
     * @return
     */
    public static long getFolderSize(File f) {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFolderSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    /**
     * 文件转Base64
     * 
     * @param filePath 文件路径
     * @return
     */
    public static String fileToBase64(String filePath) {
        if (filePath == null) {
            return null;
        }
        FileInputStream inputFile = null;
        try {
            File file = new File(filePath);
            inputFile = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            inputFile.read(buffer);
            return Base64.getEncoder().encodeToString(buffer);
        } catch (IOException e) {
            log.error("base64ToFile IOException:[{}]", e.toString());
        } finally {
            close(inputFile);
        }
        return null;
    }

    /**
     * 文件压缩并Base64加密
     * 
     * @param srcFiles
     * @return
     */
    public static String fileToZipBase64(List<File> srcFiles) {
        long start = System.currentTimeMillis();
        String toZipBase64 = "";
        ZipOutputStream zos = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            zos = new ZipOutputStream(baos);
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[1024];
                log.info("fileToZipBase64 fileName: [{}] size: [{}] ", srcFile.getName(),
                        srcFile.length());
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
            }
            long end = System.currentTimeMillis();
            log.info("fileToZipBase64 cost time：[{}] ms", (end - start));
        } catch (IOException e) {
            log.error("fileToZipBase64 IOException:[{}]", e.toString());
        } finally {
            close(zos);
        }

        byte[] refereeFileBase64Bytes = Base64.getEncoder().encode(baos.toByteArray());
        try {
            toZipBase64 = new String(refereeFileBase64Bytes, "UTF-8");
        } catch (IOException e) {
            log.error("fileToZipBase64 IOException:[{}]", e.toString());
        }
        return toZipBase64;
    }

    /**
     * zip Base64 解密 解压缩.
     * 
     * @param base64 base64加密字符
     * @param path 解压文件夹路径
     */
    public static void zipBase64ToFile(String base64, String path) {
        ByteArrayInputStream bais = null;
        ZipInputStream zis = null;
        try {
            File file = new File(path);
            if (!file.exists() && !file.isDirectory()) {
                file.mkdirs();
            }

            byte[] byteBase64 = Base64.getDecoder().decode(base64);
            bais = new ByteArrayInputStream(byteBase64);
            zis = new ZipInputStream(bais);
            ZipEntry entry = zis.getNextEntry();
            File fout = null;
            while (entry != null) {
                if (entry.isDirectory()) {
                    File subdirectory = new File(path + File.separator + entry.getName());
                    if (!subdirectory.exists() && !subdirectory.isDirectory()) {
                        subdirectory.mkdirs();
                    }
                } else {
                    log.info("zipBase64ToFile file name:[{}]", entry.getName());
                    String outPath = (path + entry.getName()).replaceAll("\\*", "/");
                    fout = new File(cleanString(outPath));
                    BufferedOutputStream bos = null;
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(fout));
                        int offo = -1;
                        byte[] buffer = new byte[1024];
                        while ((offo = zis.read(buffer)) != -1) {
                            bos.write(buffer, 0, offo);
                        }
                    } catch (IOException e) {
                        log.error("base64ToFile IOException:[{}]", e.toString());
                    } finally {
                        close(bos);
                    }
                }
                // next
                entry = zis.getNextEntry();
            }
        } catch (IOException e) {
            log.error("base64ToFile IOException:[{}]", e.toString());
        } finally {
            close(zis);
            close(bais);
        }
    }

    private static String cleanString(String str) {
        if (str == null) {
            return null;
        }
        String cleanString = "";
        for (int i = 0; i < str.length(); ++i) {
            cleanString += cleanChar(str.charAt(i));
        }
        return cleanString;
    }

    private static char cleanChar(char value) {
        // 0 - 9
        for (int i = 48; i < 58; ++i) {
            if (value == i) {
                return (char) i;
            }
        }
        // 'A' - 'Z'
        for (int i = 65; i < 91; ++i) {
            if (value == i) {
                return (char) i;
            }
        }
        // 'a' - 'z'
        for (int i = 97; i < 123; ++i) {
            if (value == i) {
                return (char) i;
            }
        }
        // other valid characters
        switch (value) {
            case '\\':
                return '\\';
            case '/':
                return '/';
            case ':':
                return ':';
            case '.':
                return '.';
            case '-':
                return '-';
            case '_':
                return '_';
            default:
                return ' ';
        }
    }

    /**
     * close Closeable.
     * 
     * @param closeable object
     */
    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("closeable IOException:[{}]", e.toString());
            }
        }
    }
}
