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
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.precompiledapi.crud.CRUDParseUtils;
import com.webank.webase.front.precompiledapi.crud.Table;
import com.webank.webase.front.precompiledapi.entity.ConsensusHandle;
import com.webank.webase.front.precompiledapi.entity.ContractManageResult;
import com.webank.webase.front.precompiledapi.entity.ContractStatusHandle;
import com.webank.webase.front.precompiledapi.entity.CrudHandle;
import com.webank.webase.front.precompiledapi.entity.NodeInfo;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.model.PrecompiledConstant;
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
    public Object queryCns(@RequestParam(defaultValue = "1") int groupId,
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
            resList = precompiledService.queryCnsByNameAndVersion(groupId, name, version);
            log.info("end queryCns useTime:{} resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            if (resList.size() != 0) {
                List2Page<CnsInfo> list2Page =
                        new List2Page<CnsInfo>(resList, pageSize, pageNumber);
                List<CnsInfo> finalList = list2Page.getPagedList();
                long totalCount = (long) resList.size();
                log.debug("in queryCns case: Contract Name And Version. finalList:{}", finalList);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
            } else {
                return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
            }
        } else {
            return ConstantCode.PARAM_ERROR;
        }
    }



    /**
     * Node manage (Consensus control)
     */

    @GetMapping("consensus/list")
    public Object getNodeList(@RequestParam(defaultValue = "1") int groupId,
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
        int groupId = consensusHandle.getGroupId();
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

    public Object addSealer(int groupId, String fromAddress, String nodeId) throws Exception {
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

    public Object addObserver(int groupId, String fromAddress, String nodeId) throws Exception {
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

    public Object removeNode(int groupId, String fromAddress, String nodeId) throws Exception {
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

    /**
     * CRUD operation checkVersionForCRUD();
     * "bcos's version above of 2.0.0-rc3 support crud operation"
     */
    @ApiOperation(value = "crudManage", notes = "operate table by crud")
    @ApiImplicitParam(name = "crudHandle", value = "crud operation info", required = true,
            dataType = "CrudHandle")
    @PostMapping("crud")
    public Object crudManageControl(@Valid @RequestBody CrudHandle crudHandle) throws Exception {
        log.info("start crudManageControl. crudHandle:{}", crudHandle);
        int groupId = crudHandle.getGroupId();
        String from = crudHandle.getSignUserId();
        String sql = crudHandle.getSql();
        // to lower case
        String[] sqlParams = sql.trim().split(" ");
        switch (sqlParams[0].toLowerCase()) {
            case "create":
                return createTable(groupId, from, sql);
            case "desc":
                return desc(groupId, sql);
            case "select":
                return select(groupId, sql);
            case "insert":
                return insert(groupId, from, sql);
            case "update":
                return update(groupId, from, sql);
            case "delete":
                return remove(groupId, from, sql);
            default:
                log.debug("end crudManageControl no such crud operation");
                return new BaseResponse(ConstantCode.PARAM_FAIL_SQL_ERROR,
                        "no such crud operation");
        }
    }


    public Object createTable(int groupId, String fromAddress, String sql) {
        Instant startTime = Instant.now();
        log.info("start createTable startTime:{}, groupId:{},fromAddress:{},sql:{}",
                startTime.toEpochMilli(), groupId, fromAddress, sql);
        Table table = new Table();
        try {
            log.debug("start parseCreateTable.");
            CRUDParseUtils.parseCreateTable(sql, table);
            log.debug("end parseCreateTable. table:{}, key:{}, keyField:{}, values:{}",
                table.getTableName(), table.getKey(), table.getKeyFieldName(), table.getValueFields());
        } catch (Exception e) {
            log.error("parseCreateTable. table:{},exception:{}", table, e);
            CRUDParseUtils.invalidSymbol(sql);
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse SQL statement.");
        }
        // CRUDParseUtils.checkTableParams(table);
        String result = precompiledService.createTable(groupId, fromAddress, table);
        log.info("end createTable useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), result);
        return result;

    }

    // check table name exist by desc(tableName)
    public Object desc(int groupId, String sql) {
        Instant startTime = Instant.now();
        log.info("start descTable startTime:{}, groupId:{},sql:{}", startTime.toEpochMilli(),
                groupId, sql);
        String[] sqlParams = sql.split(" ");
        // "desc t_demo"
        String tableName = sqlParams[1];

        if (tableName.length() > PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH) {
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "The table name length is greater than "
                            + PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH + ".",
                    "The table name length is greater than "
                            + PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH + ".");
        }
        CRUDParseUtils.invalidSymbol(tableName);
        if (tableName.endsWith(";")) {
            tableName = tableName.substring(0, tableName.length() - 1);
        }
        try {
            List<Map<String, String>> table = precompiledService.desc(groupId, tableName);
            log.info("end descTable useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), table);
            return new BaseResponse(ConstantCode.RET_SUCCESS, table);
        } catch (Exception e) {
            log.error("descTable.exception:[] ", e);
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, e.getMessage());
        }
    }

    public Object select(int groupId, String sql) throws Exception {
        Instant startTime = Instant.now();
        log.info("start select startTime:{}, groupId:{},sql:{}", startTime.toEpochMilli(), groupId,
                sql);
        Table table = new Table();
        Condition conditions = new Condition();
        List<String> selectColumns = new ArrayList<>();

        try { // 转化select语句
            log.debug("start parseSelect. sql:{}", sql);
            CRUDParseUtils.parseSelect(sql, table, conditions, selectColumns);
            log.debug("end parseSelect. table:{}, conditions:{}, selectColumns:{}", table,
                    conditions, selectColumns);
        } catch (Exception e) {
            log.error("parseSelect Error exception:[]", e);
            CRUDParseUtils.invalidSymbol(sql);
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR, "Could not parse SQL statement.");
        }

        List<Map<String, String>> descTable = null;
        try {
            descTable = precompiledService.desc(groupId, table.getTableName());
        } catch (Exception e) {
            log.error("select in descTable Error exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists ");
        }
        String keyField = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
        table.setKey(keyField);
        CRUDParseUtils.handleKey(table, conditions);
//        String fields = descTable.getKey() + "," + descTable.getValueFields();
//        List<String> fieldsList = Arrays.asList(fields.split(","));
//        for (String column : selectColumns) {
//            if (!fieldsList.contains(column) && !"*".equals(column)) {
//                return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
//                        "Unknown field '" + column + "' in field list.",
//                        "Unknown field '" + column + "' in field list.");
//            }
//        }

        List<Map<String, String>> result = precompiledService.select(groupId, table, conditions);
        log.info("end select useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), result);
        if (result.size() == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST,
                    ConstantCode.CRUD_EMPTY_SET);
        }
        result = CRUDParseUtils.filterSystemColum(result);
        if ("*".equals(selectColumns.get(0))) {
            selectColumns.clear();
            selectColumns.add(keyField);
            String[] valueArr = descTable.get(0).get(PrecompiledConstant.VALUE_FIELD_NAME).split(",");
            selectColumns.addAll(Arrays.asList(valueArr));
            result = CRUDParseUtils.getSelectedColumn(selectColumns, result);
            log.info("end select. result:{}", result);
            return new BaseResponse(ConstantCode.RET_SUCCESS, result);
        } else {
            List<Map<String, String>> selectedResult =
                    CRUDParseUtils.getSelectedColumn(selectColumns, result);
            log.info("end select. selectedResult:{}", selectedResult);
            return new BaseResponse(ConstantCode.RET_SUCCESS, selectedResult);
        }

    }

    public Object insert(int groupId, String fromAddress, String sql) throws Exception {
        Instant startTime = Instant.now();
        log.info("start insert startTime:{}, groupId:{},fromAddress:{},sql:{}",
            startTime.toEpochMilli(), groupId, fromAddress, sql);
        Table table = new Table();
        Entry entry = new Entry();
        List<Map<String, String>>  descTable = null;

        String tableName = CRUDParseUtils.parseInsertedTableName(sql);
        descTable = precompiledService.desc(groupId, tableName);
        log.debug(
            "insert, tableName: {}, descTable: {}", tableName, descTable.get(0).toString());
        CRUDParseUtils.parseInsert(sql, table, entry, descTable.get(0));
        String keyName = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
        String keyValue = entry.getFieldNameToValue().get(keyName);
        log.debug(
            "fieldNameToValue: {}, keyName: {}, keyValue: {}", entry.getFieldNameToValue(), keyName, keyValue);
        if (keyValue == null) {
            log.error("Please insert the key field '" + keyName + "'.");
            throw new FrontException("Please insert the key field '" + keyName + "'.");
        }
        // table primary key
        table.setKey(keyValue);
        String insertResult =
            precompiledService.insert(groupId, fromAddress, table, entry);
        return insertResult;
//        if (insertResult >= 0) {
//            log.info("Insert OK: {}", insertResult);
//            return new BaseResponse(insertResult, insertResult + " row affected.");
//        } else {
//            log.info("Result of insert for " + table.getTableName() + ":");
//            return new BaseResponse(insertResult, "insert failed!");
//        }
    }
//        Table table = new Table();
//        Entry entry = new Entry();
//
//        // insert sql use "values" or not
//        boolean useValues = false;
//        try {
//            log.debug("start parseInsert. sql:{}", sql);
//            useValues = CRUDParseUtils.parseInsert(sql, table, entry);
//            log.debug("end parseInsert. table:{}, entry:{}", table, entry);
//        } catch (Exception e) {
//            log.error("parseInsert Error exception:[]", e);
//            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
//                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbol(sql),
//                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbol(sql));
//        }
//
//        String tableName = table.getTableName();
//        Table descTable;
//        try {
//            descTable = precompiledService.desc(groupId, tableName);
//        } catch (Exception e) {
//            log.error("insertTable Error exception:[]", e);
//            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists");
//        }
//        String keyName = descTable.getKey();
//        String fields = keyName + "," + descTable.getValueFields();
//
//        List<String> fieldsList = Arrays.asList(fields.split(","));
//        Set<String> entryFields = entry.getFieldNameToValue().keySet();
//
//        // ex: insert into t_test values (fruit, 1, apple)
//        if (useValues) {
//            if (entry.getFieldNameToValue().size() != fieldsList.size()) {
//                log.error("field value size not equal to field size");
//                return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
//                        "Column count doesn't match value count.",
//                        "Column count doesn't match value count.");
//            } else {
//                Entry entryValue = table.getEntry();
//                for (int i = 0; i < entry.getFieldNameToValue().size(); i++) {
//                    for (String entryField : entryFields) {
//                        if ((i + "").equals(entryField)) {
//                            Map<String, String> map = new HashMap<>();
//                            map.put(fieldsList.get(i), entry.getFieldNameToValue().get(i + ""));
//                            entryValue.setFieldNameToValue(map);
//                            if (keyName.equals(fieldsList.get(i))) {
//                                table.setKey(entry.getFieldNameToValue().get(i + ""));
//                            }
//                        }
//                    }
//                }
//                entry = entryValue;
//            }
//        }
//        // ex: insert into t_test (name, item_id, item_name) values (fruit, 1, apple)
//        else {
//            for (String entryField : entryFields) {
//                if (!fieldsList.contains(entryField)) {
//                    return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
//                            "Unknown field '" + entryField + "' in field list.",
//                            "Unknown field '" + entryField + "' in field list.");
//                }
//                if (fieldsList.size() != entryFields.size()) {
//                    List<String> listString = new ArrayList<String>(fieldsList);
//                    for (String entryItem : entryFields) {
//                        listString.remove(entryItem);
//                    }
//                    StringBuilder strBuilder = new StringBuilder("Please provide field '");
//                    for (int i = 0; i < listString.size(); i++) {
//                        if (i == listString.size() - 1) {
//                            strBuilder.append(listString.get(i)).append("' ");
//                        } else {
//                            strBuilder.append(listString.get(i)).append("', '");
//                        }
//                    }
//                    strBuilder.append("in field list.");
//                    return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR, strBuilder.toString(),
//                            strBuilder.toString());
//                }
//            }
//            String keyValue = entry.getFieldNameToValue().get(keyName);
//            if (keyValue == null) {
//                return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
//                        "Column count doesn't match value count.",
//                        "Column count doesn't match value count.");
//            }
//            table.setKey(keyValue);
//        }
//        CRUDParseUtils.checkUserTableParam(entry, descTable);
//        int insertResult = precompiledService.insert(groupId, fromAddress, table, entry);
//        log.info("end insert useTime:{} insertResult:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), insertResult);
//        if (insertResult >= 0) {
//            return new BaseResponse(ConstantCode.RET_SUCCESS,
//                    "Insert OK, " + insertResult + " row(s) affected.");
//        } else {
//            return new BaseResponse(insertResult, "Insert failed.");
//        }

    public Object update(int groupId, String fromAddress, String sql) throws Exception {
        Instant startTime = Instant.now();
        log.info("start update startTime:{}, groupId:{},fromAddress:{},sql:{}",
                startTime.toEpochMilli(), groupId, fromAddress, sql);
        Table table = new Table();
        Entry entry = new Entry();
        Condition conditions = new Condition();

        try {
            log.debug("start parseUpdate. sql:{}", sql);
            CRUDParseUtils.parseUpdate(sql, table, entry, conditions);
            log.debug("end parseUpdate. table:{}, entry:{}, conditions:{}", table, entry,
                    conditions);
        } catch (Exception e) {
            log.error("parseUpdate error exception:[]", e);
            CRUDParseUtils.invalidSymbol(sql);
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR, "Could not parse SQL statement.");
        }

        String tableName = table.getTableName();
        List<Map<String, String>> descTable = null;
        try {
            descTable = precompiledService.desc(groupId, tableName);
        } catch (Exception e) {
            log.error("updateTable Error exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists");
        }

        String keyName = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
        if (entry.getFieldNameToValue().containsKey(keyName)) {
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Please don't set the key field '" + keyName + "'.",
                    "Please don't set the key field '" + keyName + "'.");
        }
        table.setKey(keyName);
        CRUDParseUtils.handleKey(table, conditions);
        String updateResult = precompiledService.update(groupId, fromAddress, table, entry, conditions);
        log.info("end update useTime:{} updateResult:{}",
                Duration.between(startTime, Instant.now()).toMillis(), updateResult);
        return updateResult;
    }

    public Object remove(int groupId, String fromAddress, String sql) {
        Instant startTime = Instant.now();
        log.info("start remove startTime:{}, groupId:{},fromAddress:{},sql:{}",
                startTime.toEpochMilli(), groupId, fromAddress, sql);
        Table table = new Table();
        Condition conditions = new Condition();

        try {
            log.debug("start parseRemove. sql:{}", sql);
            CRUDParseUtils.parseRemove(sql, table, conditions);
            log.debug("end parseRemove. table:{}, conditions:{}", table, conditions);
        } catch (Exception e) {
            log.error("parseRemove Error exception:[]", e);
            CRUDParseUtils.invalidSymbol(sql);
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR, "Could not parse SQL statement.");
        }

        List<Map<String, String>> descTable = null;
        try {
            descTable = precompiledService.desc(groupId, table.getTableName());
        } catch (Exception e) {
            log.error("removeTable Error exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists");
        }
        table.setKey(descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME));
        CRUDParseUtils.handleKey(table, conditions);
        String removeResult = precompiledService.remove(groupId, fromAddress, table, conditions);
        log.info("end remove useTime:{} removeResult:{}",
                Duration.between(startTime, Instant.now()).toMillis(), removeResult);
        return removeResult;

    }

    @ApiOperation(value = "contractStatusManage", notes = "contract status manage")
    @PostMapping("contractStatusManage")
    public Object contractStatusManage(
            @Valid @RequestBody ContractStatusHandle contractStatusHandle) throws Exception {
        log.debug("start contractStatusManage. contractStatusHandle:{}", contractStatusHandle);
        switch (contractStatusHandle.getHandleType()) {
            case PrecompiledUtils.CONTRACT_MANAGE_FREEZE:
                return contractFreeze(contractStatusHandle);
            case PrecompiledUtils.CONTRACT_MANAGE_UNFREEZE:
                return contractUnfreeze(contractStatusHandle);
            case PrecompiledUtils.CONTRACT_MANAGE_GRANTMANAGER:
                return contractGrantManager(contractStatusHandle);
            case PrecompiledUtils.CONTRACT_MANAGE_GETSTATUS:
                return contractStatus(contractStatusHandle);
            case PrecompiledUtils.CONTRACT_MANAGE_LISTMANAGER:
                return contractManagerList(contractStatusHandle);
            default:
                log.error("end contractStatusManage. invalid contract handle type");
                throw new FrontException(ConstantCode.INVALID_CONTRACT_HANDLE_TYPE);
        }
    }

    private Object contractFreeze(ContractStatusHandle contractStatusHandle) {
        Instant startTime = Instant.now();
        log.info("start contractFreeze startTime:{}", startTime.toEpochMilli());
        try {
            if (StringUtils.isBlank(contractStatusHandle.getSignUserId())) {
                log.error("signUserId is empty");
                throw new FrontException(ConstantCode.PARAM_FAIL_SIGN_USER_ID_IS_EMPTY);
            }
            String res = precompiledService.contractFreeze(contractStatusHandle.getGroupId(),
                    contractStatusHandle.getSignUserId(),
                    contractStatusHandle.getContractAddress());
            ContractManageResult contractManageResult =
                    JsonUtils.toJavaObject(res, ContractManageResult.class);
            if (contractManageResult == null) {
                return new FrontException(ConstantCode.FAIL_PARSE_JSON);
            }
            if (contractManageResult.getCode() == 0) {
                log.info("end contractFreeze useTime:{} contractManageResult:{}",
                        Duration.between(startTime, Instant.now()).toMillis(),
                        contractManageResult);
                return new BaseResponse(ConstantCode.RET_SUCCEED);
            } else {
                throw new FrontException(ConstantCode.FAIL_CONTRACT_HANDLE.getCode(),
                        contractManageResult.getMsg());
            }
        } catch (Exception e) {
            log.error("contractFreeze exception:", e);
            throw new FrontException(ConstantCode.FAIL_CONTRACT_HANDLE.getCode(), e.getMessage());
        }
    }

    private Object contractUnfreeze(ContractStatusHandle contractStatusHandle) throws Exception {
        Instant startTime = Instant.now();
        log.info("start contractUnfreeze startTime:{}", startTime.toEpochMilli());
        try {
            if (StringUtils.isBlank(contractStatusHandle.getSignUserId())) {
                log.error("signUserId is empty");
                throw new FrontException(ConstantCode.PARAM_FAIL_SIGN_USER_ID_IS_EMPTY);
            }
            String res = precompiledService.contractUnfreeze(contractStatusHandle.getGroupId(),
                    contractStatusHandle.getSignUserId(),
                    contractStatusHandle.getContractAddress());
            ContractManageResult contractManageResult =
                    JsonUtils.toJavaObject(res, ContractManageResult.class);
            if (contractManageResult.getCode() == 0) {
                log.info("end contractUnfreeze useTime:{} contractManageResult:{}",
                        Duration.between(startTime, Instant.now()).toMillis(),
                        contractManageResult);
                return new BaseResponse(ConstantCode.RET_SUCCEED);
            } else {
                throw new FrontException(ConstantCode.FAIL_CONTRACT_HANDLE.getCode(),
                        contractManageResult.getMsg());
            }
        } catch (Exception e) {
            log.error("contractUnfreeze exception:", e);
            throw new FrontException(ConstantCode.FAIL_CONTRACT_HANDLE.getCode(), e.getMessage());
        }
    }

    private Object contractGrantManager(ContractStatusHandle contractStatusHandle) {
        Instant startTime = Instant.now();
        log.info("start contractGrantManager startTime:{}", startTime.toEpochMilli());
        try {
            if (StringUtils.isBlank(contractStatusHandle.getSignUserId())) {
                log.error("signUserId is empty");
                throw new FrontException(ConstantCode.PARAM_FAIL_SIGN_USER_ID_IS_EMPTY);
            }
            if (StringUtils.isBlank(contractStatusHandle.getGrantAddress())) {
                log.error("grantAddress cannot be empty");
                throw new FrontException(ConstantCode.PARAM_FAIL_GRANT_ADDRESS_EMPTY);
            }
            String res = precompiledService.contractGrantManager(contractStatusHandle.getGroupId(),
                    contractStatusHandle.getSignUserId(), contractStatusHandle.getContractAddress(),
                    contractStatusHandle.getGrantAddress());
            ContractManageResult contractManageResult =
                    JsonUtils.toJavaObject(res, ContractManageResult.class);
            if (contractManageResult.getCode() == 0) {
                log.info("end contractGrantManager useTime:{} contractManageResult:{}",
                        Duration.between(startTime, Instant.now()).toMillis(),
                        contractManageResult);
                return new BaseResponse(ConstantCode.RET_SUCCEED);
            } else {
                throw new FrontException(ConstantCode.FAIL_CONTRACT_HANDLE.getCode(),
                        contractManageResult.getMsg());
            }
        } catch (Exception e) {
            log.error("contractGrantManager exception:", e);
            throw new FrontException(ConstantCode.FAIL_CONTRACT_HANDLE.getCode(), e.getMessage());
        }
    }

    private Object contractStatus(ContractStatusHandle contractStatusHandle) {
        Instant startTime = Instant.now();
        log.info("start contractStatus startTime:{}", startTime.toEpochMilli());
        try {
            String res = precompiledService.contractStatus(contractStatusHandle.getGroupId(),
                    contractStatusHandle.getContractAddress());
            if (res.contains("code")) {
                ContractManageResult contractManageResult =
                        JsonUtils.toJavaObject(res, ContractManageResult.class);
                throw new FrontException(ConstantCode.FAIL_CONTRACT_HANDLE.getCode(),
                        contractManageResult.getMsg());
            } else {
                BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
                response.setData(res);
                log.info("end contractStatus useTime:{} response:{}",
                        Duration.between(startTime, Instant.now()).toMillis(), response);
                return response;
            }
        } catch (Exception e) {
            log.error("contractStatus exception:", e);
            throw new FrontException(ConstantCode.FAIL_CONTRACT_HANDLE.getCode(), e.getMessage());
        }
    }

    private Object contractManagerList(ContractStatusHandle contractStatusHandle) {
        Instant startTime = Instant.now();
        log.info("start contractManagerList startTime:{}", startTime.toEpochMilli());
        try {
            List<String> res = precompiledService.contractManagerList(contractStatusHandle.getGroupId(),
                    contractStatusHandle.getContractAddress());
            BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
            response.setData(res);
            log.info("end contractManagerList useTime:{} response:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), response);
            return response;
        } catch (Exception e) {
            log.error("contractManagerList exception:", e);
            throw new FrontException(ConstantCode.FAIL_CONTRACT_HANDLE.getCode(), e.getMessage());
        }
    }

}
