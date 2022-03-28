package com.webank.webase.front.event;

import com.webank.webase.front.event.entity.ContractEventInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author marsli
 */
public interface ContractEventInfoRepository extends CrudRepository<ContractEventInfo, String>,
        JpaSpecificationExecutor<ContractEventInfo> {

    List<ContractEventInfo> findByAppId(String appId);

    List<ContractEventInfo> findByGroupId(int groupId);

    List<ContractEventInfo> findByQueueName(String queueName);

    List<ContractEventInfo> findByGroupIdAndContractAddress(int groupId, String contractAddress);

    List<ContractEventInfo> findByExchangeNameAndRoutingKey(String exchangeName, String routingKey);

    List<ContractEventInfo> findByGroupId(int groupId, Pageable pageable);

    List<ContractEventInfo> findByGroupIdAndAppId(int groupId, String appId);

    /**
     * check unique by appId exchangeName queueName and contractAddress
     */
    @Query(value = "select c from ContractEventInfo c where c.appId = ?1 and c.exchangeName = ?2 "
        + "and c.queueName = ?3 and c.contractAddress = ?4")
    ContractEventInfo findContractEventInfo(
            String appId, String exchangeName, String queueName, String contractAddress);
}