<template>
    <div>
        <content-head :headTitle="$t('route.onlineTools')"></content-head>
        <div class="module-wrapper" style="padding: 20px 20px 20px 20px;">
            <el-tabs v-model="activeName" @tab-click="handleClick">
                <el-tab-pane :label="$t('onlineTools.onlineHashCalculator')" name="first">
                    <div class="hash-wrapper">
                        <div class="calc-hash">
                            <!-- <el-tabs v-model="fileType" @tab-click="handleFileType"> -->
                                <!-- <el-tab-pane :label="$t('onlineTools.file')" name="file-first">
                                    <div>
                                        <p class="font-color-fff text-title">{{$t('onlineTools.file')}}</p>
                                        <div>
                                            <el-upload ref="upload" class="upload-file-hash" :file-list="fileList" :show-file-list="true" :limit="1" drag action :http-request="uploadCrt" :on-success="uploadSuccess">
                                                <i class="el-icon-upload"></i>
                                                <div class="el-upload__text">{{$t('onlineTools.drag')}}<em>{{$t('onlineTools.upload')}}</em></div>
                                                <div slot="tip" class="el-upload__tip">
                                                    <i class="el-icon-info"></i>
                                                    {{$t('onlineTools.uploadNumLimit')}}
                                                </div>
                                            </el-upload>

                                        </div>
                                    </div>
                                </el-tab-pane> -->
                                <!-- <el-tab-pane :label="$t('onlineTools.text')" name="file-second"> -->
                                    <div>
                                        <p class="font-color-fff text-title">{{$t('onlineTools.text')}}</p>
                                        <el-input type="textarea" v-model="inputText" @input="textFocus" style="margin-bottom: 20px;"></el-input>
                                    </div>
                                <!-- </el-tab-pane> -->
                            <!-- </el-tabs> -->
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
                            <span></span>
                            <el-select v-model="privateKey" placeholder="">
                                <el-option v-for="item in privateKeyList" :key="item.address" :label="item.userName" :value="item.address">
                                </el-option>
                            </el-select>
                            <el-button type="primary" style="margin-left:5px;" @click="querySignHash" v-loading="loading">{{$t('onlineTools.sign')}}</el-button>
                        </div>
                        <p class="font-color-fff text-title">{{$t('onlineTools.result')}}</p>
                        <div class="result">
                            <ul>
                                <li v-for="(value, key) in inputSign">
                                    <span>
                                        {{key}}:
                                    </span>
                                    <span>
                                        {{value}}
                                        <i class="wbs-icon-copy font-12 copy-public-key" @click="copyKey(value)" :title="$t('title.copy')"></i>
                                    </span>
                                </li>
                            </ul>
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import { queryLocalKeyStores, signHash } from "@/util/api";
const gm = require('@/util/SM2Sign');
import CryptoJS from 'crypto-js'
export default {
    name: 'onlineTools',

    components: {
        contentHead
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
            algorithm: "sha256",
            algorithmOpt: [
                {
                    label: "sha256",
                    value: "sha256"
                }
            ],
            privateKeyList: [],
            privateKey: "",
            loading: false,
            activeName: 'first',
            fileList: [],
            fileType: "file-first"
        }
    },

    computed: {
    },

    watch: {
    },

    created() {
    },

    mounted() {
        this.getLocalKeyStores();
    },

    methods: {
        encryption() {
            if (this.algorithm === 'sha256') {
                let content;
                if (this.inputText) {
                    content = this.inputText;
                } else {
                    content = this.inputFile;
                }
                this.inputHash = CryptoJS.SHA256(content).toString();
            } else if (this.algorithm === 'sm3') {
                let content;
                if (this.inputText) {
                    content = this.inputText;
                } else {
                    content = this.inputFile;
                }
                this.inputHash = gm.sm3Digest(content)
            }
        },
        getLocalKeyStores() {
            queryLocalKeyStores()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.privateKeyList = data;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        querySignHash() {
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
                        console.log(res.data);
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
        uploadCrt(param) {
            this.inputFile = null
            this.fileList = []
            this.fileList.push({
                name: param.file.name
            })
            var reader = new FileReader(), self = this;
            this.inputText = ""
            reader.readAsArrayBuffer(param.file);
            reader.onload = function (e) {
                console.log(1111);
                if (e.target.readyState == 2) { // DONE == 2
                    // let wordArray = null;
                    // wordArray = CryptoJS.lib.WordArray.create(e.target.result);
                    // console.log(222);
                    var wordArray =  self.arrayBufferToWordArray(e.target.result)
                    self.inputFile = wordArray;
                    var hash = CryptoJS.SHA256(self.inputFile);
                    console.log(hash.toString(CryptoJS.enc.Hex));
                }
            }
            this.$refs.upload.clearFiles()
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
            // this.$refs.upload.clearFiles()
        },
        uploadSuccess(response, file, fileList) {
            console.log(response, file, fileList);
        },
        handleFileType() {

        }
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
