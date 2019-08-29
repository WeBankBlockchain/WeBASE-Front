package com.webank.webase.front.precompiledapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.BasePageResponse;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.precompiledapi.precompiledHandle.ConsensusHandle;
import com.webank.webase.front.precompiledapi.precompiledHandle.CrudHandle;
import com.webank.webase.front.util.CRUDParseUtils;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.common.PrecompiledResponse;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.Table;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

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
        List<CnsInfo> resList = new ArrayList<>();
        // get "name:version"
        String[] params = contractNameAndVersion.split(":");
        if(params.length == 1) {
            String name = params[0];
            resList =  precompiledService.queryCnsByName(groupId, name);
            if(resList.size() != 0){
                List2Page<CnsInfo> list2Page = new List2Page<CnsInfo>(resList, pageSize, pageNumber);
                List<CnsInfo> finalList = list2Page.getPagedList();
                Long totalCount = (long) finalList.size();
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
            if(resList.size() != 0) {
                List2Page<CnsInfo> list2Page = new List2Page<CnsInfo>(resList, pageSize, pageNumber);
                List<CnsInfo> finalList = list2Page.getPagedList();
                Long totalCount = (long) finalList.size();
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

        List<NodeInfo> resList = precompiledService.getNodeList(groupId);
        if(resList.size() != 0) {
            List2Page<NodeInfo> list2Page = new List2Page<NodeInfo>(resList, pageSize, pageNumber);
            List<NodeInfo> finalList = list2Page.getPagedList();
            Long totalCount = (long) finalList.size();
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    @ApiOperation(value = "nodeManageControl", notes = "set system config value by key")
    @ApiImplicitParam(name = "consensusHandle", value = "node consensus status control", required = true, dataType = "ConsensusHandle")
    @PostMapping("consensus")
    public Object nodeManageControl(@Valid @RequestBody ConsensusHandle consensusHandle, BindingResult bindingResult)throws Exception {
        String nodeType = consensusHandle.getNodeType();

        int groupId = consensusHandle.getGroupId();
        String from = consensusHandle.getFromAddress();
        String nodeId = consensusHandle.getNodeId();

        if (!PrecompiledUtils.checkNodeId(nodeId)) {
            return ConstantCode.INVALID_NODE_ID;
        }
        switch (nodeType) {
            case "remove":
                return removeNode(groupId, from, nodeId);
            case "sealer":
                return addSealer(groupId, from, nodeId);
            case "observer":
                return addObserver(groupId, from, nodeId);
            default:
                return ConstantCode.INVALID_NODE_TYPE;
        }
    }

    public Object addSealer(int groupId, String fromAddress, String nodeId) throws Exception {
        log.info("addSealer start. nodeId:: ", nodeId);

        return precompiledService.addSealer(groupId, fromAddress, nodeId);
    }

    public Object addObserver(int groupId, String fromAddress, String nodeId) throws Exception {
        log.info("addObserver start. nodeId:: ", nodeId);

        return precompiledService.addObserver(groupId, fromAddress, nodeId);
    }

    public Object removeNode(int groupId, String fromAddress, String nodeId) throws Exception {
        log.info("removeNode start. nodeId:: ", nodeId);

        return precompiledService.removeNode(groupId, fromAddress, nodeId);
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
        int groupId = crudHandle.getGroupId();
        String from = crudHandle.getFromAddress();
        String sql = crudHandle.getSql();
        String[] sqlParams = sql.split(" ");

        switch (sqlParams[0]) {
            case "create":
                return createTable(groupId, from, sql);
            case "desc":
                return desc(groupId, sql);
            case "select":
                return select(groupId, from, sql);
            case "insert":
                return insert(groupId, from, sql);
            case "update":
                return update(groupId, from, sql);
            case "delete":
                return remove(groupId, from, sql);
            default:
                return new BaseResponse(ConstantCode.PARAM_FAIL_SQL_ERROR,
                        "no such crud operation");
        }
    }


    public Object createTable(int groupId, String fromAddress, String sql) throws Exception {
        log.info("createTable start. address:: ", fromAddress);
        Table table = new Table();
        try {
            CRUDParseUtils.parseCreateTable(sql, table);
        } catch (Exception e) {
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql),
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql));
        }
        CRUDParseUtils.checkTableParams(table);
        int result = precompiledService.createTable(groupId, fromAddress, table);

        if (result == 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS,"Create '" + table.getTableName() + "' Ok.");
        } else if (result == PrecompiledCommon.TableExist_RC3) {
            return new BaseResponse(PrecompiledCommon.TableExist_RC3, "Table already exists", "Table already exists");
        } else if (result == PrecompiledCommon.PermissionDenied_RC3) {
            return new BaseResponse(PrecompiledCommon.PermissionDenied_RC3, "Permission denied", "Permission denied");
        } else {
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR, "code: " + result + "Create '" + table.getTableName() + "' failed.",
                    "code: " + result + "Create '" + table.getTableName() + "' failed.");
        }
    }

    // check table name exist by desc(tableName)
    public Object desc(int groupId, String sql) throws Exception {
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
            return new BaseResponse(ConstantCode.RET_SUCCESS, table);
        } catch (Exception e) {
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists ");
        }
    }

    public Object select(int groupId, String fromAddress, String sql) throws Exception {
        Table table = new Table();
        Condition conditions = table.getCondition();
        List<String> selectColumns = new ArrayList<>();

        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        try { //转化select语句
            CRUDParseUtils.parseSelect(sql, table, conditions, selectColumns);
        } catch (Exception e) {
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql),
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql));
        }

        Table descTable;
        try {
            descTable = precompiledService.desc(groupId, table.getTableName());
        } catch (Exception e) {
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

        result = precompiledService.select(groupId, fromAddress, table, conditions);

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
            rows = result.size();
            return new BaseResponse(ConstantCode.RET_SUCCESS, result);
        } else {
            List<Map<String, String>> selectedResult = CRUDParseUtils.getSeletedColumn(selectColumns, result);
            rows = selectedResult.size();
            return new BaseResponse(ConstantCode.RET_SUCCESS, selectedResult);
        }

        //return new BaseResponse(ConstantCode.RET_SUCCESS, rows + " row(s) in set.");
    }

    public Object insert(int groupId, String fromAddress, String sql) throws Exception {
        Table table = new Table();
        Entry entry = new Entry();

        // insert sql use "values" or not
        boolean useValues = false;
        try {
            useValues = CRUDParseUtils.parseInsert(sql, table, entry);
        } catch (Exception e) {
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
        int insertResult = precompiledService.insert(groupId, fromAddress, table, entry);
        if (insertResult >= 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS,
                    "Insert OK, " + insertResult + " row(s) affected.");
        } else {
            return new BaseResponse(ConstantCode.SQL_ERROR, "Insert failed.");
        }
    }

    public Object update(int groupId, String fromAddress, String sql) throws Exception {
        Table table = new Table();
        Entry entry = new Entry();
        Condition conditions = new Condition();

        try {
            CRUDParseUtils.parseUpdate(sql, table, entry, conditions);
        } catch (Exception e) {
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql),
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql));
        }

        String tableName = table.getTableName();
        Table descTable;
        try {
           descTable = precompiledService.desc(groupId, tableName);
        } catch (Exception e) {
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
        int updateResult = precompiledService.update(groupId, fromAddress, table, entry, conditions);
        if (updateResult >= 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS,
                    "Update OK, " + updateResult + " row(s) affected.");
        } else {
            return new BaseResponse(ConstantCode.SQL_ERROR, "Update failed.");
        }

    }

    public Object remove(int groupId, String fromAddress, String sql) throws Exception {
        Table table = new Table();
        Condition conditions = new Condition();

        int code;
        String msg;
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        try {
            CRUDParseUtils.parseRemove(sql, table, conditions);
        } catch (Exception e) {
            return new BaseResponse(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql),
                    "Could not parse SQL statement." + CRUDParseUtils.invalidSymbolReturn(sql));
        }

        Table descTable;
        try {
            descTable = precompiledService.desc(groupId, table.getTableName());
        } catch (Exception e) {
            return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, "Table not exists");
        }
        table.setKey(descTable.getKey());
        CRUDParseUtils.handleKey(table, conditions);
        int removeResult = precompiledService.remove(groupId, fromAddress, table, conditions);
        if (removeResult >= 0) {
            return new BaseResponse(ConstantCode.RET_SUCCESS,
                    "Remove OK, " + removeResult + " row(s) affected.");
        } else {
            return new BaseResponse(ConstantCode.SQL_ERROR, "Remove failed.");
        }

    }

}
