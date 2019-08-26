package com.webank.webase.front.precompiledapi.precompiledHandle;

import com.webank.webase.front.base.ConstantCode;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class ConsensusHandle {
    @NotNull(message = ConstantCode.PARAM_FAIL_GROUPID_IS_EMPTY)
    private int groupId;
    @NotBlank(message = ConstantCode.PARAM_FAIL_NODE_TYPE_IS_EMPTY)
    private String nodeType;
    @NotBlank(message = ConstantCode.PARAM_FAIL_FROM_IS_EMPTY)
    private String fromAddress;
    @NotBlank(message = ConstantCode.PARAM_FAIL_NODE_ID_IS_EMPTY)
    private String nodeId;
}
