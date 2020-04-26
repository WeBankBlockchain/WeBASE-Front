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

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import lombok.Data;

/**
 * Entity of contract path in web solidity IDE in db(using h2.db)
 */
@Data
@Entity
@IdClass(ContractPathKey.class)
public class ContractPath implements Serializable {
    private static final long serialVersionUID = 3286516914027062194L;
    @Id
    private Integer groupId;
    @Id
    private String contractPath;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
