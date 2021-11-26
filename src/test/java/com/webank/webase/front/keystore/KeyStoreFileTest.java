/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.keystore;

import com.webank.webase.front.util.CommonUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.keystore.KeyTool;
import org.fisco.bcos.sdk.crypto.keystore.P12KeyStore;
import org.fisco.bcos.sdk.crypto.keystore.PEMKeyStore;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.utils.Numeric;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class KeyStoreFileTest {


    private static final String pemContent = "-----BEGIN PRIVATE KEY-----\n" +
        "MIGEAgEAMBAGByqGSM49AgEGBSuBBAAKBG0wawIBAQQgC8TbvFSMA9y3CghFt51/" +
        "XmExewlioX99veYHOV7dTvOhRANCAASZtMhCTcaedNP+H7iljbTIqXOFM6qm5aVs" +
        "fM/yuDBK2MRfFbfnOYVTNKyOSnmkY+xBfCR8Q86wcsQm9NZpkmFK" +
        "\n-----END PRIVATE KEY-----\n";

    @Test
    public void testLoadPem() {
        System.out.println(Numeric.toHexString(pemContent.getBytes()));
        PEMKeyStore pemManager0 = new PEMKeyStore(new ByteArrayInputStream(pemContent.getBytes()));

        System.out.println(KeyTool.getHexedPrivateKey(pemManager0.getKeyPair().getPrivate()));
    }

    @Test
    public void testLoadPemFile() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException, KeyStoreException {
        // need pem
        InputStream nodeCrtInput = new ClassPathResource("test.pem").getInputStream();
        PEMKeyStore pemManager = new PEMKeyStore(nodeCrtInput);
        System.out.println(pemManager.getKeyPair().getPrivate());
        System.out.println(KeyTool.getHexedPrivateKey(pemManager.getKeyPair().getPrivate()));
    }

    @Test
    public void loadPrivateKeyTest() throws Exception {
        String privateKey = "71f1479d9051e8d6b141a3b3ef9c01a7756da823a0af280c6bf62d18ee0cc978";
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        CryptoKeyPair credentials = cryptoSuite.getKeyPairFactory().createKeyPair(privateKey);
        // private key 实例
        System.out.println(credentials.getHexPrivateKey());
        // public key 实例
        System.out.println(credentials.getHexPublicKey());
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
    public void testDecimalPrivateKey() {
        String decimalKey = "105056100209817976175483366253040865310410704320674863234725386650312386331016";
        String hexKey = "e843a542a7a8240f9c9e418b9517c2c8f4dc041a11a44e614a3b026c3588c188";
        String convertedHexKey = Numeric.toHexStringNoPrefix(new BigInteger(decimalKey));
        System.out.println("convertedHexKey: " + convertedHexKey);
        Assert.assertEquals(hexKey, convertedHexKey);

    }

}
