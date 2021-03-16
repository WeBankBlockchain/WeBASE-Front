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

package com.webank.webase.front.base.response;

import com.webank.webase.front.base.code.RetCode;
import com.webank.webase.front.util.JsonUtils;
import lombok.Data;

/**
 * BaseResponse.
 * related with base/code/ConstantCode and base/code/Retcode (ReturnCode)
 */
@Data
public class BaseResponse {
    private int code;
    private String message;
    private Object data;

    public BaseResponse() {}

    public BaseResponse(int code) {
        this.code = code;
    }

    public BaseResponse(RetCode rsc) {
        this.code = rsc.getCode();
        this.message = rsc.getMessage();
    }

    /**
     * constructor.
     * 
     * @param rsc not null
     * @param obj not null
     */
    public BaseResponse(RetCode rsc, Object obj) {
        this.code = rsc.getCode();
        this.message = rsc.getMessage();
        this.data = obj;
    }

    /**
     * constructor.
     * 
     * @param code not null
     * @param message not null
     * @param obj not null
     */
    public BaseResponse(int code, String message, Object obj) {
        this.code = code;
        this.message = message;
        this.data = obj;
    }

    public BaseResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return JsonUtils.objToString(this);
    }

}
