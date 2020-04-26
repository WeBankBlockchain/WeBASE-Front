package com.webank.webase.front.gm.runtime;

import com.webank.webase.front.web3api.Web3ApiService;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.config.SystemConfigService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import static org.junit.Assert.assertTrue;

public class SendGMTxTest extends BaseTest {

    @Autowired
    private Web3ApiService web3ApiService;

    @Test
    public void testSystemConfigService() throws Exception {
        Credentials credentials2 =
                GenCredential.create("a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f6");

        // web3j
        SystemConfigService systemConfigSerivce = new SystemConfigService(web3ApiService.getWeb3j(1), credentials2);
        systemConfigSerivce.setValueByKey("tx_count_limit", "2000");
        String value = web3ApiService.getWeb3j(1).getSystemConfigByKey("tx_count_limit").send().getSystemConfigByKey();
        System.out.println(value);
        assertTrue("2000".equals(value));
    }
}
