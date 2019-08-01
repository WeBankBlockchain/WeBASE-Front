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
<template>
    <div>
        <v-content-head :headTitle="'节点管理'" @changeGroup="changeGroup"></v-content-head>
        <div class="module-wrapper">
            <!-- <div class="search-part">
            </div> -->
            <div class="search-table">
                <el-table :data="nodeData" class="search-table-content" v-loading="loading">
                    <el-table-column v-for="head in nodeHead" :label="head.name" :key="head.enName" :width="head.width" show-overflow-tooltip align="center">
                        <template slot-scope="scope">
                            <span v-if="head.enName === 'status'">
                                <i :style="{'color': textColor(scope.row[head.enName])}" class="wbs-icon-radio font-6"></i> {{nodesStatus(scope.row[head.enName])}}
                            </span>
                            <span v-else-if="head.enName === 'nodeId'">
                                <i class="wbs-icon-copy font-12" @click="copyNodeIdKey(scope.row[head.enName])" title="复制"></i>
                                {{scope.row[head.enName]}}
                            </span>
                            <span v-else>{{scope.row[head.enName]}}</span>
                        </template>
                    </el-table-column>
                </el-table>
                <div style="height: 20px;"></div>
            </div>
        </div>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import { queryNodesStatusInfo } from "@/util/api";
import errcode from "@/util/errcode";
import Bus from "@/bus"
export default {
    name: "group",
    components: {
        "v-content-head": contentHead,
    },
    data: function() {
        return {
            nodeData: [],
            loading: false,
            nodeHead: [
                {
                    enName: "nodeId",
                    name: "节点Id",
                    width: ""
                },
                {
                    enName: "blockNumber",
                    name: "块高",
                    width: 180
                },
                {
                    enName: "pbftView",
                    name: "pbftView",
                    width: 180
                },
                {
                    enName: "status",
                    name: "状态",
                    width: 150
                }
            ],
            group: localStorage.getItem('groupId') || null
        };
    },
    beforeDestroy: function(){
        Bus.$off("changeGroup")
    },
    mounted: function() {
        Bus.$on("changeGroup",data => {
            this.changeGroup(data)
        })
        if(this.group){
            this.getNodeTable();
        }
    },
    methods: {
        changeGroup(val){
            this.group = val
            this.getNodeTable();
        },
        getNodeTable: function() {
            queryNodesStatusInfo(this.group)
                .then(res => {
                    const { data, status} = res;
                    if (status === 200) {
                        this.nodeData = data
                    } else {
                        this.$message({
                            message: errcode.errCode[res.data.code].cn || "查询失败",
                            type: "error",
                            duration: 2000
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        message: "查询失败！",
                        type: "error",
                        duration: 2000
                    });
                });
        },
        textColor: function(val) {
            let colorString = "";
            switch (val) {
                case 1:
                    colorString = "#58cb7d";
                    break;
                case 2:
                    colorString = "#ed5454";
                    break;
            }
            return colorString;
        },
        nodesStatus: function(val) {
            let transString = "";
            switch (val) {
                case 1:
                    transString = "运行";
                    break;
                case 2:
                    transString = "异常";
                    break;
            }
            return transString;
        },
        copyNodeIdKey: function(val) {
            if (!val) {
                this.$message({
                    type: "fail",
                    showClose: true,
                    message: "key为空，不复制。",
                    duration: 2000
                });
            } else {
                this.$copyText(val).then(e => {
                    this.$message({
                        type: "success",
                        showClose: true,
                        message: "复制成功",
                        duration: 2000
                    });
                });
            }
        },
    }
};
</script>
<style scoped>
.module-wrapper {
    height: 100%;
    background-color: #20293c
}
.search-part {
    padding: 30px 41px 18px 42px;
    overflow: hidden;
}
.search-part::after {
    display: block;
    content: "";
    clear: both;
}
.search-part-left {
    float: left;
}
.search-part-left-btn {
    border-radius: 20px;
}
.search-part-right {
    float: right;
}
.search-table {
    /* padding: 0 40px 0 41px; */
    background-color: #2f3b52
}
.search-table-content {
    width: 100%;
    background-color: #20293c
}
.search-table-content>>>td,
.search-table-content>>>th {
    padding: 8px 0;
    font-size: 12px;
}
.search-table-content>>>th {
    color: #8598b0;
}
.search-table-content>>>td {
    color: #737a86;
}
.search-table-detail {
    width: 91%;
    float: right;
}
.search-table-detail>>>td,
.search-table-detail>>>th {
    color: #737a86;
}
.input-with-select>>>.el-input__inner {
    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    border: 1px solid #eaedf3;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
.input-with-select>>>.el-input-group__append {
    border-top-right-radius: 20px;
    border-bottom-right-radius: 20px;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
.input-with-select>>>.el-button {
    border: 1px solid #1f83e7;
    border-radius: inherit;
    background: #1f83e7;
    color: #fff;
}
</style>
