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
package com.webank.webase.front.web3api.entity;

import com.webank.webase.front.base.enums.DataStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeStatusInfo {

    private String nodeId;
    private long blockNumber;
    private long pbftView;
    private Integer status;
    private LocalDateTime latestStatusUpdateTime;
    private boolean timeout;
    private boolean isSyncing;

    public NodeStatusInfo (String nodeId, long blockNumber, long pbftView) {
        this.nodeId = nodeId;
        this.blockNumber = blockNumber;
        this.pbftView = pbftView;
        this.status = DataStatus.NORMAL.getValue();
        this.latestStatusUpdateTime = LocalDateTime.now();
        this.timeout = false;
        this.isSyncing = false;
    }
}
