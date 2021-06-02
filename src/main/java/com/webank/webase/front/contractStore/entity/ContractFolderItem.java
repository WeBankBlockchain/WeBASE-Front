package com.webank.webase.front.contractStore.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * contract folder item
 * @author mingzhenliu
 */
@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "unique_contractId", columnNames = {"contractFolderId"})
})
public class ContractFolderItem {
    @Id
    private Long contractFolderId;
    private Long storeId;
    private String contractFolderName;
    @Column(columnDefinition = "mediumtext")
    private String contractFolderDesc;
    @Column(columnDefinition = "mediumtext")
    private String contractFolderDetail;
    @Column(columnDefinition = "mediumtext")
    private String contractFolderDesc_en;
    @Column(columnDefinition = "mediumtext")
    private String contractFolderDetail_en;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
