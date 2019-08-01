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
        <el-form :model="nodesForm" :rules="rules" ref="nodesForm" label-width="108px" class="demo-ruleForm">
            <el-form-item label="节点IP" prop="nodeIp" style="width: 330px;">
                <el-input v-model="nodesForm.nodeIp" placeholder="请输入IP" :disabled="nodesForm['disabled']"></el-input>
            </el-form-item>
            <el-form-item label="前置服务端口" prop="frontPort" style="width: 330px;" v-if="nodesForm['dShow']">
                <el-input v-model="nodesForm.frontPort" placeholder="请输入前置服务端口" :disabled="nodesForm['disabled']"></el-input>
            </el-form-item>
            <!-- <el-form-item label="所属机构" prop="org" v-if="nodesForm['dShow']">
                <el-radio-group v-model="nodesForm.org">
                    <el-radio label="1">本机构</el-radio>
                    <el-radio label="2">其它机构</el-radio>
                </el-radio-group>
            </el-form-item> -->
        </el-form>
        <div class="dialog-footer">
            <el-button @click="modelClose">取 消</el-button>
            <el-button type="primary" @click="submit('nodesForm')" :loading="loading">确 定</el-button>
        </div>
    </div>
</template>

<script>
import { addnodes, deleteNodes } from "@/util/api";
const sha256 = require("js-sha256").sha256;
export default {
    name: "nodesDialog",
    props: {
        nodesDialogOptions: {
            type: Object
        }
    },
    watch: {
        "nodesDialogOptions.type": {
            handler(val) {
                this.type = val;
                switch (val) {
                    case "creat":
                        this.nodesForm = {
                            nodeIp: "",
                            org: "1",
                            frontPort: "",
                            disabled: false,
                            mDisabled: false,
                            dShow: true
                        };
                        break;
                    case "delete":
                        this.nodesForm = {
                            nodeIp: this.nodesDialogOptions.data["nodeIp"],
                            nodeId: this.nodesDialogOptions.data["nodeId"],
                            disabled: true,
                            mDisabled: true,
                            dShow: false
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
            type: this.nodesDialogOptions.type,
            loading: false,
            nodesForm: {},
            rules: {
                nodeIp: [
                    {
                        required: true,
                        message: "请输入IP",
                        trigger: "blur"
                    },
                    {
                        pattern: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/,
                        message: "IP格式不正确",
                        trigger: "blur"
                    }
                ],
                frontPort: [
                    {
                        required: true,
                        message: "请输入端口",
                        trigger: "blur"
                    },
                    {
                        pattern: /^[1-9]\d*$/,
                        message: "请输入正确端口号",
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
    mounted() {},
    methods: {
        modelClose: function() {
            this.nodesForm = Object.assign({
                nodeIp: "",
                frontPort: ""
            });
            this.$emit("close", false);
        },
        submit: function(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.loading = true;
                    this.getAllAccountInfo();
                } else {
                    return false;
                }
            });
        },
        getAllAccountInfo: function() {
            let type = this.type;
            switch (type) {
                case "creat":
                    this.getCreatNodesInfo();
                    break;
                case "delete":
                    this.getDeleteNodesInfo();
                    break;
            }
        },
        getCreatNodesInfo: function() {
            let networkId = localStorage.getItem("networkId");
            let reqData = {
                networkId: networkId,
                nodeIp: this.nodesForm.nodeIp,
                nodeType: this.nodesForm.org,
                frontPort: this.nodesForm.frontPort
            };
            addnodes(reqData, {})
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
                            this.errcode.errCode[err.data.code].cn || "系统错误"
                    });
                });
        },
        getDeleteNodesInfo: function() {
            deleteNodes(this.nodesForm.nodeId, {})
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
                            this.errcode.errCode[err.data.code].cn || "系统错误"
                    });
                });
        }
    }
};
</script>

<style scoped>
.dialog-footer {
    text-align: right;
    margin-right: -5px;
}
.isNone {
    display: none;
}
.isShow {
    display: block;
}
.demo-ruleForm>>>.el-radio__input.is-checked .el-radio__inner {
    border-color: rgb(75, 143, 229);
    background: rgb(75, 143, 229);
}
.demo-ruleForm>>>.el-radio__input.is-checked + .el-radio__label {
    color: rgb(41, 86, 163);
}
.demo-ruleForm>>> .el-form-item__label{
    /* padding-right: 4px; */
}
</style>
