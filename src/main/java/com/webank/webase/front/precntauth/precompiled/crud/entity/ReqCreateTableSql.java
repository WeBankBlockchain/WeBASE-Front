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
public class ReqCreateTableSql {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private String groupId;
    private String signUserId;   //请求的userId,即用户Id
    private String sqlCreate;   //创建表的SQL, e.g.   "create table t_demo3(name varchar, item_id varchar, item_name varchar, primary key(name))"
}
