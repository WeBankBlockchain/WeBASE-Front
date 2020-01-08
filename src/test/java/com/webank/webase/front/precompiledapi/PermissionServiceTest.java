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

import com.webank.webase.front.channel.test.TestBase;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * 权限 Permission Service
 */

public class PermissionServiceTest extends TestBase {
    public static ApplicationContext context = null;


    // 链管理员
    @Test
    public void testPermissionManager() throws Exception{
        address = "0x832f299af98c215f7ada010a966b12126483cd00";

        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //生成web3sdk使用的Credentials
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
        PermissionService permissionService = new PermissionService(web3j, credentialsPEM);

        System.out.println(permissionService.grantPermissionManager(address));
        assertNotNull(permissionService.grantPermissionManager(address));
        System.out.println(permissionService.listPermissionManager().get(0).getAddress());
        assertTrue(permissionService.listPermissionManager().size() != 0);
        System.out.println(permissionService.revokePermissionManager(address));
        assertNotNull(permissionService.revokePermissionManager(address));
    }

    // 系统管理员 部署合约
    @Test
    public void testDeployAndCreateManager() throws Exception{
        // 普通账户
        address = "0x832f299af98c215f7ada010a966b12126483cd00";

        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //生成web3sdk使用的Credentials
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
        // 链管理员账户签名的PermissionService
        PermissionService permissionService = new PermissionService(web3j, credentialsPEM);

        // TablePermission
        System.out.println(permissionService.grantDeployAndCreateManager(address));
        System.out.println(permissionService.grantDeployAndCreateManager(address));

        assertNotNull(permissionService.grantDeployAndCreateManager(address));
        System.out.println(permissionService.listDeployAndCreateManager().get(0).getAddress());
        assertTrue(permissionService.listDeployAndCreateManager().size() != 0);
//        System.out.println(permissionService.revokeDeployAndCreateManager(address));
//        assertNotNull(permissionService.revokeDeployAndCreateManager(address));
    }

    // 写用户表
    @Test
    public void testUserTableManager() throws Exception{
        String tableName = "t_test";
        address = "0xd5bba8fe456fce310f529edecef902e4b63129b1";

        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //生成web3sdk使用的Credentials
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
        PermissionService permissionService = new PermissionService(web3j, credentialsPEM);

        // TablePermission
        System.out.println(permissionService.grantUserTableManager(tableName, address));
        assertNotNull(permissionService.grantUserTableManager(tableName, address));
//        List<PermissionInfo> list = permissionService.listUserTableManager(tableName);
//        assertTrue(permissionService.listUserTableManager(tableName).size() != 0);
//        System.out.println(permissionService.revokeUserTableManager(tableName, address));
//        assertNotNull(permissionService.revokeUserTableManager(tableName, address));
    }

    //节点管理权限
    @Test
    public void testNodeManger() throws Exception {
        address = "0x832f299af98c215f7ada010a966b12126483cd00";
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //生成web3sdk使用的Credentials
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
        PermissionService permissionService = new PermissionService(web3j, credentialsPEM);

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
        address = "0x832f299af98c215f7ada010a966b12126483cd00";
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //生成web3sdk使用的Credentials
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
        PermissionService permissionService = new PermissionService(web3j, credentialsPEM);

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
        address = "0x832f299af98c215f7ada010a966b12126483cd00";
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //生成web3sdk使用的Credentials
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
        PermissionService permissionService = new PermissionService(web3j, credentialsPEM);

        // TablePermission
        System.out.println(permissionService.grantSysConfigManager(address));
        assertNotNull(permissionService.grantSysConfigManager(address));
        System.out.println(permissionService.listSysConfigManager().get(0).getAddress());
        assertTrue(permissionService.listSysConfigManager().size() != 0);
        System.out.println(permissionService.revokeSysConfigManager(address));
        assertNotNull(permissionService.revokeSysConfigManager(address));
    }


}
