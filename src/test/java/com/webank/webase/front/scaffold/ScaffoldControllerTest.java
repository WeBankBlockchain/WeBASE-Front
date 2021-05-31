/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.scaffold;

import com.webank.webase.front.base.SpringTestBase;
import com.webank.webase.front.scaffold.entity.ReqProject;
import com.webank.webase.front.util.JsonUtils;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class ScaffoldControllerTest extends SpringTestBase {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testExportProject() throws Exception {
        ReqProject param = new ReqProject();
        param.setArtifactName("demo");
        param.setGroup("org.webank");
        param.setContractIdList(Collections.singletonList(1));
        param.setGroupId(1);
        ResultActions resultActions = mockMvc.perform(
            MockMvcRequestBuilders.post( "/scaffold/export").
                content(JsonUtils.toJSONString(param)).
                contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
//            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
}
