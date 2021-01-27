/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.front.abi.entity;


import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * abi info used to send transaction
 * @author marsli
 */
@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "unique_address", columnNames = {"groupId", "contractAddress"}),
        @UniqueConstraint(name = "unique_name", columnNames = {"groupId", "contractName"})
})
public class AbiInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long abiId;
    private Integer groupId;
    private String contractName;
    private String contractAddress;
    @Column(columnDefinition = "mediumtext")
    private String contractAbi;
    /**
     * runtime bin
     */
    @Column(columnDefinition = "mediumtext")
    private String contractBin;
    /**
     * 1-normal, 2-invalid
     */
    // private Integer contractStatus;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
