package com.webank.webase.front.rpc.authmanager.committee.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class ReqUsrDeployInfo {

  private String groupId;
  private Boolean openFlag;
  private String userAddress;
  private String signUserId;
  @NotBlank
  private String fromAddress;
}
