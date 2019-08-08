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
    <div class="contract-menu">
        <div class="contract-menu-header">
            <span class="contract-icon" @click="editContract">
                <i class="wbs-icon-xinjian"></i>
                <span>新建</span>
            </span>
            <span class="contract-icon" @click="newContract">
                <i class="wbs-icon-shangchuan"></i>
                <span>上传</span>
            </span>
        </div>
        <ul class="contract-menus">
            <li class="contract-menu-list" v-for="(item,key) in contractList" @click="checkCode(item,key)" v-if='item.contractType == 0'>
                <span class="contract-menu-item bg-cecece" v-if="item.contractStatus === 0"></span>
                <span class="contract-menu-item bg-6d6d6d" v-if="item.contractStatus === 1"></span>
                <span class="contract-menu-item bf-58cb7d" v-if="item.contractStatus === 2"></span>
                <span class="contract-menu-item bg-ed5454" v-if="item.contractStatus === 3"></span>
                <el-tooltip :content="item.contractStatus | Contractstate" placement="top">
                    <span class="contract-menu-content" :class="key == index ? 'active' : ''">{{item.contractName + '.sol'}}</span>
                </el-tooltip>
                <span class="contract-menu-list-version" :class="key == index ? 'active' : ''">{{item.contractVersion}}</span>
                <i class="wbs-icon-delete contract-menu-handle" @click="deleteContract(item)" v-if="item.contractStatus != 2 && item.contractStatus != 0"></i>
            </li>
            <el-tooltip content="下一页" placement="top" v-if="(allTotal - total) > pageSize">
                <div class="text-center" style="margin-top:2px;margin-bottom: 10px;">
                    <el-button size="mini" @click="load">
                        点击查看更多
                        <i class="el-icon-download"></i>
                    </el-button>
                </div>
            </el-tooltip>
            <div class="text-center font-color-9da2ab font-12" style="padding: 5px 0" v-else>暂无更多</div>
        </ul>
        <v-dialog v-if="dialogShow" :dialogShow="dialogShow" @close="closeModel($event)" @success="submitModel($event)"></v-dialog>
        <v-addChainCode v-if="addDialogShow" :addDialog="addDialogShow" @close="closeModel($event)" @change="addChange($event)"></v-addChainCode>
    </div>
</template>

<script>
let Base64 = require("js-base64").Base64;
import { getContractList, deleteCode } from "@/util/api";
import newchaincode from "../dialog/newChaincode";
import addChainCode from "../dialog/addchaincode-dialog";
import constant from "@/util/constant";
import errcode from "@/util/errcode";

