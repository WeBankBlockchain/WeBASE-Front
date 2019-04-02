package com.webank.webase.front.base.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.RetCode;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

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

/**
 * ExceptionsHandler.
 * 
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
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = FrontException.class)
    public BaseResponse myExceptionHandler(FrontException frontException) throws Exception {
        log.warn("catch business exception", frontException);
        RetCode retCode = Optional.ofNullable(frontException).map(FrontException::getRetCode)
                .orElse(new RetCode(101001, frontException.getMessage()));

        BaseResponse rep = new BaseResponse(retCode);
        log.warn("business exception return:{}", mapper.writeValueAsString(rep));
        return rep;
    }

    /**
     * exceptionHandler.
     * 
     * @param exc e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public BaseResponse exceptionHandler(Exception exc) {
        log.info("catch  exception", exc);
        RetCode retCode = ConstantCode.SYSTEM_ERROR;
        BaseResponse rep = new BaseResponse(retCode);
        try {
            log.warn("exceptionHandler system exception return:{}", mapper.writeValueAsString(rep));
        } catch (JsonProcessingException ex) {
            log.warn("exceptionHandler system exception");
        }
        return rep;
    }
}
