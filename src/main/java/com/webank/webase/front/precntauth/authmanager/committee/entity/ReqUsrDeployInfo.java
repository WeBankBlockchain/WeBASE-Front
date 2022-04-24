package com.webank.webase.front.precntauth.authmanager.committee.entity;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ReqUsrDeployInfo {

  private String groupId;
  private Boolean openFlag;
  private String userAddress;
  private String signUserId;
  @NotBlank
  private String fromAddress;
}
