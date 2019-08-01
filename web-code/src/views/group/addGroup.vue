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
        <el-dialog title="添加组织" :visible.sync="dialogVisible" width="433px" top="30vh" :append-to-body="true" :center="true" :before-close="modelClose" class="dialog-wrapper">
            <div>
                <el-form :model="groupForm" :rules="rules" ref="groupForm" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="组织名称" prop="name" style="width: 300px;">
                        <el-input v-model="groupForm.name" placeholder="请输入组织名称" maxlength="12"></el-input>
                    </el-form-item>
                    <el-form-item label="是否当前组织" prop="isCurrent" style="width: 300px;">
                        <el-radio-group v-model="groupForm.isCurrent" size="medium">
                            <el-radio :label="2">否</el-radio>
                            <el-radio :label="1">是</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="备注" style="width: 300px">
                        <el-input type="textarea" v-model="groupForm.explain" placeholder="请输入备注"></el-input>
                    </el-form-item>
                </el-form>
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button @click="modelClose">取 消</el-button>
                <el-button type="primary" @click="submit('groupForm')">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { addgroup } from "@/util/api";
import errcode from '@/util/errcode'
export default {
    name: "addGroup",
    props: ["dialogGroup"],
    data: function() {
        return {
            dialogVisible: this.dialogGroup,
            groupForm: {
                name: "",
                isCurrent: 2,
                explain: ""
            },
            rules: {
                name: [
                    {
                        required: true,
                        message: "请输入组织名称",
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
                    this.add();
                } else {
                    return false;
                }
            });
        },
        add: function() {
            let networkId = localStorage.getItem("networkId");
            let reqData = {
                networkId: networkId,
                orgName: this.groupForm.name,
                orgType: this.groupForm.isCurrent,
                description: this.groupForm.explain || ""
            };
            addgroup(reqData)
                .then(res => {
                    if (res.data.code === 0) {
                        this.$emit("success");
                        this.$message({
                            type: "success",
                            message: "新建组织成功"
                        });
                        this.groupForm = {
                            name: "",
                            isCurrent: 2,
                            explain: ""
                        };
                        this.modelClose();
                    } else {
                        this.$message({
                            type: "error",
                            message: errcode.errCode[res.data.code].cn
                        });
                        this.$emit("close", false);
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: "添加组织失败！"
                    });
                    this.modelClose();
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
