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
package com.webank.webase.front.precompiledapi.permission;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precompiledapi.PrecompiledWithSignService;
import com.webank.webase.front.precompiledapi.entity.PermissionState;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionInfo;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionService;
import org.fisco.bcos.sdk.model.PrecompiledConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Permission manage service grant or revoke administrator and get administrators on chain based on
 * PrecompiledWithSignService
 */
@Slf4j
@Service
public class PermissionManageService {
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private PrecompiledWithSignService precompiledWithSignService;
    /**
     * permission state flag(enum)
     */
    // grant/revoke permission fail
    private static final int FLAG_FAIL = -1;
    // permission state is already granted
    private static final int FLAG_GRANTED = 1;
    // permission state is already revoked
    private static final int FLAG_REVOKED = 0;


    /**
     * 获取所有权限的list 仅包含cns, node, sysConfig, deployAndCreate response's data structure: { (address,
     * {(cns, 1), (sysConfig, 0)}) }
     */
    public Map<String, PermissionState> getAllPermissionStateList(int groupId) {
        Map<String, PermissionState> resultMap = getPermissionStateList(groupId);
        return resultMap;
    }


    public Map<String, PermissionState> getPermissionStateList(int groupId) {
        // key is address, value is map of its permission ex: { (address, {(cns, 1), (sysConfig,
        // 0)}) }
        Map<String, PermissionState> resultMap = new HashMap<String, PermissionState>();

        // 获取各个权限list of address 1300ms 运行时间 时而250ms
        List<PermissionInfo> deployAndCreateMgrList = listDeployAndCreateManager(groupId);
        List<PermissionInfo> nodeMgrList = listNodeManager(groupId);
        List<PermissionInfo> sysConfigMgrList = listSysConfigManager(groupId);
        List<PermissionInfo> cnsMgrList = listCNSManager(groupId);
        // 四个遍历， 更新
        for (int i = 0; i < deployAndCreateMgrList.size(); i++) {
            String address = deployAndCreateMgrList.get(i).getAddress();
            PermissionState tempState = getDefaultPermissionState();
            // 如果没这个地址的permissionState， 直接put一个新的Map到resultMap里
            if (!resultMap.containsKey(address)) {
                tempState.setDeployAndCreate(1);
                resultMap.put(address, tempState);
            } else { // 如果有，更新resultMap里(address,value)中原有的value的Map
                tempState = resultMap.get(address);
                tempState.setDeployAndCreate(1);
                resultMap.put(address, tempState);
            }
        }
        for (int i = 0; i < nodeMgrList.size(); i++) {
            String address = nodeMgrList.get(i).getAddress();
            PermissionState tempState = getDefaultPermissionState();
            if (!resultMap.containsKey(address)) {
                tempState.setNode(1);
                resultMap.put(address, tempState);
            } else {
                tempState = resultMap.get(address);
                tempState.setNode(1);
                resultMap.put(address, tempState);
            }
        }
        for (int i = 0; i < sysConfigMgrList.size(); i++) {
            String address = sysConfigMgrList.get(i).getAddress();
            PermissionState tempState = getDefaultPermissionState();
            if (!resultMap.containsKey(address)) {
                tempState.setSysConfig(1);
                resultMap.put(address, tempState);
            } else {
                tempState = resultMap.get(address);
                tempState.setSysConfig(1);
                resultMap.put(address, tempState);
            }
        }
        for (int i = 0; i < cnsMgrList.size(); i++) {
            String address = cnsMgrList.get(i).getAddress();
            PermissionState tempState = getDefaultPermissionState();
            if (!resultMap.containsKey(address)) {
                tempState.setCns(1);
                resultMap.put(address, tempState);
            } else {
                tempState = resultMap.get(address);
                tempState.setCns(1);
                resultMap.put(address, tempState);
            }
        }
        return resultMap;
    }

    /**
     * init PermissionState all 0(revoked)
     * 
     * @return
     */
    public PermissionState getDefaultPermissionState() {
        PermissionState initState = new PermissionState();
        initState.setDeployAndCreate(FLAG_REVOKED);
        initState.setCns(FLAG_REVOKED);
        initState.setNode(FLAG_REVOKED);
        initState.setSysConfig(FLAG_REVOKED);
        return initState;
    }

