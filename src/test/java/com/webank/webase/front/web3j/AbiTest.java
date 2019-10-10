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
/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.webank.webase.front.web3j;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.solidity.Abi;
import org.junit.Test;

public class AbiTest {


  @Test
  public void testGenerateKey() throws Exception {
    Credentials credentials = Credentials.create("3bed914595c159cbce70ec5fb6aff3d6797e0c5ee5a7a9224a21cae8932d84a4");
    System.out.println( credentials.getAddress());
    System.out.println( credentials.getEcKeyPair().getPrivateKey());
    System.out.println(  credentials.getEcKeyPair().getPublicKey());

  }
  @Test
  public void simpleTest() throws IOException {
    String contractAbi =
        "[{"
            + "\"name\":\"simpleFunction\","
            + "\"constant\":true,"
            + "\"payable\":true,"
            + "\"type\":\"function\","
            + "\"inputs\": [{\"name\":\"_in\", \"type\":\"bytes32\"}],"
            + "\"outputs\":[{\"name\":\"_out\",\"type\":\"bytes32\"}]}]";

    Abi abi = Abi.fromJson(contractAbi);
    assertEquals(abi.size(), 1);

    Abi.Entry onlyFunc = abi.get(0);
    assertEquals(onlyFunc.type, Abi.Entry.Type.function);
    assertEquals(onlyFunc.inputs.size(), 1);
    assertEquals(onlyFunc.outputs.size(), 1);
    assertTrue(onlyFunc.payable);
    assertTrue(onlyFunc.constant);
  }

  @Test
  public void simpleLegacyTest() throws IOException {
    String contractAbi =
        "[{"
            + "\"name\":\"simpleFunction\","
            + "\"constant\":true,"
            + "\"type\":\"function\","
            + "\"inputs\": [{\"name\":\"_in\", \"type\":\"bytes32\"}],"
            + "\"outputs\":[{\"name\":\"_out\",\"type\":\"bytes32\"}]}]";

    Abi abi = Abi.fromJson(contractAbi);
    assertEquals(abi.size(), 1);

    Abi.Entry onlyFunc = abi.get(0);
    assertEquals(onlyFunc.type, Abi.Entry.Type.function);
    assertEquals(onlyFunc.inputs.size(), 1);
    assertEquals(onlyFunc.outputs.size(), 1);
    assertTrue(onlyFunc.constant);
  }
}
