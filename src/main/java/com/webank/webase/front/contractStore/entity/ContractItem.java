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
    private Long contractId;
    private Long contractFolderId;
    private String contractName;
    @Column(columnDefinition = "mediumtext")
    private String contractDesc;
    @Column(columnDefinition = "mediumtext")
    private String contractSrc;
    @Column(columnDefinition = "mediumtext")
    private String contractDesc_en;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
