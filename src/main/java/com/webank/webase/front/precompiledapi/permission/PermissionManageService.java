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
package com.webank.webase.front.precompiledapi.permission;

import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.util.PrecompiledUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Permission manage service
 * grant or revoke administrator and get administrators on chain
 */
@Slf4j
@Service
public class PermissionManageService {
    @Autowired
    Map<Integer, Web3j> web3jMap;
    @Autowired
    private KeyStoreService keyStoreService;

    /**
     * permission state flag(enum)
     */
    //grant/revoke permission fail
    private static final int FLAG_FAIL = -1;
    // permission state is already granted
    private static final int FLAG_GRANTED = 1;
    // permission state is already revoked
    private static final int FLAG_REVOKED = 0;


    // get credentials from user address
    private Credentials getCredentials(String fromAddress, Boolean useAes) throws Exception {
        return keyStoreService.getCredentials(fromAddress, useAes);
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

    /**
     * init PermissionState all 0(revoked)
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
     * 批量grant/revoke权限
     * 先getList(约1秒），XOR异或(权限状态有修改的)才需要发交易。
     * 直接发四个交易
     */
    public Object updatePermissionStateAfterCheck(int groupId, String fromAddress, String userAddress,
                                                  PermissionState permissionState, Boolean useAes) throws Exception {
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
                deployAndCreateMgrState = deployAndCreateMgrHandle(groupId, fromAddress, userAddress,
                        deployAndCreateMgrState, useAes);
            }
            if(checkList.get(userAddress).getCns() != cnsMgrState) {
                cnsMgrState = cnsMgrHandle(groupId, fromAddress, userAddress,
                        cnsMgrState, useAes);
            }
            if(checkList.get(userAddress).getNode() != nodeMgrState) {
                nodeMgrState = nodeMgrHandle(groupId, fromAddress, userAddress,
                        nodeMgrState, useAes);
            }
            if(checkList.get(userAddress).getSysConfig() != sysConfigMgrState) {
                sysConfigMgrState = sysConfigMgrHandle(groupId, fromAddress, userAddress,
                        sysConfigMgrState, useAes);
            }
        }else { // 全部权限都没有
            deployAndCreateMgrState = deployAndCreateMgrHandle(groupId, fromAddress, userAddress,
                    deployAndCreateMgrState, useAes);
            cnsMgrState = cnsMgrHandle(groupId, fromAddress, userAddress,
                    cnsMgrState, useAes);
            nodeMgrState = nodeMgrHandle(groupId, fromAddress, userAddress,
                    nodeMgrState, useAes);
            sysConfigMgrState = sysConfigMgrHandle(groupId, fromAddress, userAddress,
                    sysConfigMgrState, useAes);
        }

