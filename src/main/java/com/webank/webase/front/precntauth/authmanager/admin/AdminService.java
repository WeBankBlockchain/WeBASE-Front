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
package com.webank.webase.front.precntauth.authmanager.admin;

import static org.fisco.bcos.sdk.v3.contract.auth.contracts.ContractAuthPrecompiled.FUNC_CLOSEMETHODAUTH;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.ContractAuthPrecompiled.FUNC_OPENMETHODAUTH;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.ContractAuthPrecompiled.FUNC_SETCONTRACTSTATUS;
import static org.fisco.bcos.sdk.v3.contract.auth.contracts.ContractAuthPrecompiled.FUNC_SETMETHODAUTHTYPE;
import static org.fisco.bcos.sdk.v3.contract.precompiled.sysconfig.SystemConfigPrecompiled.FUNC_SETVALUEBYKEY;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.precntauth.precompiled.base.PrecompiledCommonInfo;
import com.webank.webase.front.precntauth.precompiled.base.PrecompiledUtils;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.contract.auth.contracts.ContractAuthPrecompiled;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
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
      byte[] func, BigInteger authType) {
    return this.setMethodAuthTypeHandle(groupId, signUserId, contractAddr, func, authType);
  }

  public Object setMethodAuthTypeHandle(String groupId, String signUserId, String contractAddr,
      byte[] func, BigInteger authType) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(contractAddr);
    byte[] hash = web3ApiService.getWeb3j(groupId).getCryptoSuite().hash(func);
    byte[] Func = Arrays.copyOfRange(hash, 0, 4);
    //byte[] to 0xFF
    String newFunc = CommonUtils.parseByte2HexStr(Func);
    funcParams.add(newFunc);
    funcParams.add(authType.toString(10));
    String contractAddress = PrecompiledCommonInfo.getAddress(
        PrecompiledTypes.CONTRACT_AUTH);
    String abiStr = ContractAuthPrecompiled.getABI();
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, FUNC_SETMETHODAUTHTYPE, funcParams);
    return this.handleRetcodeAndReceipt(receipt, false);
  }


  /**
   * open  or close the method permission of contract
   */
  public Object setMethodAuth(String groupId, String signUserId, String contractAddr,
      byte[] func, String accountAddress, Boolean bool) throws ContractException {
    return this.setMethodAuthHandle(groupId, signUserId, contractAddr, func, accountAddress, bool);
  }

  public Object setMethodAuthHandle(String groupId, String signUserId, String contractAddr,
      byte[] func, String accountAddress, Boolean bool) {
    TransactionReceipt receipt;
    List<String> funcParams = new ArrayList<>();
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
    return this.handleRetcodeAndReceipt(receipt, false);
  }


  public String setContractStatus(String groupId, String signUserId,
      String contractAddress, boolean isFreeze) {

    List<String> funcParams = new ArrayList<>();
    funcParams.add(contractAddress);
    funcParams.add(String.valueOf(isFreeze));
    // get address and abi of precompiled contract
    String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONTRACT_AUTH);
    if (!web3ApiService.getWeb3j(groupId).isAuthCheck()) {
      throw new FrontException(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
    }
    if (web3ApiService.getWeb3j(groupId).isWASM()) {
      throw new FrontException(ConstantCode.EXEC_ENV_IS_WASM);
    }
    String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CONTRACT_AUTH);
    // execute set method
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, precompiledAddress, abiStr, FUNC_SETCONTRACTSTATUS, funcParams);
    return PrecompiledUtils.handleTransactionReceipt(receipt, false);
  }


  public String handleRetcodeAndReceipt(TransactionReceipt receipt, boolean isWasm) {
    if (receipt.getStatus() == 16) {
      RetCode sdkRetCode = new RetCode();
      if (receipt.getMessage().equals("Proposal not exist")) {
        return new BaseResponse(ConstantCode.PROPOSAL_NOT_EXIST,
            sdkRetCode.getMessage()).toString();
      } else if (receipt.getMessage().equals("Current proposal not end")) {
        return new BaseResponse(ConstantCode.PROPOSAL_NOT_END,
            sdkRetCode.getMessage()).toString();
      }
    } else if (receipt.getStatus() == -50100) {
      RetCode sdkRetCode = new RetCode();
      if (receipt.getMessage()
          .equals("Open table failed, please check the existence of the table")) {
        return new BaseResponse(ConstantCode.OPEN_TABLE_FAILED,
            sdkRetCode.getMessage()).toString();
      }
    }else if (receipt.getStatus() == -51002) {
      RetCode sdkRetCode = new RetCode();
      if (receipt.getMessage()
          .equals("The contract method auth type not set, please set method auth type first.")) {
        return new BaseResponse(ConstantCode.NOT_SET_METHOD_AUTH_TYPE,
            sdkRetCode.getMessage()).toString();
      }
    }
    return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
  }

}
