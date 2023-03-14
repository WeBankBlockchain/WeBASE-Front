/**
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.logparse;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.logparse.entity.CurrentState;
import com.webank.webase.front.logparse.entity.LogData;
import com.webank.webase.front.logparse.entity.NetWorkData;
import com.webank.webase.front.logparse.entity.TxGasData;
import com.webank.webase.front.logparse.repository.CurrentStateRepository;
import com.webank.webase.front.logparse.repository.NetWorkDataRepository;
import com.webank.webase.front.logparse.repository.TxGasDataRepository;
import com.webank.webase.front.logparse.util.FileUtil;
import com.webank.webase.front.logparse.util.LogParseUtil;
import com.webank.webase.front.logparse.util.LogTypes;
import com.webank.webase.front.util.CleanPathUtil;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


/**
 * LogParseService. 2020/3/26
 */
@Slf4j
@Service
public class LogParseService {

    @Autowired
    NetWorkDataRepository netWorkDataRepository;
    @Autowired
    TxGasDataRepository txGasDataRepository;
    @Autowired
    CurrentStateRepository currentStateRepository;
    @Autowired
    BcosSDK bcosSDK;
    @Autowired
    Constants constants;

    private static final String PATH_STAT = "/stat/";

    @Scheduled(fixedDelayString = "${constant.syncStatLogTime}")
    public void taskStart() {
        syncLogData();
    }

    public synchronized void syncLogData() {
        Instant startTime = Instant.now();
        log.debug("syncLogData start.", startTime.toEpochMilli());
        boolean checkLimit = checkCountLimit();
        if (!constants.isStatLogEnabled() || checkLimit) {
            log.debug("syncLogData enabled:{} checkLimit:{}", constants.isStatLogEnabled(),
                    checkLimit);
            return;
        }
        RandomAccessFile randomFile = null;
        try {
            String statPath = constants.getNodePath() + PATH_STAT;
            // get all files
            List<String> files = FileUtil.getFiles(statPath);
            if (files.isEmpty()) {
                log.warn("syncLogData. stat log files not exist, please check node's config.ini");
                return;
            }
            TreeMap<Long, String> treeMap = FileUtil.getStatFiles(files);
            // get currentState
            CurrentState currentState = getCurrentState();
            String currentFileName;
            long lastTimeFileSize = 0L;
            if (currentState == null) {
                currentFileName = treeMap.get(treeMap.firstKey());
            } else {
                currentFileName = currentState.getFileName();
                lastTimeFileSize = currentState.getCurrentSize();
            }
            // clear old files
            FileUtil.clearOldStatFiles(treeMap, currentFileName);
            // read current file
            File logFile = new File(CleanPathUtil.cleanString(statPath + currentFileName));
            long fileLength = logFile.length();
            if (fileLength < lastTimeFileSize) {
                return;
            } else {
                randomFile = new RandomAccessFile(logFile, "r");
                randomFile.seek(lastTimeFileSize);
                String tmp = null;
                while ((tmp = randomFile.readLine()) != null) {
                    try {
                        LogData logData = LogParseUtil.getLogData(tmp);
                        if (logData.getLogType() == LogTypes.NETWORK) {
                            NetWorkData netWorkData = LogParseUtil.parseNetworkLog(logData);
                            insertNetworkLog(netWorkData);
                        }
                        if (logData.getLogType() == LogTypes.TxGAS) {
                            TxGasData txGasData = LogParseUtil.parseTxGasUsedLog(logData);
                            insertTxGasUsedLog(txGasData);
                        }
                        lastTimeFileSize = randomFile.getFilePointer();
                        updateCurrentState(currentFileName, lastTimeFileSize);
                    } catch (Exception e) {
                        log.error("syncLogData readLine Exception.", e);
                        continue;
                    }
                }
                if (treeMap.size() > 1) {
                    FileUtil.clearCurrentStatFile(treeMap, currentFileName);
                    updateCurrentState(treeMap.get(treeMap.firstKey()), 0L);
                }
            }
            log.debug("syncLogData end useTime:{}",
                    Duration.between(startTime, Instant.now()).toMillis());
        } catch (IOException e) {
            log.error("syncLogData IOException.", e);
        } finally {
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e) {
                    log.error("syncLogData IOException.", e);
                }
            }
        }
    }

    public Page<NetWorkData> getNetWorkData(Integer groupId, Integer pageNumber, Integer pageSize,
            LocalDateTime beginDate, LocalDateTime endDate) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        Specification<NetWorkData> queryParam = new Specification<NetWorkData>() {
            @Override
            public Predicate toPredicate(Root<NetWorkData> root, CriteriaQuery<?> criteriaQuery,
                    CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("groupId"), groupId));
                if (beginDate != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"),
                            beginDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                }
                if (endDate != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"),
                            endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return netWorkDataRepository.findAll(queryParam, pageable);
    }

    public Page<TxGasData> getTxGasData(int groupId, Integer pageNumber, Integer pageSize,
            LocalDateTime beginDate, LocalDateTime endDate, String transHash) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        Specification<TxGasData> queryParam = new Specification<TxGasData>() {
            @Override
            public Predicate toPredicate(Root<TxGasData> root, CriteriaQuery<?> criteriaQuery,
                    CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("groupId"), groupId));
                if (beginDate != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"),
                            beginDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                }
                if (endDate != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"),
                            endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                }
                if (transHash != null) {
                    predicates.add(criteriaBuilder.equal(root.get("transHash"), transHash));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return txGasDataRepository.findAll(queryParam, pageable);
    }

    public int deleteData(int groupId, int type, LocalDateTime keepEndDate) {
        if (type == LogTypes.NETWORK.getValue()) {
            return netWorkDataRepository.deleteTimeAgo(groupId,
                    keepEndDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } else if (type == LogTypes.TxGAS.getValue()) {
            return txGasDataRepository.deleteTimeAgo(groupId,
                    keepEndDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } else {
            log.error("deleteData. type:{} not support", type);
            throw new FrontException(ConstantCode.INVALID_DATA_TYPE);
        }
    }

    private CurrentState getCurrentState() {
        CurrentState currentState = currentStateRepository.findById(1).orElse(null);
        return currentState;
    }

    private Boolean updateCurrentState(String name, Long currentSize) {
        CurrentState currentState = new CurrentState(1, name, currentSize);
        currentStateRepository.save(currentState);
        return true;
    }

    private Boolean checkCountLimit() {
        for (Integer groupId : bcosSDK.getGroupManagerService().getGroupList()) {
            long netWorkDataCount = netWorkDataRepository.count((Root<NetWorkData> root,
                    CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
                Predicate predicate = criteriaBuilder.equal(root.get("groupId"), groupId);
                return criteriaBuilder.and(predicate);
            });
            long txGasDataCount = txGasDataRepository.count((Root<TxGasData> root,
                    CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
                Predicate predicate = criteriaBuilder.equal(root.get("groupId"), groupId);
                return criteriaBuilder.and(predicate);
            });
            if (netWorkDataCount > constants.getSyncStatLogCountLimit()
                    || txGasDataCount > constants.getSyncStatLogCountLimit()) {
                return true;
            }
        }
        return false;
    }

    private Boolean insertNetworkLog(NetWorkData netWorkData) {
        netWorkDataRepository.save(netWorkData);
        return true;
    }

    private Boolean insertTxGasUsedLog(TxGasData txGasData) {
        txGasDataRepository.save(txGasData);
        return true;
    }
}

