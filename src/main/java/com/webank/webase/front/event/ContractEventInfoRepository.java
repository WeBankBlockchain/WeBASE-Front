package com.webank.webase.front.event;

import com.webank.webase.front.event.entity.ContractEventInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author marsli
 */
public interface ContractEventInfoRepository extends CrudRepository<ContractEventInfo, Long>,
        JpaSpecificationExecutor<ContractEventInfo> {

    ContractEventInfo findById(Long id);

    List<ContractEventInfo> findByAppId(String appId);

    List<ContractEventInfo> findByGroupId(int groupId);

    List<ContractEventInfo> findByQueueName(String queueName);

    List<ContractEventInfo> findByGroupIdAndContractAddress(int groupId, String contractAddress);

    List<ContractEventInfo> findByExchangeNameAndRoutingKey(String exchangeName, String routingKey);

    /**
     * check unique by appId exchangeName queueName and contractAddress
     */
    ContractEventInfo findByAppIdAndExchangeNameAndQueueNameAndContractAddress(
            String appId, String exchangeName, String queueName, String contractAddress);
}