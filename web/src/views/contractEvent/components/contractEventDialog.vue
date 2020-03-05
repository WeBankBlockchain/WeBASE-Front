<template>
    <div>
        <el-form :model="contractEventForm" :rules="rules" ref="contractEventForm" label-width="125px" class="demo-ruleForm">
            <el-form-item :label="$t('table.appId')" prop="appId">
                <el-input v-model="contractEventForm.appId" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.appId')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.exchangeName')" prop="exchangeName">
                <el-input v-model="contractEventForm.exchangeName" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.exchangeName')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.queueName')" prop="queueName">
                <el-input v-model="contractEventForm.queueName" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.queueName')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.contractAbi')" prop="contractAbi">
                <el-input v-model="contractEventForm.contractAbi" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.contractAbi')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.contractAddress')" prop="contractAddress">
                <el-input v-model="contractEventForm.contractAddress" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.contractAddress')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.fromBlock')" prop="fromBlock">
                <el-input v-model="contractEventForm.fromBlock" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.fromBlock')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.toBlock')" prop="toBlock">
                <el-input v-model="contractEventForm.toBlock" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.toBlock')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.topicList')" prop="topicList">
                <el-input v-model="contractEventForm.topicList" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.topicList')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.groupId')" prop="groupId">
                <el-select v-model="contractEventForm.groupId" :placeholder="$t('placeholder.selected')">
                    <el-option v-for=" item in groupList" :label="item.groupName" :value="item.group"></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">

            <el-button @click="modelClose">{{$t('dialog.cancel')}}</el-button>
            <el-button type="primary" @click="submit('contractEventForm')">{{$t('dialog.confirm')}}</el-button>
        </div>
    </div>
</template>

<script>
import { addContractEvent } from "@/util/api";
export default {
    name: 'contractEventDialog',

    components: {
    },

    props: {
    },

    data() {
        return {
            contractEventForm: {
                appId: '',
                exchangeName: '',
                queueName: '',
                groupId: '',
                contractAbi: '',
                contractAddress: '',
                fromBlock: '',
                toBlock: '',
                topicList: '',
            },
            groupList: localStorage.getItem("cluster")
                ? JSON.parse(localStorage.getItem("cluster"))
                : [],
        }
    },

    computed: {
        rules() {
            var obj = {
                appId: [
                    {
                        required: true,
                        message: this.$t('dialog.appId'),
                        trigger: "blur"
                    },
                ],
                exchangeName: [
                    {
                        required: true,
                        message: this.$t('dialog.exchangeName'),
                        trigger: "blur"
                    },
                ],
                queueName: [
                    {
                        required: true,
                        message: this.$t('dialog.queueName'),
                        trigger: "blur"
                    },
                ],
                contractAbi: [
                    {
                        required: true,
                        message: this.$t('dialog.contractAbi'),
                        trigger: "blur"
                    },
                ],
                contractAddress: [
                    {
                        required: true,
                        message: this.$t('dialog.contractAddress'),
                        trigger: "blur"
                    },
                ],
                fromBlock: [
                    {
                        required: true,
                        message: this.$t('dialog.fromBlock'),
                        trigger: "blur"
                    },
                ],
                toBlock: [
                    {
                        required: true,
                        message: this.$t('dialog.toBlock'),
                        trigger: "blur"
                    },
                ],
                topicList: [
                    {
                        required: true,
                        message: this.$t('dialog.topicList'),
                        trigger: "blur"
                    },
                ],
                groupId: [
                    {
                        required: true,
                        message: this.$t('dialog.groupId'),
                        trigger: "change"
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
    },

    methods: {
        submit(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.queryAdd()

                } else {
                    return false;
                }
            })
        },
        queryAdd() {
            console.log(this.contractEventForm)
            let param = Object.assign({},this.contractEventForm, {
                contractAbi: new Array(this.contractEventForm.contractAbi),
                topicList: new Array(this.contractEventForm.topicList),
            })
            addContractEvent(param)
                .then(res => {
                    if (res.data.code === 0) {
                        this.modelClose()
                        this.$emit("success")
                    } else {
                        this.modelClose()
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: this.$t('text.systemError')
                    });
                });
        },
        modelClose() {
            this.$emit('close')
        }
    }
}
</script>

<style scoped>
.dialog-footer {
    text-align: right;
}
</style>
