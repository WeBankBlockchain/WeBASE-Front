/*
 * Copyright 2014-2020 the original author or authors.
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

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.enums.KeyTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.contract.entity.FileContentHandle;
import com.webank.webase.front.keystore.entity.KeyStoreInfo;
import com.webank.webase.front.keystore.entity.MessageHashInfo;
import com.webank.webase.front.keystore.entity.ReqExport;
import com.webank.webase.front.keystore.entity.ReqImportPem;
import com.webank.webase.front.keystore.entity.ReqImportWithSign;
import com.webank.webase.front.keystore.entity.RspKeyFile;
import com.webank.webase.front.keystore.entity.RspMessageHashSignature;
import com.webank.webase.front.keystore.entity.RspUserInfo;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.FrontUtils;
import com.webank.webase.front.util.PemUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(value = "/privateKey")
public class KeyStoreController extends BaseController {

    @Autowired
    private KeyStoreService keyStoreService;

    @ApiOperation(value = "getKeyStore", notes = "get key store info")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "type", value = "private key type", dataType = "int"),
        @ApiImplicitParam(name = "appId", value = "app that user belong to", dataType = "String"),
        @ApiImplicitParam(name = "signUserId", value = "user id of webase-sign", dataType = "String"),
        @ApiImplicitParam(name = "userName", value = "user name", dataType = "String"),
    })
    @GetMapping
    public KeyStoreInfo getKeyStore(@RequestParam(required = false, defaultValue = "2") int type,
                                    @RequestParam(required = false) String appId,
                                    @RequestParam(required = false) String signUserId,
                                    @RequestParam(required = false) String userName,
                                    @RequestParam(required = false, defaultValue = "false") boolean returnPrivateKey) {
        // external key store (2)
        if (KeyTypes.EXTERNALUSER.getValue() == type) {
            if (StringUtils.isBlank(signUserId) || StringUtils.isBlank(appId)) {
                throw new FrontException(ConstantCode.PARAM_FAIL_APPID_SIGN_USER_ID_EMPTY);
            }
            // create from webase-sign , return keystoreInfo without private key
            KeyStoreInfo keyStoreInfo = keyStoreService.createKeyStoreWithSign(signUserId, appId, returnPrivateKey);
            return keyStoreInfo;
        } else if (KeyTypes.LOCALUSER.getValue() == type) {
            // local key store (0)
            if (StringUtils.isBlank(userName)) {
                log.error("fail createKeyStore. user name is null.");
                throw new FrontException(ConstantCode.USER_NAME_NULL);
            }
            // create locally, return keystoreInfo with private key encrypted
            return keyStoreService.createKeyStoreLocally(userName);
        } else {
            log.error("fail createKeyStore. key store type invalid");
            throw new FrontException(ConstantCode.PARAM_VAILD_FAIL);
        }
    }
    
    @ApiOperation(value = "getUserInfoWithSign", notes = "get key store info from sign")
    @GetMapping("userInfoWithSign")
    public RspUserInfo getUserInfoWithSign(@RequestParam String signUserId,
            @RequestParam(required = false, defaultValue = "false") boolean returnPrivateKey) {
        RspUserInfo rspUserInfo = keyStoreService.getUserInfoWithSign(signUserId, returnPrivateKey);
        return rspUserInfo;
    }

    @ApiOperation(value = "getKeyStoreList", notes = "get local KeyStore lists")
    @GetMapping("localKeyStores")
    public List<KeyStoreInfo> getLocalKeyStoreList() {
        log.info("start getLocalKeyStoreList.");
        return keyStoreService.getLocalKeyStoreList();
    }

    @ApiOperation(value = "delete", notes = "delete local KeyStore by address")
    @ApiImplicitParam(name = "address", value = "user address", required = true, dataType = "String")
    @DeleteMapping("/{address}")
    public BaseResponse deleteKeyStore(@PathVariable String address) {
        log.info("start deleteKeyStores. address:{}", address);
        keyStoreService.deleteKeyStore(address);
        return new BaseResponse(ConstantCode.RET_SUCCEED);
    }

    @ApiOperation(value = "import PrivateKey", notes = "import PrivateKey")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "privateKey", value = "private key", required = true, dataType = "String"),
        @ApiImplicitParam(name = "userName", value = "user name", required = true, dataType = "String")
    })
    @GetMapping("/import")
    public KeyStoreInfo importPrivateKey(@RequestParam String privateKey, @RequestParam String userName) {
        return keyStoreService.importFromPrivateKey(privateKey, userName);
    }

    @ApiOperation(value = "import PrivateKey by pem", notes = "import PrivateKey by pem")
    @ApiImplicitParam(name = "reqImportPem", value = "import pem info",
            required = true, dataType = "ReqImportPem")
    @PostMapping("/importPem")
    public BaseResponse importPemPrivateKey(@Valid @RequestBody ReqImportPem reqImportPem) {
        String pemContent = reqImportPem.getPemContent();
        String userName = reqImportPem.getUserName();
        if(!pemContent.startsWith(PemUtils.crtContentHeadNoLF)) {
            throw new FrontException(ConstantCode.PEM_FORMAT_ERROR);
        }
        keyStoreService.importKeyStoreFromPem(pemContent, userName);
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }

    @ApiOperation(value = "import PrivateKey by p12", notes = "import PrivateKey by p12")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "user name", dataType = "String"),
            @ApiImplicitParam(name = "p12File", value = ".p12 file of private key", dataType = "MultipartFile"),
            @ApiImplicitParam(name = "p12Password", value = ".p12 file password", dataType = "String")
    })
    @PostMapping("/importP12")
    public BaseResponse importP12PrivateKey(@RequestParam String userName,
                                            @RequestParam MultipartFile p12File,
                                            @RequestParam(required = false, defaultValue = "") String p12Password) {
        if (!CommonUtils.notContainsChinese(p12Password)) {
            throw new FrontException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        if (p12File.getSize() == 0) {
            throw new FrontException(ConstantCode.P12_FILE_ERROR);
        }
        keyStoreService.importKeyStoreFromP12(p12File, p12Password, userName);
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }

    @ApiOperation(value = "import PrivateKey encoded by base64", notes = "import PrivateKey")
    @ApiImplicitParam(name = "reqImportWithSign", value = "import private key to sign",
            required = true, dataType = "ReqImportWithSign")
    @PostMapping("/importWithSign")
    public KeyStoreInfo importPrivateKeyWithSign(@Valid @RequestBody ReqImportWithSign reqImportWithSign) {
        return keyStoreService.importPrivateKeyToSign(reqImportWithSign.getPrivateKey(),
                reqImportWithSign.getSignUserId(), reqImportWithSign.getAppId());
    }


    /**
     * sign MessageHash by ecdsa or guomi encryption
     *
     * @param req parameter
     */
    @ApiOperation(value = "sign MessageHash by ecdsa(default) or guomi",
            notes = "获取ECDSA或国密SM2 hash的签名数据，默认ECDSA")
    @ApiImplicitParam(name = "req", value = "Message", required = true,
            dataType = "MessageHashInfo")
    @PostMapping("/signMessageHash")
    public BaseResponse signMessageHash(@Valid @RequestBody MessageHashInfo req) {
        RspMessageHashSignature rspSignData = keyStoreService.getMessageHashSignData(req);
        return new BaseResponse(ConstantCode.RET_SUCCESS, rspSignData);

    }

    @ApiOperation(value = "export PrivateKey by pem",
        notes = "export PrivateKey by pem, address or signUserId cannot both empty")
    @ApiImplicitParam(name = "param", value = "user address and userId of user",
            required = true, dataType = "ReqExport")
    @PostMapping("/exportPem")
    public ResponseEntity<InputStreamResource> exportPemPrivateKey(@RequestBody ReqExport param) {
        Instant startTime = Instant.now();
        log.info("start exportPemPrivateKey startTime:{},param:{}", startTime.toEpochMilli(), param);
        String userAddress = param.getUserAddress();
        String signUserId = param.getSignUserId();
        if (StringUtils.isAllBlank(userAddress, signUserId)) {
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
        // get file
        FileContentHandle fileContentHandle;
        if (StringUtils.isNotBlank(signUserId)) {
            fileContentHandle = keyStoreService.exportPemWithSign(signUserId);
        } else {
            fileContentHandle = keyStoreService.exportPemLocal(userAddress);
        }
        log.info("end exportPemPrivateKey fileContentHandle:{}useTime:{}", fileContentHandle,
            Duration.between(startTime, Instant.now()).toMillis());
        return ResponseEntity.ok().headers(FrontUtils.headers(fileContentHandle.getFileName()))
            .body(new InputStreamResource(fileContentHandle.getInputStream()));
    }

    @ApiOperation(value = "export PrivateKey by p12", notes = "export PrivateKey by p12")
    @ApiImplicitParam(name = "param", value = "user address and userId and password of p12",
        required = true, dataType = "ReqExport")
    @PostMapping("/exportP12")
    public ResponseEntity<InputStreamResource> exportP12PrivateKey(@RequestBody ReqExport param) {
        Instant startTime = Instant.now();
        log.info("start exportP12PrivateKey startTime:{},param:{}", startTime.toEpochMilli(),param);
        String userAddress = param.getUserAddress();
        String signUserId = param.getSignUserId();
        String p12Password = param.getP12Password();
        if (StringUtils.isAllBlank(userAddress, signUserId)) {
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
        if (!CommonUtils.notContainsChinese(p12Password)) {
            throw new FrontException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        // get file
        FileContentHandle fileContentHandle;
        if (StringUtils.isNotBlank(signUserId)) {
            fileContentHandle = keyStoreService.exportP12WithSign(signUserId, p12Password);
        } else {
            fileContentHandle = keyStoreService.exportP12Local(userAddress, p12Password);
        }
        log.info("end exportP12PrivateKey fileContentHandle:{}useTime:{}", fileContentHandle,
            Duration.between(startTime, Instant.now()).toMillis());
        return ResponseEntity.ok().headers(FrontUtils.headers(fileContentHandle.getFileName()))
            .body(new InputStreamResource(fileContentHandle.getInputStream()));
    }


}
