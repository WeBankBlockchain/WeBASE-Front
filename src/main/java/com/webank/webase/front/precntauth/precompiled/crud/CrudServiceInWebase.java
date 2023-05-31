package com.webank.webase.front.precntauth.precompiled.crud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonArray;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precntauth.precompiled.base.PrecompiledUtils;
import com.webank.webase.front.precntauth.precompiled.crud.model.CRUDParseUtils;
import com.webank.webase.front.precntauth.precompiled.crud.model.Table;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.PrintUtil;
import com.webank.webase.front.web3api.Web3ApiService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodec;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.codec.abi.Constant;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.wrapper.*;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.TableManagerPrecompiled;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.TablePrecompiled;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.*;
import org.fisco.bcos.sdk.v3.contract.precompiled.model.PrecompiledAddress;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.*;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Common.TABLE_PREFIX;

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
     * Function: create Table By parameters
     *
     * @param groupId      链的group组Id
     * @param signUserId   请求的userId,即用户Id
     * @param tableName   表名
     * @param keyFieldName  key字段
     * @param valueFields   value字段集
     * @param keyOrder     Common.TableKeyOrder的三种int取值： Unknown(-1),Lexicographic(0),
     *                     Numerical(1)
     * @return Object
     * @throws JSQLParserException JSQLParserException
     * @throws ContractException   ContractException
     */
    public Object createTable(String groupId, String signUserId, String tableName,
                              String keyFieldName, List<String> valueFields, int keyOrder) throws ContractException, JSQLParserException {
        return this.createTableHandle(groupId, signUserId, tableName, keyFieldName,
                valueFields, keyOrder);
    }

    private Object createTableHandle(String groupId, String signUserId, String tableName,
                                     String keyFieldName, List<String> valueFields,
                                     int keyOrderValue) {

        //友好提示：这种TableInfoV320的尚不支持，原因是TableManagerPrecompiled.ABI
        // 中的createTable这个func的Tableinfo参数中，还不支持keyOrder。
        // 相当于说能支持TableManagerPrecompiled.TableInfo传参，但TableManagerPrecompiled.TableInfoV320
        // 目前还不能传。所以创建表这块，keyOrder字段目前无法生效，该预留接口字段的功能，待后续随社区进度进一步完善。

//        Common.TableKeyOrder keyOrder = Common.TableKeyOrder.valueOf(keyOrderValue);
//        TableManagerPrecompiled.TableInfoV320 tableInfo =
//                new TableManagerPrecompiled.TableInfoV320(
//                        keyOrder.getBigValue(), keyFieldName, valueFields);

        TableManagerPrecompiled.TableInfo tableInfo =
                new TableManagerPrecompiled.TableInfo(
                        keyFieldName, valueFields);

        List<Object> funcParams=new ArrayList<>();
        funcParams.add(String.valueOf(tableName));
        funcParams.add(tableInfo);

        boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
        // execute createtable method
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignObj(groupId,
                        signUserId, PrecompiledAddress.TABLE_MANAGER_PRECOMPILED_ADDRESS,
                        TableManagerPrecompiled.ABI, TableManagerPrecompiled.FUNC_CREATETABLE,
                        funcParams);
        return com.webank.webase.front.precntauth.precompiled.base.PrecompiledUtils
                .handleTransactionReceipt(receipt, isWasm);
    }

    /**
     * Function: desc Table
     *
     * @param groupId   链的group组Id
     * @param tableName 目标表的名称, e.g.  "t_demo3"
     * @return String
     */
    public String descTable(String groupId, String tableName) {
        try {
            TableCRUDService tableCRUDService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                    keyStoreService.getCredentialsForQuery(groupId));
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
            throw new FrontException(" message: {}, e:" + e.getMessage());
        } catch (ContractException | JsonProcessingException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(tableName);
            throw new FrontException(" message: {}, e:" + e.getMessage());
        }
    }

    /**
     * Function: insert Table
     *
     * @param groupId    链的group组Id
     * @param signUserId 请求的userId,即用户Id
     * @param sqlInsert  插入表的SQL, e.g.  "insert into t_demo3 values (fruit, 1, apple1)"
     * @return boolean
     */
    public Object insertTable(String groupId, String signUserId, String sqlInsert) throws ContractException, JSQLParserException {
        return insertTableHandle(groupId, signUserId, sqlInsert);
    }

    private Object insertTableHandle(String groupId, String signUserId, String sqlInsert) throws JSQLParserException, ContractException {
        //step1 ,get TablePrecompiled
        Table table = new Table();
        String tableName = CRUDParseUtils.parseTableNameFromSql(sqlInsert);
        table.setTableName(tableName);

        Client client = web3ApiService.getWeb3j(groupId);
        CryptoKeyPair credential = keyStoreService.getCredentialsForQuery(groupId);
        Boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
        String tableAddress = getTableContractAddress(isWasm,tableName,client,credential);

        //step2, call the transHandleWithSign
        TableCRUDService tableCRUDService = new TableCRUDService(client, credential);
        EnumNodeVersion.Version supportedVersion =
                EnumNodeVersion.valueOf((int) tableCRUDService.getCurrentVersion()).toVersionObj();
        Map<String, List<String>> descTable;
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
            descTable = tableCRUDService.descWithKeyOrder(tableName);
        } else {
            descTable = tableCRUDService.desc(tableName);
        }

        Entry entry = CRUDParseUtils.parseInsert(sqlInsert, table, descTable);
        TablePrecompiled.Entry entryResult = entry.covertToEntry();

        List<Object> funcParams=new ArrayList<>();
        funcParams.add(entryResult);

        // execute insert method
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignObj(groupId,
                        signUserId, tableAddress, TablePrecompiled.ABI,
                        TablePrecompiled.FUNC_INSERT, funcParams);
        return com.webank.webase.front.precntauth.precompiled.base.PrecompiledUtils
                .handleTransactionReceipt(receipt, isWasm);
    }


    /**
     * Function: selectTable Table By sqlSelect
     *
     * @param groupId   链的group组Id
     * @param sqlSelect 查询表的SQL, e.g. "select * from t_demo3 where name = fruit"
     * @return List<Map < String, String>>
     */
    public List<Map<String, String>> selectTable(String groupId, String sqlSelect) {
        try {
            Table table = new Table();
            List<String> selectColumns = new ArrayList<>();
            String tableName = CRUDParseUtils.parseTableNameFromSql(sqlSelect);
            table.setTableName(tableName);
            TableCRUDService tableCRUDService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                    keyStoreService.getCredentialsForQuery(groupId));
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
            throw new FrontException(" message: {}, e:" + e.getMessage());
        } catch (ContractException | JSQLParserException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sqlSelect);
            throw new FrontException(" message: {}, e:" + e.getMessage());
        }
    }

    /**
     * Function: update Table By sqlUpdate
     *
     * @param groupId    链的group组Id
     * @param signUserId 请求的userId,即用户Id
     * @param sqlUpdate  更新表的SQL, e.g. "update t_demo3 set item_name = orange where name = fruit and item_id = 1"
     * @return boolean
     */
    public Object updateTable(String groupId, String signUserId, String sqlUpdate) throws JSQLParserException, ContractException {
        return updateTableHandle(groupId, signUserId, sqlUpdate);
    }

    private Object updateTableHandle(String groupId, String signUserId, String sqlUpdate) throws JSQLParserException, ContractException {
        //step1 ,get TablePrecompiled
        Table table = new Table();
        String tableName = CRUDParseUtils.parseTableNameFromSql(sqlUpdate);
        table.setTableName(tableName);

        Client client = web3ApiService.getWeb3j(groupId);
        CryptoKeyPair credential = keyStoreService.getCredentialsForQuery(groupId);
        Boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
        String tableAddress = getTableContractAddress(isWasm,tableName,client,credential);

        //step2, call the transHandleWithSign
        TableCRUDService tableCRUDService = new TableCRUDService(client, credential);
        EnumNodeVersion.Version supportedVersion =
                EnumNodeVersion.valueOf((int) tableCRUDService.getCurrentVersion()).toVersionObj();
        Map<String, List<String>> descTable;
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
            descTable = tableCRUDService.descWithKeyOrder(tableName);
        } else {
            descTable = tableCRUDService.desc(tableName);
        }

        UpdateFields updateFields = new UpdateFields();
        ConditionV320 conditionV320 = new ConditionV320();
        String keyName = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
        if (updateFields.getFieldNameToValue().containsKey(keyName)) {
            System.out.println("Please don't set the key field \"" + keyName + "\".");
            return false;
        }
        table.setKeyFieldName(keyName);
        table.setValueFields(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));
        CRUDParseUtils.parseUpdate(sqlUpdate, table, conditionV320, updateFields);

        List<Object> funcParams=new ArrayList<>();
        //Update也有两种传参，这里采用第二种，即判断条件最完善的一种
        // Update传参方法一，只传入key值进行进行目标匹配
