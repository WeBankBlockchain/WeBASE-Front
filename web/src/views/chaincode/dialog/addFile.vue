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
        <el-dialog :title="$t('title.newFile')" :visible.sync="dialogVisible" :before-close="modelClose" class="dialog-wrapper" width="433px" :center="true">
            <div style="margin-bottom: 20px">
                <el-form :model="fileFrom" :rules="rules" ref="fileFrom" label-width="116px" class="demo-ruleForm">
                    <el-form-item :label="$t('text.contractName')" prop="contractName">
                        <el-input v-model="fileFrom.contractName" style="width: 210px;"></el-input>
                    </el-form-item>
                    <el-form-item :label="$t('text.filePath')">
                        <el-select v-model="fileFrom.contractType" :disabled='disabled' :placeholder="$t('placeholder.selected')">
                            <el-option v-for="item in options" :key="item.folderName" :label="item.folderName" :value="item.folderName">
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-form>
            </div>
            <div  class="text-right send-btn">
                <el-button @click="modelClose">{{$t('dialog.cancel')}}</el-button>
                <el-button type="primary" @click="submit('fileFrom')">{{$t('dialog.confirm')}}</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script>
export default {
    name: "addFile",
    props: ['fileshow', 'data', 'id'],
    computed: {
        rules() {
            var obj = {
                contractName: [
                    {
                        required: true,
                        message: `${this.$t('text.pleaseEnter')} ${this.$t('text.contractName')}`,
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 32,
                        message: this.$t('dialog.rivateKeyVerifyLength1_32'),
                        trigger: "blur"
                    },
                    {
                        pattern: /^[A-Za-z0-9_]+$/,
                        message: this.$t('dialog.contractNameIllegal'),
                        trigger: "blur"
                    }
                ]
            }
            return obj
        }
    },
    data: function () {
        return {
            dialogVisible: this.fileshow,
            fileFrom: {
                contractName: "",
                contractType: ""
            },
            disabled: false,
            folderId: this.id,
            options: [],
        }
    },
    mounted: function () {
        this.changeOptions();
    },
    methods: {
        changeOptions: function () {
            this.disabled = false
            this.options = [{
                folderName: "/",
                folderId: 1,
            }];
            if (localStorage.getItem("folderList")) {
                let arry = JSON.parse(localStorage.getItem("folderList"));
                for (let i = 0; i < arry.length; i++) {
                    if (arry[i].groupId == localStorage.getItem("groupId")) {
                        this.options.push(arry[i])
                    }
                }
            }
            this.fileFrom.contractType = this.options[0].folderName
             
            if (this.folderId) {
                this.options.forEach(value => {
                    if (value.folderId == this.folderId) {
                        this.fileFrom.contractType = value.folderName;
                        this.disabled = true
                    }
                })
            }
        },
        submit: function (formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    let data = {
                        contractName: this.fileFrom.contractName,
                        contractSource: "",
                        contractPath: this.fileFrom.contractType,
                        contractType: 'file',
                        contractActive: false,
                        contractstatus: 0,
                        contractAbi: "",
                        contractBin: "",
                        contractAddress: "",
                        contractVersion: "",
                        contractNo: (new Date()).getTime()
                    }
                    this.$emit("success", data)
                } else {
                    return false
                }
            })
        },
        modelClose: function () {
            this.$emit("close")
        }
    }
}
</script>
<style scoped>
.demo-ruleForm>>>.el-form-item{
    margin-bottom: 10px !important;
}
</style>

