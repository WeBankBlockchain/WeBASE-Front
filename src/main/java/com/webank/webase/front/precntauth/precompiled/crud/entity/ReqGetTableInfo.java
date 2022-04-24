package com.webank.webase.front.precntauth.precompiled.crud.entity;

import com.webank.webase.front.base.code.ConstantCode;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqGetTableInfo {

  @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
  private String groupId;
  private String tableName;
  private String key;

}
