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
package com.webank.webase.front.rpc.precompiled.sysconf;

import static org.fisco.bcos.sdk.contract.precompiled.sysconfig.SystemConfigPrecompiled.FUNC_SETVALUEBYKEY;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.rpc.precompiled.base.PrecompiledCommonInfo;
import com.webank.webase.front.rpc.precompiled.sysconf.entity.ReqQuerySysConfigInfo;
import com.webank.webase.front.rpc.precompiled.sysconf.entity.ReqSetSysConfigInfo;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *  Sys config service;
 *  Handle transaction through webase-sign.
 */
@Slf4j
@Service
public class SysConfigServiceInWebase {

    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    TransService transService;

    /**
     * get system config list by groupId directory through web3j instead of Precompiled instance
     *
     * @param groupId
     * @return
     * @throws IOException
     */
    public List<Object> querySysConfigByGroupId(String groupId) {
        List<Object> list = getConfigList(groupId);
        return list;
    }

    private List<Object> getConfigList(String groupId) {
        List<Object> list = new ArrayList<>();
        String txCountLimit = web3ApiService.getWeb3j(groupId)
            .getSystemConfigByKey(PrecompiledUtils.TxCountLimit).getSystemConfig().getValue();
        ReqQuerySysConfigInfo systemConfigCount = new ReqQuerySysConfigInfo();
        systemConfigCount.setConfigKey(PrecompiledUtils.TxCountLimit);
        systemConfigCount.setConfigValue(txCountLimit);
        systemConfigCount.setGroupId(groupId);

        String txGasLimit = web3ApiService.getWeb3j(groupId)
            .getSystemConfigByKey(PrecompiledUtils.TxGasLimit).getSystemConfig().getValue();
        ReqQuerySysConfigInfo systemConfigGas = new ReqQuerySysConfigInfo();
        systemConfigGas.setConfigKey(PrecompiledUtils.TxGasLimit);
        systemConfigGas.setConfigValue(txGasLimit);
        systemConfigGas.setGroupId(groupId);

        list.add(systemConfigCount);
        list.add(systemConfigGas);
        return list;
    }


    /**
     * System config related 启动项目时，检查是否已有table 否则Create table sysconfig(groupId, from key, value)
     */
    public Object setSysConfigValueByKey(ReqSetSysConfigInfo reqSetSysConfigInfo) {
        String groupId = reqSetSysConfigInfo.getGroupId();
        String signUserId = reqSetSysConfigInfo.getSignUserId();
        String key = reqSetSysConfigInfo.getConfigKey();
        String value = reqSetSysConfigInfo.getConfigValue();

        // check system value
        // check gas limit
        if (PrecompiledUtils.TxGasLimit.equals(key)) {
            if (Long.parseLong(value) < PrecompiledUtils.TxGasLimitMin ||
                Long.parseLong(value) > PrecompiledUtils.TxGasLimitMax) {
                return ConstantCode.SET_SYSTEM_CONFIG_GAS_RANGE_ERROR;
            }
        }

        // @param result {"code":0,"msg":"success"}
        String result = this.setValueByKey(groupId, signUserId, key, value);
        return result;
    }

    public String setValueByKey(String groupId, String signUserId, String key, String value) {
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(key);
        funcParams.add(value);
        // get address and abi of precompiled contract
        String contractAddress;
        if (web3ApiService.getWeb3j(groupId).isWASM()) {
            contractAddress = PrecompiledCommonInfo.getAddress(
                PrecompiledTypes.SYSTEM_CONFIG_LIQUID);
        } else {
            contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.SYSTEM_CONFIG);
        }
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.SYSTEM_CONFIG);
        // execute set method
        TransactionReceipt receipt =
            (TransactionReceipt) transService.transHandleWithSign(groupId,
                signUserId, contractAddress, abiStr, FUNC_SETVALUEBYKEY, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String getSysConfigByKey(String groupId, String key) {
        String result = web3ApiService.getWeb3j(groupId).getSystemConfigByKey(key).getSystemConfig()
            .getValue();
        return result;
    }

    private String handleTransactionReceipt(TransactionReceipt receipt) {
        log.debug("handle tx receipt of precompiled");
        try {
            RetCode sdkRetCode = ReceiptParser.parseTransactionReceipt(receipt);
            log.info("handleTransactionReceipt sdkRetCode:{}", sdkRetCode);
            if (sdkRetCode.getCode() >= 0) {
                return new BaseResponse(ConstantCode.RET_SUCCESS,
                    sdkRetCode.getMessage()).toString();
            } else {
                throw new FrontException(sdkRetCode.getCode(), sdkRetCode.getMessage());
            }
        } catch (ContractException e) {
            log.error("handleTransactionReceipt e:[]", e);
            throw new FrontException(e.getErrorCode(), e.getMessage());
        }
    }
}
