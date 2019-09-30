package com.webank.webase.front.precompiledapi.permission;

import com.webank.webase.front.base.ConstantCode;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionState {
    // 0 means revoke, 1 means grant
    @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_STATE_ALL_CONNOT_BE_EMPTY)
    private int deployAndCreate;
    @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_STATE_ALL_CONNOT_BE_EMPTY)
    private int cns;
    @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_STATE_ALL_CONNOT_BE_EMPTY)
    private int sysConfig;
    @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_STATE_ALL_CONNOT_BE_EMPTY)
    private int node;
}
