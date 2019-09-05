package com.webank.webase.front.contract;

import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import com.webank.webase.front.contract.entity.Contract;

public interface ContractRepository extends CrudRepository<Contract, Long>,
    JpaSpecificationExecutor<Contract> {

    Contract findByGroupIdAndContractPathAndContractName(int groupId, String contractPath,
        String contractName);
    
    List<Contract> findByGroupIdAndContractPath(int groupId, String contractPath);

    Contract findByGroupIdAndId(int groupId, Long contractId);
}
