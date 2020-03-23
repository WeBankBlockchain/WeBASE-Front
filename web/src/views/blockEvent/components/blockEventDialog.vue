<template>
    <div>
        <el-form :model="blockEventForm" :rules="rules" ref="blockEventForm" label-width="125px" class="demo-ruleForm">
            <el-form-item :label="$t('table.appId')" prop="appId">
                <el-input v-model="blockEventForm.appId" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.appId')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.exchangeName')" prop="exchangeName">
                <el-input v-model="blockEventForm.exchangeName" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.exchangeName')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
            <el-form-item :label="$t('table.queueName')" prop="queueName">
                <el-input v-model="blockEventForm.queueName" style="width: 210px;"></el-input>
                <el-tooltip class="item" effect="dark" :content="$t('text.queueName')" placement="right">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">

            <el-button @click="modelClose">{{$t('dialog.cancel')}}</el-button>
            <el-button type="primary" @click="submit('blockEventForm')">{{$t('dialog.confirm')}}</el-button>
        </div>
    </div>
</template>

<script>
import { addBlockEvent } from "@/util/api";
export default {
    name: 'blockEventDialog',

    components: {
    },

    props: {
    },

    data() {
        return {
            blockEventForm: {
                appId: '',
                exchangeName: '',
                queueName: '',
                groupId: localStorage.getItem("groupId")
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

            addBlockEvent(this.blockEventForm)
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
