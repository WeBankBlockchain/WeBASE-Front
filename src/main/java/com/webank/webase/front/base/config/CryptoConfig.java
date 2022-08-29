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

package com.webank.webase.front.base.config;

import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * init bean in utils
 * @author marsli
 */
@Configuration
public class CryptoConfig {

    @Bean(name = "ecdsa")
    public CryptoSuite getECDSASuite() {
        return new CryptoSuite(CryptoType.ECDSA_TYPE);
    }

    @Bean(name = "sm")
    public CryptoSuite getGuomiSuite() {
        return new CryptoSuite(CryptoType.SM_TYPE);
    }

}
