package com.webank.webase.front.precntauth.precompiled.cns.entity;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqRegisterCnsInfo {

  private String groupId;
  private String contractName;
  private String contractVersion;
  private String contractAddress;
  private String abiData;
  @NotNull
  private String signUserId;
  private String fromAddress;

}
