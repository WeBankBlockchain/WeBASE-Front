package com.webank.webase.front.precntauth.precompiled.crud.entity;

import com.webank.webase.front.base.code.ConstantCode;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author litieqiao
 * Description
 * Version 1.0
 */
@Data
public class ReqDescTableSql {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private String groupId;
    private String signUserId;   //请求的userId,即用户Id
    private String tableName;  //目标表的名称, e.g.  "t_demo3"
}
