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
 * DATA ENTITY of node monitor and host monitor (performance) module
 * related with LineDataList and PerformanceData
 * LineDataList => Data => PerformanceData
 */
@lombok.Data
public class Data {
    private LineDataList lineDataList;
    private LineDataList contrastDataList;

    public Data(LineDataList lineDataList, LineDataList contrastDataList) {
        this.lineDataList = lineDataList;
        this.contrastDataList = contrastDataList;
    }
}
