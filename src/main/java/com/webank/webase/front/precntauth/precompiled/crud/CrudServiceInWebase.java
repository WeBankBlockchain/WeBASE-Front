package com.webank.webase.front.precntauth.precompiled.crud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precntauth.precompiled.crud.model.CRUDParseUtils;
import com.webank.webase.front.precntauth.precompiled.crud.model.Table;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.util.PrintUtil;
import com.webank.webase.front.web3api.Web3ApiService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.ConditionV320;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.UpdateFields;
import org.fisco.bcos.sdk.v3.model.EnumNodeVersion;
import org.fisco.bcos.sdk.v3.model.PrecompiledConstant;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author litieqiao
 * Description 新增CRUD相关接口业务逻辑实现所需的服务
 * Version 1.0
 */
@Slf4j
@Service
public class CrudServiceInWebase {
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private TransService transService;

    private static final Logger logger = LoggerFactory.getLogger(CrudServiceInWebase.class);

    /**
     * Function: create Table By sqlCreate
     * @param groupId   链的group组Id
     * @param sqlCreate   创建表的SQL, e.g.   "create table t_demo3(name varchar, item_id varchar, item_name varchar, primary key(name))"
     * @return boolean
     * @throws JSQLParserException JSQLParserException
     * @throws ContractException ContractException
     */
    public boolean createTable(String groupId, String signUserId, String sqlCreate) throws JSQLParserException,
            ContractException {
        try {
            Table table = new Table();

            TableCRUDService tableCRUDService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                    keyStoreService.getCredentials(signUserId,groupId));

            // sql转换
            CRUDParseUtils.parseCreateTable(sqlCreate, table);
            Map<String, List<String>> tableDesc;
            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.valueOf((int) tableCRUDService.getCurrentVersion()).toVersionObj();
            RetCode result;
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                result =
                        tableCRUDService.createTable(
                                table.getTableName(),
                                table.getKeyOrder(),
                                table.getKeyFieldName(),
                                table.getValueFields());
            } else {
                result =
                        tableCRUDService.createTable(
                                table.getTableName(), table.getKeyFieldName(), table.getValueFields());
            }
            // parse the result
            if (result.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("Create '" + table.getTableName() + "' Ok.");
            } else {
                System.out.println("Create '" + table.getTableName() + "' failed ");
                PrintUtil.printJson(result.toString());
                return false;
            }

