<template>
    <div class="addAbi">
        <el-form :model="abiForm" :rules="rules" ref="abiForm" label-width="90px">
            <el-form-item :label="$t('contracts.contractName')" prop="contractName">
                <el-input v-model="abiForm.contractName"></el-input>
            </el-form-item>
            <el-form-item :label="$t('contracts.contractAddress')" prop="contractAddress">
                <el-input v-model="abiForm.contractAddress"></el-input>
            </el-form-item>
            <el-form-item :label="$t('contracts.contractAbi')" prop="contractAbi" >
                <el-input v-model="abiForm.contractAbi" type="textarea" :autosize="{ minRows: 4}" style=" overflow-y: scroll;max-height:300px;"></el-input>
            </el-form-item>
        </el-form>
        <div class="text-right send-btn">
            <el-button @click="close">{{this.$t("dialog.cancel")}}</el-button>
            <el-button type="primary" @click="submit('abiForm')" :loading="loading">{{this.$t("dialog.confirm")}}</el-button>
        </div>
    </div>

</template>

<script>
import { importAbi, addFunctionAbi } from "@/util/api";
import { isJson } from "@/util/util";
import web3 from "@/util/ethAbi"
export default {
    name: 'importAbi',

    components: {
    },

    props: {
    },

    data() {

        return {
            abiContent: '',
            abiForm: {
                contractAbi: null,
                contractAddress: '',
                contractName: ''
            },
            loading: false
        }
    },

    computed: {
        rules() {
            let _this = this;
            let validateJSON = (rule, value, callback) => {
                if (value === '') {
                    callback(new Error(_this.$t("rule.contractAbi")));
                } else {
                    if (!isJson(value)) {
                        callback(new Error('Invalid input: Unexpected end of JSON input'));
                    } else {
                        callback()
                    }

                }
            }
            let data = {
                contractAbi: [
                    {
                        required: true,
                        message: this.$t("rule.contractAbi"),
                        trigger: "blur"
                    },
                    {
                        validator: validateJSON, trigger: 'blur'
                    }
                ],
                contractAddress: [
                    {
                        required: true,
                        message: this.$t("rule.contractAddress"),
                        trigger: "blur"
                    }
                ],
                contractName: [
                    {
                        required: true,
                        message: this.$t("rule.contractName"),
                        trigger: "blur"
                    },
                    {
                        required: true,
                        min: 1,
                        max: 100,
                        message: this.$t("rule.textLong1_100"),
                        trigger: "blur"
                    }
                ]
            }
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
        submit(abiForm) {
            this.$refs[abiForm].validate(valid => {
                if (valid) {
                    this.queryImportAbi()
                } else {
                    return false;
                }
            });

        },
        queryImportAbi() {
            this.loading = true;
            let data = {
                contractName: this.abiForm.contractName,
                contractAddress: this.abiForm.contractAddress,
                contractAbi: JSON.parse(this.abiForm.contractAbi),
                groupId: localStorage.getItem('groupId')
            }
            importAbi(data)
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        this.$emit('importSuccess')
                        this.$message({
                            type: "success",
                            message: this.$t('text.importSuccessed')
                        })
                        this.setMethod()
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        })
                    }
                })
                .catch(error => {
                    this.loading = false;
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    })
                })
        },
        setMethod() {
            let Web3EthAbi = web3;
            let arry = [];
            if (this.abiForm.contractAbi) {
                let list = JSON.parse(this.abiForm.contractAbi);
                list.forEach(value => {
                    if (value.name && value.type == "function") {
                        let data = {};
                        let methodId;
                        if (localStorage.getItem("encryptionId") == 1) {
                            methodId = Web3EthAbi.smEncodeFunctionSignature({
                                name: value.name,
                                type: value.type,
                                inputs: value.inputs
                            });
                        } else {
                            methodId = Web3EthAbi.encodeFunctionSignature({
                                name: value.name,
                                type: value.type,
                                inputs: value.inputs
                            });
                        }
                        data.methodId = methodId.substr(0,10);
                        data.abiInfo = JSON.stringify(value);
                        data.methodType = value.type;
                        arry.push(data);
                    } else if (value.name && value.type == "event") {
                        let data = {};
                        let methodId;
                        if (localStorage.getItem("encryptionId") == 1) {
                            methodId = Web3EthAbi.smEncodeEventSignature({
                                name: value.name,
                                type: value.type,
                                inputs: value.inputs
                            });
                        } else {
                            methodId = Web3EthAbi.encodeEventSignature({
                                name: value.name,
                                type: value.type,
                                inputs: value.inputs
                            });
                        }
                        data.methodId = methodId.substr(0,10);
                        data.abiInfo = JSON.stringify(value);
                        data.methodType = value.type;
                        arry.push(data);
                    }
                });
                if (arry.length) {
                    this.addAbiMethod(arry)
                }
            }
        },
        addAbiMethod(list) {
            let data = {
                groupId: localStorage.getItem("groupId"),
                methodHandleList: list
            };
            addFunctionAbi(data)
                .then(res => {
                    if (res.data.code === 0) {
                        console.log("method 保存成功！");
                    } else {
                        this.$message({
                            message: this.$chooseLang(res.data.code),
                            type: "error"
                        });
                    }
                })
                .catch(err => {
                    message(constant.ERROR, "error");
                });
        },
        close() {
            this.$emit("closeImport")
        }
    }
}
</script>

<style scoped>
.addAbi .el-form-item {
    margin-bottom: 24px;
}
</style> 
