package com.webank.webase.front.gm.runtime;

import com.webank.webase.front.channel.test.SpringTestBase;
import com.webank.webase.front.web3api.Web3ApiService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class Web3JApiTestBase extends SpringTestBase {

    @Autowired
    private Web3ApiService web3ApiService;
    @Test
    public void getBlockNumber() throws IOException {
        BigInteger blockNumber = web3ApiService.getWeb3j(1).getBlockNumber().getBlockNumber();
        assertTrue(blockNumber.compareTo(new BigInteger("0")) >= 0);
    }
}
