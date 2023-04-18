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

import com.webank.webase.front.base.code.ConstantCode;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * ReqRegisterCns.
 */
@Data
public class ReqRegisterCns {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUP_ID_IS_EMPTY_STRING)
    private Integer groupId;
    @Deprecated
    private String contractName;
    @NotBlank(message = ConstantCode.PARAM_FAIL_CNS_NAME_IS_EMPTY_STRING)
    private String cnsName;
    @NotBlank(message = ConstantCode.PARAM_FAIL_VERSION_IS_EMPTY)
    private String version;
    @NotBlank(message = ConstantCode.PARAM_FAIL_CONTRACT_ADDRESS_EMPTY)
    private String contractAddress;
    @NotEmpty(message = ConstantCode.PARAM_FAIL_ABIINFO_IS_EMPTY)
    private List<ABIDefinition> abiInfo;
    private String signUserId;
    private String userAddress;
    private String contractPath;
    private boolean saveEnabled = false;
}
