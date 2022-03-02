/**
 * Copyright 2014-2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.contract.entity.wasm;

import com.webank.webase.front.contract.entity.ContractPathKey;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.time.LocalDateTime;

/**
 * 保存编译的任务，单个合约只允许编译一次
 * 编译的任务完成不删除，只要不等于running，group_contractPath_contractName 与合约项目的目录名保持一致
 * @author lining
 */
@Data
@Entity
@IdClass(CompileTaskKey.class)
public class CompileTask {
    private static final long serialVersionUID = 3286516914027062195L;

    @Id
    private String groupId;
    /**
     * contractName
     * (if contract from node-manager, contractName contains unique id after contractName
     */
    @Id
    private String contractName;
    @Id
    private String contractPath;
    /**
     * compile status: 1-running, 2-success, 3-fail
     */
    private Integer status;
    @Column(columnDefinition = "mediumtext")
    private String abi;
    @Column(columnDefinition = "mediumtext")
    private String bin;
    @Column(columnDefinition = "text")
    private String description;

    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
