package com.webank.webase.front.event;

import com.webank.webase.front.event.entity.EventRegisterInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author marsli
 */
public interface EventRegisterInfoRepository extends CrudRepository<EventRegisterInfo, Long>,
        JpaSpecificationExecutor<EventRegisterInfo> {

    EventRegisterInfo findById(Long id);

    List<EventRegisterInfo> findByAppId(String appId);

    List<EventRegisterInfo> findByGroupId(int groupId);

    List<EventRegisterInfo> findByQueueName(String queueName);

    List<EventRegisterInfo> findByGroupIdAndContractAddress(int groupId, String contractAddress);

    List<EventRegisterInfo> findByExchangeNameAndRoutingKey(String exchangeName, String routingKey);
}