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
            <tr>
                <td style="width: 100px;text-align: right" class="text-td"><span class="font-color-fff">{{$t('text.acountAddress')}}：</span></td>
                <td>
                    <el-select v-model="userId" :placeholder="placeholderText" @change="changeId" style="width: 310px">
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
                <td style="vertical-align: top;width: 100px;text-align: right" class="text-td">
                    <span class="font-color-fff">
                        {{$t('text.parame')}}：
                    </span>
                </td>
                <td>
                    <div v-for='(item,index) in inputs' :key='item.name'>
                        <el-input v-model.trim="parameter[index]" style="width: 310px;margin-bottom:10px;" :placeholder="item.type">
                            <template slot="prepend">
                                <span class="parame-item-name" :title="item.name">{{item.name}}</span>
                            </template>
                        </el-input>
                    </div>
                </td>
            </tr>
            <tr v-if='inputs.length'>
                <td></td>
                <td>
                    <p class="font-color-ed5454">
                        <i class="el-icon-info" style="padding-right: 4px;"></i>{{$t('contracts.paramsInfo')}}
                    </p>
                </td>
            </tr>
        </table>
        <div class="text-right send-btn">
            <el-button @click="close">{{$t('dialog.cancel')}}</el-button>
            <el-button type="primary" @click="submit">{{$t('dialog.confirm')}}</el-button>
        </div>
    </div>
</template>
<script>
import { queryLocalKeyStores } from "@/util/api";
import { isJson } from "@/util/util"
export default {
    name: "changeUser",
    props: ["abi"],
    data: function () {
        return {
            userName: "",
            userList: [],
            userId: null,
            inputs: [],
            parameter: [],
            abifile: JSON.parse(this.abi),
            version: "",
            versionShow: false,
            errorInfo: "",
            placeholderText: this.$t('placeholder.selectedAccountAddress')
        };
    },
    mounted: function () {
        this.changeConstructor();
        this.getLocalKeyStores();
    },
    methods: {
        getLocalKeyStores() {
            queryLocalKeyStores()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.userList = data
                        if (this.userList.length) {
                            this.userId = this.userList[0].address
                            this.userName = this.userList[0].userName
                        } else {
                            this.placeholderText = this.$t('placeholder.selectedNoUser')
                        }
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
            var params = []
            for (let i = 0; i < this.parameter.length; i++) {
                if (this.parameter[i] && isJson(this.parameter[i])) {
                    try {
                        params[i] = JSON.parse(this.parameter[i])
                    } catch (error) {
                        console.log(error)
                    }
                } else {
                    params[i] = this.parameter[i];
                }
            }
            let data = {
                userId: this.userId,
                params: params
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
.text-td {
    white-space: nowrap;
}
.parame-item-name {
    display: inline-block;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
</style>

