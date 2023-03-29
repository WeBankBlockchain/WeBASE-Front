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

package com.webank.webase.front.tool;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.tool.entity.ReqDecodeParam;
import com.webank.webase.front.tool.entity.ReqPrivateKey;
import com.webank.webase.front.tool.entity.ReqSign;
import com.webank.webase.front.tool.entity.RspHash;
import com.webank.webase.front.tool.entity.RspKeyPair;
import com.webank.webase.front.tool.entity.RspSignData;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.keypair.ECDSAKeyPair;
import org.fisco.bcos.sdk.crypto.keypair.SM2KeyPair;
import org.fisco.bcos.sdk.crypto.keystore.KeyTool;
import org.fisco.bcos.sdk.crypto.keystore.P12KeyStore;
import org.fisco.bcos.sdk.crypto.keystore.PEMKeyStore;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@Api(value = "/tool", tags = "tools controller")
@Slf4j
@RestController
@RequestMapping("tool")
public class ToolController {

    @Autowired
    @Qualifier(value = "common")
    private CryptoSuite cryptoSuite;

    @ApiOperation(value = "decode input/output", notes = "decode tx receipt's input/output")
    @ApiImplicitParam(name = "param", value = "param to be transfer", required = true, dataType = "ReqDecodeParam")
    @PostMapping("/decode")
    public Object decode(@Valid @RequestBody ReqDecodeParam param) {
        log.info("decode output start. param:{}", JsonUtils.toJSONString(param));
        // todo 自测返回值
        TransactionDecoderService txDecoder = new TransactionDecoderService(cryptoSuite);
        // decode input
        if (param.getDecodeType() == 1) {
            return txDecoder.decodeReceiptMessage(param.getInput());
        } else if (param.getDecodeType() == 2) {
            String abi = JsonUtils.objToString(param.getAbiList());
            ABICodec abiCodec = new ABICodec(cryptoSuite, true);
            // decode output
            try {
                return abiCodec.decodeMethodAndGetOutputObject(abi, param.getMethodName(),
                    param.getOutput());
            } catch (ABICodecException e) {
                log.error("abi decode fail:{}", e.getMessage());
                throw new FrontException(ConstantCode.CONTRACT_ABI_PARSE_JSON_ERROR);
            }
        }
        return new BaseResponse(ConstantCode.PARAM_ERROR);
    }

    @ApiOperation(value = "get key pair", notes = "generate key pair of front's encrypt type")
    @ApiImplicitParam(name = "param", value = "private key(hex string), representation of BigInteger", dataType = "ReqPrivateKey")
    @PostMapping("/keypair")
    public RspKeyPair getKeyPair(@Valid @RequestBody ReqPrivateKey param) {
        String privateKey = param.getPrivateKey();
        CryptoKeyPair keyPair;
        if (StringUtils.isNotBlank(privateKey)) {
            keyPair = cryptoSuite.getKeyPairFactory().createKeyPair(privateKey);
        } else {
            keyPair = cryptoSuite.getKeyPairFactory().generateKeyPair();
        }
        return new RspKeyPair(keyPair, cryptoSuite.cryptoTypeConfig);
    }

    @ApiOperation(value = "get public key's address", notes = "get address from pub")
    @ApiImplicitParam(name = "publicKey", value = "pub key(hex string format), representation of BigInteger", dataType = "String")
    @GetMapping("/address")
    public RspKeyPair getKey(@RequestParam String publicKey) {
        RspKeyPair response = new RspKeyPair();
        if (cryptoSuite.cryptoTypeConfig == CryptoType.SM_TYPE) {
            response.setAddress(SM2KeyPair.getAddressByPublicKey(publicKey));
        } else {
            response.setAddress(ECDSAKeyPair.getAddressByPublicKey(publicKey));
        }
        response.setEncryptType(cryptoSuite.cryptoTypeConfig);
        response.setPublicKey(publicKey);
        return response;
    }

    @ApiOperation(value = "get hash value", notes = "get hash value")
    @ApiImplicitParam(name = "input", value = "input to hash(hexString)", dataType = "String")
    @GetMapping("/hash")
    public RspHash getHashValue(@RequestParam String input) {
        String hashValue = cryptoSuite.hash(input);
        return new RspHash(hashValue, cryptoSuite.cryptoTypeConfig);
    }


