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
package com.webank.webase.front.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fisco.bcos.web3j.precompile.cns.CnsService;

import java.io.IOException;
import java.util.Map;

/**
 * Constants and tool function related with Precompiled module
 */
public class PrecompiledUtils {


    public static final String ContractLogFileName = "deploylog.txt";

    // SystemConfig key
    public static final String TxCountLimit = "tx_count_limit";
    public static final String TxGasLimit = "tx_gas_limit";
    // node consensus type
    public static final String NODE_TYPE_SEALER = "sealer";
    public static final String NODE_TYPE_OBSERVER = "observer";
    public static final String NODE_TYPE_REMOVE = "remove";
    // permission manage type
    public static final String PERMISSION_TYPE_PERMISSION = "permission";
    public static final String PERMISSION_TYPE_DEPLOY_AND_CREATE = "deployAndCreate";
    public static final String PERMISSION_TYPE_USERTABLE = "userTable";
    public static final String PERMISSION_TYPE_NODE = "node";
    public static final String PERMISSION_TYPE_SYS_CONFIG = "sysConfig";
    public static final String PERMISSION_TYPE_CNS = "cns";

    public static final int InvalidReturnNumber = -100;
    public static final int QueryLogCount = 20;
    public static final int LogMaxCount = 10000;
    public static final String PositiveIntegerRange = "from 1 to 2147483647";
    public static final String NonNegativeIntegerRange = "from 0 to 2147483647";
    public static final String DeployLogntegerRange = "from 1 to 100";
    public static final String NodeIdLength = "128";
    public static final String TxGasLimitRange = "from 100000 to 2147483647";
    public static final String EMPTY_CONTRACT_ADDRESS =
            "0x0000000000000000000000000000000000000000";
    public static final String EMPTY_OUTPUT = "0x";
    public static final int TxGasLimitMin = 10000;

    public static int PermissionCode = 0;
    public static int TableExist = 0;
    public static int PRECOMPILED_SUCCESS = 0; // permission denied
    public static int PERMISSION_DENIED = -50000; // permission denied
    public static int TABLE_NAME_ALREADY_EXIST = -50001; // table name already exist
    public static int UNKNOWN_FUNCTION_CALL = -50100; // unknown function call
    public static int TABLE_NOT_EXIST = -50101; // table does not exist
    public static int TABLE_NAME_AND_ADDRESS_ALREADY_EXIST = -51000; // table name and address already exist
    public static int TABLE_NAME_AND_ADDRESS_NOT_EXIST = -51001; // table name and address does not exist
    public static int SDK_INVALID_NODE_ID = -51100; // invalid node ID
    public static int LAST_SEALER_CANNOT_BE_MOVED = -51101; // the last sealer cannot be removed
    public static int SDK_NOT_REACHABLE_NODE = -51102; // the node is not reachable
    public static int SDK_NOT_A_GROUP_PEER_NODE = -51103; // the node is not a group peer
    public static int SDK_ALREADY_SEALER = -51104; // the node is already in the sealer list
    public static int SDK_ALREADY_OBSERVER = -51105; // the node is already in the observer list
    public static int SDK_CONTRACT_NAME_AND_VERSION_EXIST = -51200; // contract name and version already exist
    public static int SDK_VERSION_STRING_LENGTH_EXCEEDS_LIMIT = -51201; // version string length exceeds the maximum limit
    public static int INVALID_CONFIGURATION_ENTRY = -51300; // invalid configuration entry
    public static int CONTRACT_NAME_AND_VERSION_EXIST = -51500; // contract name and version already exist
    public static int SQL_CONDITION_PARSE_ERROR = -51501; // condition parse error
    public static int SQL_CONDITION_OPERATION_UNDEFINED = -51502; // condition operation undefined
    public static int CRUD_SQL_ERROR = -51503; // process sql error

    public static int SYS_TABLE_KEY_MAX_LENGTH = 58; // 64- "_user_".length
    public static int SYS_TABLE_KEY_FIELD_NAME_MAX_LENGTH = 64;
    public static int SYS_TABLE_VALUE_FIELD_MAX_LENGTH = 1024;
    public static int USER_TABLE_KEY_VALUE_MAX_LENGTH = 255;
    public static int USER_TABLE_FIELD_NAME_MAX_LENGTH = 64;
    public static int USER_TABLE_FIELD_VALUE_MAX_LENGTH = 16 * 1024 * 1024 - 1;


    public static boolean checkVersion(String version) {

        if (version.length() > CnsService.MAX_VERSION_LENGTH) { // length exceeds
            return false;
        }else if (!version.matches("^[A-Za-z0-9.]+$")) { // check version's character
            return false;
        }else {
            return true;
        }
    }

    public static boolean checkNodeId(String nodeId) {
        if (nodeId.length() != 128) {
            return false;
        }else {
            return true;
        }
    }

    public static JsonNode string2Json(String str)
            throws JsonParseException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(str);
        return actualObj;
    }

    public static Map string2Map(String str)
            throws JsonParseException, IOException{
        Map<String, Object> resMap;
        ObjectMapper mapper = new ObjectMapper();
        resMap = mapper.readValue(str, Map.class);
        return resMap;
    }
}
