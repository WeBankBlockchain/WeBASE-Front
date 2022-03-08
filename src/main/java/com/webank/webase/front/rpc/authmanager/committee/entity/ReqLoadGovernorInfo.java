package com.webank.webase.front.rpc.authmanager.committee.entity;

import lombok.Data;
import org.fisco.bcos.sdk.contract.auth.po.AuthType;

@Data
public class ReqLoadGovernorInfo {

  private String accountFileFormat;
  private String accountFilePath;
  private String password;

}
