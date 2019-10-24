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

import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.channel.test.TestBase;
import com.webank.webase.front.util.CRUDParseUtils;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.crud.*;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CrudServiceTest extends TestBase {

    //String addSealer(String nodeId)： 根据节点NodeID设置对应节点为共识节点。
    //String addObserver(String nodeId)： 根据节点NodeID设置对应节点为观察节点。
    //String removeNode(String nodeId)： 根据节点NodeID设置对应节点为游离节点。

    public static ApplicationContext context = null;
    public static String tableName;
    public static Table table;
    public static Entry entry;
    public static Entry updateEntry;
    public static Condition condition;
    public static String sqlCreate = "create table t_demo3(name varchar, item_id varchar, item_name varchar, primary key(name))";
    public static String sqlInsert = "insert into t_demo3 values (fruit, 1, apple1)";
    public static String sqlSelect = "select * from t_demo3 where name = fruit";
    public static String sqlUpdate = "update t_demo3 set item_name = orange where name = fruit and item_id = 1";
    public static String sqlRemove = "delete from t_demo3 where name = fruit and item_id = 1";
    /**
     * 参考console中的实现，利用sqlUtilsParse(String sql) 转换为condition
     * @throws Exception
     */
    @Test
    public void testCrud() throws Exception {
        tableName = "t_demo3";
        table = new Table();
        entry = new Entry();

        //链管理员私钥加载
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));

        CRUDService crudService = new CRUDService(web3j, credentialsPEM);

        // sql转换
        CRUDParseUtils.parseCreateTable(sqlCreate, table);
        CRUDParseUtils.checkTableParams(table);
//        int createRes = crudService.createTable(table);
//        System.out.println(createRes);
//        assertTrue(createRes == 0);

        // desc
        System.out.println("tablename: " + crudService.desc(tableName).getTableName());
        assertNotNull(crudService.desc(tableName).getTableName());

        // Insert
        entry = table.getEntry();
        CRUDParseUtils.parseInsert(sqlInsert, table, entry);
        String tableName1 = table.getTableName();
        Table descTable = crudService.desc(tableName1);
        String keyName = descTable.getKey();
        String fields = keyName + "," + descTable.getValueFields();
        List<String> fieldsList = Arrays.asList(fields.split(","));
        Set<String> entryFields = entry.getFields().keySet();

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
        CRUDParseUtils.checkUserTableParam(entry, descTable);
        int insertResult = crudService.insert(table, entry);
        System.out.println("insertResult " + insertResult);
        assertTrue(insertResult== 0 || insertResult == 1);

        //select
        condition = table.getCondition();
        List<String> selectColumns = new ArrayList<>();
        CRUDParseUtils.parseSelect(sqlSelect, table, condition, selectColumns);
        descTable = crudService.desc(table.getTableName());
        table.setKey(descTable.getKey());
        handleKey(table, condition);
        fields = descTable.getKey() + "," + descTable.getValueFields();
        fieldsList = Arrays.asList(fields.split(","));

        List<Map<String, String>> result = crudService.select(table, condition);
        System.out.println("table select: " + result.size());
        assertTrue(result.size() > 0);

        // update
        entry = table.getEntry();
        condition = table.getCondition();
        CRUDParseUtils.parseUpdate(sqlUpdate, table, entry, condition);
        String tableName = table.getTableName();
        descTable = crudService.desc(tableName);
        keyName = descTable.getKey();
        table.setKey(descTable.getKey());
        handleKey(table, condition);
        Set<String> conditonFields = condition.getConditions().keySet();
        Set<String> allFields = new HashSet<>();
        allFields.addAll(entryFields);
        allFields.addAll(conditonFields);
        CRUDParseUtils.checkUserTableParam(entry, descTable);
        int updateResult = crudService.update(table, entry, condition);
        System.out.println("updateResult: " + updateResult);
        assertTrue(updateResult >= 0);
//        System.out.println(crudService.select(table, condition));
//        assertNotNull(crudService.select(table, condition));
        condition = table.getCondition();
        CRUDParseUtils.parseRemove(sqlRemove, table, condition);
        // remove
        int removeResult = crudService.remove(table, condition);
        System.out.println("removeResult " + removeResult);
        assertTrue(removeResult >= 0 );
    }
    private void handleKey(Table table, Condition condition) throws Exception {

        String keyName = table.getKey();
        String keyValue = "";
        Map<EnumOP, String> keyMap = condition.getConditions().get(keyName);
        if (keyMap == null) {
            throw new FrontException(
                    "Please provide a equal condition for the key field '"
                            + keyName
                            + "' in where clause.");
        } else {
            Set<EnumOP> keySet = keyMap.keySet();
            for (EnumOP enumOP : keySet) {
                if (enumOP != EnumOP.eq) {
                    throw new FrontException(
                            "Please provide a equal condition for the key field '"
                                    + keyName
                                    + "' in where clause.");
                } else {
                    keyValue = keyMap.get(enumOP);
                }
            }
        }
        table.setKey(keyValue);
    }
}
