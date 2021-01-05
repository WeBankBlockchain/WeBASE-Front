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
package com.webank.webase.front.base.response;

import java.util.Collections;

import com.webank.webase.front.base.code.RetCode;
import lombok.Data;

/**
 * Entity class of page response info.
 */
@Data
public class BasePageResponse {

    private int code;
    private String message;
    private Object data = Collections.emptyList();
    private long totalCount;

    public BasePageResponse() {
    }

    public BasePageResponse(RetCode retcode) {
        this.code = retcode.getCode();
        this.message = retcode.getMessage();
    }


    public BasePageResponse(RetCode retCode, Object data, long totalCount) {
        this.code = retCode.getCode();
        this.message = retCode.getMessage();
        this.data = data;
        this.totalCount = totalCount;
    }

}
