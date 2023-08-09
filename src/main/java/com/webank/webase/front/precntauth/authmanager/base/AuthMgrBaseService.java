package com.webank.webase.front.precntauth.authmanager.base;

import com.webank.webase.front.web3api.Web3ApiService;
import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthMgrBaseService {

  @Autowired
  private Web3ApiService web3ApiService;
  /**
   * default block number interval. after current block number, it will be outdated. Default value is about a week.
   */
  public final static BigInteger DEFAULT_BLOCK_NUMBER_INTERVAL = BigInteger.valueOf(3600 * 24 * 7L);

  public boolean execEnvIsWasm(String groupId) {
    Client client = web3ApiService.getWeb3j(groupId);
    return client.isWASM();
  }


  public boolean chainHasAuth(String groupId) {
    Client client = web3ApiService.getWeb3j(groupId);
    return client.isAuthCheck();
  }


}
