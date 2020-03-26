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
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.util.*;

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
                    log.error("getBlockNumber fail.", e);
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
            txGasData.setTransHash(logValue.get("txHash"));
        }
        txGasData.setTimestamp(logData.getTimestamp());
        return txGasData;
    }

    public static void main(String args[]) {
        String logMsg =
                "info|2020-03-23 09:01:53.699992|Total|,g=11,Total_In=73153,Total_Out=32322";
        LogData logData = getLogData(logMsg);
        System.out.println("___________");
        System.out.println(
                logData.getLogData() + " " + logData.getLogType() + " " + logData.getTimestamp());
        if (logData.getLogType() == LogTypes.NETWORK) {
            NetWorkData netWorkData = parseNetworkLog(logData);
            System.out.println(netWorkData);
        }

        logMsg = "info|2020-03-23 09:10:23.078979|TxsGasUsed|,g=2,txHash=f8c2bce773e58591a30bb03dbdf0789a3037e5b76a6118d0c7d02562b617048f,gasUsed=22744";
        logData = getLogData(logMsg);
        System.out.println("___________");
        System.out.println(
                logData.getLogData() + " " + logData.getLogType() + " " + logData.getTimestamp());
        if (logData.getLogType() == LogTypes.TxGAS) {
            TxGasData t = parseTxGasUsedLog(logData);
            System.out.println(t);
        }
    }
}
