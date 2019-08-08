/**
 * Copyright 2012-2019 the original author or authors.
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
package com.webank.webase.front.base;


/**
 * Code Constant.
 */
public interface ConstantCode {

    /* return success */
    RetCode RET_SUCCEED = RetCode.mark(0, "success");

    /* paramaters check */
    RetCode PARAM_FAIL_GROUP_ID_IS_EMPTY =  RetCode.mark(201001, "groupId cannot be empty");
    String PARAM_FAIL_USER_IS_EMPTY = "{\"code\":201002,\"msg\":\"user cannot be empty\"}";
    String PARAM_FAIL_USEAES_IS_EMPTY =
        "{\"code\":201003,\"msg\":\"useAes cannot be empty\"}";
    String PARAM_FAIL_VERSION_IS_EMPTY = "{\"code\":201004,\"msg\":\"version cannot be empty\"}";
    String PARAM_FAIL_FUNCNAME_IS_EMPTY = "{\"code\":201005,\"msg\":\"funcName cannot be empty\"}";
    String PARAM_FAIL_ABIINFO_IS_EMPTY = "{\"code\":201006,\"msg\":\"abiInfo cannot be empty\"}";
    String PARAM_FAIL_CONTRACTBIN_IS_EMPTY = "{\"code\":201007,\"msg\":\"contractBin cannot be empty\"}";

    /* general error */
    RetCode CONTRACT_DEPLOYED_ERROR = RetCode.mark(201008, "contract's current version has been deployed");
    RetCode CONTRACT_NOT_DEPLOY_ERROR = RetCode.mark(201009, "contract is not deployed");
    RetCode ABI_SAVE_ERROR = RetCode.mark(201010, "save abi error");
    RetCode IN_FUNCPARAM_ERROR = RetCode.mark(201011, "contract funcParam is error");
    RetCode BLOCK_NUMBER_ERROR = RetCode.mark(201012, "requst blockNumber is greater than latest");
    RetCode ABI_GET_ERROR = RetCode.mark(201013, "get abi error");
    RetCode CONTRACT_DEPLOY_ERROR = RetCode.mark(201014, "contract deploy error");
    RetCode PRIVATEKEY_IS_NULL = RetCode.mark(201015, "user's privateKey is null");
    RetCode FILE_IS_NOT_EXIST = RetCode.mark(201016, "file is not exist");
    RetCode GET_NODE_CONFIG_FAILE = RetCode.mark(201017, "failed to get node config");
    RetCode BLOCKNUMBER_AND_PBFTVIEW_UNCHANGED = RetCode.mark(201018, "blockNumber and pbftView unchanged");
    RetCode IN_FUNCTION_ERROR = RetCode.mark(201019, "request function is error");
    RetCode TRANSACTION_QUERY_FAILED = RetCode.mark(201020, "transaction query from chain failed");
    RetCode TRANSACTION_SEND_FAILED = RetCode.mark(201021, "transaction send to chain failed");
    RetCode NODE_REQUEST_FAILED = RetCode.mark(201022, "node request failed");
    RetCode CONTRACT_EXISTS = RetCode.mark(201023, "contract already exists");
    RetCode CONTRACT_NAME_REPEAT = RetCode.mark(201024, "contract name cannot be repeated");
    RetCode INVALID_CONTRACT_ID = RetCode.mark(201025, "invalid contract id");
    RetCode CONTRACT_HAS_BEAN_DEPLOYED = RetCode.mark(201026, "contract has been deployed");
    RetCode SEND_ABI_INFO_FAIL = RetCode.mark(201027, "send abiInfo fail");
    RetCode CONTRACT_BIN_NULL = RetCode.mark(201028, "contractbin is null");
    RetCode CONTRACT_ADDRESS_NULL = RetCode.mark(201029, "contractAddress is null");
    RetCode CONTRACT_ADDRESS_INVALID = RetCode.mark(201030, "contractAddress invalid");
    RetCode PRIVATE_KEY_DECODE_FAIL = RetCode.mark(201031, "privateKey decode fail");
    RetCode NO_CONFIG_KEY_SERVER = RetCode.mark(201032, "not found config of keyServer");
    RetCode DATA_SIGN_ERROR = RetCode.mark(201033, "data request sign error");
    RetCode GROUPID_NOT_EXIST = RetCode.mark(201034, "groupId not exist");
    RetCode VERSION_AND_ADDRESS_CANNOT_ALL_BE_NULL = RetCode.mark(201035, "version and address cannot all be null");
    RetCode CONTRACT_COMPILE_FAIL = RetCode.mark(201036, "compile fail");
    RetCode USER_NAME_NULL = RetCode.mark(201037, "user name is null");
    RetCode USER_NAME_EXISTS = RetCode.mark(201038, "user name already exists");
    RetCode PRIVATEKEY_EXISTS = RetCode.mark(201039, "private key already exists");

    /* system error */
    RetCode SYSTEM_ERROR = RetCode.mark(101001, "system error");
    RetCode PARAM_VAILD_FAIL = RetCode.mark(101002, "param valid fail");
}
