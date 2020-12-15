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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long storeId;
    private String storeName;
    private String storeType;
    @Column(columnDefinition = "mediumtext")
    private String storeIcon;
    @Column(columnDefinition = "mediumtext")
    private String storeDesc;
    @Column(columnDefinition = "longtext")
    private String storeDetail;
    @Column(columnDefinition = "mediumtext")
    private String storeDesc_en;
    @Column(columnDefinition = "longtext")
    private String storeDetail_en;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
