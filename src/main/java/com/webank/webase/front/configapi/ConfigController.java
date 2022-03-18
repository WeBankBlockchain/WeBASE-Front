/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.configapi;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.Web3Config;
import com.webank.webase.front.base.properties.VersionProperties;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.configapi.entity.ConfigInfo;
import com.webank.webase.front.configapi.entity.ReqSdkConfig;
import com.webank.webase.front.version.VersionService;
import com.webank.webase.front.web3api.Web3ApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * get or update config local
 */
@Api(value = "/config", tags = "config interface")
@Slf4j
@RestController
@RequestMapping(value = "config")
public class ConfigController {
    @Autowired
    private Web3Config web3Config;
    @Autowired
    private Web3ApiService web3ApiService;
    @Autowired
    private VersionProperties versionProperties;
    @Autowired
    private VersionService versionService;
    @Autowired
    private ConfigService configService;


    @GetMapping("encrypt/{groupId}")
    public Integer getEncryptType(@PathVariable("groupId") String groupId) {
        int encrypt = web3ApiService.getCryptoType(groupId);
        log.info("getEncryptType groupId:{},type:{}", groupId, encrypt);
        return encrypt;
    }

    @GetMapping("sslCryptoType")
    public boolean getSSLCryptoType() {
        boolean sslCryptoType = configService.getSdkUseSmSsl();
        log.info("getSSLCryptoType:{}", sslCryptoType);
        return sslCryptoType;
    }



    /**
     * update sdk's peers configuration, use same sdk certificates to connect with peers
     * todo test update sdk
     * @return
     */
//    @PostMapping("bcosSDK")
//    public BaseResponse updateSDK(@RequestBody @Valid ReqSdkConfig param) {
//        Instant startTime = Instant.now();
//        log.info("start updateSDKPeers param:{}", param);
//        configService.configBcosSDK(param);
//        log.info("end updateSDKPeers useTime:{}",
//            Duration.between(startTime, Instant.now()).toMillis());
//        return new BaseResponse(ConstantCode.RET_SUCCESS);
//    }
//
//    @ApiOperation(value = "getSDKConfig", notes = "Get the latest sdk config")
//    @GetMapping("bcosSDK")
//    public BaseResponse getSDKConfig() {
//        Instant startTime = Instant.now();
//        log.info("start updateSDKPeers ");
//        Map<String, ConfigInfo> configInfoMap = configService.getConfigInfoSdk();
//        log.info("end updateSDKPeers useTime:{}",
//            Duration.between(startTime, Instant.now()).toMillis());
//        return new BaseResponse(ConstantCode.RET_SUCCESS, configInfoMap);
//    }


    /**
     * webase-web: when add first front, return version and tips
     * @return
     */
    @GetMapping("/version")
    public String getServerVersion() {
        return versionProperties.getVersion();
    }

    @GetMapping("/version/sign")
    public String getSignVersion() {
        return versionService.getSignServerVersion();
    }

}
