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
                    
                    <el-button v-if="isShowAddUserBtn" type="text" size="mini" @click="createUser()">{{$t('privateKey.addUser')}}</el-button>
 
                </td>
                <td v-show="userName">
                    <div class="user-explain font-color-fff">
                        (<span class="ellipsis-info">{{userName}}</span>)
                    </div>
                </td>
            </tr>
            <tr>
                <td style="width: 100px;text-align: right" class="text-td"><span class="font-color-fff">CNS：</span></td>
                <td>
                    <el-checkbox v-model="isCNS" @change="changeCns"></el-checkbox>
                </td>
            </tr>

            <tr v-if="isCNS">
                <td style="width: 100px;text-align: right" class="text-td"><span class="font-color-fff"></span></td>
                <td>
                    <el-form :model="cnsVersionFrom" :rules="rules" ref="cnsVersionFrom" class="demo-ruleForm">
                        <el-form-item prop="cnsName">
                            <el-input v-model="cnsVersionFrom.cnsName" style="width: 310px;" :placeholder="$t('placeholder.inputCnsName')">
                                <template slot="prepend" style="width: 51px;">
                                    <span class="parame-item-name" title="name">name</span>
                                </template>
                            </el-input>
                        </el-form-item>
                    </el-form>
                </td>
            </tr>
            <tr v-if="isCNS">
                <td style="width: 100px;text-align: right" class="text-td"><span class="font-color-fff"></span></td>
                <td>
                    <el-form :model="cnsVersionFrom" :rules="rules" ref="cnsVersionFrom" class="demo-ruleForm">
                        <el-form-item prop="cnsVersion">
                            <el-input v-model="cnsVersionFrom.cnsVersion" style="width: 310px;" :placeholder="$t('dialog.cnsVersion')">
                                <template slot="prepend" style="width: 51px;">
                                    <span class="parame-item-name" title="version">version</span>
                                </template>
                            </el-input>
                        </el-form-item>
                    </el-form>
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
                        <el-input v-model.trim="parameter[index]" style="width: 310px;margin-bottom:15px;" :placeholder="item.type">
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
          <!-- <el-dialog :title="$t('dialog.addUsername')" :visible.sync="creatUserNameVisible" :before-close="closeUserName" class="dialog-wrapper" width="640px" :center="true" :append-to-body="true"> -->
          <el-dialog :title="$t('dialog.addUsername')" :visible.sync="creatUserNameVisible"  class="dialog-wrapper" width="640px" :center="true" :append-to-body="true">
              <v-createUser  @close='createUserClose'></v-createUser>
         </el-dialog>
    </div>
</template>
<script>
import { queryLocalKeyStores } from "@/util/api";
import { isJson } from "@/util/util";
import createUser from "@/views/toolsContract/components/createUser";

export default {
      components: {
        "v-createUser": createUser
    },
    name: "changeUser",
    props: ["abi", "contractName"],
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
            placeholderText: this.$t('placeholder.selectedAccountAddress'),
            isCNS: false,
            cnsVersionFrom: {
                cnsVersion: "",
                cnsName: this.contractName
            },
            isUserNameShow: false,
            creatUserNameVisible: false,
            isShowAddUserBtn: false
        };
    },
    computed: {
        rules() {
            var obj = {
                cnsVersion: [
                    {
                        required: true,
                        message: this.$t('dialog.cnsVersion'),
                        trigger: "blur"
                    },
                    {
                        pattern: /^[A-Za-z0-9.]+$/,
                        message: this.$t('dialog.cnsVersionPattern'),
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 10,
                        message: this.$t('dialog.length1_10'),
                        trigger: "blur"
                    },
                ],
                cnsName: [
                    {
                        required: true,
                        message: this.$t('dialog.cnsName'),
                        trigger: "blur"
                    },
                    {
                        pattern: /^[A-Za-z0-9.]+$/,
                        message: this.$t('dialog.cnsVersionPattern'),
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 32,
                        message: this.$t('dialog.privateKeyVerifyLength1_32'),
                        trigger: "blur"
                    },
                ],
            }
            return obj
        },

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
                            this.isShowAddUserBtn = true;
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
                        message: err.data || this.$t('text.systemError')
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
            if (this.isCNS) {
                if (!this.cnsVersionFrom.cnsName) return;
                this.$refs['cnsVersionFrom'].validate((valid) => {
                    if (valid) {
                        this.queryDeploy()
                    } else {
                        return false;
                    }
                });
            } else {
                this.queryDeploy()
            }
        },
        queryDeploy() {
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
            let cnsObj = {
                version: this.cnsVersionFrom.cnsVersion,
                saveEnabled: this.isCNS,
                cnsName: this.cnsVersionFrom.cnsName
            }
            this.$emit("change", data, cnsObj);
            this.$emit("close");
        },
        changeCns(val) {
            if (!val) {
                this.cnsVersionFrom.cnsVersion = "";
                this.cnsVersionFrom.cnsName = "";
            } else {
                this.cnsVersionFrom.cnsName = this.contractName;
            }
        },
        createUser(){
            this.creatUserNameVisible = true;
        },
         createUserClose(data){
             console.log(data);
             this.userList = data;
             if(this.userList.length > 0 ){
                this.userId = this.userList[0].address
                this.userName = this.userList[0].userName
                this.isShowAddUserBtn = false;
             } 
             this.creatUserNameVisible = false;
        },
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
.demo-ruleForm >>> .el-form-item {
    margin-bottom: 0;
}
.demo-ruleForm >>> .el-form-item__error {
    padding-top: 0;
    transform: scale(0.9);
    top: 93%;
}
</style>

