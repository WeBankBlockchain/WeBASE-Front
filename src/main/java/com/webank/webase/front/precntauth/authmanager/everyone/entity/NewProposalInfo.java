package com.webank.webase.front.precntauth.authmanager.everyone.entity;

import java.util.List;
import lombok.Data;

@Data
public class NewProposalInfo {

  private String resourceId;
  private String proposer;
  private String proposalType;
  private long blockNumberInterval;
  private String status;
  private List<String> agreeVoters;
  private List<String> againstVoters;
  private int proposalId;

  public void NewProposalInfo() {
  }


}
