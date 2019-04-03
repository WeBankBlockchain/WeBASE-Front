package com.webank.webase.front.transLog;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class TransLog {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String contractName;
    private String contractVersion;
    private String contractAddress;
    private int  group;
    private LocalDateTime transTime;
    private String funcParam;
    private TransType type;
}
