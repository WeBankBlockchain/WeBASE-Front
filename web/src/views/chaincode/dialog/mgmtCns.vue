<template>
    <div>
        <el-form ref="cnsVersionFrom" :model="cnsVersionFrom" :rules="rules" class="" size="medium" label-width="135px">
            <el-form-item :label="$t('text.acountAddress')" prop="userId">
                <el-select v-model="cnsVersionFrom.userId" :placeholder="placeholderText" @change="changeId" style="width: 200px;">
                    <el-option :label="item.address" :value="item.address" :key="item.address" v-for='item in userList'>
                        <span class="font-12">{{item.userName}}</span>
                        <span>{{item.address}}</span>
                    </el-option>
                </el-select>
                <el-button v-if="isShowAddUserBtn" type="text" size="mini" @click="createUser()">{{$t('privateKey.addUser')}}</el-button>
            </el-form-item>
            <el-form-item :label="$t('text.cnsName')" prop="cnsName">
                <el-input v-model="cnsVersionFrom.cnsName" style="width: 200px;">
                </el-input>
            </el-form-item>
            <el-form-item :label="$t('text.version')" prop="cnsVersion">
                <el-input v-model="cnsVersionFrom.cnsVersion" style="width: 200px;">
                </el-input>
                <el-tooltip class="font-color-fff" effect="dark" :content="$t('text.registerCnsTips')" placement="top-start">
                    <i class="el-icon-info"></i>
                </el-tooltip>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer" style="text-align: right;">
            <el-button @click="modelClose">{{$t('dialog.cancel')}}</el-button>
            <el-button type="primary" @click="submit('cnsVersionFrom')">{{$t('text.register')}}</el-button>
        </div>
        <el-dialog :title="$t('dialog.addUsername')" :visible.sync="creatUserNameVisible" :before-close="closeUserName" class="dialog-wrapper" width="640px" :center="true" :append-to-body="true">
              <v-createUser  @close='createUserClose'></v-createUser>
         </el-dialog>
    </div>
</template>

<script>
import { queryLocalKeyStores, registerCns, findCnsInfo } from "@/util/api";
import createUser from "@/views/toolsContract/components/createUser";
export default {
    name: 'mgmtCns',
    components: {
        "v-createUser": createUser
    },
    props: ['mgmtCnsItem'],

    data() {
        return {
            userName: "",
            userList: [],
            userId: null,
            placeholderText: this.$t('placeholder.selectedAccountAddress'),
            loading: false,
            cnsList: [],
            cnsVersionFrom: {
                userId: "",
                cnsVersion: this.mgmtCnsItem.version,
                cnsName: this.mgmtCnsItem.contractName
            },
            reqVersion: "",
            creatUserNameVisible: false,
            isShowAddUserBtn: false
        }
    },

    computed: {
        cnsHead() {
            var arr = [
                {
                    enName: "contractName",
                    name: this.$t('table.contractName'),
                    tdWidth: '115px'
                },
                {
                    enName: "version",
                    name: this.$t('table.cnsVersion'),
                    tdWidth: "200px"
                },
                {
                    enName: "contractAddress",
                    name: this.$t('table.contractAddress'),
                    tdWidth: ''
                }
            ]
            return arr
        },
        rules() {
            var obj = {
                cnsVersion: [
                    {
                        required: true,
                        message: this.$t('dialog.cnsVersion'),
                        trigger: "change"
                    },
                    {
                        pattern: /^[A-Za-z0-9.]+$/,
                        message: this.$t('dialog.cnsVersionPattern'),
                        trigger: "blur"
                    }
                ],
                userId: [
                    {
                        required: true,
                        message: this.$t('placeholder.selectedAccountAddress'),
                        trigger: "change"
                    },
                ],
                cnsName: [
                    {
                        required: true,
                        message: this.$t('dialog.cnsName'),
                        trigger: "change"
                    },
                    {
                        pattern: /^[A-Za-z0-9.]+$/,
                        message: this.$t('dialog.cnsVersionPattern'),
                        trigger: "blur"
                    }
                ],
            }
            return obj
        },
    },

    watch: {
    },

    created() {
    },

    mounted() {
        this.getLocalKeyStores()
        this.queryFindCnsInfo()
    },

    methods: {
        getLocalKeyStores() {
            queryLocalKeyStores()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.userList = data
                        if (this.userList.length) {
                            this.cnsVersionFrom.userId = this.userList[0].address;
                        } else {
                             this.isShowAddUserBtn = true;
                            this.placeholderText = this.$t('placeholder.selectedNoUser')
                        }
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        changeId() {

        },
        submit() {
            this.$refs['cnsVersionFrom'].validate((valid) => {
                if (valid) {
                    if (this.reqVersion) {
                        if (this.reqVersion == this.cnsVersionFrom.cnsVersion) {
                            this.$message({
                                type: 'warning',
                                message: this.$t('text.registered')
                            })
                            return
                        } else {
                            this.$confirm(this.$t('text.updateRegister'), {
                                type: 'warning'
                            }).then(() => {
                                this.queryRegisterCns()
                            }).catch(() => {

                            });

                        }
                    } else {
                        this.queryRegisterCns()
                    }

                } else {
                    return false;
                }
            });

        },
        queryRegisterCns() {
            let param = {
                groupId: localStorage.getItem('groupId'),
                contractName: this.mgmtCnsItem.contractName,
                version: this.cnsVersionFrom.cnsVersion,
                abiInfo: JSON.parse(this.mgmtCnsItem.contractAbi),
                userAddress: this.cnsVersionFrom.userId,
                saveEnabled: true,
                contractAddress: this.mgmtCnsItem.contractAddress,
                cnsName: this.cnsVersionFrom.cnsName,
                contractPath: this.mgmtCnsItem.contractPath
            }
            registerCns(param)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.$message({
                            type: 'success',
                            message: this.$t('text.registerSuccess')
                        })
                        this.$emit('mgmtCnsResultSuccess')
                    } else {
                        this.$message({
                            message: this.$chooseLang(res.data.code),
                            type: "error"
                        });
                    }
                })
        },
        modelClose() {
            this.$emit('mgmtCnsResultClose')
        },
        queryFindCnsInfo() {
            let param = {
                groupId: localStorage.getItem('groupId'),
                contractAddress: this.mgmtCnsItem.contractAddress
            }
            findCnsInfo(param)
                .then(res => {
                    const { data, status } = res
                    if (status === 200) {
                        if (data.data) {
                            this.reqVersion = data.data.version
                            this.cnsVersionFrom.cnsVersion = data.data.version
                        }
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },

        createUser(){
            this.creatUserNameVisible = true;
        },
         createUserClose(data){
             this.userList = data; 
             if(this.userList.length > 0 ){
                this.isShowAddUserBtn = false;
                this.cnsVersionFrom.userId = this.userList[0].address;
             }
             this.creatUserNameVisible = false;
        },
    }
}
</script>

<style scoped>
 /* .el-form-item__label {
    display: block;
    line-height: 32px;
    float: none;
    text-align: left;
} */
</style>

