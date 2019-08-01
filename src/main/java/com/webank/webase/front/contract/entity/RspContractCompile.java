package com.webank.webase.front.contract.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RspContractCompile {
    private String contractName;
    private String contractAbi;
    private String contractBin;

}
