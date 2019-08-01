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
    <div class="login-bg" :style="{backgroundImage: 'url(' + bgLogin +')'}">
        <div class="login">
            <div>
                <img :src="logoPng" alt="fisco-bcos" class="logo">
                <!-- <h2 class="login-title">WeBASE</h2> -->
            </div>
            <div class="msg-wrapper">
                <div class="msg-error" v-show="msgError || timeout">
                    <i class="el-icon-remove"></i>
                    <span v-if="msgError">登录失败</span>
                    <span v-else-if="timeout">请求超时</span>
                </div>
            </div>
            <div class="login-content">
                <!-- <template v-if="NewOrOldUser">
                    <el-form ref="loginForm" :model="loginForm" :rules="newUserRules">
                        <el-form-item label="账号" class="login-label" prop="user">
                            <el-input v-model="loginForm.user" placeholder="请输入账号">
                            </el-input>
                        </el-form-item>
                        <el-form-item label="密码" class="login-label" prop="password">
                            <el-input v-model="loginForm.password" placeholder="请输入密码" type="password" @keyup.enter.native="submit('loginForm')">
                            </el-input>
                        </el-form-item>
                    </el-form>
                </template> -->
                <template>
                    <el-form ref="loginForm" :model="loginForm" :rules="rules">
                        <el-form-item label="账号" class="login-label" prop="user">
                            <el-input v-model="loginForm.user" placeholder="请输入账号">
                            </el-input>
                        </el-form-item>
                        <el-form-item label="密码" class="login-label" prop="password">
                            <el-input v-model="loginForm.password" placeholder="请输入密码" type="password" @keyup.enter.native="submit('loginForm')">
                            </el-input>
                        </el-form-item>
                    </el-form>
                </template>
            </div>
            <div>
                <el-button @click="submit('loginForm')" type="primary" class="login-submit" :loading="logining">登录</el-button>
            </div>
        </div>
    </div>
</template>
<script>
import { login, networkList, haveNode } from "@/util/api";
import router from "@/router";
import bg from "@/../static/image/banner.jpg";
import logo from "@/../static/image/logo-2 copy@1.5x.jpg";
import { delCookie } from '@/util/util'
const sha256 = require("js-sha256").sha256;
export default {
    name: "login",
    data: function() {
        return {
            bgLogin: bg,
            logoPng: logo,
            logining: false,
            msgError: false,
            timeout: false,
            loginForm: {
                user: "",
                password: ""
            },
            rules: {
                user: [{ required: true, message: "请输入账号", trigger: "blur" }],
                password: [
                    { required: true, message: "请输入密码", trigger: "blur" }
                ]
            },
            newUserRules: {
                user: [
                    { required: true, message: "请输入账号", trigger: "blur" },
                    {
                        min: 1,
                        max: 32,
                        message: "长度在 1 到 32 个字符",
                        trigger: "blur"
                    }
                ],
                password: [
                    { required: true, message: "请输入密码", trigger: "blur" },
                    {
                        min: 5,
                        max: 5,
                        message: "登录密码错误",
                        trigger: "blur"
                    },
                    {
                        pattern: /admin/,
                        message: "请正确输入密码",
                        trigger: "blur"
                    }
                ]
            }
        };
    },
    methods: {
        submit: function(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.logining = true;
                    this.userLogin(this.getNetworkList);
                } else {
                    return false;
                }
            });
        },
        userLogin: function(callback) {
            delCookie('JSESSIONID')
            delCookie('NODE_MGR_ACCOUNT_C')
            let reqData = {
                account: this.loginForm.user,
                accountPwd: sha256(this.loginForm.password)
            };
            login(reqData)
                .then(res => {
                    if (res.data.code === 0) {
                        localStorage.setItem("user", res.data.data.account);
                        localStorage.setItem("root", res.data.data.roleName);
                        sessionStorage.setItem(
                            "accountStatus",
                            res.data.data.accountStatus
                        );
                        sessionStorage.setItem("reload", 1);
                        let acct = res
                        callback(acct);
                    } else {
                        this.msgError = true;
                        this.loginForm.password = "";
                        this.logining = false;
                    }
                })
                .catch(err => {
                    this.timeout = true;
                    this.loginForm.password = "";
                    this.logining = false;
                });
        },
        getNetworkList: function(acct) {
            networkList({})
                .then(res => {
                    if (res.data.code === 0) {
                        localStorage.setItem(
                            "networkId",
                            res.data.data[0].networkId
                        );
                        localStorage.setItem(
                            "networkName",
                            res.data.data[0].networkName
                        );
                        router.push({
                            path: "/main",
                            query: {
                                root: acct.data.data.roleName
                            }
                        });
                    } else {
                        this.$message({
                            message: "获取网络失败！",
                            type: "error"
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        message: "获取网络失败！",
                        type: "error"
                    });
                });
        }
    }
};
</script>
<style>
.login-label.is-required:not(.is-no-asterisk) > .el-form-item__label:before {
    content: "" !important;
}
.login-bg {
    position: relative;
    width: 100%;
    height: 100%;
    background-repeat: no-repeat;
    background-size: 100% 100%;
}
.login {
    position: absolute;
    width: 400px;
    height: 460px;
    top: 50%;
    left: 70%;
    margin-top: -230px;
    margin-left: -201px;
    background-color: #fff;
    border-radius: 16px;
    text-align: center;
}
.msg-wrapper {
    min-height: 23px;
    height: auto;
    margin: 5px 0;
}
.msg-error {
    color: #e4393c;
}
.logo {
    width: 120px;
    padding-top: 80px;
}
.login-title {
    padding: 16px 0 0px 0;
    font-size: 20px;
    color: #2e2e2e;
    letter-spacing: 0.04px;
}
.login-label {
    position: relative;
    padding: 0 60px;
}
.login-label input {
    height: 54px;
    padding-left: 60px;
}
.login-content .login-label .el-form-item__label {
    position: absolute !important;
    left: 72px !important;
    top: 12px !important;
    z-index: 999 !important;
    border-right: 1px solid #cfdae9 !important;
    height: 30px !important;
    line-height: 30px !important;
}
.login-submit {
    width: 280px;
    height: 45px;
    margin-top: 10px;
    font-size: 14px;
}
</style>
