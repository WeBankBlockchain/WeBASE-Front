/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.precntauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.webank.webase.front.base.TestBase;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.precntauth.precompiled.crud.model.CRUDParseUtils;
import com.webank.webase.front.precntauth.precompiled.crud.model.Table;
import com.webank.webase.front.util.JsonUtils;

import java.util.*;

import com.webank.webase.front.util.PrintUtil;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.*;
import org.fisco.bcos.sdk.v3.model.EnumNodeVersion;
import org.fisco.bcos.sdk.v3.model.PrecompiledConstant;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class CrudServiceTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(CrudServiceTest.class);

    public static ApplicationContext context = null;
    public static Table table;
    public static TableCRUDService tableCRUDService;
    public static EnumNodeVersion.Version supportedVersion;
    public static String sqlCreate = "create table t_demo3(name varchar, item_id varchar, item_name varchar, primary key(name))";
    public static String sqlInsert = "insert into t_demo3 values (fruit, 1, apple1)";
    public static String sqlSelect = "select * from t_demo3 where name = fruit";
    public static String sqlUpdate = "update t_demo3 set item_name = orange where name = fruit and item_id = 1";
    public static String sqlRemove = "delete from t_demo3 where name = fruit and item_id = 1";

   @Before
   public void beforeTest(){
       table = new Table();
       tableCRUDService = new TableCRUDService(web3j, cryptoKeyPair);
       supportedVersion =
               EnumNodeVersion.valueOf((int) tableCRUDService.getCurrentVersion()).toVersionObj();
   }

    /**
     * 参考console中的实现，利用sqlUtilsParse(String sql) 转换为condition
     * @throws Exception Exception
     */
    @Test
    public void testCrudCreateTable() throws Exception {
        // sql转换
        CRUDParseUtils.parseCreateTable(sqlCreate, table);
        //Create Table
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
        }
        System.out.println(result);
        assertEquals(result.getCode(),0);
    }

    @Test
    public void testCrudDesc() throws Exception {
        // desc
        String tableName = "t_demo3";
        CRUDParseUtils.invalidSymbol(tableName);
        Map<String, List<String>> tableDesc;
        if (tableName.endsWith(";")) {
            tableName = tableName.substring(0, tableName.length() - 1);
        }
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
            tableDesc = tableCRUDService.descWithKeyOrder(tableName);
        } else {
            tableDesc = tableCRUDService.desc(tableName);
        }
        PrintUtil.printJson(ObjectMapperFactory.getObjectMapper().writeValueAsString(tableDesc));
    }

    @Test
    public void testCrudInsert() throws Exception {
        // Insert
        String tableName = CRUDParseUtils.parseTableNameFromSql(sqlInsert);
        tableCRUDService = new TableCRUDService(web3j, cryptoKeyPair);
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
        }
        System.out.println("insertResult " + insertResult);
    }

    @Test
    public void testCrudSelect() throws Exception {
        //select
        List<String> selectColumns = new ArrayList<>();
        String tableName = CRUDParseUtils.parseTableNameFromSql(sqlSelect);
        table.setTableName(tableName);
        Map<String, List<String>> descTableSelect = tableCRUDService.desc(tableName);
        if (checkTableNotExistence(descTableSelect)) {
            System.out.println("The table \"" + table.getTableName() + "\" doesn't exist!");
            return;
        }
        String keyField = descTableSelect.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
        table.setKeyFieldName(keyField);
        table.setValueFields(descTableSelect.get(PrecompiledConstant.VALUE_FIELD_NAME));

        List<Map<String, String>> resultSelect = new ArrayList<>();
        String keyValueSelect;
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
            ConditionV320 conditionV320 = new ConditionV320();
            CRUDParseUtils.parseSelect(sqlSelect, table, selectColumns, conditionV320);
            resultSelect = tableCRUDService.select(table.getTableName(), descTableSelect, conditionV320);
        } else {
            Condition condition = new Condition();
            CRUDParseUtils.parseSelect(sqlSelect, table, selectColumns, condition);
            keyValueSelect = condition.getEqValue();
            if (keyValueSelect.isEmpty()) {
                resultSelect = tableCRUDService.select(table.getTableName(), descTableSelect, condition);
            } else {
                Map<String, String> select =
                        tableCRUDService.select(table.getTableName(), descTableSelect, keyValueSelect);
                if (select.isEmpty()) {
                    System.out.println("Empty set.");
                    return;
                }
                resultSelect.add(select);
            }
        }
        int rows;
        if (resultSelect.isEmpty()) {
            System.out.println("Empty set.");
            return;
        }
        if ("*".equals(selectColumns.get(0))) {
            selectColumns.clear();
            selectColumns.add(keyField);
            selectColumns.addAll(descTableSelect.get(PrecompiledConstant.VALUE_FIELD_NAME));
            resultSelect = getSelectedColumn(selectColumns, resultSelect);
            rows = resultSelect.size();
        } else {
            List<Map<String, String>> selectedResult = getSelectedColumn(selectColumns, resultSelect);
            rows = selectedResult.size();
        }
        System.out.println(rows + " row(s) in set.");
        System.out.println("table select: " + resultSelect.size());
        assertTrue(resultSelect.size() > 0);
    }

    @Test
    public void testCrudUpdate() throws Exception {
        // update
        UpdateFields updateFields = new UpdateFields();
        String tableNameUpdate = CRUDParseUtils.parseTableNameFromSql(sqlUpdate);
        TableCRUDService tableCRUDService = new TableCRUDService(web3j, cryptoKeyPair);
        Map<String, List<String>> descTableUpdate = tableCRUDService.desc(tableNameUpdate);
        if (checkTableNotExistence(descTableUpdate)) {
            System.out.println("The table \"" + tableNameUpdate + "\" doesn't exist!");
            return;
        }
        String keyNameUpdate = descTableUpdate.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
        if (updateFields.getFieldNameToValue().containsKey(keyNameUpdate)) {
            System.out.println("Please don't set the key field \"" + keyNameUpdate + "\".");
            return;
        }
        table.setKeyFieldName(keyNameUpdate);
        table.setValueFields(descTableUpdate.get(PrecompiledConstant.VALUE_FIELD_NAME));
        RetCode updateResult;
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
            ConditionV320 conditionV320 = new ConditionV320();
            CRUDParseUtils.parseUpdate(sqlUpdate, table, conditionV320, updateFields);
            updateResult = tableCRUDService.update(tableNameUpdate, conditionV320, updateFields);
        } else {
            Condition condition = new Condition();
            CRUDParseUtils.parseUpdate(sqlUpdate, table, condition, updateFields);

            String keyValueUpdate = condition.getEqValue();
            updateResult =
                    keyValueUpdate.isEmpty()
                            ? tableCRUDService.update(tableNameUpdate, condition, updateFields)
                            : tableCRUDService.update(tableNameUpdate, keyValueUpdate, updateFields);
        }

        if (updateResult.getCode() >= 0) {
            System.out.println("Update success: " + updateResult.getCode() + " row affected.");
        } else {
            System.out.println("Result of update " + tableNameUpdate + " :");
            PrintUtil.printJson(updateResult.toString());
        }
    }

    @Test
    public void testCrudRemove() throws Exception {

        // remove
        String tableNameRemove = CRUDParseUtils.parseTableNameFromSql(sqlRemove);
        Map<String, List<String>> descTableDel = tableCRUDService.desc(tableNameRemove);
        table.setTableName(tableNameRemove);
        if (checkTableNotExistence(descTableDel)) {
            System.out.println("The table \"" + table.getTableName() + "\" doesn't exist!");
            return;
        }
        table.setKeyFieldName(descTableDel.get(PrecompiledConstant.KEY_FIELD_NAME).get(0));
        table.setValueFields(descTableDel.get(PrecompiledConstant.VALUE_FIELD_NAME));
        RetCode removeResult;
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
            ConditionV320 conditionV320 = new ConditionV320();
            CRUDParseUtils.parseRemove(sqlRemove, table, conditionV320);
            removeResult = tableCRUDService.remove(table.getTableName(), conditionV320);
        } else {
            Condition condition = new Condition();
            CRUDParseUtils.parseRemove(sqlRemove, table, condition);
            String keyValueDel = condition.getEqValue();
            removeResult =
                    keyValueDel.isEmpty()
                            ? tableCRUDService.remove(table.getTableName(), condition)
                            : tableCRUDService.remove(table.getTableName(), keyValueDel);
        }

        if (removeResult.getCode() >= 0) {
            System.out.println("Remove OK, " + removeResult.getCode() + " row(s) affected.");
        } else {
            System.out.println("Result of Remove " + table.getTableName() + " :");
            PrintUtil.printJson(removeResult.toString());
        }

        System.out.println("removeResult " + removeResult);
        assertTrue(removeResult.getCode() >= 0 );
    }

    private boolean checkTableNotExistence(Map<String, List<String>> descTable) {
        return !(descTable.size() != 0
                && !descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0).isEmpty());
    }

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
