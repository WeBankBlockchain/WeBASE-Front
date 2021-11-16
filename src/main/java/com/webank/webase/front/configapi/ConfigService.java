/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.configapi;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.Web3Config;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.configapi.entity.ReqPeers;
import com.webank.webase.front.util.CommonUtils;
import java.util.List;
import java.util.Stack;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConfigService {
    @Autowired
    private Web3Config web3Config;
    @Autowired
    private Stack<BcosSDK> bcosSDKs;

    public synchronized void updateBcosSDKPeers(ReqPeers param) {
        log.info("updateBcosSDKPeers param:{}", param);
        List<String> newPeers = param.getPeers();
        if (newPeers == null || newPeers.isEmpty()) {
            throw new FrontException(ConstantCode.PARAM_ERROR_EMPTY_PEERS);
        }
        List<String> oldPeers = web3Config.getPeers();
        boolean isSame = CommonUtils.compareStrList(oldPeers, newPeers);
        if (isSame) {
            log.warn("same peers with old peers in BcosSDK!");
            throw new FrontException(ConstantCode.SAME_SDK_PEERS_ERROR);
        }
        log.info("updateBcosSDKPeers newPeers:{},oldPeers:{}", newPeers, oldPeers);
        BcosSDK newBcosSDK = null;
        try {
            newBcosSDK = web3Config.buildBcosSDK(param.getPeers());
        } catch (ConfigException | JniException e) {
            log.error("updateBcosSDKPeers error:[]", e);
            throw new FrontException(ConstantCode.BUILD_SDK_WITH_NEW_PEERS_FAILED);
        }
        // todo 保存peers配置到db

        if (newBcosSDK != null) {
            bcosSDKs.pop();
            bcosSDKs.push(newBcosSDK);
        }
    }

}
