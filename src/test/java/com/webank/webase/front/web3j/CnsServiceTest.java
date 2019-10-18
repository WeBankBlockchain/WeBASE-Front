/*
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.front.web3j;

import com.webank.webase.front.channel.test.TestBase;
import org.fisco.bcos.temp.Ok;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;

public class CnsServiceTest extends TestBase {

  public Credentials credentials =
      Credentials.create("b83261efa42895c38c6c2364ca878f43e77f3cddbc922bf57d0d48070f79feb6");

  CnsService cnsService = new CnsService(web3j, credentials);

  BigInteger gasPrice = new BigInteger("300000000");
  BigInteger gasLimit = new BigInteger("300000000");

  @Test
  public void getContractAddressFromNameAndVersion() throws Exception {

    Ok okDemo = Ok.deploy(web3j, credentials, gasPrice, gasLimit).send();
    System.out.println("okdemo contract address " + okDemo.getContractAddress());
    int random = new SecureRandom().nextInt(1000);
    String name = "hello world" + random;
    String result =
        cnsService.registerCns(
            name,
            "10.0",
            okDemo.getContractAddress(),
            "[\n"
                + "\t{\n"
                + "\t\t\"constant\": false,\n"
                + "\t\t\"inputs\": [\n"
                + "\t\t\t{\n"
                + "\t\t\t\t\"name\": \"num\",\n"
                + "\t\t\t\t\"type\": \"uint256\"\n"
                + "\t\t\t}\n"
                + "\t\t],\n"
                + "\t\t\"name\": \"trans\",\n"
                + "\t\t\"outputs\": [],\n"
                + "\t\t\"payable\": false,\n"
                + "\t\t\"type\": \"function\",\n"
                + "\t\t\"stateMutability\": \"nonpayable\"\n"
                + "\t},\n"
                + "\t{\n"
                + "\t\t\"constant\": true,\n"
                + "\t\t\"inputs\": [],\n"
                + "\t\t\"name\": \"get\",\n"
                + "\t\t\"outputs\": [\n"
                + "\t\t\t{\n"
                + "\t\t\t\t\"name\": \"\",\n"
                + "\t\t\t\t\"type\": \"uint256\"\n"
                + "\t\t\t}\n"
                + "\t\t],\n"
                + "\t\t\"payable\": false,\n"
                + "\t\t\"type\": \"function\",\n"
                + "\t\t\"stateMutability\": \"view\"\n"
                + "\t},\n"
                + "\t{\n"
                + "\t\t\"inputs\": [],\n"
                + "\t\t\"type\": \"constructor\",\n"
                + "\t\t\"payable\": true,\n"
                + "\t\t\"stateMutability\": \"payable\"\n"
                + "\t}\n"
                + "]");
    System.out.println("result:" + result);
    System.out.println("CNS NAME   " + name + ":9.0");
    System.out.println("CNS register SUCCESSFULLY");
    System.out.println(
        "cnsResolver address" + cnsService.getAddressByContractNameAndVersion(name + ":10.0"));
    System.out.println("cnsResolver address" + cnsService.getAddressByContractNameAndVersion(name));
    System.out.println(
        "cnsResolver address"
            + cnsService.getAddressByContractNameAndVersion("0x67d6ab3debdcd15220dd11a724f96b5cf87d4663"));

    Ok okLoaded = Ok.load(name, web3j, credentials, gasPrice, gasLimit);
    System.out.println(okLoaded.isValid());
    BigInteger balance = okLoaded.get().send();
    System.out.println("balance = " + balance);
  }
}
