/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.tool.entity;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * decode tx receipt's input/output
 */
@Data
public class ReqDecodeParam {

    /**
     * 1-decode input, 2-decode output
     */
    @NotNull
    private Integer decodeType;

    private String input;
    private String output;
    private List<Object> abiList;
    /**
     * add in 1.5.0, not null if decode output
     */
    private String methodName;
}
