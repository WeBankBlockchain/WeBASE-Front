/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.precompiledapi.permission;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.precompiledapi.entity.ChainGovernanceHandle;
import com.webank.webase.front.util.AddressUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.math.BigInteger;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * permission manage above FISCO-BCOS v2.5.0
 * just handle request, no business code (differ from PermissionManageController); business code place in node-manager
 */
@Api(value = "/governance", tags = "permission manage interface above fisco v2.5.x")
@Slf4j
@RestController
@RequestMapping("governance")
public class ChainGovernController {
    @Autowired
    private ChainGovernService chainGovernService;

    @GetMapping("committee/list")
    public List<PermissionInfo> listCommittee(
        @RequestParam Integer groupId) throws Exception {
        log.info("start listCommittee groupId:{}", groupId);
        return chainGovernService.listChainCommittee(groupId);
    }

    /**
     * handle grant committee
     */
    @ApiOperation(value = "grantCommittee", notes = "grant address committee")
    @ApiImplicitParam(name = "governanceHandle", value = "permission info", required = true, dataType = "ChainGovernanceHandle")
    @PostMapping("committee")
    public String grantCommittee(@Valid @RequestBody ChainGovernanceHandle governanceHandle) throws Exception {
        log.info("start grantCommittee governanceHandle:{}", governanceHandle);
        Integer groupId = governanceHandle.getGroupId();
        String fromSignUserId = governanceHandle.getSignUserId();
        if (StringUtils.isBlank(governanceHandle.getAddress())) {
            throw new FrontException(ConstantCode.GOVERNANCE_ADDRESS_PARAM_ERROR);
        }
        String address = AddressUtils.checkAndGetAddress(governanceHandle.getAddress());
        return chainGovernService.grantChainCommittee(groupId, fromSignUserId, address);
    }

    /**
     * handle revoke committee
     */
    @ApiOperation(value = "revokeCommittee", notes = "revoke address committee")
    @ApiImplicitParam(name = "governanceHandle", value = "permission info", required = true, dataType = "ChainGovernanceHandle")
    @DeleteMapping("committee")
    public String revokeCommittee(@Valid @RequestBody ChainGovernanceHandle governanceHandle) throws Exception {
        log.info("start revokeCommittee governanceHandle:{}", governanceHandle);
        Integer groupId = governanceHandle.getGroupId();
        String fromSignUserId = governanceHandle.getSignUserId();
        if (StringUtils.isBlank(governanceHandle.getAddress())) {
            throw new FrontException(ConstantCode.GOVERNANCE_ADDRESS_PARAM_ERROR);
        }
        String address =  AddressUtils.checkAndGetAddress(governanceHandle.getAddress());
        return chainGovernService.revokeChainCommittee(groupId, fromSignUserId, address);
    }

    /**
     * handle update committee weight
     */
    @GetMapping("committee/weight")
    public BigInteger queryCommitteeWeight(
        @RequestParam Integer groupId,
        @RequestParam String address) throws Exception {
        log.info("start queryCommitteeWeight groupId:{},address:{}", groupId, address);
        return chainGovernService.queryChainCommitteeWeight(groupId, address);
    }

    @ApiOperation(value = "updateCommitteeWeight", notes = "update address committee weight")
    @ApiImplicitParam(name = "governanceHandle", value = "permission info", required = true, dataType = "ChainGovernanceHandle")
    @PostMapping("committee/weight")
    public String updateCommitteeWeight(@Valid @RequestBody ChainGovernanceHandle governanceHandle) {
        log.info("start grantCommittee governanceHandle:{}", governanceHandle);
        Integer groupId = governanceHandle.getGroupId();
        String fromSignUserId = governanceHandle.getSignUserId();
        if (StringUtils.isBlank(governanceHandle.getAddress())) {
            throw new FrontException(ConstantCode.GOVERNANCE_ADDRESS_PARAM_ERROR);
        }
        String address =  AddressUtils.checkAndGetAddress(governanceHandle.getAddress());
        Integer weight = governanceHandle.getWeight();
        if (weight == null || weight <= 0) {
            throw new FrontException(ConstantCode.COMMITTEE_WEIGHT_PARAM_ERROR);
        }
        return chainGovernService.updateChainCommitteeWeight(groupId, fromSignUserId, address, weight);
    }

    /**
     * handle threshold
     */
    @GetMapping("threshold")
    public BigInteger queryThreshold(
        @RequestParam Integer groupId) throws Exception {
        log.info("start queryThreshold groupId:{}", groupId);
        return chainGovernService.queryThreshold(groupId);
    }

