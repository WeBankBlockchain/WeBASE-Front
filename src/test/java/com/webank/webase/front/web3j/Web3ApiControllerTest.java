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
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class Web3ApiControllerTest extends SpringTestBase {

    private MockMvc mockMvc;
    private String groupId = "1";

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

}