/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.monitor;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.Web3Config;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.monitor.entity.GroupSizeInfo;
import com.webank.webase.front.monitor.entity.Monitor;
import com.webank.webase.front.performance.result.PerformanceData;
import com.webank.webase.front.util.CommonUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Node monitor controller distinguished from host monitor: performance
 */
@Slf4j
@RestController
@RequestMapping(value = "/chain")
public class MonitorController {

    @Autowired
    private MonitorService monitorService;
    @Autowired
    private Web3Config web3Config;

    @ApiOperation(value = "查询链上数据", notes = "查询链上数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "beginDate", value = "开始时间"),
            @ApiImplicitParam(name = "endDate", value = "结束时间"),
            @ApiImplicitParam(name = "contrastBeginDate", value = "对比开始时间"),
            @ApiImplicitParam(name = "contrastEndDate", value = "对比结束时间"),
            @ApiImplicitParam(name = "gap", value = "时间间隔", dataType = "int")})
    @GetMapping
    public List<PerformanceData> getChainMonitor(
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime beginDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime contrastBeginDate,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime contrastEndDate,
            @RequestParam(required = false, defaultValue = "1") int gap,
            @RequestParam(defaultValue = "1") int groupId) {
        Instant startTime = Instant.now();
        log.info("getChainMonitor startTime:{} groupId:[{}]", groupId,
                startTime.toEpochMilli());

        List<PerformanceData> performanceList = monitorService.findContrastDataByTime(groupId,
                beginDate, endDate, contrastBeginDate, contrastEndDate, gap);

        log.info("getChainMonitor end. useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return performanceList;
    }

    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/pagingQuery")
    public BasePageResponse pagingQuery(@RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime beginDate,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime endDate) {
        Instant startTime = Instant.now();
        log.info("pagingQuery start. groupId:{}", groupId, startTime.toEpochMilli());

        Page<Monitor> page =
                monitorService.pagingQuery(groupId, pageNumber, pageSize, beginDate, endDate);

        BasePageResponse response = new BasePageResponse(ConstantCode.RET_SUCCEED);
        response.setTotalCount(page.getTotalElements());
        response.setData(page.getContent());

        log.info("pagingQuery end. useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    @ApiOperation(value = "检查节点进程连接")
    @GetMapping("/checkNodeProcess")
    public boolean checkNodeProcess() {
        log.info("checkNodeProcess.");
        return CommonUtils.checkConnect(web3Config.getIp(),
                Integer.valueOf(web3Config.getChannelPort()));
    }

    @ApiOperation(value = "获取群组大小信息")
    @GetMapping("/getGroupSizeInfos")
    public List<GroupSizeInfo> getGroupSizeInfos() {
        Instant startTime = Instant.now();
        log.info("getGroupSizeInfos start:{}", startTime.toEpochMilli());
        List<GroupSizeInfo> groupSizeInfos = monitorService.getGroupSizeInfos();
        log.info("getGroupSizeInfos end  useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return groupSizeInfos;
    }

    /**
     * get by less than begin or larger than end order by id desc
     */
    @ApiOperation(value = "开区间分页查询", notes = "分页查询，获取时间范围以外的")
    @GetMapping("/pagingQuery/stat")
    public BasePageResponse getNodeMonitorForStat(@RequestParam(defaultValue = "1") int groupId,
        @RequestParam(defaultValue = "1") Integer pageNumber,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DATE_TIME) LocalDateTime beginDate,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DATE_TIME) LocalDateTime endDate) {
        Instant startTime = Instant.now();
        log.info("getNodeMonitorForStat start. groupId:{},startTime:{}", groupId, startTime.toEpochMilli());
        if (beginDate == null && endDate == null) {
            log.error("getNodeMonitorForStat beginDate endDate cannot be both null!");
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
        BasePageResponse response =
            monitorService.pagingQueryStat(groupId, pageNumber, pageSize, beginDate, endDate);

        log.info("getNodeMonitorForStat end. useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }
}
