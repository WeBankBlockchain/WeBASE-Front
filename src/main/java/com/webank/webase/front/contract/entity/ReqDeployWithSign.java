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
package com.webank.webase.front.contract.entity;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import com.webank.webase.front.base.code.ConstantCode;
import lombok.Data;

/**
 * deployWithSign interface parameter.
 * v1.3.0+ default with sign
 */
@Data
@Deprecated
public class ReqDeployWithSign {
    private int groupId = 1;
    private int signUserId = 100001;
    /**
     * 合约编译的bytecode(bin)，用于部署合约
     */
    @NotBlank(message = ConstantCode.PARAM_FAIL_BYTECODE_BIN_IS_EMPTY)
    private String bytecodeBin;
    @NotEmpty(message = ConstantCode.PARAM_FAIL_ABIINFO_IS_EMPTY)
    private List<Object> contractAbi;
    private List<Object> funcParam = new ArrayList<>();
}
