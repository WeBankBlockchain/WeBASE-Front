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
package com.webank.webase.front.rpc.authmanager.admin;

import static org.fisco.bcos.sdk.contract.auth.contracts.ContractAuthPrecompiled.ABI;
import static org.fisco.bcos.sdk.contract.auth.contracts.ContractAuthPrecompiled.FUNC_CLOSEMETHODAUTH;
import static org.fisco.bcos.sdk.contract.auth.contracts.ContractAuthPrecompiled.FUNC_OPENMETHODAUTH;
import static org.fisco.bcos.sdk.contract.auth.contracts.ContractAuthPrecompiled.FUNC_SETMETHODAUTHTYPE;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.rpc.authmanager.base.AuthMgrBaseService;
import com.webank.webase.front.rpc.precompiled.base.PrecompiledCommonInfo;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.codec.datatypes.generated.Bytes4;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.contract.auth.contracts.Committee;
import org.fisco.bcos.sdk.contract.auth.contracts.CommitteeManager;
import org.fisco.bcos.sdk.contract.auth.contracts.ContractAuthPrecompiled;
import org.fisco.bcos.sdk.contract.auth.contracts.DeployAuthManager;
import org.fisco.bcos.sdk.contract.auth.manager.AuthManager;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The service can be requested by admin;
 * Handle transaction through webase-sign.
 */
@Slf4j
@Service
public class AdminService {

  @Autowired
  private Web3ApiService web3ApiService;
  @Autowired
  private TransService transService;


  /**
   * set contract acl: white_list(type=1) or black_list(type=2)
   */
  public Object setMethodAuthType(String groupId, String signUserId, String contractAddr,
      byte[] func, BigInteger authType)
      throws ContractException {
    return this.setMethodAuthTypeHandle(groupId, signUserId, contractAddr, func, authType);
  }

  public Object setMethodAuthTypeHandle(String groupId, String signUserId, String contractAddr,
      byte[] func, BigInteger authType) {
    List<Object> funcParams = new ArrayList<>();
    funcParams.add(contractAddr);
    byte[] hash = web3ApiService.getWeb3j(groupId).getCryptoSuite().hash(func);
    byte[] Func = Arrays.copyOfRange(hash, 0, 4);
    //byte[] to 0xFF
    String newFunc = CommonUtils.parseByte2HexStr(Func);
    funcParams.add(newFunc);
    funcParams.add(authType);
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.CONTRACT_AUTH);
    String abiStr = ContractAuthPrecompiled.getABI();
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_SETMETHODAUTHTYPE, funcParams);
    return this.handleRetcodeAndReceipt(receipt);
  }


  /**
   * open  or close the method permission of contract
   */
  public Object setMethodAuth(String groupId, String signUserId, String contractAddr,
      byte[] func, String accountAddress, Boolean bool) throws ContractException {
    return this.setMethodAuthHandle(groupId, signUserId, contractAddr, func, accountAddress, bool);
  }

  public Object setMethodAuthHandle(String groupId, String signUserId, String contractAddr,
      byte[] func, String accountAddress, Boolean bool) throws ContractException {
    TransactionReceipt receipt;
    List<Object> funcParams = new ArrayList<>();
    funcParams.add(contractAddr);
    byte[] hash = web3ApiService.getWeb3j(groupId).getCryptoSuite().hash(func);
    byte[] Func = Arrays.copyOfRange(hash, 0, 4);
    //byte[] to 0xFF
    String newFunc = CommonUtils.parseByte2HexStr(Func);
    funcParams.add(newFunc);
    funcParams.add(accountAddress);
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.CONTRACT_AUTH);
    String abiStr = ContractAuthPrecompiled.getABI();
    if (bool) {
      receipt =
          (TransactionReceipt) transService.transHandleWithSign(groupId,
              signUserId, contractAddress, abiStr, FUNC_OPENMETHODAUTH, funcParams);
    } else {
      receipt =
          (TransactionReceipt) transService.transHandleWithSign(groupId,
              signUserId, contractAddress, abiStr, FUNC_CLOSEMETHODAUTH, funcParams);
    }
    return this.handleRetcodeAndReceipt(receipt);
  }

  public String handleRetcodeAndReceipt(TransactionReceipt receipt) {
    if (receipt.getStatus() == 16) {
      RetCode sdkRetCode = new RetCode();
      if (receipt.getMessage().equals("Proposal not exist")) {
        return new BaseResponse(ConstantCode.PROPOSAL_NOT_EXIST,
            sdkRetCode.getMessage()).toString();
      } else if (receipt.getMessage().equals("Current proposal not end")) {
        return new BaseResponse(ConstantCode.PROPOSAL_NOT_END,
            sdkRetCode.getMessage()).toString();
      }
    }
    return this.handleTransactionReceipt(receipt);
  }

  private String handleTransactionReceipt(TransactionReceipt receipt) {
    log.debug("handle tx receipt of precompiled");
    try {
      RetCode sdkRetCode = ReceiptParser.parseTransactionReceipt(receipt);
      log.info("handleTransactionReceipt sdkRetCode:{}", sdkRetCode);
      if (sdkRetCode.getCode() >= 0) {
        return new BaseResponse(ConstantCode.RET_SUCCESS,
            sdkRetCode.getMessage()).toString();
      } else {
        throw new FrontException(sdkRetCode.getCode(), sdkRetCode.getMessage());
      }
    } catch (ContractException e) {
      log.error("handleTransactionReceipt e:[]", e);
      throw new FrontException(e.getErrorCode(), e.getMessage());
    }
  }

}
