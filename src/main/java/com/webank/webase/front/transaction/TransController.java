package com.webank.webase.front.transaction;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseController;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.exception.FrontException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Copyright 2012-2019 the original author or authors.
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

/**
 * TransController.
 *
 */
@Api(value = "/trans", tags = "transaction interface")
@Slf4j
@RestController
@RequestMapping(value = "/trans")
public class TransController extends BaseController {

    @Autowired
    TransService transServiceImpl;

    /**
     * transHandle.
     * 
     * @param reqTransHandle request
     * @param result checkResult
     * @return
     */
    @ApiOperation(value = "transaction handing", notes = "transaction handing")
    @ApiImplicitParam(name = "reqTransHandle", value = "transaction info", required = true,
            dataType = "ReqTransHandle")
    @PostMapping("/handle")
    public Object transHandle(@Valid @RequestBody ReqTransHandle reqTransHandle, BindingResult result) throws Exception {
        log.info("transHandle start. ReqTransHandle:[{}]", JSON.toJSONString(reqTransHandle));
        checkParamResult(result);
        return transServiceImpl.transHandle(reqTransHandle);
    }
}
