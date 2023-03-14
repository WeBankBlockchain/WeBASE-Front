/*
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.front.precompiledapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.webase.front.base.code.ConstantCode;
import javax.validation.constraints.NotNull;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractStatusHandle {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private int groupId;
    @NotBlank(message = ConstantCode.PARAM_FAIL_CONTRACT_ADDRESS_EMPTY)
    private String contractAddress;
    @NotBlank(message = ConstantCode.PARAM_FAIL_CONTRACT_HANDLE_TYPE_EMPTY)
    private String handleType;
    private String signUserId;
    private String grantAddress;
}