    @ApiOperation(value = "get private key of pem", notes = "get private key of pem")
    @ApiImplicitParam(name = "pemFile", value = ".pem file of private key", dataType = "MultipartFile")
    @PostMapping("/decodePem")
    public RspKeyPair decodePem(@RequestParam MultipartFile pemFile) {
        if (pemFile.getSize() == 0) {
            throw new FrontException(ConstantCode.PEM_FORMAT_ERROR);
        }
        String privateKey;
        try {
            PEMKeyStore pemManager = new PEMKeyStore(new ByteArrayInputStream(pemFile.getBytes()));
            privateKey = KeyTool.getHexedPrivateKey(pemManager.getKeyPair().getPrivate());
        } catch (IOException e) {
            log.error("decodePem error:[]", e);
            throw new FrontException(ConstantCode.PEM_CONTENT_ERROR);
        }
        
        return getRspKeyPair(privateKey);
    }

    @ApiOperation(value = "get PrivateKey of p12", notes = "get PrivateKey by p12")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "p12File", value = ".p12 file of private key", dataType = "MultipartFile"),
        @ApiImplicitParam(name = "p12Password", value = ".p12 file password", dataType = "String")
    })
    @PostMapping("/decodeP12")
    public RspKeyPair decodeP12(@RequestParam MultipartFile p12File,
        @RequestParam(required = false, defaultValue = "") String p12Password) {
        if (!CommonUtils.notContainsChinese(p12Password)) {
            throw new FrontException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        if (p12File.getSize() == 0) {
            throw new FrontException(ConstantCode.P12_FILE_ERROR);
        }
        String privateKey;
        try {
            P12KeyStore p12Manager = new P12KeyStore(new ByteArrayInputStream(p12File.getBytes()), p12Password);
            privateKey = KeyTool.getHexedPrivateKey(p12Manager.getKeyPair().getPrivate());
        } catch (IOException e) {
            log.error("decodeP12 error:[]", e);
            if (e.getMessage().contains("password")) {
                throw new FrontException(ConstantCode.P12_PASSWORD_ERROR);
            }
            throw new FrontException(ConstantCode.P12_FILE_ERROR);
        }
        return getRspKeyPair(privateKey);
    }
    
    private RspKeyPair getRspKeyPair(String privateKey) {
        CryptoKeyPair keyPair = cryptoSuite.getKeyPairFactory().createKeyPair(privateKey);
        return new RspKeyPair(keyPair, cryptoSuite.cryptoTypeConfig);
    }

    @ApiOperation(value = "get hash value", notes = "get hash value")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "input", value = "input to hash(hexString or utf-8)", dataType = "String"),
        @ApiImplicitParam(name = "type", value = " input type input type 1-hexString,2-utf8(default hex)", dataType = "Integer")
    })
    @GetMapping("/convert2Bytes32")
    public String getBytes32FromStr(@RequestParam String input, @RequestParam(defaultValue = "1") Integer type) {
        Bytes32 bytes32;
        if (type == 1) {
            // hex input todo use java sdk
            bytes32 = CommonUtils.hexStrToBytes32(input);
        } else if (type == 2) {
            // utf8 input
            bytes32 = CommonUtils.utf8StringToBytes32(input);
        } else {
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
        return Numeric.toHexString(bytes32.getValue());
    }

    @ApiOperation(value = "get utf8's hex string", notes = "get hex string")
    @ApiImplicitParam(name = "input", value = "input to convert(utf8 String)", dataType = "String")
    @GetMapping("/utf8ToHexString")
    public String getHexStringFromUtf8(@RequestParam String input) {
        return CommonUtils.utf8StringToHex(input);
    }

    @ApiOperation(value = "sign raw data", notes = "sign raw data by private key")
    @ApiImplicitParam(name = "reqSign", value = "raw data & private key", dataType = "ReqSign")
    @PostMapping("/signMsg")
    public RspSignData getSignedData(@Valid @RequestBody ReqSign reqSign) {
        String privateKey = reqSign.getPrivateKey();
        String rawData = reqSign.getRawData();
        CryptoKeyPair cryptoKeyPair = cryptoSuite.getKeyPairFactory().createKeyPair(privateKey);
        SignatureResult signatureData = cryptoSuite.sign(Numeric.toHexString(rawData.getBytes()), cryptoKeyPair);
        return new RspSignData(signatureData, cryptoSuite.cryptoTypeConfig);
    }


}
