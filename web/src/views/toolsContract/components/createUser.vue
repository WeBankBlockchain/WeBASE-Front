<template> 
   <!-- <el-dialog :title="$t('dialog.addUsername')" :visible.sync="dialogVisible" :before-close="closeUserName" class="dialog-wrapper" width="400px" :center="true"> -->
            <div>
                  <el-form :model="userForm" :rules="rules" ref="userForm" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="" prop="userName">
                        <el-input v-model="userForm.userName" :placeholder="$t('dialog.pleaseEnterUserName')"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="text-right">
                    <el-button @click="closeUserName">{{$t('table.cancel')}}</el-button>
                    <el-button type="primary" @click="sureUserName('userForm')">{{$t('table.confirm')}}</el-button>
                </div>
            </div>
    <!-- </el-dialog> -->
</template>
<script>
import { queryCreatePrivateKey,queryLocalKeyStores} from "@/util/api"; 
export default {
    name: "createUser",
    // props: ['createuserShow'],
    computed: {
       
        rules() {
            var obj = {
                userName: [
                    {
                        required: true,
                        message: this.$t('dialog.pleaseEnterUserName'),
                        trigger: "blur"
                    },
                    {
                        pattern: /^[A-za-z0-9]+$/,
                        message: this.$t('dialog.privateKeyVerifyFont'),
                        trigger: "blur",

                    },
                    {
                        trigger: "blur",
                        min: 3,
                        max: 32,
                        message: this.$t('dialog.privateKeyVerifyLength'),
                    }
                ]
            }
            return obj
        }
    },
    
    data: function () {
        return {
             userForm: {
                userName: "",
            },
            loading: false,
            creatUserNameVisible: false,
            // dialogVisible: this.createuserShow,
            privateKeyList: localStorage.getItem("privateKeyList") ? JSON.parse(localStorage.getItem("privateKeyList")) : [],
        };
    },
     
    methods: {
        initUserName() {
            this.userForm = { userName: "" }
        },
        closeCallback() {
            this.initUserName()
        },
        closeUserName(data) {
            this.$emit('close',data);
        }, 
        sureUserName(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    let userArr = this.privateKeyList.map(item => {
                        return item.userName
                    })
                    if (userArr.includes(this.userForm.userName)) {
                        this.$message({
                            type: "error",
                            message: this.$t('text.nameNoSame')
                        });
                    } else {
                        this.creatUserNameVisible = false;
                        this.addUser()
                    }
                } else {
                    return false;
                }
            });
        },
        addUser: function () {
            queryCreatePrivateKey({ useAes: false, type: 0, userName: this.userForm.userName })
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.getLocalKeyStores()
                        this.$message({
                            type: "success",
                            message: this.$t('text.addUserSuccessed')
                        });  
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        getLocalKeyStores() {
            queryLocalKeyStores()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.closeUserName(data);
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                })
        },
    }
};
</script>

<style scoped>
.demo-ruleForm >>> .el-form-item__label {
    padding: 0 8px 0 0;
}
</style>
