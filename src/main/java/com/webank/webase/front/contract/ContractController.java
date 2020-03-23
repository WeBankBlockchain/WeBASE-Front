/**
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
package com.webank.webase.front.contract;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.contract.entity.ContractPath;
import com.webank.webase.front.contract.entity.FileContentHandle;
import com.webank.webase.front.contract.entity.ReqContractCompile;
import com.webank.webase.front.contract.entity.ReqContractPath;
import com.webank.webase.front.contract.entity.ReqContractSave;
import com.webank.webase.front.contract.entity.ReqDeploy;
import com.webank.webase.front.contract.entity.ReqDeployWithSign;
import com.webank.webase.front.contract.entity.ReqMultiContractCompile;
import com.webank.webase.front.contract.entity.ReqPageContract;
import com.webank.webase.front.contract.entity.ReqSendAbi;
import com.webank.webase.front.contract.entity.RspContractCompile;
import com.webank.webase.front.contract.entity.RspMultiContractCompile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import com.webank.webase.front.contract.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


/**
 * ContractController.
 */
@Api(value = "/contract", tags = "contract interface")
@Slf4j
@RestController
@RequestMapping(value = "/contract")
public class ContractController extends BaseController {

    @Autowired
    ContractService contractService;

    /**
     * deploy.
     *
     * @param reqDeploy request data
     * @param result checkResult
     */
    @ApiOperation(value = "contract deploy", notes = "contract deploy")
    @ApiImplicitParam(name = "reqDeploy", value = "contract info", required = true, dataType = "ReqDeploy")
    @PostMapping("/deployWithSign")
    public String deploy(@Valid @RequestBody ReqDeploy reqDeploy, BindingResult result)
        throws Exception {
        log.info("contract deployWithSign start. ReqDeploy:[{}]", JSON.toJSONString(reqDeploy));
        checkParamResult(result);
        if (StringUtils.isBlank(reqDeploy.getSignUserId())) {
            log.error("contract deployWithSign error: signUserId is empty");
            throw new FrontException(ConstantCode.PARAM_FAIL_SIGN_USER_ID_IS_EMPTY);
        }
        String contractAddress = contractService.caseDeploy(reqDeploy, false);
        log.info("success deployWithSign. result:{}", contractAddress);
        return contractAddress;
    }

    /**
     * deploy locally not through sign
     */
    @ApiOperation(value = "contract deploy locally", notes = "contract deploy")
    @ApiImplicitParam(name = "reqDeploy", value = "contract info", required = true, dataType = "ReqDeploy")
    @PostMapping("/deploy")
    public String deployLocal(@Valid @RequestBody ReqDeploy reqDeploy, BindingResult result)
            throws Exception {
        log.info("contract deployLocal start. ReqDeploy:[{}]", JSON.toJSONString(reqDeploy));
        checkParamResult(result);
        if (StringUtils.isBlank(reqDeploy.getUser())) {
            log.error("contract deployLocal error: user(address) is empty");
            throw new FrontException(ConstantCode.PARAM_FAIL_USER_IS_EMPTY);
        }
        String contractAddress = contractService.caseDeploy(reqDeploy, true);
        log.info("success deployLocal. result:{}", contractAddress);
        return contractAddress;
    }


    /**
     * compile java .
     *
     * @param param request data
     * @param result checkResult
     */
    @ApiOperation(value = "compile java", notes = "compile java")
    @PostMapping("/compile-java")
    public ResponseEntity<InputStreamResource> compileJavaFile(@Valid @RequestBody ReqSendAbi param,
            BindingResult result) throws FrontException, IOException {
        log.info("compileJavaFile start. reqSendAbi:{}", JSON.toJSONString(param));
        checkParamResult(result);
        FileContentHandle fileContentHandle = ContractService
            .compileToJavaFile(param.getContractName(), param.getAbiInfo(), param.getContractBin(),
                param.getPackageName());
        return ResponseEntity.ok().headers(headers(fileContentHandle.getFileName()))
                .body(new InputStreamResource(fileContentHandle.getInputStream()));
    }


    @ApiOperation(value = "delete contract abi", notes = "delete contract abi")
    @DeleteMapping("/deleteAbi/{contractName}/{version:.+}")
    public BaseResponse deleteAbi(@PathVariable String contractName, @PathVariable String version)
            throws FrontException {
        log.info("deleteAbi start. contractName:{} version:{}", contractName, version);
        return contractService.deleteAbi(contractName, version);
    }

