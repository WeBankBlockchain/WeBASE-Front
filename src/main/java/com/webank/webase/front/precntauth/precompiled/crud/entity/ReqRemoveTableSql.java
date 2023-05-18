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
public class ReqRemoveTableSql {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private String groupId;
    private String sqlRemove;  //删除表的SQL, e.g. "delete from t_demo3 where name = fruit and item_id = 1"

}
