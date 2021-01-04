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
package com.webank.webase.front.performance.result;

/**
 * Performance data handle(DTO) of monitor and performance module
 * performance response to web
 */
@lombok.Data
public class PerformanceData {
    private String metricType;
    private Data data;

    public PerformanceData(String metricType, Data data) {
        this.metricType = metricType;
        this.data = data;
    }
}
