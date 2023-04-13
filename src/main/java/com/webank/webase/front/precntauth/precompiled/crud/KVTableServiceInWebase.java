///*
// * Copyright 2014-2020 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License. You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software distributed under the License
// * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// * or implied. See the License for the specific language governing permissions and limitations under
// * the License.
// */
//package com.webank.webase.front.precntauth.precompiled.crud;
//
//import static org.fisco.bcos.sdk.v3.contract.precompiled.crud.KVTablePrecompiled.FUNC_SET;
//import static org.fisco.bcos.sdk.v3.contract.precompiled.crud.TableManagerPrecompiled.FUNC_CREATETABLE;
//
//import com.webank.webase.front.base.enums.PrecompiledTypes;
//import com.webank.webase.front.keystore.KeyStoreService;
//import com.webank.webase.front.precntauth.precompiled.base.PrecompiledCommonInfo;
//import com.webank.webase.front.precntauth.precompiled.base.PrecompiledUtils;
//import com.webank.webase.front.transaction.TransService;
//import com.webank.webase.front.web3api.Web3ApiService;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import lombok.extern.slf4j.Slf4j;
//import org.fisco.bcos.sdk.v3.contract.precompiled.crud.KVTableService;
//import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Entry;
//import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
//import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
///**
// *  kvtable service
// *  Handle transaction through webase-sign.
// */
//@Slf4j
//@Service
//public class KVTableServiceInWebase {
//
//  @Autowired
//  private Web3ApiService web3ApiService;
//  @Autowired
//  private KeyStoreService keyStoreService;
//  @Autowired
//  private TransService transService;
//
//  /***
//   * createTable
//   * @param groupId
//   * @param signUserId
//   * @param tableName
//   * @param keyFieldName
//   * @param valueFields
//   * @return
//   * @throws ContractException
//   */
//  public String createTable(String groupId, String signUserId, String tableName,
//      String keyFieldName,
//      List<String> valueFields)
//      throws ContractException {
//    String res = this.createTableHandle(groupId, signUserId, tableName, keyFieldName, valueFields);
//    return res;
//
//  }
//
//  public String createTableHandle(String groupId, String signUserId, String tableName,
//      String keyFieldName, List<String> valueFields) {
//    List<String> funcParams = new ArrayList<>();
//    funcParams.add(tableName);
//    funcParams.add(keyFieldName);
//    String valueFieldsString = convertValueFieldsToString(valueFields);
//    funcParams.add(valueFieldsString);
//    String contractAddress;
//    boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
//    if (isWasm) {
//      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CRUD_LIQUID);
//    } else {
//      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CRUD);
//    }
//    String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CRUD);
//    TransactionReceipt receipt =
//        (TransactionReceipt) transService.transHandleWithSign(groupId,
//            signUserId, contractAddress, abiStr, FUNC_CREATETABLE, funcParams, isWasm);
//    return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
//  }
//
//  /**
//   * set data
//   *
//   * @param groupId
//   * @param signUserId
//   * @param tableName
//   * @param key
//   * @param fieldNameToValue
//   * @return
//   * @throws ContractException
//   */
//  public Object set(String groupId, String signUserId, String tableName, String key,
//      Map<String, String> fieldNameToValue)
//      throws ContractException {
//    String res = this.setHandle(groupId, signUserId, tableName, key, fieldNameToValue);
//    return res;
//  }
//
//  public String setHandle(String groupId, String signUserId, String tableName, String key,
//      Map<String, String> fieldNameToValue) {
//    List<String> funcParams = new ArrayList<>();
//    funcParams.add(tableName);
//    funcParams.add(key);
//    Entry entry = new Entry(Collections.emptyList(), key, fieldNameToValue); // todo check
//    entry.setFieldNameToValue(fieldNameToValue);
//    //be careful ：entry.getKVPrecompiledEntry()
//    funcParams.add(entry.getKVPrecompiledEntry());
//    String contractAddress;
//    boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
//    if (isWasm) {
//      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CRUD_LIQUID);
//    } else {
//      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CRUD);
//    }
//    String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CRUD);
//    TransactionReceipt receipt =
//        (TransactionReceipt) transService.transHandleWithSign(groupId,
//            signUserId, contractAddress, abiStr, FUNC_SET, funcParams, isWasm);
//    return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
//  }
//
//  /**
//   * read data
//   *
//   * @param groupId
//   * @param tableName
//   * @param key
//   * @return
//   * @throws ContractException
//   */
//  public Map<String, String> get(String groupId, String tableName, String key)
//      throws ContractException {
//    KVTableService kvTableService = new KVTableService(web3ApiService.getWeb3j(groupId),
//        keyStoreService.getCredentialsForQuery(groupId));
//    Map<String, String> res = kvTableService.get(tableName, key);
//    return res;
//  }
//
//  /**
//   * desc info of table
//   *
//   * @param groupId
//   * @param tableName
//   * @return
//   * @throws ContractException
//   */
//  public Map<String, String> desc(String groupId, String tableName) throws ContractException {
//    KVTableService kvTableService = new KVTableService(web3ApiService.getWeb3j(groupId),
//        keyStoreService.getCredentialsForQuery(groupId));
//    Map<String, String> res = kvTableService.desc(tableName);
//    return res;
//  }
//
//}
