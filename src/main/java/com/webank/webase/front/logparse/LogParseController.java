/*
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.front.logparse;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.logparse.entity.NetWorkData;
import com.webank.webase.front.logparse.entity.TxGasData;
import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/charging")
public class LogParseController extends BaseController {

    @Autowired
    private LogParseService logParseService;

    @ApiOperation(value = "Get NetWork Data")
    @GetMapping("/getNetWorkData")
    public BasePageResponse getNetWorkData(@RequestParam(defaultValue = "1") Integer groupId,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime beginDate,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime endDate) {

        Instant startTime = Instant.now();
        log.info("getNetWorkData start. groupId:{}", groupId,
                startTime.toEpochMilli());

        Page<NetWorkData> page =
                logParseService.getNetWorkData(groupId, pageNumber, pageSize, beginDate, endDate);

        BasePageResponse response = new BasePageResponse(ConstantCode.RET_SUCCEED);
        response.setTotalCount(page.getTotalElements());
        response.setData(page.getContent());

        log.info("getNetWorkData end  useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    @ApiOperation(value = "Get Transaction Gas Data")
    @GetMapping("/getTxGasData")
    public BasePageResponse getTxGasData(@RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DATE_TIME) LocalDateTime beginDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String transHash) {

        Instant startTime = Instant.now();
        log.info("getTxGasData start. groupId:{}", groupId, startTime.toEpochMilli());

        Page<TxGasData> page = logParseService.getTxGasData(groupId, pageNumber, pageSize,
                beginDate, endDate, transHash);

        BasePageResponse response = new BasePageResponse(ConstantCode.RET_SUCCEED);
        response.setTotalCount(page.getTotalElements());
        response.setData(page.getContent());

        log.info("getTxGasData end useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    @ApiOperation(value = "Delete Data")
    @DeleteMapping("/deleteData")
    public BaseResponse deleteData(@RequestParam(defaultValue = "1") int groupId,
            @RequestParam int type,
            @RequestParam @DateTimeFormat(iso = DATE_TIME) LocalDateTime keepEndDate) {

        Instant startTime = Instant.now();
        log.info("deleteData start. groupId:{} type:{}", groupId, type,
                startTime.toEpochMilli());

        int count = logParseService.deleteData(groupId, type, keepEndDate);
        BaseResponse BaseResponse = new BaseResponse(ConstantCode.RET_SUCCEED);
        BaseResponse.setData(count);

        log.info("deleteData end  useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return BaseResponse;
    }
}
