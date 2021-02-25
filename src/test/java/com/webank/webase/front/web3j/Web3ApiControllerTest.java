/**
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.front.web3j;

import com.webank.webase.front.base.SpringTestBase;
import com.webank.webase.front.util.JsonUtils;
import com.webank.webase.front.web3api.entity.GenerateGroupInfo;
import com.webank.webase.front.web3api.entity.ReqGroupStatus;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class Web3ApiControllerTest extends SpringTestBase {

    private MockMvc mockMvc;
    private Integer groupId = 1;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void testGetNodeStatusList() throws Exception {

        ResultActions resultActions = mockMvc
            .perform(MockMvcRequestBuilders.post("/1/web3/getNodeStatusList"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out
            .println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGenenrateGroup() {

    }

    @Test
    public void testGenerateSingle() throws Exception {
        List<String> nodeList = new ArrayList<>();
        String targetNodeId = "dd7a2964007d583b719412d86dab9dcf773c61bccab18cb646cd480973de0827cc94fa84f33982285701c8b7a7f465a69e980126a77e8353981049831b550f5c";
        nodeList.add(targetNodeId);
        int newGroupId = 2023;
        GenerateGroupInfo param = new GenerateGroupInfo();
        param.setGenerateGroupId(newGroupId);
        param.setTimestamp(BigInteger.valueOf(new Date().getTime()));
        param.setNodeList(nodeList);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/1/web3/generateGroup").
                content(JsonUtils.toJSONString(param)).
                contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testOperate() throws Exception {
        int newGroupId = 2020;
        String type = "getStatus";
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/" + newGroupId +"/web3/operate/" + type).
                contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGroupStatusList() throws Exception {
        ReqGroupStatus param = new ReqGroupStatus();
        List<Integer> groupIdList = new ArrayList<>();
        groupIdList.add(2020);
        groupIdList.add(3);
        groupIdList.add(1);
        groupIdList.add(2021);
        groupIdList.add(2023);
        param.setGroupIdList(groupIdList);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/3/web3/queryGroupStatus")
                .content(JsonUtils.toJSONString(param))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
}