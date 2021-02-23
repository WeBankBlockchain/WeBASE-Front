/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.precompiledapi.crud;

import org.fisco.bcos.sdk.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Entry;

public class Table {

    private String tableName;
    private String key;
    private String valueFields;
    private String optional = "";

    public Table() {}

    public Table(String tableName, String key) {
        this.tableName = tableName;
        this.key = key;
    }

    public Table(String tableName, String key, String valueFields) {
        this.tableName = tableName;
        this.key = key;
        this.valueFields = valueFields;
    }

    public Table(String tableName, String key, String valueFields, String optional) {
        super();
        this.tableName = tableName;
        this.key = key;
        this.valueFields = valueFields;
        this.optional = optional;
    }

    public String getTableName() {
        return tableName;
    }

    public String getKey() {
        return key;
    }

    public String getValueFields() {
        return valueFields;
    }

    public String getOptional() {
        return optional;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValueFields(String valueFields) {
        this.valueFields = valueFields;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }

    public Entry getEntry() {
        return new Entry();
    }

    public Condition getCondition() {
        return new Condition();
    }
}
