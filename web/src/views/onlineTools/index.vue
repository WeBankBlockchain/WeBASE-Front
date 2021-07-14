<template>
    <div>
        <content-head :headTitle="$t('route.onlineTools')" @changeGroup="changeGroup"></content-head>
        <div class="module-wrapper" style="padding: 20px 20px 20px 20px;">
            <el-tabs v-model="activeName" @tab-click="handleClick">
                <el-tab-pane :label="$t('onlineTools.onlineHashCalculator')" name="first">
                    <div class="hash-wrapper">
                        <div class="calc-hash">
                            <el-tabs v-model="fileType" @tab-click="handleFileType">
                                <el-tab-pane :label="$t('onlineTools.text')" name="file-first">
                                    <div>
                                        <el-input type="textarea" v-model="inputText" @input="textFocus" style="margin-bottom: 20px;"></el-input>
                                    </div>
                                </el-tab-pane>
                                <el-tab-pane :label="$t('onlineTools.file')" name="file-second">
                                    <div>
                                        <div>
                                            <el-upload ref="upload" class="upload-file-hash" :file-list="fileList" :show-file-list="true" :limit="1" drag action :http-request="uploadCrt" :before-upload="onBeforeUpload" :on-success="uploadSuccess">
                                                <i class="el-icon-upload"></i>
                                                <div class="el-upload__text">{{$t('onlineTools.drag')}}<em>{{$t('onlineTools.upload')}}</em></div>
                                                <div slot="tip" class="el-upload__tip">
                                                    <i class="el-icon-info"></i>
                                                    {{$t('onlineTools.uploadNumLimit')}}
                                                </div>
                                            </el-upload>

                                        </div>
                                    </div>
                                </el-tab-pane>

                            </el-tabs>
                            <div style="margin-top: 10px;">
                                <span class="font-color-fff">{{$t('onlineTools.algorithm')}}</span>
                                <el-select v-model="algorithm" :placeholder="$t('placeholder.selected')" style="width:100px;margin-left:5px;">
                                    <el-option v-for="item in algorithmOpt" :key="item.value" :label="item.label" :value="item.value">
                                    </el-option>
                                </el-select>
                                <el-button type="primary" style="margin-left:5px;" @click="encryption">{{$t('onlineTools.encryption')}}</el-button>
                                <p class="font-color-fff text-title">{{$t('onlineTools.result')}}</p>
                                <div style="position: relative;">
                                    <i class="wbs-icon-copy font-12 copy-hash" @click="copyKey(inputHash)" :title="$t('title.copy')"></i>
                                    <el-input type="textarea" v-model="inputHash" readonly>

                                    </el-input>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-tab-pane>
                <el-tab-pane :label="$t('onlineTools.sign')" name="second">
                    <div class="hash-wrapper">
                        <p class="font-color-fff text-title">Hash</p>
                        <el-input type="textarea" v-model="inputSignHash" style="margin-bottom: 20px;"></el-input>
                        <div>
                            <span>{{$t('onlineTools.user')}}</span>
                            <el-select v-model="privateKey" :placeholder="placeholderText">
                                <el-option v-for="item in privateKeyList" :key="item.address" :label="item.userName" :value="item.address">
                                </el-option>
                            </el-select> 
                            <el-button v-if="isShowAddUserBtn" type="text" size="mini"  @click="createUser" v-loading="loading">{{$t('privateKey.addUser')}}</el-button>
                            <el-button  v-if="!isShowAddUserBtn" type="primary"   @click="querySignHash" v-loading="loading">{{$t('onlineTools.sign')}}</el-button>
                        </div>
                        <p class="font-color-fff text-title">{{$t('onlineTools.result')}}</p>
                        <div class="result" v-if="inputSign">
                            <ul>
                                <li v-for="(value, key) in signKey">
                                    <span>
                                        {{value}}:
                                    </span>
                                    <span>
                                        {{inputSign[value]}}
                                        <i class="wbs-icon-copy font-12 copy-public-key" @click="copyKey(inputSign[value])" :title="$t('title.copy')"></i>
                                    </span>
                                </li>
                            </ul>
                        </div>
                    </div>
                      <el-dialog :title="$t('dialog.addUsername')" :visible.sync="creatUserNameVisible" class="dialog-wrapper" width="640px" :center="true" :append-to-body="true">
                        <v-createUser  @close='createUserClose'></v-createUser>
                    </el-dialog>
                </el-tab-pane>
                <el-tab-pane :label="$t('route.parseAbi')" name="third">
                    <parse-abi></parse-abi>
                </el-tab-pane>
                <el-tab-pane :label="$t('route.eventCheck')" name="fourth">
                    <event-check ref="event" :groupId="groupId"></event-check>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import parseAbi from "../parseAbi/index.vue";
