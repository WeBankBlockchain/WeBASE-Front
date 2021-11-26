/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.precompiledapi;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.precompiledapi.entity.ConsensusHandle;
import com.webank.webase.front.precompiledapi.entity.NodeInfo;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Precompiled common controller including management of CNS, node consensus status, CRUD
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
    public Object queryCns(@RequestParam(defaultValue = "1") String groupId,
            @RequestParam String contractNameAndVersion,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception {
        Instant startTime = Instant.now();
        log.info("start queryCns. startTime:{}, groupId:{}, contractNameAndVersion:{}",
                startTime.toEpochMilli(), groupId, contractNameAndVersion);
        List<CnsInfo> resList = new ArrayList<>();
        // get "name:version"
        String[] params = contractNameAndVersion.split(":");
        if (params.length == 1) {
            String name = params[0];
            resList = precompiledService.queryCnsByName(groupId, name);
            log.info("end queryCns useTime:{} resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            if (resList.size() != 0) {
                List2Page<CnsInfo> list2Page =
                        new List2Page<CnsInfo>(resList, pageSize, pageNumber);
                List<CnsInfo> finalList = list2Page.getPagedList();
                long totalCount = (long) resList.size();
                log.debug("end queryCns. Contract Name finalList:{}", finalList);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
            } else {
                return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
            }
        } else if (params.length == 2) {
            String name = params[0];
            String version = params[1];
            if (!PrecompiledUtils.checkVersion(version)) {
                return ConstantCode.INVALID_VERSION;
            }
            // check return list size
            Tuple2<String, String> res = precompiledService.queryCnsByNameAndVersion(groupId, name, version);
            log.info("end queryCns useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return new BaseResponse(ConstantCode.RET_SUCCESS, res);
        } else {
            return ConstantCode.PARAM_ERROR;
        }
    }



    /**
     * Node manage (Consensus control)
     */

    @GetMapping("consensus/list")
    public Object getNodeList(@RequestParam(defaultValue = "1") String groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception {
        Instant startTime = Instant.now();
        log.info("start getNodeList startTime:{}, groupId:{}", startTime.toEpochMilli(), groupId);
        List<NodeInfo> resList = precompiledService.getNodeList(groupId);
        log.info("end getNodeList useTime:{} resList:{}",
                Duration.between(startTime, Instant.now()).toMillis(), resList);
        if (resList.size() != 0) {
            List2Page<NodeInfo> list2Page = new List2Page<NodeInfo>(resList, pageSize, pageNumber);
            List<NodeInfo> finalList = list2Page.getPagedList();
            long totalCount = resList.size();
            log.debug("end getNodeList. finalList:{}", finalList);
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    @ApiOperation(value = "nodeManageControl", notes = "set system config value by key")
    @ApiImplicitParam(name = "consensusHandle", value = "node consensus status control",
            required = true, dataType = "ConsensusHandle")
    @PostMapping("consensus")
    public Object nodeManageControl(@Valid @RequestBody ConsensusHandle consensusHandle)
            throws Exception {
        log.info("start nodeManageControl. consensusHandle:{}", consensusHandle);
        String nodeType = consensusHandle.getNodeType();
        String groupId = consensusHandle.getGroupId();
        String from = consensusHandle.getSignUserId();
        String nodeId = consensusHandle.getNodeId();
        if (!PrecompiledUtils.checkNodeId(nodeId)) {
            return ConstantCode.INVALID_NODE_ID;
        }
        switch (nodeType) {
            case PrecompiledUtils.NODE_TYPE_SEALER:
                return addSealer(groupId, from, nodeId);
            case PrecompiledUtils.NODE_TYPE_OBSERVER:
                return addObserver(groupId, from, nodeId);
            case PrecompiledUtils.NODE_TYPE_REMOVE:
                return removeNode(groupId, from, nodeId);
            default:
                log.debug("end nodeManageControl invalid node type");
                return ConstantCode.INVALID_NODE_TYPE;
        }
    }

    public Object addSealer(String groupId, String fromAddress, String nodeId) throws Exception {
        Instant startTime = Instant.now();
        log.info("start addSealer startTime:{}, groupId:{},fromAddress:{},nodeId:{}",
                startTime.toEpochMilli(), groupId, fromAddress, nodeId);
        try {
            Object res = precompiledService.addSealer(groupId, fromAddress, nodeId);
            log.info("end addSealer useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) {
            log.error("addSealer exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

    public Object addObserver(String groupId, String fromAddress, String nodeId) throws Exception {
        Instant startTime = Instant.now();
        log.info("start addObserver startTime:{}, groupId:{},fromAddress:{},nodeId:{}",
                startTime.toEpochMilli(), groupId, fromAddress, nodeId);
        try {
            Object res = precompiledService.addObserver(groupId, fromAddress, nodeId);
            log.info("end addObserver useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) {
            log.error("addObserver exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

    public Object removeNode(String groupId, String fromAddress, String nodeId) throws Exception {
        Instant startTime = Instant.now();
        log.info("start addSealer startTime:{}, groupId:{},fromAddress:{},nodeId:{}",
                startTime.toEpochMilli(), groupId, fromAddress, nodeId);
        try {
            Object res = precompiledService.removeNode(groupId, fromAddress, nodeId);
            log.info("end addSealer useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) { // e.getCause
            log.error("removeNode exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }
//
//    /**
//     * CRUD operation checkVersionForCRUD(); todo 加上crud
//     * "bcos's version above of 2.0.0-rc3 support crud operation"
//     */
//    @ApiOperation(value = "crudManage", notes = "operate table by crud")
//    @ApiImplicitParam(name = "crudHandle", value = "crud operation info", required = true,
//            dataType = "CrudHandle")
//    @PostMapping("crud")
//    public Object crudManageControl(@Valid @RequestBody CrudHandle crudHandle) throws Exception {
//        log.info("start crudManageControl. crudHandle:{}", crudHandle);
//        String groupId = crudHandle.getGroupId();
//        String from = crudHandle.getSignUserId();
//        String sql = crudHandle.getSql();
//        // to lower case
//        String[] sqlParams = sql.trim().split(" ");
//        switch (sqlParams[0].toLowerCase()) {
//            case "create":
//                return createTable(groupId, from, sql);
//            case "desc":
//                return desc(groupId, sql);
//            case "select":
//                return select(groupId, sql);
//            case "insert":
//                return insert(groupId, from, sql);
//            case "update":
//                return update(groupId, from, sql);
//            case "delete":
//                return remove(groupId, from, sql);
//            default:
//                log.debug("end crudManageControl no such crud operation");
//                return new BaseResponse(ConstantCode.PARAM_FAIL_SQL_ERROR,
//                        "no such crud operation");
//        }
//    }
//
//
//    public Object createTable(String groupId, String fromAddress, String sql) {
//        Instant startTime = Instant.now();
//        log.info("start createTable startTime:{}, groupId:{},fromAddress:{},sql:{}",
//                startTime.toEpochMilli(), groupId, fromAddress, sql);
//        Table table = new Table();
//        try {
//            log.debug("start parseCreateTable.");
//            CRUDParseUtils.parseCreateTable(sql, table);
//            log.debug("end parseCreateTable. table:{}, key:{}, keyField:{}, values:{}",
//                table.getTableName(), table.getKey(), table.getKeyFieldName(), table.getValueFields());
//        } catch (Exception e) {
//            log.error("parseCreateTable. table:{},exception:{}", table, e);
//            CRUDParseUtils.invalidSymbol(sql);
//            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
//                    "Could not parse SQL statement.");
//        }
//        // CRUDParseUtils.checkTableParams(table);
//        String result = precompiledService.createTable(groupId, fromAddress, table);
//        log.info("end createTable useTime:{} res:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), result);
//        return result;
//
//    }
//
//    // check table name exist by desc(tableName)
//    public Object desc(String groupId, String sql) {
//        Instant startTime = Instant.now();
//        log.info("start descTable startTime:{}, groupId:{},sql:{}", startTime.toEpochMilli(),
//                groupId, sql);
//        String[] sqlParams = sql.split(" ");
//        // "desc t_demo"
//        String tableName = sqlParams[1];
//
//        if (tableName.length() > PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH) {
//            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
//                    "The table name length is greater than "
//                            + PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH + ".",
//                    "The table name length is greater than "
//                            + PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH + ".");
//        }
//        CRUDParseUtils.invalidSymbol(tableName);
//        if (tableName.endsWith(";")) {
//            tableName = tableName.substring(0, tableName.length() - 1);
//        }
//        try {
//            List<Map<String, String>> table = precompiledService.desc(groupId, tableName);
//            log.info("end descTable useTime:{} res:{}",
//                    Duration.between(startTime, Instant.now()).toMillis(), table);
//            return new BaseResponse(ConstantCode.RET_SUCCESS, table);
//        } catch (Exception e) {
//            log.error("descTable.exception:[] ", e);
//            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, e.getMessage());
//        }
//    }
//
//    public Object select(String groupId, String sql) throws Exception {
//        Instant startTime = Instant.now();
//        log.info("start select startTime:{}, groupId:{},sql:{}", startTime.toEpochMilli(), groupId,
//                sql);
//        Table table = new Table();
//        Condition conditions = new Condition();
//        List<String> selectColumns = new ArrayList<>();
//
//        try { // 转化select语句
//            log.debug("start parseSelect. sql:{}", sql);
//            CRUDParseUtils.parseSelect(sql, table, conditions, selectColumns);
//            log.debug("end parseSelect. table:{}, conditions:{}, selectColumns:{}", table,
//                    conditions, selectColumns);
//        } catch (Exception e) {
//            log.error("parseSelect Error exception:[]", e);
//            CRUDParseUtils.invalidSymbol(sql);
//            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR, "Could not parse SQL statement.");
//        }
//
//        List<Map<String, String>> descTable = null;
//        try {
//            descTable = precompiledService.desc(groupId, table.getTableName());
//        } catch (Exception e) {
//            log.error("select in descTable Error exception:[]", e);
//            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists ");
//        }
//        String keyField = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
//        table.setKey(keyField);
//        CRUDParseUtils.handleKey(table, conditions);
////        String fields = descTable.getKey() + "," + descTable.getValueFields();
////        List<String> fieldsList = Arrays.asList(fields.split(","));
////        for (String column : selectColumns) {
////            if (!fieldsList.contains(column) && !"*".equals(column)) {
////                return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
////                        "Unknown field '" + column + "' in field list.",
////                        "Unknown field '" + column + "' in field list.");
////            }
////        }
//
//        List<Map<String, String>> result = precompiledService.select(groupId, table, conditions);
//        log.info("end select useTime:{} res:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), result);
//        if (result.size() == 0) {
//            return new BaseResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST,
//                    ConstantCode.CRUD_EMPTY_SET);
//        }
//        result = CRUDParseUtils.filterSystemColum(result);
//        if ("*".equals(selectColumns.get(0))) {
//            selectColumns.clear();
//            selectColumns.add(keyField);
//            String[] valueArr = descTable.get(0).get(PrecompiledConstant.VALUE_FIELD_NAME).split(",");
//            selectColumns.addAll(Arrays.asList(valueArr));
//            result = CRUDParseUtils.getSelectedColumn(selectColumns, result);
//            log.info("end select. result:{}", result);
//            return new BaseResponse(ConstantCode.RET_SUCCESS, result);
//        } else {
//            List<Map<String, String>> selectedResult =
//                    CRUDParseUtils.getSelectedColumn(selectColumns, result);
//            log.info("end select. selectedResult:{}", selectedResult);
//            return new BaseResponse(ConstantCode.RET_SUCCESS, selectedResult);
//        }
//
//    }
//
//    public Object insert(String groupId, String fromAddress, String sql) throws Exception {
//        Instant startTime = Instant.now();
//        log.info("start insert startTime:{}, groupId:{},fromAddress:{},sql:{}",
//            startTime.toEpochMilli(), groupId, fromAddress, sql);
//        Table table = new Table();
//        Entry entry = new Entry();
//        List<Map<String, String>>  descTable = null;
//
//        String tableName = CRUDParseUtils.parseInsertedTableName(sql);
//        descTable = precompiledService.desc(groupId, tableName);
//        log.debug(
//            "insert, tableName: {}, descTable: {}", tableName, descTable.get(0).toString());
//        CRUDParseUtils.parseInsert(sql, table, entry, descTable.get(0));
//        String keyName = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
//        String keyValue = entry.getFieldNameToValue().get(keyName);
//        log.debug(
//            "fieldNameToValue: {}, keyName: {}, keyValue: {}", entry.getFieldNameToValue(), keyName, keyValue);
//        if (keyValue == null) {
//            log.error("Please insert the key field '" + keyName + "'.");
//            throw new FrontException("Please insert the key field '" + keyName + "'.");
//        }
//        // table primary key
//        table.setKey(keyValue);
//        String insertResult =
//            precompiledService.insert(groupId, fromAddress, table, entry);
//        return insertResult;
//    }
//
//    public Object update(String groupId, String fromAddress, String sql) throws Exception {
//        Instant startTime = Instant.now();
//        log.info("start update startTime:{}, groupId:{},fromAddress:{},sql:{}",
//                startTime.toEpochMilli(), groupId, fromAddress, sql);
//        Table table = new Table();
//        Entry entry = new Entry();
//        Condition conditions = new Condition();
//
//        try {
//            log.debug("start parseUpdate. sql:{}", sql);
//            CRUDParseUtils.parseUpdate(sql, table, entry, conditions);
//            log.debug("end parseUpdate. table:{}, entry:{}, conditions:{}", table, entry,
//                    conditions);
//        } catch (Exception e) {
//            log.error("parseUpdate error exception:[]", e);
//            CRUDParseUtils.invalidSymbol(sql);
//            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR, "Could not parse SQL statement.");
//        }
//
//        String tableName = table.getTableName();
//        List<Map<String, String>> descTable = null;
//        try {
//            descTable = precompiledService.desc(groupId, tableName);
//        } catch (Exception e) {
//            log.error("updateTable Error exception:[]", e);
//            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists");
//        }
//
//        String keyName = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
//        if (entry.getFieldNameToValue().containsKey(keyName)) {
//            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
//                    "Please don't set the key field '" + keyName + "'.",
//                    "Please don't set the key field '" + keyName + "'.");
//        }
//        table.setKey(keyName);
//        CRUDParseUtils.handleKey(table, conditions);
//        String updateResult = precompiledService.update(groupId, fromAddress, table, entry, conditions);
//        log.info("end update useTime:{} updateResult:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), updateResult);
//        return updateResult;
//    }
//
//    public Object remove(String groupId, String fromAddress, String sql) {
//        Instant startTime = Instant.now();
//        log.info("start remove startTime:{}, groupId:{},fromAddress:{},sql:{}",
//                startTime.toEpochMilli(), groupId, fromAddress, sql);
//        Table table = new Table();
//        Condition conditions = new Condition();
//
//        try {
//            log.debug("start parseRemove. sql:{}", sql);
//            CRUDParseUtils.parseRemove(sql, table, conditions);
//            log.debug("end parseRemove. table:{}, conditions:{}", table, conditions);
//        } catch (Exception e) {
//            log.error("parseRemove Error exception:[]", e);
//            CRUDParseUtils.invalidSymbol(sql);
//            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR, "Could not parse SQL statement.");
//        }
//
//        List<Map<String, String>> descTable = null;
//        try {
//            descTable = precompiledService.desc(groupId, table.getTableName());
//        } catch (Exception e) {
//            log.error("removeTable Error exception:[]", e);
//            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists");
//        }
//        table.setKey(descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME));
//        CRUDParseUtils.handleKey(table, conditions);
//        String removeResult = precompiledService.remove(groupId, fromAddress, table, conditions);
//        log.info("end remove useTime:{} removeResult:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), removeResult);
//        return removeResult;
//
//    }

}
