package com.webank.webase.front.precompiledapi.sysconf.entity;

import com.webank.webase.front.base.ConstantCode;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "unique_sys_config", columnNames = {"groupId", "fromAddress",
                "configKey", "configValue"})
})
@Data
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private int groupId;
//    @JsonProperty()
    @NotNull(message = ConstantCode.PARAM_FAIL_FROM_IS_EMPTY)
    private String fromAddress;
    @NotBlank(message = ConstantCode.PARAM_FAIL_CONFIG_KEY_IS_EMPTY)
    private String configKey;
    @NotBlank(message = ConstantCode.PARAM_FAIL_CONFIG_VALUE_IS_EMPTY)
    private String configValue;

}
