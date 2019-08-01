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
    <div class="chang-wrapper">
        <table class="opt-wrapper">
            <!-- <tr>
                <td>合约版本号：</td>
                <td>
                    <el-input v-model="version" placeholder="请输入数字或字母" @blur='versionBlur' maxlength='18' style="width: 240px"></el-input>
                    <span style="color: #f00" v-show="versionShow">{{errorInfo}}</span>
                </td>
            </tr> -->
            <tr>
                <td style="width: 70px"><span class="font-color-fff">用户地址：</span></td>
                <td>
                    <el-select v-model="userId" :placeholder="placeholderText" @change="changeId" style="width: 340px">
                        <el-option :label="item.address" :value="item.address" :key="item.address" v-for='item in userList'>
                            <span class="font-12">{{item.userName}}</span>
                            <span>{{item.address}}</span>
                        </el-option>
                    </el-select>
                </td>
                <td v-show="userName">
                    <div class="user-explain font-color-fff">
                        (<span class="ellipsis-info">{{userName}}</span>)
                    </div>
                    </td>
            </tr>
            <tr v-if='inputs.length'>
                <td style="vertical-align: top;width: 70px">参数：</td>
                <td>
                    <div v-for='(item,index) in inputs' :key='item.name'>
                        <el-input v-model="parameter[index]" style="width: 240px;margin-bottom:10px;" :placeholder="item.type">
                            <template slot="prepend">
                                <span>{{item.name}}</span>
                            </template>
                        </el-input>
                        <!-- <el-tooltip class="item" effect="dark" content="如果参数类型是数组，请用逗号分隔，不需要加上引号，例如：arry1,arry2。string等其他类型也不用加上引号" placement="top-start">
                            <i class="el-icon-info" style="position: relative;top: 8px;"></i>
                        </el-tooltip> -->
                    </div>
                </td>
            </tr>
            <tr v-if='inputs.length'>
                <td></td>
                <td>
                    <p style="padding: 0px 0 0 0px;">
                        <i class="el-icon-info" style="padding-right: 4px;"></i>如果参数类型是数组，请用逗号分隔，不需要加上引号，例如：arry1,arry2。string等其他类型也不用加上引号。
                    </p>
                </td>
            </tr>
        </table>
        <div class="text-right send-btn">
            <el-button @click="close">取消</el-button>
            <el-button type="primary" @click="submit">确定</el-button>
        </div>
    </div>
</template>
<script>
import { } from "@/util/api";
import errcode from "@/util/errcode";
export default {
    name: "changeUser",
    props: ["abi"],
    data: function () {
        return {
            userName: "",
            userList: localStorage.getItem("privateKeyList")
                ? JSON.parse(localStorage.getItem("privateKeyList"))
                : [],
            userId: null,
            inputs: [],
            parameter: [],
            abifile: JSON.parse(this.abi),
            version: "",
            versionShow: false,
            errorInfo: "",
            placeholderText: "请选择用户地址"
        };
    },
    mounted: function () {
        if (this.userList.length > 0) {
            this.userId = this.userList[0].address
            this.userName = this.userList[0].userName
        }else {
            this.placeholderText = "没有用户，请去私钥管理新建用户"
        };
        this.changeConstructor();
    },
    methods: {
        changeConstructor: function () {
            if (this.abifile.length) {
                this.abifile.forEach(value => {
                    if (value.type === "constructor") {
                        this.inputs = value.inputs;
                    }
                });
            }
        },
        changeId: function (val) {
            this.userList.forEach(value => {
                if (val === value.address) {
                    this.userName = value.userName;
                }
            });
        },
        close: function () {
            this.$emit("close");
        },
        submit: function () {
            this.versionShow = false;
            this.errorInfo = "";
            let data = {
                userId: this.userId,
                params: this.parameter
            };
            this.$emit("change", data);
            this.$emit("close");
        }
    }
};
</script>
<style scoped>
.chang-wrapper {
    padding-left: 20px;
    margin-top: 15px;
}

.chang-wrapper >>> .el-input__inner {
    height: 32px;
    line-height: 32px;
}
.chang-wrapper >>> .el-input__icon {
    line-height: 32px;
}
.opt-wrapper tr td {
    padding-top: 0;
    padding-bottom: 10px;
}
.send-btn >>> .el-button {
    padding: 9px 16px;
}
.user-explain {
    display: flex;
    margin-left: 4px;
}
.user-explain > span {
    display: inline-block;
    max-width: 45px;
}
</style>

