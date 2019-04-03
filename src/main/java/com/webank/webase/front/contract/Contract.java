package com.webank.webase.front.contract;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String contractName;
    private String version;
    private String abi;
    private String binary;
    private String sol;
    private String contractAddress;
    private LocalDateTime deployTime;
    private String deployedUser;
    private String funcParam;
    private int groupId;
}
