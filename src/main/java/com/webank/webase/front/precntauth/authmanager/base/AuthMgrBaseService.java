package com.webank.webase.front.precntauth.authmanager.base;

import com.webank.webase.front.web3api.Web3ApiService;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthMgrBaseService {

  @Autowired
  private Web3ApiService web3ApiService;

  public boolean execEnvIsWasm(String groupId) {
    Client client = web3ApiService.getWeb3j(groupId);
    return client.isWASM();
  }


  public boolean chainHasAuth(String groupId) {
    Client client = web3ApiService.getWeb3j(groupId);
    return client.isAuthCheck();
  }


}
