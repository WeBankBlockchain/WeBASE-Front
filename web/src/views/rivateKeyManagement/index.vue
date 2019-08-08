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
        <v-contentHead :headTitle="$t('route.privateKeyManagementQ')"></v-contentHead>
        <div class="module-wrapper" style="padding-bottom: 20px;">
            <div class="search-part">
                <div style="display: flex;">
                    <el-button type="primary" class="search-part-left-btn" @click="creatUserBtn">{{$t('table.addUser')}}</el-button>
                    <span class="fileinput-button">
                        <span>{{$t('table.importPrivateKey')}}</span>
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
                                    <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row[head.enName])" :title="$t('title.copyAddress')"></i>
                                    {{scope.row[head.enName]}}
                                </span>
                                <span v-else-if="head.enName ==='publicKey'">
                                    <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row[head.enName])" :title="$t('title.copyPubliceKey')"></i>
                                    {{scope.row[head.enName]}}
                                </span>
                                <span v-else>{{scope.row[head.enName]}}</span>
                            </template>
                            <template v-else>
                                <el-button type="text" size="mini" @click="exportFile(scope.row)">{{$t('table.export')}}</el-button>
                                <el-button type="text" size="mini" @click="deleteFile(scope.row)">{{$t('table.delete')}}</el-button>
                            </template>
                        </template>

                    </el-table-column>

                </el-table>
                <!-- <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination> -->
            </div>
        </div>
        <el-dialog :visible.sync="creatUserNameVisible" :title="$t('dialog.addUsername')" width="400px" class="dialog-wrapper" center v-if="creatUserNameVisible" @close="closeCallback">
            <el-form ref="userForm" :rules="rules" :model="userForm">
                <el-form-item label="" prop="userName">
                    <el-input v-model="userForm.userName" :placeholder="$t('dialog.pleaseEnterUserName')"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="text-right">
                <el-button @click="closeUserName">{{$t('table.cancel')}}</el-button>
                <el-button type="primary" @click="sureUserName('userForm')">{{$t('table.confirm')}}</el-button>
            </div>
        </el-dialog>

    </div>
</template>


<script>
import contentHead from "@/components/contentHead";
import { queryCreatePrivateKey, queryImportPrivateKey, queryLocalKeyStores, queryDeletePrivateKey } from "@/util/api";
import { unique } from "@/util/util";
const FileSaver = require("file-saver");
import Bus from "@/bus";
export default {
    name: "RivateKeyManagement",
    components: {
        "v-contentHead": contentHead
    },
    computed: {
        privateKeyHead() {
            var arr = [
                {
                    enName: "address",
                    name: this.$t('table.address'),
                    tdWidth: ''
                },
                {
                    enName: "publicKey",
                    name: this.$t('table.publicKey'),
                    tdWidth: ''
                },
                {
                    enName: "userName",
                    name: this.$t('table.user'),
                    tdWidth: ""
                },
                {
                    enName: "operate",
                    name: this.$t('table.actions'),
                    tdWidth: 150
                }
            ]
            return arr
        },
        rules() {
            var obj = {
                userName: [
                    {
                        required: true,
                        message: this.$t('dialog.pleaseEnterUserName'),
                        trigger: "blur"
                    },
                    {
                        // pattern: /^\S.*\S*$/g,
                        pattern: /^[A-za-z0-9]+$/,
                        message: this.$t('dialog.rivateKeyVerifyFont'),
                        trigger: "blur",

                    },
                    {
                        trigger: "blur",
                        min: 3,
                        max: 32,
                        message: this.$t('dialog.rivateKeyVerifyLength'),
                    }
                ]
            }
            return obj
        }
    },
    data() {
        return {
            userForm: {
                userName: "",
            },
            loading: false,
            creatUserNameVisible: false,
            privateKeyList: localStorage.getItem("privateKeyList") ? JSON.parse(localStorage.getItem("privateKeyList")) : [],
            fileString: "",
            uploadMap: {}
        };
    },
    mounted() {
        this.getLocalKeyStores()
    },
    methods: {
        creatUserBtn() {
            this.userForm.userName = "";
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
        getLocalKeyStores() {
            queryLocalKeyStores()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.privateKeyList = data
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
                        message: this.$t('text.systemError')
                    });
                })
        },
        sureUserName(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    let userArr = this.privateKeyList.map(item => {
                        return item.userName
                    })
                    if (userArr.includes(this.userForm.userName)) {
                        this.$message({
                            type: "error",
                            message: this.$t('text.nameNoSame')
                        });
                    } else {
                        this.creatUserNameVisible = false;
                        this.addUser()
                    }
                } else {
                    return false;
                }
            });

        },
        addUser: function () {
            queryCreatePrivateKey({ useAes: false, type: 0, userName: this.userForm.userName })
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.getLocalKeyStores()
                        this.$message({
                            type: "success",
                            message: this.$t('text.addUserSuccessed')
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
                        message: this.$t('text.systemError')
                    });
                });
        },
        deleteFile(params) {
            this.$confirm(`${this.$t('dialog.sureDelete')}？`, {
                center: true,
                dangerouslyUseHTMLString: true
            })
                .then(() => {
                    this.getDeleteFile(params);
                })
                .catch(() => {
                    console.log('')
                });
        },
        getDeleteFile(params) {
            queryDeletePrivateKey(params.address)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.getLocalKeyStores()
                        this.$message({
                            type: "success",
                            message: this.$t('text.deleteUserSuccessed')
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
                        message: this.$t('text.systemError')
                    });
                });
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
                        privateKey: JSON.parse(_this.fileString).privateKey,
                        userName: JSON.parse(_this.fileString).userName,
                    };
                    queryImportPrivateKey(reqQuery)
                        .then(res => {
                            const { data, status } = res;
                            if (status === 200) {
                                e.target.value = '';
                                _this.getLocalKeyStores()
                                _this.$message({
                                    type: 'success',
                                    message: _this.$t('text.importSuccessed')
                                })
                            } else {
                                e.target.value = '';
                                _this.$message({
                                    type: "error",
                                    message: _this.$chooseLang(res.data.code)
                                });
                            }
                        })
                        .catch(err => {
                            e.target.value = '';
                            _this.$message({
                                type: "error",
                                message: _this.$t('text.systemError')
                            });
                        });

                } catch (error) {
                    e.target.value = '';
                    _this.$message({
                        type: 'error',
                        message: _this.$t('text.importFailed')
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
