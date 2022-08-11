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
package com.webank.webase.front.precntauth.precompiled.cns;

import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precntauth.precompiled.bfs.BFSServiceInWebase;
import com.webank.webase.front.precntauth.precompiled.cns.entity.CnsInfo;
import com.webank.webase.front.precntauth.precompiled.cns.entity.ResCnsInfo;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.web3api.Web3ApiService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled.BfsInfo;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CNS management service;
 * Handle transaction through webase-sign.
 * 2022-08-02: use BfsService instead of CNSService in sdk
 */
@Slf4j
@Service
public class CNSServiceInWebase {

  @Autowired
  private Web3ApiService web3ApiService;
  @Autowired
  private KeyStoreService keyStoreService;
  @Autowired
  private TransService transService;
  @Autowired
  private BFSServiceInWebase bfsServiceInWebase;

  private static final String CONTRACT_PREFIX = "/apps/";
  private static final String CNS_FILE_TYPE = "link";

  public String registerCNS(String groupId, String signUserId, String contractName,
      String contractVersion,
      String contractAddress, String abiData)
      throws ContractException {
    TransactionReceipt receipt;
    return bfsServiceInWebase.link(groupId, signUserId, contractName, contractVersion, contractAddress, abiData);
  }

  public List<CnsInfo> queryCnsInfoByName(String groupId, String contractName)
      throws ContractException {
    List<BfsInfo> bfsInfoList = bfsServiceInWebase.list(groupId, CONTRACT_PREFIX + contractName);
    log.info("queryCnsByNameAndVersion bfsInfoList:{}", bfsInfoList);
    // 只有link是cns的
    List<CnsInfo> cnsInfos = bfsInfoList.stream()
        .filter(bfs -> CNS_FILE_TYPE.equals(bfs.getFileType()))
        .map(bfs -> {
              String version = bfs.getFileName();
              try {
                Tuple2<String, String> addressAbi = queryCnsByNameAndVersion(groupId, contractName, version);
                return new CnsInfo(contractName, bfs.getFileName(), addressAbi.getValue1(), addressAbi.getValue2());
              } catch (ContractException e) {
                log.error("query cns name version failed:{}|{}", contractName, version);
                return new CnsInfo(contractName, bfs.getFileName(), "", "");
              }
        })
        .collect(Collectors.toList());
    log.info("queryCnsByNameAndVersion cnsInfos:{}", cnsInfos);
    return cnsInfos;
  }

  public Tuple2<String, String> queryCnsByNameAndVersion(String groupId, String contractName,
      String version) throws ContractException {

    List<BfsInfo> bfsInfoList = bfsServiceInWebase.list(groupId, CONTRACT_PREFIX + contractName + "/" + version);
    log.info("queryCnsByNameAndVersion bfsInfoList:{}", bfsInfoList);
    // 只有link是cns的
    List<BfsInfo> versionInfoList = bfsInfoList.stream().filter(bfs
        -> CNS_FILE_TYPE.equals(bfs.getFileType())).collect(Collectors.toList());
    log.info("queryCnsByNameAndVersion versionInfoList:{}", versionInfoList);
    // 一个版本的cns只有一个
    if (versionInfoList.size() == 1) {
      BfsInfo bfsInfo = versionInfoList.get(0);
      String address = bfsInfo.getExt().get(0);
      String abi = bfsInfo.getExt().get(1);
      return new Tuple2<String, String>(address, abi);
    } else {
      return new Tuple2<String, String>("", "");
    }
  }

  public ResCnsInfo queryCnsByNameAndVersion2(String groupId, String contractName,
      String version) throws ContractException {
    ResCnsInfo resCnsInfo = new ResCnsInfo();

    Tuple2<String, String> tuple2 = this.queryCnsByNameAndVersion(groupId, contractName, version);
    resCnsInfo.setAddress(tuple2.getValue1());
    resCnsInfo.setAbi(tuple2.getValue2());
    return resCnsInfo;
  }

  public String getAddressByContractNameAndVersion(String groupId, String contractName,
      String version) throws ContractException {
    Tuple2<String, String> tuple2 = this.queryCnsByNameAndVersion(groupId, contractName, version);
    return tuple2.getValue1();
  }

}
