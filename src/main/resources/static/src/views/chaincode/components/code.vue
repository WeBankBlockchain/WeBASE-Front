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
    <div class="contract-code" :class="{changeActive:changeWidth }" v-loading="loading">
        <div class="contract-code-head">
            <span class="contract-code-title" v-show="codeShow" :class="{titleActive:changeWidth }">
                <span>{{contractName + '.sol'}}</span>
                <!-- <el-tooltip class="item" effect="dark" content="未保存,按Ctrl+s保存合约内容" placement="top-start">
                    <span v-show='saveShow' style="display:inline-block;width: 8px;height: 8px;border-radius:50%;background-color: #f00"></span>
                </el-tooltip> -->
            </span>
            <span class="contract-code-handle" v-show="codeShow">
                <span class="contract-code-done" @click="saveCode" v-if="!contractAddress">
                    <el-tooltip class="item" effect="dark" content="按Ctrl+s保存合约内容" placement="top-start">
                        <i class="wbs-icon-baocun contract-icon-style font-16"></i>
                    </el-tooltip>
                    <span>保存</span>
                </span>
                <span class="contract-code-done" @click="compile" v-if="!contractAddress">
                    <i class="wbs-icon-bianyi contract-icon-style font-16"></i>
                    <span>编译</span>
                </span>
                <span class="contract-code-done" @click="deploying" v-if="!contractAddress && abiFile && bin">
                    <i class="wbs-icon-deploy contract-icon-style font-16"></i>
                    <span>部署</span>
                </span>
                <span class="contract-code-done" v-if="contractAddress" @click="send">
                    <i class="wbs-icon-send contract-icon-style font-16"></i>
                    <span>发交易</span>
                </span>
                <span class="contract-code-done" v-if="!contractAddress && abiFile && bin || contractAddress " @click="downloadJavaClass">
                    <i class="el-icon-download contract-icon-style font-16"></i>
                    <span>导出java文件</span>
                </span>
            </span>
        </div>
        <div class="contract-code-content" :class="{infoHide: !successHide}">
            <div class="contract-code-mirror" :style="{height:codeHight}" ref="codeContent">
                <div style="padding-top: 60px;text-align:center;" v-show="!codeShow">
                    <span>请在左侧面板点击打开一个合约或新建一个合约</span>
                </div>
                <div class="ace-editor" ref="ace" v-show="codeShow"></div>
            </div>
            <div class="contract-info" v-show="successHide" :style="{height:infoHeight + 'px'}">
                <div class="move" @mousedown="dragDetailWeight($event)" @mouseup="resizeCode"></div>
                <div class="contract-info-title">
                    <!-- <i class="wbs-icon-clear float-right" @click="refreshMessage" title="清除"></i> -->
                </div>
                <div>
                    <div class="contract-info-list1" v-html="compileinfo">
                    </div>
                    <div class="contract-info-list1" style="color: #f00" v-show="errorInfo">
                        {{errorInfo}}
                    </div>
                    <div class="contract-info-list1" style="color: #f00" v-show="errorInfo">
                        <span style="display:inline-block;width:calc(100% - 120px);word-wrap:break-word">{{errorMessage}}</span>
                    </div>
                    <div style="color: #68E600;padding-bottom: 15px;" v-show="abiFileShow">{{successInfo}}</div>
                    <div class="contract-info-list" v-show="contractAddress">
                        <span class="contract-info-list-title" style="color: #0B8AEE">contractAddress </span>
                        <span style="display:inline-block;width:calc(100% - 120px);word-wrap:break-word">{{contractAddress}}</span>
                    </div>
                    <div class="contract-info-list" v-show="abiFile">
                        <span class="contract-info-list-title" style="color: #0B8AEE">contractName </span>
                        <span style="display:inline-block;width:calc(100% - 120px);word-wrap:break-word">{{contractName}}</span>
                    </div>
                    <div class="contract-info-list" v-show="abiFile">
                        <span class="contract-info-list-title" style="color: #0B8AEE">abi</span>
                        <span style="display:inline-block;width:calc(100% - 120px);word-wrap:break-word">{{abiFile}}</span>
                    </div>
                    <div class="contract-info-list" style="border-bottom: 1px solid #e8e8e8" v-show="bin">
                        <span class="contract-info-list-title" style="color: #0B8AEE">bin</span>
                        <span style="display:inline-block;width:calc(100% - 120px);word-wrap:break-word">{{bin}}</span>
                    </div>
                </div>
            </div>
        </div>
        <el-dialog title="发送交易" :visible.sync="dialogVisible" width="500px" :before-close="sendClose" v-if="dialogVisible" center class="send-dialog">
            <v-transaction @success="sendSuccess($event)" @close="handleClose" ref="send" :data="data" :abi='abiFile' :version='version'></v-transaction>
        </el-dialog>
        <el-dialog title="选择用户地址" :visible.sync="dialogUser" width="550px" v-if="dialogUser" center class="send-dialog">
            <v-user @change="deployContract($event)" @close="userClose" :abi='abiFile'></v-user>
        </el-dialog>
        <v-editor v-if='editorShow' :show='editorShow' :data='editorData' @close='editorClose'></v-editor>
        <el-dialog title="填写java包名" :visible.sync="javaClassDialogVisible" width="500px" center class="send-dialog" @close="initJavaClass">
            <el-input v-model="javaClassName"></el-input>
            <i style="color: #606266">如：com.webank</i>
            <div slot="footer" class="text-right send-btn">
                <el-button @click="closeJavaClass">取 消</el-button>
                <el-button type="primary" @click="sureJavaClass">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import ace from "ace-builds";
