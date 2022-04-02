package com.webank.webase.front.rpc.authmanager.base;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "precntauth/authmanager/base/", tags = "precntauth authmanager controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/authmanager/base/")
public class AuthMgrBaseController {

  @Autowired
  private AuthMgrBaseService authMgrService;

  @ApiOperation(value = "query exec env is wasm")
  @GetMapping("queryExecEnvIsWasm")
  public Object queryExecEnvIsWasm(String groupId) {
    return authMgrService.execEnvIsWasm(groupId);
  }

  @ApiOperation(value = "query if node support wasm")
  @GetMapping("queryChainHasAuth")
  public Object queryChainHasAuth(String groupId) throws ContractException {
    return authMgrService.chainHasAuth(groupId);
  }

}
