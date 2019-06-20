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
    <div class="rivate-key-management-wrapper">
        <v-contentHead :headTitle="'私钥管理'" @changeGroup="changeGroup"></v-contentHead>
        <div class="module-wrapper" style="padding-bottom: 20px;">
            <div class="search-part">
                <div style="display: flex;">
                    <el-button type="primary" class="search-part-left-btn" @click="creatUserBtn">新增用户</el-button>
                    <span class="fileinput-button">
                        <span>导入私钥</span>
                        <input type="file" @change="importFile($event)" />
                    </span>

                </div>
            </div>
            <div class="search-table">
                <el-table :data="privateKeyList" tooltip-effect="dark" v-loading="loading">
                    <el-table-column v-for="head in privateKeyHead" :label="head.name" :key="head.enName" show-overflow-tooltip :width="head.tdWidth" align="center">
                        <template slot-scope="scope">
                            <template v-if="head.enName!='operate'">
                                <span v-if="head.enName ==='address'">
                                    <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row[head.enName])" title="复制地址"></i>
                                    {{scope.row[head.enName]}}
                                </span>
                                <span v-else-if="head.enName ==='publicKey'">
                                    <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row[head.enName])" title="复制公钥"></i>
                                    {{scope.row[head.enName]}}
                                </span>
                                <span v-else>{{scope.row[head.enName]}}</span>
                            </template>
                            <template v-else>
                                <el-button type="text" size="small" @click="exportFile(scope.row)">导出</el-button>
                            </template>
                        </template>

                    </el-table-column>

                </el-table>
                <!-- <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination> -->
            </div>
        </div>
        <el-dialog :visible.sync="creatUserNameVisible" title="添加用户名" width="400px" class="dialog-wrapper" center v-if="creatUserNameVisible" @close="closeCallback">
            <el-form ref="userForm" :rules="rules" :model="userForm">
                <el-form-item label="" prop="userName">
                    <el-input v-model="userForm.userName" placeholder="请输入用户名"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="text-right">
                <el-button @click="closeUserName">取 消</el-button>
                <el-button type="primary" @click="sureUserName('userForm')">确 定</el-button>
            </div>
        </el-dialog>

    </div>
</template>


<script>
import contentHead from "@/components/contentHead";
import { queryCreatePrivateKey, queryImportPrivateKey } from "@/util/api";
import { unique } from "@/util/util";
import errcode from "@/util/errcode";
let Base64 = require("js-base64").Base64;
const FileSaver = require("file-saver");
import Bus from "@/bus";
export default {
    name: "RivateKeyManagement",
    components: {
        "v-contentHead": contentHead
    },
    data() {
        return {
            userForm: {
                userName: "",
            },
            loading: false,
            creatUserNameVisible: false,
            privateKeyList: localStorage.getItem("privateKeyList") ? JSON.parse(localStorage.getItem("privateKeyList")) : [],
            privateKeyHead: [
                {
                    enName: "address",
                    name: "地址",
                    tdWidth: ''
                },
                {
                    enName: "publicKey",
                    name: "公钥",
                    tdWidth: ''
                },
                {
                    enName: "userName",
                    name: "用户",
                    tdWidth: ""
                },
                {
                    enName: "operate",
                    name: "操作",
                    tdWidth: 80
                }
            ],
            fileString: "",
            uploadMap: {},
            rules: {
                userName: [
                    {
                        required: true,
                        message: "请输入用户名",
                        trigger: "blur"
                    },
                    {
                        // pattern: /^\S.*\S*$/g,
                        pattern: /^[A-za-z0-9]+$/,
                        message: "只能是数字或者字母组成",
                        trigger: "blur",

                    },
                    {
                        trigger: "blur",
                        min: 3,
                        max: 32,
                        message: "长度在 3 到 32 个字符",
                    }
                ]
            },
            groupId: localStorage.getItem("groupId") || null,
        };
    },
    beforeDestroy: function () {
        Bus.$off("changeGroup")
    },
    mounted() {
        Bus.$on("changeGroup", data => {
            this.changeGroup(data)
        })
    },
    methods: {
        changeGroup(val) {
            this.groupId = val;
        },
        creatUserBtn() {
            this.creatUserNameVisible = true;
        },
        initUserName() {
            this.userForm = { userName: "" }
        },
        closeCallback() {
            this.initUserName()
        },
        closeUserName() {
            this.creatUserNameVisible = false;
            this.initUserName()
        },
        sureUserName(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.creatUserNameVisible = false;
                    this.addUser()
                } else {
                    return false;
                }
            });

        },
        addUser: function () {
            queryCreatePrivateKey({}, { useAes: false })
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        Object.assign(data, this.userForm);
                        this.privateKeyList.unshift(data);
                        this.privateKeyList = unique(this.privateKeyList, 'privateKey');
                        localStorage.setItem("privateKeyList", JSON.stringify(this.privateKeyList));
                        this.$message({
                            type: "success",
                            message: "新增成功"
                        });
                    } else {
                        this.$message({
                            type: "error",
                            message: data.errorMessage || "系统错误"
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: "系统错误"
                    });
                });
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
        exportFile(params) {
            let str = JSON.stringify(params);
            var blob = new Blob([str], { type: "text;charset=utf-8" });
            FileSaver.saveAs(blob, params.userName);
        },

        importFile(e) {
            if (!e.target.files.length) {
                return;
            }
            this.fileString = "";
            let files = e.target.files[0];
            let filessize = Math.ceil(files.size / 1024);
            let reader = new FileReader(); //新建一个FileReader
            reader.readAsText(files, "UTF-8"); //读取文件
            let _this = this;
            reader.onload = function (evt) {
                _this.fileString = evt.target.result; // 读取文件内容
                try {
                    let reqQuery = {
                        privateKey: Base64.encode(JSON.parse(_this.fileString).privateKey)
                    };
                    queryImportPrivateKey({}, reqQuery)
                        .then(res => {
                            const { data, status } = res;
                            if (status === 200) {
                                localStorage.setItem("keyInfo", JSON.stringify(data));
                                _this.uploadMap = JSON.parse(_this.fileString);
                                _this.privateKeyList.unshift(_this.uploadMap);
                                _this.privateKeyList = unique(_this.privateKeyList, 'privateKey')
                                localStorage.setItem("privateKeyList", JSON.stringify(_this.privateKeyList));

                                _this.$message({
                                    type: 'success',
                                    message: '导入成功'
                                })
                            } else {
                                _this.$message({
                                    type: "error",
                                    message: errcode.errCode[res.data.code].cn || "系统错误"
                                });
                            }
                        })
                        .catch(err => {
                            _this.$message({
                                type: "error",
                                message: "系统错误"
                            });
                        });

                } catch (error) {
                    _this.$message({
                        type: 'error',
                        message: '导入失败'
                    })
                }
            };
        },
    }
};
</script>
<style scoped>
@import "./rivateKeyManagement.css";
</style>
