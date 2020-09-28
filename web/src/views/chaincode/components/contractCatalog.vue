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
    <div class="contract-menu" style="position: relative;height: 100% ;">
        <div class="contract-menu-header">
            <el-tooltip class="item" effect="dark" :content="$t('title.newFile')" placement="top-start">
                <svg-icon icon-class='new-file' class="icon contract-icon" @click='addFile'></svg-icon>
                <!-- <i class="wbs-icon-Addfile icon contract-icon" @click='addFile'></i> -->
            </el-tooltip>
            <el-tooltip class="item" effect="dark" :content="$t('title.newFolder')" placement="top-start">
                <svg-icon icon-class='new-folder' class="icon contract-icon" @click='addFolder'></svg-icon>
                <!-- <i class="wbs-icon-Addfolder icon contract-icon" @click='addFolder'></i> -->
            </el-tooltip>
            <el-tooltip class="item" effect="dark" :content="$t('title.upload')" placement="top-start">
                <svg-icon icon-class='upload-file' class="icon contract-icon"></svg-icon>
                
            </el-tooltip>
            <input multiple type="file" id="file" ref='file' name="chaincodes" class="uploads" @change="upload($event)" />
        </div>

        <div class="contract-menu-content">
            <ul>
                <li v-for='item in contractArry' :key="item.id">
                    <div v-if='item.contractType == "file"' class="contract-file" :id='item.id' :style="{'padding-left': item.modifyState ? `${10}px`:''}">
                        <div class="ellipsis-info" :class="{'colorActive': item.contractActive}">
                           
                            <i class="wbs-icon-radio font-6" v-if="item.modifyState"></i>
                            <svg-icon icon-class='file' class="font-20"  @contextmenu.prevent="handle($event,item)" @click='select(item)' v-if='!item.renameShow' :id='item.id'></svg-icon>
                            <!-- <i class="wbs-icon-file" ></i> -->
                            <span @contextmenu.prevent="handle($event,item)" @click='select(item)' :id='item.id' v-if='!item.renameShow'>{{item.contractName}}</span>
                        </div>
                        <el-input v-model="contractName" v-focus="true" autofocus='autofocus' maxlength="32" @blur="changeName(item)" v-if="item.renameShow"></el-input>
                        <!-- <div class="contract-menu-handle" v-if='item.handleModel' :style="{'top': `${clentY}px`,'left': `${clentX}px`}" v-Clickoutside="checkNull">

                            <ul v-if="contractFile">
                                <li class="contract-menu-handle-list" @click="rename">{{$t('dialog.rename')}}</li>
                                <li class="contract-menu-handle-list" @click="deleteFile(item)">{{$t('dialog.delete')}}</li>
                            </ul>
                        </div> -->
                    </div>
                    <div v-if='item.contractType == "folder"' class="contract-folder" :id='item.folderId'>
                        <i :class="item.folderIcon" @click='open(item)' v-if="!item.renameShow" :id='item.folderId' class="cursor-pointer font-16 no-chase"></i>
                        <svg-icon icon-class='folder' class="font-20"  @click='open(item)' @contextmenu.prevent="handle($event,item)" v-if="!item.renameShow" :id='item.folderId'></svg-icon>
                        <!-- <i class="wbs-icon-folder cursor-pointer no-chase" ></i> -->
                        <span @click='open(item)' @contextmenu.prevent="handle($event,item)" :id='item.folderId' v-if="!item.renameShow" :class="{'colorActive': item.contractActive}" class="no-chase cursor-pointer">{{item.contractName}}</span>
                        <!-- <div class="contract-menu-handle" v-if='item.handleModel&&item.contractName!=="template"' :style="{'left': `${clentX}px`}" v-Clickoutside="checkNull">
                            <ul>
                                <li class="contract-menu-handle-list" @click="addFiles">{{$t('dialog.newFile')}}</li>
                                <li class="contract-menu-handle-list" @click='deleteFolder(item)'>{{$t('dialog.delete')}}</li>
                            </ul>
                        </div> -->
                        <br>
                        <ul v-if="item.folderActive" style="padding-left: 20px;">
                            <li class="contract-file" v-for='list in item.child' :key="list.id" :style="{'padding-left': list.modifyState ? `${10}px`:''}">
                                <div class="ellipsis-info" :class="{'colorActive': list.contractActive}">
                                    <i class="wbs-icon-radio font-6" v-if="list.modifyState"></i>
                                    <!-- <i class="wbs-icon-file" v-if='!list.renameShow' @click='select(list)'></i> -->
                                    <svg-icon icon-class='file' class="font-20"  v-if='!list.renameShow' @click='select(list)'></svg-icon>
                                    <span @click='select(list)' @contextmenu.prevent="handle($event,list)" :id='list.id' v-if='!list.renameShow'>{{list.contractName}}</span>
                                </div>

                                <el-input v-model="contractName" autofocus='autofocus' maxlength="32" @blur="changeName(list)" v-if="list.renameShow"></el-input>
                                <!-- <div class="contract-menu-handle" v-if='list.handleModel' :style="{'top': `${clentY}px`,'left': `${clentX - 35}px`}" v-Clickoutside="checkNull">
                                    <ul v-if="contractFile&&item.contractName!=='template'">
                                        <li class="contract-menu-handle-list" @click="rename">重命名</li>
                                        <li class="contract-menu-handle-list" @click="deleteFile(list)">删除</li>
                                    </ul>
                                </div> -->
                            </li>
                        </ul>
                    </div>
                </li>
            </ul>

        </div>
        <!-- <div class="solc-wrapper">
            <div class="select-solc">
                <el-select v-model="version" placeholder="请选择" @change="changeVersion">
                    <el-option v-for="item in versionOptions" :key="item.value" :label="item.label" :value="item.value">
                        <span style="float: left">{{ item.label }}</span>
                        <span style=" color: #8492a6; font-size: 13px" class="el-icon-circle-close" @click="deleteSloc(item.solcId)"></span>
                    </el-option>
                </el-select>
            </div>
            <div class="import-solc cursor-pointer" :title="$t('title.uploadSolc')">
                <el-upload ref="upload" action :http-request="uploadSolc" :limit="1" :on-exceed="handleExceed" :show-file-list="false" accept=".js">
                    <i class="el-icon-upload2"></i>
                </el-upload>
            </div>
        </div> -->
        <add-folder v-if="foldershow" :foldershow="foldershow" @close='folderClose' @success='folderSuccess'></add-folder>
        <add-file v-if="fileshow" :fileshow="fileshow" @close='fileClose' @success='fileSucccess($event)' :id='folderId'></add-file>
        <select-catalog v-if='cataLogShow' :show='cataLogShow' @success='catalogSuccess($event)' @close='catalogClose'></select-catalog>
    </div>
