package com.webank.webase.front.contractStore.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * contract item
 * @author mingzhenliu
 */
@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "unique_contractId", columnNames = {"contractId"})
})
public class ContractItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long contractId;
    private Long contractFolderId;
    private String contractName;
    @Column(columnDefinition = "mediumtext")
    private String contractDesc;
    @Column(columnDefinition = "longtext")
    private String contractDetail;
    @Column(columnDefinition = "mediumtext")
    private String contractDesc_en;
    @Column(columnDefinition = "longtext")
    private String contractDetail_en;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
