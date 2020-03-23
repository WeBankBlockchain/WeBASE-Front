/**
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.front.keystore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.webank.webase.front.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class KeyStoreControllerTest {

    private MockMvc mockMvc;
    private Integer groupId = 1;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void testGetPrivateKey() throws Exception {
        //{"publicKey":"0x1a532eba008787e5d16584eb40a0e3ddf1d8fa33fcef64c14d58aabbb1b191935ce540cdd4d3cbbe4779f558185cf4cea1be266cc46ee9b3903f1a819662c1b7","privateKey":"95d1ff9e42c70bf460490404a9b35a6618e711a51c0ec829d7401667cd6ad1d5","address":"0x58415e44c664af1eab071da728a8405afa1991f3"}
        String uri = "/privateKey?" + "userName=";
        String username = "test2";
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(uri+username));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out
            .println("==============================================================response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

}