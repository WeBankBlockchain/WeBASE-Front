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
    <div class="send-wrapper">
        <div class="font-color-ed5454 text-center" v-if="sendErrorMessage">{{sendErrorMessage}}</div>
        <div class="send-item">
            <span class="send-item-title">合约名称:</span>
            <span>{{data.contractName}}</span>
        </div>
        <div class="send-item">
            <span class="send-item-title">合约地址:</span>
            <el-input v-model.trim="contractAddress" style="width: 240px;" placeholder="请输入合约地址"></el-input>
            <el-tooltip class="item" effect="dark" content="选填项，导入已部署的合约地址。" placement="top-start">
                <i class="el-icon-info"></i>
            </el-tooltip>
        </div>
        <div class="send-item">
            <span class="send-item-title">方法:</span>
            <!-- <el-select v-model="transation.funcType" placeholder="方法类型" @change="changeType($event)" style="width:110px">
                <el-option label="function" :value="'function'"></el-option>
            </el-select> -->
            <el-select v-model="transation.funcName" filterable placeholder="方法名" v-if="funcList.length > 0" popper-class="func-name" @change="changeFunc" style="width:240px">
                <el-option :label="item.name" :key="item.funcId" :value="item.funcId" v-for="item in funcList">
                    <span :class=" {'func-color': !item.constant}">{{item.name}}</span>
                </el-option>
            </el-select>
        </div>
        <div class="send-item" v-show="!constant">
            <span class="send-item-title">用户地址:</span>
            <el-select v-model="transation.userName" :placeholder="placeholderText" style="width:240px" class="plac-op">
                <el-option :label="item.address" :value="item.address" :key="item.address" v-for='(item,index) in userList'>
                    <span class="font-12">{{item.userName}}</span>
                    <span>{{item.address}}</span>
                </el-option>
            </el-select>
            <span class="user-explain" v-show="userId">
                (<span class="ellipsis-info ">{{userId}}</span>)
            </span>
        </div>
        <div class="send-item" v-show="pramasData.length" style="line-height: 25px;">
            <span class="send-item-title" style="position: relative;top: 5px; ">参数:</span>
            <ul style="position: relative;top: -25px;">
                <li v-for="(item,index) in pramasData" style="margin-left:63px;">
                    <el-input v-model="transation.funcValue[index]" style="width: 240px;" :placeholder="item.type">
                        <template slot="prepend" style="width: 51px;">
                            <span>{{item.name}}</span>
                        </template>
                    </el-input>
                </li>
                <p style="padding: 5px 0 0 28px;">
                    <i class="el-icon-info" style="padding-right: 4px;"></i>如果参数类型是数组，请用逗号分隔，不需要加上引号，例如：arry1,arry2。string等其他类型也不用加上引号。
                </p>
            </ul>
        </div>

        <div class="text-right send-btn java-class">
            <el-button @click="close">取消</el-button>
            <el-button type="primary" @click="submit('transation')" :disabled='buttonClick'>确定</el-button>
        </div>
    </div>
</template>
<script>
import { sendTransation } from "@/util/api";
import errcode from "@/util/errcode";

