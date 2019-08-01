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
        <el-dialog title="新建合约" :visible.sync="dialogVisible" :before-close="modelClose" class="dialog-wrapper" width="433px" :append-to-body="true" :center="true">
            <el-form :model="chaincodeForm" :rules="rules" ref="chainForm" label-width="100px" class="demo-ruleForm">
                <el-form-item label="合约名称" prop="name" style="width:330px">
                    <el-input v-model="chaincodeForm.name" maxlength="32" placeholder="请输入合约名称"></el-input>
                    <el-tooltip effect="dark" placement="right">
                        <div slot="content">
                            <p>合约名称规则：<br>(1) 32位以内数字大小写字母以及“_”</p>
                        </div>
                        <i class="el-icon-info icon-info-base"></i>
                    </el-tooltip>
                </el-form-item>
                <el-form-item label="合约版本" prop="version" style="width:330px">
                    <el-input v-model="chaincodeForm.version" maxlength="12" placeholder="请输入版本号"></el-input>
                    <el-tooltip effect="dark" placement="right">
                        <div slot="content">
                            <p>合约版本规则：<br>(1) 12位以内数字小写字母以及“.”<br>(2) 数字或者小写字母开头，不能以“.”结尾<br>(3) 不能出现连续的“.”</p>
                        </div>
                        <i class="el-icon-info icon-info-base"></i>
                    </el-tooltip>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="modelClose">取 消</el-button>
                <el-button type="primary" @click="submit('chainForm')">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import router from "@/router";

export default {
    name: "addChainCode",
    props: ["addDialog"],
    data: function () {
        return {
            dialogVisible: this.addDialog,
            chaincodeForm: {
                name: "",
                version: ""
            },
            rules: {
                name: [
                    {
                        required: true,
                        message: "请输入合约名称",
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 32,
                        message: "长度在 1 到 32 个字符",
                        trigger: "blur"
                    },
                    {
                        pattern: /^[A-Za-z0-9_]+$/,
                        message: "合约名不符合规则",
                        trigger: "blur"
                    }
                ],
                version: [
                    {
                        required: true,
                        message: "请输入版本号",
                        trigger: "blur"
                    },
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
        submit: function (formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    let val = {
                        contractName: this.chaincodeForm.name,
                        contractSource: "",
                        contractVersion: this.chaincodeForm.version,
                        contractStatus: 0,
                        contractType: 0
                    };
                    this.$emit("change", val);
                    this.dialogVisible = false;
                    this.modelClose();
                } else {
                    return false;
                }
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
.icon-info-base {
    position: absolute;
    top: 14px;
    left: 242px;
}
</style>
