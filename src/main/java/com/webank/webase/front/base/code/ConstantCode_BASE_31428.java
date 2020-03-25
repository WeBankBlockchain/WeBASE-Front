/**
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.base.code;


/**
 * Code Constant
 * String constants used in ReqXXXParam for validation
 * RetCode constants used in controller for response
 */
public class ConstantCode {

    /* return success */
    public static final RetCode RET_SUCCEED = RetCode.mark(0, "success");

    /* paramaters check */
    public static final RetCode PARAM_FAIL_GROUP_ID_IS_EMPTY =  RetCode.mark(201001, "groupId cannot be empty");
    public static final String PARAM_FAIL_USER_IS_EMPTY = "{\"code\":201002,\"message\":\"user cannot be empty\"}";
    public static final String PARAM_FAIL_USEAES_IS_EMPTY =
        "{\"code\":201003,\"message\":\"useAes cannot be empty\"}";
    public static final String PARAM_FAIL_VERSION_IS_EMPTY = "{\"code\":201004,\"message\":\"version cannot be empty\"}";
    public static final String PARAM_FAIL_FUNCNAME_IS_EMPTY = "{\"code\":201005,\"message\":\"funcName cannot be empty\"}";
    public static final String PARAM_FAIL_ABIINFO_IS_EMPTY = "{\"code\":201006,\"message\":\"abiInfo cannot be empty\"}";
    public static final String PARAM_FAIL_CONTRACTBIN_IS_EMPTY = "{\"code\":201007,\"message\":\"contractBin cannot be empty\"}";

    /* general error */
    public static final RetCode CONTRACT_DEPLOYED_ERROR = RetCode.mark(201008, "contract's current version has been deployed");
    public static final RetCode CONTRACT_NOT_DEPLOY_ERROR = RetCode.mark(201009, "contract is not deployed");
    public static final RetCode ABI_SAVE_ERROR = RetCode.mark(201010, "save abi error");
    public static final RetCode IN_FUNCPARAM_ERROR = RetCode.mark(201011, "contract funcParam is error");
    public static final RetCode BLOCK_NUMBER_ERROR = RetCode.mark(201012, "requst blockNumber is greater than latest");
    public static final RetCode ABI_GET_ERROR = RetCode.mark(201013, "get abi error");
    public static final RetCode CONTRACT_DEPLOY_ERROR = RetCode.mark(201014, "contract deploy error");
    public static final RetCode PRIVATEKEY_IS_NULL = RetCode.mark(201015, "user's privateKey is null");
    public static final RetCode FILE_IS_NOT_EXIST = RetCode.mark(201016, "file is not exist");
    public static final RetCode GET_NODE_CONFIG_FAILE = RetCode.mark(201017, "failed to get node config");
    public static final RetCode BLOCKNUMBER_AND_PBFTVIEW_UNCHANGED = RetCode.mark(201018, "blockNumber and pbftView unchanged");
    public static final RetCode IN_FUNCTION_ERROR = RetCode.mark(201019, "request function is error");
    public static final RetCode TRANSACTION_QUERY_FAILED = RetCode.mark(201020, "transaction query from chain failed");
    public static final RetCode TRANSACTION_SEND_FAILED = RetCode.mark(201021, "transaction send to chain failed");
    public static final RetCode NODE_REQUEST_FAILED = RetCode.mark(201022, "node request failed");
    public static final RetCode CONTRACT_EXISTS = RetCode.mark(201023, "contract already exists");
    public static final RetCode CONTRACT_NAME_REPEAT = RetCode.mark(201024, "contract name cannot be repeated");
    public static final RetCode INVALID_CONTRACT_ID = RetCode.mark(201025, "invalid contract id");
    public static final RetCode CONTRACT_HAS_BEAN_DEPLOYED = RetCode.mark(201026, "contract has been deployed");
    public static final RetCode SEND_ABI_INFO_FAIL = RetCode.mark(201027, "send abiInfo fail");
    public static final RetCode CONTRACT_BIN_NULL = RetCode.mark(201028, "contractbin is null");
    public static final RetCode CONTRACT_ADDRESS_NULL = RetCode.mark(201029, "contractAddress is null");
    public static final RetCode CONTRACT_ADDRESS_INVALID = RetCode.mark(201030, "contractAddress invalid");
    public static final RetCode PRIVATE_KEY_DECODE_FAIL = RetCode.mark(201031, "privateKey decode fail");
    public static final RetCode NO_CONFIG_KEY_SERVER = RetCode.mark(201032, "not found config of keyServer");
    public static final RetCode DATA_SIGN_ERROR = RetCode.mark(201033, "data request sign error");
    public static final RetCode GROUPID_NOT_EXIST = RetCode.mark(201034, "groupId not exist");
    public static final RetCode VERSION_AND_ADDRESS_CANNOT_ALL_BE_NULL = RetCode.mark(201035, "version and address cannot all be null");
    public static final RetCode CONTRACT_COMPILE_FAIL = RetCode.mark(201036, "compile fail");
    public static final RetCode USER_NAME_NULL = RetCode.mark(201037, "user name is null");
    public static final RetCode USER_NAME_EXISTS = RetCode.mark(201038, "user name already exists");
    public static final RetCode PRIVATEKEY_EXISTS = RetCode.mark(201039, "private key already exists");

