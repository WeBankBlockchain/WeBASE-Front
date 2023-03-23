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
package com.webank.webase.front.precntauth.authmanager.committee;

import static org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager.FUNC_CREATEMODIFYDEPLOYAUTHPROPOSAL;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager.FUNC_CREATERESETADMINPROPOSAL;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager.FUNC_CREATESETDEPLOYAUTHTYPEPROPOSAL;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager.FUNC_CREATESETRATEPROPOSAL;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager.FUNC_CREATEUPDATEGOVERNORPROPOSAL;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager.FUNC_REVOKEPROPOSAL;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager.FUNC_VOTEPROPOSAL;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.precntauth.precompiled.base.PrecompiledCommonInfo;
import com.webank.webase.front.precntauth.precompiled.base.PrecompiledUtils;
import com.webank.webase.front.transaction.TransService;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The service can be requested by committee
 */
@Slf4j
@Service
public class CommitteeService {

  private BigInteger DEFAULT_BLOCK_NUMBER_INTERVAL = BigInteger.valueOf(3600 * 24 * 7);

  @Autowired
  TransService transService;

  /**
   * 更新治理委员信息。 如果是新加治理委员，新增地址和权重即可。如果是删除治理委员，将一个治理委员的权重设置为0 即可
   */
  public Object updateGovernor(String groupId, String signUserId, String accountAddress,
      BigInteger weight) {
    return this.updateGovernorHandle(groupId, signUserId, accountAddress, weight);
  }

