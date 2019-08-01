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
        <el-dialog title="新建文件夹" :visible.sync="dialogVisible" :before-close="modelClose" class="dialog-wrapper" width="433px" :center="true">
            <div>
                <el-form :model="folderFrom" :rules="rules" ref="folderFrom" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="文件夹名称" prop="folderName" style="width:330px">
                        <el-input v-model="folderFrom.folderName"></el-input>
                    </el-form-item>
                </el-form>
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button @click="modelClose">取 消</el-button>
                <el-button type="primary" @click="submit('folderFrom')">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script>
export default {
    name: "addFolder",
    props: ['foldershow'],
    data: function () {
        return {
            folderFrom: {
                folderName: ""
            },
            rules: {
                folderName: [
                    {
                        required: true,
                        message: "请输入文件夹名称",
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 12,
                        message: "长度在 1 到 12 个字符",
                        trigger: "blur"
                    },
                    {
                        pattern: /^[A-Za-z0-9_]+$/,
                        message: "文件夹名不符合规则",
                        trigger: "blur"
                    }
                ]
            },
            dialogVisible: this.foldershow,
            folderList: []
        }
    },
    mounted: function () {
        if (localStorage.getItem("folderList")) {
            this.folderList = JSON.parse(localStorage.getItem("folderList"))
        }
    },
    methods: {
        submit: function (formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    let num = 0
                    for (let i = 0; i < this.folderList.length; i++){
                        if(this.folderList[i].folderName == this.folderFrom.folderName && this.folderList[i].groupId == localStorage.getItem("groupId")){
                            num ++
                            this.$message({
                                message: '新建文件夹与已存在的文件夹名称相同',
                                type: "error"
                            });
                        };
                    }
                    if (num == 0) {
                        let data = {
                            folderName: this.folderFrom.folderName,
                            folderId: (new Date()).getTime(),
                            folderActive: false,
                            groupId: localStorage.getItem("groupId")
                        }
                        this.folderList.push(data)
                        localStorage.setItem('folderList', JSON.stringify(this.folderList))
                        this.$emit("success")
                    }
                } else {
                    return false;
                }
            })
        },
        modelClose: function () {
            this.$emit("close")
        }
    }
}
</script>

