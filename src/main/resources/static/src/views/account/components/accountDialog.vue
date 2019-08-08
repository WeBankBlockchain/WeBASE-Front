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
        <el-form :model="accountForm" :rules="rules" ref="accountForm" label-width="100px" class="demo-ruleForm">
            <el-form-item label="帐号" prop="name" style="width: 300px;">
                <el-input v-model="accountForm.name" placeholder="请输入帐号" maxlength="12" :disabled="accountForm['disabled']"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password" style="width: 300px;" v-if="accountForm['dShow']">
                <el-input v-model="accountForm.password" placeholder="请输入密码" minlength="6" maxlength="12" :type="inputType">
                    <i slot="suffix" style="color: #00122C;" :class="[inputType === 'password' ? 'el-icon-view': 'wbs-icon-view-hidden']"  @click.stop.prevent="showPassword"></i>
                </el-input>
            </el-form-item>
            <el-form-item label="角色" prop="role" style="width: 300px;" v-if="accountForm['mShow']">
                <el-select v-model="accountForm.role" placeholder="请选择" :disabled="accountForm['mDisabled']">
                    <el-option v-for="item in roleList" :key="item.roleId" :label="item.roleNameZh" :value="item.roleId">
                    </el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <div class="dialog-footer">
            <el-button @click="modelClose">取 消</el-button>
            <el-button type="primary" @click="submit('accountForm')" :loading="loading">确 定</el-button>
        </div>
    </div>
</template>

<script>
import {
    roleList,
    creatAccountInfo,
    modifyAccountInfo,
    deleteAccountInfo
} from "@/util/api";
const sha256 = require("js-sha256").sha256;
export default {
    name: "accountDialog",
    props: {
        accountDialogOptions: {
            type: Object
        }
    },
    watch: {
        "accountDialogOptions.type": {
            handler(val) {
                this.type = val;
                switch (val) {
                    case "creat":
                        this.accountForm = {
                            name: "",
                            password: "",
                            role: 100001,
                            disabled: false,
                            mDisabled: false,
                            dShow: true,
                            mShow: true
                        };
                        break;
                    case "delete":
                        this.accountForm = {
                            name: this.accountDialogOptions.data["account"],
                            password: "",
                            role: this.accountDialogOptions.data["roleId"],
                            disabled: true,
                            mDisabled: true,
                            dShow: false
                        };
                        break;
                    case "modify":
                        this.accountForm = {
                            name: this.accountDialogOptions.data["account"],
                            password: "",
                            role: this.accountDialogOptions.data["roleId"],
                            disabled: true,
                            mDisabled: false,
                            dShow: true,
                            mShow: false
                        };
                        break;
                }
            },
            deep: true,
            immediate: true
        }
    },
    data() {
        return {
            type: this.accountDialogOptions.type,
            loading: false,
            accountForm: {},
            roleList: [],
            inputType: "password",
            rules: {
                name: [
                    {
                        required: true,
                        message: "请输入帐号",
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 12,
                        message: "长度在 1 到 12 个字符",
                        trigger: "blur"
                    },
                    {
                        pattern: /^[A-Za-z0-9]+$/,
                        message: "用户名只能输入字母和数字",
                        trigger: "blur"
                    }
                ],
                password: [
                    {
                        required: true,
                        message: "请输入密码",
                        trigger: "blur"
                    },
                    {
                        min: 6,
                        max: 12,
                        message: "长度在 6 到 12 个字符",
                        trigger: "blur"
                    },
                    {
                        pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,12}$/,
                        message: "字母,数字组成,且至少包含一个大写字母和一个小写字母",
                        trigger: "blur"
                    }
                ]
            }
        };
    },
    mounted() {
        this.getRoleList();
    },
    methods: {
        modelClose: function() {
            this.$emit("close", false);
        },
        submit: function(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.$confirm("确认提交？", {
                        center: true
                    })
                        .then(() => {
                            this.loading = true;
                            this.getAllAccountInfo();
                        })
                        .catch(() => {
                            this.modelClose();
                        });
                } else {
                    return false;
                }
            });
        },
        getRoleList: function() {
            let networkId = localStorage.getItem("networkId");
            let reqQuery = {};
            reqQuery = {
                networkId: networkId,
                pageNumber: "",
                pageSize: "",
                roleId: "",
                roleName: ""
            };
            roleList({}, reqQuery)
                .then(res => {
                    if (res.data.code === 0) {
                        this.roleList = res.data.data;
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
                            this.errcode.errCode[err.data.code].cn
                    });
                });
        },
        getAllAccountInfo: function() {
            let type = this.type;
            switch (type) {
                case "creat":
                    this.getCreatAccountInfo();
                    break;
                case "modify":
                    this.getModifyAccountInfo();
                    break;
                case "delete":
                    this.getDeleteAccountInfo();
                    break;
            }
        },
        getCreatAccountInfo: function() {
            let reqData = {
                account: this.accountForm.name,
                accountPwd: sha256(this.accountForm.password),
                roleId: this.accountForm.role
            };
            creatAccountInfo(reqData, {})
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        this.$message({
                            type: "success",
                            message: "新增成功"
                        });
                        this.modelClose();
                        this.$emit("success");
                    } else {
                        this.modelClose();
                        this.$message({
                            type: "error",
                            message: this.errcode.errCode[res.data.code].cn
                        });
                    }
                })
                .catch(err => {
                    this.modelClose();
                    this.$message({
                        type: "error",
                        message:
                            this.errcode.errCode[err.data.code].cn
                    });
                });
        },
        getModifyAccountInfo: function() {
            let reqData = {
                account: this.accountForm.name,
                accountPwd: sha256(this.accountForm.password),
                roleId: this.accountForm.role
            };
            modifyAccountInfo(reqData, {})
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        this.$message({
                            type: "success",
                            message: "修改成功"
                        });
                        this.modelClose();
                        this.$emit("success");
                    } else {
                        this.modelClose();
                        this.$message({
                            type: "error",
                            message: this.errcode.errCode[res.data.code].cn
                        });
                    }
                })
                .catch(err => {
                    this.modelClose();
                    this.$message({
                        type: "error",
                        message:
                            this.errcode.errCode[err.data.code].cn
                    });
                });
        },
        getDeleteAccountInfo: function() {
            deleteAccountInfo(this.accountForm.name, {})
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        this.$message({
                            type: "success",
                            message: "删除成功"
                        });
                        this.modelClose();
                        this.$emit("success");
                    } else {
                        this.modelClose();
                        this.$message({
                            type: "error",
                            message: this.errcode.errCode[res.data.code].cn
                        });
                    }
                })
                .catch(err => {
                    this.modelClose();
                    this.$message({
                        type: "error",
                        message:
                            this.errcode.errCode[err.data.code].cn
                    });
                });
        },
        showPassword() {
            if(this.inputType === 'password'){
                this.inputType = 'text'
            }else {
                this.inputType = 'password'
            }
        }
    }
};
</script>

<style scoped>
.dialog-footer {
    text-align: right;
    margin-right: -5px;
    padding-bottom: 20px;
    padding-top: 12px;
}
.isNone {
    display: none;
}
.isShow {
    display: block;
}
.demo-ruleForm >>> .el-form-item__error {
    padding-top: 0
}
</style>
