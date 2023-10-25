package com.webank.webase.front.precntauth.authmanager.util;

import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.web3api.Web3ApiService;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.auth.manager.AuthManager;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * init one instance of AuthManager to call auth manager contract (only call)
 */
@Slf4j
@Service
public class AuthManagerService {

  @Autowired
  private Web3ApiService web3ApiService;
  @Autowired
  private KeyStoreService keyStoreService;

  private static Map<String, AuthManager> authManagerMap = new ConcurrentHashMap<>();

  public AuthManager getAuthManagerService(String groupId) throws ContractException {
    Instant startTime = Instant.now();
    if (!authManagerMap.containsKey(groupId) || authManagerMap.get(groupId) == null) {
      AuthManager authManager = new AuthManager(web3ApiService.getWeb3j(groupId),
          keyStoreService.getCredentialsForQuery(groupId));
      authManagerMap.put(groupId, authManager);
      log.info("getAuthManagerService groupId:{},{}", groupId,
          Duration.between(startTime, Instant.now()).toMillis());
    }

    log.info("getAuthManagerService groupId:{},{}", groupId,
        Duration.between(startTime, Instant.now()).toMillis());
    return authManagerMap.get(groupId);
  }

  public AuthManager getAuthManagerByUser(String groupId, String signUserId) throws FrontException {
    Instant startTime = Instant.now();

    // 从sign server获取私钥
    String privKey = keyStoreService.getPrivateKeyWithSign(signUserId);

    if (privKey == null || StringUtils.isBlank(privKey)) {
      throw new FrontException(String.format("transHandleWithSign this signUser [{%s}] not found privkey", signUserId));
    }

    CryptoKeyPair cryptoKeyPair = web3ApiService.getCryptoSuite(groupId).getKeyPairFactory().createKeyPair(privKey);

    AuthManager authManager = new AuthManager(web3ApiService.getWeb3j(groupId), cryptoKeyPair);

    log.info("getAuthManagerByUser groupId:{},{}", groupId,
            Duration.between(startTime, Instant.now()).toMillis());
    return authManager;
  }




}
