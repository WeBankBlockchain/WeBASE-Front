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
package com.webank.webase.front.contract;

import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.webank.webase.front.contract.entity.Contract;

public interface ContractRepository extends CrudRepository<Contract, Long>,
    JpaSpecificationExecutor<Contract> {

    Contract findByGroupIdAndContractPathAndContractName(int groupId, String contractPath,
        String contractName);
    
    List<Contract> findByGroupIdAndContractPath(int groupId, String contractPath);

    Contract findByGroupIdAndId(int groupId, Long contractId);

    Contract findByGroupIdAndContractAddress(int groupId, String contractAddress);

    List<Contract> findByGroupIdAndContractStatus(int groupId, int contractStatus);
}
