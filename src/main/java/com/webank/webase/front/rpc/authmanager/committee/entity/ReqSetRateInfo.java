package com.webank.webase.front.rpc.authmanager.committee.entity;

import java.math.BigInteger;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class ReqSetRateInfo {

  private String groupId;
  private BigInteger participatesRate;
  private BigInteger winRate;
  private String signUserId;
  @NotBlank
  private String fromAddress;


}
