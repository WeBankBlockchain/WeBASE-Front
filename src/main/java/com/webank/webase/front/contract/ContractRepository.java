package com.webank.webase.front.contract;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ContractRepository   extends CrudRepository<Contract, Long> {
    List<Contract> findByGroupIdAndContractAddress(int groupId,String contractAddress);
    List<Contract> findByGroupIdAndContractNameAndVersion(int groupId, String contractName , String version );

}
