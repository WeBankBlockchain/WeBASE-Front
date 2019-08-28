package com.webank.webase.front.precompiledapi.sysconf;

import com.webank.webase.front.base.BasePageResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.precompiledapi.sysconf.entity.SystemConfig;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(value = "/sys", tags = "precompiled manage interface")
@Slf4j
@RestController
@RequestMapping(value = "sys")
public class PrecompiledSysConfigController {
    @Autowired
    private PrecompiledSysConfigService precompiledSysConfigService;

    /**
     * System config manage
     * 在service校验存到db
     */
    @GetMapping("config/list")
    public Object querySystemConfigByGroupId(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception {

        List<SystemConfig> list = precompiledSysConfigService.querySysConfigByGroupId(groupId);
        List2Page<SystemConfig> list2Page = new List2Page<>(list, pageSize, pageNumber);
        List<SystemConfig> finalList = list2Page.getPagedList();
        Long totalCount = (long) finalList.size();
        return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
    }

    @ApiOperation(value = "setSysConfigValueByKey", notes = "set system config value by key")
    @ApiImplicitParam(name = "sysConfigHandle", value = "system config info", required = true, dataType = "SysConfigHandle")
    @PostMapping("config")
    public Object setSysConfigValueByKey(@Valid @RequestBody SystemConfig systemConfig, BindingResult bindingResult)throws Exception {
        String key = systemConfig.getConfigKey();
        // tx_count_limit, tx_gas_limit
        if (PrecompiledUtils.TxCountLimit.equals(key) || PrecompiledUtils.TxGasLimit.equals(key)) {
            // post返回透传
            try {
                return precompiledSysConfigService.setSysConfigValueByKey(systemConfig);
            } catch (FrontException e) { //parse error
                return ConstantCode.FAIL_SET_SYSTEM_CONFIG;
            }
        }else {
            return ConstantCode.UNSUPPORT_SYSTEM_CONFIG_KEY;
        }
    }

}
