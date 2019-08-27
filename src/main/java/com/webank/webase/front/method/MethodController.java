/**
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.front.method;

import java.time.Duration;
import java.time.Instant;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseController;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.method.entity.NewMethodInputParam;
import com.webank.webase.front.method.entity.TbMethod;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("method")
public class MethodController extends BaseController {

    @Autowired
    private MethodService methodService;

    /**
     * add method info.
     */
    @PostMapping(value = "/add")
    public BaseResponse addMethod(@RequestBody @Valid NewMethodInputParam newMethodInputParam,
        BindingResult result) throws FrontException {
        checkParamResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.RET_SUCCEED);
        Instant startTime = Instant.now();
        log.info("start addMethod. startTime:{} newMethodInputParam:{}",
            startTime.toEpochMilli(), JSON.toJSONString(newMethodInputParam));

        methodService.saveMethod(newMethodInputParam);

        log.info("end addMethod. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query by methodId.
     */
    @GetMapping(value = "findById/{groupId}/{methodId}")
    public BaseResponse getBymethodId(@PathVariable("groupId") Integer groupId,
        @PathVariable("methodId") String methodId) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.RET_SUCCEED);
        Instant startTime = Instant.now();
        log.info("start getBymethodId. startTime:{} groupId:{} methodId:{}",
            startTime.toEpochMilli(), groupId, methodId);

        TbMethod tbMethod = methodService.getByMethodId(methodId, groupId);
        baseResponse.setData(tbMethod);

        log.info("end getBymethodId. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }
}