    /**
     * 批量grant/revoke权限 先getList(约1秒），XOR异或(权限状态有修改的)才需要发交易。 直接发四个交易
     */
    public Map<String, Integer> updatePermissionStateAfterCheck(int groupId, String signUserId,
            String userAddress, PermissionState permissionState) {
        Map<String, Integer> resultList = new HashMap<>();
        // get param's state
        int cnsMgrState = permissionState.getCns();
        int deployAndCreateMgrState = permissionState.getDeployAndCreate();
        int nodeMgrState = permissionState.getNode();
        int sysConfigMgrState = permissionState.getSysConfig();
        // <address, state> map
        Map<String, PermissionState> checkList = getPermissionStateList(groupId);
        // check if user address have no permission
        PermissionState userState = checkList.get(userAddress);
        // checkList包含address拥有的权限，不包含未拥有的权限
        // 不执行的状况： get: 1, deployAndCreateMgrState: 1， get: 0, deployAndCreateMgrState: 0
        if (userState != null) {
            // if not null, only if state is different, send tx
            if (userState.getDeployAndCreate() != deployAndCreateMgrState) {
                deployAndCreateMgrState = deployAndCreateMgrHandle(groupId, signUserId, userAddress,
                    deployAndCreateMgrState);
            }
            if (userState.getCns() != cnsMgrState) {
                cnsMgrState = cnsMgrHandle(groupId, signUserId, userAddress, cnsMgrState);
            }
            if (userState.getNode() != nodeMgrState) {
                nodeMgrState = nodeMgrHandle(groupId, signUserId, userAddress, nodeMgrState);
            }
            if (userState.getSysConfig() != sysConfigMgrState) {
                sysConfigMgrState =
                    sysConfigMgrHandle(groupId, signUserId, userAddress, sysConfigMgrState);
            }
        } else {
            // if state is null, only if target state is 1, send tx
            if (deployAndCreateMgrState == FLAG_GRANTED) {
                deployAndCreateMgrState = deployAndCreateMgrHandle(groupId, signUserId, userAddress,
                    deployAndCreateMgrState);
            }
            if (cnsMgrState == FLAG_GRANTED) {
                cnsMgrState = cnsMgrHandle(groupId, signUserId, userAddress, cnsMgrState);
            }
            if (nodeMgrState == FLAG_GRANTED) {
                nodeMgrState = nodeMgrHandle(groupId, signUserId, userAddress, nodeMgrState);
            }
            if (sysConfigMgrState == FLAG_GRANTED) {
                sysConfigMgrState =
                    sysConfigMgrHandle(groupId, signUserId, userAddress, sysConfigMgrState);
            }
        }

        if (cnsMgrState == FLAG_FAIL || deployAndCreateMgrState == FLAG_FAIL
                || nodeMgrState == FLAG_FAIL || sysConfigMgrState == FLAG_FAIL) {
            log.error("updatePermissionStateAfterCheck,"
                    + " cnsMgrState{},deployAndCreateMgrState:{},nodeMgrState:{},sysConfigMgrState:{}",
                cnsMgrState, deployAndCreateMgrState, nodeMgrState, sysConfigMgrState);
            throw new FrontException(
                    "Update permission state fail, please check admin permission or param not null");
        } else {
            resultList.put("deployAndCreate", deployAndCreateMgrState);
            resultList.put("cns", cnsMgrState);
            resultList.put("node", nodeMgrState);
            resultList.put("sysConfig", sysConfigMgrState);
        }
        return resultList;
    }

    // deploy and create handle
    public int deployAndCreateMgrHandle(int groupId, String signUserId, String userAddress,
            int deployAndCreateState) {
        int resState = FLAG_FAIL;
        if (deployAndCreateState == FLAG_GRANTED) {
            String result = grantDeployAndCreateManager(groupId, signUserId, userAddress);
            int resCode = JsonUtils.stringToJsonNode(result).get("code").intValue();
            if (resCode == PrecompiledUtils.PRECOMPILED_SUCCESS
                    || resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_ALREADY_EXIST) {
                resState = FLAG_GRANTED;
            }
        } else if (deployAndCreateState == FLAG_REVOKED) {
            String result = revokeDeployAndCreateManager(groupId, signUserId, userAddress);
            int resCode = JsonUtils.stringToJsonNode(result).get("code").intValue();
            if (resCode == PrecompiledUtils.PRECOMPILED_SUCCESS
                    || resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_NOT_EXIST) {
                resState = FLAG_REVOKED;
            }
        }
        return resState;
    }

