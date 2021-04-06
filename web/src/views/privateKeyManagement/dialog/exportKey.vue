<template>
    <div class="key-dialog">
        <el-form :model="keyForm" :rules="rules" ref="keyForm" label-width="142px" class="demo-ruleForm">
            <el-form-item :label="$t('table.exportType')" prop="fileType" style="width: 546px;">
                <el-radio-group v-model="keyForm.fileType" @change="changeFileType">
                    <el-radio :label="item.enName" :key="item.enName" v-for="item in fileTypeList">{{item.enName}}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('table.fileName')" prop="fileName" style="width: 546px;">
                <el-input v-model="keyForm.fileName" :placeholder="$t('privateKey.inputFileName')" maxlength="12" clearable></el-input>
            </el-form-item>
            <el-form-item :label="$t('privateKey.password')" prop="password" style="width: 546px;" v-if="keyForm.fileType==='.p12'">
                <el-input v-model="keyForm.password" type="password" :placeholder="$t('privateKey.placeholderPassword')"></el-input>
            </el-form-item>
            <!-- <el-form-item :label="$t('table.privateKey')" prop="privateKey" style="width: 546px;" v-if="keyForm.fileType=='string'">
                <el-input v-model="keyForm.privateKey" :placeholder="$t('privateKey.validatorPrivateKey')"></el-input>
            </el-form-item> -->

        </el-form>
        <div class="dialog-footer">
            <el-button class="footer-button" @click="modelClose">{{this.$t('dialog.cancel')}}</el-button>
            <el-button style="margin-left: 10px;" type="primary" @click="submitUploadList">{{this.$t('text.confirm')}}</el-button>
        </div>
    </div>
</template>

<script>
import { queryImportPrivateKey, exportPemPrivateKey, exportP12PrivateKey } from "@/util/api";
let Base64 = require("js-base64").Base64;
const FileSaver = require("file-saver");
const Web3Utils = require('web3-utils');
export default {
    name: 'importKey',

    components: {
    },

    props: ['exportInfo'],

    data() {

        return {
            loading: false,
            disabled: false,
            keyForm: {
                fileName: this.exportInfo.userName,
                fileType: ".txt",
                password: "",
                privateKey: "",
            },
            fileTypeList: [
                {
                    enName: '.txt',
                },
                {
                    enName: '.pem',
                },
                {
                    enName: '.p12',
                },
                {
                    enName: this.$t('text.decimal'),
                },
            ],
        }
    },

    computed: {
        rules() {
            var checkData = (rule, value, callback) => {
                if (value) {
                    if (/[\u4E00-\u9FA5]/g.test(value)) {
                        callback(new Error(this.$t('privateKey.passwordError')));
                    } else {
                        callback();
                    }
                }
                callback();
            }
            let data = {
                fileName: [
                    {
                        required: true,
                        message: this.$t('privateKey.inputFileName'),
                        trigger: "blur"
                    },
                    {
                        pattern: /^[A-za-z0-9]+$/,
                        message: this.$t('dialog.privateKeyVerifyFont'),
                        trigger: "blur",

                    },
                    {
                        min: 1,
                        max: 12,
                        message: this.$t('rule.textLong1_12'),
                        trigger: "blur"
                    }
                ],
                fileType: [
                    {
                        required: true,
                        message: this.$t('privateKey.inputFileType'),
                        trigger: "blur"
                    }
                ],
                password: [
                    // { required: true, message: this.$t('privateKey.placeholderPassword'), trigger: 'blur' }
                ],
                privateKey: [
                    {
                        required: true,
                        message: this.$t('privateKey.validatorPrivateKey'),
                        trigger: "blur"
                    },
                    {
                        pattern: /([a-fA-F0-9]{1,100})$/,
                        message: this.$t('privateKey.validatorPrivateKey1'),
                        trigger: "blur"
                    }
                ]
            };
            return data
        }
    },

    watch: {
    },

    created() {
    },

    mounted() {
    },

    methods: {
        modelClose() {
            this.keyForm = Object.assign({
                fileName: "",
                fileType: ""
            });
            this.loading = false;
            this.$store.state.exportRivateKey = false;
        },
        changeFileType() {
            if (this.$refs.upload) this.$refs.upload.clearFiles();
            this.$refs['keyForm'].clearValidate();
        },
        submitUploadList() {
            this.$refs['keyForm'].validate(valid => {
                if (valid) {
                    console.log(this.keyForm.fileType);
                    switch (this.keyForm.fileType) {
                        case '十进制私钥':
                            this.textRivateKey()
                            break;
                        case 'Decimal Key':
                            this.textRivateKey()
                            break;
                        case '.txt':
                            this.textRivateKey()
                            break;

                        case '.pem':
                            this.pemRivateKey()
                            break;
                        case '.p12':
                            this.p12RivateKey()
                            break;
                    }
                }
            })
        },
        textRivateKey() {
            if (this.keyForm.fileType == 'Decimal Key' || this.keyForm.fileType == '十进制私钥') {
                const decimalBN = Web3Utils.hexToNumberString(`0x${this.exportInfo.privateKey}`)
                var blob = new Blob([decimalBN], { type: "text;charset=utf-8" });
                FileSaver.saveAs(blob, `${this.keyForm.fileName}_decimal_key_${this.exportInfo.address}`);
                this.$notify({
                    title: this.$t('text.title'),
                    message: this.$t('text.exportWeid'),
                    duration: 3000
                });
            } else {
                let str = JSON.stringify(this.exportInfo);
                var blob = new Blob([str], { type: "text;charset=utf-8" });
                FileSaver.saveAs(blob, `${this.keyForm.fileName}_key_${this.exportInfo.address}`);
            }
            this.modelClose()
        },
        pemRivateKey() {
            var form = {
                userAddress: this.exportInfo.address
            }
            exportPemPrivateKey(form)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        const blob = new Blob([res.data])
                        const fileName = `${this.keyForm.fileName}_key_${this.exportInfo.address}.pem`
                        if ('download' in document.createElement('a')) {
                            const elink = document.createElement('a')
                            elink.download = fileName
                            elink.style.display = 'none'
                            elink.href = URL.createObjectURL(blob)
                            document.body.appendChild(elink)
                            elink.click()
                            URL.revokeObjectURL(elink.href)
                            document.body.removeChild(elink)
                        } else {
                            navigator.msSaveBlob(blob, fileName)
                        }
                        this.modelClose()
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        p12RivateKey() {
            var form = {
                userAddress: this.exportInfo.address,
                p12Password: Base64.encode(this.keyForm.password)
            }
            exportP12PrivateKey(form)
                .then(res => {
                    const { data, status } = res;
                    console.log(res);
                    if (status === 200) {
                        const blob = new Blob([res.data])
                        const fileName = `${this.keyForm.fileName}_key_${this.exportInfo.address}.p12`
                        if ('download' in document.createElement('a')) {
                            const elink = document.createElement('a')
                            elink.download = fileName
                            elink.style.display = 'none'
                            elink.href = URL.createObjectURL(blob)
                            document.body.appendChild(elink)
                            elink.click()
                            URL.revokeObjectURL(elink.href)
                            document.body.removeChild(elink)
                        } else {
                            navigator.msSaveBlob(blob, fileName)
                        }
                        this.modelClose()

                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
    }
}
</script>

<style scoped>
.footer-button {
    margin-right: 10px;
}
.dialog-footer {
    display: flex;
    flex-direction: row;
    justify-content: flex-end;
    padding-bottom: 20px;
}
</style>
