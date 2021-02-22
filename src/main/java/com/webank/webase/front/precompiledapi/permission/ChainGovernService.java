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

import com.webank.webase.front.base.code.RetCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precompiledapi.PrecompiledWithSignService;
import com.webank.webase.front.web3api.Web3ApiService;
import java.math.BigInteger;
import java.util.List;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernanceService;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionInfo;
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
    public String updateChainCommitteeWeight(int groupId, String signUserId, String userAddress, int weight) {
        String res = precompiledWithSignService.updateChainCommitteeWeight(groupId,
            signUserId, userAddress, weight);
        return res;
    }

    public BigInteger queryChainCommitteeWeight(int groupId, String userAddress)
        throws Exception {
        ChainGovernanceService chainGovernanceService = new ChainGovernanceService(web3ApiService.getWeb3j(groupId),
            keyStoreService.getCredentialsForQuery());

        return chainGovernanceService.queryCommitteeMemberWeight(userAddress);
//        Tuple2<Boolean, BigInteger> res = chainGovernanceService.queryCommitteeMemberWeight(userAddress);
//        if (res.getValue1()) {
//            return res.getValue2();
//        } else {
//            throw new FrontException(new RetCode(res.getValue2().intValue(), "address not committee"));
//        }
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
     * get account's freeze status
     * @return code of status
     */
    public String getAccountStatus(int groupId, String userAddress) throws Exception {
        ChainGovernanceService chainGovernanceService = new ChainGovernanceService(web3ApiService.getWeb3j(groupId),
            keyStoreService.getCredentialsForQuery());
        String res = chainGovernanceService.getAccountStatus(userAddress);
        if (res.contains("frozen")) {
            // res: The account has been frozen. You can use this account after unfreezing it.
            return "1";
        } else {
            // res: The address is nonexistent.
            // res: The account is available.
            // res: invalid
            // res: not a account address
            return "0";
        }
    }


}
