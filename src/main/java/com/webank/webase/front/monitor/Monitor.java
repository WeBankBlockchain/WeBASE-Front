package com.webank.webase.front.monitor;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigInteger;

@Entity
@Data
public class Monitor {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private BigInteger blockHeight;
    private BigInteger pbftView;
    private BigInteger pendingTransactionCount;
    private int groupId=1;
    private Long timestamp;
}
