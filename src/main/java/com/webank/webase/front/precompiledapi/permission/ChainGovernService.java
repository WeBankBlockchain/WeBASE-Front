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

package com.webank.webase.front.precompiledapi.permission;

import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precompiledapi.PrecompiledWithSignService;
import com.webank.webase.front.precompiledapi.ReqAccountStatus;
import com.webank.webase.front.web3api.Web3ApiService;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.permission.ChainGovernanceService;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * permission manage after FISCO-BCOS v2.5.0
 */
@Service
public class ChainGovernService {
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private PrecompiledWithSignService precompiledWithSignService;

    /**
     * chain governance committee related
     *
     * @throws Exception
     */
    public String grantChainCommittee(int groupId, String signUserId, String userAddress)
        throws Exception {
        String res = precompiledWithSignService.grantChainCommittee(groupId, signUserId, userAddress);
        return res;
    }

    public String revokeChainCommittee(int groupId, String signUserId, String userAddress)
        throws Exception {
        String res = precompiledWithSignService.revokeChainCommittee(groupId, signUserId, userAddress);
        return res;
    }

    /**
     * 查询ChainCommittee 不需要发起交易
     */
    public List<PermissionInfo> listChainCommittee(int groupId) throws Exception {
        ChainGovernanceService chainGovernanceService = new ChainGovernanceService(web3ApiService.getWeb3j(groupId),
            keyStoreService.getCredentialsForQuery());

        return chainGovernanceService.listCommitteeMembers();
    }

    /**
     * account's committee weight
     */
    public String updateChainCommitteeWeight(int groupId, String signUserId, String userAddress, int weight)
        throws Exception {
        String res = precompiledWithSignService.updateChainCommitteeWeight(groupId,
            signUserId, userAddress, weight);
        return res;
    }

    public Tuple2<Boolean, BigInteger> queryChainCommitteeWeight(int groupId, String userAddress)
        throws Exception {
        ChainGovernanceService chainGovernanceService = new ChainGovernanceService(web3ApiService.getWeb3j(groupId),
            keyStoreService.getCredentialsForQuery());

        return chainGovernanceService.queryCommitteeMemberWeight(userAddress);
    }

    /**
     * Threshold related
     */
    public String updateThreshold(int groupId, String signUserId, int threshold)
        throws Exception {
        String res = precompiledWithSignService.updateThreshold(groupId, signUserId, threshold);
        return res;
    }

    public BigInteger queryThreshold(int groupId) throws Exception {
        ChainGovernanceService chainGovernanceService = new ChainGovernanceService(web3ApiService.getWeb3j(groupId),
            keyStoreService.getCredentialsForQuery());

        return chainGovernanceService.queryThreshold();
    }


    public String grantOperator(int groupId, String signUserId, String userAddress)
        throws Exception {
        String res = precompiledWithSignService.grantOperator(groupId, signUserId, userAddress);
        return res;
    }

    public String revokeOperator(int groupId, String signUserId, String userAddress)
        throws Exception {
        String res = precompiledWithSignService.revokeOperator(groupId, signUserId, userAddress);
        return res;
    }

    /**
     * 查询Operator
     */
    public List<PermissionInfo> listOperator(int groupId) throws Exception {
        ChainGovernanceService chainGovernanceService = new ChainGovernanceService(web3ApiService.getWeb3j(groupId),
            keyStoreService.getCredentialsForQuery());

        return chainGovernanceService.listOperators();
    }


    /**
     * freeze account related
     */
    public String freezeAccount(int groupId, String signUserId, String userAddress)
        throws Exception {
        String res = precompiledWithSignService.freezeAccount(groupId, signUserId, userAddress);
        return res;
    }

    public String unfreezeAccount(int groupId, String signUserId, String userAddress)
        throws Exception {
        String res = precompiledWithSignService.unfreezeAccount(groupId, signUserId, userAddress);
        return res;
    }

    /**
     * get account's committee weight
     */
    public String getAccountStatus(int groupId, String userAddress) throws Exception {
        ChainGovernanceService chainGovernanceService = new ChainGovernanceService(web3ApiService.getWeb3j(groupId),
            keyStoreService.getCredentialsForQuery());

        return chainGovernanceService.getAccountStatus(userAddress);
    }

    public Map<String, String> queryAccountStatus(ReqAccountStatus reqAccountStatus) throws Exception {
        Integer groupId = reqAccountStatus.getGroupId();
        ChainGovernanceService chainGovernanceService = new ChainGovernanceService(web3ApiService.getWeb3j(groupId),
            keyStoreService.getCredentialsForQuery());
        Map<String, String> statusResultMap = new HashMap<>();
        for (String userAddress: reqAccountStatus.getAddressList()) {
            String res = chainGovernanceService.getAccountStatus(userAddress);
            statusResultMap.put(userAddress, res);
        }
        return statusResultMap;
    }

}
