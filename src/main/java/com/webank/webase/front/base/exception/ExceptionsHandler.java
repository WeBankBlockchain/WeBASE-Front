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
package com.webank.webase.front.base.exception;

import static org.fisco.bcos.web3j.protocol.channel.StatusCode.AccountFrozen;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.AddressAlreadyUsed;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.AlreadyInChain;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.AlreadyKnown;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.BadInstruction;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.BadJumpDestination;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.BadRLP;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.BlockGasLimitReached;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.BlockLimitCheckFail;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.CallAddressError;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.ContractFrozen;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.ErrorInRPC;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.FilterCheckFail;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.GasOverflow;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.InvalidFormat;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.InvalidNonce;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.InvalidSignature;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.InvalidTxChainId;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.InvalidTxGroupId;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.InvalidZeroSignatureFormat;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.MalformedTx;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.NoCallPermission;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.NoDeployPermission;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.NoTxPermission;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.NonceCheckFail;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.NotEnoughCash;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.OutOfGas;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.OutOfGasBase;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.OutOfGasIntrinsic;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.OutOfStack;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.OverGroupMemoryLimit;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.PermissionDenied;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.PrecompiledError;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.RequestNotBelongToTheGroup;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.RevertInstruction;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.StackUnderflow;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.Success;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.TransactionRefused;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.TxPoolIsFull;
import static org.fisco.bcos.web3j.protocol.channel.StatusCode.Unknown;

import com.webank.webase.front.util.JsonUtils;
import java.util.HashMap;
import java.util.Map;

import com.webank.webase.front.base.code.RetCode;
import com.webank.webase.front.util.ErrorCodeHandleUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tx.exceptions.ContractCallException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpStatusCodeException;


/**
 * ExceptionsHandler.
 */
@ControllerAdvice
@Slf4j
public class ExceptionsHandler {

    @Autowired
    ObjectMapper mapper;

    /**
     * myExceptionHandler.
     *
     * @param frontException e
     */
    @ResponseBody
    @ExceptionHandler(value = FrontException.class)
    public ResponseEntity myExceptionHandler(FrontException frontException) throws Exception {
        log.error("catch frontException: {}",  frontException.getMessage());
        Map<String, Object> map = new HashMap<>();
        map.put("data", frontException.getDetail());
        map.put("errorMessage", frontException.getMessage());
        map.put("code", frontException.getRetCode().getCode());
        return ResponseEntity.status(422).body(map);
    }

    /**
     * parameter exception:TypeMismatchException
     */
    @ResponseBody
    @ExceptionHandler(value = TypeMismatchException.class)
    public ResponseEntity typeMismatchExceptionHandler(TypeMismatchException ex) {
        log.warn("catch typeMismatchException", ex);

        Map<String, Object> map = new HashMap<>();
        //  map.put("exception", frontException);
        map.put("errorMessage", ex.getMessage());
        map.put("code", 400);
        log.warn("typeMismatchException return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(400).body(map);
    }

    @ResponseBody
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResponseEntity bindExceptionHandler(ServletRequestBindingException ex) {
        log.warn("catch bindExceptionHandler", ex);

        Map<String, Object> map = new HashMap<>();
        //  map.put("exception", frontException);
        map.put("errorMessage", ex.getMessage());
        map.put("code", 400);
        log.warn("bindExceptionHandler return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(400).body(map);
    }

    /**
     * Method not found in request
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = HttpStatusCodeException.class)
    public ResponseEntity bindExceptionHandler(HttpStatusCodeException ex) {
        log.warn("catch bindExceptionHandler", ex);

        Map<String, Object> map = new HashMap<>();
        map.put("errorMessage", ex.getMessage());
        map.put("code", 400);
        log.warn("bindExceptionHandler return:{}", JsonUtils.toJSONString(map));
        return ResponseEntity.status(400).body(map);
    }


    /**
     * all non-catch exception Handler.
     *
     * @param exc e
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity exceptionHandler(Exception exc) {
        log.info("catch  exception: ", exc);
        RetCode errorDetail = chainErrorHandle(exc.getMessage());
        Map<String, Object> map = new HashMap<>();
        map.put("errorMessage", errorDetail.getMessage());
        map.put("code", errorDetail.getCode());
        return ResponseEntity.status(500).body(map);
    }

    private RetCode chainErrorHandle(String errorMessage) {
        RetCode response = ErrorCodeHandleUtils.handleErrorMsg(errorMessage);
        if (response == null) {
            return new RetCode(500, errorMessage);
        } else {
            return response;
        }
    }
}
