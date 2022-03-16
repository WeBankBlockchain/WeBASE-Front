package com.webank.webase.front.rpc.authmanager.everyone.entity;

import lombok.Data;

@Data
public class ReqProposalListInfo {

  private String  groupId;
  private Integer pageSize;
  private Integer pageNum;

}
