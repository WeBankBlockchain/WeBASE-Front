package com.webank.webase.front.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.transaction.ReqTransHandle;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ContractAbiUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * Copyright 2012-2019 the original author or authors.
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

/**
 * contract management.
 * 
 */
@Slf4j
@Service
public class ContractService {
    @Autowired
    Web3j web3j;
    @Autowired
    TransService transService;
    @Autowired
    Constants constants;

    /**
     * sendAbi.
     * 
     * @param req request data
     * @return
     */
    public BaseResponse sendAbi(ReqSendAbi req) throws FrontException {
        BaseResponse baseRsp = new BaseResponse(ConstantCode.RET_SUCCEED);

        String contractName = req.getContractName();
        String version = req.getVersion();
        List<Object> abiInfos = req.getAbiInfo();

        // Check if it has been deployed based on the contract name and version number
        boolean ifExisted = ContractAbiUtil.ifContractAbiExisted(contractName, version);
        if (!ifExisted) {
            // save abi
            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(abiInfos));
            ContractAbiUtil.setContractWithAbi(contractName, version, jsonArray, true);
        }

        log.info("sendAbi end. contractname:{} ,version:{}", contractName, version);
        return baseRsp;
    }

    /**
     * contract deploy.
     * 
     * @param req request data
     * @return
     */
    public BaseResponse deploy(ReqDeploy req) throws FrontException {

        int userId = req.getUserId();
        String contractName = req.getContractName();
        String version = req.getVersion();
        List<Object> abiInfos = req.getAbiInfo();
        String bytecodeBin = req.getBytecodeBin();
        List<Object> params = req.getFuncParam();

        // Check if contractAbi existed
        boolean ifExisted = ContractAbiUtil.ifContractAbiExisted(contractName, version);
        if (!ifExisted) {
            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(abiInfos));
            ContractAbiUtil.setContractWithAbi(contractName, version, jsonArray, true);
        }

        // Constructor encoded
        String encodedConstructor = "";
        // input handle
        List<String> funcInputTypes =
                ContractAbiUtil.getFuncInputType(contractName, contractName, version);
        if (funcInputTypes != null && funcInputTypes.size() > 0) {
            if (funcInputTypes.size() == params.size()) {
                List<Type> finalInputs = TransService.inputFormat(funcInputTypes, params);
                encodedConstructor = FunctionEncoder.encodeConstructor(finalInputs);
                log.info("deploy encodedConstructor:{}", encodedConstructor);
            } else {
                log.warn("deploy fail. funcInputTypes:{}, params:{}", funcInputTypes, params);
                throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
            }
        }

        // get privateKey
        String privateKey = Optional.ofNullable(transService.getPrivateKey(userId))
                .map(info -> info.getPrivateKey()).orElse(null);
        if (privateKey == null) {
            log.error("userId:{} privateKey is null.", userId);
            throw new FrontException(ConstantCode.PRIVATEKEY_IS_NULL);
        }
        Credentials credentials = Credentials.create(privateKey);

        // contract deploy
        String contractAddress = deployContract(bytecodeBin, encodedConstructor, credentials);


        // cns Params
        List<Object> cnsParams = new ArrayList<>();
        cnsParams.add(contractName + Constants.DIAGONAL + version);
        cnsParams.add(contractName);
        cnsParams.add(version);
        cnsParams.add(JSON.toJSONString(abiInfos));
        cnsParams.add(contractAddress);

        // trans Params
        ReqTransHandle reqTransHandle = new ReqTransHandle();
        reqTransHandle.setUserId(userId);
        reqTransHandle.setContractName(Constants.CNS_CONTRAC_TNAME);
        reqTransHandle.setVersion("");
        reqTransHandle.setFuncName(Constants.CNS_FUNCTION_ADDABI);
        reqTransHandle.setFuncParam(cnsParams);

        // cns add
        BaseResponse baseRsp = transService.transRequest(reqTransHandle);

        // result
        if (baseRsp.getCode() != 0) {
            return baseRsp;
        }
        baseRsp.setData(contractAddress);
        log.info("contract deploy end. baseRsp:{}", JSON.toJSONString(baseRsp));
        return baseRsp;
    }

    private String deployContract(String bytecodeBin, String encodedConstructor, Credentials credentials) throws FrontException {
        CommonContract commonContract = null;
        try {
            commonContract =
                    CommonContract.deploy(web3j, credentials, Constants.GAS_PRICE, Constants.GAS_LIMIT,
                                    Constants.INITIAL_WEI_VALUE, bytecodeBin, encodedConstructor)
                            .send();
        } catch (Exception e) {
            log.error("commonContract deploy failed.");
            throw new FrontException(ConstantCode.CONTRACT_DEPLOY_ERROR);
        }
        log.info("commonContract deploy success. contractAddress:{}", commonContract.getContractAddress());
        return commonContract.getContractAddress();

    }

    /**
     * deleteAbi.
     * 
     * @param contractName not null
     * @param version not null
     * @return
     */
    public BaseResponse deleteAbi(String contractName, String version) throws FrontException {
        BaseResponse baseRsp = new BaseResponse(ConstantCode.RET_SUCCEED);
        boolean result = CommonUtils.deleteFile(
                Constants.ABI_DIR + Constants.DIAGONAL + contractName + Constants.SEP + version);
        if (!result) {
            log.warn("deleteAbi fail. contractname:{} ,version:{}", contractName, version);
            throw new FrontException(ConstantCode.FILE_IS_NOT_EXIST);
        }
        return baseRsp;
    }
}
