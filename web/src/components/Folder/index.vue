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
        <el-dialog :title="$t('title.directory')" :visible.sync="dialogVisible" :before-close="close" class="dialog-wrapper" width="433px" :center="true">
            <div>
                <el-form :model="folderFrom" :rules="rules" ref="folderFrom" label-width="100px" class="demo-ruleForm">
                    <el-form-item :label="$t('dialog.folderName')" prop="folderName" style="width:330px">
                        <el-input v-model="folderFrom.folderName"></el-input>
                    </el-form-item>
                </el-form>
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button @click="close">{{$t('dialog.cancel')}}</el-button>
                <el-button type="primary" @click="submit('folderFrom')">{{$t('dialog.confirm')}}</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script>
import { getContractPathList } from "@/util/api"
export default {
    name: "Folder",
    props: ['folderVisible', 'folderItem'],
    computed: {
        rules() {
            var obj = {
                folderName: [
                    {
                        required: true,
                        message: `${this.$t('dialog.pleaseType')} ${this.$t('dialog.folderName')}`,
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
                        message: this.$t('dialog.folderNameIllegal'),
                        trigger: "blur"
                    }
                ]
            }
            return obj
        }
    },
    data() {
        return {
            options: [],
            folderFrom: {
                folderName: ""
            },
            dialogVisible: this.folderVisible,
            pathList: []
        }
    },
    mounted() {
        this.getContractPaths();
        if (this.folderItem) {
            this.folderFrom.folderName = this.folderItem.storeName_en
        }
    },
    methods: {
        getContractPaths() {
            getContractPathList(localStorage.getItem("groupId")).then(res => {
                if (res.status == 200) {
                    var list = res.data;
                    this.pathList = [];
                    this.pathList = list.map(item => {
                        return item.contractPath
                    })
                } else {
                    this.$message({
                        type: "error",
                        message: this.$chooseLang(res.data.code)
                    });
                }
            })
        },

        submit(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    if (valid && !this.pathList.includes(this.folderFrom.folderName)) {
                        this.$emit("success", this.folderFrom.folderName)
                    } else {
                        this.$message({
                            type: 'error',
                            message: this.$t('text.folderIsExists')
                        })
                        return false
                    }
                }

            })
        },
        close() {
            this.$emit("close")
        }
    }
}
</script>

<style scoped>
.demo-ruleForm >>> .el-form-item__label {
    padding: 0 8px 0 0;
}
</style>    