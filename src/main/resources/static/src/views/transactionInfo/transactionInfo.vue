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
    <div class="bg-f7f7f7">
        <v-content-head :headTitle="'交易信息'" :icon="true"></v-content-head>
        <div class="module-wrapper">
            <div class="search-part">
                <div class="search-part-left-bg">
                    <span>共</span>
                    <span>{{numberFormat(total, 0, ".", ",")}}</span>
                    <span>条</span>
                </div>
                <div class="search-part-right">
                    <el-input placeholder="请输入交易哈希或块高" v-model="searchKey.value" class="input-with-select" @clear="clearText">
                        <el-button slot="append" icon="el-icon-search" @click="search"></el-button>
                    </el-input>
                </div>
            </div>
            <div class="search-table">
                <el-table  :data="transactionList" class="block-table-content" :row-key="getRowKeys" :expand-row-keys="expands" v-loading="loading" @row-click="clickTable" ref="refTable">
                    <el-table-column type="expand" align="center">
                        <template slot-scope="scope">
                            <v-transaction-detail :transHash="scope.row.transHash"></v-transaction-detail>
                        </template>
                    </el-table-column>
                    <el-table-column prop="transHash" label="交易哈希" align="center" :show-overflow-tooltip="true">
                        <template slot-scope="scope">
                            <span>
                                <i class="wbs-icon-copy font-12 copy-key" @click="copyPubilcKey(scope.row['transHash'])" title="复制哈希"></i>
                                {{scope.row['transHash']}}
                            </span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="blockNumber" label="块高" width="260" align="center" :show-overflow-tooltip="true">
                        <template slot-scope="scope">
                            <span>{{scope.row['blockNumber']}}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="blockTimestamp" label="创建时间" width="280" :show-overflow-tooltip="true" align="center">
                        <template slot-scope="scope">
                            <span>{{scope.row['blockTimestamp']}}</span>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout=" sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>
            </div>
        </div>
    </div>
</template>
<script>
import contentHead from "@/components/contentHead";
import transactionDetail from "@/components/transactionDetail";
import { getTransactionList } from "@/util/api";
import router from "@/router";
import errcode from "@/util/errcode";
import { numberFormat } from "@/util/util";

export default {
    name: "transaction",
    components: {
        "v-content-head": contentHead,
        "v-transaction-detail": transactionDetail
    },
    data: function() {
        return {
            transactionList: [],
            expands: [],
            searchKey: {
                key: "",
                value: ""
            },
            currentPage: 1,
            pageSize: 10,
            total: 0,
            loading: false,
            numberFormat: numberFormat,
            getRowKeys: function(row) {
                return row.transHash;
            }
        };
    },
    mounted: function() {
        if (this.$route.query.blockNumber) {
            this.searchKey.key = "blockNumber";
            this.searchKey.value = this.$route.query.blockNumber;
        }
        this.getTransaction();
    },
    methods: {
        search: function() {
            if (
                this.searchKey.key == "transactionHash" &&
                this.searchKey.value.length != 66 &&
                this.searchKey.value
            ) {
                this.$message({
                    message: "搜索交易哈希不支持模糊查询",
                    type: "error",
                    duration: 2000
                });
            } else {
                this.currentPage = 1
                this.getTransaction();
            }
        },
        getTransaction: function() {
            this.expands = [];
            this.loading = true;
            let networkId = localStorage.getItem("networkId");
            let reqData = {
                    networkId: networkId,
                    pageNumber: this.currentPage,
                    pageSize: this.pageSize
                },
                reqQuery = {};
            if (this.searchKey.value) {
                if (this.searchKey.value.length === 66) {
                    reqQuery.transactionHash = this.searchKey.value;
                } else {
                    reqQuery.blockNumber = this.searchKey.value;
                }
            }
            getTransactionList(reqData, reqQuery)
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        this.transactionList = res.data.data;
                        this.total = res.data.totalCount;
                        // if (this.transactionList.length) {
                        //     this.expands.push(this.transactionList[0].transHash);
                        // }
                    } else {
                        this.$message({
                            message: errcode.errCode[res.data.code].cn,
                            type: "error",
                            duration: 2000
                        });
                    }
                })
                .catch(err => {
                    this.loading = false;
                    this.$message.error(this.$t('text.systemError'));
                });
        },
        handleSizeChange: function(val) {
            this.pageSize = val;
            this.currentPage = 1;
            this.getTransaction();
        },
        handleCurrentChange: function(val) {
            this.currentPage = val;
            this.getTransaction();
        },
        clickTable: function(row, $event, column) {
            let nodeName = $event.target.nodeName;
            if ($event.target.nodeName === "I") {
                return
            }
            this.$refs.refTable.toggleRowExpansion(row);
        },
        clearText: function() {
            this.getTransaction();
        },
        copyPubilcKey(val) {
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
.block-table-content {
    width: 100%;
    padding-bottom: 16px;
    font-size: 12px;
}
.block-table-content>>>.el-table__expanded-cell {
    padding: 12px 6px;
}
.block-table-content>>>.el-table__expand-icon > .el-icon {
    font-size: 14px;
}
.block-table-content>>>.el-table__row {
    cursor: pointer;
}
.search-part {
    padding: 30px 0px;
    overflow: hidden;
    margin: 0;
}
.search-part::after {
    display: block;
    content: "";
    clear: both;
}
.input-with-select {
    width: 610px;
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
.input-with-select>>>.el-input__inner {
    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    border: 1px solid #eaedf3;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
.input-with-select>>>.el-input--suffix > .el-input__inner {
    box-shadow: none;
}
.input-with-select>>>.el-input-group__prepend {
    border-left-color: #fff;
}
.input-with-select>>>.el-input-group__append {
    border-top-right-radius: 20px;
    border-bottom-right-radius: 20px;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
</style>

