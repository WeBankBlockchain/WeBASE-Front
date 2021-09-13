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
        <table>
            <tr>
                <td class="text-right text-td"><span class="font-color-fff ">{{$t('text.contractName')}}：</span></td>
                <td>
                    <span class="font-color-fff">{{data.contractName}}</span>
                </td>
            </tr>
            <tr>
                <td class="text-right text-td" style="padding: 5px 0;"><span class="font-color-fff ">CNS：</span></td>
                <td>
                    <el-checkbox v-model="isCNS" @change="changeCns"></el-checkbox>
                </td>
            </tr>
            <tr v-if="isCNS">
                <td style="padding: 11px 0;" class="text-right text-td"><span class="font-color-fff"></span></td>
                <td>
                    <el-input v-model.trim="cnsName" style="width: 260px;" :placeholder="$t('dialog.cnsName')">
                        <template slot="prepend" style="width: 51px;">
                            <span>name</span>
                        </template>
                    </el-input>
                </td>
            </tr>
            <tr v-if="isCNS">
                <td style="padding: 11px 0;" class="text-right text-td"><span class="font-color-fff"></span></td>
                <td>
                    <el-input v-model.trim="cnsVersion" style="width: 260px;" :placeholder="$t('dialog.cnsVersion')">
                        <template slot="prepend" style="width: 51px;">
                            <span>version</span>
                        </template>
                    </el-input>
                </td>
            </tr>
            <tr v-else>
                <td class="text-right text-td"><span class="font-color-fff">{{$t('text.contractAddress')}}：</span></td>
                <td>
                    <el-input v-model.trim="contractAddress" style="width: 260px;margin-bottom:4px;" :placeholder="$t('placeholder.selectedContractAddress')"></el-input>
                    <el-tooltip class="font-color-fff" effect="dark" :content="$t('title.txnContractAddExp')" placement="top-start">
                        <i class="el-icon-info"></i>
                    </el-tooltip>
                </td>
            </tr>
            <tr>
                <td class="text-right text-td">
                    <span class="font-color-fff">{{$t('text.sendFunction')}}：</span>
                </td>
                <td>
                    <el-select v-model="transation.funcName" filterable :placeholder="$t('placeholder.functionName')" v-if="funcList.length > 0" popper-class="func-name" @change="changeFunc" style="width: 260px">
                        <el-option :label="item.name" :key="item.funcId" :value="item.funcId" v-for="item in funcList">
                            <span :class=" {'func-color': !item.constant}">{{item.name}}</span>
                        </el-option>
                    </el-select>
                </td>
            </tr>
            <tr v-show="showUser">
                <td class="text-right text-td">
                    <span class="font-color-fff">{{$t('text.acountAddress')}}：</span>
                </td>
                <td>
                    <el-select v-model="transation.userName" :placeholder="placeholderText" style="width: 260px;margin-bottom:4px;" class="plac-op" @change="changeId">
                        <el-option :label="item.address" :value="item.address" :key="item.address" v-for='(item,index) in userList'>
                            <span class="font-12">{{item.userName}}</span>
                            <span>{{item.address}}</span>
                        </el-option>
                    </el-select>
                    <span class="user-explain font-color-fff" v-show="userId">
                        (<span class="ellipsis-info">{{userId}}</span>)
                    </span>
                    <el-button v-if="isShowAddUserBtn" type="text" size="mini" @click="createUser()">{{$t('privateKey.addUser')}}</el-button>
                </td>
            </tr>
            <tr v-show="pramasData.length">
                <td style="vertical-align: top" class="text-right text-td">
                    <span class="font-color-fff">{{$t('text.parame')}}：</span>
                </td>
                <td>
                    <ul>
                        <li v-for="(item,index) in pramasData">
                            <template v-if="item.type=='string'">
                                <el-input v-model="transation.funcValue[index]" style="width: 260px;" :placeholder="item.type">
                                    <template slot="prepend" style="width: 51px;">
                                        <span>{{item.name}}</span>
                                    </template>
                                </el-input>
                            </template>
                            <template v-else>
                                <el-input v-model.trim="transation.funcValue[index]" style="width: 260px;" :placeholder="item.type">
                                    <template slot="prepend" style="width: 51px;">
                                        <span>{{item.name}}</span>
                                    </template>
                                </el-input>
                            </template>

                        </li>
                        <p class="font-color-ed5454" style="padding-top: 4px;">
                            <i class="el-icon-info" style="padding-right: 4px;"></i>{{$t('contracts.paramsInfo')}}
                        </p>
                    </ul>
                </td>
            </tr>
        </table>
        <div class="text-right send-btn java-class">
            <el-button @click="close">{{$t('dialog.cancel')}}</el-button>
            <el-button type="primary" @click="submit('transation')" :disabled='buttonClick'>{{$t('dialog.confirm')}}</el-button>
        </div>
        <el-dialog :title="$t('dialog.addUsername')" :visible.sync="creatUserNameVisible" :before-close="createUserClose" class="dialog-wrapper" width="640px" :center="true" :append-to-body="true">
            <v-createUser  @close='createUserClose'></v-createUser>
        </el-dialog>
    </div>
