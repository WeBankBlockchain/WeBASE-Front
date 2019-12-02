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

import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precompiledapi.entity.NodeInfo;
import com.webank.webase.front.util.PrecompiledUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.precompile.consensus.ConsensusService;
import org.fisco.bcos.web3j.precompile.crud.CRUDService;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.Table;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Precompiled common service
 * including management of CNS, node consensus status, CRUD
 */
@Slf4j
@Service
public class PrecompiledService{

    @Autowired
    Map<Integer, Web3j> web3jMap;
    @Autowired
    private KeyStoreService keyStoreService;


    // 根据前台传的user address获取私钥
    private Credentials getCredentials(String fromAddress, Boolean useAes) throws Exception {
        return keyStoreService.getCredentials(fromAddress, useAes);
    }

    /**
     * CNS config related
     * TODO registerCns
     */
//    get single cns is not necessary
//    public Object getAddressByContractNameAndVersion(int groupId, String contractNameAndVersion) throws Exception {
//        CnsService cnsService = new CnsService(web3jMap.get(groupId), keyStoreService.getCredentialsForQuery());
//
//        return cnsService.getAddressByContractNameAndVersion(contractNameAndVersion);
//    }

    public List<CnsInfo> queryCnsByName(int groupId, String contractName) throws Exception {
        CnsService cnsService = new CnsService(web3jMap.get(groupId),
                keyStoreService.getCredentialsForQuery());

        return cnsService.queryCnsByName(contractName);
    }
    public List<CnsInfo> queryCnsByNameAndVersion(int groupId, String contractName,
                                                  String version) throws Exception {
        CnsService cnsService = new CnsService(web3jMap.get(groupId),
                keyStoreService.getCredentialsForQuery());

        return cnsService.queryCnsByNameAndVersion(contractName, version);
    }




    /**
     * Consensus config related
     */
    public Object addSealer(int groupId, String fromAddress, String nodeId,
                            Boolean useAes) throws Exception {
        ConsensusService consensusService =
                new ConsensusService(web3jMap.get(groupId), getCredentials(fromAddress, useAes));

        return consensusService.addSealer(nodeId);
    }
    public Object addObserver(int groupId, String fromAddress, String nodeId,
                              Boolean useAes) throws Exception {
        ConsensusService consensusService =
                new ConsensusService(web3jMap.get(groupId), getCredentials(fromAddress, useAes));

        return consensusService.addObserver(nodeId);
    }
    public Object removeNode(int groupId, String fromAddress, String nodeId,
                             Boolean useAes) throws Exception {
        ConsensusService consensusService =
                new ConsensusService(web3jMap.get(groupId), getCredentials(fromAddress, useAes));

        return consensusService.removeNode(nodeId);
    }

    public List<NodeInfo> getNodeList(int groupId) throws Exception {
        // nodeListWithType 组合多个带有类型的nodeid list
        List<String> sealerList = web3jMap.get(groupId).getSealerList().send().getResult();
        List<String> observerList = web3jMap.get(groupId).getObserverList().send().getResult();
        List<String> peerList = web3jMap.get(groupId).getNodeIDList().send().getResult();
        // process nodeList
        List<NodeInfo> nodeListWithType = new ArrayList<>();

        // add all sealer and observer in List
        sealerList.stream().forEach(sealer ->
                nodeListWithType.add(new NodeInfo(sealer, PrecompiledUtils.NODE_TYPE_SEALER)));
        observerList.stream().forEach(observer ->
                nodeListWithType.add(new NodeInfo(observer, PrecompiledUtils.NODE_TYPE_OBSERVER)));
        // peer not in sealer/observer but connected is remove node(游离节点)
        peerList.stream().filter(peer -> !sealerList.contains(peer) && !observerList.contains(peer))
                .forEach(peerToAdd ->
                        nodeListWithType.add(new NodeInfo(peerToAdd, PrecompiledUtils.NODE_TYPE_REMOVE)));

        return nodeListWithType;
    }

    /**
     * CRUD related
     * Table table - validation in controller
     */
    public int createTable(int groupId, String fromAddress, Table table,
                           Boolean useAes) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return crudService.createTable(table);


    }

    public Table desc(int groupId, String tableName) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId),
                keyStoreService.getCredentialsForQuery());

        Table descRes = crudService.desc(tableName);
        return descRes;

    }

    //select
    public List<Map<String, String>> select(int groupId, String fromAddress, Table table,
                                            Condition conditions, Boolean useAes) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        List<Map<String, String>> selectRes = crudService.select(table, conditions);
        return selectRes;
    }

    // insert 校验tableName等操作放在controller
    public int insert(int groupId, String fromAddress, Table table,
                      Entry entry, Boolean useAes) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentials(fromAddress, useAes));

        int insertRes = crudService.insert(table, entry);
        return insertRes;
    }

    // update
    public int update(int groupId, String fromAddress, Table table,
                      Entry entry, Condition conditions, Boolean useAes) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentials(fromAddress, useAes));

        int updateRes = crudService.update(table, entry, conditions);
        return updateRes;
    }

    // remove
    public int remove(int groupId, String fromAddress, Table table,
                      Condition conditions, Boolean useAes) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentials(fromAddress, useAes));

        int removeRes = crudService.remove(table, conditions);
        return removeRes;
    }


}
