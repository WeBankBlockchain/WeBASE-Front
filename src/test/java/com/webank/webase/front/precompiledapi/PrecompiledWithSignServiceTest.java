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

package com.webank.webase.front.precompiledapi;

import com.webank.webase.front.Application;
import com.webank.webase.front.precompiledapi.sysconf.PrecompiledSysConfigService;
import com.webank.webase.front.util.PrecompiledUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class PrecompiledWithSignServiceTest {
    @Autowired
    PrecompiledWithSignService precompiledWithSignService;
    @Autowired
    PrecompiledSysConfigService sysConfigService;
    private int groupId = 1;
    private String signAddress = "0xf16c0bf5a8bf4049ede4c3a070efcc1052095f63";

    /**
     * need webase-sign is on
     */
    @Test
    public void testSysConf() throws Exception {
        String txLimit = sysConfigService.getSysConfigByKey(groupId, PrecompiledUtils.TxCountLimit);
//        System.out.println("==========1 " + txLimit);
        String value = "1024";
        String result = precompiledWithSignService.setValueByKey(groupId, signAddress, PrecompiledUtils.TxCountLimit, value);
        String txLimit2 = sysConfigService.getSysConfigByKey(groupId, PrecompiledUtils.TxCountLimit);
//        System.out.println("==========2 " + txLimit2);
        Assert.assertNotEquals(txLimit, txLimit2);
    }

}
