package com.webank.webase.front.contract;

import com.webank.webase.front.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface ContractRepository extends CrudRepository<Contract, Long>,
    JpaSpecificationExecutor<Contract> {

    Contract findByGroupIdAndContractPathAndContractName(int groupId, String contractPath,
        String contractName);

    Contract findByGroupIdAndId(int groupId, Long contractId);
}
