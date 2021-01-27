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

import com.webank.webase.front.contract.entity.Cns;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CnsRepository extends CrudRepository<Cns, Long>,
    JpaSpecificationExecutor<Cns> {
    
    List<Cns> findByGroupIdAndContractName(int groupId, String contractName);
    
    @Query(nativeQuery=true, value = "select * from Cns n where n.group_id = ?1 and n.contract_address = ?2 order by n.modify_time desc limit 1")
    Cns findByAddressLimitOne(int groupId, String contractAddress);
}
