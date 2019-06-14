package com.webank.webase.front.performance.result;

@lombok.Data
public class Data {
    private LineDataList lineDataList;
    private LineDataList contrastDataList;

    public Data(LineDataList lineDataList, LineDataList contrastDataList) {
        this.lineDataList = lineDataList;
        this.contrastDataList = contrastDataList;
    }
}
