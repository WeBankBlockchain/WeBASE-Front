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

package com.webank.webase.front.event;

import com.webank.webase.front.base.SpringTestBase;
import com.webank.webase.front.event.entity.ContractEventInfo;
import com.webank.webase.front.event.entity.NewBlockEventInfo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EventServiceTestBase extends SpringTestBase {

    @Autowired
    private ContractEventInfoRepository eventInfoRepository;
    @Autowired
    private NewBlockEventInfoRepository blockEventInfoRepository;
    @Autowired
    private EventService eventService;

    @Test
    public void testFindContractEvent() {
        ContractEventInfo c = eventInfoRepository.findContractEventInfo("app_006", "group003", "app_003_event", "0x095b99f3b6e87931f6ee60e6ca8a41b16f0acef9");
        System.out.println("ContractEventInfo: " + c);
    }

    @Test
    public void testFindBlockEvent() {
        NewBlockEventInfo n = blockEventInfoRepository.findNewBlockEventInfo("app_006", "group003", "test_queue");
        System.out.println("NewBlockEventInfo: " + n);

    }

    @Test
    public void testGetEventSync() {
        // eventService.getContractEventLog(groupId, contractAddress, abiStr,
        //            fromBlock, toBlock, eventTopicParam·);
    }

}
