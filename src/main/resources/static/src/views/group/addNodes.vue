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
        <el-dialog title="添加节点" :visible.sync="dialogVisible" width="433px" top="30vh" :append-to-body="true" :center="true" :before-close="modelClose" class="dialog-wrapper">
            <div>
                <el-form :model="nodeForm" :rules="rules" ref="nodeForm" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="节点名称" prop="name" style="width: 300px">
                        <el-input v-model="nodeForm.name" maxlength="12"></el-input>
                    </el-form-item>
                    <el-form-item label="节点IP" prop="ip" style="width: 300px">
                        <el-input v-model="nodeForm.ip"></el-input>
                    </el-form-item>
                    <el-form-item label="p2p端口" prop="p2pPort" style="width: 300px">
                        <el-input v-model="nodeForm.p2pPort"></el-input>
                    </el-form-item>
                    <el-form-item label="rpc端口" prop="rpcPort" style="width: 300px">
                        <el-input v-model="nodeForm.rpcPort"></el-input>
                    </el-form-item>
                </el-form>
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button @click="modelClose">取 消</el-button>
                <el-button type="primary" @click="submit('nodeForm')">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { addnodes } from "@/util/api";
import errcode from "@/util/errcode";
export default {
    name: "nodeModel",
    props: ["dialogShow", "id"],
    data: function() {
        return {
            dialogVisible: this.dialogShow,
            orgId: this.id,
            nodeForm: {
                name: "",
                ip: "",
                p2pPort: null,
                rpcPort: null,
                frontPort: null
            },
            rules: {
                name: [
                    {
                        required: true,
                        message: "请输入节点名称",
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 12,
                        message: "长度在 1 到 12 个字符",
                        trigger: "blur"
                    }
                ],
                ip: [
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
                p2pPort: [
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
                ],
                rpcPort: [
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
    methods: {
        modelClose: function() {
            this.$emit("close", false);
        },
        submit: function(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.$confirm("确认提交？", {
                        center: true
                    })
                        .then(_ => {
                            this.add();
                        })
                        .catch(_ => {
                            this.modelClose();
                        });
                } else {
                    return false;
                }
            });
        },
        add: function() {
            let networkId = localStorage.getItem("networkId");
            let reqData = {
                networkId: networkId,
                orgId: this.orgId,
                nodeName: this.nodeForm.name,
                nodeIp: this.nodeForm.ip,
                p2pPort: this.nodeForm.p2pPort,
                rpcPort: this.nodeForm.rpcPort
            };
            addnodes(reqData)
                .then(res => {
                    if (res.data.code === 0) {
                        this.$emit("success");
                        this.$message({
                            type: "success",
                            message: "新建节点成功"
                        });
                        this.modelClose();
                    } else {
                        this.$emit("close", false);
                        this.$message({
                            type: "error",
                            message: errcode.errCode[res.data.code].cn || "新增失败"
                        });
                    }
                })
                .catch(err => {
                    this.modelClose();
                    this.$message({
                        type: "error",
                        message: "新建节点失败"
                    });
                });
        }
    }
};
</script>
<style scoped>
.dialog-wrapper>>>.el-dialog__title {
    font-size: 18px;
    color: #36393d;
    font-weight: bold;
    letter-spacing: 0.5px;
}
.dialog-wrapper>>>.el-dialog__body {
    padding-top: 10px;
    padding-bottom: 0;
}
.dialog-wrapper>>>.el-form-item.is-required:not(.is-no-asterisk)
    > .el-form-item__label::before {
    color: #2956a3;
    vertical-align: middle;
}
.dialog-wrapper>>>.el-form-item__label {
    font-size: 12px;
    color: #737a86;
}
.dialog-wrapper>>>.el-form-item {
    margin-bottom: 20px;
}
.dialog-wrapper>>>.el-dialog__footer {
    padding-top: 0;
}
.dialog-wrapper>>>.el-button {
    padding: 10px 20px;
}
.dialog-wrapper>>>.el-input__inner {
    height: 36px;
    line-height: 36px;
}
.dialog-footer {
    text-align: right;
}
</style>
