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
package com.webank.webase.front.precntauth.authmanager.everyone;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.precntauth.authmanager.everyone.entity.NewProposalInfo;
import com.webank.webase.front.precntauth.authmanager.everyone.entity.ReqProposalListInfo;
import com.webank.webase.front.precntauth.authmanager.util.AuthManagerService;
import com.webank.webase.front.web3api.Web3ApiService;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.contract.auth.manager.AuthManager;
import org.fisco.bcos.sdk.v3.contract.auth.po.ProposalInfo;
import org.fisco.bcos.sdk.v3.contract.auth.po.ProposalStatus;
import org.fisco.bcos.sdk.v3.contract.auth.po.ProposalType;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The service can be requested by everyone
 */
@Slf4j
@Service
public class EveryoneService {

    @Autowired
    private AuthManagerService authManagerService;
    @Autowired
    private Web3ApiService web3ApiService;

    /**
     * 查询治理委员会的详细信息
     */
    public List<Object> queryCommitteeInfo(String groupId)
        throws ContractException {
        List<Object> objectList = new LinkedList<>();
        AuthManager authManager = authManagerService.getAuthManagerService(groupId);
        objectList.add(authManager.getCommitteeInfo());
        return objectList;
    }

    /**
     * 获取某个特定的提案信息
     */
    public List<Object> queryProposalInfo(String groupId, BigInteger proposalId)
        throws ContractException {
        List<Object> objectList = new LinkedList();
        AuthManager authManager = authManagerService.getAuthManagerService(groupId);
        objectList.add(authManager.getProposalInfo(proposalId));
        return objectList;
    }

    /**
     * 获取对应提案信息
     */
    public List<Object> queryProposalInfoList(ReqProposalListInfo reqProposalListInfo)
        throws ContractException {
        AuthManager authManager = authManagerService.getAuthManagerService(
            reqProposalListInfo.getGroupId());
        BigInteger proposalCount = authManager.proposalCount();
        int startIndex = proposalCount.intValue()
            - (reqProposalListInfo.getPageNum() - 1) * reqProposalListInfo.getPageSize();
        int endIndex;
        if (startIndex - reqProposalListInfo.getPageSize() > 0) {
            endIndex = startIndex - reqProposalListInfo.getPageSize() + 1;
        } else {
            endIndex = 1;
        }
        return this.handleProposalReturnData(authManager, startIndex, endIndex);
    }

    public List<Object> handleProposalReturnData(AuthManager authManager, int startIndex,
        int endIndex)
        throws ContractException {
        List<Object> objectList = new LinkedList();
        for (int i = startIndex; i >= endIndex; i--) {
            ProposalInfo proposalInfo = authManager.getProposalInfo(BigInteger.valueOf(i));
            NewProposalInfo info = new NewProposalInfo();
            info.setResourceId(proposalInfo.getResourceId());
            info.setProposer(proposalInfo.getProposer());
            info.setProposalType(ProposalType.fromInt(proposalInfo.getProposalType()).getValue());
            info.setBlockNumberInterval(proposalInfo.getBlockNumberInterval().longValue());
            info.setStatus(ProposalStatus.fromInt(proposalInfo.getStatus()).getValue());
            info.setAgreeVoters(proposalInfo.getAgreeVoters());
            info.setAgainstVoters(proposalInfo.getAgainstVoters());
            info.setProposalId(i);
            objectList.add(info);
        }
        return objectList;
    }

    /**
     * 获取提案总数
     */
    public BigInteger queryProposalInfoCount(String groupId)
        throws ContractException {
        AuthManager authManager = authManagerService.getAuthManagerService(groupId);
        BigInteger proposalCount = authManager.proposalCount();
        return proposalCount;
    }

    /**
     * 获取当前全局部署的权限策略,策略类型：0则无策略，1则为白名单模式，2则为黑名单模式
     */
    public BigInteger queryDeployAuthType(String groupId)
        throws ContractException {
        AuthManager authManager = authManagerService.getAuthManagerService(groupId);
        return authManager.getDeployAuthType();
    }

    /**
     * 检查账号是否具有全局部署权限
     */
    public boolean checkDeployAuth(String groupId, String userAddress)
        throws ContractException {
        AuthManager authManager = authManagerService.getAuthManagerService(groupId);
        try {
            return authManager.checkDeployAuth(userAddress);
        } catch (ContractException ex) {
            log.error("checkDeployAuth error:{}", ex.getMessage());
        }
        return true;
    }

    /**
     * 检查某个账号是否有某个合约的某接口的调用权限
     */
    public Boolean checkMethodAuth(String groupId, String contractAddr, String func,
        String userAddress)
        throws ContractException {
        AuthManager authManager = authManagerService.getAuthManagerService(groupId);
        byte[] hash = web3ApiService.getCryptoSuite(groupId)
            .hash(func.getBytes());
        byte[] newFunc = Arrays.copyOfRange(hash, 0, 4);
        return authManager.checkMethodAuth(contractAddr, newFunc, userAddress);
    }

    /**
     * 获取特定合约的管理员地址
     */
    public String queryAdmin(String groupId, String contractAddr)
        throws ContractException {
        AuthManager authManager = authManagerService.getAuthManagerService(groupId);
        return authManager.getAdmin(contractAddr);
    }

    /**
     * 获取合约是否被冻结, true: available, 即not frozen
     */
    public Boolean isContractAvailable(String groupId, String contractAddr)
        throws ContractException {
        if (!web3ApiService.getWeb3j(groupId).isAuthCheck()) {
            throw new FrontException(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
        }
        AuthManager authManager = authManagerService.getAuthManagerService(groupId);
        return authManager.contractAvailable(contractAddr);
    }

    /**
     * 获取合约是否被冻结, true: available, 即not frozen
     */
    public Map<String, Boolean> listContractStatus(String groupId, List<String> contractAddrList)
        throws ContractException {
        if (!web3ApiService.getWeb3j(groupId).isAuthCheck()) {
            throw new FrontException(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        if (contractAddrList.isEmpty()) {
            return resultMap;
        }
        AuthManager authManager = authManagerService.getAuthManagerService(groupId);

        for (String contractAddr : contractAddrList) {
            Boolean available = authManager.contractAvailable(contractAddr);
            resultMap.put(contractAddr, available);
        }
        return resultMap;
    }
}