// import "ace-builds/webpack-resolver";
import "ace-builds/src-noconflict/theme-monokai";
import "ace-builds/src-noconflict/mode-javascript";
import "ace-builds/src-noconflict/ext-language_tools";
require("ace-mode-solidity/build/remix-ide/mode-solidity");
let Mode = require("ace-mode-solidity/build/remix-ide/mode-solidity").Mode;
import errcode from "@/util/errcode";
let Base64 = require("js-base64").Base64;
let wrapper = require("solc/wrapper");
let solc = wrapper(window.Module);
import constant from "@/util/constant";
import editor from "../dialog/editor";
import Bus from "@/bus";
import {
    addChaincode,
    getDeployStatus,
    setCompile,
    editChain,
    // addFunctionAbi,
    queryJavaClass
} from "@/util/api";
import transaction from "../dialog/sendTransaction";
import changeUser from "../dialog/changeUser";

export default {
    name: "codes",
    props: ["show", "changeStyle"],
    components: {
        "v-transaction": transaction,
        "v-user": changeUser,
        "v-editor": editor
    },
    data: function () {
        return {
            successHide: true,
            loading: false,
            content: "",
            successShow: true,
            errorShow: false,
            dialogVisible: false,
            javaClassDialogVisible: false,
            javaClassName: "",
            code: "",
            status: 0,
            abiFile: "",
            bin: "",
            contractAddress: "",
            contractName: "",
            infoHeight: 250,
            contractList: [],
            dialogUser: false,
            compileinfo: "",
            errorInfo: "",
            errorMessage: "",
            successInfo: "",
            abiFileShow: false,
            bytecodeBin: "",
            aceEditor: null,
            themePath: "ace/theme/monokai",
            modePath: "ace/mode/solidity",
            data: null,
            codeShow: false,
            version: "",
            saveShow: false,
            editorShow: false,
            editorData: null
        };
    },
    beforeDestroy: function () {
        Bus.$off("select");
        Bus.$off("noData");
    },
    mounted: function () {
        this.initEditor();
        Bus.$on("select", data => {
            this.codeShow = true;
            this.refreshMessage();
            this.code = "";
            this.version = "";
            this.status = null;
            this.abiFile = "";
            this.contractAddress = "";
            this.errorMessage = "";
            this.contractName = "";
            this.content = "";
            this.bin = "";
            this.data = data;
            this.code = Base64.decode(data.contractSource);
            this.content = this.code;
            this.aceEditor.setValue(this.content);
            this.status = data.contractStatus;
            this.abiFile = data.contractAbi;
            this.contractAddress = data.contractAddress;
            this.errorMessage = data.description || "";
            this.contractName = data.contractName;
            this.bin = data.contractBin;
            this.bytecodeBin = data.bytecodeBin || "";
            this.version = data.version;
        });
        Bus.$on("noData", data => {
            this.codeShow = false;
            this.refreshMessage();
            this.code = "";
            this.version = "";
            this.status = null;
            this.abiFile = "";
            this.contractAddress = "";
            this.errorMessage = "";
            this.contractName = "";
            this.content = "";
            this.bin = "";
        });
    },
    watch: {
        content: function (val) {
            let data = Base64.decode(this.data.contractSource);
            if (data != val) {
                this.saveShow = true;
            } else {
                this.saveShow = false;
            }
        },
        successHide: function (val) {
            if (val) {
                this.infoHeight = 250;
            } else {
                this.infoHeight = 0;
            }
        }
    },
    computed: {
        codeHight: function () {
            if (this.infoHeight) {
                return `calc(100% - ${this.infoHeight}px)`;
            } else {
                return `100%`;
            }
        },
        changeWidth() {
            if (this.changeStyle) {
                return this.changeStyle;
            } else {
                return false;
            }
        },
        tipShow() {
            return !this.show;
        }
    },
    methods: {
        initEditor: function () {
            let _this = this;
            this.aceEditor = ace.edit(this.$refs.ace, {
                fontSize: 14,
                fontFamily: "Consolas,Monaco,monospace",

                theme: this.themePath,
                mode: this.modePath,
                tabSize: 4,
                useSoftTabs: true
            });
            this.aceEditor.setOptions({
                enableSnippets: true,
                enableLiveAutocompletion: true,
                enableBasicAutocompletion: true,
                autoScrollEditorIntoView: true,
                copyWithEmptySelection: true
            });
            this.aceEditor.commands.addCommand({
                name: "myCommand",
                bindKey: { win: "Ctrl-S", mac: "Command-S" },
                exec: function (editor) {
                    if (_this.data.contractStatus != 2) {
                        _this.saveCode();
                    }
                }
            });
            let editor = this.aceEditor.alignCursors();
            this.aceEditor.getSession().setUseWrapMode(true);
            this.aceEditor.getSession().on("change", this.changeAce);
            this.aceEditor.on("blur", this.blurAce);
            this.aceEditor.resize();
        },
        blurAce: function () {
            let data = Base64.encode(this.content);
            if (
                this.data.contractSource != data &&
                this.data.contractStatus != 2
            ) {
                this.saveCode();
            }
        },
        saveCode: function () {
            this.data.contractSource = Base64.encode(this.content);
            Bus.$emit("save", this.data);
        },
        resizeCode: function () {
            this.aceEditor.setOptions({
                maxLines:
                Math.ceil(this.$refs.codeContent.offsetHeight / 17) - 1
            });
            this.aceEditor.resize();
        },
        dragDetailWeight: function (e) {
            let startY = e.clientY,
                infoHeight = this.infoHeight;
            document.onmousemove = e => {
                let moveY = startY - e.clientY;
                this.infoHeight = infoHeight + moveY;
            };
            document.onmouseup = e => {
                document.onmousemove = null;
                document.onmouseup = null;
            };
            this.aceEditor.setOptions({
                maxLines:
                Math.ceil(this.$refs.codeContent.offsetHeight / 17) - 1,
                minLines: 9
            });
        },
        sendSuccess: function (val) {
            this.dialogVisible = false;
            this.editorShow = true;
            this.editorData = null;
            this.editorData = val;
        },
        editorClose: function () {
            this.editorShow = false;
        },
        changeAce: function () {
            this.content = this.aceEditor.getSession().getValue();
        },

        findImports: function (path) {
            this.contractList = JSON.parse(
                localStorage.getItem("contractList")
            );
            let arry = path.split("/");
            let newpath = arry[arry.length - 1];
            let num = 0;
            if (arry.length > 1) {
                let newPath = arry[0];
                let oldPath = arry[arry.length - 1];
                let importArry = [];
                this.contractList.forEach(value => {
                    if (value.contractPath == newPath) {
                        importArry.push(value);
                    }
                });
                if (importArry.length) {
                    for (let i = 0; i < importArry.length; i++) {
                        if (oldPath == importArry[i].contractName + ".sol") {
                            return {
                                contents: Base64.decode(
                                    importArry[i].contractSource
                                )
                            };
                        }
                    }
                } else {
                    return { error: "File not found" };
                }
            } else {
                let newpath = arry[arry.length - 1];
                let newArry = [];
                this.contractList.forEach(value => {
                    if (value.contractPath == this.data.contractPath) {
                        newArry.push(value);
                    }
                });
                if (newArry.length > 1) {
                    for (let i = 0; i < newArry.length; i++) {
                        if (newpath == newArry[i].contractName + ".sol") {
                            return {
                                contents: Base64.decode(
                                    newArry[i].contractSource
                                )
                            };
                        }
                    }
                    for (let i = 0; i < this.contractList.length; i++) {
                        if (
                            newpath ==
                            this.contractList[i].contractName + ".sol"
                        ) {
                            return {
                                contents: Base64.decode(
                                    this.contractList[i].contractSource
                                )
                            };
                        } else {
                            num++;
                        }
                    }
                    if (num) {
                        return { error: "File not found" };
                    }
                } else {
                    for (let i = 0; i < this.contractList.length; i++) {
                        if (
                            newpath ==
                            this.contractList[i].contractName + ".sol"
                        ) {
                            return {
                                contents: Base64.decode(
                                    this.contractList[i].contractSource
                                )
                            };
                        }
                    }
                }
            }
        },
        compile: function () {
            this.loading = true;
            this.refreshMessage();
            for (let i = 0; i < constant.COMPILE_INFO.length; i++) {
                this.compileinfo = this.compileinfo + constant.COMPILE_INFO[i];
            }
            this.contractList = JSON.parse(
                localStorage.getItem("contractList")
            );
            let content = "";
            let output;
            let input = {
                language: "Solidity",
                settings: {
                    outputSelection: {
                        "*": {
                            "*": ["*"]
                        }
                    }
                }
            };
            input.sources = {};
            input.sources[this.contractName + ".sol"] = {};
            let libs = [];
            input.sources[this.contractName + ".sol"] = {
                content: this.content
            };
            try {
                output = JSON.parse(
                    solc.compileStandard(
                        JSON.stringify(input),
                        this.findImports
                    )
                );
            } catch (error) {
                this.errorInfo = "合约编译失败！";
                this.errorMessage = error;
                this.compileShow = true;
                this.loading = false;
            }
            setTimeout(() => {
                if (output && JSON.stringify(output.contracts) != "{}") {
                    this.status = 1;
                    if (output.contracts[this.contractName + ".sol"]) {
                        this.changeOutput(
                            output.contracts[this.contractName + ".sol"]
                        );
                    }
                } else {
                    this.errorMessage = output.errors[0];
                    this.errorInfo = "合约编译失败！";
                    this.loading = false;
                }
            }, 500);
        },
        changeOutput: function (obj) {
            let arry = [];
            let data = null;
            if (JSON.stringify(obj) !== "{}") {
                for (const key in obj) {
                    arry.push(obj[key]);
                }
                if (arry.length) {
                    this.abiFileShow = true;
                    this.successInfo = "< 编译成功！";
                    this.abiFile = arry[0].abi;
                    this.abiFile = JSON.stringify(this.abiFile);
                    this.bin = arry[0].evm.deployedBytecode.object;
                    this.bytecodeBin = arry[0].evm.bytecode.object;
                    this.data.contractAbi = this.abiFile;
                    this.data.contractBin = this.bin;
                    this.data.contractSource = Base64.encode(this.content);
                    this.$set(this.data, "bytecodeBin", this.bytecodeBin);
                    this.loading = false;
                    Bus.$emit("compile", this.data);
                } else {
                    this.errorInfo = "合约编译失败！";
                    this.compileShow = true;
                    this.loading = false;
                }
            } else {
                this.errorInfo = "合约编译失败！";
                this.compileShow = true;
                this.loading = false;
            }
        },
        refreshMessage: function () {
            this.abiFileShow = false;
            this.errorInfo = "";
            this.compileinfo = "";
            this.abiFile = "";
            this.contractAddress = "";
        },
        deploying: function () {
            this.dialogUser = true;
        },
        userClose: function () {
            this.dialogUser = false;
        },
        setMethod: function () {
            let web3 = new Web3(Web3.givenProvider);
            let arry = [];
            if (this.abiFile) {
                let list = JSON.parse(this.abiFile);
                list.forEach(value => {
                    if (value.name && value.type == "function") {
                        let data = {};
                        let methodId = web3.eth.abi.encodeFunctionSignature({
                            name: value.name,
                            type: value.type,
                            inputs: value.inputs
                        });
                        data.methodId = methodId;
                        data.abiInfo = JSON.stringify(value);
                        data.methodType = value.type;
                        arry.push(data);
                    } else if (value.name && value.type == "event") {
                        let data = {};
                        let methodId = web3.eth.abi.encodeEventSignature({
                            name: value.name,
                            type: value.type,
                            inputs: value.inputs
                        });
                        data.methodId = methodId;
                        data.abiInfo = JSON.stringify(value);
                        data.methodType = value.type;
                        arry.push(data);
                    }
                });
                if (arry.length) {
                    //    this.addAbiMethod(arry)
                }
            }
        },
        addAbiMethod: function (list) {
            let data = {
                groupId: localStorage.getItem("groupId"),
                methodList: list
            };
            addFunctionAbi(data)
                .then(res => {
                    if (res.data.code === 0) {
                        console.log("method 保存成功！");
                    } else {
                        this.$message({
                            message: errcode.errCode[res.data.code].cn || "保存失败！",
                            type: "error"
                        });
                    }
                })
                .catch(err => {
                    message(constant.ERROR, "error");
                });
        },
        deployContract(val) {
            this.loading = true;
            let reqData = {
                groupId: localStorage.getItem("groupId"),
                contractBin: this.bin,
                bytecodeBin: this.bytecodeBin,
                abiInfo: JSON.parse(this.abiFile),
                contractSource: Base64.encode(this.content),
                user: val.userId,
                contractName: this.contractName,
                contractId: this.data.id,
                contractPath: this.data.contractPath,
                useAes: false
            };
            this.version = val.version;
            if (val.params.length) {
                reqData.funcParam = val.params;
            }
            this.setMethod();
            getDeployStatus(reqData)
                .then(res => {
                    this.loading = false;
                    const { data, status } = res;
                    if (status === 200) {
                        this.abiFileShow = true;
                        this.status = 2;
                        this.contractAddress = data;
                        this.successInfo = "< 部署成功！";
                        this.$message({
                            message: "合约部署成功！",
                            type: "success"
                        });
                        this.data.contractSource = Base64.encode(this.content);
                        this.data.contractAddress = this.contractAddress;
                        this.data.contractVersion = this.version;
                        Bus.$emit("deploy", this.data);
                    } else {
                        this.status = 3;
                        this.$message({
                            message: errcode.errCode[res.data.code].cn || "部署失败！",
                            type: "error"
                        });
                    }
                })
                .catch(err => {
                    this.status = 3;
                    this.loading = false;
                    this.$message({
                        message: "系统错误",
                        type: "error"
                    });
                });
        },
        // addContract: function() {
        //     this.loading = true;
        //     let reqData = {
        //         groupId: localStorage.getItem("groupId"),
        //         contractName: this.contractName,
        //         contractBin: this.bin,
        //         bytecodeBin: this.bytecodeBin,
        //         contractVersion: this.data.contractVersion,
        //         contractSource: Base64.encode(this.content)
        //     };
        //     if (this.abiFile) {
        //         reqData.contractAbi = this.abiFile;
        //     }
        //     addChaincode(reqData)
        //         .then(res => {
        //             this.loading = false;
        //             if (res.data.code === 0) {
        //                 this.status = res.data.data.contractStatus;
        //                 this.abiFile = res.data.data.contractAbi || "";
        //                 this.contractAddress = res.data.data.contractAddress || "";
        //                 this.$message({
        //                     message: "合约保存成功！",
        //                     type: "success"
        //                 });
        //                 this.$emit("add", res.data.data);
        //             } else {
        //                 this.$message({
        //                     message: errcode.errCode[res.data.code].cn,
        //                     type: "error"
        //                 });
        //             }
        //         })
        //         .catch(err => {
        //             this.loading = false;
        //             this.$message({
        //                 message: "系统错误",
        //                 type: "error"
        //             });
        //         });
        // },
        // editContract: function() {
        //     let reqData = {
        //         groupId: localStorage.getItem("groupId"),
        //         contractId: this.data.contractId,
        //         contractBin: this.bin,
        //         bytecodeBin: this.bytecodeBin,
        //         contractSource: Base64.encode(this.content)
        //     };
        //     if (this.abiFile) {
        //         reqData.contractAbi = this.abiFile;
        //     }
        //     editChain(reqData)
        //         .then(res => {
        //             if (res.data.code === 0) {
        //                 this.$message({
        //                     message: "合约保存成功！",
        //                     type: "success"
        //                 });
        //                 this.$emit("add", res.data.data);
        //             } else {
        //                 this.$message({
        //                     message: errcode.errCode[res.data.code].cn,
        //                     type: "error"
        //                 });
        //             }
        //         })
        //         .catch(err => {
        //             this.$message({
        //                 message: "系统错误",
        //                 type: "error"
        //             });
        //         });
        // },
        foldInfo: function (val) {
            this.successHide = val;
        },
        send: function () {
            this.dialogVisible = true;
        },
        handleClose: function () {
            this.dialogVisible = false;
        },
        sendClose: function () {
            this.$refs.send.close();
        },

        downloadJavaClass: function (formName) {
            this.javaClassDialogVisible = true;
        },
        closeJavaClass: function() {
            this.javaClassDialogVisible = false;
            this.initJavaClass()
        },
        initJavaClass: function() {
            this.javaClassName = ""
        },
        sureJavaClass: function () {
            this.$confirm(`<span class="font-12 font-color-ed5454 el-icon-warning">请保证合约名和文件名一致</span><br>确认导出？`, {
                center: true,
                dangerouslyUseHTMLString: true
            })
                .then(() => {
                    this.getJavaClass();
                })
                .catch(() => {

                });
        },
        getJavaClass: function () {
            let reqData = {
                contractName: this.contractName,
                abiInfo: JSON.parse(this.abiFile),
                contractBin: this.bytecodeBin,
                packageName: this.javaClassName,

            };
            queryJavaClass(reqData)
                .then(res => {
                    const { data, status } = res
                    if (status === 200) {
                        const content = data
                        const blob = new Blob([content])
                        const fileName = this.contractName ? `${this.contractName}.java` : `undefined.java`
                        if ('download' in document.createElement('a')) { // 非IE下载
                            const elink = document.createElement('a')
                            elink.download = fileName
                            elink.style.display = 'none'
                            elink.href = URL.createObjectURL(blob)
                            document.body.appendChild(elink)
                            elink.click()
                            URL.revokeObjectURL(elink.href) // 释放URL 对象
                            document.body.removeChild(elink)
                        } else { // IE10+下载
                            navigator.msSaveBlob(blob, fileName)
                        }
                        this.closeJavaClass()
                    } else {

                        this.$message({
                            message: errcode.errCode[res.data.code].cn || "下载失败！",
                            type: "error"
                        });
                    }

                })
                .catch(err => {
                    this.$message({
                        message: "系统错误",
                        type: "error"
                    });
                });
        }
    }
};
</script>
<style scoped>
.contract-code {
    height: 100%;
    border-left: 1px solid #242e42;
    box-sizing: border-box;
}
.changeActive {
    padding-left: 0 !important;
}
.contract-code-head {
    width: 100%;
    height: 48px;
    line-height: 48px;
    color: #fff;
    border-bottom: 2px solid #242e42;
    background-color: #2b374d;
    overflow: hidden;
}
.contract-code-done {
    display: inline-block;
    margin-right: 10px;
    cursor: pointer;
}
.contract-code-done i {
    vertical-align: middle;
}
.contract-code-done span {
    font-size: 12px;
    color: #fff;
    vertical-align: middle;
}
.contract-no-content {
    border-left: 1px solid #242e42;
    height: calc(100% - 50px);
    box-sizing: border-box;
}
.contract-code-content {
    border-left: 1px solid #242e42;
    height: calc(100% - 50px);
    box-sizing: border-box;
}
.contract-code-mirror {
    width: 100%;
    height: 70%;
}
.contract-info {
    position: relative;
    padding-top: 20px;
    text-align: left;
    border-top: 1px solid #242e42;
    box-sizing: border-box;
    overflow: auto;
}
.contract-info-content {
    height: 100%;
    overflow-y: scroll;
    box-sizing: border-box;
}
.contract-code-title {
    float: left;
    font-weight: bold;
    font-size: 18px;
    /* color: #36393d; */
    padding-left: 20px;
}
.contract-code-handle {
    float: right;
    padding-right: 20px;
}
.contract-info-title {
    padding-right: 20px;
}
.move {
    position: absolute;
    width: 100%;
    height: 3px;
    top: 0;
    left: 0;
    z-index: 9999;
    cursor: s-resize;
}
.contract-info-title i {
    padding-left: 8px;
    font-size: 10px;
    color: #aeb1b5;
    cursor: pointer;
}
.contract-info-title span {
    font-size: 16px;
    font-weight: bold;
    color: #36393d;
}
.contract-info-list {
    padding: 5px 20px;
    width: 90%;
    margin: 0 auto;
    border: 1px solid #242e42;
    border-bottom: none;
}
.contract-info-list-title {
    display: inline-block;
    width: 100px;
    vertical-align: top;
}
.ace-editor {
    height: 100% !important;
    position: relative;
    text-align: left;
    letter-spacing: 0.1px;
    text-rendering: geometricPrecision;
    font-feature-settings: "liga" 0;
    font-variant-ligatures: none;
    font: 14px/normal "Monaco", "Menlo", "Ubuntu Mono", "Consolas",
        "source-code-pro", monospace !important;
}
.ace-editor>>>.ace_print-margin {
    display: none;
    text-rendering: geometricPrecision;
}
.infoHide {
    height: calc(100% - 50px);
}
.code-spread {
    position: absolute;
    width: 33px;
    height: 33px;
    line-height: 33px;
    left: 412px;
    bottom: 0;
    border: 1px solid #242e42;
    color: #aeb1b5;
    background-color: #fff;
    text-align: center;
    z-index: 9999;
    cursor: pointer;
}
.code-spread i {
    font-size: 12px;
}
.contract-info {
    background-color: #2b374d;
    color: #fff;
}
.titleActive {
    padding-left: 40px;
}
.send-dialog>>>.el-dialog--center .el-dialog__body {
    padding: 5px 25px 20px;
}

.send-btn>>>.el-button {
    padding: 9px 16px;
}
.send-dialog>>>.el-input__inner {
    height: 32px;
    line-height: 32px;
}
</style>
