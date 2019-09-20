package com.webank.webase.front.precompiledapi.permission;

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.front.base.ConstantCode;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionHandle {
    // defaultValue = "1"
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private int groupId;
    // @NotNull(message = ConstantCode.PARAM_FAIL_PERMISSION_TYPE_IS_EMPTY)
    private String permissionType;
    @NotNull(message = ConstantCode.PARAM_FAIL_FROM_IS_EMPTY)
    private String fromAddress;
    @NotNull(message = ConstantCode.PARAM_FAIL_USER_IS_EMPTY)
    private String address;
    private String tableName;
    private PermissionState permissionState;
}
