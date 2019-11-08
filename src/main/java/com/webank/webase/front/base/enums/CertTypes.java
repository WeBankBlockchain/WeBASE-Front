package com.webank.webase.front.base.enums;

public enum CertTypes {
    CHAIN(1), NODE(2), SDK(3);

    private int value;

    private CertTypes(Integer certTypes) {
        this.value = certTypes;
    }

    public int getValue() {
        return this.value;
    }
}
