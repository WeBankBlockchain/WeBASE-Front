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
    <div style="padding: 0">
        <el-tabs type="border-card" @tab-click="handleClick">
            <el-tab-pane label="input">
                <div>
                    <div class="item">
                        <span class="label">TxHash:</span>
                        <span>{{transactionData.hash}}</span>
                    </div>
                    <div class="item">
                        <span class="label">Block Height:</span>
                        <span>{{transactionData.blockNumber}}</span>
                    </div>
                    <div class="item">
                        <span class="label">From:</span>
                        <span>{{transactionData.from}}</span>
                    </div>
                    <div class="item">
                        <span class="label">To:</span>
                        <span>{{transactionData.to}}</span>
                    </div>
                    <div class="item" style="font-size: 0">
                        <span class="label">Input:</span>
                        <div class="detail-input-content">
                            <span v-if="!showDecode" class="input-data">{{transactionData.input}}</span><br v-if="!showDecode">
                            <div v-if="showDecode">
                                <div class="input-label">
                                    <span class="label">function</span>
                                    <span>{{funcData + "(" + abiType + ")"}}</span>
                                </div>
                                <div class="input-label">
                                    <span class="label">methodId</span>
                                    <span>{{methodId}}</span>
                                </div>
                                <div class="input-label">
                                    <span class="label">data</span>
                                    <el-table :data="inputData" v-if="inputData.length" style="display:inline-block;width:400px">
                                        <el-table-column prop="name" label="name" align="center" v-if="inputData[0].name"></el-table-column>
                                        <el-table-column prop="type" label="type" align="center"></el-table-column>
                                        <el-table-column prop="data" label="data" align="center" :show-overflow-tooltip="true"></el-table-column>
                                    </el-table>
                                </div>
                            </div>
                        </div>
                        <div class="item">
                            <span class="label"></span>
                            <el-button @click="decode" type="primary">{{buttonTitle}}</el-button>
                        </div>
                    </div>
                </div>
            </el-tab-pane>
            <el-tab-pane label="event" v-if="eventLog.length > 0" @click="decodeEvent">
                <div v-for="item in eventLog" v-if="eventSHow">
                    <div class="item">
                        <span class="label">Address :</span>
                        <span>{{item.address}}</span>
                    </div>
                    <div class="item">
                        <span class="label">Name :</span>
                        <span>{{item.eventName}}</span>
                    </div>
                    <div class="item">
                        <span class="label">Topics :</span>
                        <div style="display: inline-block;width:800px" v-for="(val,index) in item.topics ">[{{index}}] {{val}}</div>
                    </div>
                    <div class="item">
                        <span class="label">Data :</span>
                        <div class="detail-input-content">
                            <span class="input-data">{{item.data}}</span>
                        </div>
                    </div>
                </div>
            </el-tab-pane>
        </el-tabs>
        
    </div>
</template>
<script>
import { getTransactionReceipt } from "@/util/api";
import { getContractList,getByteCode } from "@/util/api";
import errcode from "@/util/errcode";

