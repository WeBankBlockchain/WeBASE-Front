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

        </div>

        <div class="contract-menu-content">
            <ul>
                <li v-for="item in folderList" :key="item.contractId" :id='item.contractFolderId'>
                    <div class="contract-folder" :id='item.folderId'>
                        <i :class="item.folderIcon" @click='open(item)' :id='item.folderId' class="cursor-pointer font-16 no-chase"></i>
                        <i class="wbs-icon-folder cursor-pointer no-chase" @click='open(item)' @contextmenu.prevent="handle($event,item)" style="color: #d19650" :id='item.folderId'></i>
                        <span :id='item.folderId' @click='open(item)' :class="{'colorActive': item.contractActive}" @contextmenu.prevent="handle($event,item)" class="no-chase cursor-pointer">{{item.contractFolderName}}</span>
                        <div class="contract-menu-handle" v-show='item.handleModel' :style="{'top': clentY,'left': clentX}" v-Clickoutside="checkNull">
                            <ul>
                                <li class="contract-menu-handle-list" @click.prevent="exportToIde(item, 'folder')">{{$t('contracts.exportToIde')}}</li>
                            </ul>
                        </div>
                        <br>
                        <ul v-if="item.folderActive" style="padding-left: 15px;" v-Clickoutside="checkNull">
                            <li class="contract-file" v-for='list in item.child' :key="list.contractId">
                                <i class="wbs-icon-file" @click='select(list)'></i>
                                <span @click='select(list)' @contextmenu.prevent="handleFile($event,list)" :id='list.contractId' :class="{'colorActive': list.contractActive}">{{list.contractName}} </span>
                                <div class="contract-menu-handle" v-show='list.handleFile' :style="{'top': clentY,'left': clentX}">
                                    <ul>
                                        <li class="contract-menu-handle-list" @click="exportToIde(list, 'file')">{{$t('contracts.exportToIde')}}</li>
                                    </ul>
                                </div>
                            </li>

                        </ul>
                    </div>
                </li>
            </ul>
        </div>
        <folder v-if='folderVisible' :folderVisible="folderVisible" @close="close" @success="success($event)"></folder>
        <file v-if="fileVisible" :fileVisible="fileVisible" @closeFile="closeFile" @successFile="successFile($event)"></file>
    </div>
</template>
<script>
import { getFolderItemListByStoreId, getContractItemByFolderId, saveChaincode, batchSaveContract } from "@/util/api";
import Bus from "@/bus";
import Clickoutside from 'element-ui/src/utils/clickoutside';
import Folder from "@/components/Folder";
import File from "./components/File.vue";
let Base64 = require("js-base64").Base64;
export default {
    name: "contractCatalog",
    props: {

    },
    components: {
        Folder,
        File
    },
    data() {
        return {
            storeId: "",
            folderList: [],
            folderData: null,
            folderVisible: false,
            fileVisible: false,
            folderItem: {},
            folderName: "",
            fileItem: {},
            fileName: "",
            clentX: 0,
            clentY: 0,
        };
    },
    watch: {
    },
    beforeDestroy() {
        Bus.$off('code')
    },
    mounted() {
        if (this.$route.query.storeId) {
            this.storeId = this.$route.query.storeId;
            this.queryContractFolder()
        }
        Bus.$on('code', (data, type) => {
            this.exportToIde(data, type)
        })
    },
    directives: {
        Clickoutside
    },
    methods: {
        checkNull(list) {
            this.folderList.forEach(value => {
                value.handleModel = false;
                value.child.forEach(item => {
                    item.handleFile = false
                })
            })
        },
        queryContractFolder() {
            getFolderItemListByStoreId(this.storeId)
                .then(res => {
                    if (res.data.code === 0) {
                        let list = res.data.data;
                        if (list.length) {
                            list.forEach((item, index) => {
                                item.folderIcon = index ==0 ? 'el-icon-caret-bottom':'el-icon-caret-right';
                                item.contractActive = false;
                                item.folderActive = false;
                                item.child = [];
                                item.handleModel = false;
                                item.handleFile = false;
                            })
                            this.folderList = list;
                            var contractFolderId = list[0];
                            this.open(contractFolderId)
                            this.queryContract(contractFolderId)
                        }

                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        open(val) {
            if (val.folderActive) {
                this.$set(val, 'folderActive', false)
                this.$set(val, 'folderIcon', 'el-icon-caret-right')
            } else {
                this.$set(val, 'folderActive', true)
                this.$set(val, 'folderIcon', 'el-icon-caret-bottom')
                this.queryContract(val, 'open');
            }
            // this.$set(val, 'contractActive', true);
        },
        select(val) {
            this.folderList.forEach(value => {
                this.$set(value, 'contractActive', false)
                value.child.forEach(item => {
                    if (item.contractId == val.contractId) {
                        this.$set(item, 'contractActive', true)
                    } else {
                        this.$set(item, 'contractActive', false)
                    }
                })
            })
            Bus.$emit('select', val)
        },
        queryContract(val, type) {
            getContractItemByFolderId(val.contractFolderId)
                .then(res => {
                    if (res.data.code === 0) {
                        var folderContract = res.data.data;
                        folderContract.forEach(item => {
                            item.handleFile = false;
                        });
                        this.folderList.forEach(item => {
                            if (item.contractFolderId === val.contractFolderId) {
                                item.child = folderContract;
                            }
                        })
                        this.select(folderContract[0])
                        if (type === 'export') {
                            this.queryBatchSaveContract(folderContract)
                        }

                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        handle(e, list) {
            this.checkNull();
            list.handleModel = true;
            this.clentX = e.clientX + 'px';
            this.clentY = e.clientY + 'px';
        },
        handleFile(e, list) {
            this.checkNull();
            if (this.storeId != '1') { return; }
            list.handleFile = true;
            this.clentX = e.clientX + 'px';
            this.clentY = e.clientY + 'px';
            console.log(this.folderList);
        },
        exportToIde(item, type) {
            this.checkNull()
            if (type === 'folder') {
                this.folderVisible = true;
                this.folderItem = item;
            } else if (type == 'file') {
                this.fileVisible = true;
                this.fileItem = item;
            }

        },
        exportFileToIde(list, type) {

        },
        queryBatchSaveContract(folderContract) {
            let contractItems = folderContract.map(item => {
                return {
                    contractName: item.contractName,
                    contractSource: item.contractSrc
                }
            })
            let param = {
                contractItems: contractItems,
                contractPath: this.folderName,
                groupId: localStorage.getItem("groupId")
            }
            batchSaveContract(param)
                .then(res => {
                    if (res.data.code === 0) {
                        this.$message({
                            type: 'success',
                            message: this.$t('text.importSuccessed')
                        })
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })

        },
        close() {
            this.folderVisible = false;
        },
        success(val) {
            this.folderVisible = false;
            this.folderName = val;
            this.queryContract(this.folderItem, 'export')
        },
        closeFile() {
            this.fileVisible = false;
        },
        successFile(val) {
            this.fileVisible = false;
            this.folderName = val;
            this.querySaveContract()
        },
        querySaveContract() {
            let reqData = {
                groupId: localStorage.getItem("groupId"),
                contractName: this.fileItem.contractName,
                contractPath: this.folderName,
                contractSource: this.fileItem.contractSrc
            }
            saveChaincode(reqData)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.$message({
                            type: "success",
                            message: this.$t('text.exportSuccessed')
                        })
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
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
    width: 84px;
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


