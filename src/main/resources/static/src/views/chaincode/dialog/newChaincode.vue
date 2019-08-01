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
        <el-dialog title="上传合约" :visible.sync="dialogVisible" width="575px" :before-close="modelClose" class="dialog-add">
            <el-steps :active="active" finish-status="success" align-center class="dialog-step">
                <el-step title="版本号"></el-step>
                <el-step title="上传"></el-step>
                <el-step title="确认信息"></el-step>
            </el-steps>
            <div v-show="active === 0" style="margin: 28px 0 30px 68px;position: relative;" class="base-input-placeholder">
                <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="合约版本" prop="version" style="width: 278px">
                        <el-input v-model="ruleForm.version" placeholder="请输入版本号" maxlength="12"></el-input>
                    </el-form-item>
                </el-form>
                <el-tooltip effect="dark" placement="right">
                    <div slot="content">
                        <p>合约版本规则：<br>(1) 12位以内数字小写字母以及“.”<br>(2) 数字或者小写字母开头，不能以“.”结尾<br>(3) 不能出现连续的“.”</p>
                    </div>
                    <i class="el-icon-info icon-info-base"></i>
                </el-tooltip>
            </div>
            <div v-show="active === 1" style="position: relative;text-align: left;padding:30px 0 30px 102px;font-site:12px;" class="base-input-placeholder">
                <span style="display: inline-block;margin-right:8px">选择文件</span>
                <el-input v-model="file" placeholder="请点击选择.sol格式合约" style="width: 178px;" class="unload-input"></el-input>
                <i class="wbs-icon-baocun1 font-color-333" style="margin-left:8px;"></i>
                <span style="color: #ed5454;display: inline-block;text-align: left" v-show="fileErrShow">{{fileErr}}</span>
                <input type="file" id="file" name="chaincodes" class="checkContract-upload" @change="upLoad($event)" />
            </div>
            <div v-show="active === 2" style="position: relative;text-align: left;padding:13px 0 30px 100px" class="base-input-placeholder">
                <ul class="info-wrapper">
                    <li class="chaincode-info">
                        <span class="title">区块链名称：</span>
                        <span class="text">{{networkName}}</span>
                    </li>
                    <li class="chaincode-info">
                        <span class="title">合约名称：</span>
                        <span class="text">{{filename}}</span>
                    </li>
                    <li class="chaincode-info">
                        <span class="title">合约版本：</span>
                        <span class="text">{{ruleForm.version}}</span>
                    </li>
                </ul>
            </div>
            <div class="btn-group">
                <el-button v-if="active > 0" @click="prev" class="prev-btn">{{prevButton}}</el-button>
                <el-button type="primary" @click="next" class="next-btn" v-html="nextButton"></el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
let Base64 = require("js-base64").Base64;
import { addChaincode } from "@/util/api";
import errcode from "@/util/errcode";