import eventCheck from "../eventCheck/index.vue";
import { queryLocalKeyStores, signHash } from "@/util/api";
const gm = require('@/util/SM2Sign');
import CryptoJS from 'crypto-js';
import Bus from "@/bus";
import createUser from "@/views/toolsContract/components/createUser";
export default {
    name: 'onlineTools',

    components: {
        contentHead,
        parseAbi,
        eventCheck,
        "v-createUser": createUser
    },

    props: {
    },

    data() {
        return {
            inputFile: null,
            inputText: "",
            inputHash: '',
            inputSignHash: '',
            inputSign: "",
            signKey: ['v', 'r', 's'],
            algorithm: "sha256",
            algorithmOpt: [
                {
                    label: "sha256",
                    value: "sha256"
                }
                // ,
                // {
                //     label: "sm3",
                //     value: "sm3"
                // }
            ],
            privateKeyList: [],
            privateKey: "",
            loading: false,
            activeName: 'first',
            fileList: [],
            fileType: "file-first",
            placeholderText: this.$t('placeholder.selectedAccountAddress'),
            encryptionId: localStorage.getItem('encryptionId'),
            groupId: localStorage.getItem("groupId"),
            creatUserNameVisible: false,
            isShowAddUserBtn: false
        }
    },

    computed: {
    },

    watch: {
        $route() {
            this.queryInit()
        }
    },

    created() {
    },
    beforeDestroy: function () {
        Bus.$off("changeGroup")
    },
    mounted() {
        Bus.$on("changeGroup", data => {
            this.changeGroup(data)
        })
        this.queryInit();
        this.getLocalKeyStores();
    },

    methods: {
        changeGroup(data){
            this.groupId = data;
            this.$refs.event.$emit('changeGroup', data)
        },
        queryInit() {
            if (this.$route.query.type) {
                this.activeName = "fourth";
            } else {
                this.activeName = "first";
            }
        },
        encryption() {
            if (this.algorithm === 'sha256') {
                let content;
                if (this.inputText) {
                    content = this.inputText;
                    let result = CryptoJS.SHA256(content).toString();
                    this.inputHash = `0x${result}`
                } else if (this.inputFile) {
                    content = this.inputFile;
                    let result = content.toString();
                    this.inputHash = `0x${result}`
                } else {
                    content = ""
                    let result = CryptoJS.SHA256(content).toString();
                    this.inputHash = `0x${result}`
                }

            } 
            // else if (this.algorithm === 'sm3') {
            //     let content;
            //     if (this.inputText) {
            //         content = this.inputText;
            //     } else {
            //         content = this.inputFile;
            //     }
            //     let result = gm.sm3Digest(content);
            //     this.inputHash = `0x${result}`;
            // }
        },
        getLocalKeyStores() {
            queryLocalKeyStores()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.privateKeyList = data;
                        if (this.privateKeyList.length) {
                            this.privateKey = this.privateKeyList[0]['address']
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
        querySignHash() {
            //  if(this.encryptionId == '1'){
            //    this.signKey[0] = 'p'
            // }
            if (!this.privateKey || !this.inputSignHash) return;
            this.loading = true;
            let param = {
                hash: this.inputSignHash,
                user: this.privateKey
            }
            signHash(param)
                .then(res => {
                    this.loading = false;
                    const { data, status } = res;
                    if (status === 200) {
                        this.inputSign = res.data;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        copyKey(val) {
            var val = val.toString()
            if (!val) {
                this.$message({
                    type: "fail",
                    showClose: true,
                    message: this.$t('notice.copyFailure'),
                    duration: 2000
                });
            } else {
                this.$copyText(val).then(e => {
                    this.$message({
                        type: "success",
                        showClose: true,
                        message: this.$t('notice.copySuccessfully'),
                        duration: 2000
                    });
                });
            }
        },
        // uploadCrt(param) {
        //     this.inputFile = null
        //     this.fileList = []
        //     this.fileList.push({
        //         name: param.file.name
        //     })
        //     var reader = new FileReader(), self = this;
        //     this.inputText = ""
        //     reader.readAsArrayBuffer(param.file);
        //     reader.onload = function (e) {
        //         console.log(1111);
        //         if (e.target.readyState == 2) { // DONE == 2
        //             // let wordArray = null;
        //             // wordArray = CryptoJS.lib.WordArray.create(e.target.result);
        //             // console.log(222);
        //             var wordArray = self.arrayBufferToWordArray(e.target.result)
        //             self.inputFile = wordArray;
        //             var hash = CryptoJS.SHA256(self.inputFile);
        //             console.log(hash.toString(CryptoJS.enc.Hex));
        //         }
        //     }
        //     this.$refs.upload.clearFiles()
        // },
        uploadCrt(param) {
            var contractFile = param.file;
            var reader = new FileReader(), self = this;;
            var blobSlice = File.prototype.mozSlice || File.prototype.webkitSlice || File.prototype.slice;
            // 指定文件分块大小(2M)
            var chunkSize = 6 * 1024 * 1024;
            // 计算文件分块总数
            var chunks = Math.ceil(contractFile.size / chunkSize);
            // 指定当前块指针
            var currentChunk = 0;

            var hasher = CryptoJS.algo.SHA256.create();
            // FileReader分片式读取文件
            // 计算开始读取的位置
            var start = currentChunk * chunkSize;
            // 计算结束读取的位置
            var end = start + chunkSize >= contractFile.size ? contractFile.size : start + chunkSize;
            reader.readAsArrayBuffer(blobSlice.call(contractFile, start, end));
            reader.onload = function (evt) {
                if (evt.target.readyState == 2) {
                    var fileStr = evt.target.result;
                    var tmpWordArray = self.arrayBufferToWordArray(fileStr);
                    hasher.update(tmpWordArray);
                    currentChunk += 1;
                    fileStr = null;
                    tmpWordArray = null;
                    // 判断文件是否都已经读取完
                    if (currentChunk < chunks) {
                        // 计算开始读取的位置
                        var start = currentChunk * chunkSize;
                        // 计算结束读取的位置
                        var end = start + chunkSize >= contractFile.size ? contractFile.size : start + chunkSize;
                        reader.readAsArrayBuffer(blobSlice.call(contractFile, start, end));
                    }
                }


            }
            reader.onloadend = function () {
                contractFile = null;
                var hash = hasher.finalize();
                self.inputFile = hash;
                hasher = null;
                blobSlice = null;
                reader = null;
                hash = null;
                // CollectGarbage();
            }
        },
        arrayBufferToWordArray(ab) {
            var i8a = new Uint8Array(ab);
            var a = [];
            for (var i = 0; i < i8a.length; i += 4) {
                a.push(i8a[i] << 24 | i8a[i + 1] << 16 | i8a[i + 2] << 8 | i8a[i + 3]);
            }
            return CryptoJS.lib.WordArray.create(a, i8a.length);
        },
        handleClick() {

        },
        textFocus($event) {
            this.inputFile = null;
            this.$refs.upload.clearFiles()
        },
        uploadSuccess(response, file, fileList) {

        },
        handleFileType() {
            this.inputFile = ""
            this.inputText = ""
            this.inputHash = ""
            this.$refs.upload.clearFiles()
        },
        onBeforeUpload(file) {
            const isLt1M = Math.ceil(file.size / 1024) < 5000;
            if (!isLt1M) {
                this.$message.error(this.$t('text.fileSize_5000'));
            }
            return isLt1M;
        },
          createUser(){
            this.creatUserNameVisible = true;
        },
         createUserClose(data){
             this.privateKeyList = data; 
             if(this.privateKeyList.length > 0 ){
                this.isShowAddUserBtn = false;
                this.privateKey = this.privateKeyList[0]['address']
             }
             this.creatUserNameVisible = false;
        },
    }
}
</script>

<style scoped>
.hash-title {
    border-bottom: 1px solid;
    padding: 10px 0;
}
.text-title {
    padding: 10px 0;
}
.hash-wrapper {
    padding: 0px 20px;
}
.result {
    /* padding: 5px;
    border: 1px solid; */
}
.copy-hash {
    position: absolute;
    z-index: 1;
    right: 10px;
}
.upload-file-hash {
    width: 450px;
}
.upload-file-hash >>> .el-upload-dragger {
    background-color: unset;
    height: 130px;
    width: 440px;
    border-radius: 4px;
}
.upload-file-hash >>> .el-upload-dragger .el-upload__text {
    color: #fff;
}
.upload-file-hash >>> .el-upload-dragger .el-icon-upload {
    margin-top: 16px;
}
.el-upload__tip {
    color: #fff;
}
</style>