//        funcParams.add(conditionV320.getEqValue());
//        funcParams.add(updateFields.convertToUpdateFields());

        //Update传参方法二,用condition条件进行目标匹配
        funcParams.add(conditionV320.getTableConditions());
        funcParams.add(conditionV320.getLimit());
        funcParams.add(updateFields.convertToUpdateFields());

        // execute update method
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignObj(groupId,
                        signUserId, tableAddress, TablePrecompiled.ABI,
                        TablePrecompiled.FUNC_UPDATE,
                        funcParams);
        return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
    }

    /**
     * Function: remove Table By sqlRemove
     *
     * @param groupId    链的group组Id
     * @param signUserId 请求的userId,即用户Id
     * @param sqlRemove  删除表的SQL, e.g. "delete from t_demo3 where name = fruit and item_id = 1"
     * @return boolean
     */
    public Object removeTable(String groupId, String signUserId, String sqlRemove) throws ContractException, JSQLParserException {
        return removeTableHandle(groupId, signUserId, sqlRemove);
    }

    private Object removeTableHandle(String groupId, String signUserId, String sqlRemove) throws JSQLParserException, ContractException {
        //step1 ,get TablePrecompiled
        Table table = new Table();
        String tableName = CRUDParseUtils.parseTableNameFromSql(sqlRemove);
        table.setTableName(tableName);

        Client client = web3ApiService.getWeb3j(groupId);
        CryptoKeyPair credential = keyStoreService.getCredentialsForQuery(groupId);
        Boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
        String tableAddress = getTableContractAddress(isWasm,tableName,client,credential);

        //step2, call the transHandleWithSign
        TableCRUDService tableCRUDService = new TableCRUDService(client, credential);
        Map<String, List<String>> descTable = tableCRUDService.desc(tableName);
        if (checkTableNotExistence(descTable)) {
            System.out.println("The table \"" + table.getTableName() + "\" doesn't exist!");
            return false;
        }
        table.setKeyFieldName(descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0));
        table.setValueFields(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));

        ConditionV320 conditionV320 = new ConditionV320();
        CRUDParseUtils.parseRemove(sqlRemove, table, conditionV320);

        List<Object> funcParams=new ArrayList<>();
        //Remove也有两种传参，这里采用第二种，即判断条件最完善的一种
        // Remove传参方法一，只传入key值进行进行目标匹配