</template>
<script>
import { sendTransation, queryLocalKeyStores, findCnsInfo } from "@/util/api";
import createUser from "@/views/toolsContract/components/createUser";
import { isJson } from "@/util/util"
export default {
       components: {
        "v-createUser": createUser
    },
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
            userList: [],
            abiList: [],
            pramasData: [],
            funcList: [],
            buttonClick: false,
            contractVersion: this.version,
            constant: false,
            contractAddress: this.data.contractAddress || "",
            errorMessage: '',
            placeholderText: this.$t('placeholder.selectedAccountAddress'),
            pramasObj: null,
            stateMutability: '',
            isCNS: false,
            cnsList: [],
            cnsVersion: "",
            cnsName: "",
            isShowAddUserBtn: false,
            creatUserNameVisible: false,
        };
    },
    computed: {
        showUser() {
            let showUser = true;
            if (this.constant || this.stateMutability === 'view' || this.stateMutability === 'pure') {
                showUser = false
            }
            return showUser
        }
    },
    mounted: function () {

        this.getLocalKeyStores();
        this.formatAbi();
        this.changeFunc();
    },
    methods: {
        submit: function (formName) {
            
            if (this.isCNS) {
                if (!this.cnsName || !this.cnsVersion) {
                    this.$message({
                        type: "error",
                        message: this.$t('text.cnsNameVersion')
                    })
                    return
                } else {
                    this.send();
                }
            } else {
                this.send();
            }

        },
        close: function (formName) {
            this.$emit("close", false);
        },
        getLocalKeyStores() {
            queryLocalKeyStores()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.userList = data
                        if (this.userList.length) {
                            this.transation.userName = this.userList[0]['address']
                            this.userId = this.userList[0]['userName']
                        } else {
                            this.placeholderText = this.$t('placeholder.selectedNoUser')
                            this.isShowAddUserBtn = true;
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
        changeId: function (val) {
            this.userList.forEach(value => {
                if (val === value.address) {
                    this.userId = value.userName;
                }
            });
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
                        this.pramasObj = value
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
            this.transation.funcValue = [];
            this.constant = false;
            this.funcList.forEach(value => {
                if (value.funcId === this.transation.funcName) {
                    this.pramasData = value.inputs;
                    this.constant = value.constant;
                    this.pramasObj = value;
                    this.stateMutability = value.stateMutability;
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
                    if (data && isJson(data)) {
                        try {
                            this.transation.funcValue[i] = JSON.parse(data)
                        } catch (error) {
                            console.log(error)
                        }
                    } else {
                        this.transation.funcValue[i] = data;
                    }
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
                user: this.constant || this.stateMutability === 'view' || this.stateMutability === 'pure' ? '' : this.transation.userName,
                contractName: this.data.contractName,
                contractPath: this.data.contractPath,
                version: this.isCNS && this.cnsVersion ? this.cnsVersion : '',
                funcName: functionName || "",
                funcParam: this.transation.funcValue,
                contractAddress: this.isCNS ? "" : this.contractAddress,
                contractAbi: [this.pramasObj],
                useAes: false,
                useCns: this.isCNS,
                cnsName: this.isCNS && this.cnsName ? this.cnsName : ""
            };
            sendTransation(data)
                .then(res => {
                    this.buttonClick = false;
                    this.close();
                    const { data, status } = res
                    if (status === 200) {
                        let resData = data;
                        let successData = {
                            resData: resData,
                            input: {
                                name: functionName,
                                inputs: this.pramasData
                            },
                            data: this.pramasObj
                        }
                        this.$emit("success", Object.assign({}, successData, {
                            constant: this.constant
                        }));
                        if (this.constant || this.stateMutability === 'view' || this.stateMutability === 'pure') {
                            this.$message({
                                type: "success",
                                message: this.$t('text.searchSucceeded')
                            });
                        } else {
                            if (resData.status == '0x0') {
                                this.$message({
                                    type: "success",
                                    message: this.$t('text.txnSucceeded')
                                });
                            } else {
                                this.$message({
                                    type: "error",
                                    message: this.$t('text.txnFailed')
                                });
                            }
                        }

                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                        this.close();
                    }
                })
                .catch(err => {
                    this.buttonClick = false;
                    this.close();
                    this.$message({
                        type: "error",
                        message: this.$t('text.sendFailed')
                    });
                });
        },
        changeCns(val) {
            if (val) {
                this.queryFindCnsInfo()
            }
        },
        queryFindCnsInfo() {
            let param = {
                groupId: localStorage.getItem('groupId'),
                contractAddress: this.data.contractAddress
            }
            findCnsInfo(param)
                .then(res => {
                    const { data, status } = res
                    if (status === 200) {
                        if (data.data) {
                            this.cnsVersion = data.data.version
                            this.cnsName = data.data.cnsName
                        }
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        createUser(){
            this.creatUserNameVisible = true;
        },
        createUserClose(data){
             this.userList = data;
             if(this.userList.length > 0 ){
                this.isShowAddUserBtn = false;
                this.transation.userName = this.userList[0]['address']
                this.userId = this.userList[0]['userName']
             } 
             this.creatUserNameVisible = false;
        },
    }
};
</script>

<style scoped>
.send-wrapper {
    padding-left: 25px;
}
.send-btn >>> .el-button {
    padding: 9px 16px;
}
.java-class {
    margin-top: 10px;
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
.text-td {
    white-space: nowrap;
}
.el-input .el-input--medium {
}
</style>
