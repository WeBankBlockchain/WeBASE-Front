/**
 * Copyright 2014-2020 the original author or authors.
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

import com.webank.webase.front.util.CommonUtils;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.utils.Numeric;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class KeyStoreServiceTest {

    private static final String pemContent = "-----BEGIN PRIVATE KEY-----\n" +
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

    @Test
    public void loadPrivateKeyTest() throws Exception {
        String privateKey = "71f1479d9051e8d6b141a3b3ef9c01a7756da823a0af280c6bf62d18ee0cc978";
        Credentials credentials = GenCredential.create(privateKey);
        // private key 实例
        BigInteger privateKeyInstance = credentials.getEcKeyPair().getPrivateKey();
        System.out.println(Numeric.toHexStringNoPrefix(privateKeyInstance));
        // public key 实例
        BigInteger publicKeyInstance = credentials.getEcKeyPair().getPublicKey();
        System.out.println(Numeric.toHexString(publicKeyInstance.toByteArray()));
        String address = credentials.getAddress();
        System.out.println(address);
    }

    @Test
    public void testNotContainsChinese() {
        String notContains = "test";
        String contain = "test中文";
        boolean flagTrue = CommonUtils.notContainsChinese(notContains);
        boolean flagFalse = CommonUtils.notContainsChinese(contain);
        Assert.assertTrue("contains chinese error", !flagFalse);
        Assert.assertTrue("not contains normal", flagTrue);

    }

    @Test
	public void testLoadP12() throws UnrecoverableKeyException, InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, CertificateException, IOException {
		P12Manager p12Manager = new P12Manager();
		p12Manager.setP12File("0x6399bda67f0ae8d1fdd997a885b8aee32a0c9696.p12");
		p12Manager.setPassword("123");
		p12Manager.load();
		// c5658bbb9b905345e7c057690ec6f50c06dada711d1086820980496b4954fbc7
		String privateKey = Numeric.toHexStringNoPrefix(p12Manager.getECKeyPair().getPrivateKey());
		System.out.println("load private key: " + privateKey);
		String address = GenCredential.create(privateKey).getAddress();
		System.out.println("address: " + address);
		Assert.assertTrue("pri error", address.equals("0x6399bda67f0ae8d1fdd997a885b8aee32a0c9696"));
	}

}
