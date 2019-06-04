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

import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keyServer.entity.ReqNewUserDto;
import com.webank.webase.front.keyServer.entity.ReqSignDto;
import com.webank.webase.front.keyServer.entity.RspSignDto;
import com.webank.webase.front.keyServer.entity.RspUserInfoDto;
import com.webank.webase.front.keystore.KeyStoreInfo;
import com.webank.webase.front.util.CommonUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * interface of key server.
 */
@Service
public class KeyServerService {

    @Autowired
    private KeyServerRestTools restTools;

    /**
     * new user.
     */
    public RspUserInfoDto newUser(ReqNewUserDto param) {
        String uri = KeyServerRestTools.URI_NEW_USER;
        return restTools.postForJaveBean(uri, param, RspUserInfoDto.class);
    }

    /**
     * get userInfo.
     */
    public RspUserInfoDto getUser(String userName) {
        String uri = String.format(KeyServerRestTools.URI_GET_USER, userName);
        return restTools.getForJaveBean(uri, RspUserInfoDto.class);
    }

    /**
     * get KeyStoreInfo by privateKey.
     */
    public KeyStoreInfo importUser(String privateKey, String userName) {
        List<String> nameList = Arrays.asList("privateKey", "userName");
        List<Object> valueList = Arrays.asList(privateKey, userName);
        // request param to str
        String urlParam = CommonUtils.convertUrlParam(nameList, valueList);

        String uri = KeyServerRestTools.URI_IMPORT_USER + "?" + urlParam;
        return restTools.getForJaveBean(uri, KeyStoreInfo.class);
    }


    /**
     * sign by cloud.
     */
    private String cloudSign(byte[] encodedTransaction, String userName) {
        String encodedDataStr = new String(encodedTransaction);
        ReqSignDto reqSignDto = new ReqSignDto(userName, encodedDataStr);
        String uri = KeyServerRestTools.URI_SIGN;
        //sign by key server
        RspSignDto signRsp = restTools.postForJaveBean(uri, reqSignDto, RspSignDto.class);
        return Optional.ofNullable(signRsp).map(RspSignDto::getSignDataStr)
            .orElseThrow(() -> new FrontException(ConstantCode.SIGN_FAIL));
    }
}
