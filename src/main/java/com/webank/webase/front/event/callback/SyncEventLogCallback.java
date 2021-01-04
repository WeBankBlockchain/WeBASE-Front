/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.event.callback;

import com.webank.webase.front.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.fisco.bcos.channel.event.filter.EventLogPushWithDecodeCallback;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.tx.txdecode.BaseException;
import org.fisco.bcos.web3j.tx.txdecode.LogResult;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * use CompleteFuture to get all callback of event
 */
public class SyncEventLogCallback extends EventLogPushWithDecodeCallback {

    private static final Logger logger =
        LoggerFactory.getLogger(SyncEventLogCallback.class);
    private CompletableFuture<List<LogResult>> future;
    private List<LogResult> finalList;

    public SyncEventLogCallback(TransactionDecoder decoder,
        final CompletableFuture<List<LogResult>> future) {

        this.future = future;
        this.finalList = new ArrayList<>();
        // onPush will call father class's decoder, init EventLogPushWithDecodeCallback's decoder
        this.setDecoder(decoder);
    }

    /**
     * 根据Log对象中的blockNumber，transactionIndex，logIndex进行去重
     * @param status
     * @param logs
     */
    @Override
    public void onPushEventLog(int status, List<LogResult> logs) {
        logger.info(
            "SyncEventLogCallback onPushEventLog params: {}, status: {}, logs: {}",
            getFilter().getParams(), status, logs);
        // status == 0 push not finish,
        if (status == 0) {
            // add in resultList
            if (logs != null) {
                finalList.addAll(logs);
            }
        } else if (status == 1){
            if (logs != null) {
                finalList.addAll(logs);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.error("sleep 100ms interrupted:{}", JsonUtils.objToString(e.getStackTrace()));
            }
            logger.info(
                "SyncEventLogCallback push finished status: {}, finalList size:{}",
                status, finalList.size());
            future.complete(finalList);
        } else {
            // not 0, not 1, error
            logger.error("SyncEventLogCallback onPushEventLog error!");
            future.complete(finalList);
        }
    }

    @Override
    public LogResult transferLogToLogResult(Log log) {
        try {
            LogResult logResult = getDecoder().decodeEventLogReturnObject(log);
            return logResult;
        } catch (BaseException e) {
            logger.error(" event log decode failed, log: {}", log);
            return null;
        }
    }

}
