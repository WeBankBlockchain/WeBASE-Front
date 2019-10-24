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
package com.webank.webase.front.precompiledapi.permission;

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.front.base.ConstantCode;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionHandle {
    // defaultValue = "1"
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private int groupId;
    // @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_TYPE_IS_EMPTY)
    private String permissionType;
    @NotNull(message = ConstantCode.PARAM_FAIL_FROM_IS_EMPTY)
    private String fromAddress;
    @NotNull(message = ConstantCode.PARAM_FAIL_USER_IS_EMPTY)
    private String address;
    private String tableName;
    private PermissionState permissionState;
}
