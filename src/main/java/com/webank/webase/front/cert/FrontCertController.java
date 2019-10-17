/*
 * Copyright 2014-2019  the original author or authors.
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

import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "/cert", tags = "cert manage interface")
@Slf4j
@RestController
@RequestMapping(value = "cert")
public class FrontCertController {
    @Autowired
    FrontCertService certService;
    // TODO函数命名
    @GetMapping("")
    public Object getFrontCerts() {
        Instant startTime = Instant.now();
        log.info("start getFrontCerts. startTime:{}", startTime.toEpochMilli());
        List<String> certList = new ArrayList<>();
        String chainCertStr;
        String nodeCrtStr;
        String agencyCrtStr;
        // node的crt文件可能包含节点、机构、链证书三个
        try {
            certList = certService.getNodeCerts();
            chainCertStr = certService.getChainCrt();
        }catch (FrontException e) {
            return new BaseResponse(ConstantCode.CERT_FILE_NOT_FOUND, e.getMessage());
        }
        Map<String, String> map = new HashMap<>();
        nodeCrtStr = certList.get(0);
        agencyCrtStr = certList.get(1);
        if(checkCertStrNonNull(nodeCrtStr)) {
            map.put("node", nodeCrtStr);
        }
        if(checkCertStrNonNull(agencyCrtStr)) {
            map.put("agency", agencyCrtStr);
        }
        if(checkCertStrNonNull(chainCertStr)) {
            map.put("chain", chainCertStr);
        }
        log.info("end getFrontCerts. startTime:{}, certMap:{}",
                Duration.between(startTime, Instant.now()).toMillis(), map);
        return map;
    }

    private boolean checkCertStrNonNull(String certStr) {
        if(certStr != "" || certStr != null) {
            return true;
        } else {
            return false;
        }
    }
}
