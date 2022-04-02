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
package com.webank.webase.front.rpc.precompiled.cns;

import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.rpc.precompiled.base.PrecompiledCommonInfo;
import com.webank.webase.front.rpc.precompiled.base.PrecompiledUtil;
import com.webank.webase.front.rpc.precompiled.cns.entity.ResCnsInfo;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.web3api.Web3ApiService;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.contract.precompiled.cns.CNSPrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.contract.precompiled.model.PrecompiledAddress;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CNS management service;
 * Handle transaction through webase-sign.
 */
@Service
public class CNSServiceInWebase {

  @Autowired
  private Web3ApiService web3ApiService;
  @Autowired
  private KeyStoreService keyStoreService;
  @Autowired
  private TransService transService;

  public Object registerCNS(String groupId, String signUserId, String contractName,
      String contractVersion,
      String contractAddress, String abiData)
      throws ContractException {
    TransactionReceipt receipt;
    String precompiledAddress;
    List<Object> funcParams = new ArrayList<>();
    funcParams.add(contractName);
    funcParams.add(contractVersion);
    funcParams.add(contractAddress);
    funcParams.add(abiData);
    //wasm or solidity
    Client client = web3ApiService.getWeb3j(groupId);
    if (client.isWASM()) {
      precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CNS_LIQUID);
    } else {
      precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CNS);
    }
    String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CNS);
    receipt =
        (TransactionReceipt) transService.transHandleWithSign(groupId,
            signUserId, precompiledAddress, abiStr, CNSPrecompiled.FUNC_INSERT, funcParams);
    return PrecompiledUtil.handleTransactionReceipt(receipt);
  }

  public List<CnsInfo> queryCnsInfoByName(String groupId, String contractName)
      throws ContractException {
    CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
        keyStoreService.getCredentialsForQuery(groupId));
    return cnsService.selectByName(contractName);
  }

  public Tuple2<String, String> queryCnsByNameAndVersion(String groupId, String contractName,
      String version) throws ContractException {
    CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
        keyStoreService.getCredentialsForQuery(groupId));
    return cnsService.selectByNameAndVersion(contractName, version);
  }

  public ResCnsInfo queryCnsByNameAndVersion2(String groupId, String contractName,
      String version) throws ContractException {
    ResCnsInfo resCnsInfo = new ResCnsInfo();
    CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
        keyStoreService.getCredentialsForQuery(groupId));
    Tuple2<String, String> tuple2 = cnsService.selectByNameAndVersion(contractName, version);
    resCnsInfo.setAddress(tuple2.getValue1());
    resCnsInfo.setAbi(tuple2.getValue2());
    return resCnsInfo;
  }

  public String getAddressByContractNameAndVersion(String groupId, String contractName,
      String version) throws ContractException {
    CnsService cnsService = new CnsService(web3ApiService.getWeb3j(groupId),
        keyStoreService.getCredentialsForQuery(groupId));
    return cnsService.getContractAddress(contractName, version);
  }

}
