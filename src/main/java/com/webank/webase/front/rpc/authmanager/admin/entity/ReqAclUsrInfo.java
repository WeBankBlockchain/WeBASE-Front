package com.webank.webase.front.rpc.authmanager.admin.entity;

import java.math.BigInteger;
import javax.validation.constraints.NotNull;
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
