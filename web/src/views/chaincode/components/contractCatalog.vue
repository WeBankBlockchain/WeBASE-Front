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
  <div
    class="contract-menu"
    style="position: relative; height: 100%"
    v-loading="loading"
  >
    <div class="contract-menu-header noBlurs">
      <el-tooltip
        class="item"
        effect="dark"
        :content="$t('title.newFile')"
        placement="top-start"
      >
        <i class="wbs-icon-Addfile icon contract-icon" @click="addFile"></i>
      </el-tooltip>
      <el-tooltip
        class="item"
        effect="dark"
        :content="$t('title.newFolder')"
        placement="top-start"
      >
        <i class="wbs-icon-Addfolder icon contract-icon" @click="addFolder"></i>
      </el-tooltip>
      <el-tooltip
        class="item"
        effect="dark"
        :content="$t('title.upload')"
        placement="top-start"
      >
        <i class="wbs-icon-shangchuan contract-icon" style="position: relative">
          <input
            multiple
            type="file"
            id="file"
            ref="file"
            name="chaincodes"
            class="uploads"
            @change="upload($event)"
          />
        </i>
      </el-tooltip>
      <!-- <el-button type="text" @click="fromGitHub">GitHub</el-button> -->
      <div>
        <slot name="footer"></slot>
      </div>
    </div>

    <div class="contract-menu-content">
      <ul>
        <li v-for="item in contractArry" :key="item.id">
          <div
            v-if="item.contractType == 'file'"
            class="contract-file"
            :id="item.id"
            :style="{ 'padding-left': item.modifyState ? `${10}px` : '' }"
          >
            <div
              class="ellipsis-info"
              :class="{ colorActive: item.contractActive }"
            >
              <i class="wbs-icon-radio font-6" v-if="item.modifyState"></i>
              <i
                class="wbs-icon-file"
                @contextmenu.prevent="handle($event, item)"
                @click="select(item)"
                v-if="!item.renameShow"
                :id="item.id"
              ></i>
              <span
                @contextmenu.prevent="handle($event, item)"
                @click="select(item)"
                :id="item.id"
                v-if="!item.renameShow"
                >{{ item.contractName }}</span
              >
            </div>
            <el-input
              v-model="contractName"
              v-focus
              ref="user"
              maxlength="32"
              @blur="changeName(item)"
              v-if="item.renameShow"
            ></el-input>
            <div
              class="contract-menu-handle"
              v-if="item.handleModel"
              :style="{ top: `${clentY}px`, left: `${clentX}px` }"
              v-Clickoutside="checkNull"
            >
              <ul v-if="contractFile">
                <li class="contract-menu-handle-list" @click="rename">
                  {{ $t("dialog.rename") }}
                </li>
                <li class="contract-menu-handle-list" @click="deleteFile(item)">
                  {{ $t("dialog.delete") }}
                </li>
                <li class="contract-menu-handle-list" @click="exportFile(item)">
                  {{ $t("dialog.exportSol") }}
                </li>
              </ul>
            </div>
          </div>
          <div
            v-if="item.contractType == 'folder'"
            class="contract-folder"
            :id="item.folderId"
          >
            <i
              :class="item.folderIcon"
              @click="open(item)"
              v-if="!item.renameShow"
              :id="item.folderId"
              class="cursor-pointer font-16 no-chase"
            ></i>
            <i
              class="wbs-icon-folder cursor-pointer no-chase"
              @click="open(item)"
              @contextmenu.prevent="handle($event, item)"
              v-if="!item.renameShow"
              style="color: #d19650"
              :id="item.folderId"
            ></i>
            <span
              @click="open(item)"
              @contextmenu.prevent="handle($event, item)"
              :id="item.folderId"
              v-if="!item.renameShow"
              :class="{ colorActive: item.contractActive }"
              class="no-chase cursor-pointer"
              >{{ item.contractName }}</span
            >
            <div
              class="contract-menu-handle"
              v-if="item.handleModel"
              :style="{ top: `${clentY}px`, left: `${clentX}px` }"
              v-Clickoutside="checkNull"
            >
              <ul>
                <li
                  class="contract-menu-handle-list"
                  v-if="item.contractName !== 'template'"
                  @click="addFiles(item)"
                >
                  {{ $t("dialog.newFile") }}
                </li>
                <li
                  class="contract-menu-handle-list"
                  v-if="item.contractName !== 'template'"
                  @click="deleteFolder(item)"
                >
                  {{ $t("dialog.delete") }}
                </li>
                <li
                  class="contract-menu-handle-list"
                  @click="exportFolder(item)"
                >
                  {{ $t("dialog.exportSol") }}
                </li>
              </ul>
            </div>
            <br />
            <ul v-if="item.folderActive" style="padding-left: 20px">
              <li
                class="contract-file"
                v-for="list in item.child"
                :key="list.id"
                :style="{ 'padding-left': list.modifyState ? `${10}px` : '' }"
              >
                <div
                  class="ellipsis-info"
                  :class="{ colorActive: list.contractActive }"
                >
                  <i class="wbs-icon-radio font-6" v-if="list.modifyState"></i>
                  <i
                    class="wbs-icon-file"
                    v-if="!list.renameShow"
                    @click="select(list)"
                    @contextmenu.prevent="handle($event, list)"
                    :id="list.id"
                  ></i>
                  <span
                    @click="select(list)"
                    @contextmenu.prevent="handle($event, list)"
                    :id="list.id"
                    v-if="!list.renameShow"
                    >{{ list.contractName }}</span
                  >
                </div>

                <el-input
                  v-model="contractName"
                  v-focus
                  maxlength="32"
                  @blur="changeName(list)"
                  v-if="list.renameShow"
                ></el-input>
                <div
                  class="contract-menu-handle"
                  v-if="list.handleModel"
                  :style="{ top: `${clentY}px`, left: `${clentX}px` }"
                  v-Clickoutside="checkNull"
                >
                  <ul v-if="contractFile && item.contractName !== 'template'">
                    <li class="contract-menu-handle-list" @click="rename">
                      {{ $t("dialog.rename") }}
                    </li>
                    <li
                      class="contract-menu-handle-list"
                      @click="deleteFile(list)"
                    >
                      {{ $t("dialog.delete") }}
                    </li>
                    <li
                      class="contract-menu-handle-list"
                      @click="exportFile(list)"
                    >
                      {{ $t("dialog.exportSol") }}
                    </li>
                  </ul>
                </div>
              </li>
            </ul>
          </div>
        </li>
      </ul>
    </div>
    <add-folder
      v-if="foldershow"
      :foldershow="foldershow"
      @close="folderClose"
      @success="folderSuccess"
    ></add-folder>
    <add-file
      v-if="fileshow"
      :data="selectFolderData"
      :fileshow="fileshow"
      @close="fileClose"
      @success="fileSucccess($event)"
      :id="folderId"
    ></add-file>
    <select-catalog
      v-if="cataLogShow"
      :show="cataLogShow"
      @success="catalogSuccess($event)"
      @close="catalogClose"
    ></select-catalog>
    <export-project
      v-if="$store.state.exportProjectShow"
      :show="$store.state.exportProjectShow"
      :folderList="pathList"
      @close="exportProjectShowClose"
    ></export-project>
    <el-dialog
      v-if="importFromDialog"
      :title="$t('contracts.importContractTitle')"
      :visible.sync="importFromDialog"
      width="470px"
      center
      class="send-dialog"
    >
      <import-from
        @modelClose="modelClose"
        @exportSuccessed="exportSuccessed"
      ></import-from>
    </el-dialog>
  </div>
