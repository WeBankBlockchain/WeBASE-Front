/*
 * Copyright 2012-2019 the original author or authors.
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
package com.webank.webase.front.monitor;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface MonitorRepository extends CrudRepository<Monitor, Long> {

    @Query(value="select m from Monitor m where m.timestamp between ?1 and ?2 order by m.timestamp")
    public List<Monitor> findByTimeBetween(Long startTime, Long endTime);

    @Modifying
    @Transactional
    @Query(value="delete from Monitor m where m.timestamp< ?1",nativeQuery = true)
    public int deleteTimeAgo(Long time);
}