    // cns handle
    public int cnsMgrHandle(int groupId, String signUserId, String userAddress, int cnsState)
            {
        int resState = FLAG_FAIL;
        if (cnsState == FLAG_GRANTED) {
            String result = grantCNSManager(groupId, signUserId, userAddress);
            int resCode = JsonUtils.stringToJsonNode(result).get("code").intValue();
            if (resCode == PrecompiledUtils.PRECOMPILED_SUCCESS
                    || resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_ALREADY_EXIST) {
                resState = FLAG_GRANTED;
            }
        } else if (cnsState == FLAG_REVOKED) {
            String result = revokeCNSManager(groupId, signUserId, userAddress);
            int resCode = JsonUtils.stringToJsonNode(result).get("code").intValue();
            if (resCode == PrecompiledUtils.PRECOMPILED_SUCCESS
                    || resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_NOT_EXIST) {
                resState = FLAG_REVOKED;
            }
        }
        return resState;
    }

    // node handle
    public int nodeMgrHandle(int groupId, String signUserId, String userAddress, int nodeState)
            {
        int resState = FLAG_FAIL;
        if (nodeState == FLAG_GRANTED) {
            String result = grantNodeManager(groupId, signUserId, userAddress);
            int resCode = JsonUtils.stringToJsonNode(result).get("code").intValue();
            if (resCode == PrecompiledUtils.PRECOMPILED_SUCCESS
                    || resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_ALREADY_EXIST) {
                resState = FLAG_GRANTED;
            }
        } else if (nodeState == FLAG_REVOKED) {
            String result = revokeNodeManager(groupId, signUserId, userAddress);
            int resCode = JsonUtils.stringToJsonNode(result).get("code").intValue();
            if (resCode == PrecompiledUtils.PRECOMPILED_SUCCESS
                    || resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_NOT_EXIST) {
                resState = FLAG_REVOKED;
            }
        }
        return resState;
    }

    // system config handle
    public int sysConfigMgrHandle(int groupId, String signUserId, String userAddress,
            int sysConfigState) {
        int resState = FLAG_FAIL;
        if (sysConfigState == FLAG_GRANTED) {
            String result = grantSysConfigManager(groupId, signUserId, userAddress);
            int resCode = JsonUtils.stringToJsonNode(result).get("code").intValue();
            if (resCode == PrecompiledUtils.PRECOMPILED_SUCCESS
                    || resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_ALREADY_EXIST) {
                resState = FLAG_GRANTED;
            }
        } else if (sysConfigState == FLAG_REVOKED) {
            String result = revokeSysConfigManager(groupId, signUserId, userAddress);
            int resCode = JsonUtils.stringToJsonNode(result).get("code").intValue();
            if (resCode == PrecompiledUtils.PRECOMPILED_SUCCESS
                    || resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_NOT_EXIST) {
                resState = FLAG_REVOKED;
            }
        }
        return resState;
    }

    /**
     * manage chain admin related
     * 
     * @throws Exception
     */
    public String grantPermissionManager(int groupId, String signUserId, String userAddress)
            {
        String res = precompiledWithSignService.grant(groupId, signUserId,
            PrecompiledConstant.SYS_TABLE_ACCESS, userAddress);
        return res;
    }

    public String revokePermissionManager(int groupId, String signUserId, String userAddress)
            {
        String res = precompiledWithSignService.revoke(groupId, signUserId,
                PrecompiledConstant.SYS_TABLE_ACCESS, userAddress);
        return res;
    }

    /**
     * 查询PermissionManager 不需要发起交易
     */
    public List<PermissionInfo> listPermissionManager(int groupId) {

        PermissionService permissionService = new PermissionService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        try {
            return permissionService.listPermissionManager();
        } catch (Exception e) {
            log.error("listPermissionManager fail:[]", e);
            throw new FrontException(ConstantCode.GET_LIST_MANAGER_FAIL);
        }
    }

    /**
     * manage deploy create Contract Manager related
     */
    public String grantDeployAndCreateManager(int groupId, String signUserId, String userAddress)
            {
        String res = precompiledWithSignService.grant(groupId, signUserId,
                PrecompiledConstant.SYS_TABLE, userAddress);
        return res;
    }

    public String revokeDeployAndCreateManager(int groupId, String signUserId, String userAddress)
            {
        String res = precompiledWithSignService.revoke(groupId, signUserId,
                PrecompiledConstant.SYS_TABLE, userAddress);
        return res;
    }

