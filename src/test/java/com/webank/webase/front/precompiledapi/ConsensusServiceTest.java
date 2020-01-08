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
import org.fisco.bcos.web3j.precompile.consensus.ConsensusService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertNotNull;

public class ConsensusServiceTest extends TestBase {

    //String addSealer(String nodeId)： 根据节点NodeID设置对应节点为共识节点。
    //String addObserver(String nodeId)： 根据节点NodeID设置对应节点为观察节点。
    //String removeNode(String nodeId)： 根据节点NodeID设置对应节点为游离节点。

    public static ApplicationContext context = null;
    public static String nodeId;

    @Test
    public void testConsensus() throws Exception {
        nodeId = "224e6ee23e8a02d371298b9aec828f77cc2711da3a981684896715a3711885a3177b3cf7906bf9d1b84e597fad1e0049511139332c04edfe3daddba5ed60cffa";

        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //链管理员私钥加载
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));

        ConsensusService consensusService = new ConsensusService(web3j, credentialsPEM);
        System.out.println(consensusService.addSealer(nodeId));
        assertNotNull(consensusService.addSealer(nodeId));
        System.out.println(consensusService.addObserver(nodeId));
        assertNotNull(consensusService.addObserver(nodeId));
        System.out.println(consensusService.removeNode(nodeId));
        assertNotNull(consensusService.removeNode(nodeId));
    }
}
