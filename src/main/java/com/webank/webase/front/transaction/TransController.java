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
package com.webank.webase.front.transaction;

import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.transaction.entity.ReqQueryTransHandle;
import com.webank.webase.front.transaction.entity.ReqSignedTransHandle;
import com.webank.webase.front.transaction.entity.ReqTransHandle;
import com.webank.webase.front.transaction.entity.ReqTransHandleWithSign;
import com.webank.webase.front.util.Address;
import com.webank.webase.front.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

import static com.webank.webase.front.base.code.ConstantCode.*;

/**
 * TransController.
 * handle transactions with sign to deploy/call contract
 */
@Api(value = "/trans", tags = "transaction interface")
@Slf4j
@RestController
@RequestMapping(value = "/trans")
public class TransController extends BaseController {

    @Autowired
    TransService transServiceImpl;

    /**
     * transHandle through webase-sign
     * @return
     */
    @ApiOperation(value = "transaction handing", notes = "transaction handing")
    @ApiImplicitParam(name = "reqTransHandle", value = "transaction info", required = true, dataType = "ReqTransHandle")
    @PostMapping("/handleWithSign")
    public Object transHandle(@Valid @RequestBody ReqTransHandleWithSign reqTransHandle, BindingResult result) throws Exception {
        log.info("transHandle start. ReqTransHandle:[{}]", JsonUtils.toJSONString(reqTransHandle));

        Instant startTime = Instant.now();
        log.info("transHandle start startTime:{}", startTime.toEpochMilli());

        checkParamResult(result);
        String address = reqTransHandle.getContractAddress();
        if (StringUtils.isBlank(reqTransHandle.getVersion()) && StringUtils.isBlank(address)) {
            throw new FrontException(VERSION_AND_ADDRESS_CANNOT_ALL_BE_NULL);
        }
        if (address.length() != Address.ValidLen
                || org.fisco.bcos.web3j.abi.datatypes.Address.DEFAULT.toString().equals(address)) {
            throw new FrontException(PARAM_ADDRESS_IS_INVALID);
        }
        Object obj =  transServiceImpl.transHandleWithSign(reqTransHandle);
        log.info("transHandle end  useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return obj;
    }

    @ApiOperation(value = "transaction handle locally", notes = "transaction locally")
    @ApiImplicitParam(name = "reqTransHandle", value = "transaction info", required = true, dataType = "ReqTransHandle")
    @PostMapping("/handle")
    public Object transHandleLocal(@Valid @RequestBody ReqTransHandle reqTransHandle, BindingResult result) throws Exception {
        log.info("transHandleLocal start. ReqTransHandle:[{}]", JsonUtils.toJSONString(reqTransHandle));

        Instant startTime = Instant.now();
        log.info("transHandleLocal start startTime:{}", startTime.toEpochMilli());

        checkParamResult(result);
        String address = reqTransHandle.getContractAddress();
        if (StringUtils.isBlank(reqTransHandle.getVersion()) && StringUtils.isBlank(address)) {
            throw new FrontException(VERSION_AND_ADDRESS_CANNOT_ALL_BE_NULL);
        }
        if (address.length() != Address.ValidLen) {
            throw new FrontException(PARAM_ADDRESS_IS_INVALID);
        }
        Object obj =  transServiceImpl.transHandleLocal(reqTransHandle);
        log.info("transHandleLocal end  useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return obj;
    }


    @ApiOperation(value = "send signed transaction ")
    @ApiImplicitParam(name = "reqSignedTransHandle", value = "transaction info", required = true, dataType = "ReqSignedTransHandle")
    @PostMapping("/signed-transaction")
    public TransactionReceipt sendSignedTransaction(@Valid @RequestBody ReqSignedTransHandle reqSignedTransHandle, BindingResult result) throws Exception {
        log.info("transHandleLocal start. ReqSignedTransHandle:[{}]", JsonUtils.toJSONString(reqSignedTransHandle));

        Instant startTime = Instant.now();
        log.info("transHandleLocal start startTime:{}", startTime.toEpochMilli());

        checkParamResult(result);
        String signedStr = reqSignedTransHandle.getSignedStr();
        if (StringUtils.isBlank(signedStr)) {
            throw new FrontException(ENCODE_STR_CANNOT_BE_NULL);
        }
        TransactionReceipt receipt =  transServiceImpl.sendSignedTransaction(signedStr, reqSignedTransHandle.getSync(),reqSignedTransHandle.getGroupId());
        log.info("transHandleLocal end  useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return receipt;
    }

    @ApiOperation(value = "send query transaction ")
    @ApiImplicitParam(name = "reqQueryTransHandle", value = "transaction info", required = true, dataType = "ReqQueryTransHandle")
    @PostMapping("/query-transaction")
    public Object sendQueryTransaction(@Valid @RequestBody ReqQueryTransHandle reqQueryTransHandle, BindingResult result)   {
        log.info("transHandleLocal start. ReqQueryTransHandle:[{}]", JsonUtils.toJSONString(reqQueryTransHandle));

        Instant startTime = Instant.now();
        log.info("transHandleLocal start startTime:{}", startTime.toEpochMilli());

        checkParamResult(result);
        String encodeStr = reqQueryTransHandle.getEncodeStr();
        if (StringUtils.isBlank(encodeStr)) {
            throw new FrontException(ENCODE_STR_CANNOT_BE_NULL);
        }
        Object obj =  transServiceImpl.sendQueryTransaction(encodeStr, reqQueryTransHandle.getContractAddress(),reqQueryTransHandle.getFuncName(),reqQueryTransHandle.getContractAbi(),reqQueryTransHandle.getGroupId(),reqQueryTransHandle.getUserAddress());
        log.info("transHandleLocal end  useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return obj;
    }

}
