package com.webank.webase.front.transaction.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Contract information at the time of the transaction.
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ContractOfTrans {
    private String contractName;
    private String version;
    private String contractAddress;
    private int groupId = 1;
    private String contractPath;
    private String funcName;
    private List<Object> contractAbi;
    private List<Object> funcParam;

    public ContractOfTrans(ReqTransHandle req) {
        this.contractName = req.getContractName();
        this.version = req.getVersion();
        this.contractAddress = req.getContractAddress();
        this.groupId = req.getGroupId();
        this.contractPath = req.getContractPath();
        this.funcName = req.getFuncName();
        this.contractAbi = req.getContractAbi();
        this.funcParam = req.getFuncParam();
    }

    public ContractOfTrans(ReqTransHandleWithSign req) {
        this.contractAddress = req.getContractAddress();
        this.groupId = req.getGroupId();
        this.funcName = req.getFuncName();
        this.contractAbi = req.getContractAbi();
        this.funcParam = req.getFuncParam();
    }
}
