/**
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
package com.webank.webase.front.base.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSON;
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
        //  log.warn("catch business exception", frontException);
//        RetCode retCode = Optional.ofNullable(frontException).map(FrontException::getRetCode)
//                .orElse(new RetCode(101001, frontException.getMessage()));
//
//        BaseResponse rep = new BaseResponse(retCode);

        //   log.warn("business exception return:{}", mapper.writeValueAsString(rep));

        Map<String, Object> map = new HashMap<>();
        //  map.put("exception", frontException);
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
        log.warn("typeMismatchException return:{}", JSON.toJSONString(map));
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
        log.warn("bindExceptionHandler return:{}", JSON.toJSONString(map));
        return ResponseEntity.status(400).body(map);
    }



    /**
     * exceptionHandler.
     *
     * @param exc e
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity exceptionHandler(Exception exc) {
        log.info("catch  exception", exc);
        Map<String, Object> map = new HashMap<>();
        //  map.put("exception", frontException);
        map.put("errorMessage", exc.getMessage());
        map.put("code", 500);
        return ResponseEntity.status(500).body(map);
    }
}
