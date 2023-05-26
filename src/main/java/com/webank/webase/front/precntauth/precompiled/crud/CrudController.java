package com.webank.webase.front.precntauth.precompiled.crud;

import com.webank.webase.front.precntauth.precompiled.crud.entity.*;
import com.webank.webase.front.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author litieqiao
 * Description 新增CRUD相关接口
 * Version 1.0
 */
@Api(value = "precntauth/precompiled/crud", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/crud")
public class CrudController {

    @Autowired
    private CrudServiceInWebase crudServiceInWebase;

    @ApiOperation(value = "create the table")
    @ApiImplicitParam(name = "reqCreateTableInfo", value = "create table info", required = true, dataType = "ReqCreateTableInfo")
    @PostMapping("reqCreateTable")
    public Object createTable(@Valid @RequestBody ReqCreateTableSql reqCreateTableSql)
            throws ContractException, JSQLParserException {
        return crudServiceInWebase.createTable(reqCreateTableSql.getGroupId(),
                reqCreateTableSql.getSignUserId(),
                reqCreateTableSql.getTableName(),
                reqCreateTableSql.getKeyFieldName(),
                reqCreateTableSql.getValueFields(),
                reqCreateTableSql.getKeyOrder());
    }

    @ApiOperation(value = "desc the table")
    @ApiImplicitParam(name = "reqDescTableSql", value = "desc table info", required = true,
            dataType = "ReqDescTableInfo")
    @PostMapping("reqDescTable")
    public Object descTable(@Valid @RequestBody ReqDescTableSql reqDescTableSql)
            throws ContractException, JSQLParserException {
        return crudServiceInWebase.descTable(reqDescTableSql.getGroupId(),
                reqDescTableSql.getTableName());
    }

    @ApiOperation(value = "insert into the table")
    @ApiImplicitParam(name = "reqDescTableSql", value = "insert into table info", required = true,
            dataType = "ReqInsertTableInfo")
    @PostMapping("reqInsertTable")
    public Object insertTable(@Valid @RequestBody ReqInsertTableSql reqInsertTableSql)
            throws ContractException, JSQLParserException {
        return crudServiceInWebase.insertTable(reqInsertTableSql.getGroupId(),
                reqInsertTableSql.getSignUserId(),
                reqInsertTableSql.getSqlInsert());
    }


    @ApiOperation(value = "select from the table")
    @ApiImplicitParam(name = "reqSelectTable", value = "select from table info", required = true,
            dataType = "ReqSelectTableInfo")
    @PostMapping("reqSelectTable")
    public Object selectTable(@Valid @RequestBody ReqSelectTableSql reqSelectTableSql)
            throws ContractException, JSQLParserException {
        List<Map<String, String>> res =
                crudServiceInWebase.selectTable(reqSelectTableSql.getGroupId(),
                        reqSelectTableSql.getSqlSelect());
        return JsonUtils.objToString(res);
    }


    @ApiOperation(value = "update the table")
    @ApiImplicitParam(name = "reqUpdateTable", value = "update table info", required = true,
            dataType = "ReqUpdateTableInfo")
    @PostMapping("reqUpdateTable")
    public Object updateTable(@Valid @RequestBody ReqUpdateTableSql reqUpdateTableSql)
            throws ContractException, JSQLParserException {
        return crudServiceInWebase.updateTable(reqUpdateTableSql.getGroupId(),
                reqUpdateTableSql.getSignUserId(),
                reqUpdateTableSql.getSqlUpdate());
    }


    @ApiOperation(value = "remove the table")
    @ApiImplicitParam(name = "reqRemoveTable", value = "remove the table", required = true,
            dataType = "ReqRemoveTableInfo")
    @PostMapping("reqRemoveTable")
    public Object removeTable(@Valid @RequestBody ReqRemoveTableSql reqRemoveTableSql)
            throws ContractException, JSQLParserException {
        return crudServiceInWebase.removeTable(reqRemoveTableSql.getGroupId(),
                reqRemoveTableSql.getSignUserId(),
                reqRemoveTableSql.getSqlRemove());
    }

}
