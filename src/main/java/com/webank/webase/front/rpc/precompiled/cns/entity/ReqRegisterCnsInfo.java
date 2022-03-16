package com.webank.webase.front.rpc.precompiled.cns.entity;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqRegisterCnsInfo {

  private String groudId;
  private String contractName;
  private String contractVersion;
  private String contractAddress;
  private String abiData;
  private String signUserId;
  @NotNull
  private String fromAddress;

}
