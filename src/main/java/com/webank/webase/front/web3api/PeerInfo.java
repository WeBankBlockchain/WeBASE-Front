package com.webank.webase.front.web3api;

import lombok.Data;

import java.math.BigInteger;

@Data
public class PeerInfo {
    private BigInteger blockNumber;
    private String pbftView;
    private String ipAndPort;
    private String nodeId;
}
