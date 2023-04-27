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

package com.webank.webase.front.abi;

import com.webank.webase.front.abi.entity.AbiInfo;
import com.webank.webase.front.abi.entity.ReqImportAbi;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.contract.entity.RspContractNoAbi;
import com.webank.webase.front.util.FrontUtils;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author marsli
 */
@Slf4j
@Service
public class AbiService {

    @Autowired
    AbiRepository abiRepository;
    @Autowired
    Web3ApiService web3ApiService;


    public List<AbiInfo> getListByGroupId(Integer groupId, Pageable pageable) {
        List<AbiInfo> abiList = abiRepository.findByGroupId(groupId, pageable);
        return abiList;
    }

    /**
     * get all
     */
    public List<RspContractNoAbi> getListByGroupIdNoAbi(Integer groupId) {
        List<AbiInfo> abiList = abiRepository.findByGroupId(groupId);
        List<RspContractNoAbi> resultList = new ArrayList<>();
        abiList.forEach(c -> {
            RspContractNoAbi rsp = new RspContractNoAbi();
            BeanUtils.copyProperties(c, rsp);
            resultList.add(rsp);
        });
        return resultList;
    }

    @Transactional
    public void saveAbi(ReqImportAbi param) {
        if (Objects.isNull(param.getAbiId())) {
            insertAbiInfo(param);
        } else {
            updateAbiInfo(param);
        }
    }

    @Transactional
    public void insertAbiInfo(ReqImportAbi param) {
        int groupId = param.getGroupId();
        String contractName = param.getContractName();
        String contractAddress = param.getContractAddress();
        String contractAbiStr;
        try {
            contractAbiStr = JsonUtils.toJSONString(param.getContractAbi());
        } catch (Exception e) {
            log.warn("abi parse string error:{}", param.getContractAbi());
            throw new FrontException(ConstantCode.PARAM_FAIL_ABI_INVALID);
        }
        // check address
        String contractBin = getAddressRuntimeBin(groupId, contractAddress);
        // check name and address of abi not exist
        checkAbiNotExist(groupId, contractName, contractAddress);

        AbiInfo saveAbi = new AbiInfo();
        BeanUtils.copyProperties(param, saveAbi);
        saveAbi.setContractAbi(contractAbiStr);
        saveAbi.setContractBin(contractBin);
        saveAbi.setCreateTime(LocalDateTime.now());
        saveAbi.setModifyTime(LocalDateTime.now());
        abiRepository.save(saveAbi);
    }

    @Transactional
    public void updateAbiInfo(ReqImportAbi param) {
        Long abiId = param.getAbiId();
        // check id exists
        checkAbiIdExist(abiId);
        // update
        AbiInfo updateAbi = new AbiInfo();
        BeanUtils.copyProperties(param, updateAbi);
        String contractAbiStr;
        try {
            contractAbiStr = JsonUtils.toJSONString(param.getContractAbi());
        } catch (Exception e) {
            log.warn("abi parse string error:{}", param.getContractAbi());
            throw new FrontException(ConstantCode.PARAM_FAIL_ABI_INVALID);
        }
        // check address
        String contractBin = getAddressRuntimeBin(param.getGroupId(), param.getContractAddress());
        updateAbi.setContractAbi(contractAbiStr);
        updateAbi.setContractBin(contractBin);
        updateAbi.setModifyTime(LocalDateTime.now());
        abiRepository.save(updateAbi);
    }

    public void delete(Long id) {
        checkAbiIdExist(id);
        abiRepository.deleteById(id);
    }

    public AbiInfo getAbiById(Long abiId) {
        AbiInfo abiInfo = abiRepository.findByAbiId(abiId);
        if (Objects.isNull(abiInfo)) {
            throw new FrontException(ConstantCode.ABI_INFO_NOT_EXISTS);
        }
        return abiInfo;
    }

    public AbiInfo getAbiByGroupIdAndAddress(int groupId, String contractAddress) {
        AbiInfo abiInfo = abiRepository.findByGroupIdAndContractAddress(groupId, contractAddress);
        if (Objects.isNull(abiInfo)) {
            throw new FrontException(ConstantCode.ABI_INFO_NOT_EXISTS);
        }
        return abiInfo;
    }

    private void checkAbiIdExist(Long abiId) {
        AbiInfo checkAbiId = getAbiById(abiId);
        if (Objects.isNull(checkAbiId)) {
            throw new FrontException(ConstantCode.ABI_INFO_NOT_EXISTS);
        }
    }

    /**
     * check (groupId,contractName), (groupId,address) unique
     * @param groupId
     * @param contractName
     * @param address
     */
    private void checkAbiNotExist(int groupId, String contractName, String address) {
        AbiInfo checkAbiName = abiRepository.findByGroupIdAndContractName(groupId, contractName);
        if (Objects.nonNull(checkAbiName)) {
            throw new FrontException(ConstantCode.CONTRACT_NAME_REPEAT);
        }
        AbiInfo checkAbiAddressExist = abiRepository.findByGroupIdAndContractAddress(groupId, address);
        if (Objects.nonNull(checkAbiAddressExist)) {
            throw new FrontException(ConstantCode.CONTRACT_ADDRESS_ALREADY_EXISTS);
        }
    }

    /**
     * check address is valid.
     * @return address's runtime bin
     */
    private String getAddressRuntimeBin(int groupId, String contractAddress) {
        if (StringUtils.isBlank(contractAddress)) {
            log.error("fail getAddressRuntimeBin. contractAddress is empty");
            throw new FrontException(ConstantCode.CONTRACT_ADDRESS_NULL);
        }
        String binOnChain;
        try {
            binOnChain = web3ApiService.getCode(groupId, contractAddress, BigInteger.ZERO);
        } catch (Exception e) {
            log.error("fail getAddressRuntimeBin.", e);
            throw new FrontException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
        log.info("getAddressRuntimeBin address:{} binOnChain:{}", contractAddress, binOnChain);
        String runtimeBin = FrontUtils.removeFirstStr(binOnChain, "0x");
        if (StringUtils.isBlank(runtimeBin)) {
            log.error("fail getAddressRuntimeBin. runtimeBin is null, address:{}", contractAddress);
            throw new FrontException(ConstantCode.CONTRACT_NOT_DEPLOY_ERROR);
        }
        return runtimeBin;
    }

}
