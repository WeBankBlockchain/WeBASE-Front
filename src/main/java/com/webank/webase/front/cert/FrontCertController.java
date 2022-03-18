/*
 * Copyright 2014-2020  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.cert;

import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.contract.entity.FileContentHandle;
import com.webank.webase.front.util.FrontUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * get Front's sdk's cert(.crt) in /resource
 * get ca.crt/node.crt of the node which linked with Front
 * expose api for node-manager's cert manage module
 */
@Api(value = "/cert", tags = "cert manage interface")
@Slf4j
@RestController
@RequestMapping(value = "cert")
public class FrontCertController {
    @Autowired
    FrontCertService certService;

    /**
     * get sdk cert without head and tail of "----BEGIN ----"
     * @return
     */
    @GetMapping("")
    public Object getFrontSdkCerts() {
        Instant startTime = Instant.now();
        log.info("start getFrontSdkCerts. startTime:{}", startTime.toEpochMilli());
        // node的crt文件可能包含节点、机构、链证书三个
        // sdk的node.crt文件一般包含sdk节点证书，机构证书两个

        // one crt file has multiple certs string
        Map<String, String> map = new HashMap<>();
        try {
            map = certService.getSDKNodeCert();
        }catch (FrontException e) {
            log.error("FrontCertController load [sdk] cert error: e:[]", e);
        }

        log.info("end getFrontSdkCerts. startTime:{}, certMap:{}",
                Duration.between(startTime, Instant.now()).toMillis(), map);
        return map;
    }

    /**
     * ecdsa ssl: get <"sdk.key", {sdkKeyContent}>
     * gm ssl: get <"gm/gmsdk.key", {sdkKeyContent}>
     * @return
     */
    @GetMapping("sdk")
    public Map<String, String> getSdkFiles() {
        Map<String, String> certList = certService.getSDKCertKeyMap();
        if (certList.isEmpty()) {
            log.error("getSdkFiles error return empty!");
            throw new FrontException(ConstantCode.SDK_CERT_FILE_NOT_FOUND);
        }
        return certList;
    }

    @GetMapping("sdk/zip")
    public ResponseEntity<InputStreamResource> getSdkCertZip() {
        Instant startTime = Instant.now();
        log.info("start getSdkCertZip startTime:{}", startTime.toEpochMilli());
        // get file
        FileContentHandle fileContentHandle = certService.getFrontSdkZipFile();
        log.info("end getSdkCertZip fileContentHandle:{}useTime:{}", fileContentHandle,
            Duration.between(startTime, Instant.now()).toMillis());
        return ResponseEntity.ok().headers(FrontUtils.headers(fileContentHandle.getFileName()))
            .body(new InputStreamResource(fileContentHandle.getInputStream()));
    }
}
