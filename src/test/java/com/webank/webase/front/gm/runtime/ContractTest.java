package com.webank.webase.front.gm.runtime;

import com.webank.webase.front.gm.basic.HelloWorldGM;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class ContractTest extends BaseTest {

    @Autowired
    private HashMap<Integer, Web3j> web3jMap;

    public static BigInteger gasPrice = new BigInteger("3000000");
    public static BigInteger gasLimit = new BigInteger("300000000");
    @Test
    public void deployAndCallHelloWorld() throws Exception {
        Credentials credentials2 =
                GenCredential.create("a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f6");
        System.out.println("start____________________");
        // deploy contract
        System.out.println(web3jMap.get(1).getBlockNumber().send().getBlockNumber());
        HelloWorldGM helloWorldGM =
                HelloWorldGM.deploy(
                        web3jMap.get(1),
                        credentials2,
                        new StaticGasProvider(gasPrice, gasLimit))
                        .send();
        if (helloWorldGM != null) {
            System.out.println("HelloWorld address is: " + helloWorldGM.getContractAddress());
            // call set function
            helloWorldGM.set("Hello, World!").send();
            // call get function
            String result = helloWorldGM.get().send();
            System.out.println(result);
            assertTrue("Hello, World!".equals(result));
        }
    }
}
