/*
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.front.performance;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.performance.entity.ToggleHandle;
import com.webank.webase.front.performance.result.PerformanceData;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.hyperic.sigar.SigarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

/**
 * Host monitor controller
 * monitor of host computer's performance
 * such as cpu, memory, disk etc.
 */

@RestController
@RequestMapping(value = "/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    /**
     * query performance data.
     *
     * @param beginDate beginDate
     * @param endDate endDate
     * @param contrastBeginDate contrastBeginDate
     * @param contrastEndDate contrastEndDate
     * @param gap gap
     * @return
     */
    @ApiOperation(value = "query performance data", notes = "query performance data")
    @ApiImplicitParams({@ApiImplicitParam(name = "beginDate", value = "start time"),
            @ApiImplicitParam(name = "endDate", value = "end time"),
            @ApiImplicitParam(name = "contrastBeginDate", value = "compare start time"),
            @ApiImplicitParam(name = "contrastEndDate", value = "compare end time"),
            @ApiImplicitParam(name = "gap", value = "time gap", dataType = "int")})
    @GetMapping
    public List<PerformanceData> getPerformanceRatio(
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime beginDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime contrastBeginDate,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime contrastEndDate,
            @RequestParam(required = false, defaultValue = "1") int gap) throws Exception {
        List<PerformanceData> performanceList = performanceService.findContrastDataByTime(beginDate,
                endDate, contrastBeginDate, contrastEndDate, gap);
        return performanceList;
    }

    @ApiOperation(value = "获取性能配置信息", notes = "获取性能配置信息")
    @GetMapping(value = "/config")
    public Map<String, String> getPerformanceConfig() throws SigarException, UnknownHostException {
        return performanceService.getConfigInfo();
    }

    @ApiOperation(value = "获取同步任务开关状态", notes = "获取同步任务开关状态")
    @GetMapping(value = "/toggle")
    public Object getScheduledStatus() throws Exception{
        // on is true, off is false
        Boolean onOrOff = performanceService.getToggleStatus();
        String status = onOrOff ? "ON" : "OFF";
        return new BaseResponse(0, "Sync Status is " + status, onOrOff);
    }

    @ApiOperation(value = "切换定时同步任务开关", notes = "切换定时同步任务开关")
    @PostMapping(value = "/toggle")
    public Object toggleScheduledState(@RequestBody ToggleHandle toggleHandle) throws Exception{
        boolean toggle = toggleHandle.isEnable();
        try {// on is true, off is false
            boolean onOrOff = performanceService.toggleSync(toggle);
            String status = onOrOff ? "ON" : "OFF";
            return new BaseResponse(0, "Sync Status is " + status, onOrOff);
        }catch (FrontException e){
            return new BaseResponse(ConstantCode.SYSTEM_ERROR, e.getMessage());
        }

    }
}
