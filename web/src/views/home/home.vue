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
    <div class="over-view-wrapper">
        <v-content-head :headTitle="$t('route.statistics')" @changeGroup="changeGroup">
            <!-- <solt name="group" ref="global-group"></solt> -->
        </v-content-head>
        <div style="margin: 0 20px 20px 0;">
            <el-row class="module-box-shadow">
                <el-col :xs='12' :sm="12" :md="6" :lg="6" :xl="6">
                    <div class="overview-number block-bg">
                        <div class="part1-content-amount">
                            <img :src="blockImg" alt="">
                        </div>
                        <div class="part2-content-amount">
                            <span class="font-28 part2-content-amount-number">{{numberFormat(overviewBlockNumber, 0, ".", ",")}}</span><br>
                            <span class="font-14">{{this.$t('project.blocks')}}</span>
                        </div>
                    </div>
                </el-col>
                <el-col :xs='12' :sm="12" :md="6" :lg="6" :xl="6">
                    <div class="overview-number node-bg">
                        <div class="part1-content-amount">
                            <img :src="nodesImg" alt="">
                        </div>
                        <div class="part2-content-amount">
                            <span class="font-28 part2-content-amount-number">{{numberFormat(overviewNodesNumber, 0, ".", ",")}}</span>
                            <span class="font-14">{{this.$t('project.nodes')}}</span>
                        </div>
                    </div>
                </el-col>
                <el-col :xs='12' :sm="12" :md="6" :lg="6" :xl="6">
                    <div class="overview-number transation-bg">
                        <div class="part1-content-amount">
                            <img :src="transationImg" alt="">
                        </div>
                        <div class="part2-content-amount">
                            <span class="font-28 part2-content-amount-number">{{numberFormat(overviewTxNumber, 0, ".", ",")}}</span>
                            <span class="font-14">{{this.$t('project.transactions')}}</span>
                        </div>
                    </div>
                </el-col>
                <el-col :xs='12' :sm="12" :md="6" :lg="6" :xl="6">
                    <div class="overview-number transationPendding-bg">
                        <div class="part1-content-amount">
                            <img :src="transationPeddingImg" alt="">
                        </div>
                        <div class="part2-content-amount">
                            <span class="font-28 part2-content-amount-number">{{numberFormat(overviewPendingTxNumber, 0, ".", ",")}}</span>
                            <span class="font-14">{{this.$t('project.pendingTransactions')}}</span>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>
        <div class="module-wrapper-small" style="padding: 30px 31px 26px 32px;">
            <el-input :placeholder="$t('placeholder.globalSearch')" v-model.trim="keyword" @keyup.enter.native="submit">
                <el-button slot="append" icon="el-icon-search" v-loading="searchLoading" @click="submit"></el-button>
            </el-input>
        </div>
        <div class="module-wrapper-small json-view" style="padding: 30px 31px 26px 32px;" v-show="blockData.length > 0" v-loading="loadingBlock">
            <json-viewer :value="searchMap" :expand-depth="2" copyable>

            </json-viewer>
        </div>
        <div class="module-wrapper-small" style="padding: 30px 31px 26px 32px;" v-show="transactionList.length > 0" v-loading="loadingTransaction">
            <el-table :data="transactionList" class="block-table-content" @row-click="clickTable" ref="refTable">
                <el-table-column type="expand" align="center">
                    <template slot-scope="scope">
                        <v-transaction-detail :transHash="scope.row.transHash"></v-transaction-detail>
                    </template>
                </el-table-column>
                <el-table-column prop="transHash" :label="$t('table.transactionHash')" align="center" :show-overflow-tooltip="true">
                    <template slot-scope="scope">
                        <span>
                            <i class="wbs-icon-copy font-12 copy-key" @click="copyPubilcKey(scope.row['transHash'])" :title="$t('title.copyHash')"></i>
                            {{scope.row['transHash']}}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="blockNumber" :label="$t('table.blockHeight')" width="260" align="center" :show-overflow-tooltip="true">
                    <template slot-scope="scope">
                        <span>{{scope.row['blockNumber']}}</span>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <div class="module-wrapper-small" style="padding: 30px 31px 26px 32px;display: none">
            <el-table :data="nodeData" class="search-table-content" v-loading="loadingNodes">
                <el-table-column v-for="head in nodeHead" :label="head.name" :key="head.enName" show-overflow-tooltip align="">
                    <template slot-scope="scope">
                        <template>
                            <span v-if="head.enName ==='nodeActive'">
                                <i :style="{'color': textColor(scope.row[head.enName])}" class="wbs-icon-radio font-6"></i> {{nodesStatus(scope.row[head.enName])}}
                            </span>
                            <span v-else-if="head.enName ==='nodeIp'">
                                <router-link :to="{'path': 'hostDetail', 'query': {nodeIp: scope.row['nodeIp'], nodeId: scope.row['nodeId']}}" class="node-ip">{{scope.row[head.enName]}}</router-link>
                            </span>
                            <span v-else>{{scope.row[head.enName]}}</span>
                        </template>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <div style="min-width: 540px;margin: 8px 8px 0px 9px; display: none">
            <el-row :gutter="16">
                <el-col :xs='24' :sm="24" :md="12" :lg="12" :xl="12">
                    <div class="overview-wrapper">
                        <p>
                            <span class="overview-title">区块</span>
                            <span class="overview-more cursor-pointer" @click="goRouter('blocks')">更多</span>
                        </p>
                        <div class="overview-item-base" v-loading="loadingBlock">
                            <div class="block-item font-color-2e384d" v-for="(item ,index) in blockData" :key='index'>
                                <div class="block-amount">
                                    <span>
                                        <router-link :to="{'path': 'transactionInfo', 'query': {blockNumber: item.blockNumber}}" class="node-ip">块高 {{item.blockNumber}}</router-link>
                                    </span>
                                    <span class="font-color-8798ad">{{item.blockTimestamp}}</span>
                                </div>
                                <div>
                                    <div class="block-miner">
                                        <span>出块者</span>
                                        <p :title="`${item.miner}`">{{item.miner}}</p>
                                    </div>
                                    <!-- <div class="text-right">
                                        <span>{{item.transCount}}</span>
                                        <span>txns</span>
                                    </div> -->
                                </div>
                            </div>
                        </div>
                    </div>
                </el-col>
                <el-col :xs='24' :sm="24" :md="12" :lg="12" :xl="12">
                    <div class="overview-wrapper">
                        <p>
                            <span class="overview-title">交易</span>
                            <span class="overview-more cursor-pointer" @click="goRouter('transactions')">更多</span>
                        </p>
                        <div class="overview-item-base" v-loading="loadingTransaction">
                            <div class="block-item font-color-2e384d" v-for="(item, index) in transactionList" :key='index'>
                                <div class="block-amount">
                                    <p class="trans-hash" :title="`${item.transHash}`">
                                        <router-link :to="{'path': 'transactionInfo', 'query': {blockNumber: item.transHash}}" class="node-ip">{{item.transHash}}</router-link>
                                    </p>
                                    <span class="font-color-8798ad">{{item.blockTimestamp}}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import transactionDetail from "@/components/transactionDetail";
