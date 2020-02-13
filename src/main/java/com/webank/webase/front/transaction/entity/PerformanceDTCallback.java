package com.webank.webase.front.transaction.entity;

import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.transaction.TransService;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.omg.IOP.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class PerformanceDTCallback extends TransactionSucCallback {
    private Long startTime = System.currentTimeMillis();
    private int status = 0 ;

    static Logger logger = LoggerFactory.getLogger(PerformanceDTCallback.class);

    public PerformanceDTCallback() {}

    @Override
    public void onResponse(TransactionReceipt receipt) {
        Long cost = System.currentTimeMillis() - startTime;
        status = 1;
        if(!receipt.isStatusOK()) {
                  logger.info("{}cost time :{}, status:{}",receipt.getTransactionHash(), cost , receipt.getStatus());
                  Constants.error.incrementAndGet();

        }
   //     logger.info("{}cost time :{}, status:{}",receipt.getTransactionHash(), cost , receipt.getStatus());
    }
    public int getStatus() {
        return status;
    }

}
