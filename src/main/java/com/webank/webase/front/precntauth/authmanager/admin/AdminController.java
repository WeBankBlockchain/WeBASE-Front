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
package com.webank.webase.front.precntauth.authmanager.admin;

import com.webank.webase.front.precntauth.authmanager.admin.entity.ReqAclAuthTypeInfo;
import com.webank.webase.front.precntauth.authmanager.admin.entity.ReqAclUsrInfo;
import com.webank.webase.front.precntauth.authmanager.admin.entity.ReqContractStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.math.BigInteger;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "precntauth/authmanager/admin/", tags = "precntauth authmanager controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/authmanager/admin/")
public class AdminController {

  @Autowired
  private AdminService adminService;

  /**
   * 设置合约的接口权限类型(目前只能对写方法进行控制)
   */
  @ApiOperation(value = "set contract func acl type")
  @ApiImplicitParam(name = "reqAclAuthTypeInfo", value = "aclType info", required = true, dataType = "ReqAclAuthTypeInfo")
  @PostMapping("method/auth/type")
  public Object setMethodAuthType(@Valid @RequestBody ReqAclAuthTypeInfo reqAclAuthTypeInfo)
      throws ContractException {
    String groupId = reqAclAuthTypeInfo.getGroupId();
    String signUserId = reqAclAuthTypeInfo.getSignUserId();
    String contractAddr = reqAclAuthTypeInfo.getContractAddr();
    byte[] func = reqAclAuthTypeInfo.getFunc().getBytes();
    BigInteger authType = reqAclAuthTypeInfo.getAuthType();
    Object res = adminService.setMethodAuthType(groupId, signUserId, contractAddr, func, authType);
    return res;
  }

  /**
   * 设置用户地址对合约的接口函数访问权限
   * openMethodAuth contractAdd(0xCcEeF68C9b4811b32c75df284a1396C7C5509561) set(string) accountAdd(0x7fb008862ff69353a02ddabbc6cb7dc31683d0f6)
   */
  @ApiOperation(value = "set contract func usr acl")
  @ApiImplicitParam(name = "reqAclUsrInfo", value = "aclUsr info", required = true, dataType = "ReqAclUsrInfo")
  @PostMapping("method/auth/set")
  public Object setMethodAuth(@Valid @RequestBody ReqAclUsrInfo reqAclUsrInfo)
      throws ContractException {
    String groupId = reqAclUsrInfo.getGroupId();
    String contractAddr = reqAclUsrInfo.getContractAddr();
    String signUserId = reqAclUsrInfo.getSignUserId();
    byte[] func = reqAclUsrInfo.getFunc().getBytes();
    String accountAddress = reqAclUsrInfo.getUserAddress();
    Boolean bool = reqAclUsrInfo.getIsOpen();
    Object res = adminService.setMethodAuth(groupId, signUserId, contractAddr, func,
        accountAddress,bool);
    return res;
  }


  /**
   * 冻结、解冻合约
   * openMethodAuth contractAdd(0xCcEeF68C9b4811b32c75df284a1396C7C5509561) set(string) accountAdd(0x7fb008862ff69353a02ddabbc6cb7dc31683d0f6)
   */
  @ApiOperation(value = "set contract status")
  @ApiImplicitParam(name = "reqContractStatus", value = "contract status info", required = true, dataType = "ReqContractStatus")
  @PostMapping("contract/status/set")
  public String setContractStatus(@Valid @RequestBody ReqContractStatus reqContractStatus) {
    String groupId = reqContractStatus.getGroupId();
    String contractAddr = reqContractStatus.getContractAddr();
    String signUserId = reqContractStatus.getSignUserId();
    Boolean isFreeze = reqContractStatus.getIsFreeze();
    String res = adminService.setContractStatus(groupId, signUserId, contractAddr, isFreeze);
    return res;
  }


}
