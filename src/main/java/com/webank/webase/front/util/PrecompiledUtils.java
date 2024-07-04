/*
 * Copyright 2014-2020 the original author or authors.
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

import com.webank.webase.front.base.exception.FrontException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

/**
 * Constants and tool function related with Precompiled module
 */
public class PrecompiledUtils {


    public static final String ContractLogFileName = "deploylog.txt";

    // SystemConfig key
    public static final String TxCountLimit = "tx_count_limit";
    public static final String TxGasLimit = "tx_gas_limit";
    public static final String AuthCheckStatus = "auth_check_status";
    // node consensus type
    public static final String NODE_TYPE_SEALER = "sealer";
    public static final String NODE_TYPE_OBSERVER = "observer";
    public static final String NODE_TYPE_REMOVE = "remove";

    // 修改共识节点权重
    public static final String NODE_TYPE_WEIGHT = "weight";

    public static final int TxGasLimitMin = 10000;
    public static final int TxGasLimitMax = 2147483647;
    public static final int AuthCheckStatus_Enable = 1;

    public static int SYS_TABLE_KEY_MAX_LENGTH = 58; // 64- "_user_".length
    public static int SYS_TABLE_KEY_FIELD_NAME_MAX_LENGTH = 64;
    public static int SYS_TABLE_VALUE_FIELD_MAX_LENGTH = 1024;
    public static int USER_TABLE_KEY_VALUE_MAX_LENGTH = 255;
    public static int USER_TABLE_FIELD_NAME_MAX_LENGTH = 64;
    public static int USER_TABLE_FIELD_VALUE_MAX_LENGTH = 16 * 1024 * 1024 - 1;

    public static boolean checkVersion(String version) {

        if (StringUtils.isBlank(version)) {
            return false;
        } else if (!version.matches("^[A-Za-z0-9.]+$")) { // check version's character
            return false;
        } else {
            return true;
        }
    }

    public static boolean checkNodeId(String nodeId) {
        if (nodeId.length() != 128) {
            return false;
        } else {
            return true;
        }
    }

    public static BigInteger processNonNegativeBigNumber(
            String name, String intStr, BigInteger minValue, BigInteger maxValue)
            throws FrontException {
        BigInteger intParam;
        try {
            intParam = new BigInteger(intStr);
            if (intParam.compareTo(minValue) < 0 || intParam.compareTo(maxValue) > 0) {
                throw new FrontException(
                        "Please provide \""
                                + name
                                + "\" by non-negative big int mode between "
                                + minValue
                                + " and "
                                + maxValue
                                + ".");
            }
        } catch (NumberFormatException e) {
            throw new FrontException("Invalid " + name + ": \"" + intStr + "\"!");
        }
        return intParam;
    }
}
