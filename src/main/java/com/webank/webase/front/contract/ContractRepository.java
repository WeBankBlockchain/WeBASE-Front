package com.webank.webase.front.contract;

import com.webank.webase.front.performance.Performance;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ContractRepository   extends CrudRepository<Contract, Long> {
    List<Contract> findBycontractAddress(String contractAddress);
    List<Contract> findByContractNameAndVersion(String contractName , String version );

}
