/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.precompiledapi;

import com.webank.webase.front.base.SpringTestBase;
import com.webank.webase.front.precompiledapi.permission.ChainGovernService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ChainGovernServiceTestBase extends SpringTestBase {

    @Autowired
    private ChainGovernService chainGovernService;
    @Autowired
    private PrecompiledService precompiledService;
    private static final Integer groupId = 1;
    private static final String adminAddress = "0xd031e61f6dc4dedd7d77f90128ed33caafbed0af";
    private static final String operatorAddress = "0x304852a7cc6511e62c37b6e189850861e41282b0";
    private static final String userAddress = "0xd41a42cd6e42dbb9740e4b94ab0c9fad251c1bdc";

    private static final String adminSignUserId = "037bda25bbb34067821860e9d743e9f2";
    private static final String operatorSignUserId = "f4e3a4f0699542ebb146bda977e5dbc0";
    private static final String userSignUserId = "78de05d8b7d443e9bab87fcad699edba";

    // test no permission response
    private static final String noneAddress = "0xd031e61f6dc4dedd7d77f90128ed33caafbed0a1";

    @Test
    public void testGrantCommittee() throws Exception {
        System.out.println("grant " + chainGovernService.grantChainCommittee(groupId, adminSignUserId, operatorAddress));
    }

    @Test
    public void testGrantOperator() throws Exception {
        chainGovernService.grantOperator(groupId, adminSignUserId, operatorAddress);
    }

    @Test
    public void testUpdateThreshold() throws Exception {
        chainGovernService.updateThreshold(groupId, adminSignUserId, 49);
    }

    @Test
    public void testUpdateWeight() throws Exception {
        chainGovernService.updateChainCommitteeWeight(groupId, adminSignUserId, adminAddress,12);
    }

    @Test
    public void testFreezeAccount() throws Exception {
        chainGovernService.freezeAccount(groupId, adminSignUserId, userAddress);
    }

    @Test
    public void testList() throws Exception {
        System.out.println("listChainCommittee: " + chainGovernService.listChainCommittee(groupId));
        System.out.println("listOperator: " + chainGovernService.listOperator(groupId));
        System.out.println("queryThreshold: " + chainGovernService.queryThreshold(groupId));
        System.out.println("getAccountStatus: " + chainGovernService.getAccountStatus(groupId, userAddress));
        System.out.println("queryChainCommitteeWeight: " + chainGovernService.queryChainCommitteeWeight(groupId, userAddress));
    }

    @Test
    public void testGetAccountStatus() throws Exception {
        System.out.println("getAccountStatus: " + chainGovernService.getAccountStatus(groupId, adminAddress));
    }


}
