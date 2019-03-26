package com.webank.webase.front.util;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.utils.Numeric;
import org.junit.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class KeyStoreTest {

    @Test
    public void  testCreateCredential() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair keyPair = Keys.createEcKeyPair();
        System.out.println(keyPair.getPrivateKey());
        System.out.println(keyPair.getPublicKey());
        System.out.println(Keys.getAddress(keyPair));

        //------------
        String publicKey = Numeric.toHexStringWithPrefixZeroPadded(keyPair.getPublicKey(), 128);
        String privateKey = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
        String address = "0x" + Keys.getAddress(publicKey);
        System.out.println(privateKey);
        System.out.println(publicKey);
        System.out.println(address);


        Credentials credentials = Credentials.create(privateKey);
        System.out.println(credentials.getEcKeyPair().getPrivateKey());
        System.out.println(credentials.getEcKeyPair().getPublicKey());
        System.out.println(credentials.getAddress());
    }
}
