/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.precompiledapi.sysconf;

import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.precompiledapi.entity.ResSystemConfig;
import com.webank.webase.front.precompiledapi.entity.SystemConfigHandle;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * System config value controller
 * manage tx_count and gas_limit
 */
@Api(value = "/sys", tags = "precompiled manage interface")
@Slf4j
@RestController
@RequestMapping(value = "sys")
public class PrecompiledSysConfigController{
    @Autowired
    private PrecompiledSysConfigService precompiledSysConfigService;

    /**
     * System config manage
     */
    @GetMapping("config/list")
    public Object querySystemConfigByGroupId(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception {
        Instant startTime = Instant.now();
        log.info("start querySystemConfigByGroupId startTime:{}, groupId:{}",
                startTime.toEpochMilli(), groupId);
        List<ResSystemConfig> list = new ArrayList<>();
        try {
            list = precompiledSysConfigService.querySysConfigByGroupId(groupId);
            log.info("end querySystemConfigByGroupId useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), list);
        } catch (Exception e) { //get sys config fail
            log.error("error querySystemConfigByGroupId exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_SET_SYSTEM_CONFIG, e.getMessage());
        }
        List2Page<ResSystemConfig> list2Page = new List2Page<>(list, pageSize, pageNumber);
        List<ResSystemConfig> finalList = list2Page.getPagedList();
        long totalCount = (long) list.size();
        log.info("end querySystemConfigByGroupId. finalList:{}", finalList);
        return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
    }

    @ApiOperation(value = "setSysConfigValueByKey", notes = "set system config value by key")
    @ApiImplicitParam(name = "systemConfigHandle", value = "system config info", required = true, dataType = "SystemConfigHandle")
    @PostMapping("config")
    public Object setSysConfigValueByKey(@Valid @RequestBody SystemConfigHandle systemConfigHandle)throws Exception {
        Instant startTime = Instant.now();
        log.info("start querySystemConfigByGroupId startTime:{}, systemConfigHandle:{}",
                startTime.toEpochMilli(), systemConfigHandle);
        String key = systemConfigHandle.getConfigKey();
        // tx_count_limit, tx_gas_limit
        if (!PrecompiledUtils.TxCountLimit.equals(key) && !PrecompiledUtils.TxGasLimit.equals(key)
            && !PrecompiledUtils.ConsensusTimeout.equals(key)) {
            log.error("end setSysConfigValueByKey. Exception:{}",
                    ConstantCode.UNSUPPORTED_SYSTEM_CONFIG_KEY.getMessage());
            return ConstantCode.UNSUPPORTED_SYSTEM_CONFIG_KEY;
        }
        // post返回透传
        try {
            Object res = precompiledSysConfigService.setSysConfigValueByKey(systemConfigHandle);
            log.info("end querySystemConfigByGroupId useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) { //parse error
            log.error("end setSysConfigValueByKey. Exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_SET_SYSTEM_CONFIG, e.getMessage());
        }
    }

}
