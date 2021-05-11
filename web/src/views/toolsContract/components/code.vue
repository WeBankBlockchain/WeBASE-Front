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
    <div class="contract-code" :class="{changeActive:changeWidth }" v-loading="loadingAce">
        <div class="contract-code-head">
            <span class="contract-code-title" v-show="codeShow" :class="{titleActive:changeWidth }">
                <span ref="setReadOnly">{{contractName + '.sol'}}</span>
            </span>
            <span class="contract-code-handle" v-show="codeShow&&urlQuery.storeType =='1'">
                <el-button type="primary" size="mini" @click="exportToIde">{{$t('contracts.exportToIde')}}</el-button>
            </span>
        </div>
        <div class="contract-code-content" :class="{infoHide: !successHide}">
            <div class="contract-code-mirror" :style="{height:codeHight}" ref="codeContent">
                <div style="padding-top: 60px;text-align:center;" v-show="!codeShow">
                    <span class="font-color-9da2ab">{{$t('text.noStoreContract')}}</span>
                </div>
                <div class="ace-editor" ref="ace" v-show="codeShow"></div>
            </div>
            <div class="contract-info" v-show="successHide" :style="{height:infoHeight + 'px'}">
                <div class="move" @mousedown="dragDetailWeight($event)" @mouseup="resizeCode"></div>
                <div v-if="contractDesc" class="md-text">
                    <mavon-editor :previewBackground="prop.previewBackground" :toolbars="toolbars" v-model="contractDesc" :subfield="prop.subfield" :defaultOpen="prop.defaultOpen" :toolbarsFlag="prop.toolbarsFlag" :editable="prop.editable" :scrollStyle="prop.scrollStyle" :boxShadow="prop.boxShadow" ref=md />
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import ace from "ace-builds";
import "ace-builds/webpack-resolver";
import "ace-builds/src-noconflict/theme-monokai";
import "ace-builds/src-noconflict/theme-tomorrow";
import "ace-builds/src-noconflict/mode-javascript";
import "ace-builds/src-noconflict/ext-language_tools";
require("ace-mode-solidity/build/remix-ide/mode-solidity");
let Mode = require("ace-mode-solidity/build/remix-ide/mode-solidity").Mode;
let Base64 = require("js-base64").Base64;
import Bus from "@/bus";
import { } from "@/util/api";
import transaction from "@/components/sendTransaction";
import web3 from "@/util/ethAbi"
export default {
    name: "codes",
    props: ["show", "changeStyle", "urlQuery"],
    components: {
        "v-transaction": transaction,
    },
    data() {
        return {
            successHide: true,
            loadingAce: false,
            content: "",
            code: "",
            contractName: "",
            infoHeight: 300,
            aceEditor: null,
            themePath: "ace/theme/monokai",
            modePath: "ace/mode/solidity",
            data: null,
            codeShow: false,
            saveShow: false,
            language: localStorage.getItem('lang'),
            toolbars: {},
            contractDesc: ""
        };
    },
    watch: {
        content(val) {
            let data = Base64.decode(this.data.contractSrc);
            if (data != val) {
                this.saveShow = true;
            } else {
                this.saveShow = false;
            }
        },
        successHide(val) {
            if (val) {
                this.infoHeight = 300;
            } else {
                this.infoHeight = 0;
            }
        }
    },
    computed: {
        codeHight() {
            if (this.infoHeight) {
                return `calc(100% - ${this.infoHeight}px)`;
            } else {
                return `100%`;
            }
        },
        changeWidth() {
            if (this.changeStyle) {
                return this.changeStyle;
            } else {
                return false;
            }
        },
        tipShow() {
            return !this.show;
        },
        prop() {
            let data = {
                subfield: false,// 单双栏模式
                defaultOpen: 'preview',//edit： 默认展示编辑区域 ， preview： 默认展示预览区域 
                editable: false,
                toolbarsFlag: false,
                scrollStyle: false,
                boxShadow: false,
                previewBackground: '#20293c'
            }
            return data
        }
    },


    created() {

    },
    beforeDestroy() {
        Bus.$off("select")
        Bus.$off("chooselanguage")

    },
    mounted() {
        this.initEditor();
        Bus.$on("chooselanguage", data => {
            this.language = data;
            if (!this.data) { return }
            if (this.language = 'zh') {
                this.contractDesc = this.data.contractDesc;
            } else {
                this.contractDesc = this.data.contractDesc_en;
            }
        })
        Bus.$on('select', data => {
            console.log(data.contractSrc);
            this.codeShow = true;
            this.code = "";
            this.contractName = "";
            this.content = "";
            this.contractDesc = "";
            this.data = data;
            this.code = Base64.decode(data.contractSrc);
            this.content = this.code;
            this.aceEditor.setValue(this.content);
            this.contractName = data.contractName;
            if (this.language = 'zh') {
                this.contractDesc = this.data.contractDesc;
            } else {
                this.contractDesc = this.data.contractDesc_en;
            }
        })

    },
    methods: {
        initEditor() {
            let _this = this;
            this.aceEditor = ace.edit(this.$refs.ace, {
                fontSize: 14,
                fontFamily: "Consolas,Monaco,monospace",
                theme: this.themePath,
                mode: this.modePath,
                tabSize: 4,
                useSoftTabs: true
            });
            this.aceEditor.setOptions({
                enableSnippets: true,
                enableLiveAutocompletion: true,
                enableBasicAutocompletion: true,
                autoScrollEditorIntoView: true,
                copyWithEmptySelection: true
            });
            this.aceEditor.setReadOnly(true)
            let editor = this.aceEditor.alignCursors();
            this.aceEditor.getSession().setUseWrapMode(true);
            this.aceEditor.setHighlightActiveLine(true)
            this.aceEditor.resize();
        },
        resizeCode() {
            this.aceEditor.setOptions({
                maxLines: Math.ceil(this.$refs.codeContent.offsetHeight / 17) - 1
            });
            this.aceEditor.resize();
        },
        dragDetailWeight(e) {
            let startY = e.clientY,
                infoHeight = this.infoHeight;
            document.onmousemove = e => {
                let moveY = startY - e.clientY;
                this.infoHeight = infoHeight + moveY;
            };
            document.onmouseup = e => {
                document.onmousemove = null;
                document.onmouseup = null;
            };
            this.aceEditor.setOptions({
                maxLines: Math.ceil(this.$refs.codeContent.offsetHeight / 17) - 1,
                minLines: 9
            });
        },
        exportToIde() {
            Bus.$emit('code', this.data, 'file')
        }
    }
};
</script>
<style scoped>
.contract-code {
    height: 100%;
    border-left: 1px solid #242e42;
    box-sizing: border-box;
}
.changeActive {
    padding-left: 0 !important;
}
.contract-code-head {
    width: 100%;
    height: 48px;
    line-height: 48px;
    color: #fff;
    border-bottom: 2px solid #242e42;
    background-color: #2b374d;
    position: relative;
}
.search-model {
    position: absolute;
    z-index: 100;
    right: 10px;
    top: 60px;
    float: left;
    width: 450px;
    border: 1px solid #8798ad;
    border-radius: 3px;
    background: #555651;
    height: 40px;
    line-height: 40px;
}
.search-model >>> .el-input__inner {
    height: 34px;
    line-height: 34px;
}
.search-model span {
    display: inline-block;
}
.search-span-info {
    height: 45px;
    line-height: 45px;
}
.search-btn {
    height: 30px;
    line-height: 30px;
    margin: 0 10px;
    text-align: center;
    border-radius: 3px;
    width: 60px;
}
.iconfont-info {
    margin: 0 5px;
}
.close-search {
    float: right;
    margin-right: 11px;
}
.contract-code-done {
    display: inline-block;
    margin-right: 10px;
    cursor: pointer;
}
.contract-code-done i {
    vertical-align: middle;
}
.contract-code-done span {
    font-size: 12px;
    color: #fff;
    vertical-align: middle;
}
.contract-no-content {
    border-left: 1px solid #242e42;
    height: calc(100% - 50px);
    box-sizing: border-box;
}
.contract-code-content {
    border-left: 1px solid #242e42;
    height: calc(100% - 50px);
    box-sizing: border-box;
}
.contract-code-mirror {
    width: 100%;
    height: 70%;
}
.contract-info {
    position: relative;
    padding-top: 10px;
    text-align: left;
    border-top: 1px solid #242e42;
    box-sizing: border-box;
}
.md-text {
    height: 100%;
    overflow: auto;
}
.contract-info-content {
    height: 100%;
    overflow-y: scroll;
    box-sizing: border-box;
}
.contract-code-title {
    float: left;
    font-weight: bold;
    font-size: 18px;
    padding-left: 20px;
}
.contract-code-handle {
    float: right;
    padding-right: 20px;
}
.contract-info-title {
    text-align: center;
    cursor: pointer;
    padding: 5px 0px;
}
.contract-info-title:hover > i {
    color: #fff;
    font-size: 14px;
}
.move {
    position: absolute;
    width: 100%;
    height: 3px;
    top: 0;
    left: 0;
    z-index: 9999;
    cursor: s-resize;
}
.contract-info-title i {
    padding-left: 8px;
    font-size: 10px;
    color: #aeb1b5;
}
.contract-info-list {
    padding: 5px 20px;
    width: 90%;
    margin: 0 auto;
    border: 1px solid #242e42;
    border-bottom: none;
    position: relative;
}
.contract-info-list-title {
    display: inline-block;
    width: 105px;
    vertical-align: top;
}
.contract-info-list-title::after {
    display: block;
    content: "";
    clear: both;
}
.ace-editor {
    height: 100% !important;
    position: relative;
    text-align: left;
    letter-spacing: 0.1px;
    text-rendering: geometricPrecision;
    font-feature-settings: "liga" 0;
    font-variant-ligatures: none;
    font: 14px / normal "Monaco", "Menlo", "Ubuntu Mono", "Consolas",
        "source-code-pro", monospace !important;
}
.ace-editor >>> .ace_print-margin {
    display: none;
    text-rendering: geometricPrecision;
}
.infoHide {
    height: calc(100% - 50px);
}
.code-spread {
    position: absolute;
    width: 33px;
    height: 33px;
    line-height: 33px;
    left: 412px;
    bottom: 0;
    border: 1px solid #242e42;
    color: #aeb1b5;
    background-color: #fff;
    text-align: center;
    z-index: 9999;
    cursor: pointer;
}
.code-spread i {
    font-size: 12px;
}
.contract-info {
    background-color: #2b374d;
    color: #fff;
}
.titleActive {
    padding-left: 40px;
}
.send-dialog >>> .el-dialog--center .el-dialog__body {
    padding: 10px 25px 15px;
}
.send-dialog >>> .el-dialog__footer {
    padding-top: 0;
}
.send-btn >>> .el-button {
    padding: 9px 16px;
}
.send-dialog >>> .el-input__inner {
    height: 32px;
    line-height: 32px;
}
.showText {
    display: inline-block;
    width: calc(100% - 120px);
    word-wrap: break-word;
    max-height: 73px;
    overflow: hidden;
}
.copy-public-key {
    float: right;
}
.visibility-wrapper {
    position: absolute;
    bottom: 10px;
}
.contract-info >>> .markdown-body {
    color: #9da2ab;
}
.contract-info >>> .v-note-wrapper {
    border: inherit
}
</style>
