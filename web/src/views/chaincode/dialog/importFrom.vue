<template>
    <div>
        <p>{{$t('contracts.importContractDec')}}</p>
        <p>{{$t('contracts.importContractEg')}}</p>
        <p>
            https://raw.githubusercontent.com/OpenZeppelin/openzeppelin-solidity/67bca857eedf99bf44a4b6a0fc5b5ed553135316/contracts/access/Roles.sol
        </p>
        <div>
            <el-form :model="folderFrom" :rules="rules" ref="folderFrom" label-width="100px" class="demo-ruleForm">
                <el-form-item :label="$t('dialog.outUrl')" prop="outUrl" style="width:410px">
                    <el-input v-model="folderFrom.outUrl" type="textarea"></el-input>
                </el-form-item>
                <el-form-item :label="$t('dialog.folderName')" prop="folderName">
                    <el-select v-model="folderFrom.folderName" :placeholder="$t('placeholder.selected')" style="width:310px">
                        <el-option v-for="item in options" :key="item.contractPath" :label="item.contractPath" :value="item.contractPath">
                        </el-option>
                    </el-select>
                </el-form-item>
            </el-form>
        </div>
        <div slot="footer" class="dialog-footer">
            <el-button @click="modelClose">{{$t('dialog.cancel')}}</el-button>
            <el-button type="primary" @click="submit('folderFrom')" :loading="loading">{{$t('dialog.confirm')}}</el-button>
        </div>
    </div>
</template>

<script>
import { exportOutContract, getContractPathList, saveChaincode } from "@/util/api";
import { getUrlLastValue, timestampUrl } from "@/util/util.js";
let Base64 = require("js-base64").Base64;
export default {
    name: 'ImportFrom',

    components: {
    },

    props: {
    },

    data() {
        return {
            options: [],
            contractContent: '',
            fileVisible: false,
            folderFrom: {
                outUrl: '',
                folderName: '/'
            },
            contractName: '',
            loading: false
        }
    },

    computed: {
        rules() {
            var obj = {
                folderName: [
                    {
                        required: true,
                        message: `${this.$t('dialog.pleaseType')} ${this.$t('dialog.folderName')}`,
                        trigger: "blur"
                    }
                ],
                outUrl: [
                    {
                        required: true,
                        message: `${this.$t('dialog.pleaseType')}${this.$t('dialog.outUrl')}`,
                        trigger: "blur"
                    },
                    {
                        pattern: /(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?/,
                        message: this.$t("rule.urlRule"),
                        trigger: "blur",
                    },
                ]
            }
            return obj
        }
    },

    watch: {
    },

    created() {
    },

    mounted() {
        this.getContractPaths()
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
        modelClose() {
            this.$emit('modelClose')
        },
        submit(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.contractName = getUrlLastValue(this.folderFrom.outUrl)
                    this.fetchGitContent()
                } else {
                    return false
                }
            })

            //         window.remixFileSystem.readFile(rawUrl, 'utf8', (err, content) => {
            // console.log('err:',err,'content:',content);
            // })

        },
        fetchGitContent() {
            let rawUrl = `${this.outUrl}?raw=true`
            let newUrl = timestampUrl(this.folderFrom.outUrl)
            this.loading = true
            exportOutContract(newUrl)
                .then(res => {
                    const { status, data } = res;
                    this.loading = false
                    console.log(status, data);
                    if (status === 200) {
                        this.contractContent = data
                        this.querySaveContract()
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$t('text.overtime')
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err
                    });
                })
        },
        querySaveContract() {
            let reqData = {
                groupId: localStorage.getItem("groupId"),
                contractName: this.contractName,
                contractPath: this.folderFrom.folderName,
                contractSource: Base64.encode(this.contractContent)
            }
            saveChaincode(reqData)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.$message({
                            type: "success",
                            message: this.$t('text.exportSuccessed')
                        })
                        this.$emit('exportSuccessed')
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                
        }
    }
}
</script>

<style scoped>
.dialog-footer {
    text-align: right;
}
</style>
