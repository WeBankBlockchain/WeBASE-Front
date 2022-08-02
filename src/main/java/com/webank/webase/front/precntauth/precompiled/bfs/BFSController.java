package com.webank.webase.front.precntauth.precompiled.bfs;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.precntauth.precompiled.bfs.entity.ReqCreateBFSInfo;
import com.webank.webase.front.precntauth.precompiled.bfs.entity.ReqQueryBFSInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "precntauth/precompiled/bfs", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/bfs")
public class BFSController {

  @Autowired
  private BFSServiceInWebase bfsServiceInWebase;

  /**
   * create bfs path
   */
  @ApiOperation(value = "create bfs path")
  @ApiImplicitParam(name = "reqCreateBFSInfo", value = "create bfs path info", required = true, dataType = "ReqCreateBFSInfo")
  @PostMapping("create")
  public Object createBfsPath(@Valid @RequestBody ReqCreateBFSInfo reqCreateBFSInfo)
      throws ContractException {
    return bfsServiceInWebase.mkdir(reqCreateBFSInfo.getGroupId(), reqCreateBFSInfo.getPath(),
        reqCreateBFSInfo.getSignUserId());
  }

  /**
   * query the bfs path info
   */
  @ApiOperation(value = "query the bfs path")
  @ApiImplicitParam(name = "reqQueryBFSInfo", value = "query bfs path info", required = true, dataType = "ReqQueryBFSInfo")
  @PostMapping("query")
  public Object queryBfsPath(@Valid @RequestBody ReqQueryBFSInfo reqQueryBFSInfo)
      throws ContractException {
    return bfsServiceInWebase.list(reqQueryBFSInfo.getGroupId(), reqQueryBFSInfo.getPath());
  }



}
