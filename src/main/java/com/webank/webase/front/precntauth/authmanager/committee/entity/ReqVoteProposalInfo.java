package com.webank.webase.front.precntauth.authmanager.committee.entity;

import java.math.BigInteger;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqVoteProposalInfo {

  private String groupId;
  private BigInteger proposalId;
  private String signUserId;
  private Boolean agree;
  @NotNull
  private String fromAddress;

}