</template>
<script>
import addFolder from "../dialog/addFolder";
import addFile from "../dialog/addFile";
import selectCatalog from "../dialog/selectCatalog";
import exportProject from "../dialog/exportProject";
import importFrom from "../dialog/importFrom";
import {
  searchContract,
  getContractPathList,
  saveChaincode,
  deleteCode,
  solcList,
  solcUpload,
  solcDownload,
  deleteSolcId,
  readSolcVersion,
  deletePath,
} from "@/util/api";
import Bus from "@/bus";
import Clickoutside from "element-ui/src/utils/clickoutside";
let Base64 = require("js-base64").Base64;
const FileSaver = require("file-saver");
import JSZip from "jszip";
export default {
  name: "contractCatalog",
  props: {
    solcVersionOptions: {
      type: Array,
    },
    solcVersion: {
      type: String,
    },
  },
  components: {
    "add-folder": addFolder,
    "add-file": addFile,
    "select-catalog": selectCatalog,
    exportProject,
    importFrom,
  },
  data() {
    return {
      foldershow: false,
      fileshow: false,
      filename: "",
      fileString: "",
      contractList: [],
      folderList: [],
      contractArry: [],
      contractData: null,
      cataLogShow: false,
      realContractList: [],
      contractName: "",
      clentX: 0,
      clentY: 0,
      contractFile: false,
      contractFolder: false,
      ID: "",
      handleModel: false,
      folderId: null,
      modifyState: false,
      modifyParam: {},
      uploadFiles: [],
      version: "",
      versionOptions: [],
      pathList: [],
      folderData: null,
      selectFolderData: null,
      loading: false,
      importFromDialog: false,
    };
  },
  watch: {
    solcVersionOptions(val) {
      this.versionOptions = val;
    },
    solcVersion(val) {
      this.version = this.solcVersion;
    },
  },
  create() {
    Bus.$off("compile");
    Bus.$off("deploy");
    Bus.$off("open");
    Bus.$off("save");
    Bus.$off("modifyState");
  },
  mounted() {
    this.$nextTick(function () {
      if (localStorage.getItem("groupId")) {
        this.getContractPaths();
      }
    });
    Bus.$on("compile", (data) => {
      this.saveContract(data, `${this.$t("text.compilationSucceeded")}`);
    });
    Bus.$on("save", (data) => {
      this.saveContract(data);
    });
    Bus.$on("deploy", (data) => {
      this.getContracts("", data);
    });
    Bus.$on("open", (data) => {
      this.contractArry.forEach((value) => {
        if (value.contractName == data.contractPath && !value.folderActive) {
          this.$set(value, "folderActive", true);
          this.$set(value, "folderIcon", "el-icon-caret-bottom");
        }
      });
      this.select(data);
    });
    Bus.$on("modifyState", (data) => {
      this.contractList.forEach((value) => {
        if (value.id === data.id && data.modifyState) {
          console.log(this);
          this.modifyState = data.modifyState;
          this.modifyParam = data;
          this.$set(value, "modifyState", true);
        } else {
          this.$set(value, "modifyState", false);
        }
      });
    });
     document.querySelector(".noBlurs").onmousedown = function (e) {
      console.log(1);
      Bus.$emit("limit", false);
      e.preventDefault();
    };
  
  },
  directives: {
    Clickoutside,
    focus: {
      inserted: function (el, { value }) {
        let dom = el.getElementsByClassName("el-input__inner")[0];
        dom.focus();
      },
    },
  },
  methods: {
    checkNull(list) {
      this.contractArry.forEach((value) => {
        value.handleModel = false;
        if (value.contractType == "folder") {
          value.child.forEach((list) => {
            list.handleModel = false;
          });
        }
      });
      this.ID = "";
      this.contractFile = false;
      this.contractFolder = false;
      this.handleModel = false;
    },
    handle(e, list) {
      this.checkNull();
      list.handleModel = true;
      if (e.clientX > 201) {
        this.clentX = e.clientX - 200;
      } else {
        this.clentX = e.clientX;
      }
      this.clentX = e.clientX;
      this.clentY = e.clientY;
      this.ID = e.target.id;
      let item = {};
      if (this.ID) {
        this.handleModel = true;
        this.contractArry.forEach((value) => {
          if (
            (value.id && value.id == this.ID) ||
            (value.folderId && value.folderId == this.ID)
          ) {
            item = value;
          } else if (value.contractType == "folder") {
            value.child.forEach((list) => {
              if (list.id == this.ID) {
                item = list;
              }
            });
          }
        });
        if (item.contractType == "file") {
          this.contractFile = true;
          this.contractFolder = false;
        } else {
          this.contractFile = false;
          // this.contractTotal = false;
          this.contractFolder = true;
        }
      } else {
        this.ID = "";
        this.contractFile = false;
        this.contractFolder = false;
        this.handleModel = false;
      }
    },
    rename(val) {
      this.contractArry.forEach((value) => {
        value.handleModel = false;
        if (value.contractType == "folder") {
          value.child.forEach((list) => {
            list.handleModel = false;
          });
        }
      });
      // this.$refs.user.focus()
      this.contractArry.forEach((value) => {
        if (value.id == this.ID) {
          this.$set(value, "renameShow", true);
          this.contractName = value.contractName;
        } else if (
          value.contractType == "folder" &&
          value.folderId !== this.ID
        ) {
          value.child.forEach((item) => {
            if (item.id == this.ID) {
              this.$set(item, "renameShow", true);
              this.contractName = item.contractName;
            } else if (item.id == this.ID) {
              this.$set(item, "renameShow", false);
            } else {
              this.$set(item, "renameShow", false);
            }
          });
        } else if (
          value.contractType == "folder" &&
          value.folderId == this.ID
        ) {
          let num = 0;
          if (num == 0) {
            this.$set(value, "renameShow", true);
            this.contractName = value.contractName;
          }
        } else if (value.id == this.ID) {
          this.$set(value, "renameShow", false);
        } else {
          this.$set(value, "renameShow", false);
        }
      });

      this.contractFile = false;
      this.contractFolder = false;
      this.handleModel = false;
    },
    changeName(val) {
      let pattern = /^[A-Za-z0-9_]+$/;
      if (
        pattern.test(this.contractName) &&
        this.contractName.length < 32 &&
        this.contractName.length > 1
      ) {
        if (this.contractName !== val.contractName) {
          for (let i = 0; i < this.contractList.length; i++) {
            if (
              this.contractList[i].contractName == this.contractName &&
              this.contractList[i].contractPath == val.contractPath
            ) {
              this.$message({
                message: "同目录下存在相同的合约，请重新命名",
                type: "error",
              });
              this.$set(val, "renameShow", false);
              return;
            }
          }
          if (this.contractName) {
            this.$set(val, "contractName", this.contractName);
            this.contractName = "";
            this.saveContract(val);
          } else {
            this.$set(val, "renameShow", false);
          }
        } else {
          this.$set(val, "renameShow", false);
        }
      } else {
        this.$message({
          message: "请输入数字或字母,长度为1到32位！",
          type: "error",
        });
        this.$set(val, "renameShow", false);
      }
    },
    addFolder() {
      this.checkNull();
      this.foldershow = true;
    },
    addFile() {
      this.checkNull();
      this.fileshow = true;
    },
    addFiles(val) {
      this.selectFolderData = val;
      this.fileshow = true;
      this.folderId = this.ID;
      this.ID = "";
      this.contractFile = false;
      this.contractFolder = false;
      this.handleModel = false;
    },
    upload(e) {
      this.checkNull();
      if (!e.target.files.length) {
        return;
      }
      this.uploadFiles = e.target.files;

      for (let i = 0; i < this.uploadFiles.length; i++) {
        let filessize = Math.ceil(this.uploadFiles[i].size / 1024);
        let files = this.uploadFiles[i].name.split(".");
        let filetype = files[files.length - 1];
        if (filessize > 400) {
          this.$message({
            message: this.$t("text.fileExceeds"),
            type: "error",
          });
          this.cataLogShow = false;
          break;
        } else if (filetype !== "sol") {
          this.$message({
            message: this.$t("text.uploadSol"),
            type: "error",
          });
          this.cataLogShow = false;
          break;
        } else {
          this.cataLogShow = true;
        }
      }
    },
    catalogSuccess(val) {
      let len = this.uploadFiles.length;
      for (let i = 0; i < this.uploadFiles.length; i++) {
        let reader = new FileReader(); //新建一个FileReader
        reader.readAsText(this.uploadFiles[i], "UTF-8"); //读取文件
        let filename = "",
          _this = this;
        filename = this.uploadFiles[i].name.slice(
          0,
          this.uploadFiles[i].name.length - 4
        );
        let num = 0;
        this.contractList.forEach((value) => {
          if (
            value.contractName == filename &&
            value.contractPath == val &&
            num === 0
          ) {
            this.$message({
              type: "error",
              message: this.$t("text.contractSameDirectory"),
            });
            num++;
          }
        });
        if (!num) {
          reader.onload = function (evt) {
            var fileString = "";
            fileString = Base64.encode(evt.target.result); // 读取文件内容
            let data = {
              contractName: filename,
              contractSource: fileString,
              contractPath: val,
              contractType: "file",
              contractActive: false,
              contractstatus: 0,
              contractAbi: "",
              contractBin: "",
              contractAddress: "",
              contractVersion: "",
              contractNo: new Date().getTime(),
            };
            if (i == len - 1) {
              _this.saveOneContract(data, true);
            } else {
              _this.saveOneContract(data);
            }
          };
        }
      }
      // this.$refs.file.value = "";
      this.catalogClose();
    },
    /**
     * @method 同步保存合约  （upload合约专用）
     * @param param  合约内容
     * @param type  为true执行查询合约列表请求
     */
    async saveOneContract(param, type) {
      let reqData = {
        groupId: localStorage.getItem("groupId"),
        contractName: param.contractName,
        contractPath: param.contractPath,
        contractSource: param.contractSource,
        contractAbi: param.contractAbi,
        contractBin: param.contractBin,
        bytecodeBin: param.bytecodeBin,
      };
      if (param.id) {
        reqData.contractId = param.id;
      }
      await saveChaincode(reqData)
        .then((res) => {
          if (res.status === 200) {
            setTimeout(() => {
              this.getContractPaths();
            }, 200);
            try {
              if (type) {
                this.$refs.file.value = "";
                this.getContracts(param.contractPath, res.data);
              }
              if (param.contractId) {
                this.$message({
                  type: "success",
                  message: title || this.$t("contracts.contractSaveSuccess"),
                });
              }
            } catch (error) {
              console.log(error);
            }
          } else {
            this.$message({
              message: this.$chooseLang(res.data.code),
              type: "error",
              duration: 2000,
            });
          }
        })
        .catch((err) => {
          this.$message({
            message: err.data || this.$t("text.systemError"),
            type: "error",
            duration: 2000,
          });
        });
    },
    catalogClose() {
      this.$refs.file.value = "";
      this.cataLogShow = false;
    },
    folderClose() {
      this.foldershow = false;
    },
    folderSuccess() {
      this.folderClose();

      this.getContractPaths();
      // this.getContractArry();
    },
    fileClose() {
      this.fileshow = false;
      this.folderId = "";
      this.ID = "";
    },
    getContractArry(val) {
      let result = [];
      let list = [];
      let folderArry = this.createFolder(val);
      let newFileList = [];
      list = this.contractList || [];
      list.forEach((value) => {
        this.$set(value, "handleModel", false);
        if (value.contractPath == "/") {
          newFileList.push(value);
        }
      });
      folderArry.forEach((value) => {
        this.$set(value, "handleModel", false);

        value.child = [];
        list.forEach((item, index) => {
          if (item.contractPath == value.contractName) {
            value.child.push(item);
          }
        });
      });
      result = newFileList.concat(folderArry);
      this.contractArry = result;
      if (this.contractList.length && !val) {
        this.select(this.contractList[0]);
      } else if (val && val.contractType == "folder") {
        this.select(val, "title");
        // return
        // let num = 0
        // for (let i = 0; i < this.contractArry.length; i++) {
        //     if (val.contractName === this.contractArry[i].contractName) {
        //         if (this.contractArry[i].child.length) {
        //             this.select(this.contractArry[i].child[0]);
        //             num++;
        //         }
        //     }
        // }
        // if (num == 0) {
        //     let index = 0;
        //     for (let i = 0; i < this.contractList.length; i++) {
        //         if (this.contractList[i] && this.contractList[i].contractPath == "/") {
        //             this.select(this.contractList[i]);
        //             index++;
        //             console.log("*")
        //             continue;
        //         }
        //     }
        //     if (index == 0) {
        //         this.select(this.contractList[0]);
        //     }
        // }
      } else if (
        val &&
        val.contractType !== "folder" &&
        this.contractList.length
      ) {
        this.select(val);
      } else {
        Bus.$emit("noData", true);
      }
    },
    saveContract(param, title, callback) {
      let reqData = {
        groupId: localStorage.getItem("groupId"),
        contractName: param.contractName,
        contractPath: param.contractPath,
        contractSource: param.contractSource,
        contractAbi: param.contractAbi,
        contractBin: param.contractBin,
        bytecodeBin: param.bytecodeBin,
      };
      if (param.id) {
        reqData.contractId = param.id;
      }
      if (param.contractAddress) {
        reqData.contractAddress = param.contractAddress;
      }

      saveChaincode(reqData)
        .then((res) => {
          const { data, status } = res;
          if (status === 200) {
            if (param.id) {
              this.$message({
                type: "success",
                message: title || this.$t("text.saveSucceeded"),
              });
            }
            this.modifyState = false;
            this.getContracts(data.contractPath, data);
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
          });
        });
    },
    getContractPaths(val) {
      getContractPathList(localStorage.getItem("groupId"))
        .then((res) => {
          if (res.status == 200) {
            this.pathList = res.data;
            this.folderList = [];
            for (let i = 0; i < this.pathList.length; i++) {
              if (this.pathList[i].contractPath != "/") {
                let item = {
                  folderName: this.pathList[i].contractPath,
                  folderId:
                    new Date().getTime() + this.pathList[i].contractPath,
                  folderActive: false,
                  groupId: localStorage.getItem("groupId"),
                  modifyTime: this.pathList[i].modifyTime,
                };
                this.folderList.push(item);
              }
            }
            if (val) {
              this.getContracts(val);
            } else {
              this.getContracts();
            }
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
          });
        });
    },
    changeContractData(list1, list2) {
      let arry = [];
      let obj = {};
      let list3 = list1.concat(list2);
      let list = [];
      list3 = list3.reduce(function (item, next) {
        obj[next.id] ? "" : (obj[next.id] = true && item.push(next));
        return item;
      }, []);
      for (let i = 0; i < list3.length; i++) {
        if (list3[i].groupId == localStorage.getItem("groupId")) {
          list.push(list3[i]);
        }
      }
      for (let i = 0; i < list.length; i++) {
        for (let j = 0; j < list2.length; j++) {
          if (list[i].id === list2[j].id) {
            list[i].contractName = list2[j].contractName;
            list[i].contractPath = list2[j].contractPath;
            list[i].contractAddress = list2[j].contractAddress;
            list[i].contractSource = list2[j].contractSource;
            list[i].contractStatus = list2[j].contractStatus;
            list[i].contractAbi = list2[j].contractAbi;
            list[i].contractBin = list2[j].contractBin;
            list[i].bytecodeBin = list2[j].bytecodeBin;
            list[i].contractAddress = list2[j].contractAddress;
          }
        }
      }
      return list;
    },
    getContracts(path, list) {
      let data = {
        groupId: localStorage.getItem("groupId"),
        // pageNumber: 1,
        // pageSize: 500
      };
      if (path && this.$store.state.contractDataList.length > 0) {
        data.contractPathList = [path];
      } else if (path && this.$store.state.contractDataList.length == 0) {
        if (typeof path == "object") {
          path.push("/");
        } else {
          path = [path, "/"];
        }

        data.contractPathList = path;
      } else if (this.$route.query.contractPath) {
        if (this.$route.query.contractPath == "/") {
          data.contractPathList = [this.$route.query.contractPath];
        } else {
          data.contractPathList = [this.$route.query.contractPath, "/"];
        }
      } else if (sessionStorage.getItem("selectData")) {
        if (JSON.parse(sessionStorage.getItem("selectData"))) {
          if (
            JSON.parse(sessionStorage.getItem("selectData")).contractPath &&
            JSON.parse(sessionStorage.getItem("selectData")).contractPath == "/"
          ) {
            data.contractPathList = [
              JSON.parse(sessionStorage.getItem("selectData")).contractPath,
            ];
          } else if (
            JSON.parse(sessionStorage.getItem("selectData")).contractType ==
            "folder"
          ) {
            data.contractPathList = [
              JSON.parse(sessionStorage.getItem("selectData")).contractName,
              "/",
            ];
          } else {
            data.contractPathList = [
              JSON.parse(sessionStorage.getItem("selectData")).contractPath,
              "/",
            ];
          }
        }
      } else {
        data.contractPathList = ["/"];
      }
      this.loading = true;
      searchContract(data)
        .then((res) => {
          this.loading = false;
          const { data, status } = res;
          if (status === 200) {
            let contractList = data.data || [];
            let contractDataList = this.$store.state.contractDataList;
            this.contractList = this.changeContractData(
              contractDataList,
              contractList
            );
            this.$store.dispatch("set_contractDataList", this.contractList);
            localStorage.setItem(
              "contractList",
              JSON.stringify(this.contractList)
            );
            if (data.data.length) {
              let result = [];
              let array = [];
              let obj = {};
              this.contractList.forEach((value) => {
                this.$set(value, "contractType", "file");
                this.$set(value, "contractActive", false);
                this.$set(value, "renameShow", false);
                this.$set(value, "inputShow", false);
                this.$set(value, "modifyState", false);
              });
              console.log(this.modifyState);
              if (list) {
                this.getContractArry(list);
              } else if (this.$route.query.id) {
                let index = 0;
                this.contractList.forEach((value) => {
                  if (value.id == this.$route.query.id) {
                    index++;
                    this.getContractArry(value);
                  }
                });
                if (index == 0) {
                  this.getContractArry();
                }
              } else if (
                this.$route.query.contractPath &&
                !this.$route.query.contractId
              ) {
                if (res.data.data.length) {
                  this.getContractArry(res.data.data[0]);
                } else {
                  this.getContractArry();
                }
              } else if (
                sessionStorage.getItem("selectData") &&
                JSON.parse(sessionStorage.getItem("selectData")) &&
                JSON.parse(sessionStorage.getItem("selectData")).id
              ) {
                let num = 0;
                this.contractList.forEach((value) => {
                  if (
                    value.id ==
                    JSON.parse(sessionStorage.getItem("selectData")).id
                  ) {
                    num++;
                    this.getContractArry(value);
                  }
                });
                if (!num) {
                  this.getContractArry();
                }
              } else {
                this.getContractArry();
              }
            } else {
              if (list) {
                this.getContractArry(list);
              } else {
                this.getContractArry();
              }
            }
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
          });
        });
    },
    fileSucccess(val) {
      let num = 0;
      this.contractList.forEach((value) => {
        if (
          value.contractName == val.contractName &&
          value.contractPath == val.contractPath
        ) {
          this.$message({
            type: "error",
            message: this.$t("text.contractSameDirectory"),
          });
          num++;
        }
      });
      if (!num) {
        this.saveContract(val);
      }
      this.fileClose();
    },
    createFolder(val) {
      let result = [];
      this.folderList.forEach((value, index) => {
        let num = 0;
        let data = {
          contractName: value.folderName,
          folderId: value.folderId,
          contractActive: false,
          contractType: "folder",
          folderIcon: "el-icon-caret-right",
          folderActive: false,
          renameShow: false,
          inputShow: false,
        };
        this.contractArry.forEach((item, index) => {
          if (
            item.contractType == "folder" &&
            item.contractName == data.contractName
          ) {
            data.folderIcon = item.folderIcon;
            data.folderActive = item.folderActive;
            // data.contractActive = true;
            this.$set(data, "contractActive", data.contractActive);
          }
        });
        if (val && val.contractPath && val.contractPath != "/") {
          if (data.contractName == val.contractPath) {
            data.folderIcon = "el-icon-caret-bottom";
            data.folderActive = true;
            data.contractActive = true;
          }
        }
        result.push(data);
      });
      return result;
    },
    open(val) {
      sessionStorage.setItem("selectData", "");
      if (val.contractName != "/" && val.contractPath != "/") {
        this.getContracts(val.contractName, val);
      }
      this.contractArry.forEach((value) => {
        this.$set(value, "contractActive", false);
        if (value.contractType == "folder") {
          value.child.forEach((item) => {
            this.$set(item, "contractActive", false);
          });
        }
        if (
          value.contractName === val.contractName &&
          value.contractType == "folder"
        ) {
          this.$set(value, "contractActive", true);
        }
      });
      if (val.folderActive) {
        this.$set(val, "folderActive", false);
        this.$set(val, "folderIcon", "el-icon-caret-right");
      } else {
        this.$set(val, "folderActive", true);
        this.$set(val, "folderIcon", "el-icon-caret-bottom");
      }
      this.$set(val, "contractActive", true);
      this.folderData = val;
    },
    sureSelect(val, type) {
      let num = 0;
      this.contractArry.forEach((value) => {
        if (val && val.id && value.id === val.id) {
          this.$set(value, "contractActive", true);
        } else if (value.contractType == "folder") {
          if (
            this.folderData &&
            this.folderData.contractName === value.contractName
          ) {
            this.$set(value, "contractActive", this.folderData.contractActive);
          } else {
            this.$set(value, "contractActive", false);
            value.contractActive = false;
          }
          value.child.forEach((item) => {
            if (item.id == val.id) {
              this.$set(item, "contractActive", true);
            } else {
              this.$set(item, "contractActive", false);
            }
          });
        } else {
          this.$set(value, "contractActive", false);
        }
      });
      this.modifyState = false;
      this.folderData = null;

      if (!type) {
        Bus.$emit("select", val);
        sessionStorage.setItem("selectData", JSON.stringify(val));
      }
    },
    select(val, type) {
      console.log(1);

      if (!type) this.$store.dispatch("set_selected_contracts_action", val);
      if (val.modifyState) {
        this.$confirm(`${this.$t("text.unsavedContract")}？`, {
          center: true,
          dangerouslyUseHTMLString: true,
        })
          .then(() => {
            this.saveContract(this.modifyParam);
          })
          .catch(() => {
            this.sureSelect(val, type);
          });
      } else {
        this.sureSelect(val, type);
      }
    },
    deleteFile(val) {
      this.$confirm(`${this.$t("dialog.sureDelete")}？`)
        .then((_) => {
          this.deleteData(val);
        })
        .catch((_) => {});
    },
    deleteData(val) {
      this.loading = true;
      let data = {
        groupId: localStorage.getItem("groupId"),
        contractId: val.id,
      };
      deleteCode(data, {})
        .then((res) => {
          const { data, status } = res;
          if (status === 200) {
            let contractList = this.$store.state.contractDataList;
            for (let i = 0; i < contractList.length; i++) {
              if (contractList[i].id === val.id) {
                contractList.splice(i, 1);
              }
            }
            this.$store.dispatch("set_contractDataList", []);
            
            this.getContracts(val.contractPath);
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.loading = false;
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
          });
        });
    },
    deleteFolder(val) {
      this.$confirm(`${this.$t("dialog.sureDelete")}？`)
        .then((_) => {
          this.deleteFolderData(val);
        })
        .catch((_) => {});
    },
    deleteFolderData(val) {
      this.loading = true;
      deletePath(localStorage.getItem("groupId"), val.contractName)
        .then((res) => {
          // this.loading = false
          if (res.status === 200) {
            let contractList = this.$store.state.contractDataList;
            let list = [];
            for (let i = 0; i < contractList.length; i++) {
              if (contractList[i].contractPath === val.contractName) {
                contractList[i].sure = true;
              }
            }
            for (let i = 0; i < contractList.length; i++) {
              if (!contractList[i].sure) {
                list.push(contractList[i]);
              }
            }
            this.$store.dispatch("set_contractDataList", []);
            let arry = [];
            for (let i = 0; i < this.contractArry.length; i++) {
              if (
                this.contractArry[i].contractType == "folder" &&
                this.contractArry[i].folderActive
              ) {
                arry.push(this.contractArry[i].contractName);
              }
            }
            this.getContractPaths(arry);
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
          });
        });
    },
    handleExceed(files, fileList) {
      if (files.length > 1) {
        this.$message({
          type: "warning",
          message: this.$t("contracts.limit_1"),
        });
      }
    },
    querySolcList() {
      solcList()
        .then((res) => {
          if (res.data.code === 0) {
            var array = [];
            res.data.data.forEach((item) => {
              array.push({
                value: item.solcName,
                label: item.solcName,
                solcId: item.solcId,
              });
            });
            this.versionOptions = this.versionOptions.concat(array);
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
          });
        });
    },
    uploadSolc(param) {
      var reader = new FileReader(),
        self = this;
      var filename = param.file.name.substring(
        0,
        param.file.name.lastIndexOf(".")
      );
      var version = param.file.name;
      reader.readAsText(param.file, "UTF-8");
      reader.onload = function (e) {
        var fileString = e.target.result;
        self.queryUploadSolc(param.file, filename, version);
      };
      this.$refs.upload.clearFiles();
    },
    queryUploadSolc(file, filename, version) {
      this.$emit("uploadLoading", true);
      var form = new FormData();
      form.append("fileName", filename);
      form.append("solcFile", file);
      form.append("description", "");
      solcUpload(form)
        .then((res) => {
          this.$emit("uploadLoading", false);
          if (res.data.code === 0) {
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.$emit("uploadLoading", false);
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
          });
        });
    },
    changeVersion(val) {
      this.$emit("uploadLoading", true);
      let param = {
        fileName: "soljson-v0.4.25+commit.59dbf8f1.js",
      };
      readSolcVersion("soljson-v0.4.25+commit.59dbf8f1.js")
        .then((res) => {
          this.$emit("uploadLoading", false);
          if (res.data.code === 0) {
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.$emit("uploadLoading", false);
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
          });
        });
    },
    async loadScript(src) {
      await new Promise((resolve) => {
        var scriptNode = document.createElement("script");
        scriptNode.setAttribute("type", "text/javascript");
        scriptNode.setAttribute("src", src);
        scriptNode.setAttribute("id", "soljson");
        if (document.getElementById("soljson")) {
          document.getElementById("soljson").remove();
          document.head.append(scriptNode);
        }
        if (scriptNode.readyState) {
          //IE 判断
          scriptNode.onreadystatechange = () => {
            if (
              scriptNode.readyState == "complete" ||
              scriptNode.readyState == "loaded"
            ) {
              resolve();
            }
          };
        } else {
          scriptNode.onload = () => {
            console.log("script loaded");
            resolve();
          };
        }
      });
    },
    _checkIsLoadScript(src) {
      let scriptObjs = document.getElementsByTagName("script");
      for (let sObj of scriptObjs) {
        if (sObj.src == src) {
          return true;
        }
      }
      return false;
    },
    deleteSloc(val) {},
    exportFile(val) {
      this.$confirm(`${this.$t("dialog.sureExport")}？`)
        .then((_) => {
          this.sureExportSol(val);
        })
        .catch((_) => {});
    },
    sureExportSol(val) {
      const zip = new JSZip();
      let contractSource = Base64.decode(val.contractSource);
      let contractAbi = val.contractAbi;
      let contractBin = val.bytecodeBin;
      var blobContractSource = new Blob([contractSource], {
        type: "text;charset=utf-8",
      });
      var blobContractAbi = new Blob([contractAbi], {
        type: "text;charset=utf-8",
      });
      var blobContractBin = new Blob([contractBin], {
        type: "text;charset=utf-8",
      });
      zip.file(`${val.contractName}.sol`, blobContractSource, { binary: true });
      zip.file(`${val.contractName}.abi`, blobContractAbi, { binary: true });
      zip.file(`${val.contractName}.bin`, blobContractBin, { binary: true });
      zip.generateAsync({ type: "blob" }).then((content) => {
        FileSaver.saveAs(content, `${val.contractName}`);
      });
    },
    exportFolder(val) {
      this.$confirm(`${this.$t("dialog.sureExport")}？`)
        .then((_) => {
          this.sureExportFolderSol(val);
        })
        .catch((_) => {});
    },
    sureExportFolderSol(val) {
      this.loading = true;
      let data = {
        groupId: localStorage.getItem("groupId"),
        contractPathList: [val.contractName],
      };
      searchContract(data).then((res) => {
        if (res.data.code == 0) {
          this.loading = false;
          var contractList = res.data.data;
          const zip = new JSZip();
          if (contractList.length > 0) {
            contractList.forEach((item) => {
              var blobContractSource = new Blob(
                [Base64.decode(item.contractSource)],
                { type: "text;charset=utf-8" }
              );
              var blobContractAbi = new Blob([item.contractAbi], {
                type: "text;charset=utf-8",
              });
              var blobContractBin = new Blob([item.bytecodeBin], {
                type: "text;charset=utf-8",
              });
              zip.file(`${item.contractName}.sol`, blobContractSource, {
                binary: true,
              });
              zip.file(`${item.contractName}.abi`, blobContractAbi, {
                binary: true,
              });
              zip.file(`${item.contractName}.bin`, blobContractBin, {
                binary: true,
              });
            });
            zip.generateAsync({ type: "blob" }).then((content) => {
              FileSaver.saveAs(content, `${val.contractName}`);
            });
          } else {
            this.$message({
              type: "warning",
              message: this.$t("text.emptyFolder"),
              duration: 2000,
            });
          }
        } else {
          this.$message({
            message: this.$chooseLang(res.data.code),
            type: "error",
            duration: 2000,
          });
        }
      });
    },
    // 导出项目
    exportProjectShowClose() {
      this.$store.dispatch("set_exportProject_show_action", false);
    },
    fromGitHub() {
      this.importFromDialog = true;
    },
    modelClose() {
      this.importFromDialog = false;
    },
    exportSuccessed() {
      this.importFromDialog = false;
      this.getContractPaths();
    },
  },
};
</script>
<style scoped>
.icon {
  font-weight: bold;
}
.contract-menu {
  color: #fff;
  background-color: #2f3b52;
}
.contract-menu-header {
  width: calc(100% + 1px);
  height: 48px;
  line-height: 48px;
  border-bottom: 2px solid #20293c;
  color: #4f6178;
}
.contract-icon {
  vertical-align: middle;
  padding-left: 10px;
  cursor: pointer;
  color: #98a7b9;
}
.checkContract-upload {
  display: block;
  position: absolute;
  height: 30px;
  left: 0;
  margin-top: -30px;
  width: 100%;
  opacity: 0;
  z-index: 9;
  cursor: pointer;
}
.contract-file {
  position: relative;
  padding-left: 25px;
}
.contract-folder {
  padding-left: 5px;
}
.contract-file span {
  cursor: pointer;
}
.contract-file i {
  cursor: pointer;
}
.uploads {
  position: absolute;
  width: 18px;
  height: 18px;
  left: 10px;
  top: 0;
  opacity: 0;
  z-index: 999;
  cursor: pointer;
}
.colorActive {
  color: rgb(55, 238, 242);
}
.contract-delete {
  padding-left: 20px;
  font-weight: 100;
  font-size: 16px;
}
.contract-file-handle {
  position: absolute;
  width: 60px;
  top: 24px;
  padding: 10px;
  background-color: #fff;
  z-index: 9999;
  box-shadow: 1px 1px 1px;
}
.contract-menu-content {
  overflow: auto;
  height: calc(100% - 86px);
}
.contract-menu-content >>> .el-input__inner {
  width: 100px;
  height: 24px;
  line-height: 24px;
  padding: 0 5px;
}
.contract-menu-handle {
  position: fixed;
  font-size: 0;
  width: 70px;
  cursor: pointer;
  font-size: 12px;
  text-align: center;
  background-color: #fff;
  z-index: 100;
}
.contract-menu-handle li {
  font-size: 12px;
  height: 30px;
  line-height: 30px;
}
.contract-menu-handle-list {
  cursor: pointer;
  color: #666;
}
.contract-menu-handle-list:hover {
  color: rgb(55, 238, 242);
}
.solc-wrapper {
  display: flex;
  flex-direction: row;
}
.solc-wrapper >>> .el-select {
  width: 100%;
}
.select-solc {
  width: 100%;
}
.import-solc {
  border: 1px solid;
  height: 36px;
  line-height: 36px;
}
</style>


