package com.webank.webase.front.event;

import com.webank.webase.front.event.entity.EventLogPushInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author marsli
 */
public interface EventLogPushInfoRepository extends CrudRepository<EventLogPushInfo, Long>,
        JpaSpecificationExecutor<EventLogPushInfo> {

    EventLogPushInfo findById(Long id);

    List<EventLogPushInfo> findByAppId(String appId);

    List<EventLogPushInfo> findByGroupId(int groupId);

    List<EventLogPushInfo> findByQueueName(String queueName);

    List<EventLogPushInfo> findByGroupIdAndContractAddress(int groupId, String contractAddress);

    List<EventLogPushInfo> findByExchangeNameAndRoutingKey(String exchangeName, String routingKey);

}