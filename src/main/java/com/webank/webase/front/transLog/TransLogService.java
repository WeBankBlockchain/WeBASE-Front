package com.webank.webase.front.transLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransLogService {

    @Autowired
    TransLogRepository transLogRepository;

    public void save(TransLog transLog) {
    transLogRepository.save(transLog);
    }
}
