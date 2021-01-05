/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.abi;

import com.webank.webase.front.Application;
import com.webank.webase.front.abi.entity.ReqImportAbi;
import com.webank.webase.front.util.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class AbiControllerTest {
    private MockMvc mockMvc;
    private Integer groupId = 1;
    private Integer pageNumber = 1;
    private Integer pageSize = 5;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testInserAbi() throws Exception {
        // test insert
        ReqImportAbi abiInsert = new ReqImportAbi();
        abiInsert.setGroupId(1);
        abiInsert.setContractAddress("0xd8e1e0834b38081982f4a080aeae350a6d422915");
        abiInsert.setContractName("Hello");
        String abiStr = "[{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_ua\",\"type\":\"uint256[]\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
        abiInsert.setContractAbi(JsonUtils.toJavaObjectList(abiStr, Object.class));

        // post action
        ResultActions resultActions = mockMvc.perform(
            MockMvcRequestBuilders.post("/abi")
                .content(JsonUtils.toJSONString(abiInsert))
                .contentType(MediaType.APPLICATION_JSON)
        );
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out
                .println("response:" + resultActions.andReturn().getResponse().getContentAsString());

    }

    @Test
    public void testGetAbiById() throws Exception {
        Integer abiId = 1;
        // post action
        String url = String.format("/abi/%1s", abiId);
        ResultActions resultActions = mockMvc.perform(
            MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
        );
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out
            .println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetAbi() throws Exception {
        // post action
        String url = String.format("/abi/list/%1s/%2s/%3s", groupId, pageNumber, pageSize);
        ResultActions resultActions = mockMvc.perform(
            MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
        );
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out
            .println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testUpdateAbi() throws Exception {
        // test insert
        ReqImportAbi abiUpdate = new ReqImportAbi();
        // abi id needed in update
        abiUpdate.setAbiId(1L);
        abiUpdate.setGroupId(groupId);
        abiUpdate.setContractAddress("0xd8e1e0834b38081982f4a080aeae350a6d422915");
        abiUpdate.setContractName("Hello_222");
        String abiStr = "[{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_ua\",\"type\":\"uint256[]\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
        abiUpdate.setContractAbi(JsonUtils.toJavaObjectList(abiStr, Object.class));

        // post action
        ResultActions resultActions = mockMvc.perform(
            MockMvcRequestBuilders.put("/abi")
                .content(JsonUtils.toJSONString(abiUpdate))
                .contentType(MediaType.APPLICATION_JSON)
        );
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out
            .println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }
}
