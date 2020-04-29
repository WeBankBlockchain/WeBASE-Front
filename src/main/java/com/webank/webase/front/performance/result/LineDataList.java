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

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DATA UNIT of monitor data
 * LineDataList => Data => PerformanceData
 * containing list of result number which comes from monitor result
 */
@Data
public class LineDataList {
    List<Long> timestampList;
    List<BigDecimal> valueList;

    public LineDataList(List<Long> timestampList, List<BigDecimal> valueList) {
        this.timestampList = timestampList;
        this.valueList = valueList;
    }
}
