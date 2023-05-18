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
public class ReqUpdateTableSql {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private String groupId;
    private String sqlUpdate;  //更新表的SQL, e.g. "update t_demo3 set item_name = orange where name = fruit and item_id = 1"

}