export default {
    name: "sendTransation",
    props: ["data", "dialogClose", "abi", 'version', 'sendErrorMessage'],
    data: function () {
        return {
            transation: {
                userName: "",
                funcName: "",
                funcValue: [],
                funcType: "function"
            },
            userId: "",
            userList: localStorage.getItem('privateKeyList') ? JSON.parse(localStorage.getItem('privateKeyList')) : [],
            abiList: [],
            pramasData: [],
            funcList: [],
            buttonClick: false,
            contractVersion: this.version,
            constant: false,
            contractAddress: this.data.contractAddress || "",
            errorMessage: '',
            placeholderText: "请选择用户地址"
        };
    },
    mounted: function () {
        if (this.userList.length) {
            this.transation.userName = this.userList[0]['address']
            this.userId = this.userList[0]['userName']
        } else {
            this.placeholderText = "没有用户，请去私钥管理新建用户"
        }
        this.formatAbi();
        this.changeFunc();
    },
    methods: {
        submit: function (formName) {
            this.send();
        },
        close: function (formName) {
            this.$emit("close", false);
        },
        changeType: function (val) {
            this.funcList = [];
            if (val && val === "function") {
                this.abiList.forEach((value, index) => {
                    if (value.type === val) {
                        value.funcId = index
                        this.funcList.push(value);
                    }
                });
            } else if (val && val === "constructor") {
                this.abiList.forEach(value => {
                    if (value.type === val) {
                        this.pramasData = value.inputs;
                    }
                });
            } else {
                this.abiList.forEach((value, index) => {
                    if (value.type === "function") {
                        value.funcId = index
                        this.funcList.push(value);
                    }
                });
            }
            if (this.funcList.length) {
                this.transation.funcName = this.funcList[0].funcId;
            }
            this.changeFunc();
        },
        formatAbi: function () {
            let abi = this.abi;
            if (abi) {
                this.abiList = JSON.parse(abi);
                this.changeType();
            }
        },
        changeFunc: function () {
            this.constant = false;
            this.funcList.forEach(value => {
                if (value.funcId === this.transation.funcName) {
                    this.pramasData = value.inputs;
                    this.constant = value.constant;
                }
            });
            this.funcList.sort(function (a, b) {
                return (a.name + '').localeCompare(b.name + '')
            })
        },
        send: function () {
            this.buttonClick = true;
            let pattren = /^\s+|\s+$/g;
            if (this.transation.funcType === "constructor") {
                this.transation.funcName = this.data.contractName;
            }
            if (this.transation.funcValue.length) {
                for (let i = 0; i < this.transation.funcValue.length; i++) {
                    let data = this.transation.funcValue[i].replace(/^\s+|\s+$/g, "");
                    this.transation.funcValue[i] = data;
                }
            }
            let functionName = "";
            this.funcList.forEach(value => {
                if (value.funcId == this.transation.funcName) {
                    functionName = value.name
                }
            })
            let data = {
                groupId: localStorage.getItem("groupId"),
                user: this.constant ? ' ' : this.transation.userName,
                contractName: this.data.contractName,
                version: this.contractVersion,
                funcName: functionName || "",
                funcParam: this.transation.funcValue,
                contractAddress: this.contractAddress,
                useAes: false
            };
            sendTransation(data)
                .then(res => {
                    this.buttonClick = false;
                    this.close();
                    const { data, status } = res
                    if (status === 200) {
                        let resData = data;
                        this.$emit("success", resData);
                        if (this.constant) {
                            this.$message({
                                type: "success",
                                message: "查询成功!"
                            });
                        } else {
                            if (resData.statusOK) {
                                this.$message({
                                    type: "success",
                                    message: "交易成功!"
                                });
                            } else {
                                this.$message({
                                    type: "success",
                                    message: "交易失败!"
                                });
                            }
                        }

                    } else {
                        this.$message({
                            type: "error",
                            message: errcode.errCode[res.data.code].cn || '发送交易失败！'
                        });
                        this.close();
                    }
                })
                .catch(err => {
                    this.buttonClick = false;
                    this.close();
                    this.$message({
                        type: "error",
                        message: "发送交易失败！"
                    });
                });
        }
    }
};
</script>

<style scoped>
.send-wrapper {
    padding-left: 30px;
}
.send-item {
    line-height: 40px;
}
.send-item-title {
    display: inline-block;
    width: 60px;
    text-align: right;
}
.send-item-params {
    display: inline-block;
}
.send-item >>> .el-input__inner {
    height: 32px;
    line-height: 32px;
}
.send-btn {
}
.send-btn >>> .el-button {
    padding: 9px 16px;
}
.java-class {
    margin-top: 10px;
}
.send-item > ul > .el-input-group__prepend > span {
    width: 51px;
    text-align: center;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-break: break-all;
    display: inline-block;
}
.func-color {
    color: #409eff;
}
.func-name .el-select-dropdown__list .el-select-dropdown__item.selected {
    color: #606266;
    font-weight: 700;
}
.user-explain {
    margin-left: 4px;
}
.user-explain > span {
    display: inline-block;
    max-width: 45px;
    height: 25px;
    line-height: 25px;
    position: relative;
    top: 9px;
}
</style>
