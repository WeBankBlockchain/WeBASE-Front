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
  <div class="contract-code" :class="{ changeActive: changeWidth }" v-loading="loadingAce">
    <div class="contract-code-head">
      <span class="contract-code-title" v-show="codeShow" :class="{ titleActive: changeWidth }">
        <span ref="setReadOnly">{{ contractName + ".sol" }}</span>
      </span>
      <span class="contract-code-handle" v-show="codeShow">
        <span class="contract-code-done noBlur" @click="saveCode" id="saveId">
          <el-tooltip class="item" effect="dark" :content="$t('title.handleSave')" placement="top-start">
            <i class="wbs-icon-baocun contract-icon-style font-16"></i>
          </el-tooltip>
          <span>{{ $t("title.save") }}</span>
        </span>
        <span class="contract-code-done noBlur" @click="compile" v-if="!loadingAce" id="compileId">
          <el-tooltip class="item" effect="dark" :content="$t('title.handleCompile')" placement="top-start">
            <i class="wbs-icon-bianyi contract-icon-style font-16"></i>
          </el-tooltip>
          <span>{{ $t("title.compile") }}</span>
        </span>
        <span class="contract-code-done" @click="deploying">
          <el-tooltip class="item" effect="dark" :content="$t('title.handleDeploy')" placement="top-start">
            <i class="wbs-icon-deploy contract-icon-style font-16"></i>
          </el-tooltip>
          <span>{{ $t("title.deploy") }}</span>
        </span>
        <span class="contract-code-done" @click="send">
          <el-tooltip class="item" effect="dark" :content="$t('title.handleCallContract')" placement="top-start">
            <i class="wbs-icon-send contract-icon-style font-16"></i>
          </el-tooltip>
          <span>{{ $t("title.callContract") }}</span>
        </span>
        <span class="contract-code-done" @click="downloadJavaClass">
          <i class="el-icon-download contract-icon-style font-16"></i>
          <span>{{ $t("title.exportJavaFile") }}</span>
        </span>
        <span class="contract-code-done" @click="exportSdk">
          <i class="el-icon-download contract-icon-style font-16"></i>
          <span>{{ $t("title.exportSdk") }}</span>
        </span>
        <span class="contract-code-done" @click="exportJava">
          <i class="el-icon-download contract-icon-style font-16"></i>
          <span>{{ $t("title.exportJavaProject") }}</span>
        </span>
      </span>
      <div class="search-model" v-if="searchVisibility">
        <el-input v-model="keyword" placeholder="搜索" style="width: 266px; margin-left: 10px" ref="searchInput" @keyup.esc.native="closeBtn" @keyup.enter.native="searchBtn" @keyup.down.native="nextBtn"
          @keyup.up.native="previousBtn"></el-input>
        <span class="search-btn bf-58cb7d cursor-pointer no-chase" @click="searchBtn">查找</span>
        <span class="search-span-info" @click="previousBtn">
          <i class="
              el-icon-back
              iconfont-info
              font-color-58cb7d font-15
              cursor-pointer
              no-chase
            "></i>
        </span>
        <span class="search-span-info" @click="nextBtn">
          <i class="
              el-icon-right
              iconfont-info
              font-color-58cb7d font-15
              cursor-pointer
              no-chase
            "></i>
        </span>
        <span class="close-search cursor-pointer search-span-info" @click="closeBtn">
          <i class="el-icon-close font-15"></i>
        </span>
      </div>
    </div>
    <div class="contract-code-content" :class="{ infoHide: !successHide }">
      <div class="contract-code-mirror" :style="{ height: codeHight }" ref="codeContent">
        <div style="padding-top: 60px; text-align: center" v-show="!codeShow">
          <span class="font-color-9da2ab">{{ $t("text.noContract") }}</span>
        </div>
        <div class="ace-editor" ref="ace" v-show="codeShow"></div>
      </div>
      <div class="contract-info" v-show="successHide" :style="{ height: infoHeight + 'px' }">
        <div class="move" @mousedown="dragDetailWeight($event)" @mouseup="resizeCode"></div>
        <div class="contract-info-title" @mouseover="mouseHover = true" @mouseleave="mouseHover = false" v-show="abiFile || contractAddress" @click="collapse">
          <i :class="[
              showCompileText ? 'el-icon-caret-bottom' : 'el-icon-caret-top',
            ]">
          </i>
          <template v-if="showCompileText && mouseHover">
            {{ $t("text.hide") }}
          </template>
          <template v-else-if="!showCompileText && mouseHover">
            {{ $t("text.expand") }}
          </template>
        </div>
        <div>
          <div class="contract-info-list1" v-html="compileinfo"></div>
          <div class="contract-info-list1" style="color: #f56c6c" v-show="errorInfo">
            {{ errorInfo }}
          </div>
          <div class="contract-info-list1 error-item" style="color: #f56c6c" v-show="errorInfo">
            <!-- <span style="display:inline-block;width:calc(100% - 120px);word-wrap:break-word" v-for="(item, index) in errorMessage">{{index+1}}、{{item}}</span> -->
            <!-- <span style="display:inline-block;width:calc(100% - 120px);word-wrap:break-word" v-for="(item, index) in errorMessage" :style="{'color' : severityColor(item)}">
                            {{index+1}}: {{item | formatErrorMessage}}
                            <i class="el-icon-circle-plus-outline" @click="optenErrorInfo(item, index)"></i>{{item | formatErrorMessage}}
                            <span style="display:inline-block;width:calc(100% - 120px);word-wrap:break-word" v-show="item.open">
                                <span>
                                    <pre>{{item}}</pre>
                                </span>
                            </span>
                        </span> -->
            <el-collapse v-model="activeNames" @change="handleChange">
              <el-collapse-item :name="index" v-for="(item, index) in errorMessage" :key="index" :style="{ color: severityColor(item) }">
                <template slot="title">
                  {{ index + 1 }}、{{ item | formatErrorMessage }}
                </template>
                <span style="
                    display: inline-block;
                    width: calc(100% - 120px);
                    word-wrap: break-word;
                  ">
                  <span>
                    <pre :style="{ color: severityColor(item) }">{{
                      item
                    }}</pre>
                  </span>
                </span>
              </el-collapse-item>
            </el-collapse>
          </div>
          <div style="color: #68e600; padding-bottom: 15px" v-show="abiFileShow">
            {{ successInfo }}
          </div>
          <div class="contract-info-list" v-if="contractAddress">
            <span class="contract-info-list-title" style="color: #0b8aee">contractAddress
              <i class="wbs-icon-copy font-12 copy-public-key" @click="copyKey(contractAddress)" :title="$t('title.copyContractAddress')"></i>
            </span>
            <span style="
                display: inline-block;
                width: calc(100% - 120px);
                word-wrap: break-word;
              ">
              {{ contractAddress }}
              <span v-if="reqVersion" style="margin-left: 10px">(CNS: {{ cnsName }} {{ reqVersion }})</span>
              <span v-else style="color: #1f83e7; cursor: pointer; margin-left: 10px" @click="handleRegisterCns">{{ $t("text.register") }}</span>
            </span>
          </div>
          <div v-else v-show="abiFile" class="contract-info-list">
            <span v-if="!abiEmpty" class="contract-info-list-title" style="color: #0b8aee">contractAddress
            </span>
            <span v-if="!abiEmpty" style="color: #1f83e7; cursor: pointer; margin-left: 10px" @click="addContractAddress">{{ $t("text.addContractAddress") }}</span>
          </div>
          <div class="contract-info-list" v-if="abiFile">
            <span class="contract-info-list-title" style="color: #0b8aee">contractName
              <i class="wbs-icon-copy font-12 copy-public-key" @click="copyKey(contractName)" :title="$t('title.copyContractName')"></i>
            </span>
            <span style="
                display: inline-block;
                width: calc(100% - 120px);
                word-wrap: break-word;
              ">
              {{ contractName }}
            </span>
          </div>
          <div class="contract-info-list" v-show="abiFile">
            <span class="contract-info-list-title" style="color: #0b8aee">abi
              <i class="wbs-icon-copy font-12 copy-public-key" @click="copyKey(abiFile)" :title="$t('title.copyAbi')"></i>
            </span>
            <span class="showText" ref="showAbiText">
              {{ abiFile }}
            </span>
            <i :class="[
                showAbi ? 'el-icon-arrow-down' : 'el-icon-arrow-up',
                'font-13',
                'cursor-pointer',
                'visibility-wrapper',
              ]" v-if="complieAbiTextHeight" @click="showAbiText"></i>
          </div>
          <div class="contract-info-list" style="border-bottom: 1px solid #242e42" v-show="abiFile">
            <span class="contract-info-list-title" style="color: #0b8aee">bytecodeBin
              <i class="wbs-icon-copy font-12 copy-public-key" @click="copyKey(bytecodeBin)" :title="$t('title.copyBin')"></i>
            </span>
            <span class="showText" ref="showBinText">
              {{ bytecodeBin }}
            </span>
            <i :class="[
                showBin ? 'el-icon-arrow-down' : 'el-icon-arrow-up',
                'font-13',
                'cursor-pointer',
                'visibility-wrapper',
              ]" v-if="complieBinTextHeight" @click="showBinText"></i>
          </div>
        </div>
      </div>
    </div>
    <el-dialog :title="$t('title.callContract')" :visible.sync="dialogVisible" width="600px" :before-close="sendClose" v-if="dialogVisible" center class="send-dialog">
      <v-transaction @success="sendSuccess($event)" @close="handleClose" ref="send" :sendErrorMessage="sendErrorMessage" :data="data" :abi="abiFile" :version="version"></v-transaction>
    </el-dialog>
    <el-dialog :title="$t('title.selectAccountAddress')" :visible.sync="dialogUser" width="550px" v-if="dialogUser" center class="send-dialog">
      <v-user @change="deployContract(arguments)" @close="userClose" :contractName="contractName" :abi="abiFile"></v-user>
    </el-dialog>
    <v-editor v-if="editorShow" :show="editorShow" :data="editorData" :sendConstant="sendConstant" @close="editorClose" ref="editor" :input="editorInput" :editorOutput="editorOutput"></v-editor>
    <el-dialog :title="$t('title.writeJavaName')" :visible.sync="javaClassDialogVisible" width="500px" center class="send-dialog" @close="initJavaClass">
      <el-input v-model="javaClassName" :placeholder="$t('placeholder.javaPackage')"></el-input>
      <span class="font-12 font-color-ed5454" style="margin-left: 5px">{{
        $t("dialog.exportJavaNote")
      }}</span>
      <div slot="footer" class="text-right send-btn">
        <el-button @click="closeJavaClass">{{ $t("dialog.cancel") }}</el-button>
        <el-button type="primary" @click="sureJavaClass">{{
          $t("dialog.confirm")
        }}</el-button>
      </div>
    </el-dialog>
    <el-dialog v-if="mgmtCnsVisible" :title="$t('text.cns')" :visible.sync="mgmtCnsVisible" width="470px" center class="send-dialog">
      <mgmt-cns :mgmtCnsItem="mgmtCnsItem" @mgmtCnsResultSuccess="mgmtCnsResultSuccess($event)" @mgmtCnsResultClose="mgmtCnsResultClose"></mgmt-cns>
    </el-dialog>

    <el-dialog :visible.sync="addContractAddressVisible" :title="$t('dialog.addContractAddress')" width="400px" class="dialog-wrapper" center v-if="addContractAddressVisible">
      <el-form ref="contractForm" :model="contractForm">
        <el-form-item label="" prop="contractAddress">
          <el-input v-model="contractForm.contractAddress" :placeholder="$t('contracts.contractAddressInput')"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="text-right">
        <el-button @click="closeContractAddress">{{
          $t("table.cancel")
        }}</el-button>
        <el-button type="primary" @click="sureContractAddress('contractForm')">{{ $t("table.confirm") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import ace from "ace-builds";
// import "ace-builds/webpack-resolver";
import "ace-builds/src-noconflict/theme-monokai";
import "ace-builds/src-noconflict/theme-tomorrow";
import "ace-builds/src-noconflict/mode-javascript";
import "ace-builds/src-noconflict/ext-language_tools";
require("ace-mode-solidity/build/remix-ide/mode-solidity");
let Mode = require("ace-mode-solidity/build/remix-ide/mode-solidity").Mode;
let Base64 = require("js-base64").Base64;
import constant from "@/util/constant";
import editor from "../dialog/editor";
import Bus from "@/bus";
import {
  getDeployStatus,
  ifChangedDepaloy,
  queryJavaClass,
  addFunctionAbi,
  backgroundCompile,
  registerCns,
  findCnsInfo,
  exportCertSdk,
  saveChaincode,
} from "@/util/api";
import transaction from "@/components/sendTransaction";
import changeUser from "../dialog/changeUser";
import web3 from "@/util/ethAbi";
import mgmtCns from "../dialog/mgmtCns";
export default {
  name: "codes",
  props: ["show", "changeStyle"],
  components: {
    "v-transaction": transaction,
    "v-user": changeUser,
    "v-editor": editor,
    mgmtCns,
  },
  data: function () {
    return {
      successHide: true,
      loadingAce: false,
      content: "",
      successShow: true,
      errorShow: false,
      dialogVisible: false,
      javaClassDialogVisible: false,
      javaClassName: "",
      code: "",
      status: 0,
      abiFile: "",
      abiEmpty: true,
      bin: "",
      contractAddress: "",
      contractName: "",
      infoHeight: 250,
      contractList: [],
      dialogUser: false,
      compileinfo: "",
      errorInfo: "",
      errorMessage: "",
      successInfo: "",
      abiFileShow: false,
      bytecodeBin: "",
      aceEditor: null,
      themePath: "ace/theme/monokai",
      modePath: "ace/mode/solidity",
      data: null,
      codeShow: false,
      version: "",
      saveShow: false,
      editorShow: false,
      editorData: null,
      editorInput: null,
      editorOutput: null,
      showAbi: true,
      showBin: true,
      complieAbiTextHeight: false,
      complieBinTextHeight: false,
      mouseHover: false,
      showCompileText: true,
      keyword: "",
      searchVisibility: false,
      modifyState: false,
      sendErrorMessage: "",
      sendConstant: null,
      errorInfoVisible: false,
      openErrorIndex: "",
      reqVersion: "",
      cnsName: "",
      mgmtCnsVisible: false,
      mgmtCnsItem: {},
      activeNames: [],
      addContractAddressVisible: false,
      contractForm: {
        contractAddress: "",
      },
      nolimit: true,
    };
  },
  watch: {
    content: function (val) {
      let data = Base64.decode(this.data.contractSource);
      if (data != val) {
        this.saveShow = true;
      } else {
        this.saveShow = false;
      }
    },
    successHide: function (val) {
      if (val) {
        this.infoHeight = 250;
      } else {
        this.infoHeight = 0;
      }
    },
    keyword: function (val) {
      this.aceEditor.find(`${val}`, {
        backwards: false,
        wrap: true,
        caseSensitive: false,
        regExp: false,
      });
      this.aceEditor.findPrevious();
    },
  },
  computed: {
    codeHight: function () {
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
  },

  created() {},
  beforeMount() {},
  mounted: function () {
    this.initEditor();
    Bus.$on("select", (data) => {
      this.codeShow = true;
      this.refreshMessage();
      this.code = "";
      this.version = "";
      this.status = null;
      this.abiFile = "";
      (this.abiEmpty = true), (this.contractAddress = "");
      this.errorMessage = "";
      this.contractName = "";
      this.content = "";
      this.bin = "";
      this.data = data;
      this.code = Base64.decode(data.contractSource);
      this.content = this.code;
      this.aceEditor.setValue(this.content);
      this.status = data.contractStatus;
      this.abiFile = data.contractAbi;
      if (!(!this.abiFile || this.abiFile == "[]")) {
        this.abiEmpty = false;
      }
      this.contractAddress = data.contractAddress;
      this.errorMessage = data.description || "";
      this.contractName = data.contractName;
      this.bin = data.contractBin;
      this.bytecodeBin = data.bytecodeBin || "";
      this.version = data.version;
      this.complieAbiTextHeight = false;
      this.complieBinTextHeight = false;
      this.$nextTick(() => {
        if (
          (this.contractName === "Asset" && data.contractPath === "template") ||
          (this.contractName === "Evidence" &&
            data.contractPath === "template") ||
          (this.contractName === "EvidenceFactory" &&
            data.contractPath === "template")
        ) {
          this.aceEditor.setReadOnly(true);
        } else {
          this.aceEditor.setReadOnly(false);
        }
      });
      if (this.data.contractAddress) {
        this.queryFindCnsInfo();
      }
    });
    Bus.$on("noData", (data) => {
      this.codeShow = false;
      this.refreshMessage();
      this.code = "";
      this.version = "";
      this.status = null;
      this.abiFile = "";
      this.abiEmpty = true;
      this.contractAddress = "";
      this.errorMessage = "";
      this.contractName = "";
      this.content = "";
      this.bin = "";
    });
    Bus.$on("limit", (data) => {
      this.nolimit = false;
      let that = this;
      setTimeout(() => {
        that.nolimit = true;
      }, 500);
    });
    [...document.querySelectorAll(".noBlur")].map((item) => {
      item.onmousedown = (e) => {
        if (e && e.preventDefault) {
          e.preventDefault();
        } else {
          window.event.returnValue = false;
        }
        return false;
      };
    });
  },

  methods: {
    initEditor: function () {
      let _this = this;
      this.aceEditor = ace.edit(this.$refs.ace, {
        fontSize: 14,
        fontFamily: "Consolas,Monaco,monospace",
        theme: this.themePath,
        mode: this.modePath,
        tabSize: 4,
        useSoftTabs: true,
      });
      this.aceEditor.setOptions({
        enableSnippets: true,
        enableLiveAutocompletion: true,
        enableBasicAutocompletion: true,
        autoScrollEditorIntoView: true,
        copyWithEmptySelection: true,
      });
      this.aceEditor.commands.addCommand({
        name: "save",
        bindKey: { win: "Ctrl-S", mac: "Command-S" },
        exec: function (editor) {
          _this.saveCode();
        },
      });
      this.aceEditor.commands.addCommand({
        name: "compile",
        bindKey: { win: "Alt-C", mac: "Option-C" },
        exec: function (editor) {
          _this.compile();
        },
      });
      this.aceEditor.commands.addCommand({
        name: "deploying",
        bindKey: { win: "Alt-D", mac: "Option-D" },
        exec: function (editor) {
          _this.deploying();
        },
      });
      this.aceEditor.commands.addCommand({
        name: "send",
        bindKey: { win: "Alt-T", mac: "Option-T" },
        exec: function (editor) {
          _this.send();
        },
      });
      this.aceEditor.commands.addCommand({
        name: "search-keyword",
        bindKey: { win: "Ctrl-F", mac: "Command-F" },
        exec: function (editor) {
          _this.searchKeyword();
        },
      });
      //  this.aceEditor.commands.addCommand({
      //     name: 'backing',
      //     bindKey: { win: "Ctrl-z", mac: "Command-z" },
      //     exec: function (editor) {
      //         _this.backing()
      //     }
      // });
      let editor = this.aceEditor.alignCursors();
      this.aceEditor.getSession().setUseWrapMode(true);

      this.aceEditor.getSession().on("change", this.changeAce);
      this.aceEditor.setHighlightActiveLine(true);
      this.aceEditor.on("blur", this.blurAce);
      this.aceEditor.resize();
    },
    // backing:function(){
    //     console.log('backing');
    //     console.log(this.content);
    //     if(this.content==''){
    //          this.aceEditor.setValue(this.code);
    //     }
    // },
    blurAce: function (callback) {
      console.log("blur");
      let data = Base64.encode(this.content);
      // if (this.data.contractSource != data) {
      //     this.saveCode()
      // }
      //  this.saveShow = true;
      if (this.data.contractSource != data && this.nolimit) {
        console.log("合约改变弹框提示");
        this.$confirm(
          `${this.$t("text.unsavedContract")}？`,
          `${this.$t("text.title")}`,
          {
            confirmButtonText: this.$t("title.save"),
            center: true,
            type: "warning",
            dangerouslyUseHTMLString: true,
          }
        )
          .then(() => {
            this.saveCode();
          })
          .catch(() => {
            this.$message({
              type: "info",
              message: this.$t("text.noSave"),
            });
          });
      }
    },
    saveCode: function () {
      this.data.contractSource = Base64.encode(this.content);
      Bus.$emit("save", this.data);
      var id = this.data.id;
    },
    resizeCode: function () {
      this.aceEditor.setOptions({
        maxLines: Math.ceil(this.$refs.codeContent.offsetHeight / 17) - 1,
      });
      this.aceEditor.resize();
    },
    dragDetailWeight: function (e) {
      let startY = e.clientY,
        infoHeight = this.infoHeight;
      document.onmousemove = (e) => {
        let moveY = startY - e.clientY;
        this.infoHeight = infoHeight + moveY;
      };
      document.onmouseup = (e) => {
        document.onmousemove = null;
        document.onmouseup = null;
      };
      this.aceEditor.setOptions({
        maxLines: Math.ceil(this.$refs.codeContent.offsetHeight / 17) - 1,
        minLines: 9,
      });
    },
    sendSuccess: function (val) {
      this.sendConstant = val.constant;
      this.dialogVisible = false;
      this.editorShow = true;
      this.editorData = null;
      this.editorData = val.resData;
      this.editorInput = val.input;
      this.editorOutput = val.data.outputs;
    },
    editorClose: function () {
      this.editorShow = false;
    },
    changeAce: function () {
      //     console.log(this.code);
      //    console.log(this.content);

      this.$nextTick(() => {
        this.content = this.aceEditor.getSession().getValue();
        //  console.log(this.code);
        // console.log(this.content);
        if (this.content.replace(/[\r\n\s]/g, "") == "") {
          //         console.log(this.code);
          //    console.log(this.content);

          this.aceEditor.setValue(this.code);
          this.content = this.aceEditor.getSession().getValue();
          return;
        }
        var id = this.data.id;
        // this.$nextTick(() => {
        if (
          Base64.decode(this.data.contractSource).length === this.content.length
        ) {
          Bus.$emit("modifyState", {
            id: id,
            modifyState: false,
          });
        } else {
          Bus.$emit(
            "modifyState",
            Object.assign({}, this.data, {
              id: id,
              modifyState: true,
              contractSource: Base64.encode(this.content),
            })
          );
        }
        // })
      });

      // var id = this.data.id;
      // // this.$nextTick(() => {
      //     if (Base64.decode(this.data.contractSource).length === this.content.length) {
      //         Bus.$emit('modifyState', {
      //             id: id,
      //             modifyState: false
      //         })
      //     } else {

      //         Bus.$emit('modifyState', Object.assign({}, this.data, {
      //             id: id,
      //             modifyState: true,
      //             contractSource: Base64.encode(this.content)
      //         }))
      //     }
      // // })
    },

    findImports: function (path) {
      this.contractList = this.$store.state.contractDataList;
      let arry = path.split("/");
      let newpath = arry[arry.length - 1];
      let num = 0;
      if (arry.length > 1) {
        let newPath = arry[0];
        let oldPath = arry[arry.length - 1];
        let importArry = [];
        this.contractList.forEach((value) => {
          if (value.contractPath == newPath) {
            importArry.push(value);
          }
        });
        if (importArry.length) {
          for (let i = 0; i < importArry.length; i++) {
            if (oldPath == importArry[i].contractName + ".sol") {
              return {
                contents: Base64.decode(importArry[i].contractSource),
              };
            }
          }
        } else {
          return { error: "File not found" };
        }
      } else {
        let newpath = arry[arry.length - 1];
        let newArry = [];
        this.contractList.forEach((value) => {
          if (value.contractPath == this.data.contractPath) {
            newArry.push(value);
          }
        });
        if (newArry.length > 1) {
          for (let i = 0; i < newArry.length; i++) {
            if (newpath == newArry[i].contractName + ".sol") {
              return {
                contents: Base64.decode(newArry[i].contractSource),
              };
            }
          }
          for (let i = 0; i < this.contractList.length; i++) {
            if (newpath == this.contractList[i].contractName + ".sol") {
              return {
                contents: Base64.decode(this.contractList[i].contractSource),
              };
            } else {
              num++;
            }
          }
          if (num) {
            return { error: "File not found" };
          }
        } else {
          for (let i = 0; i < this.contractList.length; i++) {
            if (newpath == this.contractList[i].contractName + ".sol") {
              return {
                contents: Base64.decode(this.contractList[i].contractSource),
              };
            }
          }
        }
      }
    },
    compile(callback) {
      //  let data = Base64.encode(this.content);
      //   if (this.data.contractSource != data) {
      //       this.saveCode();
      //   }
      //   }else{
      // this.$emit("compile", true)
      //   this.loadingAce = true;
      let version = this.$store.state.versionData;
      if (version && version.net !== 0) {
        this.compileHighVersion(callback);
      } else {
        setTimeout(() => {
          this.compileLowVersion(callback);
        }, 500);
      }
      //   }
    },
    //v0.6.10
    compileHighVersion(callback) {
      //  this.loadingAce = true;
      let that = this;
      this.refreshMessage();
      this.contractList = this.$store.state.contractDataList;
      let content = "";
      let output;
      let input = {
        language: "Solidity",
        settings: {
          outputSelection: {
            "*": {
              "*": ["*"],
            },
          },
        },
      };
      input.sources = {};
      input.sources[this.contractName + ".sol"] = {};
      let libs = [];
      input.sources[this.contractName + ".sol"] = {
        content: this.content,
      };
      let w = this.$store.state.worker;
      w.postMessage({
        cmd: "compile",
        input: JSON.stringify(input),
        list: this.$store.state.contractDataList,
        path: this.data.contractPath,
      });
      let num = 0;
      // w.addEventListener('message', function (ev) {
      w.onmessage = function (ev) {
        num++;
        if (ev.data.cmd == "compiled" && num == 1) {
          that.loadingAce = false;
          output = JSON.parse(ev.data.data);
          if (
            output &&
            output.contracts &&
            JSON.stringify(output.contracts) != "{}"
          ) {
            that.status = 1;
            if (output.contracts[that.contractName + ".sol"]) {
              that.changeOutput(
                output.contracts[that.contractName + ".sol"],
                callback
              );
            }
          } else {
            that.errorMessage = output.errors;
            if (output.error) {
              that.errorMessage = [
                { component: "general", formattedMessage: output.error },
              ];
            }
            that.errorInfo = that.$t("contracts.contractCompileFail");
            that.loadingAce = false;
          }
        } else {
          console.log(ev.data);
          console.log(JSON.parse(ev.data.data));
        }
      };

      w.addEventListener("error", function (ev) {
        that.errorInfo = ev;
        that.errorMessage = ev;
        that.compileShow = true;
        that.loadingAce = false;
      });
    },
    //v0.4.25 v0.5.1
    compileLowVersion: function (callback) {
      console.log("656:wrapper:", wrapper);
      console.log("656:Module:", window, window.Module);
      let wrapper = require("solc/wrapper");
      let solc = wrapper(window.Module);

      this.refreshMessage();
      for (let i = 0; i < constant.COMPILE_INFO.length; i++) {
        this.compileinfo = this.compileinfo + constant.COMPILE_INFO(i);
      }
      this.contractList = this.$store.state.contractDataList;
      let content = "";
      let output;
      let input = {
        language: "Solidity",
        settings: {
          outputSelection: {
            "*": {
              "*": ["*"],
            },
          },
        },
      };
      input.sources = {};
      input.sources[this.contractName + ".sol"] = {};
      let libs = [];
      input.sources[this.contractName + ".sol"] = {
        content: this.content,
      };
      try {
        output = JSON.parse(
          solc.compile(JSON.stringify(input), { import: this.findImports })
        );
      } catch (error) {
        this.errorInfo = this.$t("text.compilationFailed");
        this.errorMessage = error;
        this.compileShow = true;
        this.loadingAce = false;
      }
      setTimeout(() => {
        if (output && JSON.stringify(output.contracts) != "{}") {
          this.status = 1;
          if (output.contracts[this.contractName + ".sol"]) {
            this.changeOutput(
              output.contracts[this.contractName + ".sol"],
              callback
            );
          }
        } else {
          this.errorMessage = output.errors;
          this.errorMessage.forEach((item) => {
            // item.open = false;
          });
          this.errorInfo = this.$t("text.compilationFailed");
          this.loadingAce = false;
        }
      }, 500);
    },
    changeOutput: function (obj, callback) {
      if (JSON.stringify(obj) !== "{}") {
        if (obj.hasOwnProperty(this.contractName)) {
          let compiledMap = obj[this.contractName];
          this.abiFileShow = true;
          this.successInfo = `< ${this.$t("text.compilationSucceeded")}`;
          this.abiFile = compiledMap.abi;
          this.abiFile = JSON.stringify(this.abiFile);
          if (!(!this.abiFile || this.abiFile == "[]")) {
            this.abiEmpty = false;
          }
          this.bin = compiledMap.evm.deployedBytecode.object;
          this.bytecodeBin = compiledMap.evm.bytecode.object;
          this.data.contractAbi = this.abiFile;
          this.data.contractBin = this.bin;
          this.data.contractSource = Base64.encode(this.content);
          this.$set(this.data, "bytecodeBin", this.bytecodeBin);
          // this.$emit("compile", false)
          this.loadingAce = false;
          if (callback && !callback.type) {
            callback();
          }
          Bus.$emit("compile", this.data);
          this.setMethod();
        } else {
          this.$message({
            type: "error",
            message: this.$t("text.contractFileName"),
          });
          this.errorInfo = this.$t("text.compilationFailed");
          this.compileShow = true;
          this.loadingAce = false;
        }
      } else {
        this.errorInfo = this.$t("text.compilationFailed");
        this.compileShow = true;
        this.loadingAce = false;
      }
    },
    refreshMessage: function () {
      this.abiFileShow = false;
      this.errorInfo = "";
      this.compileinfo = "";
      this.abiFile = "";
      this.abiEmpty = true;
      this.contractAddress = "";
      this.bin = "";
    },
    deploying: function () {
      if (!this.bytecodeBin) {
        this.$message({
          type: "warning",
          message: this.$t("text.notHaveBin"),
          duration: 2000,
        });
        return;
      }
      if (JSON.parse(this.abiFile).length == 0 || !this.abiFile) {
        this.$message({
          type: "error",
          message: this.$t("text.haveAbi"),
        });
      } else {
        if (this.abiFile) {
          this.compile(this.deploy);
        } else {
          this.$message.error(`${this.$t("text.compilationFailed")}`);
        }
      }
    },
    deploy: function () {
      if (this.abiFile) {
        this.dialogUser = true;
      } else {
        this.$message.error(`${this.$t("text.compilationFailed")}`);
      }
    },
    userClose: function () {
      this.dialogUser = false;
    },
    setMethod: function () {
      let Web3EthAbi = web3;
      let arry = [];
      if (this.abiFile) {
        let list = JSON.parse(this.abiFile);
        list.forEach((value) => {
          if (value.name && value.type == "function") {
            let data = {};
            let methodId;
            if (localStorage.getItem("encryptionId") == 1) {
              methodId = Web3EthAbi.smEncodeFunctionSignature({
                name: value.name,
                type: value.type,
                inputs: value.inputs,
              });
            } else {
              methodId = Web3EthAbi.encodeFunctionSignature({
                name: value.name,
                type: value.type,
                inputs: value.inputs,
              });
            }
            data.methodId = methodId.substr(0, 10);
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
                inputs: value.inputs,
              });
            } else {
              methodId = Web3EthAbi.encodeEventSignature({
                name: value.name,
                type: value.type,
                inputs: value.inputs,
              });
            }
            data.methodId = methodId.substr(0, 10);
            data.abiInfo = JSON.stringify(value);
            data.methodType = value.type;
            arry.push(data);
          }
        });
        if (arry.length) {
          this.addAbiMethod(arry);
        }
      }
    },
    addAbiMethod: function (list) {
      let data = {
        groupId: localStorage.getItem("groupId"),
        methodHandleList: list,
      };
      addFunctionAbi(data)
        .then((res) => {
          if (res.data.code === 0) {
            console.log("method 保存成功！");
          } else {
            this.$message({
              message: this.$chooseLang(res.data.code),
              type: "error",
            });
          }
        })
        .catch((err) => {
          message(constant.ERROR, "error");
        });
    },
    deployContract($event) {
      var val = $event[0],
        cns = $event[1];
      this.loadingAce = true;
      let reqData = {
        groupId: localStorage.getItem("groupId"),
        contractBin: this.bin,
        bytecodeBin: this.bytecodeBin,
        abiInfo: JSON.parse(this.abiFile),
        contractSource: Base64.encode(this.content),
        user: val.userId,
        contractName: this.contractName,
        contractId: this.data.id,
        contractPath: this.data.contractPath,
        useAes: false,
      };
      this.version = val.version;
      if (val.params.length) {
        reqData.funcParam = val.params;
      }
      getDeployStatus(reqData).then((res) => {
        this.loadingAce = false;
        const { data, status } = res;
        if (status === 200) {
          this.abiFileShow = true;
          this.status = 2;
          this.contractAddress = data;
          this.successInfo = `< ${this.$t("text.deploySucceeded")}`;
          this.$message({
            message: this.$t("text.deploySucceeded"),
            type: "success",
          });
          this.data.contractSource = Base64.encode(this.content);
          this.data.contractAddress = this.contractAddress;
          this.data.contractVersion = this.version;
          if (cns.saveEnabled) {
            this.queryRegisterCns(val, cns);
          }
          Bus.$emit("deploy", this.data);
        } else {
          this.status = 3;
          this.$message({
            message: this.$chooseLang(res.data.code),
            type: "error",
          });
        }
      });
    },
    queryRegisterCns(val, cns) {
      let param = {
        groupId: localStorage.getItem("groupId"),
        contractName: this.contractName,
        version: cns.version,
        abiInfo: JSON.parse(this.abiFile),
        userAddress: val.userId,
        saveEnabled: true,
        contractAddress: this.contractAddress,
        cnsName: cns.cnsName,
        contractPath: this.data.contractPath,
      };
      registerCns(param).then((res) => {
        const { data, status } = res;
        if (status === 200) {
          this.queryFindCnsInfo();
        } else {
          this.$message({
            message: this.$chooseLang(res.data.code),
            type: "error",
          });
        }
      });
    },
    foldInfo: function (val) {
      this.successHide = val;
    },
    send: function () {
      if (JSON.parse(this.abiFile).length == 0 || !this.abiFile) {
        this.$message({
          type: "error",
          message: this.$t("text.haveAbi"),
        });
      } else {
        ifChangedDepaloy(localStorage.getItem("groupId"), this.data.id).then(
          (res) => {
            const { data, status } = res;
            if (status === 200) {
              if (data) {
                this.sendErrorMessage = this.$t("text.redeploy");
                this.dialogVisible = true;
              } else {
                this.dialogVisible = true;
                this.sendErrorMessage = "";
              }
            }
          }
        );
      }
    },
    handleClose: function () {
      this.dialogVisible = false;
    },
    sendClose: function () {
      this.$refs.send.close();
    },

    downloadJavaClass: function (formName) {
      if (!this.abiFile || this.abiFile == "[]") {
        this.$message({
          type: "warning",
          message: this.$t("text.notHaveAbi"),
          duration: 2000,
        });
        return;
      }
      this.javaClassDialogVisible = true;
    },
    closeJavaClass: function () {
      this.javaClassDialogVisible = false;
      this.initJavaClass();
    },
    initJavaClass: function () {
      this.javaClassName = "";
    },
    sureJavaClass: function () {
      this.$confirm(`${this.$t("dialog.sureExport")}？`, {
        center: true,
        dangerouslyUseHTMLString: true,
      })
        .then(() => {
          this.getJavaClass();
        })
        .catch(() => {});
    },
    getJavaClass: function () {
      if (!this.abiFile) {
        this.$message({
          type: "warning",
          message: this.$t("text.notHaveAbi"),
          duration: 2000,
        });
        return;
      }
      let reqData = {
        contractName: this.contractName,
        abiInfo: JSON.parse(this.abiFile),
        contractBin: this.bytecodeBin,
        packageName: this.javaClassName,
      };
      queryJavaClass(reqData)
        .then((res) => {
          const { data, status } = res;
          if (status === 200) {
            const content = data;
            const blob = new Blob([content]);
            const fileName = this.contractName
              ? `${this.contractName}.java`
              : `undefined.java`;
            if ("download" in document.createElement("a")) {
              // 非IE下载
              const elink = document.createElement("a");
              elink.download = fileName;
              elink.style.display = "none";
              elink.href = URL.createObjectURL(blob);
              document.body.appendChild(elink);
              elink.click();
              URL.revokeObjectURL(elink.href); // 释放URL 对象
              document.body.removeChild(elink);
            } else {
              // IE10+下载
              navigator.msSaveBlob(blob, fileName);
            }
            this.closeJavaClass();
          } else {
            this.closeJavaClass();
            this.$message({
              message: this.$chooseLang(res.data.code),
              type: "error",
            });
          }
        })
        .catch((err) => {
          this.closeJavaClass();
          this.$message({
            message: err.data || this.$t("text.systemError"),
            type: "error",
          });
        });
    },
    showAbiText() {
      this.showAbi = !this.showAbi;
      if (this.$refs["showAbiText"].style.overflow === "visible") {
        this.$refs["showAbiText"].style.overflow = "hidden";
      } else if (
        this.$refs["showAbiText"].style.overflow === "" ||
        this.$refs["showAbiText"].style.overflow === "hidden"
      ) {
        this.$refs["showAbiText"].style.overflow = "visible";
      }
    },
    showBinText() {
      this.showBin = !this.showBin;
      if (this.$refs["showBinText"].style.overflow === "visible") {
        this.$refs["showBinText"].style.overflow = "hidden";
      } else if (
        this.$refs["showBinText"].style.overflow === "" ||
        this.$refs["showBinText"].style.overflow === "hidden"
      ) {
        this.$refs["showBinText"].style.overflow = "visible";
      }
    },
    collapse() {
      this.showCompileText = !this.showCompileText;
      if (this.showCompileText) {
        this.infoHeight = 250;
      } else if (!this.showCompileText) {
        this.infoHeight = 50;
      }
      this.$nextTick(() => {
        this.resizeCode();
      });
    },
    searchKeyword() {
      this.searchVisibility = true;
      this.$nextTick(() => {
        this.$refs["searchInput"].$refs.input.focus();
      });
    },
    searchBtn() {
      this.aceEditor.findNext();
    },
    previousBtn() {
      this.aceEditor.findPrevious();
    },
    nextBtn() {
      this.aceEditor.findNext();
    },
    closeBtn() {
      this.searchVisibility = false;
      this.keyword = "";
    },
    copyKey(val) {
      if (!val) {
        this.$message({
          type: "fail",
          showClose: true,
          message: this.$t("notice.copyFailure"),
          duration: 2000,
        });
      } else {
        this.$copyText(val).then((e) => {
          this.$message({
            type: "success",
            showClose: true,
            message: this.$t("notice.copySuccessfully"),
            duration: 2000,
          });
        });
      }
    },
    optenErrorInfo(item, index) {
      console.log(index, this.errorMessage);
      this.errorMessage.forEach((err, it) => {
        if (err.open !== this.errorMessage[index]["open"]) {
          err.open = false;
        }
      });
      item.open = !item.open;
    },
    severityColor(item) {
      let key = item.severity;
      switch (key) {
        case "error":
          return "#F56C6C";
          break;

        case "warning":
          return "#E6A23C";
          break;
      }
    },
    queryFindCnsInfo() {
      let param = {
        groupId: localStorage.getItem("groupId"),
        contractAddress: this.data.contractAddress,
      };
      findCnsInfo(param).then((res) => {
        const { data, status } = res;
        if (status === 200) {
          if (data.data) {
            this.reqVersion = data.data.version;
            this.cnsName = data.data.cnsName;
          } else {
            this.reqVersion = "";
            this.cnsName = "";
          }
        } else {
          this.$message({
            type: "error",
            message: this.$chooseLang(res.data.code),
          });
        }
      });
    },
    handleRegisterCns(val) {
      this.mgmtCnsItem = this.data;
      this.mgmtCnsVisible = true;
    },
    mgmtCnsResultSuccess() {
      this.queryFindCnsInfo();
      this.mgmtCnsVisible = false;
    },
    mgmtCnsResultClose() {
      this.mgmtCnsVisible = false;
    },
    handleChange(val) {
      console.log(val);
    },
    exportSdk() {
      this.sureExportSdk();
    },
    sureExportSdk() {
      exportCertSdk().then((res) => {
        const { status } = res;
        if (status === 200) {
          const blob = new Blob([res.data]);
          const fileName = `sdk.zip`;
          if ("download" in document.createElement("a")) {
            const elink = document.createElement("a");
            elink.download = fileName;
            elink.style.display = "none";
            elink.href = URL.createObjectURL(blob);
            document.body.appendChild(elink);
            elink.click();
            URL.revokeObjectURL(elink.href);
            document.body.removeChild(elink);
          } else {
            navigator.msSaveBlob(blob, fileName);
          }
        } else {
          this.$message({
            type: "error",
            message: this.$t("text.haveCertSdk"),
          });
        }
      });
    },
    // 导出java项目
    exportJava() {
      this.$store.dispatch("set_exportProject_show_action", true);
    },
    addContractAddress() {
      this.contractForm.contractAddress = "";
      this.addContractAddressVisible = true;
    },
    closeContractAddress() {
      this.addContractAddressVisible = false;
      this.contractForm.contractAddress = "";
    },
    sureContractAddress(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (
            this.contractForm.contractAddress == "" ||
            this.contractForm.contractAddress == null
          ) {
            this.$message({
              type: "error",
              message: this.$t("contracts.contractAddressInput"),
            });
          } else {
            let web3Utils = require("web3-utils");
            if (web3Utils.isAddress(this.contractForm.contractAddress)) {
              this.addContractAddressVisible = false;
              this.addContract();
            } else {
              this.$message({
                type: "error",
                message: this.$t("contracts.contractAddressInput"),
              });
            }
          }
        } else {
          return false;
        }
      });
    },
    addContract: function () {
      let reqData = {
        groupId: localStorage.getItem("groupId"),
        contractName: this.data.contractName,
        contractPath: this.data.contractPath,
        contractSource: this.data.contractSource,
        contractAbi: this.data.contractAbi,
        contractBin: this.data.contractBin,
        bytecodeBin: this.data.bytecodeBin,
        contractAddress: this.contractForm.contractAddress,
      };
      if (this.data.id) {
        reqData.id = this.data.id;
      }
      Bus.$emit("save", reqData);
    },
  },
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
  min-width: 960px;
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
  min-width:960px
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
  overflow: auto;
  padding-left: 5px;
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
.error-item >>> .el-collapse {
  border-bottom: 1px solid #2b374d;
  border-top: 1px solid #2b374d;
}
.error-item >>> .el-collapse-item__header {
  color: inherit;
  background-color: inherit;
  height: inherit;
  line-height: inherit;
  border-bottom: 1px solid #2b374d;
  font-size: 12px;
  font-weight: none;
}
.error-item >>> .el-collapse-item__content {
  background-color: #2b374d;
}
.error-item >>> .el-collapse-item__wrap {
  border-bottom: 1px solid #2b374d;
}
</style>
