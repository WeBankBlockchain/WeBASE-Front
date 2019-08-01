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
        <v-content-head :headTitle="`${nodesQuery.nodeIp}`" :icon="true"></v-content-head>
        <div class="module-wrapper">
            <div class="more-search-table">
                <div class="search-item">
                    <span>显示日期</span>
                    <el-date-picker v-model="currentDate" type="date" placeholder="选择日期" :picker-options="pickerOption" format="yyyy 年 MM 月 dd 日" value-format="yyyy-MM-dd" :default-value="`${Date()}`" class=" select-32" @change="changeCurrentDate">
                    </el-date-picker>
                </div>
                <div class="search-item">
                    <span>对比日期</span>
                    <el-date-picker v-model="contrastDate" type="date" placeholder="选择日期" :picker-options="pickerOption" format="yyyy 年 MM 月 dd 日" value-format="yyyy-MM-dd" class=" select-32" @change="changeContrastDate">
                    </el-date-picker>
                </div>
                <div class="search-item">
                    <span>起止时间</span>
                    <el-time-picker is-range v-model="startEndTime" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" placeholder="选择时间范围" class="time-select-32">
                    </el-time-picker>
                </div>
                <div class="search-item">
                    <span>数据粒度</span>
                    <el-radio-group v-model="timeGranularity">
                        <el-radio :label="60">5分钟</el-radio>
                        <el-radio :label="12">1分钟</el-radio>
                        <el-radio :label="1">5秒钟</el-radio>
                    </el-radio-group>
                    <el-button type="primary" @click="confirmParam(tab)" size="small" style="margin-left: 12px;" :loading="sureing">确认</el-button>
                </div>
            </div>
            <div class="metric-content">
                <el-button-group class="tab-list">
                    <el-button :class="tab==='hostInfo'?'active':''" @click="changeTab('hostInfo')">主机信息</el-button>
                    <el-button :class="tab==='chainInfo'?'active':''" @click="changeTab('chainInfo')">节点信息</el-button>
                </el-button-group>
                <div class="tab-metric">
                    <el-row v-show="tab==='hostInfo'" v-loading="loadingInit">
                        <template v-for="item in metricData">
                            <el-col :xs='24' :sm="24" :md="24" :lg="12" :xl="12">
                                <v-metric-chart :chartOption="item" :reload="reloadNum" v-loading="loading"></v-metric-chart>
                            </el-col>
                        </template>
                    </el-row>
                    <el-row v-show="tab==='chainInfo'">
                        <template v-for="item in nodesHealthData">
                            <el-col :xs='24' :sm="24" :md="24" :lg="12" :xl="12">
                                <v-nodes-chart :chartOption="item" :reload="nodesReloadNum" v-loading="loading"></v-nodes-chart>
                            </el-col>
                        </template>
                    </el-row>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import metricChart from "./components/metricChart";
