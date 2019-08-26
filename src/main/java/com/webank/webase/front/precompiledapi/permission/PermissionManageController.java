package com.webank.webase.front.precompiledapi.permission;

import com.webank.webase.front.base.*;
import com.webank.webase.front.util.AddressUtils;
import com.webank.webase.front.util.pageutils.List2Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@Api(value = "/", tags = "permission manage interface")
@Slf4j
@RestController
@RequestMapping(value = "/")

public class PermissionManageController extends BaseController {
    @Autowired
    private PermissionManageService permissionManageService;

    //分发Get请求
    @GetMapping("/permission")
    public Object permissionGetControl(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String permissionType,
            @RequestParam(defaultValue = "", required = false) String tableName,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception {

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

    // 分发Post请求
    @ApiOperation(value = "grantPermissionManager", notes = "grant address PermissionManager")
    @ApiImplicitParam(name = "permissionHandle", value = "transaction info", required = true, dataType = "PermissionHandle")
    @PostMapping("/permission")
    public Object permissionPostControl(@Valid @RequestBody PermissionHandle permissionHandle, BindingResult result) throws Exception {
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
                return grantUserTableManager(groupId, from, tableName, address);
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

    //分发Get请求
    @ApiOperation(value = "revokePermissionManager", notes = "revoke address PermissionManager")
    @ApiImplicitParam(name = "permissionHandle", value = "transaction info", required = true, dataType = "PermissionHandle")
    @DeleteMapping("/permission")
    public Object permissionDeleteControl(@Valid @RequestBody PermissionHandle permissionHandle, BindingResult result) throws Exception {

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
                return revokeUserTableManager(groupId, from, tableName, address);
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
        log.info("grantPermissionManager start. address:[{}]", address);
        //checkParamResult(result);
        String res = permissionManageService.grantPermissionManager(groupId, from, address);

        return res;
    }

    public Object revokePermissionManager(int groupId, String from, String address) throws Exception {
        log.info("revokePermissionManager start. address:[{}]", address);
        //checkParamResult(result);
        String res = permissionManageService.revokePermissionManager(groupId, from, address);
        return res;
    }

    public Object listPermissionManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("listPermissionManager start. ");
        List<PermissionInfo> resList = permissionManageService.listPermissionManager(groupId);
        List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
        List<PermissionInfo> finalList = list2Page.getPagedList();
        Long totalCount = (long) finalList.size();
        return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
    }

    /**
     * manage DeployAndCreateManager related
     */

    public Object grantDeployAndCreateManager(int groupId, String from, String address) throws Exception {
        log.info("grantDeployAndCreateManager start. address:[{}]", address);
        //checkParamResult(result);
        return permissionManageService.grantDeployAndCreateManager(groupId, from, address);
    }

    public Object revokeDeployAndCreateManager(int groupId, String from, String address) throws Exception {
        log.info("revokeDeployAndCreateManager start. address:[{}]", address);
        //checkParamResult(result);
        return permissionManageService.revokeDeployAndCreateManager(groupId, from, address);
    }

    public Object listDeployAndCreateManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("listDeployAndCreateManager start. ");
        List<PermissionInfo> resList = permissionManageService.listDeployAndCreateManager(groupId);
        List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
        return new BaseResponse(ConstantCode.RET_SUCCESS, list2Page.getPagedList());
    }

    /**
     * manage UserTableManager
     */
    public Object grantUserTableManager(int groupId, String from, String tableName, String address) throws Exception {
        if(Objects.isNull(tableName)){
            return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
        }else {
            log.info("grantUserTableManager start.tableName: ", tableName, "  address:[{}]: ", address);
            //checkParamResult(result);
            return permissionManageService.grantUserTableManager(groupId, from, tableName, address);
        }
    }

    public Object revokeUserTableManager(int groupId, String from, String tableName, String address) throws Exception {
        if(Objects.isNull(tableName)){
            return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
        }else {
            log.info("revokeUserTableManager start.tableName: ", tableName, "  address: ", address);
            //checkParamResult(result);
            return permissionManageService.revokeUserTableManager(groupId, from, tableName, address);
        }
    }

    public Object listUserTableManager(int groupId, String tableName, int pageSize, int pageNumber) throws Exception {
        if(Objects.isNull(tableName)){
            return ConstantCode.PARAM_FAIL_TABLE_NAME_IS_EMPTY;
        }else {
            log.info("listPermissionManager start. ");
            List<PermissionInfo> resList = permissionManageService.listUserTableManager(groupId, tableName);
            List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
            List<PermissionInfo> finalList = list2Page.getPagedList();
            Long totalCount = (long) finalList.size();
            return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
        }
    }

    /**
     * node
     */
    public Object grantNodeManager(int groupId, String from, String address) throws Exception {
        log.info("grantNodeManager start. address:: ", address);
        //checkParamResult(result);
        return permissionManageService.grantNodeManager(groupId, from, address);
    }

    public Object revokeNodeManager(int groupId, String from, String address) throws Exception {
        log.info("revokeNodeManager start. address:: ", address);
        //checkParamResult(result);
        return permissionManageService.revokeNodeManager(groupId, from, address);
    }

    public Object listNodeManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("listNodeManager start.  ");
        List<PermissionInfo> resList = permissionManageService.listNodeManager(groupId);
        List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
        List<PermissionInfo> finalList = list2Page.getPagedList();
        Long totalCount = (long) finalList.size();
        return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
    }

    /**
     * manage system config Manager related
     */
    public Object grantSysConfigManager(int groupId, String from, String address) throws Exception {
        log.info("grantSysConfigManager start. address:: ", address);
        //checkParamResult(result);
        return permissionManageService.grantSysConfigManager(groupId, from, address);
    }

    public Object revokeSysConfigManager(int groupId, String from, String address) throws Exception {
        log.info("revokeSysConfigManager start. address:: ", address);
        //checkParamResult(result);
        return permissionManageService.revokeSysConfigManager(groupId, from, address);
    }
    public Object listSysConfigManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("listSysConfigManager start.  ");
        List<PermissionInfo> resList = permissionManageService.listSysConfigManager(groupId);
        List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
        List<PermissionInfo> finalList = list2Page.getPagedList();
        Long totalCount = (long) finalList.size();
        return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
    }

    /**
     *  manage CNS Manager related
     */

    public Object grantCNSManager(int groupId, String from, String address) throws Exception {
        log.info("grantCNSManager start. address:: ", address);
        //checkParamResult(result);
        return permissionManageService.grantCNSManager(groupId, from, address);
    }

    public Object revokeCNSManager(int groupId, String from, String address) throws Exception {
        log.info("revokeCNSManager start. address:: ", address);
        //checkParamResult(result);
        return permissionManageService.revokeCNSManager(groupId, from, address);
    }
    public Object listCNSManager(int groupId, int pageSize, int pageNumber) throws Exception {
        log.info("listCNSManager start.  ");
        List<PermissionInfo> resList = permissionManageService.listCNSManager(groupId);
        List2Page<PermissionInfo> list2Page = new List2Page<>(resList, pageSize, pageNumber);
        List<PermissionInfo> finalList = list2Page.getPagedList();
        Long totalCount = (long) finalList.size();
        return new BasePageResponse(ConstantCode.RET_SUCCESS, finalList, totalCount);
    }

}
