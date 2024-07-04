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

import com.webank.webase.front.event.entity.DecodedEventLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.fisco.bcos.sdk.v3.codec.ContractCodec;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.eventsub.EventSubCallback;
import org.fisco.bcos.sdk.v3.model.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * use CompleteFuture to get all callback of event
 */
public class SyncEventLogCallback implements EventSubCallback {

    private static final Logger logger =
        LoggerFactory.getLogger(SyncEventLogCallback.class);

    private ContractCodec abiCodec;
    private String contractAbi;
    private String eventName;

    // private CompletableFuture<List<EventLog>> future;
    private CompletableFuture<List<DecodedEventLog>> future;
    // private List<EventLog> finalList;
    private List<DecodedEventLog> finalList;

    public SyncEventLogCallback(ContractCodec abiCodec, String contractAbi, String eventName,
        final CompletableFuture<List<DecodedEventLog>> future) {
        this.abiCodec = abiCodec;
        this.contractAbi = contractAbi;
        this.eventName = eventName;
        this.future = future;
        this.finalList = new ArrayList<>();
    }

    /**
     * onReceiveLog called when sdk receive any response of the target subscription. logs will be
     * parsed by the user through the ABI module.
     *
     * @param status the status that peer response to sdk.
     * @param logs   logs from the message.
     */
    @Override
    public void onReceiveLog(String eventSubId, int status, List<EventLog> logs) {
        logger.info(
            "SyncEventLogCallback onPushEventLog eventSubId:{}, status: {}, logs: {}", eventSubId, status, logs);
        // status == 0 push not finish,
        if (status == 0) {
            // add in resultList
            if (logs != null) {
                List<DecodedEventLog> decodedList = this.decodeEvent(logs);
                finalList.addAll(decodedList);
            }
        } else if (status == 1){
            if (logs != null) {
                List<DecodedEventLog> decodedList = this.decodeEvent(logs);
                finalList.addAll(decodedList);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("sleep 100ms interrupted:[]", e);
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

    /**
     * 根据Log对象中的blockNumber，transactionIndex，logIndex进行去重
     */

    private List<DecodedEventLog> decodeEvent(List<EventLog> logs) {
        List<DecodedEventLog> decodedLogList = new ArrayList<>();
        for (EventLog log : logs) {
            logger.debug(
                " blockNumber:" + log.getBlockNumber()
                    + ",txIndex:" + log.getTransactionIndex()
                    + " data:" + log.getData());
            try {
                List<String> list = abiCodec.decodeEventToString(contractAbi, eventName, log);
                DecodedEventLog decodedEventLog = new DecodedEventLog(log, list);
                logger.debug("decode event of :{}, log content:{} ", eventName, list);
                decodedLogList.add(decodedEventLog);
            } catch (ContractCodecException e) {
                logger.error("decode event log error:{} ", e.getMessage());
            }
        }
        return decodedLogList;
    }

}
