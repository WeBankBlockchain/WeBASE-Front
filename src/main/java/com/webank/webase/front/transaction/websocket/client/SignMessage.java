package com.webank.webase.front.transaction.websocket.client;


import org.fisco.bcos.web3j.protocol.core.Response;

/**
 * eth_sign.
 */
public class SignMessage extends Response<String> {
    public String getSignature() {
        return getRawResponse();
    }
}
