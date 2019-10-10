/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.precompiledapi.precompiledHandle;

public class NodeInfo {
    private String nodeId;
    private String nodeType;

    public NodeInfo(String nodeId, String nodeType) {
        super();
        this.nodeId = nodeId;
        this.nodeType = nodeType;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public String getNodeType() {
        return this.nodeType;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
}
