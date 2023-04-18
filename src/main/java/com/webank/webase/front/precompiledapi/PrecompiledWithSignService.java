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

package com.webank.webase.front.precompiledapi;

import static org.fisco.bcos.sdk.contract.precompiled.consensus.ConsensusPrecompiled.FUNC_ADDOBSERVER;
import static org.fisco.bcos.sdk.contract.precompiled.consensus.ConsensusPrecompiled.FUNC_ADDSEALER;
import static org.fisco.bcos.sdk.contract.precompiled.contractmgr.ContractLifeCyclePrecompiled.FUNC_FREEZE;
import static org.fisco.bcos.sdk.contract.precompiled.contractmgr.ContractLifeCyclePrecompiled.FUNC_GRANTMANAGER;
import static org.fisco.bcos.sdk.contract.precompiled.contractmgr.ContractLifeCyclePrecompiled.FUNC_UNFREEZE;
import static org.fisco.bcos.sdk.contract.precompiled.crud.CRUD.FUNC_INSERT;
import static org.fisco.bcos.sdk.contract.precompiled.crud.CRUD.FUNC_REMOVE;
import static org.fisco.bcos.sdk.contract.precompiled.crud.CRUD.FUNC_UPDATE;
import static org.fisco.bcos.sdk.contract.precompiled.crud.table.TableFactory.FUNC_CREATETABLE;
import static org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernancePrecompiled.FUNC_FREEZEACCOUNT;
import static org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernancePrecompiled.FUNC_GRANTCOMMITTEEMEMBER;
import static org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernancePrecompiled.FUNC_GRANTOPERATOR;
import static org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernancePrecompiled.FUNC_REVOKECOMMITTEEMEMBER;
import static org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernancePrecompiled.FUNC_REVOKEOPERATOR;
import static org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernancePrecompiled.FUNC_UNFREEZEACCOUNT;
import static org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernancePrecompiled.FUNC_UPDATECOMMITTEEMEMBERWEIGHT;
import static org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernancePrecompiled.FUNC_UPDATETHRESHOLD;
import static org.fisco.bcos.sdk.contract.precompiled.permission.PermissionPrecompiled.FUNC_GRANTWRITE;
import static org.fisco.bcos.sdk.contract.precompiled.permission.PermissionPrecompiled.FUNC_REVOKEWRITE;
import static org.fisco.bcos.sdk.contract.precompiled.sysconfig.SystemConfigPrecompiled.FUNC_SETVALUEBYKEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.precompiledapi.crud.Table;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.contract.precompiled.cns.CNSPrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.fisco.bcos.sdk.model.PrecompiledConstant;
import org.fisco.bcos.sdk.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * send raw transaction through webase-sign to call precompiled
 * 
 * @author marsli
 */
@Slf4j
@Service
public class PrecompiledWithSignService {

    @Autowired
    @Lazy
    TransService transService;
    @Autowired
    private Web3ApiService web3ApiService;
    public static final Integer NODE_LOWEST_SUPPORT_VERSION_INT = 241;
    public static final String GROUP_FILE_NOT_EXIST = "INEXISTENT";

