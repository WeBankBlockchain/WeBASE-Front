package com.webank.webase.front.transLog;

import com.webank.webase.front.contract.Contract;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransLogRepository  extends CrudRepository<TransLog, Long> {
    List<TransLog> findBycontractByGroupIdAndAddress(int groupId, String contractAddress);
    List<TransLog> findByGroupIdAndContractNameAndVersion(int groupId, String contractName , String version );


}
