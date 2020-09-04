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
package com.webank.webase.front.precompiledapi.entity;

import com.webank.webase.front.base.code.ConstantCode;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * handle POST permission state in api("/sorted") in permission controller
 */
@Data
public class PermissionState {
    // 0 means revoked, 1 means granted
    @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_STATE_ALL_CONNOT_BE_EMPTY)
    private int deployAndCreate;
    @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_STATE_ALL_CONNOT_BE_EMPTY)
    private int cns;
    @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_STATE_ALL_CONNOT_BE_EMPTY)
    private int sysConfig;
    @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_STATE_ALL_CONNOT_BE_EMPTY)
    private int node;
}
