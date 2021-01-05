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
        <el-dialog :title="$t('title.selectDirectory')" :visible.sync="dialogVisible" :before-close="close" class="dialog-wrapper" width="433px" :center="true">
            <div>
                <el-form :model="folderFrom" :rules="rules" ref="folderFrom" label-width="100px" class="demo-ruleForm">
                    <el-form-item :label="$t('dialog.folderName')" prop="folderName" style="width:330px">
                        <el-select v-model="folderFrom.folderName" :placeholder="$t('placeholder.selected')">
                            <el-option v-for="item in options" :key="item.contractPath" :label="item.contractPath" :value="item.contractPath">
                            </el-option>
                        </el-select>
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
    name: "File",
    props: ['fileVisible'],
    computed: {
        rules() {
            var obj = {
                folderName: [
                    {
                        required: true,
                        message: `${this.$t('dialog.pleaseType')} ${this.$t('dialog.folderName')}`,
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
                folderName: "/"
            },
            dialogVisible: this.fileVisible,
            pathList: []
        }
    },
    mounted() {
        this.getContractPaths();
    },
    methods: {
        getContractPaths() {
            getContractPathList(localStorage.getItem("groupId")).then(res => {
                if (res.status == 200) {
                    var list = res.data;
                    this.options = list.filter(item => {
                        return item.contractPath != "template"
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
                    this.$emit("successFile", this.folderFrom.folderName)
                } else {
                    return false
                }
            })
        },

        close() {
            this.$emit("closeFile")
        }
    }
}
</script>

<style scoped>
.demo-ruleForm >>> .el-form-item__label {
    padding: 0 8px 0 0;
}
</style>    