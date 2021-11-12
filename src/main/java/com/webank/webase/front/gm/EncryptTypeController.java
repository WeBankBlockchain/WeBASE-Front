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

import com.webank.webase.front.base.config.Web3Config;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Web3Config web3Config;

//    @GetMapping("encrypt")
//    public Integer getEncryptType() {
//        int encrypt = web3ApiService.getCryptoType();
//        log.info("getEncryptType:{}", encrypt);
//        return encrypt;
//    }
//    todo 根据群组获取加密类型

    @GetMapping("sslCryptoType")
    public boolean getSSLCryptoType() {
        boolean sslCryptoType = web3Config.isUseSmSsl();
        log.info("getSSLCryptoType:{}", sslCryptoType);
        return sslCryptoType;
    }
}
