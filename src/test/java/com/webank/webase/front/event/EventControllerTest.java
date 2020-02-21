/**
 * Copyright 2014-2019 the original author or authors.
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

package com.webank.webase.front.event;

import com.alibaba.fastjson.JSON;
import com.webank.webase.front.event.entity.ReqNewBlockEventRegister;
import com.webank.webase.front.event.entity.ReqContractEventRegister;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@WebAppConfiguration
public class EventControllerTest extends BaseTest {

	private MockMvc mockMvc;
	private Integer groupId = 1;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void setUp()  {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	/**
	 * need to create exchange and queue with routing key in mq
	 * @throws Exception
	 */
	@Test
	public void testRegisterContractEvent() throws Exception {
		ReqContractEventRegister param = new ReqContractEventRegister();
		param.setGroupId(groupId);
		param.setAppId("app1");
		param.setExchangeName("group001");
		param.setQueueName("user1");
		param.setContractAbi("[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"string\"}],\"name\":\"SetName\",\"type\":\"event\"}]");
		param.setFromBlock("latest");
		param.setToBlock("latest");
		param.setContractAddress("0x657201d59ec41d1dc278a67916f751f86ca672f7");
		List<String> topics = new ArrayList<>();
		topics.add("SetName(string)");
		param.setTopicList(topics);
		ResultActions resultActions = mockMvc
				.perform(MockMvcRequestBuilders.post("/event/contractEvent").
						content(JSON.toJSONString(param)).
						contentType(MediaType.APPLICATION_JSON)
				);
		resultActions.
				andExpect(MockMvcResultMatchers.status().isOk()).
				andDo(MockMvcResultHandlers.print());
		System.out
				.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
	}

	/**
	 * need to create exchange and queue with routing key in mq
	 * @throws Exception
	 */
	@Test
	public void testRegisterNewBlockEvent() throws Exception {
		ReqNewBlockEventRegister param = new ReqNewBlockEventRegister();
		param.setExchangeName("group001");
		param.setQueueName("user1");
		param.setAppId("app1");
		param.setGroupId(1);
		ResultActions resultActions = mockMvc
				.perform(MockMvcRequestBuilders.post("/event/newBlockEvent").
						content(JSON.toJSONString(param)).
						contentType(MediaType.APPLICATION_JSON)
				);
		resultActions.
				andExpect(MockMvcResultMatchers.status().isOk()).
				andDo(MockMvcResultHandlers.print());
		System.out
				.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
	}
}
