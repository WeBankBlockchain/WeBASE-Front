package com.webank.webase.front.rpc.precompiled.consensus.entity;

import lombok.Data;

@Data
public class ReqNodeListInfo {

  private String groupId;
  private Integer pageSize;
  private Integer pageNumber;
}
