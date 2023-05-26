package com.webank.webase.front.precntauth.precompiled.crud.entity;

import com.webank.webase.front.base.code.ConstantCode;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

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
    private String tableName;
    private String keyFieldName;
    private List<String> valueFields;
    private int keyOrder;    //Common.TableKeyOrder: Unknown(-1),Lexicographic(0),Numerical(1)
}
