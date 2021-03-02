///*
// * Copyright 2014-2020 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.webank.webase.front.precompiledapi;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import com.webank.webase.front.base.TestBase;
//import com.webank.webase.front.base.exception.FrontException;
//import com.webank.webase.front.precompiledapi.crud.CRUDParseUtils;
//import com.webank.webase.front.precompiledapi.crud.Table;
//import com.webank.webase.front.util.JsonUtils;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import org.fisco.bcos.sdk.contract.precompiled.crud.TableCRUDService;
//import org.fisco.bcos.sdk.contract.precompiled.crud.common.Condition;
//import org.fisco.bcos.sdk.contract.precompiled.crud.common.ConditionOperator;
//import org.fisco.bcos.sdk.contract.precompiled.crud.common.Entry;
//import org.fisco.bcos.sdk.model.PrecompiledConstant;
//import org.fisco.bcos.sdk.model.RetCode;
//import org.junit.Test;
//import org.springframework.context.ApplicationContext;
//
//public class CrudServiceTest extends TestBase {
//
//    //String addSealer(String nodeId)： 根据节点NodeID设置对应节点为共识节点。
//    //String addObserver(String nodeId)： 根据节点NodeID设置对应节点为观察节点。
//    //String removeNode(String nodeId)： 根据节点NodeID设置对应节点为游离节点。
//
//    public static ApplicationContext context = null;
//    public static String tableName;
//    public static Table table;
//    public static Entry entry;
//    public static Entry updateEntry;
//    public static Condition condition;
//    public static String sqlCreate = "create table t_demo3(name varchar, item_id varchar, item_name varchar, primary key(name))";
//    public static String sqlInsert = "insert into t_demo3 values (fruit, 1, apple1)";
//    public static String sqlSelect = "select * from t_demo3 where name = fruit";
//    public static String sqlUpdate = "update t_demo3 set item_name = orange where name = fruit and item_id = 1";
//    public static String sqlRemove = "delete from t_demo3 where name = fruit and item_id = 1";
//    /**
//     * 参考console中的实现，利用sqlUtilsParse(String sql) 转换为condition
//     * @throws Exception
//     */
//    @Test
//    public void testCrud() throws Exception {
//        tableName = "t_demo3";
//        table = new Table();
//        entry = new Entry();
//
//        TableCRUDService crudService = new TableCRUDService(web3j, cryptoKeyPair);
//
//        // sql转换
//        CRUDParseUtils.parseCreateTable(sqlCreate, table);
//        //CRUDParseUtils.checkTableParams(table);
//        RetCode createRes = crudService.createTable(table.getTableName(), table.getKey(),
//            JsonUtils.toJavaObject(table.getValueFields(), List.class));
//        System.out.println(createRes);
//        assertEquals(0, createRes);
//
//        // desc
//        List<Map<String, String>> descRes = crudService.desc(tableName);
//        String descKeyName = descRes.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
//        String descFields = descRes.get(0).get(PrecompiledConstant.VALUE_FIELD_NAME);
//
//        System.out.println("tablename: " + descRes);
//        assertNotNull(descRes);
//
//        // Insert
//        entry = table.getEntry();
//        CRUDParseUtils.parseInsert(sqlInsert, table, entry);
//        String tableName1 = table.getTableName();
//        List<String> fieldsList = Arrays.asList(descFields.split(","));
//        Set<String> entryFields = entry.getFieldNameToValue().keySet();
//
//        Entry entryValue = table.getEntry();
//        for (int i = 0; i < entry.getFieldNameToValue().size(); i++) {
//            for (String entryField : entryFields) {
//                if ((i + "").equals(entryField)) {
//                    Map<String, String> map = new HashMap<>();
//                    map.put(fieldsList.get(i), entry.getFieldNameToValue().get(i + ""));
//                    entryValue.setFieldNameToValue(map);
//                    if (descKeyName.equals(fieldsList.get(i))) {
//                        table.setKey(entry.getFieldNameToValue().get(i + ""));
//                    }
//                }
//            }
//        }
//        entry = entryValue;
//        //CRUDParseUtils.checkUserTableParam(entry, );
//        RetCode insertResult = crudService.insert(table.getTableName(), table.getKey(), entry);
//        System.out.println("insertResult " + insertResult);
//
//        //select
//        condition = table.getCondition();
//        List<String> selectColumns = new ArrayList<>();
//        CRUDParseUtils.parseSelect(sqlSelect, table, condition, selectColumns);
//        table.setKey(descKeyName);
//        handleKey(table, condition);
//        descFields = descKeyName + "," + descFields;
//        fieldsList = Arrays.asList(descFields.split(","));
//
//        List<Map<String, String>> result = crudService.select(table.getTableName(), table.getKey(), condition);
//        System.out.println("table select: " + result.size());
//        assertTrue(result.size() > 0);
//
//        // update
//        entry = table.getEntry();
//        condition = table.getCondition();
//        CRUDParseUtils.parseUpdate(sqlUpdate, table, entry, condition);
//        String tableName = table.getTableName();
//
//        table.setKey(descKeyName);
//        handleKey(table, condition);
//        Set<String> conditonFields = condition.getConditions().keySet();
//        Set<String> allFields = new HashSet<>();
//        allFields.addAll(entryFields);
//        allFields.addAll(conditonFields);
//        //CRUDParseUtils.checkUserTableParam(entry, descTable);
//        RetCode updateResult = crudService.update(table.getTableName(), table.getKey(), entry, condition);
//        System.out.println("updateResult: " + updateResult);
////        assertTrue(updateResult >= 0);
//        condition = table.getCondition();
//        CRUDParseUtils.parseRemove(sqlRemove, table, condition);
//        // remove
//        RetCode removeResult = crudService.remove(table.getTableName(), table.getKey(), condition);
//        System.out.println("removeResult " + removeResult);
////        assertTrue(removeResult >= 0 );
//    }
//
//    private void handleKey(Table table, Condition condition) {
//
//        String descKeyName = table.getKey();
//        String keyValue = "";
//        Map<ConditionOperator, String> keyMap = condition.getConditions().get(descKeyName);
//        if (keyMap == null) {
//            throw new FrontException(
//                    "Please provide a equal condition for the key field '"
//                            + descKeyName
//                            + "' in where clause.");
//        } else {
//            Set<ConditionOperator> keySet = keyMap.keySet();
//            for (ConditionOperator enumOP : keySet) {
//                if (enumOP != ConditionOperator.eq) {
//                    throw new FrontException(
//                            "Please provide a equal condition for the key field '"
//                                    + descKeyName
//                                    + "' in where clause.");
//                } else {
//                    keyValue = keyMap.get(enumOP);
//                }
//            }
//        }
//        table.setKey(keyValue);
//    }
//}
