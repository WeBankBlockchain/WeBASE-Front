package com.webank.webase.front.contract.entity;

import lombok.Data;

import java.util.List;
@Data
public class ReqCopyContracts {
    private Integer groupId;
    private String contractPath;
    private List<RepCopyContractItem> contractItems;
}
