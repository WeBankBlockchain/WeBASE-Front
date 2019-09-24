package com.webank.webase.front.precompiledapi.permission;

import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreInfo;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.util.PrecompiledUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
public class PermissionManageService {
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
        Credentials credentialsPEM = GenCredential.create(pemManager.getECKeyPair().getPrivateKey().toString(16));
        return credentialsPEM;
    }

    /**
     * 获取所有权限的list
     * 仅包含cns, node, sysConfig, deployAndCreate
     * response's data structure: { (address, {(cns, 1), (sysConfig, 0)}) }
     */
    public Map<String, PermissionState> getAllPermissionStateList(int groupId) throws Exception {
        Map<String, PermissionState> resultMap = getPermissionStateList(groupId);
        return resultMap;
    }


    public Map<String, PermissionState> getPermissionStateList(int groupId) throws Exception {
        // key is address, value is map of its permission ex: { (address, {(cns, 1), (sysConfig, 0)}) }
        Map<String, PermissionState> resultMap = new HashMap<String, PermissionState>();

        // 获取各个权限list of address  1300ms 运行时间 时而250ms
        List<PermissionInfo> deployAndCreateMgrList = listDeployAndCreateManager(groupId);
        List<PermissionInfo> nodeMgrList = listNodeManager(groupId);
        List<PermissionInfo> sysConfigMgrList = listSysConfigManager(groupId);
        List<PermissionInfo> cnsMgrList = listCNSManager(groupId);
        // 四个遍历， 更新
        for(int i = 0; i < deployAndCreateMgrList.size(); i++) {
            String address = deployAndCreateMgrList.get(i).getAddress();
            PermissionState tempState = getDefaultPermissionState();
            // 如果没这个地址的permissionState， 直接put一个新的Map到resultMap里
            if(!resultMap.containsKey(address)){
                tempState.setDeployAndCreate(1);
                resultMap.put(address, tempState);
            }else{ // 如果有，更新resultMap里(address,value)中原有的value的Map
                tempState = resultMap.get(address);
                tempState.setDeployAndCreate(1);
                resultMap.put(address, tempState);
            }
        }
        for(int i = 0; i < nodeMgrList.size(); i++) {
            String  address = nodeMgrList.get(i).getAddress();
            PermissionState tempState = getDefaultPermissionState();
            if(!resultMap.containsKey(address)){
                tempState.setNode(1);
                resultMap.put(address, tempState);
            }else{
                tempState = resultMap.get(address);
                tempState.setNode(1);
                resultMap.put(address, tempState);
            }
        }
        for(int i = 0; i < sysConfigMgrList.size(); i++) {
            String address = sysConfigMgrList.get(i).getAddress();
            PermissionState tempState = getDefaultPermissionState();
            if(!resultMap.containsKey(address)){
                tempState.setSysConfig(1);
                resultMap.put(address, tempState);
            }else{
                tempState = resultMap.get(address);
                tempState.setSysConfig(1);
                resultMap.put(address, tempState);
            }
        }
        for(int i = 0; i < cnsMgrList.size(); i++) {
            String address = cnsMgrList.get(i).getAddress();
            PermissionState tempState = getDefaultPermissionState();
            if(!resultMap.containsKey(address)){
                tempState.setCns(1);
                resultMap.put(address, tempState);
            }else{
                tempState = resultMap.get(address);
                tempState.setCns(1);
                resultMap.put(address, tempState);
            }
        }
        return resultMap;
    }

    public PermissionState getDefaultPermissionState() {
        // 默认全部权限为0
        PermissionState initState = new PermissionState();
        initState.setDeployAndCreate(0);
        initState.setCns(0);
        initState.setNode(0);
        initState.setSysConfig(0);
        return initState;
    }

    /**
     * 批量grant/revoke权限
     * 两种方案： 如果发送三笔交易的时间远超一秒，才需要先get再发交易
     * 先getList(约1秒），XOR异或(不同的)才需要发交易。
     * 直接发四个交易
     */
    // 方案一 如果全部相同，只需要200ms, 如果全部不同，要2s
    public Object updatePermissionStateAfterCheck(int groupId, String fromAddress, String userAddress, PermissionState permissionState) throws Exception {
        Map<String, Integer> resultList = new HashMap<>();
        Map<String, PermissionState> checkList = getPermissionStateList(groupId);
        int cnsMgrState = permissionState.getCns();
        int deployAndCreateMgrState = permissionState.getDeployAndCreate();
        int nodeMgrState = permissionState.getNode();
        int sysConfigMgrState = permissionState.getSysConfig();
        // checkList包含address拥有的权限，不包含未拥有的权限
        if(checkList.containsKey(userAddress)){ //checkList包含address拥有的权限，不包含未拥有的权限有部分权限
            //不执行的状况： get: 1, deployAndCreateMgrState: 1， get: 0, deployAndCreateMgrState: 0
            if(checkList.get(userAddress).getDeployAndCreate() != deployAndCreateMgrState) {
                deployAndCreateMgrState = deployAndCreateMgrHandle(groupId, fromAddress, userAddress, deployAndCreateMgrState);
            }
            if(checkList.get(userAddress).getCns() != cnsMgrState) {
                cnsMgrState = cnsMgrHandle(groupId, fromAddress, userAddress, cnsMgrState);
            }
            if(checkList.get(userAddress).getNode() != nodeMgrState) {
                nodeMgrState = nodeMgrHandle(groupId, fromAddress, userAddress, nodeMgrState);
            }
            if(checkList.get(userAddress).getSysConfig() != sysConfigMgrState) {
                sysConfigMgrState = sysConfigMgrHandle(groupId, fromAddress, userAddress, sysConfigMgrState);
            }
        }else { // 全部权限都没有
            deployAndCreateMgrState = deployAndCreateMgrHandle(groupId, fromAddress, userAddress, deployAndCreateMgrState);
            cnsMgrState = cnsMgrHandle(groupId, fromAddress, userAddress, cnsMgrState);
            nodeMgrState = nodeMgrHandle(groupId, fromAddress, userAddress, nodeMgrState);
            sysConfigMgrState = sysConfigMgrHandle(groupId, fromAddress, userAddress, sysConfigMgrState);
        }

        if(cnsMgrState == -1 || deployAndCreateMgrState == -1
                || nodeMgrState == -1 || sysConfigMgrState == -1) {
            throw new FrontException("Update permission state fail, please check admin permission or param not null");
        } else {
            resultList.put("deployAndCreate", deployAndCreateMgrState);
            resultList.put("cns", cnsMgrState);
            resultList.put("node", nodeMgrState);
            resultList.put("sysConfig", sysConfigMgrState);
        }
        return resultList;
    }
    // 方案二 all grant: 1.6s  all revoke:2.5  2 grant 2 revoke: 3.8s 1.8s
    public Object updatePermissionState(int groupId, String fromAddress, String userAddress, PermissionState permissionState) throws Exception {
        Map<String, Integer> resultList = new HashMap<>();
        int cnsMgrState = cnsMgrHandle(groupId, fromAddress, userAddress, permissionState.getCns());
        int deployAndCreateMgrState = deployAndCreateMgrHandle(groupId, fromAddress, userAddress, permissionState.getDeployAndCreate());
        int nodeMgrState = nodeMgrHandle(groupId, fromAddress, userAddress, permissionState.getNode());
        int sysConfigMgrState = sysConfigMgrHandle(groupId, fromAddress, userAddress, permissionState.getSysConfig());
        if(cnsMgrState == -1 || deployAndCreateMgrState == -1
                || nodeMgrState == -1 || sysConfigMgrState == -1) {
            return "Update permission state fail, please check admin permission";
        } else {
            resultList.put("cns", cnsMgrState);
            resultList.put("deployAndCreate", deployAndCreateMgrState);
            resultList.put("node", nodeMgrState);
            resultList.put("sysConfig", sysConfigMgrState);
        }
        return resultList;
    }

    // deploy and create handle
    public int deployAndCreateMgrHandle(int groupId, String fromAddress, String userAddress, int deployAndCreateState) throws Exception {
        int resState = -1;
        if(deployAndCreateState == 1) {
            String result = grantDeployAndCreateManager(groupId, fromAddress, userAddress);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == 0 || resCode == -51000) {
                resState = 1;
            }else if(resCode == -50000){  // permission denied
                throw new FrontException("Update permission state fail for permission denied");
            }
        }else if(deployAndCreateState == 0) {
            String result = revokeDeployAndCreateManager(groupId, fromAddress, userAddress);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == 0 || resCode == -51001) {
                resState = 0;
            }else if(resCode == -50000){
                throw new FrontException("Update permission state fail for permission denied");
            }
        }
        return resState;
    }
    // cns handle
    public int cnsMgrHandle(int groupId, String fromAddress, String userAddress, int cnsState) throws Exception {
        int resState = -1;
        if(cnsState == 1) {
            String result = grantCNSManager(groupId, fromAddress, userAddress);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == 0 || resCode == -51000) {
                resState = 1;
            }else if(resCode == -50000){  // permission denied
                throw new FrontException("Update permission state fail for permission denied");
            }
        }else if(cnsState == 0) {
            String result = revokeCNSManager(groupId, fromAddress, userAddress);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == 0 || resCode == -51001) {
                resState = 0;
            }else if(resCode == -50000){
                throw new FrontException("Update permission state fail for permission denied");
            }
        }
        return resState;
    }
    // node handle
    public int nodeMgrHandle(int groupId, String fromAddress, String userAddress, int nodeState) throws Exception {
        int resState = -1;
        if(nodeState == 1) {
            String result = grantNodeManager(groupId, fromAddress, userAddress);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == 0 || resCode == -51000) {
                resState = 1;
            }else if(resCode == -50000){  // permission denied
                throw new FrontException("Update permission state fail for permission denied");
            }
        }else if(nodeState == 0) {
            String result = revokeNodeManager(groupId, fromAddress, userAddress);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == 0 || resCode == -51001) {
                resState = 0;
            }else if(resCode == -50000){
                throw new FrontException("Update permission state fail for permission denied");
            }
        }
        return resState;
    }
    // system config handle
    public int sysConfigMgrHandle(int groupId, String fromAddress, String userAddress, int sysConfigState) throws Exception {
        int resState = -1;
        if(sysConfigState == 1) {
            String result = grantSysConfigManager(groupId, fromAddress, userAddress);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == 0 || resCode == -51000) {
                resState = 1;
            }else if(resCode == -50000){  // permission denied
                throw new FrontException("Update permission state fail for permission denied");
            }
        }else if(sysConfigState == 0) {
            String result = revokeSysConfigManager(groupId, fromAddress, userAddress);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == 0 || resCode == -51001) {
                resState = 0;
            }else if(resCode == -50000){
                throw new FrontException("Update permission state fail for permission denied");
            }
        }
        return resState;
    }

    /**
     * manage chain admin related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantPermissionManager(int groupId, String fromAddress, String userAddress) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));
        return permissionService.grantPermissionManager(userAddress);
    }

    public String  revokePermissionManager(int groupId, String fromAddress, String userAddress) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokePermissionManager(userAddress);
    }

    /**
     * 查询PermissionManager 不需要发起交易
     */
    public List<PermissionInfo> listPermissionManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());

        return permissionService.listPermissionManager();
    }

    /**
     * manage deploy create Contract Manager related
     */
    public String grantDeployAndCreateManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantDeployAndCreateManager(userAddress);
    }

    public String revokeDeployAndCreateManager(int groupId, String fromAddress, String userAddress) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));
        return permissionService.revokeDeployAndCreateManager(userAddress);
    }

    public List<PermissionInfo> listDeployAndCreateManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listDeployAndCreateManager();
    }

    /**
     * manage userTableManager related
     * @param groupId
     * @param tableName
     * @param userAddress
     * @return
     * @throws Exception
     */
    public Object grantUserTableManager(int groupId, String fromAddress, String tableName, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantUserTableManager(tableName, userAddress);

    }

    public Object revokeUserTableManager(int groupId, String fromAddress, String tableName, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokeUserTableManager(tableName, userAddress);
    }

    public List<PermissionInfo> listUserTableManager(int groupId, String tableName) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listUserTableManager(tableName);

    }

    /**
     * manage NodeManager related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantNodeManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantNodeManager(userAddress);
    }

    public String revokeNodeManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokeNodeManager(userAddress);
    }

    public List<PermissionInfo> listNodeManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listNodeManager();
    }

    /**
     * manage system config Manager related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantSysConfigManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantSysConfigManager(userAddress);
    }

    public String revokeSysConfigManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokeSysConfigManager(userAddress);
    }

    public List<PermissionInfo> listSysConfigManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listSysConfigManager();
    }

    /**
     * manage CNS Manager related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantCNSManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantCNSManager(userAddress);
    }

    public String revokeCNSManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokeCNSManager(userAddress);
    }

    public List<PermissionInfo> listCNSManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listCNSManager();
    }



}
