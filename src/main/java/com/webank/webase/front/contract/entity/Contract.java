package com.webank.webase.front.contract.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "unique_contract", columnNames = {"groupId", "contractPath",
        "contractName"})
})
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String contractPath;
    private String version;
    private String contractName;
    private Integer contractStatus;
    private Integer groupId;
    @Column(columnDefinition = "text")
    private String contractSource;
    @Column(columnDefinition = "text")
    private String contractAbi;
    @Column(columnDefinition = "text")
    private String contractBin;
    @Column(columnDefinition = "text")
    private String bytecodeBin;
    private String contractAddress;
    private LocalDateTime deployTime;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
