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
package com.webank.webase.front.base.config;

import com.webank.webase.front.base.properties.Constants;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Restful request template configuration
 */
@Data
@Configuration
public class RestTemplateConfig {
    @Autowired
    private Constants constants;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = getHttpFactoryForDeploy();
        // ms
        factory.setReadTimeout(constants.getHttp_read_timeOut());
        // ms
        factory.setConnectTimeout(constants.getHttp_connect_timeOut());
        return new RestTemplate(factory);
    }

    /**
     * factory for deploy.
     */
    @Bean()
    @Scope("prototype")
    public SimpleClientHttpRequestFactory getHttpFactoryForDeploy() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        return factory;
    }

//    /**
//     * httpRequestFactory.
//     *
//     * @return
//     */
//    @Bean
//    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
//
//        PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(
//                30, TimeUnit.SECONDS);
//        // max connection
//        pollingConnectionManager.setMaxTotal(constants.getRestTemplateMaxTotal());
//
//        pollingConnectionManager.setDefaultMaxPerRoute(constants.getRestTemplateMaxPerRoute());
//        HttpClientBuilder httpClientBuilder = HttpClients.custom();
//        httpClientBuilder.setConnectionManager(pollingConnectionManager);
//        // add Keep-Alive
//        httpClientBuilder
//                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
//        HttpClient httpClient = httpClientBuilder.build();
//        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
//                new HttpComponentsClientHttpRequestFactory(httpClient);
//        clientHttpRequestFactory.setReadTimeout(constants.getHttp_read_timeOut());
//        clientHttpRequestFactory.setConnectTimeout(constants.getHttp_connect_timeOut());
//        return clientHttpRequestFactory;
//    }
}
