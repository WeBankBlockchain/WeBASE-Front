/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.base.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.code.RetCode;
import com.webank.webase.front.util.ErrorCodeHandleUtils;
import com.webank.webase.front.util.JsonUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpStatusCodeException;


/**
 * ExceptionsHandler.
 */
@ControllerAdvice
@Slf4j
public class ExceptionsHandler {

    @Autowired
    ObjectMapper mapper;

    /**
     * myExceptionHandler.
     *
     * @param frontException e
     */
    @ResponseBody
    @ExceptionHandler(value = FrontException.class)
    public ResponseEntity myExceptionHandler(FrontException frontException) {
        log.error("catch frontException: []",  frontException);
        Map<String, Object> map = new HashMap<>();
        if (frontException.getRetCode() != null) {
            map.put("code", frontException.getRetCode().getCode());
        }
        map.put("data", frontException.getDetail());
        map.put("errorMessage", frontException.getMessage());

        return ResponseEntity.status(422).body(map);
    }

    /**
     * parameter exception:TypeMismatchException
     */
    @ResponseBody
    @ExceptionHandler(value = TypeMismatchException.class)
    public ResponseEntity typeMismatchExceptionHandler(TypeMismatchException ex) {
        log.error("catch typeMismatchException:[]", ex);

        Map<String, Object> map = new HashMap<>();
        map.put("errorMessage", ex.getMessage());
        map.put("code", 400);
        log.warn("typeMismatchException return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(400).body(map);
    }

    @ResponseBody
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResponseEntity bindExceptionHandler(ServletRequestBindingException ex) {
        log.error("catch bindExceptionHandler []", ex);

        Map<String, Object> map = new HashMap<>();
        map.put("errorMessage", ex.getMessage());
        map.put("code", 400);
        log.warn("bindExceptionHandler return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(400).body(map);
    }

    /**
     * Method not found in request
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = HttpStatusCodeException.class)
    public ResponseEntity bindExceptionHandler(HttpStatusCodeException ex) {
        log.error("catch bindExceptionHandler:[]", ex);

        Map<String, Object> map = new HashMap<>();
        map.put("errorMessage", ex.getMessage());
        map.put("code", 400);
        log.warn("bindExceptionHandler return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(400).body(map);
    }

    /**
     * catch java sdk ContractException
     * @param exc e
     */
    @ResponseBody
    @ExceptionHandler(value = ContractException.class)
    public ResponseEntity contractExceptionHandler(ContractException exc) {
        log.error("catch contractExceptionHandler: []", exc);
        Map<String, Object> map = new HashMap<>();
        map.put("errorMessage", exc.getMessage());
        map.put("code", exc.getErrorCode());
        log.warn("contractExceptionHandler return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(500).body(map);
    }

    /**
     * all non-catch exception Handler.
     * v1.4.3: add NODE_NOT_ACTIVE error code
     * @param exc e
     */
    @ResponseBody
    @ExceptionHandler(value = IOException.class)
    public ResponseEntity exceptionHandler(IOException exc) {
        log.error("catch IOException: []", exc);
        RetCode errorDetail = chainErrorHandle(exc.getMessage());
        Map<String, Object> map = new HashMap<>();
        map.put("errorMessage", errorDetail.getMessage());
        map.put("code", errorDetail.getCode());
        log.warn("bindExceptionHandler return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(500).body(map);
    }


    /**
     * all non-catch exception Handler.
     * v1.4.3: add NODE_NOT_ACTIVE error code
     * @param exc e
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity exceptionHandler(Exception exc) {
        log.error("catch  exception: []", exc);
        RetCode errorDetail = chainErrorHandle(exc.getMessage());
        Map<String, Object> map = new HashMap<>();
        map.put("errorMessage", errorDetail.getMessage());
        map.put("code", errorDetail.getCode());
        log.warn("bindExceptionHandler return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(500).body(map);
    }

    private RetCode chainErrorHandle(String errorMessage) {
        RetCode response = ErrorCodeHandleUtils.handleErrorMsg(errorMessage);
        if (response == null) {
            return new RetCode(500, errorMessage);
        } else {
            return response;
        }
    }

}
