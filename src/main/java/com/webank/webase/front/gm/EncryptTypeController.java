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

package com.webank.webase.front.gm;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * return encrypt type to web 0 is standard, 1 is guomi
 */
@Api(value = "/encrypt", tags = "encrypt type interface(standard/guomi)")
@Slf4j
@RestController
@RequestMapping(value = "")
public class EncryptTypeController {
    @Autowired
    @Qualifier("common")
    private CryptoSuite cryptoSuite;
    @Autowired
    private BcosSDK bcosSDK;

    @GetMapping("encrypt")
    public Integer getEncryptType() {
        int encrypt = cryptoSuite.cryptoTypeConfig;
        log.info("getEncryptType:{}", encrypt);
        return encrypt;
    }
    
    @GetMapping("sslCryptoType")
    public Integer getSSLCryptoType() {
        int sslCryptoType = bcosSDK.getSSLCryptoType();
        log.info("getSSLCryptoType:{}", sslCryptoType);
        return sslCryptoType;
    }
}
