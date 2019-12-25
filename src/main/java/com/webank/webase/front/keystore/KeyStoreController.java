/*
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.front.keystore;

import java.util.List;

import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.entity.KeyStoreInfo;
import com.webank.webase.front.keystore.entity.ReqImportPem;
import com.webank.webase.front.util.PemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.KeyTypes;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/privateKey")
public class KeyStoreController extends BaseController {

    @Autowired
    private KeyStoreService keyStoreService;

    @ApiOperation(value = "getKeyStore", notes = "get key store info")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "useAes", value = "Is encrypting the private key", dataType = "Boolean"),
        @ApiImplicitParam(name = "type", value = "private key type", dataType = "int"),
        @ApiImplicitParam(name = "userName", value = "user name", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.GET)
    public KeyStoreInfo getKeyStore(@RequestParam(required = true) boolean useAes, 
            @RequestParam(required = false, defaultValue = "2") int type,
            @RequestParam(required = false) String userName) {
        return keyStoreService.createKeyStore(useAes, type, userName);
    }

    @ApiOperation(value = "import PrivateKey", notes = "import PrivateKey")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "privateKey", value = "private key", required = true, dataType = "String"),
        @ApiImplicitParam(name = "userName", value = "user name", required = true, dataType = "String")
    })
    @RequestMapping(method = RequestMethod.GET, value = "/import")
    public KeyStoreInfo importPrivateKey(@RequestParam(required = true) String privateKey,
        @RequestParam(required = true) String userName) {
        return keyStoreService.getKeyStoreFromPrivateKey(privateKey, false, KeyTypes.LOCALUSER.getValue(), userName);
    }

    @ApiOperation(value = "import PrivateKey by pem", notes = "import PrivateKey by pem")
    @ApiImplicitParam(name = "reqImportPem", value = "import pem info", required = true, dataType = "ReqImportPem")
    @PostMapping("/importPem")
    public BaseResponse importPemPrivateKey(@Valid @RequestBody ReqImportPem reqImportPem) {
        String pemContent = reqImportPem.getPemContent();
        String userName = reqImportPem.getUserName();
        if(!pemContent.startsWith(PemUtils.crtContentHead)) {
            throw new FrontException(ConstantCode.PEM_FORMAT_ERROR);
        }
        keyStoreService.importKeyStoreFromPem(pemContent, false, KeyTypes.LOCALUSER.getValue(), userName);
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }
    
    @ApiOperation(value = "getKeyStores", notes = "get local KeyStore lists")
    @RequestMapping(method = RequestMethod.GET, value = "/localKeyStores")
    public List<KeyStoreInfo> getLocalKeyStores() {
        log.info("start getLocalKeyStores.");
        return keyStoreService.getLocalKeyStores();
    }
    
    @ApiOperation(value = "delete", notes = "delete local KeyStore by address")
    @ApiImplicitParam(name = "address", value = "user address", required = true, dataType = "String")
    @DeleteMapping("/{address}")
    public BaseResponse deleteKeyStore(@PathVariable String address) {
        log.info("start deleteKeyStores. address:{}", address);
        keyStoreService.deleteKeyStore(address);
        return new BaseResponse(ConstantCode.RET_SUCCEED);
    }
}
