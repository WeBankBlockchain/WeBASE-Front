/*
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.front.base.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.webase.front.base.properties.Constants;
import com.webank.webase.front.util.CommonUtils;
import lombok.Data;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * initial info of node's configuration
 */
@Slf4j
@Data
@Configuration
public class NodeConfig implements InitializingBean {
    @JsonIgnore
    @Autowired
    private Constants constants;

    private String orgName;
    private String p2pip;
    private String listenip;
    private String rpcport;
    private String p2pport;
    private String channelPort;

    @Override
    public void afterPropertiesSet() throws Exception {
        if(constants.getNodeDir()=="") {
            return ;
        }
        List<String> nodeInfos = CommonUtils.readFileToList(constants.getNodeDir() + Constants.CONFIG_FILE);

        if (nodeInfos == null || nodeInfos.size() == 0) {
          //  throw new FrontException(ConstantCode.GET_NODE_CONFIG_FAILE);
            log.info("cannot read config.ini");
        }
        
        this.p2pip = CommonUtils.getCurrentIp();
        for (String str : nodeInfos) {
            if (str.contains("listen_ip")) {
                this.listenip = str.substring(str.indexOf("=")+1);
            }
            if (str.contains("jsonrpc_listen_port")) {
                this.rpcport = str.substring(str.indexOf("=")+1);
            }
            if (str.contains("listen_port")) {
                this.p2pport = str.substring(str.indexOf("=")+1);
            }
            if (str.contains("channel_listen_port")) {
                this.channelPort = str.substring(str.indexOf("=")+1);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"orgName\":\"").append(orgName).append('\"');
        sb.append(",\"p2pip\":\"").append(p2pip).append('\"');
        sb.append(",\"listenip\":\"").append(listenip).append('\"');
        sb.append(",\"rpcport\":\"").append(rpcport).append('\"');
        sb.append(",\"p2pport\":\"").append(p2pport).append('\"');
        sb.append(",\"channelPort\":\"").append(channelPort).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