</template>
<script>
import addFolder from "../dialog/addFolder";
import addFile from "../dialog/addFile";
import selectCatalog from "../dialog/selectCatalog";
import { getContractList, saveChaincode, deleteCode, solcList, solcUpload, solcDownload, deleteSolcId, readSolcVersion,queryGroup } from "@/util/api";
import Bus from "@/bus";
import Clickoutside from 'element-ui/src/utils/clickoutside'
export default {
    name: "contractCatalog",
    props: {
        solcVersionOptions: {
            type: Array
        },
        solcVersion: {
            type: String
        }
    },
    components: {
        "add-folder": addFolder,
        "add-file": addFile,
        "select-catalog": selectCatalog
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
            group: 1

        };
    },
    watch: {
        solcVersionOptions(val){
            this.versionOptions = val
            // this.querySolcList()
        },
        solcVersion(val) {
            this.version = this.solcVersion
        }
    },
    beforeDestroy() {
        Bus.$off("compile");
        Bus.$off("deploy");
        Bus.$off("open");
        Bus.$off("save");
        Bus.$off("modifyState");
    },
    mounted() {
        this.$nextTick(function () {
            if(localStorage.getItem("groupId")){
                this.getContracts();
            }else{
                this.getGroup(this.getContracts)
            }
        });
        Bus.$on("compile", data => {
            this.saveContract(data, `${this.$t('text.compilationSucceeded')}`);
        });
        Bus.$on("save", data => {
            this.saveContract(data);
        });
        Bus.$on("deploy", data => {
            this.getContracts(data);
        });
        Bus.$on("open", data => {
            this.contractArry.forEach(value => {
                if (value.contractName == data.contractPath && !value.folderActive) {
                    this.$set(value, "folderActive", true);
                    this.$set(value, "folderIcon", "el-icon-arrow-down");
                }
            });
            this.select(data);
        });
        Bus.$on('modifyState', data => {
            this.contractList.forEach(value => {
                if (value.id === data.id && data.modifyState) {
                    this.modifyState = data.modifyState
                    this.modifyParam = data;
                    this.$set(value, "modifyState", true);
                } else {
                    this.$set(value, "modifyState", false);

                }
            })
        })
        // this.querySolcList()
    },
    directives: {
        Clickoutside,
        focus: {
            update(el, { value }) {
                if (value) {
                    el.focus();
                }
            }
        }
    },
    methods: {
        getGroup(callback) {
            queryGroup()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        let arr = data.sort((a, b) => {
                            return a - b
                        })
                        this.group = arr[0];
                        // this.groupList = list;
                        // if (!this.group) {
                        //     this.group = this.groupList[0].group;
                        //     this.groupName = this.groupList[0].groupName;
                        // } else {

                        // }
                        // localStorage.setItem("groupName", this.groupName)
                        localStorage.setItem('groupId', this.group);
                        // localStorage.setItem("cluster", JSON.stringify(list));
                        callback()
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: this.$t('text.systemError')
                    });
                })
        },
        checkNull(list) {
            this.contractArry.forEach(value => {
                value.handleModel = false;
                if (value.contractType == 'folder') {
                    value.child.forEach(list => {
                        list.handleModel = false;
                    })
                }
            })
            this.ID = "";
            this.contractFile = false;
            this.contractFolder = false;
            this.handleModel = false;
        },
        handle(e, list) {
            this.checkNull()
            list.handleModel = true
            if (e.clientX > 201) {
                this.clentX = e.clientX - 200;
            } else {
                this.clentX = e.clientX;
            }
            this.clentY = 20;
            this.ID = e.target.id;
            let item = {};
            if (this.ID) {
                this.handleModel = true;
                this.contractArry.forEach(value => {
                    if ((value.id && value.id == this.ID) || (value.folderId && value.folderId == this.ID)) {
                        item = value;
                    } else if (value.contractType == "folder") {
                        value.child.forEach(list => {
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
            this.contractArry.forEach(value => {
                value.handleModel = false;
                if (value.contractType == 'folder') {
                    value.child.forEach(list => {
                        list.handleModel = false;
                    })
                }
            })
            this.contractArry.forEach(value => {
                if (value.id == this.ID) {
                    this.$set(value, "renameShow", true);
                    this.contractName = value.contractName;
                } else if (value.contractType == "folder" && value.folderId !== this.ID) {
                    value.child.forEach(item => {
                        if (item.id == this.ID) {
                            this.$set(item, "renameShow", true);
                            this.contractName = item.contractName;
                        } else if (item.id == this.ID) {
                            this.$set(item, "renameShow", false);
                        } else {
                            this.$set(item, "renameShow", false);
                        }
                    });
                } else if (value.contractType == "folder" && value.folderId == this.ID) {
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
            if (pattern.test(this.contractName) && this.contractName.length < 32 && this.contractName.length > 1) {
                if (this.contractName !== val.contractName) {
                    for (let i = 0; i < this.contractList.length; i++) {
                        if (this.contractList[i].contractName == this.contractName && this.contractList[i].contractPath == val.contractPath) {
                            this.$message({
                                message: "同目录下存在相同的合约，请重新命名",
                                type: "error"
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
                    type: "error"
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
        addFiles() {
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
                let filetype = this.uploadFiles[i].name.split(".")[1];
                if (filessize > 400) {
                    this.$message({
                        message: this.$t('text.fileExceeds'),
                        type: "error"
                    });
                    this.cataLogShow = false
                    break;
                } else if (filetype !== "sol") {
                    this.$message({
                        message: this.$t('text.uploadSol'),
                        type: "error"
                    });
                    this.cataLogShow = false
                    break;
                } else {
                    this.cataLogShow = true
                }
            }
        },
        catalogSuccess(val) {
            for (let i = 0; i < this.uploadFiles.length; i++) {
                let reader = new FileReader(); //新建一个FileReader
                reader.readAsText(this.uploadFiles[i], "UTF-8"); //读取文件
                let filename = "", _this = this;
                filename = this.uploadFiles[i].name.split(".")[0];
                let num = 0;
                this.contractList.forEach(value => {
                    if (value.contractName == filename && value.contractPath == val && num === 0) {
                        this.$message({
                            type: "error",
                            message: this.$t('text.contractSameDirectory')
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
                            contractNo: new Date().getTime()
                        };
                        _this.saveContract(data);
                    };
                }
            }
            this.$refs.file.value = "";
            this.catalogClose();
        },
        catalogClose() {
            this.cataLogShow = false;
        },
        folderClose() {
            this.foldershow = false;
        },
        folderSuccess() {
            this.folderClose();
            this.getContractArry();
        },
        fileClose() {
            this.fileshow = false;
            this.folderId = "";
            this.ID = "";
        },
        getContractArry(val) {
            let result = [];
            let list = [];
            let folderArry = this.createFolder();
            let newFileList = [];
            list = this.contractList || [];
            list.forEach(value => {
                this.$set(value, 'handleModel', false)
                if (value.contractPath == "/") {
                    newFileList.push(value);
                }
            });
            folderArry.forEach(value => {
                this.$set(value, 'handleModel', false)
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
            } else if (val && this.contractList.length) {
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
                bytecodeBin: param.bytecodeBin
            };
            if (param.id) {
                reqData.contractId = param.id;
            }
            saveChaincode(reqData)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        if (param.id) {
                            this.$message({
                                type: "success",
                                message: title || this.$t('text.saveSucceeded')
                            })
                        }
                        this.modifyState = false
                        this.getContracts(data);
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
                        message: this.$t('text.systemError')
                    });
                });
        },
        getContracts(list) {
            let data = {
                groupId: localStorage.getItem("groupId"),
                pageNumber: 1,
                pageSize: 500
            };
            getContractList(data)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.contractList = data.data || [];
                        localStorage.setItem("contractList", JSON.stringify(this.contractList))
                        if (data.data.length) {
                            if (localStorage.getItem("folderList")) {
                                this.folderList = JSON.parse(
                                    localStorage.getItem("folderList")
                                );
                            } else {
                                this.folderList = [];
                            }
                            this.contractList.forEach(value => {
                                if (value.contractPath != "/") {
                                    let item = {
                                        folderName: value.contractPath,
                                        folderId: new Date().getTime() + `${value.contractPath}`,
                                        folderActive: false,
                                        groupId: localStorage.getItem("groupId"),
                                        modifyTime: value.modifyTime
                                    };
                                    this.folderList.push(item);
                                }
                            });
                            let result = [];
                            let array = [];
                            let obj = {};
                            for (let i = 0; i < this.folderList.length; i++) {
                                if (!obj[this.folderList[i].folderName] && this.folderList[i].groupId == localStorage.getItem("groupId")) {
                                    result.push(this.folderList[i]);
                                    obj[this.folderList[i].folderName] = true;
                                } else if (this.folderList[i].groupId != localStorage.getItem('groupId')) {
                                    array.push(this.folderList[i]);
                                }

                            }
                            this.folderList = result.concat(array);
                            localStorage.setItem(
                                "folderList",
                                JSON.stringify(this.folderList)
                            );
                            this.contractList.forEach(value => {
                                this.$set(value, "contractType", "file");
                                this.$set(value, "contractActive", false);
                                this.$set(value, "renameShow", false);
                                this.$set(value, "inputShow", false);
                                this.$set(value, "modifyState", false);
                            });
                            if (list) {
                                this.getContractArry(list);
                            } else if (this.$route.query.id) {
                                let index = 0
                                this.contractList.forEach(value => {
                                    if (
                                        value.id ==
                                        this.$route.query.id
                                    ) {
                                        index++
                                        this.getContractArry(value);
                                    }
                                });
                                if (index == 0) {
                                    this.getContractArry()
                                }
                            } else {
                                this.getContractArry();
                            }
                        } else {
                            if (localStorage.getItem("folderList")) {
                                this.folderList = JSON.parse(
                                    localStorage.getItem("folderList")
                                );
                                this.getContractArry();
                            }
                        }
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
                        message: this.$t('text.systemError')
                    });
                });
        },
        fileSucccess(val) {
            let num = 0;
            this.contractList.forEach(value => {
                if (value.contractName == val.contractName && value.contractPath == val.contractPath) {
                    this.$message({
                        type: "error",
                        message: this.$t('text.contractSameDirectory')
                    });
                    num++;
                }
            });
            if (!num) {
                this.saveContract(val);
            }
            this.fileClose();
        },
        createFolder() {
            let result = [];
            if (localStorage.getItem("folderList")) {
                this.folderList = JSON.parse(
                    localStorage.getItem("folderList")
                );
            } else {
                this.folderList = [];
            }
            this.folderList.forEach((value, index) => {
                let num = 0;
                if (!value.groupId || (value.groupId && localStorage.getItem("groupId") && value.groupId == localStorage.getItem("groupId"))) {
                    var data = {
                        contractName: value.folderName,
                        folderId: value.folderId,
                        contractActive: false,
                        contractType: "folder",
                        folderIcon: "el-icon-arrow-down",
                        folderActive: true,
                        renameShow: false,
                        inputShow: false
                    };
                    if (index != 0) {
                        data = {
                            contractName: value.folderName,
                            folderId: value.folderId,
                            contractActive: false,
                            contractType: "folder",
                            folderIcon: "el-icon-arrow-up",
                            folderActive: false,
                            renameShow: false,
                            inputShow: false
                        }
                    }
                    this.contractArry.forEach((item, index) => {
                        if (item.contractType == "folder" && item.folderId == value.folderId) {
                            data.folderIcon = item.folderIcon;
                            data.folderActive = item.folderActive;
                        }
                    });
                    result.push(data);
                }
            });
            return result;
        },
        open(val) {
            this.contractArry.forEach(value => {
                this.$set(value, "contractActive", false);
                if (value.contractType == "folder") {
                    value.child.forEach(item => {
                        this.$set(item, "contractActive", false);
                    });
                }
            });
            if (val.folderActive) {
                this.$set(val, "folderActive", false);
                this.$set(val, "folderIcon", "el-icon-arrow-up");
            } else {
                this.$set(val, "folderActive", true);
                this.$set(val, "folderIcon", "el-icon-arrow-down");
            }
            this.$set(val, "contractActive", true);
        },
        sureSelect(val) {
            let num = 0;
            this.contractArry.forEach(value => {
                if (value.id == val.id) {
                    this.$set(value, "contractActive", true);
                } else if (value.contractType == "folder") {
                    this.$set(value, "contractActive", false);
                    value.child.forEach(item => {
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
            Bus.$emit("select", val);
        },
        select(val) {
            if (this.modifyState) {
                this.$confirm(`${this.$t('text.unsavedContract')}？`, {
                    center: true,
                    dangerouslyUseHTMLString: true
                })
                    .then(() => {
                        this.saveContract(this.modifyParam)
                    })
                    .catch(() => {
                        this.sureSelect(val)
                    })
            } else {
                this.sureSelect(val)
            }
        },
        deleteFile(val) {
            this.$confirm(`${this.$t('dialog.sureDelete')}？`)
                .then(_ => {
                    this.deleteData(val);
                })
                .catch(_ => { });
        },
        deleteData(val) {
            let data = {
                groupId: localStorage.getItem("groupId"),
                contractId: val.id
            };
            deleteCode(data, {})
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.getContracts();
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.loading = false;
                    this.$message({
                        type: "error",
                        message: this.$t('text.systemError')
                    });
                });
        },
        deleteFolder(val) {
            this.$confirm(`${this.$t('dialog.sureDelete')}？`)
                .then(_ => {
                    let list = val.child;
                    while (list.length > 0) {
                        this.deleteData(list[0]);
                        list.splice(0, 1);
                    }
                    this.$nextTick(() => {
                        this.folderList = JSON.parse(
                            localStorage.getItem("folderList")
                        );
                        this.folderList.forEach((value, index) => {
                            if (val.folderId == value.folderId) {
                                this.folderList.splice(index, 1);
                            }
                        });
                        localStorage.setItem("folderList", JSON.stringify(this.folderList));
                        if (list.length === 0) {
                            setTimeout(() => {
                                this.getContracts()
                            }, 10);
                        }
                    })
                })
                .catch(_ => { });
        },
        handleExceed(files, fileList) {
            if (files.length > 1) {
                this.$message({
                    type: 'warning',
                    message: this.$t('contracts.limit_1')
                })
            }
        },
        querySolcList() {
            solcList()
                .then(res => {
                    if (res.data.code === 0) {
                        var array = []
                        res.data.data.forEach(item=>{
                            array.push({
                                value: item.solcName,
                                label: item.solcName,
                                solcId: item.solcId
                            })
                        })
                        console.log(array)
                        this.versionOptions = this.versionOptions.concat(array)
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        })
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: this.$t('text.systemError')
                    });
                });

        },
        uploadSolc(param) {
            var reader = new FileReader(), self = this;
            var filename = param.file.name.substring(0, param.file.name.lastIndexOf("."));
            var version = param.file.name;
            // new FormData()
            reader.readAsText(param.file, "UTF-8");
            reader.onload = function (e) {
                var fileString = e.target.result;
                self.queryUploadSolc(param.file, filename, version)
            }
            this.$refs.upload.clearFiles()
        },
        queryUploadSolc(file, filename, version) {
            this.$emit('uploadLoading', true)
            var form = new FormData()
            form.append('fileName', filename)
            form.append('solcFile', file)
            form.append('description', '')
            solcUpload(form)
                .then(res => {
                    console.log(res)
                    this.$emit('uploadLoading', false)
                    if (res.data.code === 0) {


                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        })
                    }
                })
                .catch(err => {
                    this.$emit('uploadLoading', false)
                    this.$message({
                        type: "error",
                        message: this.$t('text.systemError')
                    });
                });

        },
        queryDownloadSolc() {
            // solcDownload()

        },
        changeVersion(val) {
            // sessionStorage.setItem('solcVersion', val)
            // this.$router.push('blank')
            console.log(val)
            this.$emit('uploadLoading', true)
            let param = {
                fileName: 'soljson-v0.4.25+commit.59dbf8f1.js'
            }
            readSolcVersion('soljson-v0.4.25+commit.59dbf8f1.js')
                .then(res => {
                    console.log(res)
                    this.$emit('uploadLoading', false)
                    if (res.data.code === 0) {

                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        })
                    }
                })
                .catch(err => {
                    this.$emit('uploadLoading', false)
                    this.$message({
                        type: "error",
                        message: this.$t('text.systemError')
                    });
                });
            // this.loadScript(val)
        },
        async  loadScript(src) {
            await new Promise(resolve => {
                // 如果已经加载了本js，直接调用回调
                // if (this._checkIsLoadScript(src)) {
                //     resolve();
                // }

                var scriptNode = document.createElement("script");
                scriptNode.setAttribute("type", "text/javascript");
                scriptNode.setAttribute("src", src);
                scriptNode.setAttribute('id', 'soljson');
                if (document.getElementById('soljson')) {
                    document.getElementById('soljson').remove()
                    document.head.append(scriptNode)
                }
                if (scriptNode.readyState) { //IE 判断
                    scriptNode.onreadystatechange = () => {
                        if (scriptNode.readyState == "complete" || scriptNode.readyState == 'loaded') {
                            resolve();
                        }
                    }
                } else {
                    scriptNode.onload = () => {
                        console.log("script loaded");
                        resolve();
                    }
                }
            })
        },
        _checkIsLoadScript(src) {
            let scriptObjs = document.getElementsByTagName('script');
            for (let sObj of scriptObjs) {
                if (sObj.src == src) {
                    return true;
                }
            }
            return false;
        },
        deleteSloc(val) {
            console.log(val)
        }
    }
};
</script>
<style scoped>
.icon {
    font-weight: bold;
}
.contract-menu {
    color: #fff;
    background: rgba(69,54,187,0.70);
    border-radius: 16px 0 0 0;
    border-radius: 16px 0px 0px 0px;
}
.contract-menu-header {
    width: calc(100% + 1px);
    height: 56px;
    line-height: 56px;
    border-bottom: 4px solid  rgb(22, 14, 83);
    padding-left: 30px;
    color: #4f6178;
}
.contract-icon {
    vertical-align: middle;
    padding-left: 10px;
    cursor: pointer;
    font-size: 32px;
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
    width: 32px;
    height: 32px;
    left: 132px;
    top: 14px;
    opacity: 0;
    z-index: 999999;
    background-color: #fff;
    cursor: pointer;
    border-radius: 50%;
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
    padding: 25px 0 0 30px;
    overflow: auto;
    height: calc(100% - 86px);
    box-sizing: border-box;
}
.contract-menu-content >>> .el-input__inner {
    width: 100px;
    height: 24px;
    line-height: 24px;
    padding: 0 5px;
}
.contract-menu-handle {
    position: absolute;
    font-size: 0;
    width: 70px;
    cursor: pointer;
    font-size: 14px;
    text-align: center;
    background-color: #fff;
    z-index: 100;
}
.contract-menu-handle li {
    font-size: 14px;
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


