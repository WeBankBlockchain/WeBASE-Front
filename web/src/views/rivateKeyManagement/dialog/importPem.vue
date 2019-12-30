<template>
    <div>
        <el-dialog :title="$t('table.importPem')" :visible.sync="dialogVisible" :before-close="modelClose" class="dialog-wrapper" width="433px" :center="true">
            <el-form :model="importPemFrom" :rules="rules" ref="importPemFrom" label-width="125px" class="demo-ruleForm">
                <el-form-item :label="$t('text.fileName')" prop="fileName">
                    <el-input v-model="importPemFrom.fileName" style="width: 210px;"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="modelClose">{{$t('dialog.cancel')}}</el-button>
                <span class="fileinput-button1">
                    <span>{{$t('text.importFile')}}</span>
                    <!-- <el-button type="primary"></el-button> -->
                    <input type="file" @change="importFile($event)" accept=".pem"/>
                </span>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import {ImportPemPrivateKey} from "@/util/api"
export default {
    name: "importPem",
    props: ['show'],
    data() {
        return {
            dialogVisible: this.show,
            importPemFrom: {
                fileName: "",

            },
            rules: {
                fileName: [
                    {
                        required: true,
                        message: `${this.$t('text.pleaseEnter')} ${this.$t('text.pemName')}`,
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 32,
                        message: this.$t('dialog.rivateKeyVerifyLength1_32'),
                        trigger: "blur"
                    },
                ]
            },
            fileString: ""
        }
    },
    methods: {
        importFile: function(e){
            this.$refs["importPemFrom"].validate(valid => {
                if(valid){
                    if (!e.target.files.length) {
                        return;
                    }
                    console.log(e)
                    this.fileString = "";
                    let files = e.target.files[0];
                    let filessize = Math.ceil(files.size / 1024);
                    let reader = new FileReader(); //新建一个FileReader
                    reader.readAsText(files, "UTF-8"); //读取文件
                    let _this = this;
                    reader.onload = function (evt) {
                        _this.fileString = evt.target.result; // 读取文件内容
                        try {
                            let reqQuery = {
                                pemContent: _this.fileString,
                                userName: _this.importPemFrom.fileName,
                            };
                            console.log(reqQuery)
                            ImportPemPrivateKey(reqQuery)
                                .then(res => {
                                    const { data, status } = res;
                                    if (status === 200) {
                                        e.target.value = '';
                                        _this.$message({
                                            type: 'success',
                                            message: _this.$t('text.importSuccessed')
                                        });
                                        _this.modelClose()
                                    } else {
                                        e.target.value = '';
                                        _this.$message({
                                            type: "error",
                                            message: _this.$chooseLang(res.data.code)
                                        });
                                    }
                                })
                                .catch(err => {
                                    e.target.value = '';
                                    _this.$message({
                                        type: "error",
                                        message: _this.$t('text.systemError')
                                    });
                                });

                        } catch (error) {
                            e.target.value = '';
                            _this.$message({
                                type: 'error',
                                message: _this.$t('text.importFailed')
                            })
                        }
                    }
                }else{
                    return false
                }
            })
            
        },
        modelClose: function(){
            this.$emit('close')
        }
    }
}
</script>

<style scoped>
@import "../rivateKeyManagement.css";
</style>