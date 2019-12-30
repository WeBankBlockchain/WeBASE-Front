/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.gm.runtime;

import com.webank.webase.front.util.AesUtils;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.utils.Numeric;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class PublicKeyTest extends BaseTest {
    @Autowired
    private AesUtils aesUtils;

    @Test
    public void checkPubCorrect() throws IOException {
        String defaultUserPrivateKeyAfterAes = "SzK9KCjpyVCW0T9K9r/MSlmcpkeckYKVn/D1X7fzzp18MM7yHhUHQugTxKXVJJY5XWOb4zZ79IXMBu77zmXsr0mCRnATZTUqFfWLX6tUBIw=";
        String defaultPub = "0xc5d877bff9923af55f248fb48b8907dc7d00cac3ba19b4259aebefe325510af7bd0a75e9a8e8234aa7aa58bc70510ee4bef02201a86006196da4e771c47b71b4";
        String defaultAddress = "0xf1585b8d0e08a0a00fff662e24d67ba95a438256";

        String defaultUserPrivateKey = aesUtils.aesDecrypt(defaultUserPrivateKeyAfterAes);
        System.out.println("decrypt aes");
        System.out.println(defaultUserPrivateKey);
        Credentials credential = GenCredential.create(defaultUserPrivateKey);
        System.out.println("private: ");
        System.out.println(credential.getEcKeyPair().getPrivateKey());
        System.out.println(Numeric.toHexStringNoPrefix(credential.getEcKeyPair().getPrivateKey()));
        System.out.println("pub: ");
        System.out.println(credential.getEcKeyPair().getPublicKey());
        System.out.println("address: ");
        System.out.println(credential.getAddress());
        System.out.println(Keys.getAddress(credential.getEcKeyPair().getPublicKey()));
    }

}
