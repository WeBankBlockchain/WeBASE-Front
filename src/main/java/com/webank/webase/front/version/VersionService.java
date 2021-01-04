/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.front.version;

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class VersionService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private Constants constants;

    /**
     * get webase-sign version
     */
    public String getSignServerVersion() {
        try {
            // webase-sign api(v1.4.0) support
            String url = String.format(Constants.WEBASE_SIGN_VERSION_URI, constants.getKeyServer());
            log.info("getSignServerVersion url:{}", url);
            HttpHeaders headers = CommonUtils.buildHeaders();
            HttpEntity<String> formEntity =
                new HttpEntity<String>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(url,
                HttpMethod.GET, formEntity, String.class);
            log.info("getSignServerVersion response:{}", response);
            return response.getBody();
        } catch (ResourceAccessException ex) {
            log.error("fail restTemplateExchange", ex);
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        } catch (HttpStatusCodeException e) {
            JsonNode error = JsonUtils.stringToJsonNode(e.getResponseBodyAsString());
            log.error("http request fail. error:{}", JsonUtils.toJSONString(error));
            try {
                // if return 404, no code or errorMessage
                int code = error.get("code").intValue();
                String errorMessage = error.get("errorMessage").asText();
                throw new FrontException(code, errorMessage);
            } catch (Exception ex) {
                throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
            }
        } catch (Exception e) {
            log.error("getSignUserEntity exception", e);
            throw new FrontException(ConstantCode.DATA_SIGN_ERROR);
        }
    }

}
