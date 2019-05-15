/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.base;

import java.math.BigInteger;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Contract Constant.
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = Constants.CONSTANT_PREFIX)
public class Constants {
    public static final BigInteger GAS_PRICE = new BigInteger("100000000");
    public static final BigInteger GAS_LIMIT = new BigInteger("100000000");
    public static final BigInteger INITIAL_WEI_VALUE = new BigInteger("0");

    public static final String CNS_CONTRAC_TNAME = "ContractAbiMgr";
    public static final String CNS_FUNCTION_ADDABI = "addAbi";
    public static final String CNS_FUNCTION_GETABI = "getAbi";

    public static final String SEP = "_";
    public static final String DIAGONAL = "/";
    public static final String SYMPOL = "@";
    public static final String TYPE_EVENT = "event";
    public static final String TYPE_FUNCTION = "function";
    public static final String TYPE_CONSTRUCTOR = "constructor";
    public static final String SHELL_COMMAND = "babel-node deploy.js %s %s %s %s";
    public static final String NODE_CONNECTION = "node@%s:%s";
    public static final String CONFIG_JSON = "/config.json";
    public static final String TOOL = "/tool/";
    public static final String OUTPUT = "output/";
    public static final String ABI_DIR = "./conf/files";
    public static final String FILE_ABI = ".abi";
    public static final String FILE_SOL = ".sol";
    public static final String FILE_ADDRESS = ".address";
    public static final String GET_PRIVATE_KEY = "/user/privateKey/%s";

    public static final String CONSTANT_PREFIX = "constant";
    private String nodeListenIp = "127.0.0.1";
    private String nodeDir = "/data/app/build/node0";
    private String mgrBaseUrl = "http://%s/webase-node-mgr";
    private String mgrIpPorts = "10.0.0.1:8082";
    private int transMaxWait = 30;
    private String monitorDisk = "/data";
}
