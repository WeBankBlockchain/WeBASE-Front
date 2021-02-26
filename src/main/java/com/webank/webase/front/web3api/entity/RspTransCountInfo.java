package com.webank.webase.front.web3api.entity;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * for node-manager
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RspTransCountInfo {
   private BigInteger txSum;
   private BigInteger blockNumber;
   private BigInteger failedTxSum;
}