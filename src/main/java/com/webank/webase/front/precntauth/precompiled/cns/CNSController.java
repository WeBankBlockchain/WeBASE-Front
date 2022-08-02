package com.webank.webase.front.precntauth.precompiled.cns;

import com.webank.webase.front.precntauth.precompiled.cns.entity.ReqCnsInfoByName;
import com.webank.webase.front.precntauth.precompiled.cns.entity.ReqInfoByNameVersion;
import com.webank.webase.front.precntauth.precompiled.cns.entity.ReqRegisterCnsInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "precntauth/precompiled/cns", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/cns")
public class CNSController {

  @Autowired
  private CNSServiceInWebase cnsServiceInWebase;

  @ApiOperation(value = "register the cns info")
  @ApiImplicitParam(name = "reqCnsInfo", value = "register info", required = true, dataType = "ReqRegisterCnsInfo")
  @PostMapping("register")
  public Object registerCNS(@Valid @RequestBody ReqRegisterCnsInfo reqCnsInfo)
      throws ContractException {
    return cnsServiceInWebase.registerCNS(reqCnsInfo.getGroupId(), reqCnsInfo.getSignUserId(),
        reqCnsInfo.getContractName(), reqCnsInfo.getContractVersion(),
        reqCnsInfo.getContractAddress(), reqCnsInfo.getAbiData());
  }

  @ApiOperation(value = "query the cns info by name")
  @ApiImplicitParam(name = "reqCnsInfoByName", value = "queryCns info", required = true, dataType = "ReqCnsInfoByName")
  @PostMapping("queryCnsByName")
  public Object queryCnsInfoByName(@Valid @RequestBody ReqCnsInfoByName reqCnsInfoByName)
      throws ContractException {
    return cnsServiceInWebase.queryCnsInfoByName(reqCnsInfoByName.getGroupId(),
        reqCnsInfoByName.getContractName());
  }

  @ApiOperation(value = "query the cns info by name version")
  @ApiImplicitParam(name = "reqCnsInfoByNameVersion", value = "name and version info", required = true, dataType = "ReqInfoByNameVersion")
  @PostMapping("queryCnsByNameVersion")
  public Object queryCnsByNameVersion(
      @Valid @RequestBody ReqInfoByNameVersion reqCnsInfoByNameVersion)
      throws ContractException {
    return cnsServiceInWebase.queryCnsByNameAndVersion2(reqCnsInfoByNameVersion.getGroupId(),
        reqCnsInfoByNameVersion.getContractName(), reqCnsInfoByNameVersion.getVersion());
  }

  @ApiOperation(value = "query the address info by name version")
  @ApiImplicitParam(name = "reqAddressInfoByNameVersion", value = "name and version info", required = true, dataType = "ReqInfoByNameVersion")
  @PostMapping("reqAddressInfoByNameVersion")
  public Object queryAddressByNameVersion(
      @Valid @RequestBody ReqInfoByNameVersion reqAddressInfoByNameVersion)
      throws ContractException {
    return cnsServiceInWebase.getAddressByContractNameAndVersion(reqAddressInfoByNameVersion.getGroupId(),
        reqAddressInfoByNameVersion.getContractName(), reqAddressInfoByNameVersion.getVersion());
  }

}