    @ApiOperation(value = "updateCommitteeThreshold", notes = "update address committee threshold")
    @ApiImplicitParam(name = "governanceHandle", value = "permission info", required = true, dataType = "ChainGovernanceHandle")
    @PostMapping("threshold")
    public String updateThreshold(@Valid @RequestBody ChainGovernanceHandle governanceHandle) throws Exception {
        log.info("start updateThreshold governanceHandle:{}", governanceHandle);
        Integer groupId = governanceHandle.getGroupId();
        String fromSignUserId = governanceHandle.getSignUserId();
        Integer threshold = governanceHandle.getThreshold();
        if (threshold == null || threshold < 0) {
            throw new FrontException(ConstantCode.CHAIN_THRESHOLD_PARAM_ERROR);
        }
        return chainGovernService.updateThreshold(groupId, fromSignUserId, threshold);
    }

    @GetMapping("operator/list")
    public List<PermissionInfo> listOperator(
        @RequestParam Integer groupId) throws Exception {
        log.info("start listOperator groupId:{}", groupId);
        return chainGovernService.listOperator(groupId);
    }

    /**
     * handle grant operator
     */
    @ApiOperation(value = "grantOperator", notes = "grant address operator")
    @ApiImplicitParam(name = "governanceHandle", value = "permission info", required = true, dataType = "ChainGovernanceHandle")
    @PostMapping("operator")
    public String grantOperator(@Valid @RequestBody ChainGovernanceHandle governanceHandle) throws Exception {
        log.info("start grantOperator governanceHandle:{}", governanceHandle);
        Integer groupId = governanceHandle.getGroupId();
        String fromSignUserId = governanceHandle.getSignUserId();
        if (StringUtils.isBlank(governanceHandle.getAddress())) {
            throw new FrontException(ConstantCode.GOVERNANCE_ADDRESS_PARAM_ERROR);
        }
        String address = AddressUtils.checkAndGetAddress(governanceHandle.getAddress());
        return chainGovernService.grantOperator(groupId, fromSignUserId, address);
    }

    /**
     * handle revoke operator
     */
    @ApiOperation(value = "revokeOperator", notes = "revoke address operator")
    @ApiImplicitParam(name = "governanceHandle", value = "permission info", required = true, dataType = "ChainGovernanceHandle")
    @DeleteMapping("operator")
    public String revokeOperator(@Valid @RequestBody ChainGovernanceHandle governanceHandle) throws Exception {
        log.info("start revokeOperator governanceHandle:{}", governanceHandle);
        Integer groupId = governanceHandle.getGroupId();
        String fromSignUserId = governanceHandle.getSignUserId();
        if (StringUtils.isBlank(governanceHandle.getAddress())) {
            throw new FrontException(ConstantCode.GOVERNANCE_ADDRESS_PARAM_ERROR);
        }
        String address = AddressUtils.checkAndGetAddress(governanceHandle.getAddress());
        return chainGovernService.revokeOperator(groupId, fromSignUserId, address);
    }


    /**
     * handle freeze/unfreeze account address
     */
    @GetMapping("account/status")
    public String getAccountStatus(@RequestParam Integer groupId, @RequestParam String address) throws Exception {
        log.info("start getAccountStatus groupId:{},address:{}", groupId, address);
        return chainGovernService.getAccountStatus(groupId, address);
    }

    @ApiOperation(value = "freezeAccount", notes = "freeze account address")
    @ApiImplicitParam(name = "governanceHandle", value = "permission info", required = true, dataType = "ChainGovernanceHandle")
    @PostMapping("account/freeze")
    public String freezeAccount(@Valid @RequestBody ChainGovernanceHandle governanceHandle) throws Exception {
        log.info("start freezeAccount governanceHandle:{}", governanceHandle);
        Integer groupId = governanceHandle.getGroupId();
        String fromSignUserId = governanceHandle.getSignUserId();
        if (StringUtils.isBlank(governanceHandle.getAddress())) {
            throw new FrontException(ConstantCode.GOVERNANCE_ADDRESS_PARAM_ERROR);
        }
        String address = AddressUtils.checkAndGetAddress(governanceHandle.getAddress());
        return chainGovernService.freezeAccount(groupId, fromSignUserId, address);
    }

    @ApiOperation(value = "unfreezeAccount", notes = "unfreeze account address")
    @ApiImplicitParam(name = "governanceHandle", value = "permission info", required = true, dataType = "ChainGovernanceHandle")
    @DeleteMapping("account/unfreeze")
    public String unfreezeAccount(@Valid @RequestBody ChainGovernanceHandle governanceHandle) throws Exception {
        log.info("start unfreezeAccount governanceHandle:{}", governanceHandle);
        Integer groupId = governanceHandle.getGroupId();
        String fromSignUserId = governanceHandle.getSignUserId();
        if (StringUtils.isBlank(governanceHandle.getAddress())) {
            throw new FrontException(ConstantCode.GOVERNANCE_ADDRESS_PARAM_ERROR);
        }
        String address = AddressUtils.checkAndGetAddress(governanceHandle.getAddress());
        return chainGovernService.unfreezeAccount(groupId, fromSignUserId, address);
    }

}
