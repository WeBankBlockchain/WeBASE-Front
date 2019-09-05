package com.webank.webase.front.precompiledapi.sysconf;

import com.webank.webase.front.base.ConstantCode;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
public class SystemConfigHandle {

    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private int groupId;
    @NotNull(message = ConstantCode.PARAM_FAIL_FROM_IS_EMPTY)
    private String fromAddress;
    @NotBlank(message = ConstantCode.PARAM_FAIL_CONFIG_KEY_IS_EMPTY)
    private String configKey;
    @NotBlank(message = ConstantCode.PARAM_FAIL_CONFIG_VALUE_IS_EMPTY)
    private String configValue;

}
