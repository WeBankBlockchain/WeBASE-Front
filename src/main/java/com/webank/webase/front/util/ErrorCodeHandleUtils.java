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

package com.webank.webase.front.util;

import com.webank.webase.front.base.code.RetCode;
import lombok.extern.slf4j.Slf4j;

/**
 * chain error code handle util
 */
@Slf4j
public class ErrorCodeHandleUtils {

    public static final String NODE_INACTIVE_MSG = "no active connection available";
    public static final RetCode NODE_NOT_ACTIVE = RetCode.mark(-1, "no active connection available with node, please check node status");

    // json rpc error
    public static final RetCode RPC_INVALID_JSON_REQUEST = RetCode.mark(-32600, "invalid json request");
    public static final RetCode RPC_METHOD_NOT_FOUND = RetCode.mark(-32601, "method not found / not support");
    public static final RetCode RPC_INVALID_PARAMS = RetCode.mark(-32602, "invalid params when request");
    public static final RetCode RPC_INTERNAL_ERROR	 = RetCode.mark(-32603, "node internal error");
    public static final RetCode RPC_PROCEDURE_IS_METHOD = RetCode.mark(-32604, "request require id value for procedure");
    public static final RetCode RPC_JSON_PARSE_ERROR = RetCode.mark(-32700, "node receive json that parse error");

    // fisco bcos rpc error
    public static final RetCode FISCO_RPC_GROUP_ID_NOT_EXIST = RetCode.mark(-40001, "GroupID does not exist");
    public static final RetCode FISCO_RPC_JSON_PARSE_ERROR = RetCode.mark(-40002, "Response json parse error");
    public static final RetCode FISCO_RPC_BLOCKHASH_NOT_EXIST = RetCode.mark(-40003, "BlockHash does not exist");
    public static final RetCode FISCO_RPC_BLOCKNUMBER_NOT_EXIST = RetCode.mark(-40004, "BlockNumber does not exist");
    public static final RetCode FISCO_RPC_TX_INDEX_OUT_OF_RANGE = RetCode.mark(-40005, "TransactionIndex is out of range");
    public static final RetCode FISCO_RPC_CALL_FROM_NEEDED = RetCode.mark(-40006, "Call needs a 'from' field");
    public static final RetCode FISCO_RPC_PBFT_VIEW_SUPPORT = RetCode.mark(-40007, "Only pbft consensus supports the view property");
    public static final RetCode FISCO_RPC_INVALID_SYSTEM_CONFIG = RetCode.mark(-40008, "Invalid System Config");
    public static final RetCode FISCO_RPC_GROUP_NOT_BELONG = RetCode.mark(-40009, "Don't send requests to this group, the node doesn't belong to the group");
    public static final RetCode FISCO_RPC_INIT_INCOMPLETE = RetCode.mark(-400010, "RPC module initialization is incomplete");

    // precompiled service api error
    public static final RetCode PRECOMPILED_PERMISSION_DENIED = RetCode.mark(-50000, "Permission denied");
    public static final RetCode PRECOMPILED_TABLE_NAME_EXIST = RetCode.mark(-50001, "table name already exist");
    public static final RetCode PRECOMPILED_TABLE_NOT_EXIST = RetCode.mark(-50100, "table does not exist");
    public static final RetCode PRECOMPILED_UNKNOWN_FUNCTION_CALL = RetCode.mark(-50101, "unknow function call");
    public static final RetCode PRECOMPILED_ADDRESS_INVALID = RetCode.mark(-50102, "address invalid");
    public static final RetCode PRECOMPILED_TABLE_NAME_AND_ADDRESS_EXIST = RetCode.mark(-50100, "table name and address already exist");
    public static final RetCode PRECOMPILED_TABLE_NAME_AND_ADDRESS_NOT_EXIST = RetCode.mark(-51001, "table name and address does not exist");
    public static final RetCode PRECOMPILED_INVALID_NODE_ID = RetCode.mark(-51100, "invalid node ID");
    public static final RetCode PRECOMPILED_LAST_SEALER_CANNOT_REMOVE = RetCode.mark(-51101, "the last sealer cannot be removed");
    public static final RetCode PRECOMPILED_NODE_NOT_REACHABLE = RetCode.mark(-51102, "the node is not reachable");
    public static final RetCode PRECOMPILED_NODE_NOT_GROUP_PEER = RetCode.mark(-51103, "the node is not a group peer");
    public static final RetCode PRECOMPILED_NODE_ALREADY_SEALER = RetCode.mark(-51104, "the node is already in the sealer list");
    public static final RetCode PRECOMPILED_NODE_ALREADY_OBSERVER = RetCode.mark(-51105, "the node is already in the observer list");
    public static final RetCode PRECOMPILED_CONTRACT_NAME_VERSION_EXIST = RetCode.mark(-51200, "contract name and version already exist");
    public static final RetCode PRECOMPILED_VERSION_LENGTH_EXCEED = RetCode.mark(-51201, "version string length exceeds the maximum limit");
    public static final RetCode PRECOMPILED_INVALID_CONFIG_ENTRY = RetCode.mark(-51300, "invalid configuration entry");
    public static final RetCode PRECOMPILED_PARSE_ENTRY_ERROR = RetCode.mark(-51500, "entry parse error");
    public static final RetCode PRECOMPILED_CONDITION_PARSE_ERROR = RetCode.mark(-51501, "condition parse error");
    public static final RetCode PRECOMPILED_CONDITION_OPERATION_UNDEFINED = RetCode.mark(-51502, "condition operation undefined");

