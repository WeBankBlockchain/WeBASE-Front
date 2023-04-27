package com.webank.webase.front.transaction.entity;


import javax.validation.constraints.NotNull;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ReqQueryTransHandle {

  private String encodeStr;
  @NotNull
  private String contractAddress;
  private String funcName;
  @NotBlank
  private String contractAbi;
  private String userAddress = "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB";
  private int groupId =1 ;
}