    public List<PermissionInfo> listDeployAndCreateManager(int groupId) {
        PermissionService permissionService = new PermissionService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        try{
            return permissionService.listDeployAndCreateManager();
        } catch (Exception e) {
            log.error("listDeployAndCreateManager fail:[]", e);
            throw new FrontException(ConstantCode.GET_LIST_MANAGER_FAIL);
        }
    }

    /**
     * manage userTableManager related
     * @throws Exception
     */
    public Object grantUserTableManager(int groupId, String signUserId, String tableName,
            String userAddress) throws Exception {
        // CRUD.desc to check table exists
        TableCRUDService crudService = new TableCRUDService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        crudService.desc(tableName);
        String res = precompiledWithSignService.grantWrite(groupId, signUserId, tableName, userAddress);
        return res;
    }

    public Object revokeUserTableManager(int groupId, String signUserId, String tableName,
            String userAddress) {
        String res = precompiledWithSignService.revokeWrite(groupId, signUserId, tableName, userAddress);
        return res;
    }

    public List<PermissionInfo> listUserTableManager(int groupId, String tableName) {

        PermissionService permissionService = new PermissionService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        try {
            // return permissionService.listUserTableManager(tableName);
            return permissionService.queryPermissionByTableName(tableName);
        } catch (Exception e) {
            log.error("listUserTableManager fail:[]", e);
            throw new FrontException(ConstantCode.GET_LIST_MANAGER_FAIL);
        }
    }

    /**
     * manage NodeManager related
     * 
     * @throws Exception
     */
    public String grantNodeManager(int groupId, String signUserId, String userAddress)
            {
        String res = precompiledWithSignService.grant(groupId, signUserId,
                PrecompiledConstant.SYS_CONSENSUS, userAddress);
        return res;
    }

    public String revokeNodeManager(int groupId, String signUserId, String userAddress)
            {
        String res = precompiledWithSignService.revoke(groupId, signUserId,
                PrecompiledConstant.SYS_CONSENSUS, userAddress);
        return res;
    }

    public List<PermissionInfo> listNodeManager(int groupId) {
        PermissionService permissionService = new PermissionService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        try {
            return permissionService.listNodeManager();
        } catch (Exception e) {
            log.error("listNodeManager fail:[]", e);
            throw new FrontException(ConstantCode.GET_LIST_MANAGER_FAIL);
        }
    }

    /**
     * manage system config Manager related
     * 
     * @throws Exception
     */
    public String grantSysConfigManager(int groupId, String signUserId, String userAddress)
            {
        String res = precompiledWithSignService.grant(groupId, signUserId,
                PrecompiledConstant.SYS_CONFIG, userAddress);
        return res;
    }

    public String revokeSysConfigManager(int groupId, String signUserId, String userAddress)
            {
        String res = precompiledWithSignService.revoke(groupId, signUserId,
                PrecompiledConstant.SYS_CONFIG, userAddress);
        return res;
    }

    public List<PermissionInfo> listSysConfigManager(int groupId) {
        PermissionService permissionService = new PermissionService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        try {
            return permissionService.listSysConfigManager();
        } catch (Exception e) {
            log.error("listSysConfigManager fail:[]", e);
            throw new FrontException(ConstantCode.GET_LIST_MANAGER_FAIL);
        }
    }

    /**
     * manage CNS Manager related
     * 
     * @throws Exception
     */
    public String grantCNSManager(int groupId, String signUserId, String userAddress) {
        String res = precompiledWithSignService.grant(groupId, signUserId,
                PrecompiledConstant.SYS_CNS, userAddress);
        return res;
    }

    public String revokeCNSManager(int groupId, String signUserId, String userAddress) {
        String res = precompiledWithSignService.revoke(groupId, signUserId,
                PrecompiledConstant.SYS_CNS, userAddress);
        return res;
    }

    public List<PermissionInfo> listCNSManager(int groupId) {
        PermissionService permissionService = new PermissionService(web3ApiService.getWeb3j(groupId),
                keyStoreService.getCredentialsForQuery());
        try {
            return permissionService.listCNSManager();
        } catch (Exception e) {
            log.error("listCNSManager fail:[]", e);
            throw new FrontException(ConstantCode.GET_LIST_MANAGER_FAIL);
        }
    }



}
