package com.webank.webase.front.precntauth.precompiled.bfs.entity;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCreateBFSInfo {

  private String groupId;
  private String path;
  private String signUserId;
  @NotNull
  private String fromAddress;

}
