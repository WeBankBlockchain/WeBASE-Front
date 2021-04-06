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
import org.fisco.bcos.sdk.contract.precompiled.cns.CNSPrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.consensus.ConsensusPrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.contractmgr.ContractLifeCyclePrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.crud.CRUD;
import org.fisco.bcos.sdk.contract.precompiled.crud.table.TableFactory;
import org.fisco.bcos.sdk.contract.precompiled.model.PrecompiledAddress;
import org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernancePrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionPrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.sysconfig.SystemConfigPrecompiled;

/**
 * precompiled contract address, function name, abi
 * @link PrecompiledWithSign.java
 * @author marsli
 */
public class PrecompiledCommonInfo {

    public static String getAddress(PrecompiledTypes types) {
        switch (types) {
            case SYSTEM_CONFIG:
                return PrecompiledAddress.SYSCONFIG_PRECOMPILED_ADDRESS;
            case TABLE_FACTORY:
                return PrecompiledAddress.TABLEFACTORY_PRECOMPILED_ADDRESS;
            case CRUD:
                return PrecompiledAddress.CRUD_PRECOMPILED_ADDRESS;
            case CONSENSUS:
                return PrecompiledAddress.CONSENSUS_PRECOMPILED_ADDRESS;
            case CNS:
                return PrecompiledAddress.CNS_PRECOMPILED_ADDRESS;
            case PERMISSION:
                return PrecompiledAddress.PERMISSION_PRECOMPILED_ADDRESS;
            case CSM:
                return PrecompiledAddress.CONTRACT_LIFECYCLE_PRECOMPILED_ADDRESS;
            case CHAIN_GOVERN:
                return PrecompiledAddress.CHAINGOVERNANCE_PRECOMPILED_ADDRESS;
            default:
                return "";
        }
    }

    public static String getAbi(PrecompiledTypes types) {
        switch (types) {
            case SYSTEM_CONFIG:
                return SystemConfigPrecompiled.ABI;
            case TABLE_FACTORY:
                return TableFactory.ABI;
            case CRUD:
                return CRUD.ABI;
            case CONSENSUS:
                return ConsensusPrecompiled.ABI;
            case CNS:
                return CNSPrecompiled.ABI;
            case PERMISSION:
                return PermissionPrecompiled.ABI;
            case CSM:
                return ContractLifeCyclePrecompiled.ABI;
            case CHAIN_GOVERN:
                return ChainGovernancePrecompiled.ABI;
            default:
                return "";
        }
    }

}
