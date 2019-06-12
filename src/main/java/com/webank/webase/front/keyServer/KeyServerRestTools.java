/**
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.front.keyServer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.util.CommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * about http request for webase-front.
 */
@Log4j2
@Service
public class KeyServerRestTools {

    private static final int REST_RSP_SUCCESS = 0;
    public static final String URI_NEW_USER = "user/newUser";
    public static final String URI_GET_USER = "%%1d/%%2s/user/userInfo";
    public static final String URI_IMPORT_USER = "user/import";
    public static final String URI_SIGN = "sign";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Constants constants;


    /**
     * get data from response.
     */
    private <T> T getResponseData(BaseResponse rsp, Class<T> clazz) {
        if (Objects.isNull(rsp)) {
            return null;
        }
        if (REST_RSP_SUCCESS != rsp.getCode()) {
            log.warn("fail getResponseData. restResponse:{}", JSON.toJSONString(rsp));
            throw new FrontException(rsp.getCode(), rsp.getMessage());
        }
        return Optional.ofNullable(rsp.getData()).map(d -> CommonUtils.object2JavaBean(d, clazz))
            .orElse(null);
    }

    /**
     * get for java bean.
     */
    public <T> T getForJaveBean(String uri, Class<T> clazz) {
        BaseResponse rsp = restTemplateExchange(uri, HttpMethod.GET, null, BaseResponse.class);
        return getResponseData(rsp, clazz);
    }

    /**
     * post for java bean.
     */
    public <T> T postForJaveBean(String uri, Object param, Class<T> clazz) {
        BaseResponse rsp = restTemplateExchange(uri, HttpMethod.POST, param, BaseResponse.class);
        return getResponseData(rsp, clazz);
    }


    /**
     * build  url of front service.
     */
    private String buildUrl(ArrayList<String> list, String uri) {
        Collections.shuffle(list);//random one
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String ipPort = iterator.next();
            String url = String.format(constants.getKeyServerUrl(), ipPort, uri)
                .replaceAll(" ", "");
            iterator.remove();
            return url;
        }
        log.info("end buildUrl. url is null");
        return null;
    }

    /**
     * build httpEntity
     */
    public static HttpEntity buildHttpEntity(Object param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String paramStr = null;
        if (Objects.nonNull(param)) {
            paramStr = JSON.toJSONString(param);
        }
        HttpEntity requestEntity = new HttpEntity(paramStr, headers);
        return requestEntity;
    }


    /**
     * restTemplate exchange.
     */
    private <T> T restTemplateExchange(String uri, HttpMethod method, Object param,
        Class<T> clazz) {
        String[] ipPortArr = constants.getKeyServerIpPort().split(",");
        if (ipPortArr == null || ipPortArr.length == 0) {
            log.error("fail restTemplateExchange. ipPortArr is empty");
            throw new FrontException(ConstantCode.NO_CONFIG_KEY_SERVER);
        }
        ArrayList<String> list = new ArrayList<>(Arrays.asList(ipPortArr));

        while (list != null && list.size() > 0) {
            String url = buildUrl(list, uri);//build url
            try {
                HttpEntity entity = buildHttpEntity(param);// build entity
                ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
                return response.getBody();
            } catch (ResourceAccessException ex) {
                log.warn("fail request,url:{}", url, ex);
                log.info("continue next front");
                continue;
            } catch (HttpStatusCodeException e) {
                JSONObject error = JSONObject.parseObject(e.getResponseBodyAsString());
                log.error("http request fail.url:{} error:{}", url, JSON.toJSONString(error));
                throw new FrontException(error.getInteger("code"), error.getString("errorMessage"));
            }
        }
        return null;
    }
}