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

package com.webank.webase.front.precntauth.precompiled.crud.model;

import java.util.List;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Common;

public class Table {

    private String tableName;
    private String keyFieldName;
    private List<String> valueFields;
    private String optional = "";
    private Common.TableKeyOrder keyOrder = Common.TableKeyOrder.Lexicographic;

    public Table() {}

    public String getTableName() {
        return tableName;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public void setKeyFieldName(String keyFieldName) {
        this.keyFieldName = keyFieldName;
    }

    public List<String> getValueFields() {
        return valueFields;
    }

    public String getOptional() {
        return optional;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setValueFields(List<String> valueFields) {
        this.valueFields = valueFields;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }

    public Common.TableKeyOrder getKeyOrder() {
        return keyOrder;
    }

    public void setKeyOrder(Common.TableKeyOrder keyOrder) {
        this.keyOrder = keyOrder;
    }
}