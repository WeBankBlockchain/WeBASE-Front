/*
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
package com.webank.webase.front.logparse.repository;

import com.webank.webase.front.logparse.entity.TxGasData;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TxGasDataRepository
        extends CrudRepository<TxGasData, Long>, JpaSpecificationExecutor<TxGasData> {

    @Modifying
    @Transactional
    @Query(value = "delete from t_tx_gas_data t where t.group_id = ?1 and t.timestamp < ?2",
            nativeQuery = true)
    public int deleteTimeAgo(int groupId, Long time);
}
