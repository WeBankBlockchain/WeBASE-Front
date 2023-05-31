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
public class ReqInsertTableSql {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private String groupId;
    private String signUserId;   //请求的userId,即用户Id
    private String sqlInsert;   //插入表的SQL, e.g.  "insert into t_demo5 values (fruit, fruit, 1,apple1)"

    //示例表t_demo5结构如下：
//    {
//        "key_field":[
//        "name"
//    ],
//        "value_field":[
//        "name",
//                "item_id",
//                "item_name"
//    ],
//        "key_order":[
//        "Lexicographic"
//    ]
//    }

    // 注意，在sqlInsert括号中有四个示例字段，其中第一个字段项对应的是key_field(“name”字段)的取值“fruit”，
    // 后面再跟的多个字段项对应的是value_field("name","item_id","item_name"字段列表)
    // 多个属性字段项的取值,依次是 "fruit", "1","apple1"

}
