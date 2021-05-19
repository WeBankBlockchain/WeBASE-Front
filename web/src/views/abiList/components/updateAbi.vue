<template>
    <div>
        <el-form :model="abiForm" :rules="rules" ref="abiForm" label-width="90px">
            <el-form-item :label="$t('contracts.contractAbi')" prop="contractAbi">
                <el-input v-model="abiForm.contractAbi" type="textarea" :autosize="{ minRows: 4}"></el-input>
            </el-form-item>
        </el-form>
        <div class="text-right send-btn">
            <el-button @click="close">{{this.$t("dialog.cancel")}}</el-button>
            <el-button type="primary" @click="submit('abiForm')" :loading="loading">{{this.$t("dialog.confirm")}}</el-button>
        </div>
    </div>

</template>

<script>
import { updateImportAbi } from "@/util/api";
import { isJson } from "@/util/util";
export default {
    name: 'importAbi',

    components: {
    },

    props: {
        updateItem: {
            type: Object
        }
    },

    data() {

        return {
            abiContent: '',
            abiForm: {
                contractAbi: this.updateItem.contractAbi,
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
                    this.queryUpdateAbi()
                } else {
                    return false;
                }
            });

        },
        queryUpdateAbi() {
            this.loading = true;
            let data = {
                contractName: this.updateItem.contractName,
                contractAddress: this.updateItem.contractAddress,
                contractAbi: JSON.parse(this.abiForm.contractAbi),
                groupId: localStorage.getItem('groupId'),
                abiId: this.updateItem.abiId
            }
            updateImportAbi(data)
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        this.$emit('updateSuccess')
                        this.$message({
                            type: "success",
                            message: this.$t('text.updateSuccess')
                        })
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
        close() {
            this.$emit("closeUpdate")
        }
    }
}
</script>

<style scoped>
</style>
