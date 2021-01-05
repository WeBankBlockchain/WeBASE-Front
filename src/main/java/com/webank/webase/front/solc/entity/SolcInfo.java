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

package com.webank.webase.front.solc.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "unique_file_name", columnNames = {"solcName"}),
    @UniqueConstraint(name = "unique_md5", columnNames = {"md5"})
})
public class SolcInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer solcId;
    private String solcName;
    private Integer fileSize;
    private String md5;
//    private String filePath;
    private String description;
    private LocalDateTime createTime;
}