import charts from "./components/chart";
import {
    getChartData,
    getNetworkStatistics,
    getNodeList,
    getBlockPage,
    getTransactionList,
    queryBlockNumber,
    queryNodesNumber,
    queryTxNumber,
    queryPendingTxNumber,
    queryHomeSearch,
    queryGroup
} from "@/util/api";
import { changWeek, numberFormat, format } from "@/util/util";
import router from "@/router";
import block_img from "@/../static/image/block.png"
import nodes_img from "@/../static/image/nodes.png"
import transation_img from "@/../static/image/transation.png"
import transationPedding_img from "@/../static/image/transationPedding.png"
import Bus from "@/bus";

export default {
    name: "home",
    components: {
        "v-content-head": contentHead,
        "v-chart": charts,
        "v-transaction-detail": transactionDetail
    },
    data: function () {
        return {
            blockImg: block_img,
            nodesImg: nodes_img,
            transationImg: transation_img,
            transationPeddingImg: transationPedding_img,
            loadingNumber: false,
            loadingCharts: false,
            loadingNodes: false,
            loadingBlock: false,
            loadingTransaction: false,
            searchLoading: false,
            numberFormat: numberFormat,
            format: format,
            detailsList: [
                {
                    label: "节点个数",
                    name: "nodeCount",
                    value: 0,
                    color: "#8693f3"
                },
                {
                    label: "合约块高",
                    name: "contractCount",
                    value: 0,
                    color: "#bc8dee"
                },
                {
                    label: "区块数量",
                    name: "latestBlock",
                    value: 0,
                    color: "#ffa897"
                },
                {
                    label: "交易数量",
                    name: "transactionCount",
                    value: 0,
                    color: "#89c3f8"
                }
            ],
            networkDetails: null,
            chartStatistics: {
                show: false,
                date: [],
                dataArr: [],
                chartSize: {
                    width: 0,
                    height: 0
                }
            },
            reloadNumber: true,
            networkId: localStorage.getItem("networkId"),
            nodeHead: [
                {
                    enName: "orgName",
                    name: "机构名称"
                },
                {
                    enName: "nodeName",
                    name: "节点名称"
                },
                {
                    enName: "blockNumber",
                    name: "块高"
                },
                {
                    enName: "pbftView",
                    name: "pbftView"
                },
                {
                    enName: "nodeIp",
                    name: "ip"
                },
                {
                    enName: "p2pPort",
                    name: "p2p端口"
                },
                {
                    enName: "rpcPort",
                    name: "rpc端口"
                },
                {
                    enName: "nodeActive",
                    name: "状态"
                }
            ],
            nodeData: [],
            blockData: [],
            transactionList: [],
            keyword: "",
            reqQuery: {},
            getRowKeys: function (row) {
                return row.transHash;
            },
            overviewBlockNumber: "",
            overviewNodesNumber: "",
            overviewTxNumber: "",
            overviewPendingTxNumber: "",
            group: localStorage.getItem("groupId"),
            searchMap: {}
        };
    },
    beforeDestroy: function () {
        Bus.$off("changeGroup")
    },
    mounted: function () {
        Bus.$on("changeGroup", data => {
            this.changeGroup(data)
        })
        this.networkId = localStorage.getItem("networkId");
        // this.getNetworkDetails();
        // this.getNodeTable();
        // this.getBlockList();
        // this.getTransaction();
        if (this.group) {
            this.getAllOverview()
        } else {
            this.getGroup()
        }
        this.$nextTick(function () {
            // this.chartStatistics.chartSize.width = this.$refs.chart.offsetWidth;
            // this.chartStatistics.chartSize.height = this.$refs.chart.offsetHeight;
            // this.getChart();
        });
    },
    destroyed() { },
    methods: {
        getGroup() {
            queryGroup()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200 && data && data.length) {
                        if(data && data.length){
                            let arr = data.sort((a, b) => {
                                return a - b
                            })
                            this.group = arr[0]
                            this.getAllOverview()
                        }
                    }
                })
                .catch(err => {
                    console.log(err)
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                })
        },
        getAllOverview() {
            this.getBlockNumber();
            this.getNodesNumber();
            this.getTxNumber();
            this.getPendingTxNumber();
        },
        changeGroup(val) {
            this.group = val;
            this.blockData = [];
            this.transactionList = [];
            this.keyword = "";
            this.getBlockNumber();
            this.getNodesNumber();
            this.getTxNumber();
            this.getPendingTxNumber();
        },
        getBlockNumber: function () {
            queryBlockNumber(this.group)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.overviewBlockNumber = data;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        getNodesNumber: function () {
            queryNodesNumber(this.group)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.overviewNodesNumber = data.length;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        getTxNumber: function () {
            queryTxNumber(this.group)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.overviewTxNumber = data.txSum;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        getPendingTxNumber: function () {
            queryPendingTxNumber(this.group)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.overviewPendingTxNumber = data;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        getHomeSearch: function () {
            var reqQuery = {
                input: this.keyword
            };
            queryHomeSearch(this.group, reqQuery)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        if (!data) {
                            this.blockData = []
                            this.transactionList = [];
                            this.$message({
                                type: "error",
                                message: this.$t('text.searchEmpty')
                            });
                        } else {
                            var arr = [];
                            this.blockData = []
                            this.transactionList = [];
                            if (this.keyword.length == 66) {
                                this.searchMap = data;
                                arr.push({
                                    blockNumber: data["blockNumber"],
                                    transHash: data["hash"]
                                });
                                this.transactionList = arr;
                            } else {
                                this.searchMap = data;
                                arr.push({
                                    blockNumber: data["number"],
                                    pkHash: data["hash"]
                                });
                                this.blockData = arr;
                            }
                        }

                    } else {
                        this.blockData = [];
                        this.transactionList = [];
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },

        getNetworkDetails: function () {
            this.loadingNumber = true;
            let networkId = this.networkId;
            getNetworkStatistics(networkId)
                .then(res => {
                    this.loadingNumber = false;
                    if (res.data.code === 0) {
                        this.detailsList.forEach(function (value, index) {
                            for (let i in res.data.data) {
                                if (value.name === i) {
                                    value.value = res.data.data[i];
                                }
                            }
                        });
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        getChart: function () {
            this.loadingCharts = true;
            this.chartStatistics.show = false;
            this.chartStatistics.date = [];
            this.chartStatistics.dataArr = [];
            let networkId = localStorage.getItem("networkId");
            getChartData(networkId)
                .then(res => {
                    this.loadingCharts = false;
                    if (res.data.code === 0) {
                        let resData = changWeek(res.data.data);
                        for (let i = 0; i < resData.length; i++) {
                            this.chartStatistics.date.push(resData[i].day);
                            this.chartStatistics.dataArr.push(
                                resData[i].transCount
                            );
                        }
                        this.$set(this.chartStatistics, "show", true);
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        getNodeTable: function () {
            this.loadingNodes = true;
            let networkId = localStorage.getItem("networkId");
            let reqString = `${networkId}/1/100`;
            let reqData = {
                networkId: networkId,
                pageNumber: 1,
                pageSize: 4
            },
                reqQuery = {};
            getNodeList(reqData, reqQuery)
                .then(res => {
                    this.loadingNodes = false;
                    if (res.data.code === 0) {
                        this.total = res.data.totalCount;
                        this.nodeData = res.data.data || [];
                        this.nodeData.forEach((value, index) => {
                            if (value.status === 0) {
                                value.value = "运行";
                            } else if (value.status === 1) {
                                value.value = "停止";
                            }
                        });
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
        getBlockList: function () {
            this.loadingBlock = true;
            this.searchLoading = true;
            let networkId = localStorage.getItem("networkId");
            let reqData = {
                networkId: networkId,
                pageNumber: 1,
                pageSize: 6
            };
            getBlockPage(reqData, this.reqQuery)
                .then(res => {
                    this.loadingBlock = false;
                    this.searchLoading = false;
                    if (res.data.code === 0) {
                        this.blockData = res.data.data;
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
        getTransaction: function () {
            this.loadingTransaction = true;
            this.searchLoading = true;
            let networkId = localStorage.getItem("networkId");
            let reqData = {
                networkId: networkId,
                pageNumber: 1,
                pageSize: 6
            };
            getTransactionList(reqData, this.reqQuery)
                .then(res => {
                    this.loadingTransaction = false;
                    this.searchLoading = false;
                    if (res.data.code === 0) {
                        this.transactionList = res.data.data;
                    } else {
                        this.$message({
                            message: this.$chooseLang(res.data.code),
                            type: "error",
                            duration: 2000
                        });
                    }
                })
                .catch(err => {
                    this.$message.error(this.$t('text.systemError'));
                });
        },
        bindSvg(item) {
            var str = "";
            switch (item.name) {
                case "orgCount":
                    str = "#wbs-icon-h-group";
                    break;

                case "nodeCount":
                    str = "#wbs-icon-h-nodes";
                    break;
                case "contractCount":
                    str = "#wbs-icon-h-deploy";
                    break;
                case "latestBlock":
                    str = "#wbs-icon-h-block";
                    break;
                case "transactionCount":
                    str = "#wbs-icon-transaction";
                    break;
            }
            return str;
        },
        textColor: function (val) {
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
        nodesStatus: function (val) {
            let transString = "";
            switch (val) {
                case 1:
                    transString = "运行";
                    break;
                case 2:
                    transString = "停止";
                    break;
            }
            return transString;
        },
        goRouter: function (val) {
            switch (val) {
                case "blocks":
                    router.push("blockInfo");
                    break;

                case "transactions":
                    router.push("transactionInfo");
                    break;
            }
        },
        submit: function () {
            if (!this.keyword) return;
            this.reqQuery = {};
            this.getHomeSearch();
            // this.transactionList = [];
            // this.blockData = [];
            // if(this.keyword.length === 66) {
            //     this.reqQuery.transactionHash = this.keyword.trim()
            //     this.getTransaction()
            // }else {
            //     this.reqQuery.blockNumber = this.keyword.trim()
            //     this.getBlockList()
            // }
        },
        link: function (val) {
            // return;
            // router.push({
            //     path: "/blockInfo",
            //     query: {
            //         blockNumber: val
            //     }
            // });
        },
        clickTable: function (row, column, $event) {
            let nodeName = $event.target.nodeName;
            if (nodeName === "I") {
                return;
            }
            this.$refs.refTable.toggleRowExpansion(row);
        },
        copyPubilcKey(val) {
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
.block-bg {
    background: linear-gradient(to top right, #47befa, #37eef2);
}
.node-bg {
    background: linear-gradient(to top right, #466dff, #30a7ff);
}
.transation-bg {
    background: linear-gradient(to top right, #736aff, #b287ff);
}
.transationPendding-bg {
    background: linear-gradient(to top right, #ff6e9a, #ffa895);
}
.over-view-wrapper {
    background: #20293c;
}
.amount-wrapper {
    margin: 30px 30px 0 31px;
}
.font-12 {
    font-size: 12px;
    color: #9da2ab;
}
.part1-content {
    display: flex;
    background: #20293c;
    flex-direction: row;
    flex-wrap: nowrap;
    justify-content: space-between;
}
.overview-number {
    /* width: 23%; */
    /* max-width: 280px; */
    margin-top: 20px;
    margin-left: 20px;
    padding: 16px 6px;
    font-size: 0;
    color: #fff;
}
.part1-content-amount {
    display: inline-block;
    width: 41%;
    max-width: 114px;
    vertical-align: middle;
}
.part1-content-amount img {
    width: 100%;
    max-width: 114px;
}
.part2-content-amount {
    font-size: 14px;
    display: inline-block;
    width: 59%;
    max-width: 90px;
    vertical-align: middle;
}
.part2-content-amount-number {
    display: inline-block;
    width: 100%;
}
.part2-title {
    padding: 22px 31px 26px 32px;
}
.part2-title::after {
    display: block;
    content: "";
    clear: both;
}
.part2-title-left {
    float: left;
    font-size: 16px;
    color: #000e1f;
    font-weight: bold;
}
.part2-title-right {
    float: right;
    font-size: 12px;
    color: #727476;
    padding: 2px 12px;
    border-radius: 20px;
    background: #f6f6f6;
}
.part3-title {
    padding: 40px 60px 40px 40px;
}
.part3-title::after {
    display: block;
    content: "";
    clear: both;
}
.more-content {
    font-size: 14px;
    color: #2d5f9e;
    cursor: pointer;
}
.part3-table-content {
    width: 100%;
    padding: 0 39px 48px 40px;
    font-size: 12px;
}
.part3-table-content >>> th,
.part3-table-content >>> td {
    padding: 8px 0;
}
.part1-details-bottom {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
}
.part1-details-arrow-right {
    position: relative;
    top: 4px;
}
.search-table-content {
    width: 100%;
}
.search-table-content >>> th {
    background: #fafafa;
    color: #00122c;
}
.search-table-content >>> th,
.search-table-content >>> td {
    font-size: 14px;
}
.overview-wrapper {
    background: #fff;
    font-size: 15px;
    box-shadow: 0 4px 12px 0 #dfe2e9;
    border-radius: 2px;
}
.overview-wrapper > p {
    padding: 26px 18px 0px 22px;
    border-bottom: 1px solid #f2f2f2;
    display: flex;
    justify-content: space-between;
}
.overview-title {
    font-size: 15px;
    color: #2e384d;
    padding-bottom: 22px;
    border-bottom: 2px solid #2e384d;
}
.overview-more {
    font-size: 14px;
    color: #2fcdd1;
}
.block-item {
    display: flex;
    flex-flow: row;
    justify-content: space-between;
    padding-bottom: 10px;
}
.block-amount {
    display: flex;
    flex-flow: column;
}
.overview-item-base {
    margin: 26px 18px 30px 22px;
}
.block-miner {
    display: flex;
    flex-flow: row wrap;
}
.block-miner > p {
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-left: 10px;
}
.trans-hash {
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
}
.node-ip {
    color: #2d5f9e;
}
.block-table-content >>> .el-table__row {
    cursor: pointer;
}
.json-view{
    max-height: 550px;
    overflow-y: scroll;
}
</style>