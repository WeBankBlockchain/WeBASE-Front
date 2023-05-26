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
public class ReqSelectTableSql {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private String groupId;
    private String sqlSelect;  //查询表的SQL, e.g. "select * from t_demo3 where name = fruit"

}
