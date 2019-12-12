package com.webank.webase.front.gm.runtime;

import org.fisco.bcos.web3j.protocol.Web3j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class Web3jApiTest extends BaseTest {

    @Autowired
    HashMap<Integer, Web3j> web3jMap;
    @Test
    public void getBlockNumber() throws IOException {
        BigInteger blockNumber = web3jMap.get(1).getBlockNumber().send().getBlockNumber();
        assertTrue(blockNumber.compareTo(new BigInteger("0")) >= 0);
    }
}
