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
package com.webank.webase.front.precompiledapi.sysconf;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.precompiledapi.PrecompiledWithSignService;
import com.webank.webase.front.precompiledapi.entity.ResSystemConfig;
import com.webank.webase.front.precompiledapi.entity.SystemConfigHandle;
import com.webank.webase.front.util.PrecompiledUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * System config service
 */
@Service
public class PrecompiledSysConfigService {

    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    PrecompiledWithSignService precompiledWithSignService;


    /**
     * System config related 启动项目时，检查是否已有table 否则Create table sysconfig(groupId, from key, value)
     */
    public Object setSysConfigValueByKey(SystemConfigHandle systemConfigHandle) throws Exception {
        int groupId = systemConfigHandle.getGroupId();
        String signUserId = systemConfigHandle.getSignUserId();
        String key = systemConfigHandle.getConfigKey();
        String value = systemConfigHandle.getConfigValue();

        // check system value
        if (PrecompiledUtils.TxGasLimit.equals(key)) {
            if (Integer.parseInt(value) < PrecompiledUtils.TxGasLimitMin) {
                return ConstantCode.FAIL_SET_SYSTEM_CONFIG_TOO_SMALL;
            }
        }
        // @param result {"code":0,"msg":"success"}
        String result = precompiledWithSignService.setValueByKey(groupId, signUserId, key, value);
        return result;
    }

    public List<ResSystemConfig> querySysConfigByGroupId(int groupId) throws Exception {

        List<ResSystemConfig> list = getConfigList(groupId);

        return list;
    }

    /**
     * get system config list by groupId directory through web3j instead of Precompiled instance
     * 
     * @param groupId
     * @return
     * @throws IOException
     */
    private List<ResSystemConfig> getConfigList(int groupId) throws IOException {
        List<ResSystemConfig> list = new ArrayList<>();

        String txCountLimit = web3ApiService.getWeb3j(groupId)
                .getSystemConfigByKey(PrecompiledUtils.TxCountLimit).sendForReturnString();
        ResSystemConfig systemConfigCount = new ResSystemConfig();
        systemConfigCount.setConfigKey(PrecompiledUtils.TxCountLimit);
        systemConfigCount.setConfigValue(txCountLimit);
        systemConfigCount.setGroupId(groupId);

        String txGasLimit = web3ApiService.getWeb3j(groupId)
                .getSystemConfigByKey(PrecompiledUtils.TxGasLimit).sendForReturnString();
        ResSystemConfig systemConfigGas = new ResSystemConfig();
        systemConfigGas.setConfigKey(PrecompiledUtils.TxGasLimit);
        systemConfigGas.setConfigValue(txGasLimit);
        systemConfigGas.setGroupId(groupId);

        list.add(systemConfigCount);
        list.add(systemConfigGas);
        return list;
    }

    public String getSysConfigByKey(int groupId, String key) throws Exception {
        // 校验
        String result = web3ApiService.getWeb3j(groupId).getSystemConfigByKey(key).sendForReturnString();
        return result;

    }



}
