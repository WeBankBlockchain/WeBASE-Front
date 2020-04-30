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

import com.webank.webase.front.base.code.RetCode;

/**
 * FrontException.
 * 
 */
public class FrontException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private RetCode retCode;
    /**
     * detail message
     */
    private String detail;

    public FrontException(RetCode retCode) {
        super(retCode.getMessage());
        this.retCode = retCode;
    }

    public FrontException(RetCode retCode, String data) {
        super(retCode.getMessage());
        this.retCode = retCode;
        this.detail = data;
    }

    public FrontException(int code, String msg) {
        super(msg);
        this.retCode = new RetCode(code, msg);
    }

    public FrontException(int code, String msg, String data) {
        super(msg);
        this.retCode = new RetCode(code, msg);
        this.detail = data;
    }

    public FrontException(String msg) {
        super(msg);
    }

    public RetCode getRetCode() {
        return retCode;
    }

    public String getDetail() {
        return detail;
    }
}
