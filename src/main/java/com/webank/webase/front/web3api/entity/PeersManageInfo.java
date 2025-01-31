package com.webank.webase.front.web3api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeersManageInfo {
    private String peerIpPort;
    private List<String> peers;
}
