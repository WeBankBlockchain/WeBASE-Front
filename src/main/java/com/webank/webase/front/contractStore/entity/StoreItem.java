package com.webank.webase.front.contractStore.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * contract store item
 * @author mingzhenliu
 */
@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "unique_storeId", columnNames = {"storeId"})
})
public class StoreItem {
    @Id
    private Long storeId;
    private String storeName;
    private String storeName_en;
    private String storeType;
    @Column(columnDefinition = "mediumtext")
    private String storeIcon;
    @Column(columnDefinition = "mediumtext")
    private String storeDesc;
    @Column(columnDefinition = "mediumtext")
    private String storeDetail;
    @Column(columnDefinition = "mediumtext")
    private String storeDesc_en;
    @Column(columnDefinition = "mediumtext")
    private String storeDetail_en;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