            return true;
        } catch (FrontException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        } catch (ContractException | JSQLParserException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sqlCreate);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        }
    }

    /**
     *  Function: desc Table
     * @param groupId   链的group组Id
     * @param tableName  目标表的名称, e.g.  "t_demo3"
     * @return String
     */
    public String descTable(String groupId, String signUserId, String tableName) {
        try {
            TableCRUDService tableCRUDService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                    keyStoreService.getCredentials(signUserId,groupId));
            CRUDParseUtils.invalidSymbol(tableName);
            if (tableName.endsWith(";")) {
                tableName = tableName.substring(0, tableName.length() - 1);
            }
            Map<String, List<String>> tableDesc;
            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.valueOf((int) tableCRUDService.getCurrentVersion()).toVersionObj();
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                tableDesc = tableCRUDService.descWithKeyOrder(tableName);
            } else {
                tableDesc = tableCRUDService.desc(tableName);
            }
            PrintUtil.printJson(ObjectMapperFactory.getObjectMapper().writeValueAsString(tableDesc));

            return tableDesc.toString();
        } catch (FrontException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        } catch (ContractException | JsonProcessingException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(tableName);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        }
    }

    /**
     * Function: insert Table
     * @param groupId   链的group组Id
     * @param sqlInsert   插入表的SQL, e.g.  "insert into t_demo3 values (fruit, 1, apple1)"
     * @return boolean
     */
    public boolean insertTable(String groupId, String signUserId, String sqlInsert) {
        try {
            Table table = new Table();
            String tableName = CRUDParseUtils.parseTableNameFromSql(sqlInsert);

            TableCRUDService tableCRUDService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                    keyStoreService.getCredentials(signUserId,groupId));
            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.valueOf((int) tableCRUDService.getCurrentVersion()).toVersionObj();
            Map<String, List<String>> descTable;
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                descTable = tableCRUDService.descWithKeyOrder(tableName);
            } else {
                descTable = tableCRUDService.desc(tableName);
            }
            table.setTableName(tableName);
            if (checkTableNotExistence(descTable)) {
                System.out.println("The table \"" + tableName + "\" doesn't exist!");
                throw new FrontException("The table \"" + tableName + "\" doesn't exist!");
            }
            logger.debug("insert, tableName: {}, descTable: {}", tableName, descTable);
            Entry entry = CRUDParseUtils.parseInsert(sqlInsert, table, descTable);
            String keyName = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
            String keyValue = entry.getKey();
            logger.debug(
                    "fieldNameToValue: {}, keyName: {}, keyValue: {}",
                    entry.getFieldNameToValue(),
                    keyName,
                    keyValue);
            if (keyValue == null) {
                throw new FrontException("Please insert the key field '" + keyName + "'.");
            }

            RetCode insertResult = tableCRUDService.insert(table.getTableName(), entry);
            if (insertResult.getCode() >= 0) {
                System.out.println("Insert OK: ");
                System.out.println(insertResult.getCode() + " row(s) affected.");
            } else {
                System.out.println("Result of insert for " + table.getTableName() + ":");
                PrintUtil.printJson(insertResult.toString());
                return false;
            }
            System.out.println("insertResult " + insertResult);
            return true;
        } catch (FrontException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        } catch (ContractException | JSQLParserException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sqlInsert);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        }
    }


    /**
     * Function: selectTable Table By sqlSelect
     * @param groupId   链的group组Id
     * @param sqlSelect   查询表的SQL, e.g. "select * from t_demo3 where name = fruit"
     * @return List<Map<String, String>>
     */
    public List<Map<String, String>> selectTable(String groupId, String signUserId, String sqlSelect) {
        try {
            Table table = new Table();
            List<String> selectColumns = new ArrayList<>();
            String tableName = CRUDParseUtils.parseTableNameFromSql(sqlSelect);
            table.setTableName(tableName);
            TableCRUDService tableCRUDService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                    keyStoreService.getCredentials(signUserId,groupId));
            Map<String, List<String>> descTable = tableCRUDService.desc(tableName);
            if (checkTableNotExistence(descTable)) {
                System.out.println("The table \"" + table.getTableName() + "\" doesn't exist!");
                throw new FrontException("The table \"" + table.getTableName() + "\" doesn't exist!");
            }
            String keyField = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
            table.setKeyFieldName(keyField);
            table.setValueFields(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));
            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.valueOf((int) tableCRUDService.getCurrentVersion()).toVersionObj();

            List<Map<String, String>> resultSelect = new ArrayList<>();
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                ConditionV320 conditionV320 = new ConditionV320();
                CRUDParseUtils.parseSelect(sqlSelect, table, selectColumns, conditionV320);
                resultSelect = tableCRUDService.select(table.getTableName(), descTable, conditionV320);
            } else {
                Condition condition = new Condition();
                CRUDParseUtils.parseSelect(sqlSelect, table, selectColumns, condition);
                String keyValue = condition.getEqValue();
                if (keyValue.isEmpty()) {
                    resultSelect = tableCRUDService.select(table.getTableName(), descTable, condition);
                } else {
                    Map<String, String> select =
                            tableCRUDService.select(table.getTableName(), descTable, keyValue);
                    if (select.isEmpty()) {
                        System.out.println("Empty set.");
                        throw new FrontException("Empty set.");
                    }
                    resultSelect.add(select);
                }
            }
            int rows;
            if (resultSelect.isEmpty()) {
                System.out.println("Empty set.");
                throw new FrontException("Empty set.");
            }
            if ("*".equals(selectColumns.get(0))) {
                selectColumns.clear();
                selectColumns.add(keyField);
                selectColumns.addAll(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));
                resultSelect = getSelectedColumn(selectColumns, resultSelect);
                rows = resultSelect.size();
            } else {
                List<Map<String, String>> selectedResult = getSelectedColumn(selectColumns, resultSelect);
                rows = selectedResult.size();
            }
            System.out.println(rows + " row(s) in set.");
            System.out.println("table select: " + resultSelect.size());
            return resultSelect;
        } catch (FrontException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        } catch (ContractException | JSQLParserException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sqlSelect);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        }
    }

    /**
     * Function: update Table By sqlUpdate
     * @param groupId   链的group组Id
     * @param sqlUpdate   更新表的SQL, e.g. "update t_demo3 set item_name = orange where name = fruit and item_id = 1"
     * @return boolean
     */
    public boolean updateTable(String groupId, String signUserId, String sqlUpdate) {
        try {
            Table table = new Table();
            UpdateFields updateFields = new UpdateFields();
            String tableName = CRUDParseUtils.parseTableNameFromSql(sqlUpdate);
            TableCRUDService tableCRUDService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                    keyStoreService.getCredentials(signUserId,groupId));
            Map<String, List<String>> descTable = tableCRUDService.desc(tableName);
            if (checkTableNotExistence(descTable)) {
                System.out.println("The table \"" + tableName + "\" doesn't exist!");
                return false;
            }
            String keyName = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
            if (updateFields.getFieldNameToValue().containsKey(keyName)) {
                System.out.println("Please don't set the key field \"" + keyName + "\".");
                return false;
            }
            table.setKeyFieldName(keyName);
            table.setValueFields(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));
            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.valueOf((int) tableCRUDService.getCurrentVersion()).toVersionObj();
            RetCode updateResult;
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                ConditionV320 conditionV320 = new ConditionV320();
                CRUDParseUtils.parseUpdate(sqlUpdate, table, conditionV320, updateFields);
                updateResult = tableCRUDService.update(tableName, conditionV320, updateFields);
            } else {
                Condition condition = new Condition();
                CRUDParseUtils.parseUpdate(sqlUpdate, table, condition, updateFields);

                String keyValue = condition.getEqValue();
                updateResult =
                        keyValue.isEmpty()
                                ? tableCRUDService.update(tableName, condition, updateFields)
                                : tableCRUDService.update(tableName, keyValue, updateFields);
            }

            if (updateResult.getCode() >= 0) {
                System.out.println("Update success: " + updateResult.getCode() + " row affected.");
            } else {
                System.out.println("Result of update " + tableName + " :");
                PrintUtil.printJson(updateResult.toString());
            }

            return (updateResult.getCode() >= 0);
        } catch (FrontException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        } catch (ContractException | JSQLParserException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sqlUpdate);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        }
    }

    /**
     * Function: remove Table By sqlRemove
     * @param groupId   链的group组Id
     * @param sqlRemove   删除表的SQL, e.g. "delete from t_demo3 where name = fruit and item_id = 1"
     * @return boolean
     */
    public boolean removeTable(String groupId, String signUserId, String sqlRemove) {
        try {
            Table table = new Table();
            String tableName = CRUDParseUtils.parseTableNameFromSql(sqlRemove);
            TableCRUDService tableCRUDService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                    keyStoreService.getCredentials(signUserId,groupId));
            Map<String, List<String>> descTable = tableCRUDService.desc(tableName);
            table.setTableName(tableName);
            if (checkTableNotExistence(descTable)) {
                System.out.println("The table \"" + table.getTableName() + "\" doesn't exist!");
                return false;
            }
            table.setKeyFieldName(descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0));
            table.setValueFields(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));

            EnumNodeVersion.Version supportedVersion = EnumNodeVersion.valueOf((int) tableCRUDService.getCurrentVersion())
                    .toVersionObj();
            RetCode removeResult;
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                ConditionV320 conditionV320 = new ConditionV320();
                CRUDParseUtils.parseRemove(sqlRemove, table, conditionV320);
                removeResult = tableCRUDService.remove(table.getTableName(), conditionV320);
            } else {
                Condition condition = new Condition();
                CRUDParseUtils.parseRemove(sqlRemove, table, condition);
                String keyValue = condition.getEqValue();
                removeResult =
                        keyValue.isEmpty()
                                ? tableCRUDService.remove(table.getTableName(), condition)
                                : tableCRUDService.remove(table.getTableName(), keyValue);
            }

            return (removeResult.getCode() >= 0);
        } catch (FrontException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        } catch (ContractException | JSQLParserException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sqlRemove);
            throw new FrontException(" message: {}, e:"+e.getMessage());
        }
    }


    //判断表是否存在
    private boolean checkTableNotExistence(Map<String, List<String>> descTable) {
        return !(descTable.size() != 0
                && !descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0).isEmpty());
    }

    //获取查询的目标列表项
    private List<Map<String, String>> getSelectedColumn(
            List<String> selectColumns, List<Map<String, String>> result) {
        List<Map<String, String>> selectedResult = new ArrayList<>(result.size());
        Map<String, String> selectedRecords;
        for (Map<String, String> records : result) {
            selectedRecords = new LinkedHashMap<>();
            for (String column : selectColumns) {
                Set<String> recordKeys = records.keySet();
                for (String recordKey : recordKeys) {
                    if (recordKey.equals(column)) {
                        selectedRecords.put(recordKey, records.get(recordKey));
                    }
                }
            }
            selectedResult.add(selectedRecords);
        }
        selectedResult.forEach(System.out::println);
        return selectedResult;
    }
}
