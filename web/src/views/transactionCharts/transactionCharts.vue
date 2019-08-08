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
        <v-content-head :headTitle="'交易审计'" :headSubTitle="headSubTitle"></v-content-head>
        <div class="module-wrapper">
            <div class="more-search-table search-min-width">
                <div class="text-left">
                    <div class="text-left-label">
                        <span>用户：</span>
                        <el-select v-model="userName" @change="changeUserName" class="select-32" clearable>
                            <el-option v-for="item in userList" :key="item.userName" :label="item.userName" :value="item.userName">
                                <span v-if="item.userType==0">{{item.userName}}</span>
                                <span v-else class="font-color-ed5454">{{item.userName}}</span>
                            </el-option>
                        </el-select>
                    </div>
                    <div class="text-left-label">
                        <span class="text-left-label-title">时长：</span>
                        <el-date-picker @change="changeDate" v-model="transDate" type="datetimerange" :picker-options="transPickerOptions" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" align="left" value-format="yyyy-MM-dd HH:mm:ss" class="date-select-32">
                        </el-date-picker>
                    </div>
                    <div class="text-left-label1">
                        <span class="text-left-label1-title">接口：</span>
                        <el-select v-model="interfaceName" @change="changeInterfaceName" class="select-32" clearable>
                            <el-option v-for="item in interfaceList" :key="item.interfaceName" :label="item.interfaceName" :value="item.interfaceName">
                                <span v-if="item.transUnusualType==0">{{item.interfaceName}}</span>
                                <span v-else class="font-color-ed5454">{{item.interfaceName}}</span>
                            </el-option>
                        </el-select>
                    </div>
                </div>
                <div class="divide-line"></div>
            </div>

            <div class="chart" ref="chart">
                <v-chart ref="linechart" :id="'transId'" v-if="chartStatistics.show" :chartStatistics="chartStatistics" v-bind:reload="reloadNum"   v-loading="loading"></v-chart>
            </div>
        </div>
    </div>
</template>

