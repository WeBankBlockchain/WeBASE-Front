/**
 * Copyright 2014-2020  the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * comment method.
 */
@Log4j2
public class FrontUtils {

    private static List<String> IGNORE_PARAM = Arrays.asList("pageNumber", "pageSize");

    /**
     * build Predicate by param.
     */
    public static Predicate buildPredicate(Root<?> root, CriteriaBuilder builder, Object param) {
        Objects.requireNonNull(builder, "CriteriaBuilder is null");
        Objects.requireNonNull(param, "param is null");

        Class clazz = param.getClass();
        Field[] fields = clazz.getDeclaredFields(); //all fields
        if (fields.length == 0) {
            log.info("fail buildPredicate. param have not properties");
            return null;
        }

        List<Predicate> list = new ArrayList<>();
        for (Field field : fields) {
            String fieldName = field.getName();//field name
            if (IGNORE_PARAM.contains(fieldName)) {
                continue;
            }
            try {
                Method method = clazz.getMethod("get" + StringUtils.capitalize(fieldName));
                Object fieldValue = method.invoke(param);
                if (Objects.nonNull(fieldValue) && fieldValue != "") {
                    list.add(builder.equal(root.get(fieldName).as(String.class), fieldValue));
                }
            } catch (Exception ex) {
                log.warn("fail buildPredicate", ex);
                continue;
            }
        }

        Predicate[] p = new Predicate[list.size()];
        return builder.and(list.toArray(p));
    }


    /**
     * remove "0x" and last character.
     */
    public static String removeBinFirstAndLast(String contractBin, int removaLastLength) {
        if (StringUtils.isBlank(contractBin)) {
            return null;
        }
        contractBin = removeFirstStr(contractBin, "0x");
        if (contractBin.length() > removaLastLength) {
            contractBin = contractBin.substring(0, contractBin.length() - removaLastLength);
        }
        return contractBin;
    }

    /**
     * remove fist string.
     */
    public static String removeFirstStr(String constant, String target) {
        if (StringUtils.isBlank(constant) || StringUtils.isBlank(target)) {
            return constant;
        }
        if (constant.startsWith(target)) {
            constant = StringUtils.removeStart(constant, target);
        }
        return constant;
    }

    /**
     * create file if not exist.
     */
    public static void createFileIfNotExist(File targetFile, boolean deleteOld) throws IOException {
        Objects.requireNonNull(targetFile, "fail create file. targetFile is null");
        File parentFile = targetFile.getParentFile();
        if (!parentFile.exists()) {
            boolean mkdirResult = parentFile.mkdirs();
            log.info("createFileIfNotExist mkdirResult:{}", mkdirResult);
        }
        if (deleteOld) {
            targetFile.deleteOnExit();
        }

        if (!targetFile.exists()) {
            if (targetFile.isFile()) {
                boolean newFileResult = targetFile.createNewFile();
                log.info("createFileIfNotExist newFileResult:{}", newFileResult);
            } else if (targetFile.isDirectory()) {
                boolean newDirResult = targetFile.mkdir();
                log.info("createFileIfNotExist mkdirResult:{}", newDirResult);
            }
        }
    }

    /**
     * convert list of String to String
     * @param list
     * @return
     */
    public static String listStr2String(List<String> list) {
        if(list == null) {
            return null;
        }
        return JsonUtils.toJSONString(list);
    }

    /**
     * list of String recover from String
     * @return
     */
    public static List<String> string2ListStr(String input) {
        if(input == null) {
            return null;
        }
        return JsonUtils.toJavaObjectList(input, String.class);
    }

    /**
     * if message is null, set message
     * else, return original message
     * @return
     */
//    public static String handleReceiptMsg(TransactionReceipt receipt) {
//        if (receipt.getMessage() == null) {
//            return StatusCode.getStatusMessage(receipt.getStatus());
//        } else {
//            return receipt.getMessage();
//        }
//    }

    public static HttpHeaders headers(String fileName) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
            "attachment;filename*=UTF-8''" + encode(fileName));
        return httpHeaders;
    }

    public static String encode(String name) {
        try {
            return URLEncoder.encode(name, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
