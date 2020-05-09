/*
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.front.logparse.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="t_network_data", 
       indexes = {@Index(columnList="group_id", unique = false)})
public class NetWorkData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "total_in")
    private Long totalIn;
    @Column(name = "total_out")
    private Long totalOut;
    @Column(name = "group_id")
    private Integer groupId;
    private Long timestamp;
    
    public NetWorkData() {}

    public NetWorkData(Long totalIn, Long totalOut, Long timestamp, Integer groupId) {
        super();
        this.totalIn = totalIn;
        this.totalOut = totalOut;
        this.timestamp = timestamp;
        this.groupId = groupId;
    }
}