<script>
import charts from "./components/chart";
import contentHead from "@/components/contentHead";
import { format, completionDateData } from "@/util/util";
import {
    monitorUserList,
    monitorUserInterfaceList,
    monitorTransactionInfo
} from "@/util/api";
import errcode from "@/util/errcode";
export default {
    name: "transactionCharts",
    components: {
        "v-content-head": contentHead,
        "v-chart": charts
    },
    data() {
        return {
            loading: false,
            networkId: localStorage.getItem("networkId"),
            headSubTitle: "用户交易",
            reloadNum: 1,
            chartStatistics: {
                show: true,
                data: [],
                transactionDataArr: [],
                chartSize: {
                    width: 0,
                    height: 0
                },
                totalCount: ""
            },
            userList: [],
            interfaceList: [],
            userName: "",
            interfaceName: "",
            transDate: [],
            transPickerOptions: {
                shortcuts: [
                    {
                        text: "最近一周",
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            let startTime = start.getTime();
                            start.setTime(startTime - 3600 * 1000 * 24 * 7);
                            picker.$emit("pick", [start, end]);
                        }
                    },
                    {
                        text: "最近一个月",
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            let startTime = start.getTime();
                            start.setTime(startTime - 3600 * 1000 * 24 * 30);
                            picker.$emit("pick", [start, end]);
                        }
                    },
                    {
                        text: "最近三个月",
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            let startTime = start.getTime();
                            start.setTime(startTime - 3600 * 1000 * 24 * 90);
                            picker.$emit("pick", [start, end]);
                        }
                    }
                ],
                disabledDate(time) {
                    return time.getTime() > Date.now() - 8.64e6;
                }
            }
        };
    },
    mounted() {
        this.$nextTick(() => {
            this.chartStatistics.chartSize.width = this.$refs.chart.offsetWidth;
            this.chartStatistics.chartSize.height = this.$refs.chart.offsetHeight;
            this.getMonitorTransactionInfo();
            this.getMonitorUserList();
        });
    },
    methods: {
        getMonitorTransactionInfo() {
            this.loading = true
            let reqData = {
                    networkId: this.networkId
                },
                reqQurey = {};
            reqQurey = {
                userName: this.userName,
                interfaceName: this.interfaceName
            };
            if (this.transDate) {
                reqQurey.startDate = this.transDate[0];
                reqQurey.endDate = this.transDate[1];
            }
            monitorTransactionInfo(reqData, reqQurey)
                .then(res => {
                    this.loading = false;
                    if (res.data.code == 0) {
                        let transInfoList = res.data.data.transInfoList;
                        var len = transInfoList.length;
                        if (len === 0) {
                            this.$set(this.chartStatistics, "data", []);
                            this.$set(
                                this.chartStatistics,
                                "transactionDataArr",
                                []
                            );
                            this.$set(this.chartStatistics, "totalCount", 0);
                        } else {
                            var startDate = format(
                                    new Date(
                                        transInfoList[0]["time"]
                                    ).getTime(),
                                    "yyyy-MM-dd"
                                ),
                                endDate = format(
                                    new Date(
                                        transInfoList[len - 1]["time"]
                                    ).getTime(),
                                    "yyyy-MM-dd"
                                );

                            transInfoList.forEach(item => {
                                item.time = format(
                                    new Date(item.time).getTime(),
                                    "MM-dd"
                                );
                            });
                            var productSenceMap = {};
                            transInfoList.forEach(item => {
                                var transCount = productSenceMap[item.time];
                                if (!transCount) {
                                    transCount = item.transCount;
                                } else {
                                    transCount = transCount + item.transCount;
                                }
                                productSenceMap[item.time] = transCount;
                            });
                            var arr = [],
                                obj = {};

                            for (var key in productSenceMap) {
                                obj = {
                                    transCount: productSenceMap[key],
                                    time: key
                                };
                                arr.push(obj);
                            }
                            arr = completionDateData(startDate, endDate, arr);
                            let dateList = arr.map(item => {
                                return item.time;
                            });
                            let transactionData = arr.map(item => {
                                return item.transCount;
                            });
                            this.$set(this.chartStatistics, "data", dateList);
                            this.$set(
                                this.chartStatistics,
                                "transactionDataArr",
                                transactionData
                            );
                            this.$set(
                                this.chartStatistics,
                                "totalCount",
                                res.data.data.totalCount
                            );
                        }
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
                            this.errcode.errCode[err.data.code].cn ||
                            "获取监管用户列表失败！"
                    });
                });
        },
        getMonitorUserList() {
            let reqData = {
                networkId: this.networkId
            };
            monitorUserList(reqData, {})
                .then(res => {
                    if (res.data.code == 0) {
                        this.userList = res.data.data;
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
                            this.errcode.errCode[err.data.code].cn ||
                            "获取监管用户列表失败！"
                    });
                });
        },
        getMonitorUserInterfaceList(val) {
            let reqData = {
                    networkId: this.networkId
                },
                reqQurey = {};
            reqQurey = {
                userName: val
            };
            monitorUserInterfaceList(reqData, reqQurey)
                .then(res => {
                    if (res.data.code == 0) {
                        this.interfaceList = res.data.data;
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
                            this.errcode.errCode[err.data.code].cn ||
                            "获取监管用户列表失败！"
                    });
                });
        },
        changeUserName(val) {
            this.getMonitorUserInterfaceList(val);
            this.getMonitorTransactionInfo();
        },
        changeDate() {
            this.getMonitorTransactionInfo();
        },
        changeInterfaceName() {
            this.getMonitorTransactionInfo();
        }
    }
};
</script>

<style scoped>
.divide-line {
    border: 1px solid #e7ebf0;
    margin-top: 30px;
}
.text-left-label {
    display: inline;
}
.text-left-label1 {
    display: inline;
}
.text-left-label-title {
    margin-left: 50px;
}
.text-left-label1-title {
    margin-left: 50px;
}
@media only screen and (max-width: 1240px) {
    .text-left-label1 {
        display: block;
        padding: 5px 0;
    }
    .text-left-label1-title {
        margin-left: 0;
    }
}
@media only screen and (max-width: 1000px) {
    .text-left-label {
        display: block;
        padding: 5px 0;
    }
    .text-left-label-title {
        margin-left: 0;
    }
    .text-left-label1 {
        display: block;
        padding: 5px 0;
    }
    .text-left-label1-title {
        margin-left: 0;
    }
}
</style>
