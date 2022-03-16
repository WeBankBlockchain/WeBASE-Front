package com.webank.webase.front.rpc.authmanager.everyone.entity;

import java.math.BigInteger;
import lombok.Data;

@Data
public class ReqProposalInfo {

  private String groupId;
  private BigInteger proposalId;

}
