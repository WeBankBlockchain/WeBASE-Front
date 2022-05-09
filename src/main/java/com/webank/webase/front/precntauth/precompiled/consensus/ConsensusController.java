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
package com.webank.webase.front.precntauth.precompiled.consensus;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.precntauth.precompiled.consensus.entity.ConsensusHandle;
import com.webank.webase.front.precntauth.precompiled.consensus.entity.NodeInfo;
import com.webank.webase.front.precntauth.precompiled.consensus.entity.ReqNodeListInfo;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "precntauth/precompiled/consensus", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/consensus")
public class ConsensusController {

    @Autowired
    private ConsensusServiceInWebase consensusService;

    @ApiOperation(value = "query consensus node list")
    @ApiImplicitParam(name = "reqNodeListInfo", value = "node consensus list", required = true, dataType = "ReqNodeListInfo")
    @PostMapping("list")
    public Object getNodeList(@Valid @RequestBody ReqNodeListInfo reqNodeListInfo) {
        Instant startTime = Instant.now();
        log.info("start getNodeList startTime:{}, groupId:{}", startTime.toEpochMilli(),
            reqNodeListInfo.getGroupId());
        List<NodeInfo> resList = consensusService.getNodeList(reqNodeListInfo.getGroupId());
        log.info("end getNodeList useTime:{} resList:{}",
            Duration.between(startTime, Instant.now()).toMillis(), resList);
        if (resList.size() != 0) {
            List2Page<NodeInfo> list2Page = new List2Page<NodeInfo>(resList,
                reqNodeListInfo.getPageSize(), reqNodeListInfo.getPageNumber());
            List<NodeInfo> finalList = list2Page.getPagedList();
            long totalCount = resList.size();
            log.debug("end getNodeList. finalList:{}", finalList);
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    @ApiOperation(value = "manage node type", notes = "addSealer addObserver removeNode")
    @ApiImplicitParam(name = "consensusHandle", value = "node consensus info", required = true, dataType = "ConsensusHandle")
    @PostMapping("manage")
    public Object nodeManageControl(@Valid @RequestBody ConsensusHandle consensusHandle) {
        log.info("start nodeManageControl. consensusHandle:{}", consensusHandle);
        String nodeType = consensusHandle.getNodeType();
        String groupId = consensusHandle.getGroupId();
        String from = consensusHandle.getSignUserId();
        String nodeId = consensusHandle.getNodeId();
        BigInteger weight = consensusHandle.getWeight();
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

    public Object addSealer(String groupId, String fromAddress, String nodeId, BigInteger weight) {
        Instant startTime = Instant.now();
        log.info("start addSealer startTime:{}, groupId:{},fromAddress:{},nodeId:{},weight:{}",
            startTime.toEpochMilli(), groupId, fromAddress, nodeId, weight);
        if (weight == null) {
            throw new FrontException(ConstantCode.ADD_SEALER_WEIGHT_CANNOT_NULL);
        }
        try {
            Object res = consensusService.addSealer(groupId, fromAddress, nodeId, weight);
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
            Object res = consensusService.addObserver(groupId, fromAddress, nodeId);
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
            Object res = consensusService.removeNode(groupId, fromAddress, nodeId);
            log.info("end addSealer useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) { // e.getCause
            log.error("removeNode exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

}
