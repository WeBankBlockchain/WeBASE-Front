/**
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
package com.webank.webase.front.logparse;

import com.webank.webase.front.base.SpringTestBase;
import com.webank.webase.front.logparse.entity.CurrentState;
import com.webank.webase.front.logparse.entity.NetWorkData;
import com.webank.webase.front.logparse.entity.TxGasData;
import com.webank.webase.front.logparse.repository.CurrentStateRepository;
import com.webank.webase.front.logparse.repository.NetWorkDataRepository;
import com.webank.webase.front.logparse.repository.TxGasDataRepository;
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

/**
 * test chain controller
 */
public class LogParseControllerTest extends SpringTestBase {
    private MockMvc mockMvc;
    private Integer groupId = 1;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    NetWorkDataRepository netWorkDataRepository;
    @Autowired
    TxGasDataRepository txGasDataRepository;
    @Autowired
    CurrentStateRepository currentStateRepository;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testSave() throws Exception {

        NetWorkData netWorkData = new NetWorkData(Long.valueOf("101"), Long.valueOf("201"),
                System.currentTimeMillis(), groupId);
        netWorkDataRepository.save(netWorkData);

        NetWorkData netWorkData2 = netWorkDataRepository.findAll().iterator().next();
        System.out.println("netWorkData: " + netWorkData2);

        TxGasData txGasData =
                new TxGasData("0xc7b2509298c78a248aac93fba66f3ec6f77fb4bc72a251a8e2ebc259d40f779a",
                        Long.valueOf("101"), System.currentTimeMillis(), groupId);
        txGasDataRepository.save(txGasData);

        TxGasData txGasData2 = txGasDataRepository.findAll().iterator().next();
        System.out.println("netWorkData: " + txGasData2);

        CurrentState currentState = new CurrentState(1, "aaa.log", Long.valueOf("100"));
        currentStateRepository.save(currentState);

        CurrentState currentState2 = currentStateRepository.findAll().iterator().next();
        System.out.println("currentState: " + currentState2);
    }

    @Test
    public void testGetNetWorkData() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/charging/getNetWorkData?beginDate=2020-03-26T10:40:00&endDate=2020-03-26T20:10:55&groupId="
                        + groupId)
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGetTxGasData() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/charging/getTxGasData?beginDate=2020-03-26T10:40:00&endDate=2020-03-26T22:55:55&groupId="
                        + groupId)
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
    
    @Test
    public void testDeleteData() throws Exception {
        
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete("/charging/deleteData?type=2&keepEndDate=2020-03-26T22:45:55&groupId="
                        + groupId)
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.print());
    }
}
