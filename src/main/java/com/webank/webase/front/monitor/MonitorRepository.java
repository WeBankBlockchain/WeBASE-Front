/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.monitor;

import com.webank.webase.front.monitor.entity.Monitor;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface MonitorRepository
        extends CrudRepository<Monitor, Long>, JpaSpecificationExecutor<Monitor> {

    @Query(value = "select m from Monitor m where m.groupId = ?1 and m.timestamp between ?2 and ?3 order by m.id")
    public List<Monitor> findByTimeBetween(int groupId, Long startTime, Long endTime);

    @Modifying
    @Transactional
    @Query(value = "delete from Monitor m where m.timestamp< ?1", nativeQuery = true)
    public int deleteTimeAgo(Long time);
}
