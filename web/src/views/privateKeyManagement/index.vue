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
                    <el-button type="primary" class="search-part-left-btn" @click="$store.dispatch('switch_import_rivate_key_dialog')">{{this.$t('table.importPrivateKey')}}</el-button>
                    <el-tooltip class="item" effect="dark" :content="$t('text.privateKeyManagementInfo')" placement="top-start">
                        <i class="el-icon-info" style="color: #fff;font-size: 18px;margin: 12px 0 0 15px;"></i>
                    </el-tooltip>
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
            </div>
        </div>
        <el-dialog :visible.sync="creatUserNameVisible" :title="$t('dialog.addUsername')" width="640px" class="dialog-wrapper" center v-if="creatUserNameVisible" @close="closeCallback">
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
        <el-dialog :visible.sync="$store.state.importRivateKey" :title="$t('table.importPrivateKey')" width="640px" :append-to-body="true" class="dialog-wrapper" v-if='$store.state.importRivateKey' center>
            <import-key @importPrivateKeySuccess="importPrivateKeySuccess" ref="importKey"></import-key>
        </el-dialog>
        <el-dialog :visible.sync="$store.state.exportRivateKey" :title="$t('table.export')" width="640px" :append-to-body="true" class="dialog-wrapper" v-if='$store.state.exportRivateKey' center>
            <export-key :exportInfo="exportInfo" @exportPrivateKeySuccess="exportPrivateKeySuccess"></export-key>
        </el-dialog>
    </div>
</template>


<script>
import contentHead from "@/components/contentHead";
import importKey from "./dialog/importKey"
import { queryCreatePrivateKey, queryLocalKeyStores, queryDeletePrivateKey, encryption } from "@/util/api";
import { unique } from "@/util/util";

import Bus from "@/bus";
import ExportKey from './dialog/exportKey.vue';

export default {
    name: "privateKeyManagement",
    components: {
        "v-contentHead": contentHead,
        importKey,
        ExportKey
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
                        pattern: /^[A-za-z0-9]+$/,
                        message: this.$t('dialog.privateKeyVerifyFont'),
                        trigger: "blur",

                    },
                    {
                        trigger: "blur",
                        min: 3,
                        max: 32,
                        message: this.$t('dialog.privateKeyVerifyLength'),
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
            uploadMap: {},
            exportInfo: {}
        };
    },
    mounted() {
        this.getLocalKeyStores();
        this.getEncryption()
    },
    methods: {
        getEncryption: function () {
            encryption().then(res => {
                if (res.status == 200) {
                    localStorage.setItem("encryptionId", res.data)
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
                        message: err.data || this.$t('text.systemError')
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
                        message: err.data || this.$t('text.systemError')
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
                        message: err.data || this.$t('text.systemError')
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
            this.exportInfo = params
            this.$store.dispatch('switch_export_rivate_key_dialog')
        },

        importPrivateKeySuccess() {
            this.getLocalKeyStores();
        },
        exportPrivateKeySuccess(){
            this.$store.dispatch('switch_export_rivate_key_dialog')
        }
    }
};
</script>
<style scoped>
@import "./privateKeyManagement.css";
</style>