export default {
    name: "transactionDetail",
    props: ["data", "conTract"],
    data: function() {
        return {
            detail: null,
            transactionData: this.data,
            contractList: [],
            decodeData: {},
            funcData: "",
            showDecode: false,
            buttonTitle: "解码",
            abiType: [],
            methodId: "",
            inputData: [],
            eventLog: [],
            buttonSHow: false,
            eventSHow: false
        };
    },
    mounted: function() {
        this.getContacts(this.decodeAbi);
        this.getAdderss();
    },
    destroyed: function() {
        localStorage.setItem("transaction", "");
    },
    methods: {
        handleClick(tab, event) {
            if(tab.label == "event"){
                this.decodeEvent();
            }
        },
        decode: function() {
            if (this.showDecode) {
                this.buttonTitle = "解码";
                this.showDecode = false;
            } else {
                this.buttonTitle = "还原";
                this.showDecode = true;
            }
        },
        getBin: function(adr,blockHeight){
            let data = {
                address: adr,
                blockNumber: blockHeight
            }
            getByteCode(data).then(response => {
                if(response.data.code === 0){
                    let resdata = response.data.data.code
                    return resdata
                }else{
                    this.$message({
                        type: "error",
                        message: errcode.errCode[response.data.code].cn
                    });
                    return
                }
            }).catch(err => {
                    this.$message({
                        type: "error",
                        message: "系统错误!"
                    });
                    return
                });
        },
        getContacts: function(callback) {
            let reqdata = {
                networkId: localStorage.getItem("networkId"),
                pageSize: 1,
                pageNumber: 1000
            };
            getContractList(reqdata, {})
                .then(res => {
                    if (res.data.code === 0) {
                        this.contractList = res.data.data || [];
                        callback();
                    } else {
                        this.$message({
                            type: "error",
                            message: errcode.errCode[res.data.code].cn
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: "系统错误!"
                    });
                });
        },
        decodeAbi: function() {
            let input = this.transactionData.input;
            let transactionTo = this.transactionData.to;
            if (transactionTo) {
                this.decodefun(input, transactionTo);
            } else {
                this.methodId = input.substring(0, 10);
                this.getAdderss();
            }
        },
        getAdderss: function() {
            let data = {
                transHash: this.transactionData.hash
            };
            getTransactionReceipt(data)
                .then(res => {
                    if (res.data.code === 0) {
                        this.eventLog = res.data.data.logs;
                        let bin = this.getBin(res.data.data.contractAddress,res.data.data.blockNumber)
                        this.decodeDeloy(res.data.data.contractAddress);
                    } else {
                        this.$message({
                            type: "error",
                            message: errcode.errCode[res.data.code].cn
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: "系统错误！"
                    });
                });
        },
        decodeEvent: function() {
            let web3 = new Web3(Web3.givenProvider);
            if (this.eventLog.length) {
                this.eventSHow = true;
                let abi = "";
                let abiData = {};
                for(let i = 0; i < this.eventLog.length; i++){
                    this.contractList.forEach(val => {
                        if(val.contractAddress === this.eventLog[i].address){
                            if(val.contractAbi){
                                this.eventLog[i].abi = JSON.parse(val.contractAbi);
                            }else{
                                this.eventLog[i].abi = [];
                            }  
                        }
                    })
                };
                this.eventLog.forEach(val => {
                    if(val.abi.length){
                        val.abi.forEach(value => {
                            if(value.type == "event"){
                                val.eventName = value.name + "("
                                for(let i = 0; i < value.inputs.length; i++){
                                    if(value.inputs[i].indexed != undefined){
                                        val.eventName = val.eventName + value.inputs[i].indexed + " "
                                    }
                                    if(i == value.inputs.length -1){
                                        val.eventName = val.eventName + value.inputs[i].type + " " + value.inputs[i].name
                                    }else{
                                        val.eventName = val.eventName + value.inputs[i].type + " " + value.inputs[i].name + ","
                                    } 
                                }
                                val.eventName =  val.eventName + ")"
                                let eventData = web3.eth.abi.decodeLog(value.inputs,val.data,val.topics.slice(1));
                            }
                        })
                    }
                })
            } else {
                this.eventSHow = false;
            }
        },
        decodefun: function(input, adr) {
            let web3 = new Web3(Web3.givenProvider);
            let data = input.substring(0, 10);
            this.methodId = data;
            let inputDatas = "0x" + input.substring(10);
            let abi = "";
            let abiData = {};
            if (this.contractList.length) {
                this.contractList.forEach(value => {
                    if (value.contractAddress === adr) {
                        abi = value.contractAbi;
                        this.buttonSHow = true;
                    }
                });
            }
            if (abi) {
                abiData = JSON.parse(abi);
                abiData.forEach(value => {
                    value.encode = web3.eth.abi.encodeFunctionSignature({
                        name: value.name,
                        type: value.type,
                        inputs: value.inputs
                    });
                });
                abiData.forEach(value => {
                    if (value.encode === data) {
                        value.inputs.forEach((val, index) => {
                            if (val && val.type && val.name) {
                                this.abiType[index] = val.type + " " + val.name;
                            } else if (val && val.name) {
                                this.abiType[index] = val.name;
                            } else if (val && val.type) {
                                this.abiType[index] = val.type;
                            } else if (val) {
                                this.abiType[index] = val;
                            }
                        });
                        this.funcData = value.name;
                        if (value.inputs.length) {
                            this.decodeData = web3.eth.abi.decodeParameters(value.inputs,inputDatas);
                            if (JSON.stringify(this.decodeData) != "{}") {
                                for (const key in this.decodeData) {
                                    value.inputs.forEach((val, index) => {
                                        if (val && val.name && val.type) {
                                            if (key === val.name) {
                                                this.inputData[index] = {};
                                                this.inputData[index].name =val.name;
                                                this.inputData[index].type =val.type;
                                                this.inputData[index].data = this.decodeData[key];
                                            }
                                        } else if (val) {
                                            if (index == key) {
                                                this.inputData[index] = {};
                                                this.inputData[index].type = val;
                                                this.inputData[index].data = this.decodeData[key];
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        },
        //部署合约产生的交易解码
        decodeDeloy: function(val) {
            let abi = "";
            let contractName = "";
            let input = {};
            if (this.contractList.length) {
                this.contractList.forEach(value => {
                    if (value.contractAddress === val) {
                        abi = value.contractAbi;
                        contractName = value.contractName;
                        this.buttonSHow = true;
                    }
                });
            }
            if (abi) {
                input = JSON.parse(abi);
                if (input.length > 0) {
                    input.forEach(value => {
                        if (value.type === "constructor") {
                            this.funcData = contractName;
                            value.inputs.forEach((item, index) => {
                                if (item && item.type && item.name) {
                                    this.abiType[index] =
                                        item.type + " " + item.name;
                                } else if (item && item.name) {
                                    this.abiType[index] = item.name;
                                } else if (item && item.type) {
                                    this.abiType[index] = item.type;
                                } else if (item) {
                                    this.abiType[index] = item;
                                }
                            });
                        }
                    });
                }
            }
        }
    }
};
</script>
<style scoped>
.label {
    display: inline-block;
    width: 120px;
    font-weight: bold;
    vertical-align: top;
    font-size: 12px;
}
.item {
    padding: 8px 0;
}
.input {
    display: inline-block;
    font-size: 0;
    vertical-align: top;
}
.input span {
    font-size: 12px;
}
.input-data {
    display: inline-block;
    width: 100%;
    word-break: break-all; /* 支持IE和chrome，FF不支持*/
    word-wrap: break-word; /* 以上三个浏览器均支持 */
    max-height: 66px;
    overflow-y: auto;
}
.input-label {
    padding-bottom: 15px;
}
.input-label .label {
    width: 80px;
    font-size: 12px;
}
.detail-input-content {
    display: inline-block;
    width: 800px;
    padding: 10px;
    height: auto;
    border: 1px solid #eaedf3;
    border-radius: 4px;
    font-size: 12px;
}
</style>


