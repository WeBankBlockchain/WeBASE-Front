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
package com.webank.webase.front.configapi.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.NoArgsConstructor;

/**
 * Config info in db
 */
@Data
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "unique_config", columnNames = {"type","config_key"})
})
@NoArgsConstructor
@AllArgsConstructor
public class ConfigInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * example: type is "sdk"
     */
    private String type;
    /**
     * example: key is "peers"
     */
    @Column(name = "config_key")
    private String key;
    /**
     * example: value is ["127.0.0.1:20200"]
     */
    @Column(name = "config_value", columnDefinition = "text")
    private String value;
    private Integer version;

    public ConfigInfo(String type, String key) {
        this.type = type;
        this.key = key;
    }

    public ConfigInfo(String type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }
}
