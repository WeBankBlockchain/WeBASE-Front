/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.contract.entity;


import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class ReqContractSave {

    @NotNull
    private Integer groupId;
    private Long contractId;
    @NotBlank
    private String contractName;
    private String version;
    @NotBlank
    private String contractPath;
    private String contractSource;
    private String contractAbi;
    /**
     * 合约编译的bytecode(bin)，用于部署合约
     */
    private String bytecodeBin;
    /**
     * 合约编译的runtime-bytecode(runtime-bin)，用于交易解析
     */
    private String contractBin;
}