  public Object updateGovernorHandle(String groupId, String signUserId, String account,
      BigInteger weight) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(account);
    funcParams.add(weight.toString(10));
    funcParams.add(DEFAULT_BLOCK_NUMBER_INTERVAL.toString(10));
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.COMMITTEE_MANAGER);
    String abiStr = CommitteeManager.getABI();
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_CREATEUPDATEGOVERNORPROPOSAL, funcParams);
    return this.handleRetcodeAndReceipt(receipt, false);
  }

  /**
   * 设置提案阈值，提案阈值分为参与阈值和权重阈值。
   */
  public Object setRate(String groupId, String signUserId, BigInteger participatesRate,
      BigInteger winRate) throws ContractException {
    return this.setRateHandle(groupId, signUserId, participatesRate, winRate);
  }

  public Object setRateHandle(String groupId, String signUserId, BigInteger participatesRate,
      BigInteger winRate) throws ContractException {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(participatesRate.toString(10));
    funcParams.add(winRate.toString(10));
    funcParams.add(DEFAULT_BLOCK_NUMBER_INTERVAL.toString(10));
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.COMMITTEE_MANAGER);
    String abiStr = CommitteeManager.getABI();
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_CREATESETRATEPROPOSAL, funcParams);
    return this.handleRetcodeAndReceipt(receipt, false);
  }

  /**
   * 设置部署的ACL策略 只支持 white_list 和 black_list 两种策略 type为1时，设置为白名单，type为2时，设置为黑名单。
   */
  public Object setDeployAuthType(String groupId, String signUserId, BigInteger deployAuthType) {
    return this.setDeployAuthTypeHandle(groupId, signUserId, deployAuthType);
  }

  public String setDeployAuthTypeHandle(String groupId, String signUserId,
      BigInteger deployAuthType) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(deployAuthType.toString(10));
    funcParams.add(DEFAULT_BLOCK_NUMBER_INTERVAL.toString(10));
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.COMMITTEE_MANAGER);
    String abiStr = CommitteeManager.getABI();
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_CREATESETDEPLOYAUTHTYPEPROPOSAL,
            funcParams);
    return this.handleRetcodeAndReceipt(receipt, false);
  }

  /**
   * 修改某个账户的部署权限提案
   */
  public Object modifyDeployAuth(String groupId, String signUserId, Boolean openFlag,
      String userAddress) {
    return this.modifyDeployAuthHandle(groupId, signUserId, userAddress, openFlag);
  }

  public Object modifyDeployAuthHandle(String groupId, String signUserId, String userAddress,
      Boolean openFlag) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(userAddress);
    funcParams.add(openFlag.toString());
    funcParams.add(DEFAULT_BLOCK_NUMBER_INTERVAL.toString(10));
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.COMMITTEE_MANAGER);
    String abiStr = CommitteeManager.getABI();
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_CREATEMODIFYDEPLOYAUTHPROPOSAL,
            funcParams);
    return this.handleRetcodeAndReceipt(receipt, false);
  }

  /**
   * 重置某个合约的管理员账号提案
   */
  public Object resetAdmin(String groupId, String signUserId, String newAdmin, String contractAddr)
      throws ContractException, ContractCodecException, TransactionException, IOException {
    return this.resetAdminHandle(groupId, signUserId, newAdmin, contractAddr);
  }

  public Object resetAdminHandle(String groupId, String signUserId, String newAdmin,
      String contractAddr) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(newAdmin);
    funcParams.add(contractAddr);
    funcParams.add(DEFAULT_BLOCK_NUMBER_INTERVAL.toString(10));
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.COMMITTEE_MANAGER);
    String abiStr = CommitteeManager.getABI();
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_CREATERESETADMINPROPOSAL,
            funcParams);
    return this.handleRetcodeAndReceipt(receipt, false);
  }

  /**
   * 撤销提案的发起，该操作只有发起提案的治理委员才能操作
   */
  public Object revokeProposal(String groupId, String signUserId, BigInteger proposalId)
      throws ContractException, ContractCodecException, TransactionException, IOException {
    return this.revokeProposalHandle(groupId, signUserId, proposalId);
  }

  public Object revokeProposalHandle(String groupId, String signUserId, BigInteger proposalId) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(proposalId.toString(10));
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.COMMITTEE_MANAGER);
    String abiStr = CommitteeManager.getABI();
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_REVOKEPROPOSAL,
            funcParams);
    return this.handleRetcodeAndReceipt(receipt, false);
  }

  /**
   * 向某个提案进行投票
   */
  public Object voteProposal(String groupId, String signUserId, BigInteger proposalId,
      Boolean agree)
      throws ContractException {
    return this.voteProposalHandle(groupId, signUserId, proposalId, agree);
  }

  public String voteProposalHandle(String groupId, String signUserId, BigInteger proposalId,
      Boolean agree) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(proposalId.toString(10));
    funcParams.add(agree.toString());
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.COMMITTEE_MANAGER);
    String abiStr = CommitteeManager.getABI();
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_VOTEPROPOSAL,
            funcParams);
    return this.handleRetcodeAndReceipt(receipt, false);
  }

  public String handleRetcodeAndReceipt(TransactionReceipt receipt, boolean isWasm) {
    if (receipt.getStatus() == 16) {
      RetCode sdkRetCode = new RetCode();
      if (receipt.getMessage().equals("Proposal not exist")) {
        return new BaseResponse(ConstantCode.PROPOSAL_NOT_EXIST,
            sdkRetCode.getMessage()).toString();
      } else if (receipt.getMessage().equals("Already voted")) {
        return new BaseResponse(ConstantCode.PROPOSAL_IS_ALREADY_VOTED,
            sdkRetCode.getMessage()).toString();
      } else if (receipt.getMessage().equals("Proposal is not votable")) {
        return new BaseResponse(ConstantCode.PROPOSAL_IS_NOT_VOTABLE,
            sdkRetCode.getMessage()).toString();
      } else if (receipt.getMessage()
          .equals("The proposal is not votable , please ensure the proposal")) {
        return new BaseResponse(ConstantCode.PROPOSAL_IS_VOTING,
            sdkRetCode.getMessage()).toString();
      } else if (receipt.getMessage().equals("Only newly created proposal can be revoked")) {
        return new BaseResponse(ConstantCode.PROPOSAL_NOT_NEW_CREATED,
            sdkRetCode.getMessage()).toString();
      } else if (receipt.getMessage().equals("Current proposal not end")) {
        return new BaseResponse(ConstantCode.PROPOSAL_NOT_END,
            sdkRetCode.getMessage()).toString();
      } else if (receipt.getMessage()
          .equals("the account has been the admin of concurrt contract.")) {
        return new BaseResponse(ConstantCode.ALREADY_ADMIN_OF_CONTRACT,
            sdkRetCode.getMessage()).toString();
      } else if (receipt.getMessage()
          .equals("you must be governor")) {
        return new BaseResponse(ConstantCode.MUST_BE_GOVERNOR,
            sdkRetCode.getMessage()).toString();
      }else if (receipt.getMessage()
          .equals("Only proposer can revoke")) {
        return new BaseResponse(ConstantCode.MUST_BE_PROPOSER,
            sdkRetCode.getMessage()).toString();
      }

    }
    return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
  }

}
