package com.webank.webase.front.precntauth.authmanager.committee;

import com.webank.webase.front.precntauth.authmanager.committee.entity.ReqDeployAuthTypeInfo;
import com.webank.webase.front.precntauth.authmanager.committee.entity.ReqRevokeProposalInfo;
import com.webank.webase.front.precntauth.authmanager.committee.entity.ReqVoteProposalInfo;
import com.webank.webase.front.precntauth.authmanager.committee.entity.ReqResetAdminInfo;
import com.webank.webase.front.precntauth.authmanager.committee.entity.ReqSetRateInfo;
import com.webank.webase.front.precntauth.authmanager.committee.entity.ReqUpdateGovernorInfo;
import com.webank.webase.front.precntauth.authmanager.committee.entity.ReqUsrDeployInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api(value = "precntauth/authmanager/committee/", tags = "precntauth authmanager controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/authmanager/committee/")
public class CommitteeController {

  @Autowired
  private CommitteeService committeeService;

  @ApiOperation(value = "update committee governor")
  @ApiImplicitParam(name = "reqUpdateGovernorInfo", value = "governor info", required = true
      , dataType = "ReqUpdateGovernorInfo")
  @PostMapping("governor")
  public Object updateGovernor(@Valid @RequestBody ReqUpdateGovernorInfo reqUpdateGovernorInfo)
      throws ContractException, ContractCodecException, TransactionException, IOException {
    return committeeService.updateGovernor(reqUpdateGovernorInfo.getGroupId(),
        reqUpdateGovernorInfo.getSignUserId(), reqUpdateGovernorInfo.getAccountAddress(),
        reqUpdateGovernorInfo.getWeight());
  }

  @ApiOperation(value = "set committee rate")
  @ApiImplicitParam(name = "reqSetRateInfo", value = "rate info", required = true,
      dataType = "ReqSetRateInfo")
  @PostMapping("rate")
  public Object setRate(@Valid @RequestBody ReqSetRateInfo reqSetRateInfo)
      throws ContractException, ContractCodecException, TransactionException, IOException {
    return committeeService.setRate(reqSetRateInfo.getGroupId(), reqSetRateInfo.getSignUserId(),
        reqSetRateInfo.getParticipatesRate(),
        reqSetRateInfo.getWinRate());
  }

  @ApiOperation(value = "set deploy type")
  @ApiImplicitParam(name = "reqDeployAuthTypeInfo", value = "DeployAuthTypeInfo", required = true,
      dataType = "ReqDeployAuthTypeInfo")
  @PostMapping("deploy/type")
  public Object setDeployAuthType(@Valid @RequestBody ReqDeployAuthTypeInfo reqDeployAuthTypeInfo) {
    return committeeService.setDeployAuthType(reqDeployAuthTypeInfo.getGroupId(),
        reqDeployAuthTypeInfo.getSignUserId(), reqDeployAuthTypeInfo.getDeployAuthType());
  }

  @ApiOperation(value = "modify deploy user", notes = "openFlag value is true or false")
  @ApiImplicitParam(name = "reqUsrDeployInfo", value = "usrDeployAuth info", required = true,
      dataType = "ReqUsrDeployInfo")
  @PostMapping("usr/deploy")
  public Object modifyDeployAuth(@Valid @RequestBody ReqUsrDeployInfo reqUsrDeployInfo) {
    return committeeService.modifyDeployAuth(reqUsrDeployInfo.getGroupId(),
        reqUsrDeployInfo.getSignUserId(), reqUsrDeployInfo.getOpenFlag(),
        reqUsrDeployInfo.getUserAddress());
  }

  @ApiOperation(value = "reset the admin of contract")
  @ApiImplicitParam(name = "reqResetAdminInfo", value = "resetAdmin info", required = true,
      dataType = "ReqResetAdminInfo")
  @PostMapping("contract/admin")
  public Object resetAdmin(@Valid @RequestBody ReqResetAdminInfo reqResetAdminInfo)
      throws ContractCodecException, TransactionException, ContractException, IOException {
    return committeeService.resetAdmin(reqResetAdminInfo.getGroupId(),
        reqResetAdminInfo.getSignUserId(), reqResetAdminInfo.getNewAdmin(),
        reqResetAdminInfo.getContractAddr());
  }

  @ApiOperation(value = "revoke the before proposal")
  @ApiImplicitParam(name = "reqRevokeProposalInfo", value = "revokeProposal info", required = true,
      dataType = "ReqRevokeProposalInfo")
  @PostMapping("proposal/revoke")
  public Object revokeProposal(@Valid @RequestBody ReqRevokeProposalInfo reqRevokeProposalInfo)
      throws ContractCodecException, TransactionException, ContractException, IOException {
    return committeeService.revokeProposal(reqRevokeProposalInfo.getGroupId(),
        reqRevokeProposalInfo.getSignUserId(), reqRevokeProposalInfo.getProposalId());
  }

  @ApiOperation(value = "vote the proposal")
  @ApiImplicitParam(name = "reqVoteProposalInfo", value = "voteProposal info", required = true,
      dataType = "ReqVoteProposalInfo")
  @PostMapping("proposal/vote")
  public Object voteProposal(@Valid @RequestBody ReqVoteProposalInfo reqVoteProposalInfo)
      throws ContractCodecException, TransactionException, ContractException, IOException {
    return committeeService.voteProposal(reqVoteProposalInfo.getGroupId(),
        reqVoteProposalInfo.getSignUserId(), reqVoteProposalInfo.getProposalId(),
        reqVoteProposalInfo.getAgree());
  }

}
