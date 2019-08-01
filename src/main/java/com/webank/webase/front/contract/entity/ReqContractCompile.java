package com.webank.webase.front.contract.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class ReqContractCompile {
    @NotBlank
    private String solidityBase64; // Base64 of the contract source
    @NotBlank
    private String solidityName; // name of contract
}
