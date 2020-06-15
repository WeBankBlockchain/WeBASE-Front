package com.webank.webase.front.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.text.SimpleDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JacksonUtils {
    // data format
    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final ThreadLocal<ObjectMapper> OBJECT_MAPPER = new ThreadLocal<ObjectMapper>() {
        @Override
        protected ObjectMapper initialValue() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            objectMapper.setDateFormat(new SimpleDateFormat(STANDARD_FORMAT));
            return objectMapper;
        }
    };

    public static boolean isJson(String str) {
        try {
            OBJECT_MAPPER.get().readTree(str);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public static JsonNode stringToJsonNode(String str) {
        try {
            return OBJECT_MAPPER.get().readTree(str);
        } catch (IOException e) {
            log.error("Parse String to JsonNode error : {}", e.getMessage());
            return null;
        }
    }

    public static <T> String objToString(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj
                    : OBJECT_MAPPER.get().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Parse Object to String error : {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T stringToObj(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : OBJECT_MAPPER.get().readValue(str, clazz);
        } catch (Exception e) {
            log.error("Parse String to Object error : {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T stringToObj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class) ? str
                    : OBJECT_MAPPER.get().readValue(str, typeReference));
        } catch (IOException e) {
            log.error("Parse String to Object error", e);
            return null;
        }
    }

    public static <T> T stringToObj(String str, Class<?> collectionClazz,
            Class<?>... elementClazzes) {
        JavaType javaType = OBJECT_MAPPER.get().getTypeFactory()
                .constructParametricType(collectionClazz, elementClazzes);
        try {
            return OBJECT_MAPPER.get().readValue(str, javaType);
        } catch (IOException e) {
            log.error("Parse String to Object error : {}" + e.getMessage());
            return null;
        }
    }
}
