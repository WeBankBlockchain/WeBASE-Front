package com.webank.webase.front.performance.result;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LineDataList {
    List<Long> timestampList;
    List<BigDecimal> valueList;

    public LineDataList(List<Long> timestampList, List<BigDecimal> valueList) {
        this.timestampList = timestampList;
        this.valueList = valueList;
    }
}
