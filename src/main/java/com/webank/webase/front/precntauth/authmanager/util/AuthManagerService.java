package com.webank.webase.front.precntauth.authmanager.util;

import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.web3api.Web3ApiService;
import org.fisco.bcos.sdk.contract.auth.manager.AuthManager;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthManagerService {

  @Autowired
  private Web3ApiService web3ApiService;
  @Autowired
  private KeyStoreService keyStoreService;

  public AuthManager getAuthManagerService(String groupId) throws ContractException {
    AuthManager authManager = new AuthManager(web3ApiService.getWeb3j(groupId),
        keyStoreService.getCredentialsForQuery(groupId));
    return authManager;
  }

  public Web3ApiService getWeb3ApiService() {
    return web3ApiService;
  }


}
