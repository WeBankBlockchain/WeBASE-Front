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
    <div class="account-wrapper">
        <content-head :headTitle="'账户管理'"></content-head>
        <div class="module-wrapper">
            <div class="search-part">
                <div class="search-part-left">
                    <el-button type="primary" class="search-part-left-btn" @click="creatAccount">新增账号</el-button>
                </div>
            </div>
            <div class="search-table">
                <el-table :data="accountList" tooltip-effect="light" v-loading="loading">
                    <el-table-column v-for="head in accountHeader" :label="head.name" :key="head.enName" show-overflow-tooltip align="center">
                        <template slot-scope="scope">
                            <template v-if="head.enName!='operate'">
                                <span>{{scope.row[head.enName]}}</span>
                            </template>
                            <template v-else>
                                <el-button type="text" size="small" @click="deleteAccount(scope.row,'delete')" style="color:#ed5454">删除</el-button>
                                <el-button type="text" size="small" @click="modifyAccount(scope.row,'modify')">修改</el-button>
                            </template>
                        </template>

                    </el-table-column>
                </el-table>
                <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>

                <el-dialog :visible.sync="accountDialogVisible" :title="accountDialogTitle" width="433px" :append-to-body="true" :center="true" class="dialog-wrapper" v-if="accountDialogVisible">
                    <account-dialog :accountDialogOptions="accountDialogOptions" @success="success" @close="close"></account-dialog>
                </el-dialog>
                
            </div>

        </div>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import { accountList, modifyAccountInfo } from "@/util/api";
import accountDialog from "./components/accountDialog";
export default {
    name: "accountList",
    components: {
        contentHead,
        accountDialog
    },
    data() {
        return {
            accountName: "",
            loading: false,
            currentPage: 1,
            pageSize: 10,
            total: 0,
            accountDialogVisible: false,
            accountDialogTitle: '',
            accountDialogOptions: {},
            accountHeader: [
                {
                    enName: "account",
                    name: "帐号"
                },
                {
                    enName: "roleNameZh",
                    name: "角色"
                },
                {
                    enName: "operate",
                    name: "操作"
                }
            ],
            accountList: []
        };
    },
    mounted() {
        this.getAccountList();
    },
    methods: {
        success(){
            this.getAccountList()
        },
        close(val){
            this.accountDialogVisible = val
        },
        getAccountList() {
            this.loading = true;
            let reqData = {
                    pageNumber: this.currentPage,
                    pageSize: this.pageSize
                },
                reqQuery = {
                    account: ''
                };
            accountList(reqData, reqQuery)
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        this.accountList = res.data.data || [];
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
                        message: this.$chooseLang(res.data.code)
                    });
                });
        },
        handleSizeChange: function(val) {
            this.pageSize = val;
            this.currentPage = 1;
            this.getAccountList();
        },
        handleCurrentChange: function(val) {
            this.currentPage = val;
            this.getAccountList();
        },
        creatAccount(){
            this.accountDialogVisible = true
            this.accountDialogTitle = '创建帐号'
            this.accountDialogOptions = {
                type:'creat',
                data: {
                    account: '',
                    role: ''
                }
            }
        },
        deleteAccount(val, type) {
            this.accountDialogOptions = {
                type: type,
                data: val
            }
            this.accountDialogVisible = true
            this.accountDialogTitle = '删除帐号'
            
        },
        modifyAccount(val, type) {
            this.accountDialogOptions = {
                type: type,
                data: val
            }
            this.accountDialogVisible = true
            this.accountDialogTitle = '修改帐号'
        }
    }
};
</script>

<style scoped>
@import "./accountInfo.css";
</style>