export default {
    name: "newchaincode",
    props: ["dialogShow"],
    data() {
        return {
            dialogVisible: this.dialogShow,
            active: 0,
            file: null,
            nextButton: "下一步",
            prevButton: "上一步",
            fileErr: "",
            fileErrShow: false,
            fileString: "",
            filename: "",
            networkName: localStorage.getItem("networkName") || "-",
            ruleForm: {
                version: ""
            },
            rules: {
                version: [
                    { required: true, message: "请输入版本号", trigger: "blur" },
                    {
                        pattern: /^[a-z0-9]+(?:[.][a-z0-9]+)*$/,
                        message: "版本号不符合规则",
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 12,
                        message: "长度在 1 到 12 个字符",
                        trigger: "blur"
                    }
                ]
            }
        };
    },
    methods: {
        modelClose: function () {
            this.$emit("close", false);
        },
        next: function () {
            if (this.active === 0) {
                this.$refs.ruleForm.validate(valid => {
                    if (valid) {
                        this.active++;
                    } else {
                        return false;
                    }
                });
            } else if (this.active === 2) {
                this.$confirm("确认提交？", { center: true })
                    .then(_ => {
                        this.addChaincode();
                    })
                    .catch(_ => {
                        this.modelClose();
                    });
            } else if (this.active === 1) {
                if (this.filename === "") {
                    this.fileErrShow = true;
                    this.fileErr = "请上传合约";
                } else {
                    this.fileErrShow = false;
                    this.fileErr = "";
                    this.active++;
                    this.nextButton = "创&nbsp&nbsp&nbsp建";
                }
            }
        },
        prev: function () {
            if (this.active > 0) {
                this.active--;
                this.nextButton = "下一步";
            }
        },
        addChaincode: function () {
            let groupId = localStorage.getItem("groupId");
            let reqData = {
                groupId: groupId,
                contractName: this.filename,
                contractVersion: this.ruleForm.version,
                contractSource: this.fileString
            };
            addChaincode(reqData)
                .then(res => {
                    if (res.data.code === 0) {
                        this.$emit("success", res.data.data);
                        this.modelClose();
                    } else {
                        this.$message({
                            message: errcode.errCode[res.data.code].cn,
                            type: "error"
                        });
                        this.modelClose();
                    }
                })
                .catch(err => {
                    this.$message({
                        message: "合约上传失败！",
                        type: "error"
                    });
                    this.modelClose();
                });
        },
        upLoad: function (e) {
            if (!e.target.files.length) {
                return;
            }
            this.filename = "";
            this.fileString = "";
            let files = e.target.files[0];
            let filessize = Math.ceil(files.size / 1024);
            let filetype = files.name.split(".")[1];
            if (filessize > 400) {
                this.fileErrShow = true;
                this.fileErr = "文件大小超过400k，请上传小于400k的文件";
            } else if (filetype !== "sol") {
                this.fileErrShow = true;
                this.fileErr = "请上传.sol格式的文件";
            } else {
                this.fileErrShow = false;
                this.fileErr = "";
                this.filename = files.name.split(".")[0];
                this.file = files.name;
                let reader = new FileReader();
                reader.readAsText(files, "UTF-8");
                let _this = this;
                reader.onload = function (evt) {
                    _this.fileString = Base64.encode(evt.target.result);
                };
            }
        }
    }
};
</script>
<style scoped>
.checkContract-upload {
    display: block;
    position: absolute;
    height: 30px;
    padding: 10px 0;
    left: 0;
    width: 100%;
    margin-top: -40px;
    opacity: 0;
    z-index: 99;
    cursor: pointer;
}
.chaincode-info {
    line-height: 18px;
}
.chaincode-info .title {
    display: inline-block;
    width: 78px;
    font-size: 12px;
    color: #9da2ab;
}
.chaincode-info .text {
    font-size: 12px;
    color: #737a86;
}
.dialog-step {
    padding: 0 47px !important;
}
.demo-ruleForm>>>.el-input__inner:focus,
.unload-input>>>.el-input__inner:focus {
    border-color: #2956a3;
}
.demo-ruleForm>>>.el-input__inner:active {
    border-color: #2956a3;
}
.demo-ruleForm>>>.el-form-item.is-success .el-input__inner,
.demo-ruleForm .el-form-item.is-success .el-input__inner:focus {
    border-color: #2956a3;
}
.checkContract-upload:focus {
    border-color: #2956a3;
}
.demo-ruleForm>>>.el-form-item.is-required:not(.is-no-asterisk)
    > .el-form-item__label:before {
    color: #2956a3;
}
.demo-ruleForm>>>.el-form-item__label {
    font-size: 12px;
    padding: 0 10px 0 0;
}
.demo-ruleForm>>>.el-input__inner,
.unload-input>>>.el-input__inner {
    height: 36px;
    line-height: 36px;
}
.btn-group {
    text-align: right;
}
.next-btn,
.prev-btn {
    padding: 11px 13px;
    font-size: 12px;
}
.base-input-placeholder>>>.el-input {
    font-size: 12px;
}
.icon-info-base {
    position: absolute;
    top: 14px;
    left: 290px;
}
</style>
