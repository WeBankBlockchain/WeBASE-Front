package com.webank.webase.front.precntauth.authmanager.committee.entity;

import lombok.Data;

@Data
public class ReqLoadGovernorInfo {

  private String accountFileFormat;
  private String accountFilePath;
  private String password;

}
