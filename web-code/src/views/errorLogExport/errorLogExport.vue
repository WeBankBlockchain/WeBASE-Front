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
    <div class="export-wrapper">
        <v-content-head :headTitle="'系统监控'" :headSubTitle="'错误日志'"></v-content-head>
        <div class="module-wrapper">
            <div class="search-part text-left search-min-width">
                <div class="search-part-label">
                    <span style="margin-right:5px">选择节点名:</span>
                    <el-select v-model="nodeId" placeholder="请选择" @change="changeNodeId" class="select-32" style="width:189px;">
                        <el-option v-for="item in nodeList" :key="item.nodeId" :label="item.nodeName" :value="item.nodeId">
                            <span class="font-12 text-left">{{item.nodeName}}</span>
                            <span class="font-12 text-right font-color-9da2ab font-weight-500">{{item.orgName}}</span>
                        </el-option>
                    </el-select>
                </div>
                <div class="search-part-label">
                    <span class="search-part-label-title">选择日期:</span>
                    <el-date-picker @change="changeDate" v-model="errorLogDate" type="datetimerange" :picker-options="errorLogPickerOptions" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" align="left" value-format="yyyy-MM-dd HH:mm:ss" class="date-select-32">
                    </el-date-picker>
                </div>
                <div class="search-part-label1">
                    <el-button type="primary" @click="sureSelct" class="button-32 search-part-label-title1">确 定</el-button>
                </div>
            </div>
            <div class="dashed-line"></div>
            <div class="search-table">
                <el-table :data="errorLogList" tooltip-effect="light" v-loading="loading">
                    <el-table-column v-for="head in errorLogHead" :label="head.name" :key="head.enName" show-overflow-tooltip align="left" :width="tdWidth[head.enName]">
                        <template slot-scope="scope">
                            <span v-if="head.enName ==='logMsg'">
                                <i class="wbs-icon-copy font-12" @click="copyErrorInfo(scope.row[head.enName])" title="复制错误信息"></i>
                                <span class="copy-error-info" @click="showErrorInfo(scope.row[head.enName])">{{scope.row[head.enName]}}</span>
                            </span>
                            <span v-else>{{scope.row[head.enName]}}</span>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>
            </div>
        </div>
        <el-dialog :visible.sync="creatErrorVisible" title="错误信息" width="621px" :append-to-body="true" class="dialog-wrapper" center>
            <error-info :showCurrentErrorInfo="showCurrentErrorInfo" ref="creatError"></error-info>
        </el-dialog>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import { getErrorNodeList, errorLogList } from "@/util/api";
import errorInfo from "./components/errorInfo";
export default {
    name: "errorLogExport",
    components: {
        "v-content-head": contentHead,
        errorInfo
    },
    data() {
        return {
            errorLogPickerOptions: {
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
            },
            errorLogDate: [],
            nodeId: "",
            nodeList: [],
            loading: false,
            currentPage: 1,
            pageSize: 10,
            total: 0,
            errorLogList: [],
            errorLogHead: [
                {
                    enName: "logTime",
                    name: "时间"
                },
                {
                    enName: "logMsg",
                    name: "错误信息"
                }
            ],
            tdWidth: {
                logTime: 200
            },
            showCurrentErrorInfo: "",
            creatErrorVisible: false
        };
    },
    mounted() {
        this.getNodeData(this.getErrorLog);
    },
    methods: {
        changeNodeId() {
            this.getErrorLog();
        },
        changeDate() {
            this.getErrorLog();
        },
        sureSelct() {
            this.getErrorLog();
        },
        getNodeData(callback) {
            this.loading = true;
            let networkId = localStorage.getItem("networkId");
            let reqString = `${networkId}/1/100`;
            getErrorNodeList(reqString)
                .then(res => {
                    if (res.data.code === 0) {
                        this.nodeList = res.data.data || [];
                        this.nodeId = res.data.data[0]["nodeId"];
                        this.loading = false;
                        callback();
                    } else {
                        this.loading = false;
                        this.$message({
                            message: this.errcode.errCode[res.data.code].cn,
                            type: "error",
                            duration: 2000
                        });
                    }
                })
                .catch(err => {
                    this.loading = false;
                    this.$message({
                        message:
                            this.errcode.errCode[err.data.code].cn || "查询节点失败！",
                        type: "error",
                        duration: 2000
                    });
                });
        },
        getErrorLog() {
            this.loading = true;
            let reqData = {
                    networkId: localStorage.getItem("networkId"),
                    nodeId: this.nodeId,
                    pageNumber: this.currentPage,
                    pageSize: this.pageSize
                },
                reqQuery = {};
            if (this.errorLogDate) {
                reqQuery = {
                    startTime: this.errorLogDate[0],
                    endTime: this.errorLogDate[1]
                };
            }
            errorLogList(reqData, reqQuery)
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        this.errorLogList = res.data.data || [];
                        this.total = res.data.totalCount;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.errcode.errCode[res.data.code].cn
                        });
                    }
                })
                .catch(err => {
                    this.loading = false;
                    this.$message({
                        type: "error",
                        message:
                            this.errcode.errCode[err.data.code].cn || "系统错误！"
                    });
                });
        },
        handleSizeChange: function(val) {
            this.pageSize = val;
            this.currentPage = 1;
            this.getErrorLog();
        },
        handleCurrentChange: function(val) {
            this.currentPage = val;
            this.getErrorLog();
        },
        copyErrorInfo(val) {
            if (!val) {
                this.$message({
                    type: "fail",
                    showClose: true,
                    message: "error为空，不复制。",
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
        showErrorInfo(val) {
            this.showCurrentErrorInfo = val;
            this.creatErrorVisible = true;
        }
    }
};
</script>

<style scoped>
@import "./errorLogExport.css";
</style>
