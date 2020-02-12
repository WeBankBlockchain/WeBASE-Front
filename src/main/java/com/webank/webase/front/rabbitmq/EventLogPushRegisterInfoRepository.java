package com.webank.webase.front.rabbitmq;

import com.webank.webase.front.rabbitmq.entity.EventLogPushRegisterInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author marsli
 */
public interface EventLogPushRegisterInfoRepository extends CrudRepository<EventLogPushRegisterInfo, Long>,
        JpaSpecificationExecutor<EventLogPushRegisterInfo> {

    EventLogPushRegisterInfo findById(Long id);
    
    List<EventLogPushRegisterInfo> findByGroupId(int groupId);

    List<EventLogPushRegisterInfo> findByGroupIdAndContractAddress(int groupId, String contractAddress);

    List<EventLogPushRegisterInfo> findByExchangeNameAndRoutingKey(String exchangeName, String routingKey);
}