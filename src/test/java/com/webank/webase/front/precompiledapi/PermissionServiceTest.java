/*
 * Copyright 2014-2020 the original author or authors.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.webank.webase.front.base.TestBase;
import java.util.List;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionInfo;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionService;
import org.fisco.bcos.sdk.model.RetCode;
import org.junit.Test;


/**
 * 权限 Permission Service
 */

public class PermissionServiceTest extends TestBase {


    // 链管理员
    @Test
    public void testPermissionManager() throws Exception{
        address = "0x832f299af98c215f7ada010a966b12126483cd00";

        PermissionService permissionService = new PermissionService(web3j, cryptoKeyPair);
        RetCode retCode = permissionService.grantPermissionManager(address);
        System.out.println(retCode);
        assertNotNull(retCode);
        System.out.println(permissionService.listPermissionManager().size());
        assertTrue(permissionService.listPermissionManager().size() != 0);
        RetCode revodeCode = permissionService.revokePermissionManager(address);
        System.out.println(revodeCode);
        assertNotNull(revodeCode);
    }

    // 系统管理员 部署合约
    @Test
    public void testDeployAndCreateManager() throws Exception{

        // 链管理员账户签名的PermissionService
        PermissionService permissionService = new PermissionService(web3j, cryptoKeyPair);

        // TablePermission
        System.out.println(permissionService.grantDeployAndCreateManager(address));
        System.out.println(permissionService.grantDeployAndCreateManager(address));

        assertNotNull(permissionService.grantDeployAndCreateManager(address));
        System.out.println(permissionService.listDeployAndCreateManager().get(0).getAddress());
        assertTrue(permissionService.listDeployAndCreateManager().size() != 0);
        System.out.println(permissionService.revokeDeployAndCreateManager(address));
        assertNotNull(permissionService.revokeDeployAndCreateManager(address));
    }

    // 写用户表
    @Test
    public void testUserTableManager() throws Exception{
        String tableName = "t_test";
        PermissionService permissionService = new PermissionService(web3j, cryptoKeyPair);

        // TablePermission
        System.out.println(permissionService.grantWrite(tableName, address));
        assertNotNull(permissionService.grantWrite(tableName, address));
        List<PermissionInfo> list = permissionService.queryPermissionByTableName(tableName);
        System.out.println(list);
        assertTrue(permissionService.queryPermissionByTableName(tableName).size() != 0);
        System.out.println(permissionService.revokeWrite(tableName, address));
        assertNotNull(permissionService.revokeWrite(tableName, address));
    }

    //节点管理权限
    @Test
    public void testNodeManger() throws Exception {
        PermissionService permissionService = new PermissionService(web3j, cryptoKeyPair);

        // TablePermission
        System.out.println(permissionService.grantNodeManager(address));
        assertNotNull(permissionService.grantNodeManager(address));
        System.out.println(permissionService.listNodeManager().get(0).getAddress());
        assertTrue(permissionService.listNodeManager().size() != 0);
        System.out.println(permissionService.revokeNodeManager(address));
        assertNotNull(permissionService.revokeNodeManager(address));
    }

    // CNS权限
    @Test
    public void testCNSManager() throws Exception {
        PermissionService permissionService = new PermissionService(web3j, cryptoKeyPair);

        // TablePermission
        System.out.println(permissionService.grantCNSManager(address));
        assertNotNull(permissionService.grantCNSManager(address));
        System.out.println(permissionService.listCNSManager().get(0).getAddress());
        assertTrue(permissionService.listCNSManager().size() != 0);
        System.out.println(permissionService.revokeCNSManager(address));
        assertNotNull(permissionService.revokeCNSManager(address));
    }

    // 系统配置权限
    @Test
    public void testConfigManager() throws Exception {
        PermissionService permissionService = new PermissionService(web3j, cryptoKeyPair);

        // TablePermission
        System.out.println(permissionService.grantSysConfigManager(address));
        assertNotNull(permissionService.grantSysConfigManager(address));
        System.out.println(permissionService.listSysConfigManager().get(0).getAddress());
        assertTrue(permissionService.listSysConfigManager().size() != 0);
        System.out.println(permissionService.revokeSysConfigManager(address));
        assertNotNull(permissionService.revokeSysConfigManager(address));
    }


}