    /**
     * sendAbi.
     *
     * @param reqSendAbi request data
     * @param result checkResult
     */
    @ApiOperation(value = "send abi", notes = "send abi")
    @PostMapping("/abiInfo")
    public ResponseEntity sendAbi(@Valid @RequestBody ReqSendAbi reqSendAbi, BindingResult result)
            throws FrontException {
        log.info("sendAbi start. ReqSendAbi:[{}]", JSON.toJSONString(reqSendAbi));
        checkParamResult(result);
        if (Objects.isNull(reqSendAbi.getGroupId())) {
            log.warn("fail sendAbi. groupId is null");
            throw new FrontException(ConstantCode.PARAM_FAIL_GROUP_ID_IS_EMPTY);
        }
        contractService.sendAbi(reqSendAbi);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/cns")
    public String getAddressByContractNameAndVersion(@RequestParam int groupId,
            @RequestParam String name, @RequestParam String version) {
        log.info("cns start. groupId:{} name:{} version:{}", groupId, name, version);
        return contractService.getAddressByContractNameAndVersion(groupId, name, version);
    }

    public HttpHeaders headers(String fileName) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename*=UTF-8''" + encode(fileName));
        return httpHeaders;
    }

    private String encode(String name) {
        try {
            return URLEncoder.encode(name, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * save contract.
     */
    @ApiOperation(value = "save contract", notes = "save contract")
    @PostMapping(value = "/save")
    public Contract saveContract(@RequestBody @Valid ReqContractSave contract, BindingResult result)
            throws FrontException {
        log.info("saveContract start. contract:{}", JSON.toJSONString(contract));
        checkParamResult(result);
        return contractService.saveContract(contract);
    }

    /**
     * delete by contractId.
     */
    @ApiOperation(value = "delete by contractId", notes = "delete by contractId")
    @DeleteMapping("/{groupId}/{contractId}")
    public BaseResponse deleteByContractId(@PathVariable Integer groupId,
            @PathVariable Long contractId) throws FrontException {
        log.info("deleteByContractId start. groupId:{} contractId:{}", groupId, contractId);
        contractService.deleteContract(contractId, groupId);
        return new BaseResponse(ConstantCode.RET_SUCCEED);
    }

    /**
     * query list of contract.
     */
    @ApiOperation(value = "query list of contract", notes = "query list of contract ")
    @PostMapping(value = "/contractList")
    public BasePageResponse findByPage(@RequestBody @Valid ReqPageContract req,
            BindingResult result) throws FrontException, IOException {
        log.info("findByPage start. ReqPageContract:{}", JSON.toJSONString(req));
        checkParamResult(result);
        Page<Contract> page = contractService.findContractByPage(req);
        BasePageResponse response = new BasePageResponse(ConstantCode.RET_SUCCEED);
        response.setTotalCount(page.getTotalElements());
        response.setData(page.getContent());
        return response;
    }

    /**
     * verify that if the contract changed.
     */
    @ApiOperation(value = "verify contract", notes = "verify that if the contract changed")
    @GetMapping("/ifChanged/{groupId}/{contractId}")
    public boolean ifChanged(@PathVariable Integer groupId, @PathVariable Long contractId) {
        log.info("ifChanged start. groupId:{} contractId:{}", groupId, contractId);
        return contractService.verifyContractChange(contractId, groupId);
    }

    /**
     * single contract compile.
     */
    @ApiOperation(value = "compile contract", notes = "compile contract")
    @PostMapping(value = "/contractCompile")
    public RspContractCompile contractCompile(@RequestBody @Valid ReqContractCompile req,
            BindingResult result) throws FrontException {
        log.info("contractCompile start. param:{}", JSON.toJSONString(req));
        checkParamResult(result);
        return contractService.contractCompile(req.getContractName(), req.getSolidityBase64());
    }

    /**
     * multiple contract compile.
     */
    @ApiOperation(value = "multiple contract compile", notes = "multiple contract compile")
    @PostMapping(value = "/multiContractCompile")
    public List<RspMultiContractCompile> multiContractCompile(
            @RequestBody @Valid ReqMultiContractCompile req, BindingResult result)
            throws FrontException, IOException {
        Instant startTime = Instant.now();
        log.info("start multiContractCompile startTime:{} param:{}", startTime.toEpochMilli(),
                JSON.toJSONString(req));
        checkParamResult(result);
        List<RspMultiContractCompile> response = contractService.multiContractCompile(req);
        log.info("end multiContractCompile useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    @PostMapping(value = "/addContractPath")
    public ContractPath addContractPath(@RequestBody @Valid ReqContractPath req) {
        log.info("addContractPath start. param:{}", JSON.toJSONString(req));
        return contractService.addContractPath(req);
    }

    /**
     * query by groupId.
     */
    @GetMapping(value = "/findPathList/{groupId}")
    public List<ContractPath> findPathList(@PathVariable("groupId") Integer groupId) {
        log.info("start findPathList. groupId:{}", groupId);
        return contractService.findPathList(groupId);
    }

    @DeleteMapping("deletePath/{groupId}/{contractPath}")
    public BaseResponse deletePath(@PathVariable("groupId") Integer groupId,
            @PathVariable String contractPath) {
        log.info("start deletePath. contractPath:{}", contractPath);
        contractService.deletePath(groupId, contractPath);
        return new BaseResponse(ConstantCode.RET_SUCCEED);
    }
}
