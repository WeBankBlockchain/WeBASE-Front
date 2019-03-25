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
