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
    <div class="key-dialog">
        <!-- <div class="divide-line"></div> -->
        <div v-if="creatSuccess">
            <table class="rivate-key-dialog">
                <tr v-for=" (val , key) in privateKeyMap">
                    <td v-if="key!='userName'">
                        <el-tooltip :content="val" placement="top" effect="light">
                            <span class="dialog-text">
                                {{transferCh(key)}}：{{val}}
                            </span>
                        </el-tooltip>
                        <i class="wbs-icon-copy font-12" style="margin-left:5px;" :title="`复制${transferCh(key)}`" @click="copyAddress(val)"></i>
                    </td>
                </tr>
            </table>
            <i class="el-icon-warning font-color-ed5454"> 请记得保存私钥！ </i>
        </div>
    </div>
</template>

<script>
import { queryCreatePrivateKey, queryImportPrivateKey } from "@/util/api";
import errcode from "@/util/errcode";
import { unique } from "@/util/util";
export default {
    name: "AddUser",
    props: {
        userForm: {
            type: Object
        }
    },
    data: function () {
        return {
            privateKeyMap: {},
            privateKeyList: localStorage.getItem('privateKeyList') ? JSON.parse(localStorage.getItem('privateKeyList')) : [],
            creatSuccess: false
        };
    },
    mounted() {
        this.addUser();
    },
    methods: {
        addUser: function () {
            queryCreatePrivateKey({}, { useAes: false, userName: this.userForm.userName })
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.creatSuccess = true;
                        this.privateKeyMap = data;
                        Object.assign(data, this.userForm);
                        this.privateKeyList.unshift(data)
                        this.privateKeyList = unique(this.privateKeyList, 'privateKey')
                        this.$emit('creatUserSuccess', this.privateKeyList)
                        localStorage.setItem("privateKeyList", JSON.stringify(this.privateKeyList));
                    } else {
                        this.creatSuccess = false;
                        this.$emit('creatUserFailed');
                        this.$message({
                            type: "error",
                            message: data.errorMessage || "系统错误"
                        });
                    }
                })
                .catch(err => {
                    this.$emit('creatUserFailed');
                    this.$message({
                        type: "error",
                        message: "系统错误"
                    });
                });
        },

        transferCh(key) {
            let str = "";
            switch (key) {
                case "publicKey":
                    str = "用户公钥";
                    break;
                case "privateKey":
                    str = "用户私钥";
                    break;
                case "address":
                    str = "用户地址";
                    break;
            }
            return str;
        },
        copyAddress(val) {
            if (!val) {
                this.$message({
                    type: "fail",
                    showClose: true,
                    message: "address为空，不复制。",
                    duration: 2000
                });
            } else {
                this.$copyText(val).then(e => {
                    this.$message({
                        type: "success",
                        showClose: true,
                        message: "复制成功",
                        duration: 2000
                    });
                });
            }
        },

    }
};
</script>
<style scoped>
.key-dialog {
    margin-top: 10px;
    padding-bottom: 10px;
}
.dialog-footer {
    text-align: right;
    margin-right: -5px;
    padding-bottom: 20px;
}
.radio-key {
    cursor: context-menu;
    font-size: 14px;
}
.base-span-key {
    margin-left: 8px;
    color: #00122c;
}
.pub-key {
    margin-left: 30px;
}
.riv-key {
    margin-left: 50px;
}

.divide-line {
    border: 1px solid #e7ebf0;
    margin-top: 15px;
    margin-bottom: 25px;
}
.dialog-text {
    max-width: 325px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    display: inline-block;
}
</style>