import nodesChart from "./components/nodesChart";
import { metricInfo, nodesHealth } from "@/util/api";
import { format, numberFormat } from "@/util/util.js";
export default {
    name: "hostDetail",
    components: {
        "v-content-head": contentHead,
        "v-metric-chart": metricChart,
        "v-nodes-chart": nodesChart
    },
    watch: {
        nodeIp: function() {
            this.getNodeIpDetail();
        },
        $route: function() {
            this.nodesQuery = this.$root.$route.query;
        }
    },
    data() {
        return {
            loadingInit: false,
            loading: false,
            sureing: false,
            currentDate: format(new Date().getTime(),'yyyy-MM-dd'),
            contrastDate: null,
            startEndTime: [new Date(new Date().toLocaleDateString()), new Date()],
            beginDate: "",
            endDate: "",
            contrastBeginDate: "",
            contrastEndDate: "",
            timeGranularity: 60,
            chartType: "",
            reloadNum: 1,
            nodesReloadNum: 1,
            colNum: 12,
            tab: "hostInfo",
            chartParam: {
                gap: 1,
                beginDate: `${format(new Date().getTime(),'yyyy-MM-dd')}T${format(new Date(new Date().toLocaleDateString()).getTime(),'HH:mm:ss')}`,
                endDate: `${format(new Date().getTime(),'yyyy-MM-dd')}T${format(new Date().getTime(),'HH:mm:ss')}`,
                contrastBeginDate: "",
                contrastEndDate: ""
            },
            chartTypeList: [
                {
                    type: "cpu",
                    name: "cpu"
                },
                {
                    type: "ram",
                    name: "ram"
                },
                {
                    type: "hd",
                    name: "硬盘"
                }
            ],
            pickerOption: {
                disabledDate(time) {
                    return time.getTime() > Date.now() - 8.64e6;
                }
            },
            metricData: [],
            nodesHealthData: [],
            nodesQuery: this.$root.$route.query
        };
    },
    mounted() {
        this.getChartData();
    },
    methods: {
        getChartData() {
            if(this.reloadNum===1){
                this.loadingInit = true
            }
            this.loading = true;
            this.sureing = true;
            var reqData = {
                    nodeId: this.nodesQuery.nodeId
                },
                reqQurey = {};
            reqQurey = this.chartParam;
            metricInfo(reqData, reqQurey)
                .then(res => {
                    this.loadingInit = false;
                    this.loading = false;
                    this.sureing = false;
                    if (res.data.code === 0) {
                        var data = res.data.data;
                        if (
                            data[0]["data"]["lineDataList"]["timestampList"]
                                .length > 0
                        ) {
                            var timestampList =
                                data[0]["data"]["lineDataList"][
                                    "timestampList"
                                ] || [];
                        } else {
                            var timestampList =
                                data[0]["data"]["contrastDataList"][
                                    "timestampList"
                                ] || [];
                        }
                        this.metricData = data;
                        this.metricData.forEach(item => {
                            item.gap = this.timeGranularity;
                            if (item.metricType === "cpu") {
                                item.metricName = "cpu";
                                item.metricUint = "利用率";
                                item.metricU = "%";
                            } else if (item.metricType === "memory") {
                                item.metricName = "内存";
                                item.metricUint = "利用率";
                                item.metricU = "%";
                            } else if (item.metricType === "disk") {
                                item.metricName = "硬盘";
                                item.metricUint = "利用率";
                                item.metricU = "%";
                            } else if (item.metricType === "txbps") {
                                item.metricName = "上行";
                                item.metricUint = "带宽";
                                item.metricU = "KB/s";
                            } else if (item.metricType === "rxbps") {
                                item.metricName = "下行";
                                item.metricUint = "带宽";
                                item.metricU = "KB/s";
                            }
                            item.data.contrastDataList.timestampList = timestampList;
                            item.data.lineDataList.timestampList = timestampList;
                        });
                        this.reloadNum++;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.errcode.errCode[res.data.code].cn
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message:
                            this.errcode.errCode[err.data.code].cn || "系统错误"
                    });
                });
        },
        getHealthData() {
            this.loading = true;
            this.sureing = true;
            var reqData = {
                    nodeId: this.nodesQuery.nodeId
                },
                reqQurey = {};
            reqQurey = this.chartParam;
            nodesHealth(reqData, reqQurey)
                .then(res => {
                    this.loading = false;
                    this.sureing = false;
                    if (res.data.code === 0) {
                        var data = res.data.data;
                        if (
                            data[0]["data"]["lineDataList"]["timestampList"]
                                .length > 0
                        ) {
                            var timestampList =
                                data[0]["data"]["lineDataList"][
                                    "timestampList"
                                ] || [];
                        } else {
                            var timestampList =
                                data[0]["data"]["contrastDataList"][
                                    "timestampList"
                                ] || [];
                        }
                        this.nodesHealthData = data;
                        this.nodesHealthData.forEach(item => {
                            if (item.metricType === "blockHeight") {
                                item.metricName = "区块高度";
                            } else if (item.metricType === "pbftView") {
                                item.metricName = "pbftView";
                            }
                            item.data.contrastDataList.timestampList = timestampList;
                            item.data.lineDataList.timestampList = timestampList;
                        });
                        this.nodesReloadNum++;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.errcode.errCode[res.data.code].cn
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message:
                            this.errcode.errCode[err.data.code].cn || "系统错误"
                    });
                });
        },
        changeCurrentDate($event) {
            this.startEndTime = [new Date(new Date().toLocaleDateString()), new Date()];
        },
        changeContrastDate($event) {},
        confirmParam(val) {
            this.timeParam();
            switch (val) {
                case "hostInfo":
                    this.getChartData();
                    break;
                case "chainInfo":
                    this.getHealthData();
                    break;
            }
        },
        changeTab(tab) {
            if (!this.startEndTime || !this.currentDate) {
                this.$message({
                    type: "error",
                    message: "请选择显示日期和时间"
                });
                return;
            }
            if (!this.contrastDate && !this.startEndTime) {
                this.$message({
                    type: "error",
                    message: "请选择显示日期和时间"
                });
                return;
            }
            this.timeParam();
            this.tab = tab;
            switch (tab) {
                case "hostInfo":
                    this.confirmParam(tab);
                    break;
                case "chainInfo":
                    this.confirmParam(tab);
                    break;
            }
        },
        timeParam() {
            let initStartTime = format(new Date(this.startEndTime[0]).getTime(),'HH:mm:ss') ,
                initEndTime = format(new Date(this.startEndTime[1]).getTime(),'HH:mm:ss');
            if (this.currentDate) {
                this.beginDate = `${this.currentDate}T${initStartTime}`;
                this.endDate = `${this.currentDate}T${initEndTime}`;
            }
            if (this.currentDate && this.contrastDate) {
                this.beginDate = `${this.currentDate}T${initStartTime}`;
                this.endDate = `${this.currentDate}T${initEndTime}`;
                this.contrastBeginDate = `${this
                    .contrastDate}T${initStartTime}`;
                this.contrastEndDate = `${this.contrastDate}T${initEndTime}`;
            }
            if (!this.contrastDate) {
                this.beginDate = `${this.currentDate}T${initStartTime}`;
                this.endDate = `${this.currentDate}T${initEndTime}`;
                this.contrastBeginDate = "";
                this.contrastEndDate = "";
            }
            this.chartParam.beginDate = this.beginDate;
            this.chartParam.endDate = this.endDate;
            this.chartParam.contrastBeginDate = this.contrastBeginDate;
            this.chartParam.contrastEndDate = this.contrastEndDate;
            this.chartParam.gap = this.timeGranularity;
        }
    }
};
</script>

<style scoped>
.search-item {
    display: inline-block;
    margin-bottom: 8px;
    margin-right: 12px;
}
.search-item > span {
    margin-right: 5px;
}
.more-search-table>>>.el-radio + .el-radio {
    margin-left: 10px;
}
.metric-content {
    padding-left: 30px;
    padding-right: 30px;
    margin-bottom: 10px;
    padding-bottom: 15px;
}
.tab-metric {
    border: 1px solid #ddd;
    margin-top: -1px;
    min-height: 700px;
}
.tab-list>>>.el-button {
    border: none;
    background: transparent;
    padding: 10px 12px;
    color: #666666;
    border-radius: 0;
}
.tab-list>>>.el-button:hover {
    color: #1e1e1e;
}
.tab-list .active {
    color: #1a4ea1;
    background: #ffffff;
    border: 1px solid #ddd;
    border-bottom-color: #fff;
}

.tab-list .active:focus {
    color: #1a4ea1;
}
</style>
