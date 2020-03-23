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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fisco.bcos.web3j.precompile.cns.CnsService;

import java.io.IOException;
import java.util.List;
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
    public static int TABLE_NAME_AND_ADDRESS_ALREADY_EXIST = -51000; // table name and address already exist
    public static int TABLE_NAME_AND_ADDRESS_NOT_EXIST = -51001; // table name and address does not exist
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
