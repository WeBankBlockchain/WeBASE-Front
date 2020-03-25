package com.webank.webase.front.event;

import com.webank.webase.front.event.entity.ContractEventInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author marsli
 */
public interface ContractEventInfoRepository extends CrudRepository<ContractEventInfo, String>,
        JpaSpecificationExecutor<ContractEventInfo> {

    ContractEventInfo findById(String id);

    List<ContractEventInfo> findByAppId(String appId);

    List<ContractEventInfo> findByGroupId(int groupId);

    List<ContractEventInfo> findByQueueName(String queueName);

    List<ContractEventInfo> findByGroupIdAndContractAddress(int groupId, String contractAddress);

    List<ContractEventInfo> findByExchangeNameAndRoutingKey(String exchangeName, String routingKey);

	List<ContractEventInfo> findByGroupId(int groupId, Pageable pageable);

	List<ContractEventInfo> findByGroupIdAndAppId(int groupId, String appId);

}