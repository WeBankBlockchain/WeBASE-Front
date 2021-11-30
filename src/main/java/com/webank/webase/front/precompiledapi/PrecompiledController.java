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
package com.webank.webase.front.precompiledapi;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.precompiledapi.entity.ConsensusHandle;
import com.webank.webase.front.precompiledapi.entity.NodeInfo;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Precompiled common controller including management of CNS, node consensus status, CRUD
 */
@Api(value = "/precompiled", tags = "precompiled manage interface")
@Slf4j
@RestController
@RequestMapping(value = "precompiled")
public class PrecompiledController {
    @Autowired
    private PrecompiledService precompiledService;

    /**
     * Cns manage
     */
    @GetMapping("cns/list")
    public Object queryCns(@RequestParam(defaultValue = "1") String groupId,
            @RequestParam String contractNameAndVersion,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception {
        Instant startTime = Instant.now();
        log.info("start queryCns. startTime:{}, groupId:{}, contractNameAndVersion:{}",
                startTime.toEpochMilli(), groupId, contractNameAndVersion);
        List<CnsInfo> resList = new ArrayList<>();
        // get "name:version"
        String[] params = contractNameAndVersion.split(":");
        if (params.length == 1) {
            String name = params[0];
            resList = precompiledService.queryCnsByName(groupId, name);
            log.info("end queryCns useTime:{} resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            if (resList.size() != 0) {
                List2Page<CnsInfo> list2Page =
                        new List2Page<CnsInfo>(resList, pageSize, pageNumber);
                List<CnsInfo> finalList = list2Page.getPagedList();
                long totalCount = (long) resList.size();
                log.debug("end queryCns. Contract Name finalList:{}", finalList);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
            } else {
                return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
            }
        } else if (params.length == 2) {
            String name = params[0];
            String version = params[1];
            if (!PrecompiledUtils.checkVersion(version)) {
                return ConstantCode.INVALID_VERSION;
            }
            // check return list size
            Tuple2<String, String> res = precompiledService.queryCnsByNameAndVersion(groupId, name, version);
            log.info("end queryCns useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return new BaseResponse(ConstantCode.RET_SUCCESS, res);
        } else {
            return ConstantCode.PARAM_ERROR;
        }
    }



    /**
     * Node manage (Consensus control)
     */

    @GetMapping("consensus/list")
    public Object getNodeList(@RequestParam(defaultValue = "1") String groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {
        Instant startTime = Instant.now();
        log.info("start getNodeList startTime:{}, groupId:{}", startTime.toEpochMilli(), groupId);
        List<NodeInfo> resList = precompiledService.getNodeList(groupId);
        log.info("end getNodeList useTime:{} resList:{}",
                Duration.between(startTime, Instant.now()).toMillis(), resList);
        if (resList.size() != 0) {
            List2Page<NodeInfo> list2Page = new List2Page<NodeInfo>(resList, pageSize, pageNumber);
            List<NodeInfo> finalList = list2Page.getPagedList();
            long totalCount = resList.size();
            log.debug("end getNodeList. finalList:{}", finalList);
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    @ApiOperation(value = "nodeManageControl", notes = "set system config value by key")
    @ApiImplicitParam(name = "consensusHandle", value = "node consensus status control",
            required = true, dataType = "ConsensusHandle")
    @PostMapping("consensus")
    public Object nodeManageControl(@Valid @RequestBody ConsensusHandle consensusHandle)
            {
        log.info("start nodeManageControl. consensusHandle:{}", consensusHandle);
        String nodeType = consensusHandle.getNodeType();
        String groupId = consensusHandle.getGroupId();
        String from = consensusHandle.getSignUserId();
        String nodeId = consensusHandle.getNodeId();
        Integer weight = consensusHandle.getWeight();
        if (!PrecompiledUtils.checkNodeId(nodeId)) {
            return ConstantCode.INVALID_NODE_ID;
        }
        switch (nodeType) {
            case PrecompiledUtils.NODE_TYPE_SEALER:
                return addSealer(groupId, from, nodeId, weight);
            case PrecompiledUtils.NODE_TYPE_OBSERVER:
                return addObserver(groupId, from, nodeId);
            case PrecompiledUtils.NODE_TYPE_REMOVE:
                return removeNode(groupId, from, nodeId);
            default:
                log.debug("end nodeManageControl invalid node type");
                return ConstantCode.INVALID_NODE_TYPE;
        }
    }

    public Object addSealer(String groupId, String fromAddress, String nodeId, Integer weight) {
        Instant startTime = Instant.now();
        log.info("start addSealer startTime:{}, groupId:{},fromAddress:{},nodeId:{},weight:{}",
                startTime.toEpochMilli(), groupId, fromAddress, nodeId, weight);
        if (weight == null) {
            throw new FrontException(ConstantCode.ADD_SEALER_WEIGHT_CANNOT_NULL);
        }
        try {
            Object res = precompiledService.addSealer(groupId, fromAddress, nodeId, weight);
            log.info("end addSealer useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) {
            log.error("addSealer exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

    public Object addObserver(String groupId, String fromAddress, String nodeId) {
        Instant startTime = Instant.now();
        log.info("start addObserver startTime:{}, groupId:{},fromAddress:{},nodeId:{}",
                startTime.toEpochMilli(), groupId, fromAddress, nodeId);
        try {
            Object res = precompiledService.addObserver(groupId, fromAddress, nodeId);
            log.info("end addObserver useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) {
            log.error("addObserver exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

    public Object removeNode(String groupId, String fromAddress, String nodeId) {
        Instant startTime = Instant.now();
        log.info("start addSealer startTime:{}, groupId:{},fromAddress:{},nodeId:{}",
                startTime.toEpochMilli(), groupId, fromAddress, nodeId);
        try {
            Object res = precompiledService.removeNode(groupId, fromAddress, nodeId);
            log.info("end addSealer useTime:{} res:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) { // e.getCause
            log.error("removeNode exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

}
