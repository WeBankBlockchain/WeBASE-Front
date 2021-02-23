/*
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.front.base.code;

import lombok.Data;

/**
 * Constant Return Code
 */
@Data
public class RetCode {
    private Integer code;
    private String message;

    public RetCode() {}

    public RetCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static RetCode mark(int code, String message) {
        return new RetCode(code, message);
    }

    public static RetCode mark(Integer code) {
        return new RetCode(code, null);
    }

    @Override
    public String toString() {
        return "{" + "\"code\":" + code + ", \"msg\":\"" + message + "\"}";
    }
}
