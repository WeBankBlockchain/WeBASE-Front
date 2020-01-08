/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.precompiledapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.precompiledapi.entity.ConsensusHandle;
import com.webank.webase.front.precompiledapi.entity.CrudHandle;
import com.webank.webase.front.precompiledapi.entity.NodeInfo;
import com.webank.webase.front.util.CRUDParseUtils;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.Table;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Precompiled common controller
 * including management of CNS, node consensus status, CRUD
 */
@Api(value = "/precompiled", tags = "precompiled manage interface")
@Slf4j
@RestController
@RequestMapping(value = "precompiled")
public class PrecompiledController {
    @Autowired
    private PrecompiledService precompiledService;

    /**
     * Cns manage
     */
    @GetMapping("cns/list")
    public Object queryCns(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String contractNameAndVersion,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception {
        Instant startTime = Instant.now();
        log.info("start queryCns. startTime:{}, groupId:{}, contractNameAndVersion:{}",
                startTime.toEpochMilli(), groupId, contractNameAndVersion);
        List<CnsInfo> resList = new ArrayList<>();
        // get "name:version"
        String[] params = contractNameAndVersion.split(":");
        if(params.length == 1) {
            String name = params[0];
            resList =  precompiledService.queryCnsByName(groupId, name);
            log.info("end queryCns useTime:{} resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            if(resList.size() != 0){
                List2Page<CnsInfo> list2Page = new List2Page<CnsInfo>(resList, pageSize, pageNumber);
                List<CnsInfo> finalList = list2Page.getPagedList();
                long totalCount = (long) resList.size();
                log.debug("end queryCns. Contract Name finalList:{}", finalList);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
            } else {
                return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
            }
        }else if(params.length == 2) {
            String name = params[0];
            String version = params[1];
            if(!PrecompiledUtils.checkVersion(version)) {
                return ConstantCode.INVALID_VERSION;
            }
            // check return list size
            resList = precompiledService.queryCnsByNameAndVersion(groupId, name, version);
            log.info("end queryCns useTime:{} resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            if(resList.size() != 0) {
                List2Page<CnsInfo> list2Page = new List2Page<CnsInfo>(resList, pageSize, pageNumber);
                List<CnsInfo> finalList = list2Page.getPagedList();
                long totalCount = (long) resList.size();
                log.debug("in queryCns case: Contract Name And Version. finalList:{}", finalList);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
            } else {
                return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
            }
        }else {
            return ConstantCode.PARAM_ERROR;
        }
    }



    /**
     * Node manage (Consensus control)
     */

    @GetMapping("consensus/list")
    public Object getNodeList(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception {
        Instant startTime = Instant.now();
        log.info("start getNodeList startTime:{}, groupId:{}", startTime.toEpochMilli(), groupId);
        List<NodeInfo> resList = precompiledService.getNodeList(groupId);
        log.info("end getNodeList useTime:{} resList:{}",
                Duration.between(startTime, Instant.now()).toMillis(), resList);
        if(resList.size() != 0) {
            List2Page<NodeInfo> list2Page = new List2Page<NodeInfo>(resList, pageSize, pageNumber);
            List<NodeInfo> finalList = list2Page.getPagedList();
            long totalCount = (long) resList.size();
            log.debug("end getNodeList. finalList:{}", finalList);
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    @ApiOperation(value = "nodeManageControl", notes = "set system config value by key")
    @ApiImplicitParam(name = "consensusHandle", value = "node consensus status control", required = true, dataType = "ConsensusHandle")
    @PostMapping("consensus") //TODO url change to node
    public Object nodeManageControl(@Valid @RequestBody ConsensusHandle consensusHandle)throws Exception {
        log.info("start nodeManageControl. consensusHandle:{}", consensusHandle);
        String nodeType = consensusHandle.getNodeType();
        int groupId = consensusHandle.getGroupId();
        String from = consensusHandle.getFromAddress();
        String nodeId = consensusHandle.getNodeId();
        if (!PrecompiledUtils.checkNodeId(nodeId)) {
            return ConstantCode.INVALID_NODE_ID;
        }
        if(consensusHandle.getUseAes() == null){
            consensusHandle.setUseAes(false);
        }
        Boolean useAes = consensusHandle.getUseAes();
        switch (nodeType) {
            case PrecompiledUtils.NODE_TYPE_SEALER:
                return addSealer(groupId, from, nodeId, useAes);
            case PrecompiledUtils.NODE_TYPE_OBSERVER:
                return addObserver(groupId, from, nodeId, useAes);
            case PrecompiledUtils.NODE_TYPE_REMOVE:
                return removeNode(groupId, from, nodeId, useAes);
            default:
                log.debug("end nodeManageControl invalid node type");
                return ConstantCode.INVALID_NODE_TYPE;
        }
    }

    public Object addSealer(int groupId, String fromAddress, String nodeId,
                            Boolean useAes) throws Exception {
        Instant startTime = Instant.now();
        log.info("start addSealer startTime:{}, groupId:{},fromAddress:{},nodeId:{}",
                startTime.toEpochMilli(), groupId, fromAddress, nodeId);
        try{
            Object res = precompiledService.addSealer(groupId, fromAddress, nodeId, useAes);
            log.info("end addSealer useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) {
            log.error("addSealer exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

    public Object addObserver(int groupId, String fromAddress, String nodeId,
                              Boolean useAes) throws Exception {
        Instant startTime = Instant.now();
        log.info("start addObserver startTime:{}, groupId:{},fromAddress:{},nodeId:{}",
                startTime.toEpochMilli(), groupId, fromAddress, nodeId);
        try{
            Object res = precompiledService.addObserver(groupId, fromAddress, nodeId, useAes);
            log.info("end addObserver useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) {
            log.error("addObserver exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

    public Object removeNode(int groupId, String fromAddress, String nodeId,
                             Boolean useAes) throws Exception {
        Instant startTime = Instant.now();
        log.info("start addSealer startTime:{}, groupId:{},fromAddress:{},nodeId:{}",
                startTime.toEpochMilli(), groupId, fromAddress, nodeId);
        try{
            Object res = precompiledService.removeNode(groupId, fromAddress, nodeId, useAes);
            log.info("end addSealer useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) { // e.getCause
            log.error("removeNode exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

    /**
     * CRUD operation
     * checkVersionForCRUD();
     * TODO exception for "bcos's version above of 2.0.0-rc3 support crud operation"
     */
    @ApiOperation(value = "crudManage", notes = "operate table by crud")
    @ApiImplicitParam(name = "crudHandle", value = "crud operation info", required = true, dataType = "CrudHandle")
    @PostMapping("crud")
    public Object crudManageControl(@Valid @RequestBody CrudHandle crudHandle, BindingResult bindingResult)throws Exception {
        log.info("start crudManageControl. crudHandle:{}", crudHandle);
        int groupId = crudHandle.getGroupId();
        String from = crudHandle.getFromAddress();
        String sql = crudHandle.getSql();
        if(crudHandle.getUseAes() == null){
            crudHandle.setUseAes(false);
        }
        Boolean useAes = crudHandle.getUseAes();
        // to lower case
        String[] sqlParams = sql.trim().split(" ");
        switch (sqlParams[0].toLowerCase()) {
            case "create":
                return createTable(groupId, from, sql, useAes);
            case "desc":
                return desc(groupId, sql);
            case "select":
                return select(groupId, from, sql, useAes);
            case "insert":
                return insert(groupId, from, sql, useAes);
            case "update":
                return update(groupId, from, sql, useAes);
            case "delete":
                return remove(groupId, from, sql, useAes);
            default:
                log.debug("end crudManageControl no such crud operation");
                return new BaseResponse(ConstantCode.PARAM_FAIL_SQL_ERROR,
                        "no such crud operation");
        }
    }


    public Object createTable(int groupId, String fromAddress, String sql, Boolean useAes) throws Exception {
        Instant startTime = Instant.now();
        log.info("start createTable startTime:{}, groupId:{},fromAddress:{},sql:{}",
                startTime.toEpochMilli(), groupId, fromAddress, sql);
        Table table = new Table();
        try {
            log.debug("start parseCreateTable.");
            CRUDParseUtils.parseCreateTable(sql, table);
            log.debug("end parseCreateTable. table:{}", table);
        } catch (Exception e) {
            log.error("parseCreateTable. table:{},exception:{}", table, e);
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql),
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql));
        }
        CRUDParseUtils.checkTableParams(table);
        int result = precompiledService.createTable(groupId, fromAddress, table, useAes);
        log.info("end createTable useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), result);

        if (result == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS,"Create '" + table.getTableName() + "' Ok.");
        } else if (result == PrecompiledCommon.TableExist_RC3) {
            log.debug("createTable " + "Table already exists");
            return new BaseResponse(PrecompiledCommon.TableExist_RC3, "Table already exists", "Table already exists");
        } else if (result == PrecompiledCommon.PermissionDenied_RC3) {
            log.debug("createTable " + "Permission denied");
            return new BaseResponse(PrecompiledCommon.PermissionDenied_RC3, "Permission denied", "Permission denied");
        } else {
            log.debug("createTable " + "code: " + result + "Create '" + table.getTableName() + "' failed.");
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR, "code: " + result + "Create '" + table.getTableName() + "' failed.",
                    "code: " + result + "Create '" + table.getTableName() + "' failed.");
        }
    }

    // check table name exist by desc(tableName)
    public Object desc(int groupId, String sql) throws Exception {
        Instant startTime = Instant.now();
        log.info("start descTable startTime:{}, groupId:{},sql:{}",
                startTime.toEpochMilli(), groupId, sql);
        Table table = new Table();
        String[] sqlParams = sql.split(" ");
        // "desc t_demo"
        String tableName = sqlParams[1];

        if (tableName.length() > PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH) {
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                            "The table name length is greater than " +
                            PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH + ".",
                    "The table name length is greater than " +
                            PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH + ".");
        }
        CRUDParseUtils.invalidSymbol(tableName);
        if (tableName.endsWith(";")) {
            tableName = tableName.substring(0, tableName.length() - 1);
        }
        try {
            table = precompiledService.desc(groupId, tableName);
            log.info("end descTable useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), table);
            return new BaseResponse(ConstantCode.RET_SUCCESS, table);
        } catch (Exception e) {
            log.error("descTable.exception:[] ", e);
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, e.getMessage());
        }
    }

    public Object select(int groupId, String fromAddress, String sql, Boolean useAes) throws Exception {
        Instant startTime = Instant.now();
        log.info("start select startTime:{}, groupId:{},fromAddress:{},sql:{}",
                startTime.toEpochMilli(), groupId, fromAddress, sql);
        Table table = new Table();
        Condition conditions = table.getCondition();
        List<String> selectColumns = new ArrayList<>();

        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        try { //转化select语句
            log.debug("start parseSelect. sql:{}", sql);
            CRUDParseUtils.parseSelect(sql, table, conditions, selectColumns);
            log.debug("end parseSelect. table:{}, conditions:{}, selectColumns:{}",
                    table, conditions, selectColumns);
        } catch (Exception e) {
            log.error("parseSelect Error exception:[]", e);
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql),
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql));
        }

        Table descTable;
        try {
            descTable = precompiledService.desc(groupId, table.getTableName());
        } catch (Exception e) {
            log.error("select in descTable Error exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists ");
        }
        table.setKey(descTable.getKey());
        CRUDParseUtils.handleKey(table, conditions);
        String fields = descTable.getKey() + "," + descTable.getValueFields();
        List<String> fieldsList = Arrays.asList(fields.split(","));
        for (String column : selectColumns) {
            if (!fieldsList.contains(column) && !"*".equals(column)) {
                return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                        "Unknown field '" + column + "' in field list.",
                        "Unknown field '" + column + "' in field list.");
            }
        }
        List<Map<String, String>> result = new ArrayList<>();

        result = precompiledService.select(groupId, fromAddress, table, conditions, useAes);
        log.info("end select useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), result);
        int rows = 0;
        if (result.size() == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, ConstantCode.CRUD_EMPTY_SET);
        }
        result = CRUDParseUtils.filterSystemColum(result);
        if ("*".equals(selectColumns.get(0))) {
            selectColumns.clear();
            selectColumns.add(descTable.getKey());
            String[] valueArr = descTable.getValueFields().split(",");
            selectColumns.addAll(Arrays.asList(valueArr));
            result = CRUDParseUtils.getSeletedColumn(selectColumns, result);
            log.info("end select. result:{}", result);
            return new BaseResponse(ConstantCode.RET_SUCCESS, result);
        } else {
            List<Map<String, String>> selectedResult = CRUDParseUtils.getSeletedColumn(selectColumns, result);
            log.info("end select. selectedResult:{}", selectedResult);
            return new BaseResponse(ConstantCode.RET_SUCCESS, selectedResult);
        }

        //return new BaseResponse(ConstantCode.RET_SUCCESS, rows + " row(s) in set.");
    }

    public Object insert(int groupId, String fromAddress, String sql, Boolean useAes) throws Exception {
        Instant startTime = Instant.now();
        log.info("start insert startTime:{}, groupId:{},fromAddress:{},sql:{}",
                startTime.toEpochMilli(), groupId, fromAddress, sql);        Table table = new Table();
        Entry entry = new Entry();

        // insert sql use "values" or not
        boolean useValues = false;
        try {
            log.debug("start parseInsert. sql:{}", sql);
            useValues = CRUDParseUtils.parseInsert(sql, table, entry);
            log.debug("end parseInsert. table:{}, entry:{}", table, entry);
        } catch (Exception e) {
            log.error("parseInsert Error exception:[]", e);
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                            "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql),
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql));
        }

        Set<String> entryFields = entry.getFields().keySet();

        String tableName = table.getTableName();
        Table descTable;
        try {
            descTable = precompiledService.desc(groupId, tableName);
        } catch (Exception e) {
            log.error("insertTable Error exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists");
        }
        String keyName = descTable.getKey();
        String fields = keyName + "," + descTable.getValueFields();

        List<String> fieldsList = Arrays.asList(fields.split(","));

        // ex: insert into t_test values (fruit, 1, apple)
        if (useValues) {
            if (entry.getFields().size() != fieldsList.size()) {
                return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                        "Column count doesn't match value count.",
                        "Column count doesn't match value count.");
            } else {
                Entry entryValue = table.getEntry();
                for (int i = 0; i < entry.getFields().size(); i++) {
                    for (String entryField : entryFields) {
                        if ((i + "").equals(entryField)) {
                            entryValue.put(fieldsList.get(i), entry.get(i + ""));
                            if (keyName.equals(fieldsList.get(i))) {
                                table.setKey(entry.get(i + ""));
                            }
                        }
                    }
                }
                entry = entryValue;
            }
        }
        // ex: insert into t_test (name, item_id, item_name) values (fruit, 1, apple)
        else {
            for (String entryField : entryFields) {
                if (!fieldsList.contains(entryField)) {
                    return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                            "Unknown field '" + entryField + "' in field list.",
                            "Unknown field '" + entryField + "' in field list.");
                }
                if (fieldsList.size() != entryFields.size()) {
                    List<String> listString = new ArrayList<String>(fieldsList);
                    for (String entryItem : entryFields) {
                        listString.remove(entryItem);
                    }
                    StringBuilder strBuilder = new StringBuilder("Please provide field '");
                    for (int i = 0; i < listString.size(); i++) {
                        if (i == listString.size() - 1) {
                            strBuilder.append(listString.get(i)).append("' ");
                        } else {
                            strBuilder.append(listString.get(i)).append("', '");
                        }
                    }
                    strBuilder.append("in field list.");
                    return new BaseResponse(
                            PrecompiledUtils.CRUD_SQL_ERROR, strBuilder.toString(), strBuilder.toString());
                }
            }
            String keyValue = entry.get(keyName);
            if (keyValue == null) {
                return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                        "Column count doesn't match value count.",
                        "Column count doesn't match value count.");
            }
            table.setKey(keyValue);
        }
        CRUDParseUtils.checkUserTableParam(entry, descTable);
        int insertResult = precompiledService.insert(groupId, fromAddress, table, entry, useAes);
        log.info("end insert useTime:{} insertResult:{}",
                Duration.between(startTime, Instant.now()).toMillis(), insertResult);
        if (insertResult >= 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS,
                    "Insert OK, " + insertResult + " row(s) affected.");
        } else {
            return new BaseResponse(ConstantCode.SQL_ERROR, "Insert failed.");
        }
    }

    public Object update(int groupId, String fromAddress, String sql, Boolean useAes) throws Exception {
        Instant startTime = Instant.now();
        log.info("start update startTime:{}, groupId:{},fromAddress:{},sql:{}",
                startTime.toEpochMilli(), groupId, fromAddress, sql);        Table table = new Table();
        Entry entry = new Entry();
        Condition conditions = new Condition();

        try {
            log.debug("start parseUpdate. sql:{}", sql);
            CRUDParseUtils.parseUpdate(sql, table, entry, conditions);
            log.debug("end parseUpdate. table:{}, entry:{}, conditions:{}",
                    table, entry, conditions);
        } catch (Exception e) {
            log.error("parseUpdate error exception:[]", e);
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql),
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql));
        }

        String tableName = table.getTableName();
        Table descTable;
        try {
           descTable = precompiledService.desc(groupId, tableName);
        } catch (Exception e) {
            log.error("updateTable Error exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists");
        }

        String keyName = descTable.getKey();
        if (entry.getFields().containsKey(keyName)) {
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Please don't set the key field '" + keyName + "'.",
                    "Please don't set the key field '" + keyName + "'.");
        }
        table.setKey(descTable.getKey());
        CRUDParseUtils.handleKey(table, conditions);
        String fields = descTable.getKey() + "," + descTable.getValueFields();
        List<String> fieldsList = Arrays.asList(fields.split(","));
        Set<String> entryFields = entry.getFields().keySet();
        Set<String> conditonFields = conditions.getConditions().keySet();
        Set<String> allFields = new HashSet<>();
        allFields.addAll(entryFields);
        allFields.addAll(conditonFields);
        for (String entryField : allFields) {
            if (!fieldsList.contains(entryField)) {
                return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                        "Unknown field '" + entryField + "' in field list.",
                        "Unknown field '" + entryField + "' in field list.");
            }
        }
        CRUDParseUtils.checkUserTableParam(entry, descTable);
        int updateResult = precompiledService.update(groupId, fromAddress,
                table, entry, conditions, useAes);
        log.info("end update useTime:{} updateResult:{}",
                Duration.between(startTime, Instant.now()).toMillis(), updateResult);
        if (updateResult >= 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS,
                    "Update OK, " + updateResult + " row(s) affected.");
        } else {
            return new BaseResponse(ConstantCode.SQL_ERROR, "Update failed.");
        }

    }

    public Object remove(int groupId, String fromAddress, String sql, Boolean useAes) throws Exception {
        Instant startTime = Instant.now();
        log.info("start remove startTime:{}, groupId:{},fromAddress:{},sql:{}",
                startTime.toEpochMilli(), groupId, fromAddress, sql);        Table table = new Table();
        Condition conditions = new Condition();

        int code;
        String msg;
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        try {
            log.debug("start parseRemove. sql:{}", sql);
            CRUDParseUtils.parseRemove(sql, table, conditions);
            log.debug("end parseRemove. table:{}, conditions:{}", table, conditions);
        } catch (Exception e) {
            log.error("parseRemove Error exception:[]", e);
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql),
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql));
        }

        Table descTable;
        try {
            descTable = precompiledService.desc(groupId, table.getTableName());
        } catch (Exception e) {
            log.error("removeTable Error exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists");
        }
        table.setKey(descTable.getKey());
        CRUDParseUtils.handleKey(table, conditions);
        int removeResult = precompiledService.remove(groupId, fromAddress,
                table, conditions, useAes);
        log.info("end remove useTime:{} removeResult:{}",
                Duration.between(startTime, Instant.now()).toMillis(), removeResult);
        if (removeResult >= 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS,
                    "Remove OK, " + removeResult + " row(s) affected.");
        } else {
            return new BaseResponse(ConstantCode.SQL_ERROR, "Remove failed.");
        }

    }

}
