package com.webank.webase.front.contract;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.BaseController;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.file.FileContent;
import com.webank.webase.front.keystore.KeyStoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
 * ContractController.
 *
 */
@Api(value = "/contract", tags = "contract interface")
@Slf4j
@RestController
@RequestMapping(value = "/contract")
public class ContractController extends BaseController {

    @Autowired
    ContractService contractService;
    @Autowired
    KeyStoreService keyService;

    /**
     * saveAbi.
     * 
     * @param reqSendAbi request data
     * @param result checkResult
     * @return
     */
    @ApiOperation(value = "send abi", notes = "send abi")
    @ApiImplicitParam(name = "reqSendAbi", value = "abi info", required = true, dataType = "ReqSendAbi")
    @PostMapping("/abiInfo")
    public BaseResponse sendAbi(@Valid @RequestBody ReqSendAbi reqSendAbi, BindingResult result)throws FrontException {
       // log.info("saveAbi start. ReqSendAbi:[{}]", JSON.toJSONString(reqSendAbi));
        checkParamResult(result);
        return contractService.saveAbi(reqSendAbi);
    }


    /**
     * compile java .
     * @param reqSendAbi request data
     * @param result checkResult
     * @return
     */
    @ApiOperation(value = "compile java", notes = "compile java")
    @ApiImplicitParam(name = "reqSendAbi", value = "abi info", required = true, dataType = "ReqSendAbi")
    @PostMapping("/compile-java")
    public ResponseEntity<InputStreamResource> compileJavaFile(@Valid @RequestBody ReqSendAbi reqSendAbi, @RequestParam String packageName, BindingResult result) throws FrontException, IOException {
        checkParamResult(result);
       FileContent fileContent =  ContractService.compileToJavaFile(reqSendAbi.getContractName(),reqSendAbi.getAbiInfo(),reqSendAbi.getBinaryCode(),packageName);
        return ResponseEntity.ok().headers(headers(fileContent.getFileName())).body(new InputStreamResource(fileContent.getInputStream()));
    }

    /**
     * deploy.
     * 
     * @param reqDeploy request data
     * @param result checkResult
     * @return
     */
    @ApiOperation(value = "contract deploy", notes = "contract deploy")
    @ApiImplicitParam(name = "reqDeploy", value = "contract info", required = true, dataType = "ReqDeploy")
    @PostMapping("/deploy")
    public BaseResponse deploy(@Valid @RequestBody ReqDeploy reqDeploy, BindingResult result) throws Exception {
        log.info("contract deploy start. ReqDeploy:[{}]", JSON.toJSONString(reqDeploy));
        checkParamResult(result);
        return contractService.deploy(reqDeploy);
    }


    @ApiOperation(value = "delete contract abi", notes = "delete contract abi")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractName", value = "contractName", required = true,
                    dataType = "String"),
            @ApiImplicitParam(name = "version", value = "version", required = true,
                    dataType = "String")})
    @DeleteMapping("/deleteAbi/{contractName}/{version:.+}")
    public BaseResponse deleteAbi(@PathVariable String contractName, @PathVariable String version)
            throws FrontException {
        log.info("deleteAbi start. contractName:{} version:{}", contractName, version);
        return contractService.deleteAbi(contractName, version);
    }

    public HttpHeaders headers(String fileName) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename*=UTF-8''"+encode(fileName));
        return httpHeaders;
    }

    private String encode(String name){
        try {
            return URLEncoder.encode(name, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
