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

import com.webank.webase.front.util.JsonUtils;
import java.util.HashMap;
import java.util.Map;

import com.webank.webase.front.base.code.RetCode;
import com.webank.webase.front.util.ErrorCodeHandleUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;



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
    public ResponseEntity myExceptionHandler(FrontException frontException) throws Exception {
        log.error("catch frontException: {}",  frontException.getMessage());
        Map<String, Object> map = new HashMap<>();
        map.put("data", frontException.getDetail());
        map.put("errorMessage", frontException.getMessage());
        map.put("code", frontException.getRetCode().getCode());
        return ResponseEntity.status(422).body(map);
    }

    /**
     * parameter exception:TypeMismatchException
     */
    @ResponseBody
    @ExceptionHandler(value = TypeMismatchException.class)
    public ResponseEntity typeMismatchExceptionHandler(TypeMismatchException ex) {
        log.warn("catch typeMismatchException", ex);

        Map<String, Object> map = new HashMap<>();
        //  map.put("exception", frontException);
        map.put("errorMessage", ex.getMessage());
        map.put("code", 400);
        log.warn("typeMismatchException return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(400).body(map);
    }

    @ResponseBody
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResponseEntity bindExceptionHandler(ServletRequestBindingException ex) {
        log.warn("catch bindExceptionHandler", ex);

        Map<String, Object> map = new HashMap<>();
        //  map.put("exception", frontException);
        map.put("errorMessage", ex.getMessage());
        map.put("code", 400);
        log.warn("bindExceptionHandler return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(400).body(map);
    }



    /**
     * all non-catch exception Handler.
     *
     * @param exc e
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity exceptionHandler(Exception exc) {
        log.info("catch  exception: ", exc);
        RetCode errorDetail = chainErrorHandle(exc.getMessage());
        Map<String, Object> map = new HashMap<>();
        map.put("errorMessage", errorDetail.getMessage());
        map.put("code", errorDetail.getCode());
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
