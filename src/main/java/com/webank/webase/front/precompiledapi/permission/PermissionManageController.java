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
package com.webank.webase.front.precompiledapi.permission;

import static com.webank.webase.front.util.PrecompiledUtils.PERMISSION_TYPE_CNS;
import static com.webank.webase.front.util.PrecompiledUtils.PERMISSION_TYPE_DEPLOY_AND_CREATE;
import static com.webank.webase.front.util.PrecompiledUtils.PERMISSION_TYPE_NODE;
import static com.webank.webase.front.util.PrecompiledUtils.PERMISSION_TYPE_PERMISSION;
import static com.webank.webase.front.util.PrecompiledUtils.PERMISSION_TYPE_SYS_CONFIG;
import static com.webank.webase.front.util.PrecompiledUtils.PERMISSION_TYPE_USERTABLE;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BasePageResponse;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.precompiledapi.entity.PermissionHandle;
import com.webank.webase.front.precompiledapi.entity.PermissionState;
import com.webank.webase.front.util.AddressUtils;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
 * Permission contoller
 * grant or revoke administrator and get administrators on chain
 */
@Api(value = "/permission", tags = "permission manage interface")
@Slf4j
@RestController
@RequestMapping(value = "/permission")
public class PermissionManageController extends BaseController {
    @Autowired
    private PermissionManageService permissionManageService;
    /**
     * handle get request by permission type
     * to get list of different administrators on chain
     */
    @GetMapping("")
    public Object permissionGetControl(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String permissionType,
            @RequestParam(defaultValue = "") String tableName,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {
        log.info("start permissionGetControl permissionType:{}", permissionType);
        switch (permissionType) {
            case PERMISSION_TYPE_PERMISSION:
                return listPermissionManager(groupId, pageSize, pageNumber);
            case PERMISSION_TYPE_DEPLOY_AND_CREATE:
                return listDeployAndCreateManager(groupId, pageSize, pageNumber);
            case PERMISSION_TYPE_USERTABLE:
                if(tableName.isEmpty()){
                    return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
                }else {
                    return listUserTableManager(groupId, tableName, pageSize, pageNumber);
                }
            case PERMISSION_TYPE_NODE:
                return listNodeManager(groupId, pageSize, pageNumber);
            case PERMISSION_TYPE_SYS_CONFIG:
                return listSysConfigManager(groupId, pageSize, pageNumber);
            case PERMISSION_TYPE_CNS:
                return listCNSManager(groupId, pageSize, pageNumber);
            default:
                log.debug("end permissionGetControl, permission type not exists");
                return ConstantCode.PARAM_FAIL_PERMISSION_TYPE_NOT_EXIST;
        }
    }

    /**
     * get list of different administrators on chain not paged
     */
    @GetMapping("/full")
    public Object getFullListByType(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String permissionType,
            @RequestParam(defaultValue = "", required = false) String tableName) {
        log.info("start getFullListByType permissionType:{}", permissionType);
        List<PermissionInfo> resList = new ArrayList<>();
        switch (permissionType) {
            case PERMISSION_TYPE_PERMISSION:
                resList = permissionManageService.listPermissionManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            case PERMISSION_TYPE_DEPLOY_AND_CREATE:
                resList = permissionManageService.listDeployAndCreateManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            case PERMISSION_TYPE_USERTABLE:
                if(tableName.isEmpty()){
                    return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
                }else {
                    resList = permissionManageService.listUserTableManager(groupId, tableName);
                    return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
                }
            case PERMISSION_TYPE_NODE:
                resList = permissionManageService.listNodeManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            case PERMISSION_TYPE_SYS_CONFIG:
                resList = permissionManageService.listSysConfigManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            case PERMISSION_TYPE_CNS:
                resList = permissionManageService.listCNSManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            default:
                log.debug("end getFullListByType, permission type not exists");
                return ConstantCode.PARAM_FAIL_PERMISSION_TYPE_NOT_EXIST;
        }
    }

    /**
     * get list of user's permission state not paged
     */
    @GetMapping("/sorted/full")
    public BasePageResponse gerPermissionStateFull(
            @RequestParam(defaultValue = "1") int groupId) {
        Instant startTime = Instant.now();
        log.info("start gerPermissionStateFull startTime:{}", startTime.toEpochMilli());
        Map<String, PermissionState> resultMap = permissionManageService.getAllPermissionStateList(groupId);
        log.info("end listCns useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(resultMap));
        return new BasePageResponse(ConstantCode.RET_SUCCESS, resultMap, resultMap.size());
    }


    /**
     * manage user's permission state by address,
     * get state before grant or revoke
     * @param permissionHandle
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "updateUserPermissionState", notes = "update address's all kinds of permissions")
    @ApiImplicitParam(name = "permissionHandle", value = "permission info", required = true, dataType = "PermissionHandle")
    @PostMapping("/sorted")
    public BaseResponse updateUserPermissionStateAfterCheck(@Valid @RequestBody PermissionHandle permissionHandle) {
        Instant startTime = Instant.now();
        log.info("start updateUserPermissionStateAfterCheck startTime:{}, permissionHandle:{}",
                startTime.toEpochMilli(), permissionHandle);
        int groupId = permissionHandle.getGroupId();
        String fromAddress = permissionHandle.getSignUserId();
        String userAddress = permissionHandle.getAddress();
        PermissionState permissionState = permissionHandle.getPermissionState();
        try {
            Map<String, Integer> resultState = permissionManageService.updatePermissionStateAfterCheck(groupId,
                    fromAddress, userAddress, permissionState);
            log.info("end updateUserPermissionStateAfterCheck startTime:{}, resultState:{}",
                    Instant.now().toEpochMilli(), resultState);
            return new BaseResponse(ConstantCode.RET_SUCCEED, resultState);
        } catch(NullPointerException e) {
            log.error("end updateUserPermissionStateAfterCheck for startTime:{}, Exception:{}",
                    Instant.now().toEpochMilli(), e.getMessage());
            return new BaseResponse(ConstantCode.PARAM_VAILD_FAIL, "all cns, node, sysConfig, deployAndCreate cannot be null");
        } catch(FrontException e) {
            log.error("end updateUserPermissionStateAfterCheck for startTime:{}, Exception:{}",
                    Instant.now().toEpochMilli(), e.getMessage());
            return new BaseResponse(ConstantCode.PERMISSION_DENIED, e.getMessage());
        }
    }

    /**
     * handle post request(grant) by permission type
     * @param permissionHandle
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "grantPermissionManager", notes = "grant address PermissionManager")
    @PostMapping
    public Object permissionPostControl(@Valid @RequestBody PermissionHandle permissionHandle) {
        log.info("start permissionPostControl permissionHandle:{}", permissionHandle);
        int groupId = permissionHandle.getGroupId();
        String permissionType = permissionHandle.getPermissionType();
        String from = permissionHandle.getSignUserId();
        String address = AddressUtils.checkAndGetAddress(permissionHandle.getAddress());
        String tableName = permissionHandle.getTableName();
        switch (permissionType) {
            case PERMISSION_TYPE_PERMISSION:
                return grantPermissionManager(groupId, from, address);
            case PERMISSION_TYPE_DEPLOY_AND_CREATE:
                return grantDeployAndCreateManager(groupId, from, address);
            case PERMISSION_TYPE_USERTABLE:
                if(tableName.isEmpty()){
                    return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
                }else {
                    return grantUserTableManager(groupId, from, tableName, address);
                }
            case PERMISSION_TYPE_NODE:
                return grantNodeManager(groupId, from, address);
            case PERMISSION_TYPE_SYS_CONFIG:
                return grantSysConfigManager(groupId, from, address);
            case PERMISSION_TYPE_CNS:
                return grantCNSManager(groupId, from, address);
            default:
                log.debug("end permissionPostControl, permission type not exists");
                return ConstantCode.PARAM_FAIL_PERMISSION_TYPE_NOT_EXIST;
        }
    }

    /**
     * handle delete request(revoke) by permission type
     * @param permissionHandle
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "revokePermissionManager", notes = "revoke address PermissionManager")
    @ApiImplicitParam(name = "permissionHandle", value = "transaction info", required = true, dataType = "PermissionHandle")
    @DeleteMapping("")
    public Object permissionDeleteControl(@Valid @RequestBody PermissionHandle permissionHandle) {
        log.info("start permissionDeleteControl permissionHandle:{}", permissionHandle);
        int groupId = permissionHandle.getGroupId();
        String permissionType = permissionHandle.getPermissionType();
        String from = permissionHandle.getSignUserId();
        String address = AddressUtils.checkAndGetAddress(permissionHandle.getAddress());
        String tableName = permissionHandle.getTableName();
        switch (permissionType) {
            case PERMISSION_TYPE_PERMISSION:
                return revokePermissionManager(groupId, from, address);
            case PERMISSION_TYPE_DEPLOY_AND_CREATE:
                return revokeDeployAndCreateManager(groupId, from, address);
            case PERMISSION_TYPE_USERTABLE:
                if(tableName.isEmpty()){
                    return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
                }else {
                    return revokeUserTableManager(groupId, from, tableName, address);
                }
            case PERMISSION_TYPE_NODE:
                return revokeNodeManager(groupId, from, address);
            case PERMISSION_TYPE_SYS_CONFIG:
                return revokeSysConfigManager(groupId, from, address);
            case PERMISSION_TYPE_CNS:
                return revokeCNSManager(groupId, from, address);
            default:
                log.debug("end permissionDeleteControl, permission type not exists");
                return ConstantCode.PARAM_FAIL_PERMISSION_TYPE_NOT_EXIST;
        }
    }

    /**
     * Permission manager, the admin to manage all types of admins
     */
    public Object grantPermissionManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start grantPermissionManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res = permissionManageService.grantPermissionManager(groupId, from, address);
        log.info("end grantPermissionManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }

    public Object revokePermissionManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start revokePermissionManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res = permissionManageService.revokePermissionManager(groupId, from, address);
        log.info("end revokePermissionManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }

    public Object listPermissionManager(int groupId, int pageSize, int pageNumber) {
        Instant startTime = Instant.now();
        log.info("start listPermissionManager startTime:{}, groupId:{}",
                startTime.toEpochMilli(), groupId);
        List<PermissionInfo> resList = permissionManageService.listPermissionManager(groupId);
        log.info("end listPermissionManager useTime:{} resList:{}",
                Duration.between(startTime, Instant.now()).toMillis(), resList);
        if(resList.size() != 0) {
            List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
            List<PermissionInfo> finalList = list2Page.getPagedList();
            long totalCount = (long) resList.size();
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    /**
     * manage DeployAndCreateManager related
     */
    public Object grantDeployAndCreateManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start grantDeployAndCreateManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res = permissionManageService.grantDeployAndCreateManager(groupId, from, address);
        log.info("end grantDeployAndCreateManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }

    public Object revokeDeployAndCreateManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start revokeDeployAndCreateManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res =  permissionManageService.revokeDeployAndCreateManager(groupId, from, address);
        log.info("end revokeDeployAndCreateManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }

    public Object listDeployAndCreateManager(int groupId, int pageSize, int pageNumber) {
        Instant startTime = Instant.now();
        log.info("start listDeployAndCreateManager startTime:{}, groupId:{}",
                startTime.toEpochMilli(), groupId);
        List<PermissionInfo> resList = permissionManageService.listDeployAndCreateManager(groupId);
        log.info("end listDeployAndCreateManager useTime:{} resList:{}",
                Duration.between(startTime, Instant.now()).toMillis(), resList);
        if(resList.size() != 0) {
            List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
            return new BasePageResponse(ConstantCode.RET_SUCCESS, list2Page.getPagedList(), resList.size());
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    /**
     * manage UserTableManager
     * admin that manage who can use CRUD in spectacular table on chain
     */
    public Object grantUserTableManager(int groupId, String from, String tableName,
                                        String address) {
        if(Objects.isNull(tableName)){
            log.error("grantUserTableManager error for table name is empty");
            return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
        }else {
            Instant startTime = Instant.now();
            log.info("start grantUserTableManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                    startTime.toEpochMilli(), groupId, from, address);
            try{
                Object res = permissionManageService
                        .grantUserTableManager(groupId, from, tableName, address);
                log.info("end grantUserTableManager useTime:{} res:{}",
                        Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
                return res;
            } catch (Exception e) {
                log.error("end grantUserTableManager for startTime:{}, Exception:{}",
                        Instant.now().toEpochMilli(), e);
                return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, e.getMessage());
            }
        }
    }

    public Object revokeUserTableManager(int groupId, String from, String tableName,
                                         String address) {
        if(Objects.isNull(tableName)){
            log.error("revokeUserTableManager error for table name is empty");
            return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
        }else {
            Instant startTime = Instant.now();
            log.info("start revokeUserTableManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                    startTime.toEpochMilli(), groupId, from, address);
            try {
                Object res = permissionManageService
                        .revokeUserTableManager(groupId, from, tableName, address);
                log.info("end revokeUserTableManager useTime:{} res:{}",
                        Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
                return res;
            } catch (Exception e) {
                log.error("end grantUserTableManager for startTime:{}, Exception:{}",
                        Instant.now().toEpochMilli(), e);
                return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, e.getMessage());
            }
        }
    }

    public Object listUserTableManager(int groupId, String tableName, int pageSize, int pageNumber) {
        if(Objects.isNull(tableName)){
            return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
        }else {
            Instant startTime = Instant.now();
            log.info("start listUserTableManager startTime:{}, groupId:{}",
                    startTime.toEpochMilli(), groupId);
            List<PermissionInfo> resList = permissionManageService.listUserTableManager(groupId, tableName);
            log.info("end listUserTableManager useTime:{} resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            if(resList.size() != 0) {
                List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
                List<PermissionInfo> finalList = list2Page.getPagedList();
                long totalCount = (long) resList.size();
                return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
            } else {
                return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
            }

        }
    }

    /**
     * node consensus admin  manage
     */
    public Object grantNodeManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start grantNodeManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res = permissionManageService.grantNodeManager(groupId, from, address);
        log.info("end grantNodeManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }

    public Object revokeNodeManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start revokeNodeManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res = permissionManageService.revokeNodeManager(groupId, from, address);
        log.info("end revokeNodeManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }

    public Object listNodeManager(int groupId, int pageSize, int pageNumber) {
        Instant startTime = Instant.now();
        log.info("start listNodeManager startTime:{}, groupId:{}",
                startTime.toEpochMilli(), groupId);
        List<PermissionInfo> resList = permissionManageService.listNodeManager(groupId);
        log.info("end listNodeManager useTime:{} resList:{}",
                Duration.between(startTime, Instant.now()).toMillis(), resList);
        if(resList.size() != 0) {
            List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
            List<PermissionInfo> finalList = list2Page.getPagedList();
            long totalCount = (long) resList.size();
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    /**
     * manage system config Manager related
     * admin who manage gas limit per tx and transaction limit per block
     */
    public Object grantSysConfigManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start grantSysConfigManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res = permissionManageService.grantSysConfigManager(groupId, from, address);
        log.info("end grantSysConfigManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }

    public Object revokeSysConfigManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start revokeSysConfigManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res = permissionManageService.revokeSysConfigManager(groupId, from, address);
        log.info("end revokeSysConfigManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }
    public Object listSysConfigManager(int groupId, int pageSize, int pageNumber) {
        Instant startTime = Instant.now();
        log.info("start listSysConfigManager startTime:{}, groupId:{}",
                startTime.toEpochMilli(), groupId);
        List<PermissionInfo> resList = permissionManageService.listSysConfigManager(groupId);
        log.info("end listSysConfigManager useTime:{} resList:{}",
                Duration.between(startTime, Instant.now()).toMillis(), resList);
        if(resList.size() != 0){
            List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
            List<PermissionInfo> finalList = list2Page.getPagedList();
            long totalCount = (long) resList.size();
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    /**
     *  manage CNS Manager related
     *  cns admin
     */

    public Object grantCNSManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start grantCNSManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res = permissionManageService.grantCNSManager(groupId, from, address);
        log.info("end grantCNSManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }

    public Object revokeCNSManager(int groupId, String from, String address) {
        Instant startTime = Instant.now();
        log.info("start revokeCNSManager.  startTime:{}, groupId:{}, fromAddress:{}, address:{}",
                startTime.toEpochMilli(), groupId, from, address);
        String res = permissionManageService.revokeCNSManager(groupId, from, address);
        log.info("end revokeCNSManager useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonUtils.toJSONString(res));
        return res;
    }

    public Object listCNSManager(int groupId, int pageSize, int pageNumber) {
        Instant startTime = Instant.now();
        log.info("start listCNSManager startTime:{}, groupId:{}",
                startTime.toEpochMilli(), groupId);
        List<PermissionInfo> resList = permissionManageService.listCNSManager(groupId);
        log.info("end listCNSManager useTime:{} resList:{}",
                Duration.between(startTime, Instant.now()).toMillis(), resList);
        if(resList.size() != 0){
            List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
            List<PermissionInfo> finalList = list2Page.getPagedList();
            long totalCount = (long) resList.size();
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }

    }

}
