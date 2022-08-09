package com.webank.webase.front.precntauth.authmanager.everyone;

import com.webank.webase.front.precntauth.authmanager.everyone.entity.ReqCheckMethodAuthInfo;
import com.webank.webase.front.precntauth.authmanager.everyone.entity.ReqContractAdminInfo;
import com.webank.webase.front.precntauth.authmanager.everyone.entity.ReqContractStatusList;
import com.webank.webase.front.precntauth.authmanager.everyone.entity.ReqProposalInfo;
import com.webank.webase.front.precntauth.authmanager.everyone.entity.ReqProposalListInfo;
import com.webank.webase.front.precntauth.authmanager.everyone.entity.ReqUsrDeployAuthInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "precntauth/authmanager/everyone/", tags = "precntauth authmanager controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/authmanager/everyone/")
public class EveryoneController {

  @Autowired
  private EveryoneService everyoneService;

  @ApiOperation(value = "query the committee info")
  @ApiImplicitParam(name = "groupId", value = "committee info", required = true)
  @GetMapping("cmtInfo")
  public List<Object> queryCommittee(String groupId) throws ContractException {
    return everyoneService.queryCommitteeInfo(groupId);
  }

  /**
   * 获取当前全局部署的权限策略 策略类型：0则无策略，1则为白名单模式，2则为黑名单模式
   */
  @ApiOperation(value = "query the deploy type")
  @ApiImplicitParam(name = "groupId", value = "groupId", required = true)
  @GetMapping("deploy/type")
  public BigInteger queryDeployAuthType(String groupId)
      throws ContractException {
    return everyoneService.queryDeployAuthType(groupId);
  }

  @ApiOperation(value = "query the proposal info")
  @ApiImplicitParam(name = "reqProposalInfo", value = "proposal info", required = true,
      dataType = "ReqProposalInfo")
  @PostMapping("proposalInfo")
  public List<Object> queryProposal(@Valid @RequestBody ReqProposalInfo reqProposalInfo)
      throws ContractException {
    return everyoneService.queryProposalInfo(reqProposalInfo.getGroupId(),
        reqProposalInfo.getProposalId());
  }

  @ApiOperation(value = "query the proposal info list")
  @ApiImplicitParam(name = "reqProposalListInfo", value = "proposal info list", required = true,
      dataType = "ReqProposalListInfo")
  @PostMapping("proposalInfoList")
  public List<Object> queryProposalList(@Valid @RequestBody ReqProposalListInfo reqProposalListInfo)
      throws ContractException {
    return everyoneService.queryProposalInfoList(reqProposalListInfo);
  }

  @ApiOperation(value = "query the proposal info count")
  @ApiImplicitParam(name = "groupId", value = "proposal info count", required = true)
  @GetMapping("proposalInfoCount")
  public BigInteger queryProposalList(String groupId)
      throws ContractException {
    return everyoneService.queryProposalInfoCount(groupId);
  }

  /**
   * 检查账号是否具有全局部署权限
   */
  @ApiOperation(value = "query the user deploy auth")
  @ApiImplicitParam(name = "reqUsrDeployAuthInfo", value = "usrDeployAuth info", required = true,
      dataType = "ReqUsrDeployAuthInfo")
  @PostMapping("usr/deploy")
  public Boolean checkDeployAuth(@Valid @RequestBody ReqUsrDeployAuthInfo reqUsrDeployAuthInfo)
      throws ContractException {
    Boolean res = everyoneService.checkDeployAuth(reqUsrDeployAuthInfo.getGroupId(),
        reqUsrDeployAuthInfo.getUserAddress());
    return res;
  }

  /**
   * 查询合约的管理员
   */
  @ApiOperation(value = "query the contract's admin")
  @ApiImplicitParam(name = "reqContractAdminInfo", value = "contractAdmin info", required = true,
      dataType = "ReqContractAdminInfo")
  @PostMapping("contract/admin")
  public String queryAdmin(@Valid @RequestBody ReqContractAdminInfo reqContractAdminInfo)
      throws ContractException {
    return everyoneService.queryAdmin(reqContractAdminInfo.getGroupId(),
        reqContractAdminInfo.getContractAddr());
  }


  /**
   * 查询某用户地址对合约函数的访问是否有权限
   */
  @ApiOperation(value = "query the userAddress permission of contract")
  @ApiImplicitParam(name = "reqCheckMethodAuthInfo", value = "contractAdmin info", required = true,
      dataType = "ReqCheckMethodAuthInfo")
  @PostMapping("contract/method/auth")
  public Boolean checkMethodAuth(@Valid @RequestBody ReqCheckMethodAuthInfo reqCheckMethodAuthInfo)
      throws ContractException {
    return everyoneService.checkMethodAuth(reqCheckMethodAuthInfo.getGroupId(),
        reqCheckMethodAuthInfo.getContractAddr(), reqCheckMethodAuthInfo.getFunc(),
        reqCheckMethodAuthInfo.getUserAddress());
  }


  /**
   * 查询某用户地址对合约函数的访问是否有权限
   */
  @ApiOperation(value = "query the contract address whether available")
  @ApiImplicitParam(name = "reqContractAdminInfo", value = "contractAdmin info", required = true,
      dataType = "ReqContractAdminInfo")
  @PostMapping("contract/status")
  public Boolean checkContractAvailable(@Valid @RequestBody ReqContractAdminInfo reqContractAdminInfo)
      throws ContractException {
    return everyoneService.isContractAvailable(reqContractAdminInfo.getGroupId(),
        reqContractAdminInfo.getContractAddr());
  }

  /**
   * 查询某用户地址对合约函数的访问是否有权限
   */
  @ApiOperation(value = "query list of the contract address whether available")
  @ApiImplicitParam(name = "reqContractStatusList", value = "contract status info", required = true,
      dataType = "ReqContractStatusList")
  @PostMapping("contract/status/list")
  public Map<String, Boolean> checkContractListAvailable(@Valid @RequestBody ReqContractStatusList reqContractStatusList)
      throws ContractException {
    return everyoneService.listContractStatus(reqContractStatusList.getGroupId(),
        reqContractStatusList.getContractAddressList());
  }


}