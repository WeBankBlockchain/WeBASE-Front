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

package com.webank.webase.front.util;

import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2KeyGenerator;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2PrivateKey;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2PublicKey;
import org.fisco.bcos.web3j.crypto.gm.sm2.util.encoders.Hex;

import java.math.BigInteger;
import java.security.KeyPair;

@Log4j2
public class CredentialUtils extends GenCredential {

    public static ECKeyPair createKeyPair(String privKey) {
        if (EncryptType.encryptType == 1) return createGuomiKeyPair(privKey);
        return createECDSAKeyPair(privKey);
    }
    public static ECKeyPair createKeyPair() {
        // use guomi
        if (EncryptType.encryptType == 1) return createGuomiKeyPair();
        return createECDSAKeyPair(); // default use ECDSA
    }

    private static ECKeyPair createECDSAKeyPair() {
        try {
            ECKeyPair keyPair = Keys.createEcKeyPair();
            return keyPair;
        } catch (Exception e) {
            log.error("create keypair of ECDSA failed, error msg:" + e.getMessage());
            return null;
        }
    }

    private static ECKeyPair createGuomiKeyPair(String privKey) {
        SM2KeyGenerator generator = new SM2KeyGenerator();
        final KeyPair keyPairData = generator.generateKeyPair(privKey);
        if (keyPairData != null) return genEcPairFromKeyPair(keyPairData);
        return null;
    }

    private static ECKeyPair genEcPairFromKeyPair(KeyPair keyPairData) {
        try {
            SM2PrivateKey vk = (SM2PrivateKey) keyPairData.getPrivate();
            SM2PublicKey pk = (SM2PublicKey) keyPairData.getPublic();
            final byte[] publicKey = pk.getEncoded();
            final byte[] privateKey = vk.getEncoded();

            // System.out.println("===public:" + Hex.toHexString(publicKey));
            // System.out.println("===private:" + Hex.toHexString(privateKey));
            BigInteger biPublic = new BigInteger(Hex.toHexString(publicKey), 16);
            BigInteger biPrivate = new BigInteger(Hex.toHexString(privateKey), 16);

            // System.out.println("---public:" + biPublic.toString(16));
            // System.out.println("---private:" + biPrivate.toString(16));

            ECKeyPair keyPair = new ECKeyPair(biPrivate, biPublic);
            return keyPair;
        } catch (Exception e) {
            log.error("GmUtils create ec_keypair of guomi failed, error msg:" + e.getMessage());
            return null;
        }
    }

    private static ECKeyPair createECDSAKeyPair(String privKey) {
        try {
            BigInteger bigPrivKey = new BigInteger(privKey, 16);
            ECKeyPair keyPair = ECKeyPair.create(bigPrivKey);
            return keyPair;
        } catch (Exception e) {
            log.error("GmUtils create keypair of ECDSA failed, error msg:" + e.getMessage());
            return null;
        }
    }
}
