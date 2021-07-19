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
            <div>
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
            <div slot="footer" class="dialog-footer">
                <el-button @click="modelClose">{{$t('dialog.cancel')}}</el-button>
                <el-button type="primary" @click="submit('fileFrom')">{{$t('dialog.confirm')}}</el-button>
            </div>
        </el-dialog>
         <add-folder v-if="foldershow" :foldershow="foldershow"  :isAddFile="isAddFile" @close='folderClose'></add-folder>
    </div>
</template>
<script>
import { getContractPathList } from "@/util/api"
import { filter } from 'jszip'
import addFolder from "../dialog/addFolder"
export default {
    name: "addFile",
    props: ['fileshow', 'data', 'id'],
    components: {
        "add-folder": addFolder
    },
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
                        message: this.$t('dialog.privateKeyVerifyLength1_32'),
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
            folderList: [],
            pathList: [],
            foldershow: false, 
            isAddFile: ""
        }
    },
    mounted: function () {
        this.getContractPaths();
    },
    methods: {
        getContractPaths() {
            getContractPathList(localStorage.getItem("groupId")).then(res => {
                if (res.status == 200) {
                    this.pathList = res.data;
                    let num = 0;
                    this.folderList = []
                    for (let i = 0; i < this.pathList.length; i++) {
                        let item = {
                            folderName: this.pathList[i].contractPath,
                            folderId: new Date().getTime() + this.pathList[i].contractPath,
                            folderActive: false,
                            groupId: localStorage.getItem("groupId"),
                            modifyTime: this.pathList[i].modifyTime
                        };
                        this.folderList.push(item)
                        if (this.pathList[i].contractPath == this.userFolader) {
                            num++
                        }
                    }

                    this.changeOptions();
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
                });
        },
        changeOptions: function () {
            this.disabled = false
            // this.options = [{
            //     folderName: "/",
            //     folderId: 1,
            // }];
            //     let arry = thfolderList;
            //     for (let i = 0; i < arry.length; i++) {
            //         if (arry[i].groupId == localStorage.getItem("groupId")) {
            //             this.options.push(arry[i])
            //         }
            //     }
            // this.fileFrom.contractType = this.options[0].folderName

            // if (this.folderId) {
            //     this.options.forEach(value => {
            //         if (value.folderId == this.folderId) {
            //             this.fileFrom.contractType = value.folderName;
            //             this.disabled = true
            //         }
            //     })
            // } 
            if (this.folderList.length) {
                let num = 0;
                for (let i = 0; i < this.folderList.length; i++) {
                    if (this.folderList[i].folderName === "/") {
                        num++
                    }
                }
                if (num == 0) {
                    let data = {
                        folderName: "/",
                        folderId: 1,
                    }
                    this.folderList.unshift(data)
                }
                this.options = this.folderList
            } else {
                this.options = [{
                    folderName: "/",
                    folderId: 1,
                }];
            }
            this.fileFrom.contractType = this.options[0].folderName
            for (let i = 0; i < this.options.length; i++) {
                if (this.data && this.options[i].folderName == this.data.contractName) {
                    this.fileFrom.contractType = this.options[i].folderName
                }
            }
            this.options = this.options.filter(item => {
                return item.folderName != "template"
            })
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
        },
          /**
         * 关闭文件夹弹窗
         */
        folderClose: function () {
            this.getContractPaths();
            this.foldershow = false
        },
        /**
         * 新增文件夹 打开文件夹弹窗
         */
        addFolder: function () {
            // this.checkNull();
            this.foldershow = true
        },
    }
}
</script>