    /**
     * system config: setValueByKey through webase-sign
     * 
     * @return String result {"code":0,"msg":"success"}
     */
    public String setValueByKey(int groupId, String signUserId, String key, String value) {
        List<String> funcParams = new ArrayList<>();
        funcParams.add(key);
        funcParams.add(value);
        // get address and abi of precompiled contract
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.SYSTEM_CONFIG);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.SYSTEM_CONFIG);
        // execute set method
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, contractAddress, abiStr, FUNC_SETVALUEBYKEY, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * permission: grant through webase-sign
     *
     * @return String result {"code":0,"msg":"success"}
     */
    public String grant(int groupId, String signUserId, String tableName, String toAddress) {
        List<String> funcParams = new ArrayList<>();
        funcParams.add(tableName);
        funcParams.add(toAddress);
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.PERMISSION);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.PERMISSION);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, contractAddress, abiStr, FUNC_INSERT, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * permission: revoke through webase-sign
     *
     * @return String result {"code":0,"msg":"success"}
     */
    public String revoke(int groupId, String signUserId, String tableName, String toAddress) {
        List<String> funcParams = new ArrayList<>();
        funcParams.add(tableName);
        funcParams.add(toAddress);
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.PERMISSION);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.PERMISSION);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, contractAddress, abiStr, FUNC_REMOVE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * user table permission: grant through webase-sign
     *
     * @return String result {"code":0,"msg":"success"}
     */
    public String grantWrite(int groupId, String signUserId, String tableName, String toAddress) {
        List<String> funcParams = new ArrayList<>();
        funcParams.add(tableName);
        funcParams.add(toAddress);
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.PERMISSION);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.PERMISSION);
        TransactionReceipt receipt =
            (TransactionReceipt) transService.transHandleWithSign(groupId,
                signUserId, contractAddress, abiStr, FUNC_GRANTWRITE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * user table permission: revoke through webase-sign
     *
     * @return String result {"code":0,"msg":"success"}
     */
    public String revokeWrite(int groupId, String signUserId, String tableName, String toAddress) {
        List<String> funcParams = new ArrayList<>();
        funcParams.add(tableName);
        funcParams.add(toAddress);
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.PERMISSION);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.PERMISSION);
        TransactionReceipt receipt =
            (TransactionReceipt) transService.transHandleWithSign(groupId,
                signUserId, contractAddress, abiStr, FUNC_REVOKEWRITE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }


    /**
     * consensus: add sealer through webase-sign
     * v1.5.0 增加校验群组文件是否存在，P2P连接存在
     */
    public String addSealer(int groupId, String signUserId, String nodeId) {
        // check node id
        if (!isValidNodeID(nodeId)) {
            return PrecompiledRetCode.CODE_INVALID_NODEID.toString();
        }
        List<String> sealerList = web3ApiService.getSealerList(groupId);
        if (sealerList.contains(nodeId)) {
            return ConstantCode.ALREADY_EXISTS_IN_SEALER_LIST.toString();
        }
        List<String> nodeIdList = web3ApiService.getNodeIdList();
        if (!nodeIdList.contains(nodeId)) {
            log.error("nodeId is not connected with others, cannot added as sealer");
            return ConstantCode.PEERS_NOT_CONNECTED.toString();
        }
        // check group file
        if (!containsGroupFile(groupId)) {
            throw new FrontException(ConstantCode.GENESIS_CONF_NOT_FOUND);
        }
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CONSENSUS);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, contractAddress, abiStr, FUNC_ADDSEALER, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * consensus: add observer through webase-sign
     */
    public String addObserver(int groupId, String signUserId, String nodeId) {
        // check node id
        if (!isValidNodeID(nodeId)) {
            return PrecompiledRetCode.CODE_INVALID_NODEID.toString();
        }
        List<String> observerList = web3ApiService.getObserverList(groupId);
        if (observerList.contains(nodeId)) {
            return ConstantCode.ALREADY_EXISTS_IN_OBSERVER_LIST.toString();
        }

        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CONSENSUS);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, contractAddress, abiStr, FUNC_ADDOBSERVER, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * consensus: remove node from list through webase-sign
     */
    public String removeNode(int groupId, String signUserId, String nodeId) {
        List<String> groupPeers = web3ApiService.getGroupPeers(groupId);
        if (!groupPeers.contains(nodeId)) {
            return ConstantCode.ALREADY_REMOVED_FROM_THE_GROUP.toString();
        }
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CONSENSUS);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CONSENSUS);
        TransactionReceipt receipt = new TransactionReceipt();
        try {
            receipt = (TransactionReceipt) transService.transHandleWithSign(groupId,
                    signUserId, contractAddress, abiStr, FUNC_REMOVE, funcParams);
        } catch (RuntimeException e) {
            // firstly remove node that sdk connected to the node, return the request that present
            // susscces
            // because the exception is throwed by getTransactionReceipt, we need ignore it.
            if (e.getMessage().contains("Don't send requests to this group")) {
                return ConstantCode.ALREADY_REMOVED_FROM_THE_GROUP.toString();
            } else {
                throw e;
            }
        }
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * check node id
     */
    private boolean isValidNodeID(String _nodeID) {
        boolean flag = false;
        List<String> nodeIDs = web3ApiService.getNodeIdList();
        for (String nodeID : nodeIDs) {
            if (_nodeID.equals(nodeID)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * CRUD: create table through webase-sign
     */
    public String createTable(int groupId, String signUserId, Table table) {
        List<String> funcParams = new ArrayList<>();
        funcParams.add(table.getTableName());
        funcParams.add(table.getKey());
        String valueFieldsString = TableCRUDService.convertValueFieldsToString(table.getValueFields());
        funcParams.add(valueFieldsString);
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.TABLE_FACTORY);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.TABLE_FACTORY);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, contractAddress, abiStr, FUNC_CREATETABLE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * CRUD: insert table through webase-sign
     */
    public String insert(int groupId, String signUserId, Table table, Entry entry) {
        checkTableKeyLength(table);
        // trans
        String entryJsonStr;
        try {
            entryJsonStr =
                    ObjectMapperFactory.getObjectMapper().writeValueAsString(entry.getFieldNameToValue());
        } catch (JsonProcessingException e) {
            log.error("remove JsonProcessingException:[]", e);
            throw new FrontException(ConstantCode.CRUD_PARSE_CONDITION_ENTRY_FIELD_JSON_ERROR);
        }
        List<String> funcParams = new ArrayList<>();
        funcParams.add(table.getTableName());
        funcParams.add(table.getKey());
        funcParams.add(entryJsonStr);
        funcParams.add(table.getOptional());
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CRUD);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CRUD);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, contractAddress, abiStr, FUNC_INSERT, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * CRUD: update table through webase-sign
     */
    public String update(int groupId, String signUserId, Table table, Entry entry,
            Condition condition) {
        checkTableKeyLength(table);
        // trans
        String entryJsonStr, conditionStr;
        try {
            entryJsonStr =
                    ObjectMapperFactory.getObjectMapper().writeValueAsString(entry.getFieldNameToValue());
            conditionStr = ObjectMapperFactory.getObjectMapper()
                    .writeValueAsString(condition.getConditions());
        } catch (JsonProcessingException e) {
            log.error("update JsonProcessingException:[]", e);
            throw new FrontException(ConstantCode.CRUD_PARSE_CONDITION_ENTRY_FIELD_JSON_ERROR);
        }
        List<String> funcParams = new ArrayList<>();
        funcParams.add(table.getTableName());
        funcParams.add(table.getKey());
        funcParams.add(entryJsonStr);
        funcParams.add(conditionStr);
        funcParams.add(table.getOptional());
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CRUD);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CRUD);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, contractAddress, abiStr, FUNC_UPDATE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * CRUD: remove table through webase-sign
     */
    public String remove(int groupId, String signUserId, Table table, Condition condition) {
        checkTableKeyLength(table);
        // trans
        String conditionStr;
        try {
            conditionStr = ObjectMapperFactory.getObjectMapper()
                    .writeValueAsString(condition.getConditions());
        } catch (JsonProcessingException e) {
            log.error("remove JsonProcessingException:[]", e);
            throw new FrontException(ConstantCode.CRUD_PARSE_CONDITION_ENTRY_FIELD_JSON_ERROR);
        }
        List<String> funcParams = new ArrayList<>();
        funcParams.add(table.getTableName());
        funcParams.add(table.getKey());
        funcParams.add(conditionStr);
        funcParams.add(table.getOptional());
        String contractAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CRUD);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CRUD);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, contractAddress, abiStr, FUNC_REMOVE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    private void checkTableKeyLength(Table table) {
        if (table.getKey().length() > PrecompiledConstant.TABLE_KEY_MAX_LENGTH) {
            throw new FrontException(ConstantCode.CRUD_TABLE_KEY_LENGTH_ERROR.getCode(),
                    "The value of the table key exceeds the maximum limit("
                            + PrecompiledConstant.TABLE_KEY_MAX_LENGTH + ").");
        }
    }

    public String contractFreeze(int groupId, String signUserId, String contractAddress) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(contractAddress);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CSM);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CSM);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_FREEZE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String contractUnfreeze(int groupId, String signUserId, String contractAddress) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(contractAddress);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CSM);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CSM);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_UNFREEZE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String contractGrantManager(int groupId, String signUserId, String contractAddress,
            String grantAddress) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(contractAddress);
        funcParams.add(grantAddress);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CSM);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CSM);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_GRANTMANAGER, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * chain governance, above FISCO-BCOS v2.5.0
     */
    public String grantChainCommittee(int groupId, String signUserId, String toAddress) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CHAIN_GOVERN);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CHAIN_GOVERN);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_GRANTCOMMITTEEMEMBER,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String revokeChainCommittee(int groupId, String signUserId, String toAddress) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CHAIN_GOVERN);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CHAIN_GOVERN);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_REVOKECOMMITTEEMEMBER,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String updateChainCommitteeWeight(int groupId, String signUserId, String toAddress,
            int weight) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        funcParams.add(String.valueOf(weight));
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CHAIN_GOVERN);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CHAIN_GOVERN);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_UPDATECOMMITTEEMEMBERWEIGHT,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String updateThreshold(int groupId, String signUserId, int threshold) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(String.valueOf(threshold));
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CHAIN_GOVERN);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CHAIN_GOVERN);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_UPDATETHRESHOLD,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String grantOperator(int groupId, String signUserId, String toAddress) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CHAIN_GOVERN);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CHAIN_GOVERN);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_GRANTOPERATOR, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String revokeOperator(int groupId, String signUserId, String toAddress) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CHAIN_GOVERN);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CHAIN_GOVERN);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_REVOKEOPERATOR, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String freezeAccount(int groupId, String signUserId, String toAddress) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CHAIN_GOVERN);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CHAIN_GOVERN);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_FREEZEACCOUNT, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String unfreezeAccount(int groupId, String signUserId, String toAddress) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CHAIN_GOVERN);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CHAIN_GOVERN);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, FUNC_UNFREEZEACCOUNT,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String registerCns(int groupId, String signUserId, String cnsContractName, String version,
            String contractAddress, String abiInfo) {
        // trans
        List<String> funcParams = new ArrayList<>();
        funcParams.add(cnsContractName);
        funcParams.add(version);
        funcParams.add(contractAddress);
        funcParams.add(abiInfo);
        String precompiledAddress = PrecompiledCommonInfo.getAddress(PrecompiledTypes.CNS);
        String abiStr = PrecompiledCommonInfo.getAbi(PrecompiledTypes.CNS);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSign(groupId,
                        signUserId, precompiledAddress, abiStr, CNSPrecompiled.FUNC_INSERT, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * handle receipt of precompiled
     * @related: PrecompiledRetCode and ReceiptParser
     * return: {"code":1,"msg":"Success"} => {"code":0,"message":"Success"}
     */
    private String handleTransactionReceipt(TransactionReceipt receipt) {
        log.debug("handle tx receipt of precompiled");
        try {
            RetCode sdkRetCode = ReceiptParser.parseTransactionReceipt(receipt);
            log.info("handleTransactionReceipt sdkRetCode:{}", sdkRetCode);
            if (sdkRetCode.getCode() >= 0) {
                return new BaseResponse(ConstantCode.RET_SUCCESS, sdkRetCode.getMessage()).toString();
            } else {
                throw new FrontException(sdkRetCode.getCode(), sdkRetCode.getMessage());
            }
        } catch (ContractException e) {
            log.error("handleTransactionReceipt e:[]", e);
            throw new FrontException(e.getErrorCode(), e.getMessage());
        }
    }

    /**
     * check group config file exist before add as sealer/observer
     */
    private boolean containsGroupFile(int groupId) {
        log.info("check front's node contains group file of groupId:{}", groupId);
        ClientVersion clientVersion = web3ApiService.getClientVersion();
        int supportVer = CommonUtils.getVersionFromStr(clientVersion.getSupportedVersion());
        if (supportVer < 241) {
            log.info("client support version not support dynamic group");
            return true;
        }
        // INEXISTENT
        String groupStatus = (String) web3ApiService.querySingleGroupStatus(groupId).getData();
        if (GROUP_FILE_NOT_EXIST.equals(groupStatus)) {
            log.error("node contains no group file to add in this group:{}", groupId);
            return false;
        }
        return true;
    }

}
