package com.webank.webase.front.precntauth.authmanager.committee.entity;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqResetAdminInfo {

  private String groupId;
  private String contractAddr;
  private String newAdmin;
  private String signUserId;
  @NotNull
  private String fromAddress;

}
