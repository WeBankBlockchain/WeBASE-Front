package com.webank.webase.front.precntauth.authmanager.committee.entity;

import java.math.BigInteger;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ReqSetRateInfo {

  private String groupId;
  private BigInteger participatesRate;
  private BigInteger winRate;
  private String signUserId;
  @NotBlank
  private String fromAddress;


}
