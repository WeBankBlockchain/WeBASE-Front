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

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.config.Web3Config;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.VersionProperties;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.configapi.entity.ConfigInfo;
import com.webank.webase.front.configapi.entity.ReqSdkConfig;
import com.webank.webase.front.configapi.entity.RspAccessWx;
import com.webank.webase.front.tool.entity.ReqSign;
import com.webank.webase.front.tool.entity.RspSignData;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.version.VersionService;
import com.webank.webase.front.web3api.Web3ApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

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
    @Autowired
    private RestTemplate restTemplate;

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

    /**
     * 校验wx后台
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @return
     */
    @GetMapping("")
    public String checkFromWxServer(@RequestParam(name = "signature") String signature,
        @RequestParam(name = "timestamp") String timestamp,
        @RequestParam(name = "nonce") String nonce,
        @RequestParam(name = "echostr") String echostr) {
        log.info("checkFromWxServer {}|{}|{}|{}", signature, timestamp, nonce, echostr);
        /*
        1）将token、timestamp、nonce三个参数进行字典序排序

        2）将三个参数字符串拼接成一个字符串进行sha1加密

        3）开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
         */
        return echostr;
    }

    private static final String appid = "wx8d2d1aa9f12503e5";
    private static final String secret = "bbcfadefc052625fdc17f80e9987bad8";

    /**
     * 此处的token是基础token
     * {"access_token":"72_nx2rZymJ-nDBkyE4GQrr8OGfTbfUFb7TvEnbe4uAK-KFw_-DQ6kJeExcBpTwEnUvLSYji8nfmAgZuLKhxiKpDewnIi9fk3ai0VQbZGoauG7hxWc3FhHKi8xKFuUZWZiAGAFCF","expires_in":7200}
     * 拿到基础token后，可以获取用户信息 接口为：https://api.weixin.qq.com/cgi-bin/user/info?
     * access_token=72_mVN2QSiqNpx31vaJdhaWxp5b7B14_K7n5jsGgbTuGKC8Z6kCRDAR5I_bO1GQ8XiaIeH8b_LbuP1OscUDxxGXhIZ3M_EIqi4AIF7nT-bZPgRhjSlVkseD4LipGzAVRCjAAANJU&openid=oQ3dT6SSdJOIMiIk20JNezcyDYnI
     * @return
     */
    @GetMapping("/loginwxbase")
    public String loginwx() {
        log.info("loginwx {}|{}, code:{},access_token:{}", appid, secret);
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
        log.info("loginwx url: {}", url);
        String finalUrl = String.format(url, appid, secret);
        log.info("loginwx url: {}", finalUrl);
        /*
           GET https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
           {"access_token":"ACCESS_TOKEN","expires_in":7200}
         */
        String response = restTemplateExchange(finalUrl, HttpMethod.GET, null, String.class);
        log.info("loginwx response: {}", response);

        return response;
    }

    /**
     * wxOpenId response:
     *{"access_token":"72_x3ndcM1sAfaAIVGQPL4RvnVK_p-iCepw9mAQLBkKPTuXTOop2kFEwzTVXkHGOpjKTedp3YmJ2nsjT7B1o4pL2JsHZhDZi9c8-kUFKR-3mcY","expires_in":7200,"refresh_token":"72_K9moQzPW78pFeFiD-5sAao_oZTVRO-hX1ecbY9Zexd4zmghQagF7YFGn68xJHiHJd5Ro0vi3E5tIHBm1zWamSsF_deWkgCgYVAs4PXNITxg",
     * "openid":"oQ3dT6SSdJOIMiIk20JNezcyDYnI","scope":"snsapi_base"}
     * 根据code获取 授权token
     * @param code
     * @return
     */
    @GetMapping("/wxopenid")
    public String wxOpenId(
        @RequestParam(name = "code", required = false) String code
    ) {
        log.info("wxOpenId {}|{}, code:{},access_token:{}", appid, secret, code);
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        log.info("wxOpenId url: {}", url);
        String finalUrl = String.format(url, appid, secret, code);
        log.info("wxOpenId url: {}", finalUrl);
        String response = restTemplateExchange(finalUrl, HttpMethod.GET, null, String.class);
        log.info("wxOpenId response: {}", response);

        return response;
    }


    /**
     * 拿到授权的token后，传入openid查询基础信息
     * userInfo: {
     * "openid":"oQ3dT6SSdJOIMiIk20JNezcyDYnI",
     * "nickname":"ç«è´°",// 乱码
     * "sex":0,"language":"","city":"","province":"","country":"",
     * "headimgurl":"https:\/\/thirdwx.qlogo.cn\/mmopen\/vi_32\/Uy5MVM2s7uKJtciafzaIbag8uUnyw98C1pAEV20ib7qojv14OFEeic8RndypibjFrjVlm2rzAU0wDsGQAZiaIh8DGDg\/132","privilege":[]}
     * @param openid
     * @param access_token
     * @return
     */
    @GetMapping("/wxuserInfo")
    public String userInfo(
        @RequestParam(name = "code", required = false) String code,
        @RequestParam(name = "openid", required = false) String openid,
        @RequestParam(name = "access_token", required = false) String access_token
    ) {

        log.info("wxOpenId {}|{}, code:{},access_token:{}", appid, secret, code);
        String urlAuth = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String urlAuthFinal = String.format(urlAuth, appid, secret, code);
        log.info("wxOpenId url: {}", urlAuthFinal);
        String response = restTemplateExchange(urlAuthFinal, HttpMethod.GET, null, String.class);
        RspAccessWx rspAccessWx = JsonUtils.toJavaObject(response, RspAccessWx.class);
        log.info("wxOpenId rspAccessWx: {}", rspAccessWx);
        openid = rspAccessWx.getOpenid();
        access_token = rspAccessWx.getAccess_token();
        log.info("userInfo {}|{}, openid:{},access_token:{}", appid, secret,
            openid, access_token);
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN";
        log.info("userInfo url: {}", url);
        String finalUrl = String.format(url, access_token, openid);
        log.info("userInfo url: {}", finalUrl);
        String userInfo = restTemplateExchange(finalUrl, HttpMethod.GET, null, String.class);
        log.info("userInfo response: {}", userInfo);

        return userInfo;
    }


    private <T> T restTemplateExchange(String url, HttpMethod method, Object param, Class<T> clazz) {
        try{
            log.info("restTemplateExchange url:{}, param:{}", url, param);
            HttpEntity entity = CommonUtils.buildHttpEntity(param);
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
            return response.getBody();
        } catch (ResourceAccessException ex) {
            log.error("getSignUserEntity fail restTemplateExchange", ex);
            throw new FrontException(ConstantCode.DATA_SIGN_NOT_ACCESSIBLE);
        } catch (HttpStatusCodeException e) {
            JsonNode error = JsonUtils.stringToJsonNode(e.getResponseBodyAsString());
            if (error == null) {
                throw e;
            }
            log.error("getSignUserEntity http request fail. error:{}", JsonUtils.toJSONString(error));
            // if return 404, no code or errorMessage
            int code = error.get("code").intValue();
            String errorMessage = error.get("errorMessage").asText();
            throw new FrontException(code, errorMessage);
        }
    }



}