export default {
    name: "contractMenu",
    components: {
        "v-dialog": newchaincode,
        "v-addChainCode": addChainCode
    },
    props: ["value"],
    data: function () {
        return {
            contractList: [],
            dialogShow: false,
            addDialogShow: false,
            index: -1,
            pageSize: 15,
            currentPage: 1,
            total: 0,
            allTotal: 0
        };
    },
    beforeCreate: function () {
        localStorage.setItem("tipShow", true);
        localStorage.setItem("codeShow", false);
    },
    mounted: function () {
        this.$nextTick(function () {
            this.getContractList();
        });
    },
    methods: {
        load: function () {
            this.pageSize = this.pageSize + 10;
            this.getContractList();
        },
        getContractList: function (val) {
            let reqData = {
                groupId: localStorage.getItem("groupId"),
                pageNumber: this.currentPage,
                pageSize: this.pageSize
            };
            getContractList(reqData)
                .then(res => {
                    if (res.data.code === 0) {
                        this.total = 0;
                        this.allTotal = res.data.totalCount;
                        this.contractList = res.data.data || [];
                        localStorage.setItem(
                            "contractList",
                            JSON.stringify(this.contractList)
                        );
                        if (val) {
                            this.checkVal(val, this.contractList);
                        }
                        if (this.contractList.length) {
                            this.contractList.forEach(value => {
                                if (value.contractType == 1) {
                                    this.total++;
                                }
                            });
                        }
                    } else {
                        this.$message({
                            message: errcode.errCode[res.data.code].cn,
                            type: "error"
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        message: this.$t('text.systemError'),
                        type: "error"
                    });
                });
        },
        deleteCode: function (val) {
            let contractId = val.contractId;
            deleteCode(contractId)
                .then(res => {
                    if (res.data.code === 0) {
                        this.$message({
                            message: "合约删除成功！",
                            type: "success"
                        });
                        localStorage.setItem("tipShow", true);
                        localStorage.setItem("codeShow", false);
                        this.getContractList();
                    } else {
                        this.$message({
                            message: errcode.errCode[res.data.code].cn,
                            type: "error"
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        message: this.$t('text.systemError'),
                        type: "error"
                    });
                });
        },
        deleteContract: function (val) {
            this.$confirm("此操作将永久删除该文件, 是否继续?", "删除合约", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                confirmButtonClass: "sure-btn",
                cancelButtonClass: "close-btn"
            })
                .then(() => {
                    this.deleteCode(val);
                })
                .catch(() => {
                    return false;
                });
        },
        newContract: function () {
            this.dialogShow = true;
        },
        editContract: function () {
            this.addDialogShow = true;
        },
        closeModel: function (val) {
            this.dialogShow = val;
            this.addDialogShow = val;
        },
        submitModel: function (val) {
            this.getContractList(val);
        },
        checkCode: function (val, key) {
            this.$emit("change", val);
            this.index = key;
            localStorage.setItem("tipShow", false);
            localStorage.setItem("codeShow", true);
        },
        addChange: function (val) {
            this.contractList.unshift(val);
            localStorage.getItem(
                "contractList",
                JSON.stringify(this.contractList)
            );
            this.checkCode(val, 0);
        },
        saveContact: function (val) {
            this.getContractList(val);
        },
        handleCurrentChange: function (val) {
            this.currentPage = val;
            this.getOrgTable();
        },
        checkVal: function (val, data) {
            if (data.length > 0) {
                data.forEach((value, index) => {
                    if (value.contractId === val.contractId) {
                        this.checkCode(value, index);
                    }
                });
            } else {
                return;
            }
        }
    },
    filters: {
        Contractstate: function (value) {
            return constant.STATES[value];
        }
    }
};
</script>
<style scoped>
.contract-menu {
    height: 100%;
    font-size: 14px;
    background-color: #fff;
    border-left: 1px solid #eceef2;
    box-sizing: border-box;
    padding-top: 0 !important;
}
.contract-menu-header {
    width: calc(100% + 1px);
    height: 48px;
    line-height: 48px;
    border-bottom: 2px solid #e7ebf0;
    border-right: 1px solid #e7ebf0;
    text-align: left;
}
.contract-icon {
    padding-left: 20px;
    font-size: 12px;
    letter-spacing: 0;
    text-align: left;
    cursor: pointer;
}
.contract-icon-right {
    padding-right: 10px;
    font-size: 12px;
    letter-spacing: 0;
    float: right;
    cursor: pointer;
}
.contract-icon span {
    color: #9da2ab;
    cursor: pointer;
    vertical-align: middle;
    font-size: 12px;
}
.contract-icon-right i {
    font-size: 16px;
    vertical-align: middle;
}
.contract-menus {
    padding-top: 10px;
    height: calc(100% - 60px);
    overflow-y: auto;
    overflow-x: hidden;
    text-align: left;
}
.contract-menu-item {
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    vertical-align: 2px;
}
.contract-menu-list {
    width: 100%;
    height: 45px;
    padding: 2px 6px 10px 8px;
    line-height: 20px;
    text-align: left;
    overflow: hidden;
    border-bottom: 1px solid #eee;
    box-sizing: border-box;
}
.contract-menu-list:hover i {
    visibility: visible;
}
.contract-menu-content {
    display: inline-block;
    line-height: 14px;
    width: calc(100% - 18px);
    padding: 0 0 0 2px;
    cursor: pointer;
    font-weight: bold;
    font-size: 14px;
    color: #737a86;
}
.contract-menu-handle {
    float: right;
    padding-right: 2px;
    cursor: pointer;
    font-size: 16px;
    vertical-align: middle;
}
.active {
    color: #2956a3 !important;
}
.icon {
    float: right;
    visibility: hidden;
    padding-right: 25px;
    line-height: 32px;
    padding-top: 10px;
    cursor: pointer;
}
.contract-menu-list-version {
    display: inline-block;
    padding: 0 0 0 10px;
    font-size: 12px;
    color: #9da2ab;
}
.el-button--small,
.el-button--small.is-round {
    padding: 11px 15px;
}
</style>
<style>
.close-btn {
    background: #2956a3;
    border-color: #2956a3;
    color: #fff;
}
.sure-btn {
    background: #fff !important;
    border-color: #9da2ab !important;
    color: #9da2ab !important;
}
</style>

