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

package com.webank.webase.front.keystore;

import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.utils.Numeric;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class KeyStoreServiceTest {

    public static final String pemContent = "-----BEGIN PRIVATE KEY-----\n" +
            "MIGEAgEAMBAGByqGSM49AgEGBSuBBAAKBG0wawIBAQQgC8TbvFSMA9y3CghFt51/" +
            "XmExewlioX99veYHOV7dTvOhRANCAASZtMhCTcaedNP+H7iljbTIqXOFM6qm5aVs" +
            "fM/yuDBK2MRfFbfnOYVTNKyOSnmkY+xBfCR8Q86wcsQm9NZpkmFK" +
            "\n-----END PRIVATE KEY-----\n";

    @Test
    public void testLoadPem() throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, IOException, UnrecoverableKeyException {
        System.out.println(Numeric.toHexString(pemContent.getBytes()));
        PEMManager pemManager0 = new PEMManager();
        pemManager0.load(new ByteArrayInputStream(pemContent.getBytes()));

        System.out.println(Numeric.toHexStringNoPrefix(pemManager0.getECKeyPair().getPrivateKey()));
//        System.out.println(Numeric.toHexStringNoPrefix(pemManager0.getPrivateKey().getEncoded()));
    }

    @Test
    public void testLoadPemFile() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException, KeyStoreException {
        PEMManager pemManager = new PEMManager();
        // need pem
        InputStream nodeCrtInput = new ClassPathResource("test.pem").getInputStream();
        pemManager.load(nodeCrtInput);
        System.out.println(pemManager.getPrivateKey());
        System.out.println(Numeric.toHexString(pemManager.getPrivateKey().getEncoded()));
    }
}
