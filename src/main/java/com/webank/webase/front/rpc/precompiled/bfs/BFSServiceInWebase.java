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
package com.webank.webase.front.rpc.precompiled.bfs;

import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.rpc.precompiled.base.PrecompiledCommonInfo;
import com.webank.webase.front.rpc.precompiled.base.PrecompiledUtil;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.web3api.Web3ApiService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.fisco.bcos.sdk.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.contract.precompiled.bfs.BFSPrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.contract.precompiled.bfs.FileInfo;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
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
  public Object mkdir(String groupId, String path, String signUserId){
    List<Object> funcParams = new ArrayList<>();
    funcParams.add(path);
    String contractAddress;
    if (web3ApiService.getWeb3j(groupId).isWASM()) {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.BFS_LIQUID);
    } else {
      contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.BFS);
    }
    String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.BFS);
    TransactionReceipt receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, contractAddress, abiStr, BFSPrecompiled.FUNC_MKDIR, funcParams);
    return PrecompiledUtil.handleTransactionReceipt(receipt);
  }

  /**
   * BFS获取某个目录信息
   */
  public Object list(String groupId, String path)
      throws ContractException {
    List<String> resList = new ArrayList<>();
    BFSService bfsService = new BFSService(web3ApiService.getWeb3j(groupId),
        keyStoreService.getCredentialsForQuery(groupId));
    List<FileInfo> fileInfoList = bfsService.list(path);
    for (FileInfo fileInfo : fileInfoList) {
      resList.add(fileInfo.getName().toLowerCase(Locale.ROOT));
    }
    return resList;
  }


}
