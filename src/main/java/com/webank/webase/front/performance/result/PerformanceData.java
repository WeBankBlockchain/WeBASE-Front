package com.webank.webase.front.performance.result;

@lombok.Data
public class PerformanceData {
    private String metricType;
    private Data data;

    public PerformanceData(String metricType, Data data) {
        this.metricType = metricType;
        this.data = data;
    }
}
