package com.webank.webase.front.precntauth.base;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountInfo {

  private String accountFileFormat;
  private String accountFile;
  private String password;

  public AccountInfo(String accountFileFormat, String accountFile, String password) {
    this.accountFile = accountFile;
    this.accountFileFormat = accountFileFormat;
    this.password = password;
  }

  public String getAccountFileFormat() {
    return accountFileFormat;
  }

  public void setAccountFileFormat(String accountFileFormat) {
    this.accountFileFormat = accountFileFormat;
  }

  public String getAccountFile() {
    return accountFile;
  }

  public void setAccountFile(String accountFile) {
    this.accountFile = accountFile;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


}