        if(cnsMgrState == FLAG_FAIL || deployAndCreateMgrState == FLAG_FAIL
                || nodeMgrState == FLAG_FAIL || sysConfigMgrState == FLAG_FAIL) {
            throw new FrontException("Update permission state fail, please check admin permission or param not null");
        } else {
            resultList.put("deployAndCreate", deployAndCreateMgrState);
            resultList.put("cns", cnsMgrState);
            resultList.put("node", nodeMgrState);
            resultList.put("sysConfig", sysConfigMgrState);
        }
        return resultList;
    }

    // deploy and create handle
    public int deployAndCreateMgrHandle(int groupId, String fromAddress, String userAddress,
                                        int deployAndCreateState, Boolean useAes) throws Exception {
        int resState = FLAG_FAIL;
        if(deployAndCreateState == FLAG_GRANTED) {
            String result = grantDeployAndCreateManager(groupId, fromAddress, userAddress, useAes);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == PrecompiledUtils.PRECOMPILED_SUCCESS ||
                    resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_ALREADY_EXIST) {
                resState = FLAG_GRANTED;
            }else if(resCode == PrecompiledUtils.PERMISSION_DENIED){  // permission denied
                throw new FrontException("Update permission state fail for permission denied");
            }
        }else if(deployAndCreateState == FLAG_REVOKED) {
            String result = revokeDeployAndCreateManager(groupId, fromAddress, userAddress, useAes);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == PrecompiledUtils.PRECOMPILED_SUCCESS ||
                    resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_NOT_EXIST) {
                resState = FLAG_REVOKED;
            }else if(resCode == PrecompiledUtils.PERMISSION_DENIED){
                throw new FrontException("Update permission state fail for permission denied");
            }
        }
        return resState;
    }

    // cns handle
    public int cnsMgrHandle(int groupId, String fromAddress, String userAddress,
                            int cnsState, Boolean useAes) throws Exception {
        int resState = FLAG_FAIL;
        if(cnsState == FLAG_GRANTED) {
            String result = grantCNSManager(groupId, fromAddress, userAddress, useAes);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == PrecompiledUtils.PRECOMPILED_SUCCESS ||
                    resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_ALREADY_EXIST) {
                resState = FLAG_GRANTED;
            }else if(resCode == PrecompiledUtils.PERMISSION_DENIED){  // permission denied
                throw new FrontException("Update permission state fail for permission denied");
            }
        }else if(cnsState == FLAG_REVOKED) {
            String result = revokeCNSManager(groupId, fromAddress, userAddress, useAes);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == PrecompiledUtils.PRECOMPILED_SUCCESS ||
                    resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_NOT_EXIST) {
                resState = FLAG_REVOKED;
            }else if(resCode == PrecompiledUtils.PERMISSION_DENIED){
                throw new FrontException("Update permission state fail for permission denied");
            }
        }
        return resState;
    }

    // node handle
    public int nodeMgrHandle(int groupId, String fromAddress, String userAddress,
                             int nodeState, Boolean useAes) throws Exception {
        int resState = FLAG_FAIL;
        if(nodeState == FLAG_GRANTED) {
            String result = grantNodeManager(groupId, fromAddress, userAddress, useAes);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == PrecompiledUtils.PRECOMPILED_SUCCESS ||
                    resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_ALREADY_EXIST) {
                resState = FLAG_GRANTED;
            }else if(resCode == PrecompiledUtils.PERMISSION_DENIED){  // permission denied
                throw new FrontException("Update permission state fail for permission denied");
            }
        }else if(nodeState == FLAG_REVOKED) {
            String result = revokeNodeManager(groupId, fromAddress, userAddress, useAes);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == PrecompiledUtils.PRECOMPILED_SUCCESS ||
                    resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_NOT_EXIST) {
                resState = FLAG_REVOKED;
            }else if(resCode == PrecompiledUtils.PERMISSION_DENIED){
                throw new FrontException("Update permission state fail for permission denied");
            }
        }
        return resState;
    }

    // system config handle
    public int sysConfigMgrHandle(int groupId, String fromAddress, String userAddress,
                                  int sysConfigState, Boolean useAes) throws Exception {
        int resState = FLAG_FAIL;
        if(sysConfigState == FLAG_GRANTED) {
            String result = grantSysConfigManager(groupId, fromAddress, userAddress, useAes);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == PrecompiledUtils.PRECOMPILED_SUCCESS ||
                    resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_ALREADY_EXIST) {
                resState = FLAG_GRANTED;
            }else if(resCode == PrecompiledUtils.PERMISSION_DENIED){  // permission denied
                throw new FrontException("Update permission state fail for permission denied");
            }
        }else if(sysConfigState == FLAG_REVOKED) {
            String result = revokeSysConfigManager(groupId, fromAddress, userAddress, useAes);
            int resCode = PrecompiledUtils.string2Json(result).get("code").intValue();
            if( resCode == PrecompiledUtils.PRECOMPILED_SUCCESS ||
                    resCode == PrecompiledUtils.TABLE_NAME_AND_ADDRESS_NOT_EXIST) {
                resState = FLAG_REVOKED;
            }else if(resCode == PrecompiledUtils.PERMISSION_DENIED){
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
    public String grantPermissionManager(int groupId, String fromAddress, String userAddress,
                                         Boolean useAes) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));
        return permissionService.grantPermissionManager(userAddress);
    }

    public String  revokePermissionManager(int groupId, String fromAddress, String userAddress,
                                           Boolean useAes) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.revokePermissionManager(userAddress);
    }

    /**
     * 查询PermissionManager 不需要发起交易
     */
    public List<PermissionInfo> listPermissionManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                keyStoreService.getCredentialsForQuery());

        return permissionService.listPermissionManager();
    }

    /**
     * manage deploy create Contract Manager related
     */
    public String grantDeployAndCreateManager(int groupId, String fromAddress, String userAddress,
                                              Boolean useAes) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.grantDeployAndCreateManager(userAddress);
    }

    public String revokeDeployAndCreateManager(int groupId, String fromAddress, String userAddress,
                                               Boolean useAes) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));
        return permissionService.revokeDeployAndCreateManager(userAddress);
    }

    public List<PermissionInfo> listDeployAndCreateManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                keyStoreService.getCredentialsForQuery());
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
    public Object grantUserTableManager(int groupId, String fromAddress, String tableName, String userAddress,
                                        Boolean useAes) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.grantUserTableManager(tableName, userAddress);

    }

    public Object revokeUserTableManager(int groupId, String fromAddress, String tableName, String userAddress,
                                         Boolean useAes) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.revokeUserTableManager(tableName, userAddress);
    }

    public List<PermissionInfo> listUserTableManager(int groupId, String tableName) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                keyStoreService.getCredentialsForQuery());
        return permissionService.listUserTableManager(tableName);

    }

    /**
     * manage NodeManager related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantNodeManager(int groupId, String fromAddress, String userAddress,
                                   Boolean useAes) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.grantNodeManager(userAddress);
    }

    public String revokeNodeManager(int groupId, String fromAddress, String userAddress,
                                    Boolean useAes) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.revokeNodeManager(userAddress);
    }

    public List<PermissionInfo> listNodeManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                keyStoreService.getCredentialsForQuery());
        return permissionService.listNodeManager();
    }

    /**
     * manage system config Manager related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantSysConfigManager(int groupId, String fromAddress, String userAddress,
                                        Boolean useAes) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.grantSysConfigManager(userAddress);
    }

    public String revokeSysConfigManager(int groupId, String fromAddress, String userAddress,
                                         Boolean useAes) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.revokeSysConfigManager(userAddress);
    }

    public List<PermissionInfo> listSysConfigManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                keyStoreService.getCredentialsForQuery());
        return permissionService.listSysConfigManager();
    }

    /**
     * manage CNS Manager related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantCNSManager(int groupId, String fromAddress, String userAddress,
                                  Boolean useAes) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.grantCNSManager(userAddress);
    }

    public String revokeCNSManager(int groupId, String fromAddress, String userAddress,
                                   Boolean useAes) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                getCredentials(fromAddress, useAes));

        return permissionService.revokeCNSManager(userAddress);
    }

    public List<PermissionInfo> listCNSManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId),
                keyStoreService.getCredentialsForQuery());
        return permissionService.listCNSManager();
    }



}
