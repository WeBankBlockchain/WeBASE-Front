package com.webank.webase.front.rpc.precompiled.cns.entity;

import lombok.Data;

@Data
public class ReqInfoByNameVersion {

  private String groupId;
  private String contractName;
  private String version;

}
