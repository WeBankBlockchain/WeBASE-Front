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
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precompiledapi.entity.NodeInfo;
import com.webank.webase.front.web3api.Web3ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.precompile.crud.CRUDService;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.Table;
import org.fisco.bcos.web3j.precompile.csm.ContractStatusService;
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
    public List<CnsInfo> queryCnsByName(int groupId, String contractName) throws Exception {
        CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        return cnsService.queryCnsByName(contractName);
    }

    public List<CnsInfo> queryCnsByNameAndVersion(int groupId, String contractName,
                                                  String version) throws Exception {
        CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        return cnsService.queryCnsByNameAndVersion(contractName, version);
    }

    public String getAddressByContractNameAndVersion(int groupId, String contractName,
                                                  String version) throws Exception {
        CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        String contractNameAndVersion = contractName + Constants.SYMPOL + version;
        return cnsService.getAddressByContractNameAndVersion(contractNameAndVersion);
    }

    /**
     * Consensus config related
     */
    public String addSealer(int groupId, String signUserId, String nodeId) throws Exception {
        String res = precompiledWithSignService.addSealer(groupId, signUserId, nodeId);
        return res;
    }

    public String addObserver(int groupId, String signUserId, String nodeId) throws Exception {
        String res = precompiledWithSignService.addObserver(groupId, signUserId, nodeId);
        return res;
    }

    public String removeNode(int groupId, String signUserId, String nodeId) throws Exception {
        String res = precompiledWithSignService.removeNode(groupId, signUserId, nodeId);
        return res;
    }

    public List<NodeInfo> getNodeList(int groupId) throws Exception {
        // nodeListWithType 组合多个带有类型的nodeid list
        List<String> sealerList =
                web3ApiService.getWeb3j(groupId).getSealerList().send().getResult();
        List<String> observerList =
                web3ApiService.getWeb3j(groupId).getObserverList().send().getResult();
        List<String> peerList = web3ApiService.getWeb3j(groupId).getNodeIDList().send().getResult();
        // process nodeList
        List<NodeInfo> nodeListWithType = new ArrayList<>();

        // add all sealer and observer in List
        sealerList.stream()
                .forEach(sealer -> nodeListWithType.add(new NodeInfo(sealer, NODE_TYPE_SEALER)));
        observerList.stream().forEach(
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
    public int createTable(int groupId, String signUserId, Table table) throws Exception {
        int res = precompiledWithSignService.createTable(groupId, signUserId, table);
        return res;
    }

    /**
     * insert 校验tableName等操作放在controller
     */
    public int insert(int groupId, String signUserId, Table table, Entry entry) throws Exception {
        int res = precompiledWithSignService.insert(groupId, signUserId, table, entry);
        return res;
    }

    /**
     * update
     */
    public int update(int groupId, String signUserId, Table table, Entry entry, Condition condition)
            throws Exception {
        int res = precompiledWithSignService.update(groupId, signUserId, table, entry, condition);
        return res;
    }

    /**
     * remove
     */
    public int remove(int groupId, String signUserId, Table table, Condition condition)
            throws Exception {
        int res = precompiledWithSignService.remove(groupId, signUserId, table, condition);
        return res;
    }

    /**
     * desc
     */
    public Table desc(int groupId, String tableName) throws Exception {
        CRUDService crudService = new CRUDService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        Table descRes = crudService.desc(tableName);
        return descRes;
    }

    /**
     * select
     */
    public List<Map<String, String>> select(int groupId, Table table,
                                            Condition conditions) throws Exception {
        CRUDService crudService = new CRUDService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        List<Map<String, String>> selectRes = crudService.select(table, conditions);
        return selectRes;
    }

    public String contractFreeze(int groupId, String signUserId, String contractAddress)
            throws Exception {
        return precompiledWithSignService.contractFreeze(groupId, signUserId, contractAddress);
    }

    public String contractUnfreeze(int groupId, String signUserId, String contractAddress)
            throws Exception {
        return precompiledWithSignService.contractUnfreeze(groupId, signUserId, contractAddress);
    }

    public String contractGrantManager(int groupId, String signUserId, String contractAddress,
            String grantAddress) throws Exception {
        return precompiledWithSignService.contractGrantManager(groupId, signUserId, contractAddress,
                grantAddress);
    }

    public String contractStatus(int groupId, String contractAddress) throws Exception {
        ContractStatusService contractStatusService = new ContractStatusService(
                web3ApiService.getWeb3j(groupId), keyStoreService.getCredentialsForQuery());
        return contractStatusService.getStatus(contractAddress);
    }

    public String contractManagerList(int groupId, String contractAddress) throws Exception {
        ContractStatusService contractStatusService = new ContractStatusService(
                web3ApiService.getWeb3j(groupId), keyStoreService.getCredentialsForQuery());
        return contractStatusService.listManager(contractAddress);
    }
}
