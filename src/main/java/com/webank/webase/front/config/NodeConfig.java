package com.webank.webase.front.config;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.util.CommonUtils;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

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
@Data
@Configuration
public class NodeConfig implements InitializingBean {
    @Autowired
    private Constants constants;
    private String orgName;
    private String p2pip;
    private String listenip;
    private String rpcport;
    private String p2pport;
    private String channelPort;
    private String datadir;

    @Override
    public void afterPropertiesSet() throws Exception {
        String nodeInfos = CommonUtils.readFile(constants.getNodeDir() + Constants.CONFIG_JSON);
        NodeConfig nodeConfig =
                CommonUtils.object2JavaBean(JSON.parseObject(nodeInfos), NodeConfig.class);
        String p2pip = CommonUtils.getCurrentIp();

        if (nodeConfig == null) {
            throw new FrontException(ConstantCode.GET_NODE_CONFIG_FAILE);
        }
        this.p2pip = p2pip;
        this.listenip = nodeConfig.getListenip();
        this.rpcport = nodeConfig.getRpcport();
        this.p2pport = nodeConfig.getP2pport();
        this.channelPort = nodeConfig.getChannelPort();
        this.datadir = nodeConfig.getDatadir();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append(",\"orgName\":\"").append(orgName).append('\"');
        sb.append(",\"p2pip\":\"").append(p2pip).append('\"');
        sb.append(",\"listenip\":\"").append(listenip).append('\"');
        sb.append(",\"rpcport\":\"").append(rpcport).append('\"');
        sb.append(",\"p2pport\":\"").append(p2pport).append('\"');
        sb.append(",\"channelPort\":\"").append(channelPort).append('\"');
        sb.append(",\"datadir\":\"").append(datadir).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
