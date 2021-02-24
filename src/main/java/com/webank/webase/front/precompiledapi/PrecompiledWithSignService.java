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
import com.webank.webase.front.precompiledapi.crud.Table;
import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.web3api.Web3ApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.contract.precompiled.cns.CNSPrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.model.PrecompiledConstant;
import org.fisco.bcos.sdk.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    TransService transService;
    @Autowired
    private Web3ApiService web3ApiService;

    /**
     * system config: setValueByKey through webase-sign
     * 
     * @return String result {"code":0,"msg":"success"}
     */
    public String setValueByKey(int groupId, String signUserId, String key, String value) {
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(key);
        funcParams.add(value);
        // execute set method
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.SYSTEM_CONFIG, FUNC_SETVALUEBYKEY, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * permission: grant through webase-sign
     * 
     * @return String result {"code":0,"msg":"success"}
     */
    public String grant(int groupId, String signUserId, String tableName, String toAddress) {
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(tableName);
        funcParams.add(toAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.PERMISSION, FUNC_INSERT, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * permission: revoke through webase-sign
     * 
     * @return String result {"code":0,"msg":"success"}
     */
    public String revoke(int groupId, String signUserId, String tableName, String toAddress) {
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(tableName);
        funcParams.add(toAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.PERMISSION, FUNC_REMOVE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * user table permission: grant through webase-sign
     *
     * @return String result {"code":0,"msg":"success"}
     */
    public String grantWrite(int groupId, String signUserId, String tableName, String toAddress) {
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(tableName);
        funcParams.add(toAddress);
        TransactionReceipt receipt =
            (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                signUserId, PrecompiledTypes.PERMISSION, FUNC_GRANTWRITE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * user table permission: revoke through webase-sign
     *
     * @return String result {"code":0,"msg":"success"}
     */
    public String revokeWrite(int groupId, String signUserId, String tableName, String toAddress) {
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(tableName);
        funcParams.add(toAddress);
        TransactionReceipt receipt =
            (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                signUserId, PrecompiledTypes.PERMISSION, FUNC_REVOKEWRITE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }


    /**
     * consensus: add sealer through webase-sign
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
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CONSENSUS, FUNC_ADDSEALER, funcParams);
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
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CONSENSUS, FUNC_ADDOBSERVER, funcParams);
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
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        TransactionReceipt receipt = new TransactionReceipt();
        try {
            receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                    signUserId, PrecompiledTypes.CONSENSUS, FUNC_REMOVE, funcParams);
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

    //todo 复制控制台的格式化接工具类
    /**
     * CRUD: create table through webase-sign
     */
    public int createTable(int groupId, String signUserId, Table table) {
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(table.getTableName());
        funcParams.add(table.getKey());
        funcParams.add(table.getValueFields());
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.TABLE_FACTORY, FUNC_CREATETABLE, funcParams);
        return this.handleTransactionReceiptCRUD(receipt);
    }

    /**
     * CRUD: insert table through webase-sign
     */
    public int insert(int groupId, String signUserId, Table table, Entry entry) {
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
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(table.getTableName());
        funcParams.add(table.getKey());
        funcParams.add(entryJsonStr);
        funcParams.add(table.getOptional());
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CRUD, FUNC_INSERT, funcParams);
        return this.handleTransactionReceiptCRUD(receipt);
    }

    /**
     * CRUD: update table through webase-sign
     */
    public int update(int groupId, String signUserId, Table table, Entry entry,
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
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(table.getTableName());
        funcParams.add(table.getKey());
        funcParams.add(entryJsonStr);
        funcParams.add(conditionStr);
        funcParams.add(table.getOptional());
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CRUD, FUNC_UPDATE, funcParams);
        return this.handleTransactionReceiptCRUD(receipt);
    }

    /**
     * CRUD: remove table through webase-sign
     */
    public int remove(int groupId, String signUserId, Table table, Condition condition) {
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
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(table.getTableName());
        funcParams.add(table.getKey());
        funcParams.add(conditionStr);
        funcParams.add(table.getOptional());
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CRUD, FUNC_REMOVE, funcParams);
        return this.handleTransactionReceiptCRUD(receipt);
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
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(contractAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CSM, FUNC_FREEZE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String contractUnfreeze(int groupId, String signUserId, String contractAddress) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(contractAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CSM, FUNC_UNFREEZE, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String contractGrantManager(int groupId, String signUserId, String contractAddress,
            String grantAddress) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(contractAddress);
        funcParams.add(grantAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CSM, FUNC_GRANTMANAGER, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * chain governance, above FISCO-BCOS v2.5.0
     */
    public String grantChainCommittee(int groupId, String signUserId, String toAddress) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CHAIN_GOVERN, FUNC_GRANTCOMMITTEEMEMBER,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String revokeChainCommittee(int groupId, String signUserId, String toAddress) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CHAIN_GOVERN, FUNC_REVOKECOMMITTEEMEMBER,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String updateChainCommitteeWeight(int groupId, String signUserId, String toAddress,
            int weight) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        funcParams.add(weight);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CHAIN_GOVERN, FUNC_UPDATECOMMITTEEMEMBERWEIGHT,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String updateThreshold(int groupId, String signUserId, int threshold) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(threshold);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CHAIN_GOVERN, FUNC_UPDATETHRESHOLD,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String grantOperator(int groupId, String signUserId, String toAddress) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CHAIN_GOVERN, FUNC_GRANTOPERATOR, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String revokeOperator(int groupId, String signUserId, String toAddress) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CHAIN_GOVERN, FUNC_REVOKEOPERATOR, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String freezeAccount(int groupId, String signUserId, String toAddress) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CHAIN_GOVERN, FUNC_FREEZEACCOUNT, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String unfreezeAccount(int groupId, String signUserId, String toAddress) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(toAddress);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CHAIN_GOVERN, FUNC_UNFREEZEACCOUNT,
                        funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    public String registerCns(int groupId, String signUserId, String contractName, String version,
            String contractAddress, String abiInfo) {
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(contractName);
        funcParams.add(version);
        funcParams.add(contractAddress);
        funcParams.add(abiInfo);
        TransactionReceipt receipt =
                (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
                        signUserId, PrecompiledTypes.CNS, CNSPrecompiled.FUNC_INSERT, funcParams);
        return this.handleTransactionReceipt(receipt);
    }

    /**
     * handle receipt of precompiled
     * @related: PrecompiledRetCode and ReceiptParser
     * @throws IOException
     */
    private String handleTransactionReceipt(TransactionReceipt receipt) {
        log.debug("handle tx receipt of precompiled");
        try {
            return ReceiptParser.parseTransactionReceipt(receipt).toString();
        } catch (ContractException e) {
            throw new FrontException(e.getErrorCode(), e.getResponseOutput().getOutput());
        }
//        String status = receipt.getStatus();
//        if (!"0x0".equals(status)) {
//            throw new FrontException(TX_RECEIPT_CODE_ERROR.getCode(),
//                    StatusCode.getStatusMessage(receipt.getStatus(), receipt.getMessage()));
//        } else {
//            if (receipt.getOutput() != null) {
//                try {
//                    String codeMsgFromOutput =
//                            PrecompiledConstant.getJsonStr(receipt.getOutput(), web3j);
//                    return PrecompiledUtils.handleReceiptOutput(codeMsgFromOutput);
//                } catch (IOException e) {
//                    log.error("handleTransactionReceipt getJsonStr of error tx receipt fail:[]", e);
//                    throw new FrontException(
//                            ConstantCode.TX_RECEIPT_OUTPUT_PARSE_JSON_FAIL.getCode(),
//                            e.getMessage());
//                }
//            } else {
//                throw new FrontException(ConstantCode.TX_RECEIPT_OUTPUT_NULL);
//            }
//        }
    }

    public int handleTransactionReceiptCRUD(TransactionReceipt receipt) {
        try {
            return ReceiptParser.parseTransactionReceipt(receipt).getCode();
        } catch (ContractException e) {
            throw new FrontException(e.getErrorCode(), e.getResponseOutput().getOutput());
        }


//
//        String status = receipt.getStatus();
//        if (!"0x0".equals(status)) {
//            throw new FrontException(TX_RECEIPT_CODE_ERROR.getCode(),
//                    StatusCode.getStatusMessage(status, receipt.getMessage()));
//        }
//        String output = receipt.getOutput();
//        if (!"0x".equals(output)) {
//            return new BigInteger(output.substring(2, output.length()), 16).intValue();
//        } else {
//            throw new FrontException(ConstantCode.TX_RECEIPT_OUTPUT_NULL);
//        }
    }

}
