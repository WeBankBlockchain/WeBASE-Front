package com.webank.webase.front.util.pageutils;

import lombok.Data;

@Data
public class MapHandle<T> {
    private String key;
    private Object data;

    public MapHandle(String key, Object data) {
        this.key = key;
        this.data = data;
    }
}
