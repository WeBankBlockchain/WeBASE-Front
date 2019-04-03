package com.webank.webase.front.transLog;

import com.webank.webase.front.contract.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransLogService {

    @Autowired
    TransLogRepository transLogRepository;

    public void save(TransLog transLog) {
    transLogRepository.save(transLog);
    }

    public List<TransLog> findByCriteria(int groupId, String contractName, String version, String contractAddress) {
        if (contractAddress != null) {
            return transLogRepository.findBycontractByGroupIdAndAddress(groupId, contractAddress);
        }
            return transLogRepository.findByGroupIdAndContractNameAndVersion(groupId, contractName, version);
    }
}
