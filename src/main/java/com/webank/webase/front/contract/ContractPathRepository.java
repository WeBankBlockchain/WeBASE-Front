package com.webank.webase.front.contract;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import com.webank.webase.front.contract.entity.ContractPath;
import com.webank.webase.front.contract.entity.ContractPathKey;

public interface ContractPathRepository extends CrudRepository<ContractPath, ContractPathKey>,
    JpaSpecificationExecutor<ContractPath> {
}
