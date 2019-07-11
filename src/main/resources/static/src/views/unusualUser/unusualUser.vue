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
    <div style="height:100%">
        <content-head :headTitle="'交易审计'" :headSubTitle="'异常用户'"></content-head>
        <div class="module-wrapper auto-wrapper">
            <div class="search-part">
                <div class="search-part-right">
                    <el-input placeholder="请输入用户" v-model="userName" class="input-with-select" @clear="clearText">
                        <el-button slot="append" icon="el-icon-search" @click="search"></el-button>
                    </el-input>
                </div>
            </div>
            <div class="search-table" style="height: 76%">
                <el-table :data="hashData" tooltip-effect="light" ="100%" style="overflow-y: auto; height:100%" v-loading="loading">
                    <el-table-column type="expand">
                        <template slot-scope="props">
                            <div class="expand-item">
                                <ul class="expand-item-ul">
                                    <li v-for="item in props.row['hashs']">
                                    <div @click="showItem(item)"  class="expand-item-div">
                                        <i :class="item.show?'el-icon-arrow-down':'el-icon-arrow-up'"></i>
                                        <span class="expand-item-span">TxHash：
                                            <span>{{item.hash}}</span>
                                        </span>
                                    </div>
                                    <transaction-detail :transHash="item.hash" v-if="item.show"></transaction-detail>
                                </li>
                                </ul>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column v-for="head in unusualUserHead" :label="head.name" :key="head.enName" show-overflow-tooltip align="center" v-if="head.enName!=='hashs'">
                        <template slot-scope="scope">
                            <span>{{scope.row[head.enName]}}</span>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>
            </div>
        </div>
    </div>
</template>

<script>
import { unusualUserList } from "@/util/api";
import contentHead from "@/components/contentHead";
import transactionDetail from "@/components/transactionDetail";
import errcode from "@/util/errcode";
export default {
    name: "unusualUser",
    components: {
        contentHead,
        transactionDetail
    },
    data() {
        return {
            loading: false,
            currentPage: 1,
            pageSize: 10,
            total: 0,
            userName:'',
            unusualUserHead: [
                {
                    enName: "userName",
                    name: "用户名称"
                },
                {
                    enName: "transCount",
                    name: "交易量"
                },
                {
                    enName: "time",
                    name: "最新交易时间"
                },
                {
                    enName: "hashs",
                    name: "哈希"
                }
            ],
            unusualUserList: []
        };
    },
    computed: {
        hashData() {
            var arr = this.unusualUserList,
                list = [];
            arr.forEach(item => {
                item.hashs = item["hashs"].split(",");
            });
            arr.forEach(item => {
                let hashArr = [];
                for (let i = 0; i < item.hashs.length; i++) {
                    let obj = {};
                    if (i === 0) {
                        obj.hash = item.hashs[i];
                        obj.show = true;
                    } else {
                        obj.hash = item.hashs[i];
                        obj.show = false;
                    }
                    hashArr.push(obj);
                }
                item.hashs = hashArr;
            });
            return arr;
        }
    },
    mounted() {
        this.getUnusualUserList();
    },
    methods: {
        getUnusualUserList() {
            this.loading = true;
            let networkId = localStorage.getItem("networkId");
            let reqData = {
                    networkId: networkId,
                    pageNumber: 1,
                    pageSize: 10
                },
                reqQuery = {};
            reqQuery = {
                userName: this.userName
            };
            unusualUserList(reqData, reqQuery)
                .then(res => {
                    if (res.data.code === 0) {
                        this.unusualUserList = res.data.data;
                        this.total = res.data.totalCount;
                        this.loading = false;
                    } else {
                        this.loading = false;
                        this.$message({
                            message: errcode.errCode[res.data.code].cn,
                            type: "error",
                            duration: 2000
                        });
                    }
                })
                .catch(err => {
                    this.loading = false;
                    this.$message({
                        message: "查询异常用户失败！",
                        type: "error",
                        duration: 2000
                    });
                });
        },
        hashList(val) {
            let list = [];
            list = val.split(",");
            return list;
        },
        showItem(val) {
            this.$set(val, "show", !val.show);
        },
        search() {
            this.getUnusualUserList();
        },
        handleSizeChange: function(val) {
            this.pageSize = val;
            this.currentPage = 1;
            this.getUnusualUserList();
        },
        handleCurrentChange: function(val) {
            this.currentPage = val;
            this.getUnusualUserList();
        },
        clearText: function(){
            this.getUnusualUserList()
        }
    }
};
</script>

<style scoped>
.search-part-right {
    float: right;
    width: 464px;
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


.search-table>>>.el-table__expanded-cell[class*="cell"] {
    padding-left: 20px;
    padding-top: 0;
}
.search-table>>> .el-table__body-wrapper {
    overflow-y: auto;
}

.expand-item-div {
    display: block;
    cursor: context-menu;
    padding: 17px 0;
    border-bottom: 1px dashed #e8e8e8;
    border-top: 1px dashed #e8e8e8;
    margin-bottom: 5px;
    padding-left: 21px;
}

.expand-item-div:first-child {
    border-top: none;
}

.expand-item-div:hover {
    background: #f3f5f9;
}
.expand-item-span {
    margin-left: 26px;
    font-size: 12px;
    color: #00122c;
}
.expand-item-span > span {
    color: #515356;
    margin-left: 47px;
}
</style>
