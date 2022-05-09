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
package com.webank.webase.front.precntauth.precompiled.sysconf;

import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.precntauth.precompiled.sysconf.entity.ReqSetSysConfigInfo;
import com.webank.webase.front.util.PrecompiledUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * System config controller
 * manage tx_count and gas_limit
 */
@Api(value = "precntauth/precompiled/sys", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/sys")
public class SysConfigController {

    @Autowired
    private SysConfigServiceInWebase SysConfigServiceInWebase;

    @ApiOperation(value = "querySystemConfigList", notes = "query system config lis")
    @ApiImplicitParam(name = "groupId", value = "query system config list", required = true, dataType = "String")
    @GetMapping("config/list")
    public List<Object> querySystemConfigList(@RequestParam String groupId) throws Exception {
        List<Object> res = null;
        Instant startTime = Instant.now();
        log.info("start querySystemConfigByGroupId startTime:{}, groupId:{}",
            startTime.toEpochMilli(), groupId);
        try {
            res = SysConfigServiceInWebase.querySysConfigByGroupId(groupId);
            log.info("end querySystemConfigByGroupId useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res.toString());
        } catch (Exception e) {
            //get sys config fail
            log.error("error querySystemConfigByGroupId exception:[]", e);
            BaseResponse base = new BaseResponse(ConstantCode.FAIL_SET_SYSTEM_CONFIG,
                e.getMessage());
            res.add(base);
            return res;
        }
        return res;
    }

    @ApiOperation(value = "setSysConfigValueByKey", notes = "set system config value by key")
    @ApiImplicitParam(name = "reqSetSysConfigInfo", value = "system config info", required = true, dataType = "ReqSetSysConfigInfo")
    @PostMapping("config")
    public Object setSysConfigValueByKey(
        @Valid @RequestBody ReqSetSysConfigInfo reqSetSysConfigInfo) {
        Instant startTime = Instant.now();
        log.info("start querySystemConfigByGroupId startTime:{}, systemConfigHandle:{}",
            startTime.toEpochMilli(), reqSetSysConfigInfo);
        String key = reqSetSysConfigInfo.getConfigKey();
        // tx_count_limit, tx_gas_limit
        if (!PrecompiledUtils.TxCountLimit.equals(key) && !PrecompiledUtils.TxGasLimit.equals(
            key)) {
            log.error("end setSysConfigValueByKey. Exception:{}",
                ConstantCode.UNSUPPORTED_SYSTEM_CONFIG_KEY.getMessage());
            return ConstantCode.UNSUPPORTED_SYSTEM_CONFIG_KEY;
        }
        // post返回透传
        try {
            Object res = SysConfigServiceInWebase.setSysConfigValueByKey(reqSetSysConfigInfo);
            log.info("end querySystemConfigByGroupId useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) { //parse error
            log.error("end setSysConfigValueByKey. Exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_SET_SYSTEM_CONFIG, e.getMessage());
        }
    }
}
