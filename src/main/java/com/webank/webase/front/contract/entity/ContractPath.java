package com.webank.webase.front.contract.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import lombok.Data;

@Data
@Entity
@IdClass(ContractPathKey.class)
public class ContractPath implements Serializable {
    private static final long serialVersionUID = 3286516914027062194L;
    @Id
    private Integer groupId;
    @Id
    private String contractPath;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
