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

package com.webank.webase.front.precompiledapi;


import com.webank.webase.front.base.enums.PrecompiledTypes;
import org.fisco.bcos.web3j.precompile.csm.ContractLifeCyclePrecompiled;
import org.fisco.bcos.web3j.precompile.permission.ChainGovernance;
import org.fisco.bcos.web3j.precompile.permission.Permission;

/**
 * precompiled contract address, function name, abi
 * @link PrecompiledWithSign.java
 * @author marsli
 */
public class PrecompiledCommonInfo {
    /**
     * System config: setValueByKey
     */
    private static final String SystemConfigPrecompileAddress =
            "0x0000000000000000000000000000000000001000";
    private static final String ABI_SYSTEM_CONFIG = "[{\"constant\":false,\"inputs\":[{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"string\"}],\"name\":\"setValueByKey\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";

    /**
     * Table Factory : createTable
     * CRUD: update, select, insert, remove
     */
    private static final String TableFactoryPrecompileAddress =
            "0x0000000000000000000000000000000000001001";
    private static final String CRUDPrecompileAddress =
            "0x0000000000000000000000000000000000001002";
    private static final String ABI_TABLE_FACTORY = "[{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"valueField\",\"type\":\"string\"}],\"name\":\"createTable\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    private static final String ABI_CRUD = "[{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"entry\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"update\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"select\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"entry\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";

    /**
     * Consensus: addObserver, remove, addSealer
     */
    private static final String ConsensusPrecompileAddress =
            "0x0000000000000000000000000000000000001003";
    private static final String ABI_CONSENSUS = "[{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"addObserver\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"addSealer\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    /**
     * CNS: selectByName, selectByNameAndVersion, insert
      */
    private static final String CNSAddress =
            "0x0000000000000000000000000000000000001004";
    private static final String ABI_CNS = "[{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"}],\"name\":\"selectByName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"}],\"name\":\"selectByNameAndVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"},{\"name\":\"abi\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";

    /**
     * Permission: insert, queryByName, remove
     */
    private static String PermissionPrecompileAddress =
            "0x0000000000000000000000000000000000001005";
    private static final String ABI_PERMISSION = "[{\"constant\":false,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"}],\"name\":\"queryByName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    
    /**
     * CSM: freeze, unfreeze, grantManager, getStatus, listManager
     */
    private static final String CSMAddress =
            "0x0000000000000000000000000000000000001007";
    private static final String ABI_CSM = "[{\"constant\":true,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"getStatus\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"},{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"unfreeze\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"freeze\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"contractAddr\",\"type\":\"address\"},{\"name\":\"userAddr\",\"type\":\"address\"}],\"name\":\"grantManager\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"listManager\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"},{\"name\":\"\",\"type\":\"address[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";
    /**
     * Chain governance: listOperators, updateCommitteeMemberWeight, queryThreshold, queryCommitteeMemberWeight, grantCommitteeMember, unfreezeAccount, listCommitteeMembers, updateThreshold, revokeCommitteeMember, grantOperator, freezeAccount, revokeOperator, getAccountStatus
     */
    private static final String ChainGovernAddress = "0x0000000000000000000000000000000000001008";

    public static String getAddress(PrecompiledTypes types) {
        switch (types) {
            case SYSTEM_CONFIG:
                return SystemConfigPrecompileAddress;
            case TABLE_FACTORY:
                return TableFactoryPrecompileAddress;
            case CRUD:
                return CRUDPrecompileAddress;
            case CONSENSUS:
                return ConsensusPrecompileAddress;
            case CNS:
                return CNSAddress;
            case PERMISSION:
                return PermissionPrecompileAddress;
            case CSM:
                return CSMAddress;
            case CHAIN_GOVERN:
                return ChainGovernAddress;
            default:
                return "";
        }
    }

    public static String getAbi(PrecompiledTypes types) {
        switch (types) {
            case SYSTEM_CONFIG:
                return ABI_SYSTEM_CONFIG;
            case TABLE_FACTORY:
                return ABI_TABLE_FACTORY;
            case CRUD:
                return ABI_CRUD;
            case CONSENSUS:
                return ABI_CONSENSUS;
            case CNS:
                return ABI_CNS;
            case PERMISSION:
                return Permission.ABI;
            case CSM:
                return ContractLifeCyclePrecompiled.ABI;
            case CHAIN_GOVERN:
                return ChainGovernance.ABI;
            default:
                return "";
        }
    }

}
