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
package com.webank.webase.front.transaction.entity;

import com.webank.webase.front.base.code.ConstantCode;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * transHandleWithSign interface parameter.
 * handle transactions of deploy/call contract
 * v1.3.0+ default with sign
 */
@Data
public class ReqTransHandleWithSign {
    private int groupId = 1;
    @NotNull(message = ConstantCode.PARAM_FAIL_SIGN_USER_ID_IS_EMPTY_STRING)
    private String signUserId;
    /**
     * used in saving abi file
     */
    private String contractName;
    private String version;
    private String contractAddress;
    private String contractPath;
    @NotBlank(message = ConstantCode.PARAM_FAIL_FUNCNAME_IS_EMPTY)
    private String funcName;
    private List<Object> contractAbi = new ArrayList<>();
    private List<String> funcParam = new ArrayList<>();
    // 1.4.3
    private boolean useCns = false;
    private String cnsName;
}
