package com.webank.webase.front.precompiledapi.precompiledHandle;

import com.webank.webase.front.base.ConstantCode;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class CrudHandle {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private int groupId;
    @NotBlank(message = ConstantCode.PARAM_FAIL_FROM_IS_EMPTY)
    private String fromAddress;
    private String sql;
}
