/*
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

package com.webank.webase.front.logparse.util;

import com.webank.webase.front.logparse.entity.LogData;
import com.webank.webase.front.logparse.entity.NetWorkData;
import com.webank.webase.front.logparse.entity.TxGasData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * LogParseUtil. format log
 */
@Slf4j
public class LogParseUtil {

    public static LogData getLogData(String logMsg) {
        String[] sArray = logMsg.split("\\|");
        if (sArray.length > 3) {
            Long timestamp = 0L;
            String[] sArray2 = sArray[1].split("\\.");
            if (sArray2.length > 0) {
                try {
                    Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sArray2[0]);
                    timestamp = parse.getTime();
                } catch (Exception e) {
                    log.error("getLogData fail.", e);
                }
            }

            if (sArray[2].equals("Total")) {
                return new LogData(timestamp, LogTypes.NETWORK, sArray[3]);
            } else if (sArray[2].equals("TxsGasUsed")) {
                return new LogData(timestamp, LogTypes.TxGAS, sArray[3]);
            }
        }
        return new LogData(0L, LogTypes.UNKNOWN, null);
    }

    public static HashMap<String, String> parseLogValue(String logMsg) {
        HashMap<String, String> logValue = new HashMap<>();
        String[] sArray = logMsg.split(",");
        for (String str : sArray) {
            String[] sValue = str.split("=");
            if (sValue.length >= 2) {
                logValue.put(sValue[0], sValue[1]);
            }
        }
        return logValue;
    }

    public static NetWorkData parseNetworkLog(LogData logData) {
        HashMap<String, String> logValue = parseLogValue(logData.getLogData());
        NetWorkData netWorkData = new NetWorkData();
        if (logValue.containsKey("Total_In")) {
            String str = logValue.get("Total_In");
            Long total_in = Long.parseLong(str);
            netWorkData.setTotalIn(total_in);
        }
        if (logValue.containsKey("g")) {
            netWorkData.setGroupId(Integer.parseInt(logValue.get("g")));
        }
        if (logValue.containsKey("Total_Out")) {
            String str = logValue.get("Total_Out");
            Long total_out = Long.parseLong(str);
            netWorkData.setTotalOut(total_out);
        }
        netWorkData.setTimestamp(logData.getTimestamp());
        return netWorkData;
    }

    public static TxGasData parseTxGasUsedLog(LogData logData) {
        HashMap<String, String> logValue = parseLogValue(logData.getLogData());
        TxGasData txGasData = new TxGasData();
        if (logValue.containsKey("gasUsed")) {
            txGasData.setGasUsed(Long.parseLong(logValue.get("gasUsed")));
        }
        if (logValue.containsKey("g")) {
            txGasData.setGroupId(Integer.parseInt(logValue.get("g")));
        }
        if (logValue.containsKey("txHash")) {
            txGasData.setTransHash("0x" + logValue.get("txHash"));
        }
        txGasData.setTimestamp(logData.getTimestamp());
        return txGasData;
    }
}
