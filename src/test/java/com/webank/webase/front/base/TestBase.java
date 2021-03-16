/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.base;

import java.math.BigInteger;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestBase {
  protected static BcosSDK bcosSDK;
  protected static Client web3j;
  protected static CryptoSuite cryptoSuite;
  protected static CryptoKeyPair cryptoKeyPair;
  protected static BigInteger chainId = new BigInteger("1");
  protected static Integer groupId = 1;

  protected static String address;
  protected static BigInteger blockNumber;
  protected static String blockHash;

  @BeforeClass
  public static void setUpBeforeClass() {
    // 绝对路径
    String configFile = ".\\src\\test\\resources\\config-example.toml";
    bcosSDK =  BcosSDK.build(configFile);
    web3j = bcosSDK.getClient(groupId);
    cryptoSuite = web3j.getCryptoSuite();
    cryptoKeyPair = web3j.getCryptoSuite().createKeyPair("71f1479d9051e8d6b141a3b3ef9c01a7756da823a0af280c6bf62d18ee0cc978");

    //todo deploy ok.sol
    // blockHash = web3j.getBlockHashByNumber(blockNumber).getBlockHashByNumber();
  }

}
