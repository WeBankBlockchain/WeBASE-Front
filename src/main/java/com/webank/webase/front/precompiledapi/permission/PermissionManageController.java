/*
 * Copyright 2014-2019 the original author or authors.
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

import com.fasterxml.jackson.core.JsonParseException;
import com.webank.webase.front.base.*;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.util.AddressUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import com.webank.webase.front.util.pageutils.Map2PagedList;
import com.webank.webase.front.util.pageutils.MapHandle;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Api(value = "/permission", tags = "permission manage interface")
@Slf4j
@RestController
@RequestMapping(value = "/permission")

public class PermissionManageController extends BaseController {
    @Autowired
    private PermissionManageService permissionManageService;

    //分发Get请求, 根据type获取权限管理员的list
    @GetMapping("")
    public Object permissionGetControl(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String permissionType,
            @RequestParam(defaultValue = "", required = false) String tableName,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception {
        log.info("start permissionGetControl startTime:{}, permissionType:{}",
                Instant.now().toEpochMilli(), permissionType);
        switch (permissionType) {
            case "permission":
                return listPermissionManager(groupId, pageSize, pageNumber);
            case "deployAndCreate":
                return listDeployAndCreateManager(groupId, pageSize, pageNumber);
            case "userTable":
                if(tableName.isEmpty()){
                    return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
                }else {
                    return listUserTableManager(groupId, tableName, pageSize, pageNumber);
                }
            case "node":
                return listNodeManager(groupId, pageSize, pageNumber);
            case "sysConfig":
                return listSysConfigManager(groupId, pageSize, pageNumber);
            case "cns":
                return listCNSManager(groupId, pageSize, pageNumber);
            default:
                return ConstantCode.PARAM_FAIL_PERMISSION_TYPE_NOT_EXIST;
        }
    }

    // 根据type获取不分页的管理员list
    @GetMapping("/full")
    public Object getFullListByType(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String permissionType,
            @RequestParam(defaultValue = "", required = false) String tableName) throws Exception {
        log.info("start getFullListByType startTime:{}, permissionType:{}",
                Instant.now().toEpochMilli(), permissionType);
        List<PermissionInfo> resList = new ArrayList<>();
        switch (permissionType) {
            case "permission":
                resList = permissionManageService.listPermissionManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            case "deployAndCreate":
                resList = permissionManageService.listDeployAndCreateManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            case "userTable":
                if(tableName.isEmpty()){
                    return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
                }else {
                    resList = permissionManageService.listUserTableManager(groupId, tableName);
                    return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
                }
            case "node":
                resList = permissionManageService.listNodeManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            case "sysConfig":
                resList = permissionManageService.listSysConfigManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            case "cns":
                resList = permissionManageService.listCNSManager(groupId);
                return new BasePageResponse(ConstantCode.RET_SUCCESS, resList, resList.size());
            default:
                return ConstantCode.PARAM_FAIL_PERMISSION_TYPE_NOT_EXIST;
        }
    }

    // 返回不分页的全量的list
    @GetMapping("/sorted/full")
    public Object gerPermissionStateFull(
            @RequestParam(defaultValue = "1") int groupId) throws Exception {
        log.info("start gerPermissionStateFull startTime:{}",
                Instant.now().toEpochMilli());
        Map<String, PermissionState> resultMap = permissionManageService.getAllPermissionStateList(groupId);
        log.info("end gerPermissionStateFull startTime:{}, result of PermissionState Map:{}",
                Instant.now().toEpochMilli(), resultMap);
        return new BasePageResponse(ConstantCode.RET_SUCCESS, resultMap, resultMap.size());
    }


    // 批量修改address的权限，包含grant&revoke 先get后发交易
    @ApiOperation(value = "updateUserPermissionState", notes = "update address's all kinds of permissions")
    @ApiImplicitParam(name = "permissionHandle", value = "permission info", required = true, dataType = "PermissionHandle")
    @PostMapping("/sorted")
    public Object updateUserPermissionStateAfterCheck(@Valid @RequestBody PermissionHandle permissionHandle){
        log.info("start updateUserPermissionStateAfterCheck startTime:{}, permissionHandle:{}",
                Instant.now().toEpochMilli(), permissionHandle);
        int groupId = permissionHandle.getGroupId();
        String fromAddress = permissionHandle.getFromAddress();
        String userAddress = permissionHandle.getAddress();
        PermissionState permissionState = permissionHandle.getPermissionState();
        try {
            Object resultState = permissionManageService.updatePermissionStateAfterCheck(groupId, fromAddress, userAddress, permissionState);
            log.info("end updateUserPermissionStateAfterCheck startTime:{}, resultState:{}",
                    Instant.now().toEpochMilli(), resultState);
            return new BaseResponse(ConstantCode.RET_SUCCEED, resultState);
        }catch (Exception e) {
            log.info("end updateUserPermissionStateAfterCheck for startTime:{}, Exception:{}",
                    Instant.now().toEpochMilli(), e.getMessage());
            if(e instanceof JsonParseException) {
                return new BaseResponse(ConstantCode.SYSTEM_ERROR, "Parse json fail, please check permissionState's object structure");
            }
            if(e instanceof NullPointerException) {
                return new BaseResponse(ConstantCode.PARAM_VAILD_FAIL, "all cns, node, sysConfig, deployAndCreate cannot be null");
            }
            if(e instanceof FrontException) {
                return new BaseResponse(ConstantCode.PERMISSION_DENIED, e.getMessage());
            }
            return new BaseResponse(ConstantCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    // 分发Post请求
    @ApiOperation(value = "grantPermissionManager", notes = "grant address PermissionManager")
    @ApiImplicitParam(name = "permissionHandle", value = "transaction info", required = true, dataType = "PermissionHandle")
    @PostMapping("")
    public Object permissionPostControl(@Valid @RequestBody PermissionHandle permissionHandle, BindingResult result) throws Exception {
        log.info("start permissionPostControl startTime:{}, permissionHandle:{}",
                Instant.now().toEpochMilli(), permissionHandle);
        int groupId = permissionHandle.getGroupId();
        String permissionType = permissionHandle.getPermissionType();
        String from = permissionHandle.getFromAddress();
        String address = permissionHandle.getAddress();
        String tableName = permissionHandle.getTableName();
        // validate address
        Address converAddr = AddressUtils.convertAddress(address);
        if(!converAddr.isValid()){
            return ConstantCode.PARAM_ADDRESS_IS_INVALID;
        }
        address = converAddr.getAddress();
        switch (permissionType) {
            case "permission":
                return grantPermissionManager(groupId, from, address);
            case "deployAndCreate":
                return grantDeployAndCreateManager(groupId, from, address);
            case "userTable":
                if(tableName.isEmpty()){
                    return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
                }else {
                    return grantUserTableManager(groupId, from, tableName, address);
                }
            case "node":
                return grantNodeManager(groupId, from, address);
            case "sysConfig":
                return grantSysConfigManager(groupId, from, address);
            case "cns":
                return grantCNSManager(groupId, from, address);
            default:
                return ConstantCode.PARAM_FAIL_PERMISSION_TYPE_NOT_EXIST;
        }
    }

    //分发del请求
    @ApiOperation(value = "revokePermissionManager", notes = "revoke address PermissionManager")
    @ApiImplicitParam(name = "permissionHandle", value = "transaction info", required = true, dataType = "PermissionHandle")
    @DeleteMapping("")
    public Object permissionDeleteControl(@Valid @RequestBody PermissionHandle permissionHandle, BindingResult result) throws Exception {
        log.info("start permissionDeleteControl startTime:{}, permissionHandle:{}",
                Instant.now().toEpochMilli(), permissionHandle);
        int groupId = permissionHandle.getGroupId();
        String permissionType = permissionHandle.getPermissionType();
        String from = permissionHandle.getFromAddress();
        String address = permissionHandle.getAddress();
        String tableName = permissionHandle.getTableName();
        // validate address
        Address convertAddress = AddressUtils.convertAddress(address);
        if(!convertAddress.isValid()){
            return ConstantCode.PARAM_ADDRESS_IS_INVALID;
        }
        address = convertAddress.getAddress();
        switch (permissionType) {
            case "permission":
                return revokePermissionManager(groupId, from, address);
            case "deployAndCreate":
                return revokeDeployAndCreateManager(groupId, from, address);
            case "userTable":
                if(tableName.isEmpty()){
                    return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
                }else {
                    return revokeUserTableManager(groupId, from, tableName, address);
                }
            case "node":
                return revokeNodeManager(groupId, from, address);
            case "sysConfig":
                return revokeSysConfigManager(groupId, from, address);
            case "cns":
                return revokeCNSManager(groupId, from, address);
            default:
                return ConstantCode.PARAM_FAIL_PERMISSION_TYPE_NOT_EXIST;
        }
    }

    /**
     * manage PermissionManager related
     */

    public Object grantPermissionManager(int groupId, String from, String address) throws Exception {
        log.info("start grantPermissionManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        //checkParamResult(result);
        String res = permissionManageService.grantPermissionManager(groupId, from, address);
        log.info("end grantPermissionManager. res:{}", res);
        return res;
    }

    public Object revokePermissionManager(int groupId, String from, String address) throws Exception {
        log.info("start revokePermissionManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        //checkParamResult(result);
        String res = permissionManageService.revokePermissionManager(groupId, from, address);
        log.info("end revokePermissionManager. res:{}", res);
        return res;
    }

    public Object listPermissionManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("listPermissionManager. groupId:{}", groupId);
        List<PermissionInfo> resList = permissionManageService.listPermissionManager(groupId);
        log.info("end listPermissionManager. groupId:{}, resList:{}", groupId, resList);
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

    public Object grantDeployAndCreateManager(int groupId, String from, String address) throws Exception {
        log.info("start grantDeployAndCreateManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        String res = permissionManageService.grantDeployAndCreateManager(groupId, from, address);
        log.info("end grantDeployAndCreateManager. res:{}", res);
        return res;
    }

    public Object revokeDeployAndCreateManager(int groupId, String from, String address) throws Exception {
        log.info("start revokeDeployAndCreateManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        String res =  permissionManageService.revokeDeployAndCreateManager(groupId, from, address);
        log.info("end revokeDeployAndCreateManager. res:{}", res);
        return res;
    }

    public Object listDeployAndCreateManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("start listDeployAndCreateManager. ");
        List<PermissionInfo> resList = permissionManageService.listDeployAndCreateManager(groupId);
        log.info("end listDeployAndCreateManager. resList:{}", resList);
        if(resList.size() != 0) {
            List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
            return new BasePageResponse(ConstantCode.RET_SUCCESS, list2Page.getPagedList(), resList.size());
        } else {
            return new BasePageResponse(ConstantCode.RET_SUCCESS_EMPTY_LIST, resList, 0);
        }
    }

    /**
     * manage UserTableManager
     */
    public Object grantUserTableManager(int groupId, String from, String tableName, String address) throws Exception {
        if(Objects.isNull(tableName)){
            return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
        }else {
            log.info("start grantUserTableManager.groupId:{},from:{}, tableName:{},address:{} ", groupId, from, tableName, address);
            //checkParamResult(result);
            try{
                Object res = permissionManageService.grantUserTableManager(groupId, from, tableName, address);
                log.info("end grantUserTableManager. res:{}", res);
                return res;
            } catch (Exception e) {
                return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, e.getMessage());
            }
        }
    }

    public Object revokeUserTableManager(int groupId, String from, String tableName, String address) throws Exception {
        if(Objects.isNull(tableName)){
            return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
        }else {
            log.info("start revokeUserTableManager.groupId:{},from:{}, tableName:{},address:{} ", groupId, from, tableName, address);
            //checkParamResult(result);
            try {
                Object res = permissionManageService.revokeUserTableManager(groupId, from, tableName, address);
                log.info("end revokeUserTableManager. res:{}", res);
                return res;
            } catch (Exception e) {
                return new BaseResponse(ConstantCode.FAIL_TABLE_NOT_EXISTS, e.getMessage());
            }
        }
    }

    public Object listUserTableManager(int groupId, String tableName, int pageSize, int pageNumber) throws Exception {
        if(Objects.isNull(tableName)){
            return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
        }else {
            log.info("start listUserTableManager.groupId:{}, tableName:{} ", groupId, tableName);
            //  if tableName not exists, exception catch will return list whose size is 0
            List<PermissionInfo> resList = permissionManageService.listUserTableManager(groupId, tableName);
            log.info("end listUserTableManager.resList:{}", resList);
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
     * node
     */
    public Object grantNodeManager(int groupId, String from, String address) throws Exception {
        log.info("start grantNodeManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        String res = permissionManageService.grantNodeManager(groupId, from, address);
        log.info("end grantNodeManager. res:{}", res);
        return res;
    }

    public Object revokeNodeManager(int groupId, String from, String address) throws Exception {
        log.info("start revokeNodeManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        //checkParamResult(result);
        String res = permissionManageService.revokeNodeManager(groupId, from, address);
        log.info("end revokeNodeManager. res:{}", res);
        return res;    }

    public Object listNodeManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("start listNodeManager.  ");
        List<PermissionInfo> resList = permissionManageService.listNodeManager(groupId);
        log.info("end listNodeManager. resList:{}", resList);
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
     */
    public Object grantSysConfigManager(int groupId, String from, String address) throws Exception {
        log.info("start grantSysConfigManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        //checkParamResult(result);
        String res = permissionManageService.grantSysConfigManager(groupId, from, address);
        log.info("end grantSysConfigManager. res:{}", res);
        return res;
    }

    public Object revokeSysConfigManager(int groupId, String from, String address) throws Exception {
        log.info("start revokeSysConfigManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        //checkParamResult(result);
        String res = permissionManageService.revokeSysConfigManager(groupId, from, address);
        log.info("end revokeSysConfigManager. res:{}", res);
        return res;
    }
    public Object listSysConfigManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("start listSysConfigManager.  ");
        List<PermissionInfo> resList = permissionManageService.listSysConfigManager(groupId);
        log.info("end listSysConfigManager. resList:{}", resList);
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
     */

    public Object grantCNSManager(int groupId, String from, String address) throws Exception {
        log.info("start grantCNSManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        //checkParamResult(result);
        String res = permissionManageService.grantCNSManager(groupId, from, address);
        log.info("end grantCNSManager. res:{}", res);
        return res;
    }

    public Object revokeCNSManager(int groupId, String from, String address) throws Exception {
        log.info("start revokeCNSManager. groupId:{}, fromAddress:{}, address:{}", groupId, from, address);
        //checkParamResult(result);
        String res = permissionManageService.revokeCNSManager(groupId, from, address);
        log.info("end revokeCNSManager. res:{}", res);
        return res;}

    public Object listCNSManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("start listCNSManager.  ");
        List<PermissionInfo> resList = permissionManageService.listCNSManager(groupId);
        log.info("end listCNSManager. resList:{}", resList);
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
