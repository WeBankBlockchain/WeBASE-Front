package com.webank.webase.front.monitor;

import com.webank.webase.front.performance.result.PerformanceData;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Slf4j
@RestController
@RequestMapping(value = "/chain")
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    @ApiOperation(value = "查询链上数据", notes = "查询链上数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "beginDate", value = "开始时间"),
            @ApiImplicitParam(name = "endDate", value = "结束时间"),
            @ApiImplicitParam(name = "contrastBeginDate", value = "对比开始时间"),
            @ApiImplicitParam(name = "contrastEndDate", value = "对比结束时间"),
            @ApiImplicitParam(name = "gap", value = "时间间隔", dataType = "int")
    })
    @GetMapping
    public List<PerformanceData> getChainMonitor(@RequestParam(required= false) @DateTimeFormat(iso=DATE_TIME) LocalDateTime beginDate,
                                                 @RequestParam(required= false) @DateTimeFormat(iso=DATE_TIME) LocalDateTime endDate,
                                                 @RequestParam(required= false) @DateTimeFormat(iso=DATE_TIME) LocalDateTime contrastBeginDate,
                                                 @RequestParam(required= false) @DateTimeFormat(iso=DATE_TIME) LocalDateTime contrastEndDate,
                                                 @RequestParam(required = false, defaultValue = "1") int gap) throws Exception {
        List<PerformanceData> performanceList = monitorService.findContrastDataByTime(beginDate,endDate,contrastBeginDate,contrastEndDate,gap);
        return performanceList;
    }
}
