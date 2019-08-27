package com.webank.webase.front.precompiledapi;

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
