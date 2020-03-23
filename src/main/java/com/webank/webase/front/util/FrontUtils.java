/**
 * Copyright 2014-2019  the original author or authors.
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

import com.alibaba.fastjson.JSON;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

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
        if (Objects.isNull(fields) || fields.length == 0) {
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
     * conver object to java bean.
     */
    public static <T> T object2JavaBean(Object obj, Class<T> clazz) {
        if (obj == null || clazz == null) {
            log.warn("object2JavaBean. obj or clazz null");
            return null;
        }
        String jsonStr = JSON.toJSONString(obj);

        return JSON.parseObject(jsonStr, clazz);
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
    public static void createFileIfNotExist(File targetFile, boolean deketeOld) throws IOException {
        Objects.requireNonNull(targetFile, "fail create file. targetFile is null");
        File parentFile = targetFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (deketeOld) {
            targetFile.deleteOnExit();
        }

        if (!targetFile.exists()) {
            if (targetFile.isFile()) {
                targetFile.createNewFile();
            } else if (targetFile.isDirectory()) {
                targetFile.mkdir();
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
        return String.join(",", list);
    }

    /**
     * list of String recover from String
     * @param str
     * @return
     */
    public static List<String> string2ListStr(String str) {
        if(str == null) {
            return null;
        }
        return Arrays.asList(str.split(","));
    }
}
