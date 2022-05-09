package com.webank.webase.front.precntauth.authmanager.admin.entity;

import lombok.Data;

@Data
public class ReqAclUsrInfo {

  private String groupId;
  private String contractAddr;
  private String func;
  private String userAddress;
  private Boolean isOpen;
  private String signUserId;
  private String fromAddress;


}