    /* system error */
    public static final RetCode SYSTEM_ERROR = RetCode.mark(101001, "system error");
    public static final RetCode PARAM_VAILD_FAIL = RetCode.mark(101002, "param valid fail");

    /* precompiled success */
    public static final RetCode RET_SUCCESS = RetCode.mark(0, "success");
    public static final RetCode RET_SUCCESS_EMPTY_LIST = RetCode.mark(0,"Empty Set ");

    /* precompiled check */
    public static final String PARAM_FAIL_GROUPID_IS_EMPTY = "{\"code\":201101,\"message\":\"groupId cannot be empty\"}";
    public static final RetCode PARAM_FAIL_TABLE_NAME_IS_EMPTY = RetCode.mark(201102, "tableName cannot be empty");
    public static final String PARAM_FAIL_PERMISSION_TYPE_IS_EMPTY = "{\"code\":201103,\"message\":\"permissionType cannot be empty\"}";
    public static final RetCode PARAM_FAIL_PERMISSION_TYPE_NOT_EXIST =  RetCode.mark(201104, "permissionType not exists");
    public static final String PARAM_FAIL_FROM_IS_EMPTY = "{\"code\":201105,\"message\":\"from address cannot be empty\"}";
    public static final String PARAM_FAIL_CONTRACT_NAME_IS_EMPTY = "{\"code\":201106,\"message\":\"contract name cannot be empty\"}";
    public static final String PARAM_FAIL_CONFIG_KEY_IS_EMPTY = "{\"code\":201107,\"message\":\"system config key cannot be empty\"}";
    public static final String PARAM_FAIL_CONFIG_VALUE_IS_EMPTY = "{\"code\":201108,\"message\":\"system config value cannot be empty\"}";
    public static final String PARAM_FAIL_NODE_ID_IS_EMPTY = "{\"code\":2011109,\"message\":\"node id cannot be empty\"}";
    public static final String PARAM_FAIL_NODE_TYPE_IS_EMPTY = "{\"code\":201110,\"message\":\"node type cannot be empty\"}";
    public static final String PARAM_FAIL_PERMISSION_STATE_ALL_CONNOT_BE_EMPTY = "{\"code\":201111,\"message\":\"Permission state cannot be all empty\"}";
    /* precompiled runtime check or error */

    // param
    public static final RetCode PARAM_ERROR = RetCode.mark(201200,"params not fit");
    public static final RetCode PARAM_ADDRESS_IS_INVALID = RetCode.mark(201201, "address is invalid");
    // permission
    public static final RetCode PERMISSION_DENIED = RetCode.mark(201202, "permission denied, please check chain administrator permission");

    // sys config
    public static final RetCode SYSTEM_CONFIG_EXIST = RetCode.mark(201206, "create system config in db fail for already exist");
    public static final RetCode INVALID_SYSTEM_CONFIG_KEY = RetCode.mark(201207, "system config key is invalid");
    public static final RetCode UNSUPPORTED_SYSTEM_CONFIG_KEY = RetCode.mark(201208, "unsupported for this system config key");
    public static final RetCode FAIL_SET_SYSTEM_CONFIG_TOO_SMALL =  RetCode.mark(201209,
            "provide value by positive integer mode, from 100000 to 2147483647");
    public static final RetCode FAIL_SET_SYSTEM_CONFIG = RetCode.mark(201210, "set system config value fail for params error or permission denied ");
    public static final RetCode FAIL_QUERY_SYSTEM_CONFIG = RetCode.mark(201211, "query system config value list fail");
    // consensus (node manager)
    public static final RetCode INVALID_NODE_ID = RetCode.mark(201216,"node id is invalid");
    public static final RetCode INVALID_NODE_TYPE = RetCode.mark(201217,"invalid node type: sealer, observer, remove ");
    public static final RetCode FAIL_CHANGE_NODE_TYPE = RetCode.mark(201218,"set node consensus type fail, check permission or node's group config file");
    // cns
    public static final RetCode INVALID_VERSION = RetCode.mark(201221,"Contract version should only contains 'A-Z' or 'a-z' or '0-9' or dot mark ");
    public static final RetCode INVALID_VERSION_EXCEED_LENGTH = RetCode.mark(201222,"version of contract is out of length");

    // crud
    public static int CODE_CRUD_SQL_ERROR = -51503;
    public static final RetCode PARAM_FAIL_SQL_ERROR = RetCode.mark(201226, "sql syntax error");
    public static final RetCode SQL_ERROR = RetCode.mark(201227, "crud sql fail");
    public static final RetCode FAIL_TABLE_NOT_EXISTS = RetCode.mark(201228, "table not exists");
    public static final String CRUD_EMPTY_SET = "Empty Set.";

    // cert PEM_FORMAT_ERROR
    public static final RetCode CERT_FILE_NOT_FOUND = RetCode.mark(201231, "Cert file not found, please check cert path in config");
    public static final RetCode PEM_FORMAT_ERROR = RetCode.mark(201232, "Pem file format error, must surrounded by -----XXXXX PRIVATE KEY-----");
    public static final RetCode PEM_CONTENT_ERROR = RetCode.mark(201233, "Pem file content error");


}
