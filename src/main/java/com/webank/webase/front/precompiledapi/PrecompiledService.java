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

import com.webank.webase.front.base.Constants;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precompiledapi.precompiledHandle.NodeInfo;
import com.webank.webase.front.util.PrecompiledUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.precompile.consensus.ConsensusService;
import org.fisco.bcos.web3j.precompile.crud.CRUDService;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.Table;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PrecompiledService{

    @Autowired
    Map<Integer, Web3j> web3jMap;
    @Autowired
    private KeyStoreService keyStoreService;


    // 根据前台传的user address获取私钥
    public Credentials getCredentials(String fromAddress) throws Exception {
        return keyStoreService.getCredentials(fromAddress, false);
    }
    public Credentials getCredentialsForQuery() throws Exception {
        PEMManager pemManager = new PEMManager();
        InputStream pemStream = new ClassPathResource(Constants.account1Path).getInputStream();
        pemManager.load(pemStream);
        return GenCredential.create(pemManager.getECKeyPair().getPrivateKey().toString(16));
    }

    /**
     * CNS config related
     * TODO registerCns
     */
//    get single cns is not necessary
//    public Object getAddressByContractNameAndVersion(int groupId, String contractNameAndVersion) throws Exception {
//        CnsService cnsService = new CnsService(web3jMap.get(groupId), getCredentialsForQuery());
//
//        return cnsService.getAddressByContractNameAndVersion(contractNameAndVersion);
//    }

    public List<CnsInfo> queryCnsByName(int groupId, String contractName) throws Exception {
        CnsService cnsService = new CnsService(web3jMap.get(groupId), getCredentialsForQuery());

        return cnsService.queryCnsByName(contractName);
    }
    public List<CnsInfo> queryCnsByNameAndVersion(int groupId, String contractName, String version) throws Exception {
        CnsService cnsService = new CnsService(web3jMap.get(groupId), getCredentialsForQuery());

        return cnsService.queryCnsByNameAndVersion(contractName, version);
    }




    /**
     * Consensus config related
     */
    public Object addSealer(int groupId, String fromAddress, String nodeId) throws Exception {
        ConsensusService consensusService = new ConsensusService(web3jMap.get(groupId), getCredentials(fromAddress));

        return consensusService.addSealer(nodeId);
    }
    public Object addObserver(int groupId, String fromAddress, String nodeId) throws Exception {
        ConsensusService consensusService = new ConsensusService(web3jMap.get(groupId), getCredentials(fromAddress));

        return consensusService.addObserver(nodeId);
    }
    public Object removeNode(int groupId, String fromAddress, String nodeId) throws Exception {
        ConsensusService consensusService = new ConsensusService(web3jMap.get(groupId), getCredentials(fromAddress));

        return consensusService.removeNode(nodeId);
    }

    public List<NodeInfo> getNodeList(int groupId) throws Exception {
        // nodeListWithType 组合多个带有类型的nodeid list
        List<String> sealerList = web3jMap.get(groupId).getSealerList().send().getResult();
        List<String> observerList = web3jMap.get(groupId).getObserverList().send().getResult();
        List<String> nodeList = web3jMap.get(groupId).getNodeIDList().send().getResult();
        // process nodeList
        List<NodeInfo> nodeListWithType = new ArrayList<>();
        for(int i = 0; i < nodeList.size(); i++) {
            // 默认游离节点
            String nodeId = nodeList.get(i);
            NodeInfo nodeInfo = new NodeInfo(nodeId, PrecompiledUtils.NODE_TYPE_REMOVE);

            for(int j= 0; j < sealerList.size(); j++){
                if(sealerList.get(j).equals(nodeId)) {
                    nodeInfo.setNodeType(PrecompiledUtils.NODE_TYPE_SEALER);
                }
            }
            for(int k= 0; k < observerList.size(); k++){
                if(observerList.get(k).equals(nodeId)) {
                    nodeInfo.setNodeType(PrecompiledUtils.NODE_TYPE_OBSERVER);
                }
            }
            nodeListWithType.add(nodeInfo);
        }

        return nodeListWithType;
    }

    /**
     * CRUD related
     * Table table - validation in controller
     */
    public int createTable(int groupId, String fromAddress, Table table) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentials(fromAddress));

        return crudService.createTable(table);


    }

    public Table desc(int groupId, String tableName) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentialsForQuery());

        Table descRes = crudService.desc(tableName);
        return descRes;

    }

    //select
    public List<Map<String, String>> select(int groupId, String fromAddress, Table table, Condition conditions) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentials(fromAddress));

        List<Map<String, String>> selectRes = crudService.select(table, conditions);
        return selectRes;
    }

    // insert 校验tableName等操作放在controller
    public int insert(int groupId, String fromAddress, Table table, Entry entry) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentials(fromAddress));

        int insertRes = crudService.insert(table, entry);
        return insertRes;
    }

    // update
    public int update(int groupId, String fromAddress, Table table, Entry entry, Condition conditions) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentials(fromAddress));

        int updateRes = crudService.update(table, entry, conditions);
        return updateRes;
    }

    // remove
    public int remove(int groupId, String fromAddress, Table table, Condition conditions) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentials(fromAddress));

        int removeRes = crudService.remove(table, conditions);
        return removeRes;
    }


}
