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

package com.webank.webase.front.precntauth.precompiled.base;


import com.webank.webase.front.base.enums.PrecompiledTypes;
import org.fisco.bcos.sdk.v3.contract.auth.contracts.CommitteeManager;
import org.fisco.bcos.sdk.v3.contract.auth.contracts.ContractAuthPrecompiled;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled;
import org.fisco.bcos.sdk.v3.contract.precompiled.consensus.ConsensusPrecompiled;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.KVTablePrecompiled;
import org.fisco.bcos.sdk.v3.contract.precompiled.model.PrecompiledAddress;
import org.fisco.bcos.sdk.v3.contract.precompiled.sysconfig.SystemConfigPrecompiled;

/**
 * precompiled contract address, function name, abi
 * @link PrecompiledWithSign.java
 * @author marsli
 */
public class PrecompiledCommonInfo {


    public static String getAddress(PrecompiledTypes types) {
        switch (types) {
            case SYSTEM_CONFIG:
                return PrecompiledAddress.SYS_CONFIG_PRECOMPILED_ADDRESS;
            case SYSTEM_CONFIG_LIQUID:
                return PrecompiledAddress.SYS_CONFIG_PRECOMPILED_NAME;
//            case CRUD:
//                return PrecompiledAddress.KV_TABLE_PRECOMPILED_ADDRESS;
//            case CRUD_LIQUID:
//                return PrecompiledAddress.KV_TABLE_PRECOMPILED_NAME;
            case CONSENSUS:
                return PrecompiledAddress.CONSENSUS_PRECOMPILED_ADDRESS;
            case CONSENSUS_LIQUID:
                return PrecompiledAddress.CONSENSUS_PRECOMPILED_NAME;
            case BFS:
                return PrecompiledAddress.BFS_PRECOMPILED_ADDRESS;
            case BFS_LIQUID:
                return PrecompiledAddress.BFS_PRECOMPILED_NAME;
            case COMMITTEE_MANAGER:
                return PrecompiledAddress.COMMITTEE_MANAGER_ADDRESS;
            case CONTRACT_AUTH:
                return PrecompiledAddress.CONTRACT_AUTH_ADDRESS;
            default:
                return "";
        }
    }

    public static String getAbi(PrecompiledTypes types) {
        switch (types) {
            case SYSTEM_CONFIG:
                return SystemConfigPrecompiled.ABI;
            case CRUD:
                return KVTablePrecompiled.ABI;
            case CONSENSUS:
                return ConsensusPrecompiled.ABI;
            case CONTRACT_AUTH:
                return ContractAuthPrecompiled.ABI;
            case BFS:
                return BFSPrecompiled.ABI;
            case COMMITTEE_MANAGER:
                return CommitteeManager.ABI;
            default:
                return "";
        }
    }

}
