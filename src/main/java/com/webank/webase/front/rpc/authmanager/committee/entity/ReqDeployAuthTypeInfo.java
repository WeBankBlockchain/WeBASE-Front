package com.webank.webase.front.rpc.authmanager.committee.entity;

import java.math.BigInteger;
import lombok.Data;
import org.fisco.bcos.sdk.contract.auth.po.AuthType;
import javax.validation.constraints.NotBlank;

@Data
public class ReqDeployAuthTypeInfo {

  private String groupId;
  private BigInteger  deployAuthType;
  private String signUserId;
  @NotBlank
  private String fromAddress;

}