    /**
     * ex: {"error":{"code":-32601,"message":"METHOD_NOT_FOUND: The method being requested is not available on this server"},"id":133330,"jsonrpc":"2.0"}
     * @param errorMsg
     * @return
     */
    public static RetCode handleErrorMsg(String errorMsg) {
        // node inactive exception
        if (errorMsg == null) {
            return null;
        }
        if (errorMsg.contains(NODE_INACTIVE_MSG)) {
            return NODE_NOT_ACTIVE;
        }
        // json rpc
        if (errorMsg.contains(RPC_INVALID_JSON_REQUEST.getCode().toString())) {
            return RPC_INVALID_JSON_REQUEST;
        }
        if (errorMsg.contains(RPC_METHOD_NOT_FOUND.getCode().toString())) {
            return RPC_METHOD_NOT_FOUND;
        }
        if (errorMsg.contains(RPC_INVALID_PARAMS.getCode().toString())) {
            return RPC_INVALID_PARAMS;
        }
        if (errorMsg.contains(RPC_INTERNAL_ERROR.getCode().toString())) {
            return RPC_INTERNAL_ERROR;
        }
        if (errorMsg.contains(RPC_PROCEDURE_IS_METHOD.getCode().toString())) {
            return RPC_PROCEDURE_IS_METHOD;
        }
        if (errorMsg.contains(RPC_JSON_PARSE_ERROR.getCode().toString())) {
            return RPC_JSON_PARSE_ERROR;
        }

        // fisco rpc
        if (errorMsg.contains(FISCO_RPC_GROUP_ID_NOT_EXIST.getCode().toString())) {
            return FISCO_RPC_GROUP_ID_NOT_EXIST;
        }
        if (errorMsg.contains(FISCO_RPC_JSON_PARSE_ERROR.getCode().toString())) {
            return FISCO_RPC_JSON_PARSE_ERROR;
        }
        if (errorMsg.contains(FISCO_RPC_BLOCKHASH_NOT_EXIST.getCode().toString())) {
            return FISCO_RPC_BLOCKHASH_NOT_EXIST;
        }
        if (errorMsg.contains(FISCO_RPC_BLOCKNUMBER_NOT_EXIST.getCode().toString())) {
            return FISCO_RPC_BLOCKNUMBER_NOT_EXIST;
        }
        if (errorMsg.contains(FISCO_RPC_TX_INDEX_OUT_OF_RANGE.getCode().toString())) {
            return FISCO_RPC_TX_INDEX_OUT_OF_RANGE;
        }
        if (errorMsg.contains(FISCO_RPC_CALL_FROM_NEEDED.getCode().toString())) {
            return FISCO_RPC_CALL_FROM_NEEDED;
        }
        if (errorMsg.contains(FISCO_RPC_PBFT_VIEW_SUPPORT.getCode().toString())) {
            return FISCO_RPC_PBFT_VIEW_SUPPORT;
        }
        if (errorMsg.contains(FISCO_RPC_INVALID_SYSTEM_CONFIG.getCode().toString())) {
            return FISCO_RPC_INVALID_SYSTEM_CONFIG;
        }
        if (errorMsg.contains(FISCO_RPC_GROUP_NOT_BELONG.getCode().toString())) {
            return FISCO_RPC_GROUP_NOT_BELONG;
        }
        if (errorMsg.contains(FISCO_RPC_INIT_INCOMPLETE.getCode().toString())) {
            return FISCO_RPC_INIT_INCOMPLETE;
        }

        // precompiled
        if (errorMsg.contains(PRECOMPILED_PERMISSION_DENIED.getCode().toString())) {
            return PRECOMPILED_PERMISSION_DENIED;
        }
        if (errorMsg.contains(PRECOMPILED_TABLE_NAME_EXIST.getCode().toString())) {
            return PRECOMPILED_TABLE_NAME_EXIST;
        }
        if (errorMsg.contains(PRECOMPILED_TABLE_NOT_EXIST.getCode().toString())) {
            return PRECOMPILED_TABLE_NOT_EXIST;
        }
        if (errorMsg.contains(PRECOMPILED_UNKNOWN_FUNCTION_CALL.getCode().toString())) {
            return PRECOMPILED_UNKNOWN_FUNCTION_CALL;
        }
        if (errorMsg.contains(PRECOMPILED_ADDRESS_INVALID.getCode().toString())) {
            return PRECOMPILED_ADDRESS_INVALID;
        }
        if (errorMsg.contains(PRECOMPILED_TABLE_NAME_AND_ADDRESS_EXIST.getCode().toString())) {
            return PRECOMPILED_TABLE_NAME_AND_ADDRESS_EXIST;
        }
        if (errorMsg.contains(PRECOMPILED_TABLE_NAME_AND_ADDRESS_NOT_EXIST.getCode().toString())) {
            return PRECOMPILED_TABLE_NAME_AND_ADDRESS_NOT_EXIST;
        }
        if (errorMsg.contains(PRECOMPILED_INVALID_NODE_ID.getCode().toString())) {
            return PRECOMPILED_INVALID_NODE_ID;
        }
        if (errorMsg.contains(PRECOMPILED_LAST_SEALER_CANNOT_REMOVE.getCode().toString())) {
            return PRECOMPILED_LAST_SEALER_CANNOT_REMOVE;
        }
        if (errorMsg.contains(PRECOMPILED_NODE_NOT_REACHABLE.getCode().toString())) {
            return PRECOMPILED_NODE_NOT_REACHABLE;
        }
        if (errorMsg.contains(PRECOMPILED_NODE_NOT_GROUP_PEER.getCode().toString())) {
            return PRECOMPILED_NODE_NOT_GROUP_PEER;
        }
        if (errorMsg.contains(PRECOMPILED_NODE_ALREADY_SEALER.getCode().toString())) {
            return PRECOMPILED_NODE_ALREADY_SEALER;
        }
        if (errorMsg.contains(PRECOMPILED_NODE_ALREADY_OBSERVER.getCode().toString())) {
            return PRECOMPILED_NODE_ALREADY_OBSERVER;
        }
        if (errorMsg.contains(PRECOMPILED_CONTRACT_NAME_VERSION_EXIST.getCode().toString())) {
            return PRECOMPILED_CONTRACT_NAME_VERSION_EXIST;
        }
        if (errorMsg.contains(PRECOMPILED_VERSION_LENGTH_EXCEED.getCode().toString())) {
            return PRECOMPILED_VERSION_LENGTH_EXCEED;
        }
        if (errorMsg.contains(PRECOMPILED_INVALID_CONFIG_ENTRY.getCode().toString())) {
            return PRECOMPILED_INVALID_CONFIG_ENTRY;
        }
        if (errorMsg.contains(PRECOMPILED_PARSE_ENTRY_ERROR.getCode().toString())) {
            return PRECOMPILED_PARSE_ENTRY_ERROR;
        }
        if (errorMsg.contains(PRECOMPILED_CONDITION_PARSE_ERROR.getCode().toString())) {
            return PRECOMPILED_CONDITION_PARSE_ERROR;
        }
        if (errorMsg.contains(PRECOMPILED_CONDITION_OPERATION_UNDEFINED.getCode().toString())) {
            return PRECOMPILED_CONDITION_OPERATION_UNDEFINED;
        }
        return null;
    }
}
