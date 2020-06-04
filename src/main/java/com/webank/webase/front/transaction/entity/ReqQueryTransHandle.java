package com.webank.webase.front.transaction.entity;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqQueryTransHandle {

  private String encodeStr;
  @NotNull
  private String contractAddress;
  private String funcName;
  @NotNull
  private String contractAbi;
  private String userAddress = "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB";
  private int groupId =1 ;
}
