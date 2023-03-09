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
package com.webank.webase.front.precntauth.precompiled.bfs;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precntauth.precompiled.base.PrecompiledCommonInfo;
import com.webank.webase.front.precntauth.precompiled.base.PrecompiledUtils;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.web3api.Web3ApiService;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled.BfsInfo;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BFS service;
 * Handle transaction through webase-sign.
 */
@Service
public class BFSServiceInWebase {

  @Autowired
  private Web3ApiService web3ApiService;
  @Autowired
  private TransService transService;
  @Autowired
  private KeyStoreService keyStoreService;

  /**
   * BFS创建某个目录
   */
  public Object mkdir(String groupId, String path, String signUserId) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(path);
    String contractAddress;
    boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
    if (isWasm) {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.BFS_LIQUID);
    } else {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.BFS);
    }
    String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.BFS);
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, BFSPrecompiled.FUNC_MKDIR, funcParams, isWasm);
    return this.handleRetcodeAndReceipt(receipt, isWasm);
  }

  /**
   * BFS获取某个目录信息
   */
  public List<BfsInfo> list(String groupId, String path)
      throws ContractException {
    BFSService bfsService = new BFSService(web3ApiService.getWeb3j(groupId),
        keyStoreService.getCredentialsForQuery(groupId));
    List<BfsInfo> fileInfoList = bfsService.list(path);
    return fileInfoList;
  }

  /**
   * BFS link
   */
  public String link(String groupId, String signUserId, String contractName,
      String contractVersion,
      String contractAddress, String abiData) {
    List<String> funcParams = new ArrayList<>();
    funcParams.add(contractName);
    funcParams.add(contractVersion);
    funcParams.add(contractAddress);
    funcParams.add(abiData);

    String bfsAddress;
    boolean isWasm = web3ApiService.getWeb3j(groupId).isWASM();
    if (isWasm) {
      bfsAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.BFS_LIQUID);
    } else {
      bfsAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.BFS);
    }
    String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.BFS);
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, bfsAddress, abiStr, BFSPrecompiled.FUNC_LINK, funcParams, isWasm);
    return this.handleRetcodeAndReceipt(receipt, isWasm);
  }

  /**
   * BFS readlink 获取某个目录信息
   */
  public Object readlink(String groupId, String path)
      throws ContractException {
    BFSService bfsService = new BFSService(web3ApiService.getWeb3j(groupId),
        keyStoreService.getCredentialsForQuery(groupId));
    String readlink = bfsService.readlink(path);
    return readlink;
  }


  public String handleRetcodeAndReceipt(TransactionReceipt receipt, boolean isWasm) {
    if (receipt.getStatus() == -53005) {
      RetCode sdkRetCode = new RetCode();
      if (receipt.getMessage().equals("Invalid path")) {
        return new BaseResponse(ConstantCode.BFS_INVALID_PATH,
            sdkRetCode.getMessage()).toString();
      }
    }
    return PrecompiledUtils.handleTransactionReceipt(receipt, isWasm);
  }

}
