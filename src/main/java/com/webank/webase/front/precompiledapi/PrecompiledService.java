package com.webank.webase.front.precompiledapi;

import com.webank.webase.front.base.Constants;
import com.webank.webase.front.keystore.KeyStoreService;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
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
//        KeyStoreInfo keyStoreInfo = keyStoreService.createKeyStore(false, KeyTypes.LOCALRANDOM.getValue(), "");
//        return Credentials.create(keyStoreInfo.getPrivateKey());
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
            String nodeId = nodeList.get(i);// 默认游离节点
            NodeInfo nodeInfo = new NodeInfo(nodeId, "remove");

            for(int j= 0; j < sealerList.size(); j++){
                if(sealerList.get(j).equals(nodeId)) {
                    System.out.println("sealer: " + sealerList.get(j));
                    nodeInfo.setNodeType("sealer");
                }
            }
            for(int k= 0; k < observerList.size(); k++){
                if(observerList.get(k).equals(nodeId)) {
                    System.out.println("observer: " + observerList.get(k));
                    nodeInfo.setNodeType("observer");
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
    public String createTable(int groupId, String fromAddress, Table table) throws Exception {
        CRUDService crudService = new CRUDService(web3jMap.get(groupId), getCredentials(fromAddress));

        int result = crudService.createTable(table);
        if (result == 0) {
            return "Create '" + table.getTableName() + "' Ok.";
        } else if (result == PrecompiledCommon.TableExist_RC3) {
            return PrecompiledCommon.transferToJson(PrecompiledCommon.TableExist_RC3);
        } else if (result == PrecompiledCommon.PermissionDenied_RC3) {
            return PrecompiledCommon.transferToJson(PrecompiledCommon.PermissionDenied_RC3);
        } else {
            return "code: " + result + "Create '" + table.getTableName() + "' failed.";
        }

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
