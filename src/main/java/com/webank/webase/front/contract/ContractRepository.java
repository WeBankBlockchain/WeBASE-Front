package com.webank.webase.front.contract;

import com.webank.webase.front.performance.Performance;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ContractRepository   extends CrudRepository<Contract, Long> {
    List<Contract> findBycontractByGroupIdAndAddress(int groupId,String contractAddress);
    List<Contract> findByGroupIdAndContractNameAndVersion(int groupId, String contractName , String version );

}
