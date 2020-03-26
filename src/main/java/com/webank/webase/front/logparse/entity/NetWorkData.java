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

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
public class NetWorkData {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private Long totalIn;
    private Long totalOut;
    private int groupId=1;
    private Long timestamp;

    public NetWorkData(Long totalIn, Long totalOut, Long timestamp, int groupId) {
        super();
        this.totalIn = totalIn;
        this.totalOut = totalOut;
        this.timestamp = timestamp;
        this.groupId = groupId;
    }
}