//        funcParams.add(conditionV320.getEqValue());

        //Remove传参方法二，用condition条件进行目标匹配
        funcParams.add(conditionV320.getTableConditions());
        funcParams.add(conditionV320.getLimit());

        // execute remove method
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignObj(groupId,
                        signUserId, tableAddress, TablePrecompiled.ABI,
                        TablePrecompiled.FUNC_REMOVE,
                        funcParams);
        return com.webank.webase.front.precntauth.precompiled.base.PrecompiledUtils
                .handleTransactionReceipt(receipt, isWasm);
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

    //查找并获取目标表的合约地址
    private String getTableContractAddress(Boolean isWasm, String tableName,Client client,
                                           CryptoKeyPair credential) throws ContractException {
        if(isWasm){
            return  getTableName(tableName);
        }else{
            TableManagerPrecompiled tableManagerPrecompiled = TableManagerPrecompiled.load(
                    isWasm
                            ? PrecompiledAddress.TABLE_MANAGER_PRECOMPILED_NAME
                            : PrecompiledAddress.TABLE_MANAGER_PRECOMPILED_ADDRESS,
                    client, credential
            );
            return tableManagerPrecompiled.openTable(tableName);
        }
    }

    private String getTableName(String tableName) {
        if (tableName.length() > TABLE_PREFIX.length() && tableName.startsWith(TABLE_PREFIX)) {
            return tableName;
        }
        return TABLE_PREFIX + (tableName.startsWith("/") ? tableName.substring(1) : tableName);
    }

}
