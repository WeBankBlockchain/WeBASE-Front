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

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.Hash;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tx.txdecode.BaseException;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoderFactory;
import org.fisco.bcos.web3j.utils.ByteUtil;
import org.fisco.bcos.web3j.utils.Numeric;
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
    
    @ApiOperation(value = "decode input/output", notes = "decode tx receipt's input/output")
    @ApiImplicitParam(name = "param", value = "param to be transfer", required = true, dataType = "ReqDecodeParam")
    @PostMapping("/decode")
    public Object decode(@Valid @RequestBody ReqDecodeParam param)
        throws BaseException, TransactionException, JsonProcessingException {
        log.info("decode output start. param:{}", JsonUtils.toJSONString(param));
        TransactionDecoder decoder = TransactionDecoderFactory.buildTransactionDecoder(JsonUtils.objToString(param.getAbiList()), "");
        // decode input
        if (param.getDecodeType() == 1) {
            // return json
            if (param.getReturnType() == 2) {
                return decoder.decodeInputReturnJson(param.getInput());
            } else if (param.getReturnType() == 1) {
                return decoder.decodeInputReturnObject(param.getInput());
            }
        } else if (param.getDecodeType() == 2) {
            // return json
            if (param.getReturnType() == 2) {
                return decoder.decodeOutputReturnJson(param.getInput(), param.getOutput());
            } else if (param.getReturnType() == 1) {
                return decoder.decodeOutputReturnObject(param.getInput(), param.getOutput());
            }
        }
        return new BaseResponse(ConstantCode.PARAM_ERROR);
    }

    @ApiOperation(value = "get key pair", notes = "generate key pair of front's encrypt type")
    @ApiImplicitParam(name = "param", value = "private key(hex string), representation of BigInteger", dataType = "ReqPrivateKey")
    @PostMapping("/keypair")
    public RspKeyPair getKeyPair(@Valid @RequestBody ReqPrivateKey param) {
        String privateKey = param.getPrivateKey();
        ECKeyPair ecKeyPair;
        if (StringUtils.isNotBlank(privateKey)) {
            ecKeyPair = GenCredential.createKeyPair(privateKey);
        } else {
            ecKeyPair = GenCredential.createKeyPair();
        }
        return new RspKeyPair(ecKeyPair, EncryptType.encryptType);
    }

    @ApiOperation(value = "get public key's address", notes = "get address from pub")
    @ApiImplicitParam(name = "publicKey", value = "pub key(hex string format), representation of BigInteger", dataType = "String")
    @GetMapping("/address")
    public RspKeyPair getKey(@RequestParam String publicKey) {
        RspKeyPair response = new RspKeyPair();
        response.setEncryptType(EncryptType.encryptType);
        response.setPublicKey(publicKey);
        response.setAddress(Keys.getAddress(publicKey));
        return response;
    }

    @ApiOperation(value = "get hash value", notes = "get hash value")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "input", value = "input to hash(utf-8 or hexString)", dataType = "String"),
        @ApiImplicitParam(name = "type", value = " input type 1-hexString,2-utf8", dataType = "Integer")
    })
    @GetMapping("/hash")
    public RspHash getHashValue(@RequestParam String input, @RequestParam(defaultValue = "1") Integer type) {
        String hashValue;
        if (type == 2) {
            // utf8 input
            hashValue = Hash.sha3String(input);
        } else if (type == 1){
            // hex input
            hashValue = Hash.sha3(input);
        } else {
            throw new FrontException(ConstantCode.PARAM_ERROR);
        }
        return new RspHash(hashValue, EncryptType.encryptType);
    }


    @ApiOperation(value = "get private key of pem", notes = "get private key of pem")
    @ApiImplicitParam(name = "pemFile", value = ".pem file of private key", dataType = "MultipartFile")
    @PostMapping("/decodePem")
    public RspKeyPair decodePem(@RequestParam MultipartFile pemFile) {
        if (pemFile.getSize() == 0) {
            throw new FrontException(ConstantCode.PEM_FORMAT_ERROR);
        }
        PEMManager pemManager = new PEMManager();
        String privateKey;
        try {
            pemManager.load(new ByteArrayInputStream(pemFile.getBytes()));
            privateKey = Numeric.toHexStringNoPrefix(pemManager.getECKeyPair().getPrivateKey());
        } catch (NoSuchAlgorithmException| CertificateException| IOException e) {
            log.error("decodePem error:{}", e);
            throw new FrontException(ConstantCode.PEM_CONTENT_ERROR);
        } catch (UnrecoverableKeyException | InvalidKeySpecException e) {
            log.error("decodePem get kepair error:{}", e);
            throw new FrontException(ConstantCode.WEB3J_PEM_P12_MANAGER_GET_KEY_PAIR_ERROR.getCode(), e.getMessage());
        } catch (KeyStoreException | NoSuchProviderException e) {
            log.error("decodePem init p12 for dependency error:{}", e);
            throw new FrontException(ConstantCode.WEB3J_PEM_P12_MANAGER_DEPENDENCY_ERROR.getCode(), e.getMessage());
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
        P12Manager p12Manager = new P12Manager();
        String privateKey;
        try {
            // manually set password and load
            p12Manager.setPassword(p12Password);
            p12Manager.load(p12File.getInputStream(), p12Password);
            privateKey = Numeric.toHexStringNoPrefix(p12Manager.getECKeyPair().getPrivateKey());
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            log.error("decodeP12 error:{}", e);
            if (e.getMessage().contains("password")) {
                throw new FrontException(ConstantCode.P12_PASSWORD_ERROR);
            }
            throw new FrontException(ConstantCode.P12_FILE_ERROR);
        } catch (UnrecoverableKeyException | InvalidKeySpecException e) {
            log.error("decodeP12 get kepair error:{}", e);
            throw new FrontException(ConstantCode.WEB3J_PEM_P12_MANAGER_GET_KEY_PAIR_ERROR.getCode(), e.getMessage());
        } catch (KeyStoreException | NoSuchProviderException e) {
            log.error("decodeP12 init p12 for dependency error:{}", e);
            throw new FrontException(ConstantCode.WEB3J_PEM_P12_MANAGER_DEPENDENCY_ERROR.getCode(), e.getMessage());
        }
        return getRspKeyPair(privateKey);
    }
    
    private RspKeyPair getRspKeyPair(String privateKey) {
        ECKeyPair ecKeyPair = GenCredential.createKeyPair(privateKey);
        return new RspKeyPair(ecKeyPair, EncryptType.encryptType);
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
            // hex input
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
        Credentials credentials = GenCredential.create(privateKey);
        Sign.SignatureData signatureData = Sign.getSignInterface().signMessage(
            ByteUtil.hexStringToBytes(Numeric.toHexString(rawData.getBytes())), credentials.getEcKeyPair());
        return new RspSignData(signatureData, EncryptType.encryptType);
    }


}
