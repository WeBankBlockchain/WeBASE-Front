package com.webank.webase.front.precntauth.precompiled.crud;

import com.webank.webase.front.precntauth.precompiled.crud.entity.ReqCreateTableInfo;
import com.webank.webase.front.precntauth.precompiled.crud.entity.ReqGetTableInfo;
import com.webank.webase.front.precntauth.precompiled.crud.entity.ReqSetTableInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.util.Map;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "precntauth/precompiled/kvtable", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/kvtable")
public class KVTableController {

  @Autowired
  private KVTableServiceInWebase kvTableService;

  @ApiOperation(value = "create the table")
  @ApiImplicitParam(name = "reqCreateTableInfo", value = "create table info", required = true, dataType = "ReqCreateTableInfo")
  @PostMapping("reqCreateTable")
  public Object createTable(@Valid @RequestBody ReqCreateTableInfo reqCreateTableInfo)
      throws ContractException {
    String res = kvTableService.createTable(reqCreateTableInfo.getGroupId(),
        reqCreateTableInfo.getSignUserId(),
        reqCreateTableInfo.getTableName(), reqCreateTableInfo.getKeyFieldName(),
        reqCreateTableInfo.getValueFields());
    return res;
  }

  @ApiOperation(value = "set the table")
  @ApiImplicitParam(name = "reqSetTableInfo", value = "set table info", required = true, dataType = "ReqSetTableInfo")
  @PostMapping("reqSetTable")
  public Object set(@Valid @RequestBody ReqSetTableInfo reqSetTableInfo) throws ContractException {
    Object res = kvTableService.set(reqSetTableInfo.getGroupId(), reqSetTableInfo.getSignUserId(),
        reqSetTableInfo.getTableName(),
        reqSetTableInfo.getKey(),
        reqSetTableInfo.getFieldNameToValue());
    return res;
  }

  @ApiOperation(value = "get the table")
  @ApiImplicitParam(name = "reqGetTableInfo", value = "get table info", required = true, dataType = "ReqGetTableInfo")
  @PostMapping("reqGetTable")
  public Object get(@Valid @RequestBody ReqGetTableInfo reqGetTableInfo) throws ContractException {
    Map<String, String> res = kvTableService.get(reqGetTableInfo.getGroupId(),
        reqGetTableInfo.getTableName(),
        reqGetTableInfo.getKey());
    return res;
  }

}
