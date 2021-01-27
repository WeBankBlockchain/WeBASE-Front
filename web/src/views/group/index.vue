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
        <v-content-head :headTitle="$t('route.nodeManagementQ')" @changeGroup="changeGroup"></v-content-head>
        <div class="module-wrapper">
            <div class="search-table">
                <el-table :data="nodeData" class="search-table-content" v-loading="loading">
                    <el-table-column v-for="head in nodeHead" :label="head.name" :key="head.enName" :width="head.width" show-overflow-tooltip align="center">
                        <template slot-scope="scope">
                            <span v-if="head.enName === 'status'">
                                <i :style="{'color': textColor(scope.row[head.enName])}" class="wbs-icon-radio font-6"></i> {{nodesStatus(scope.row[head.enName])}}
                            </span>
                            <span v-else-if="head.enName === 'nodeId'">
                                <i class="wbs-icon-copy font-12" @click="copyNodeIdKey(scope.row[head.enName])" :title="$t('title.copy')"></i>
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
import Bus from "@/bus"
export default {
    name: "group",
    components: {
        "v-content-head": contentHead,
    },
    computed: {
        nodeHead() {
            var arr = [
                {
                    enName: "nodeId",
                    name: this.$t('table.nodeId'),
                    width: ""
                },
                {
                    enName: "blockNumber",
                    name: this.$t('table.blockHeight'),
                    width: 180
                },
                {
                    enName: "pbftView",
                    name: this.$t('table.pbftView'),
                    width: 180
                },
                {
                    enName: "status",
                    name: this.$t('table.status'),
                    width: 150
                }
            ]
            return arr
        }
    },
    data() {
        return {
            nodeData: [],
            loading: false,
            group: localStorage.getItem('groupId') || null
        };
    },
    beforeDestroy() {
        Bus.$off("changeGroup")
    },
    mounted() {
        Bus.$on("changeGroup", data => {
            this.changeGroup(data)
        })
        if (this.group) {
            this.getNodeTable();
        }
    },
    methods: {
        changeGroup(val) {
            this.group = val
            this.getNodeTable();
        },
        getNodeTable() {
            queryNodesStatusInfo(this.group)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.nodeData = data
                    } else {
                        this.$message({
                            message: this.$chooseLang(res.data.code),
                            type: "error",
                            duration: 2000
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        message: err.data || this.$t('text.systemError'),
                        type: "error",
                        duration: 2000
                    });
                });
        },
        textColor(val) {
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
        nodesStatus(val) {
            let transString = "";
            switch (val) {
                case 1:
                    transString = this.$t('table.run');
                    break;
                case 2:
                    transString = this.$t('table.abnormal');
                    break;
            }
            return transString;
        },
        copyNodeIdKey(val) {
            if (!val) {
                this.$message({
                    type: "fail",
                    showClose: true,
                    message: this.$t('notice.copyFailure'),
                    duration: 2000
                });
            } else {
                this.$copyText(val).then(e => {
                    this.$message({
                        type: "success",
                        showClose: true,
                        message: this.$t('notice.copySuccessfully'),
                        duration: 2000
                    });
                });
            }
        }
    }
};
</script>
<style scoped>
.module-wrapper {
    height: 100%;
    background-color: #20293c;
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
    background-color: #2f3b52;
}
.search-table-content {
    width: 100%;
    background-color: #20293c;
}
.search-table-content >>> td,
.search-table-content >>> th {
    padding: 8px 0;
    font-size: 12px;
}
.search-table-content >>> th {
    color: #8598b0;
}
.search-table-content >>> td {
    color: #737a86;
}
.search-table-detail {
    width: 91%;
    float: right;
}
.search-table-detail >>> td,
.search-table-detail >>> th {
    color: #737a86;
}
.input-with-select >>> .el-input__inner {
    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    border: 1px solid #eaedf3;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
.input-with-select >>> .el-input-group__append {
    border-top-right-radius: 20px;
    border-bottom-right-radius: 20px;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
.input-with-select >>> .el-button {
    border: 1px solid #1f83e7;
    border-radius: inherit;
    background: #1f83e7;
    color: #fff;
}
</style>
