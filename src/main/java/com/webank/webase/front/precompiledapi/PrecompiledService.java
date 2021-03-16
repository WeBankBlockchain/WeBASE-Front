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

import static com.webank.webase.front.util.PrecompiledUtils.NODE_TYPE_OBSERVER;
import static com.webank.webase.front.util.PrecompiledUtils.NODE_TYPE_REMOVE;
import static com.webank.webase.front.util.PrecompiledUtils.NODE_TYPE_SEALER;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precompiledapi.crud.CRUDParseUtils;
import com.webank.webase.front.precompiledapi.crud.Table;
import com.webank.webase.front.precompiledapi.entity.NodeInfo;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.contract.precompiled.contractmgr.ContractLifeCycleService;
import org.fisco.bcos.sdk.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.model.PrecompiledConstant;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Precompiled common service including management of CNS, node consensus status, CRUD based on
 * PrecompiledWithSignService
 */
@Service
public class PrecompiledService {

    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private PrecompiledWithSignService precompiledWithSignService;

    /**
     * CNS config related
     */
    public List<CnsInfo> queryCnsByName(int groupId, String contractName) throws ContractException {
        CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        return cnsService.selectByName(contractName);
    }

    public List<CnsInfo> queryCnsByNameAndVersion(int groupId, String contractName,
        String version) throws ContractException {
        CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        return cnsService.selectByNameAndVersion(contractName, version);
    }

    public String getAddressByContractNameAndVersion(int groupId, String contractName,
        String version) throws ContractException {
        CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        return cnsService.getContractAddress(contractName, version);
    }

    /**
     * Consensus config related
     */
    public String addSealer(int groupId, String signUserId, String nodeId) {
        String res = precompiledWithSignService.addSealer(groupId, signUserId, nodeId);
        return res;
    }

    public String addObserver(int groupId, String signUserId, String nodeId) {
        String res = precompiledWithSignService.addObserver(groupId, signUserId, nodeId);
        return res;
    }

    public String removeNode(int groupId, String signUserId, String nodeId) {
        String res = precompiledWithSignService.removeNode(groupId, signUserId, nodeId);
        return res;
    }

    public List<NodeInfo> getNodeList(int groupId) throws IOException {
        // nodeListWithType 组合多个带有类型的nodeid list
        List<String> sealerList =
                web3ApiService.getWeb3j(groupId).getSealerList().getSealerList();
        List<String> observerList =
                web3ApiService.getWeb3j(groupId).getObserverList().getObserverList();
        List<String> peerList = web3ApiService.getWeb3j(groupId).getNodeIDList().getNodeIDList();
        // process nodeList
        List<NodeInfo> nodeListWithType = new ArrayList<>();

        // add all sealer and observer in List
        sealerList.forEach(sealer -> nodeListWithType.add(new NodeInfo(sealer, NODE_TYPE_SEALER)));
        observerList.forEach(
                observer -> nodeListWithType.add(new NodeInfo(observer, NODE_TYPE_OBSERVER)));
        // peer not in sealer/observer but connected is remove node(游离节点)
        peerList.stream().filter(peer -> !sealerList.contains(peer) && !observerList.contains(peer))
                .forEach(peerToAdd -> nodeListWithType
                        .add(new NodeInfo(peerToAdd, NODE_TYPE_REMOVE)));

        return nodeListWithType;
    }

    /**
     * CRUD related Table table - validation in controller
     */
    public String createTable(int groupId, String signUserId, Table table) {
        String res = precompiledWithSignService.createTable(groupId, signUserId, table);
        return res;
    }

    /**
     * insert 校验tableName等操作放在controller
     */
    public String insert(int groupId, String signUserId, Table table, Entry entry) {
        String res = precompiledWithSignService.insert(groupId, signUserId, table, entry);
        return res;
    }

    /**
     * update
     */
    public String update(int groupId, String signUserId, Table table, Entry entry, Condition condition)
            {
        String res = precompiledWithSignService.update(groupId, signUserId, table, entry, condition);
        return res;
    }

    /**
     * remove
     */
    public String remove(int groupId, String signUserId, Table table, Condition condition) {
        String res = precompiledWithSignService.remove(groupId, signUserId, table, condition);
        return res;
    }

    /**
     * desc
     */
    public List<Map<String, String>> desc(int groupId, String tableName) throws Exception {
        TableCRUDService crudService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        List<Map<String, String>> descRes = crudService.desc(tableName);
        if (!CRUDParseUtils.checkTableExistence(descRes)) {
            throw new FrontException(ConstantCode.FAIL_TABLE_NOT_EXISTS);
        }
        return descRes;
//        String tableKey = descRes.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
//        String valueFields = descRes.get(0).get(PrecompiledConstant.VALUE_FIELD_NAME);
//        return new Table(tableName, tableKey, valueFields);
    }

    public String descTable(int groupId, String tableName) throws Exception {
        List<Map<String, String>> descRes = this.desc(groupId, tableName);
        if (!CRUDParseUtils.checkTableExistence(descRes)) {
            throw new FrontException(ConstantCode.FAIL_TABLE_NOT_EXISTS);
        }
        String tableInfo = JsonUtils.objToString(descRes);
        return CRUDParseUtils.formatJson(tableInfo);
    }

    /**
     * select
     */
    public List<Map<String, String>> select(int groupId, Table table,
                                            Condition conditions) throws Exception {
        TableCRUDService crudService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        List<Map<String, String>> selectRes = crudService.select(table.getTableName(), table.getKey(), conditions);
        return selectRes;
    }

    public String contractFreeze(int groupId, String signUserId, String contractAddress)
            {
        return precompiledWithSignService.contractFreeze(groupId, signUserId, contractAddress);
    }

    public String contractUnfreeze(int groupId, String signUserId, String contractAddress)
            {
        return precompiledWithSignService.contractUnfreeze(groupId, signUserId, contractAddress);
    }

    public String contractGrantManager(int groupId, String signUserId, String contractAddress,
            String grantAddress) {
        return precompiledWithSignService.contractGrantManager(groupId, signUserId, contractAddress,
                grantAddress);
    }

    public String contractStatus(int groupId, String contractAddress) throws Exception {
        ContractLifeCycleService contractStatusService = new ContractLifeCycleService(
                web3ApiService.getWeb3j(groupId), keyStoreService.getCredentialsForQuery());
        String res = contractStatusService.getContractStatus(contractAddress);
        if (res.contains("frozen")) {
            // res: The account has been frozen. You can use this account after unfreezing it.
            return "1";
        } else {
            // res: The address is nonexistent.
            // res: The account is available.
            // res: invalid
            // res: not a account address
            return "0";
        }
    }

    public List<String> contractManagerList(int groupId, String contractAddress) throws Exception {
        ContractLifeCycleService contractStatusService = new ContractLifeCycleService(
                web3ApiService.getWeb3j(groupId), keyStoreService.getCredentialsForQuery());
        return contractStatusService.listManager(contractAddress);
    }
}
