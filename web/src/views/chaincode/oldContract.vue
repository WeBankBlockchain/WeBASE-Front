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
    <div class="rivate-key-management-wrapper">
        <v-contentHead :headTitle="$t('route.contractManagementQ')" :headSubTitle="$t('route.contractList')" @changeGroup="changeGroup"></v-contentHead>
        <div class="module-wrapper">
            <div class="search-part">
                <div class="search-part-right">
                    <el-input :placeholder="$t('placeholder.contractListSearch')" v-model="contractData" class="input-with-select" clearable @clear="clearInput">
                        <el-button slot="append" icon="el-icon-search" @click="search"></el-button>
                    </el-input>
                </div>
            </div>
            <div class="search-table">
                <el-table :data="contractList" tooltip-effect="dark" v-loading="loading">
                    <el-table-column prop="contractName" :label="$t('table.contractName')" show-overflow-tooltip width="120" align="center">
                        <template slot-scope="scope">
                            <span style="color: #1f83e7;cursor:pointer" @click='open(scope.row)'>{{scope.row.contractName}}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="contractPath" :label="$t('table.contractPath')" show-overflow-tooltip width="120" align="center"></el-table-column>
                    <!-- <el-table-column prop="version" :label="$t('table.cnsVersion')" show-overflow-tooltip width="120" align="center"></el-table-column> -->
                    <el-table-column prop="contractAddress" :label="$t('table.contractAddress')" show-overflow-tooltip align="center">
                        <template slot-scope="scope">
                            <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row.contractAddress)" :title="$t('title.copyContractAddress')"></i>
                            <span>{{scope.row.contractAddress}}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="contractAbi" :label="$t('table.contractAbi')" show-overflow-tooltip align="center">
                        <template slot-scope="scope">
                            <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row.contractAbi)" :title="$t('title.copyAbi')"></i>
                            <span style="color:#1f83e7" class="link" @click='openAbi(scope.row)'>{{scope.row.contractAbi}}</span>
                        </template>
                    </el-table-column>

                    <el-table-column prop="contractBin" :label="$t('table.contractBin')" show-overflow-tooltip align="center">
                        <template slot-scope="scope">
                            <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row.contractBin)" :title="$t('title.copyBin')"></i>
                            <span>{{scope.row.contractBin}}</span>
                        </template>
                    </el-table-column>

                    <el-table-column prop="createTime" :label="$t('table.createdTime')" show-overflow-tooltip width="150" align="center"></el-table-column>

                    <el-table-column :label="$t('table.actions')" width="280">
                        <template slot-scope="scope">
                            <el-button :disabled="!scope.row.contractAddress" :class="{'grayColor': !scope.row.contractAddress}" @click="send(scope.row)" type="text" size="small">{{$t('title.callContract')}}</el-button>
                            <el-button :disabled="!scope.row.contractAddress || !scope.row.haveEvent" :class="{'grayColor': !scope.row.contractAddress || !scope.row.haveEvent}" @click="handleEvent(scope.row)" type="text" size="small">{{$t('title.checkEvent')}}</el-button>
                            <el-button @click="handleMgmtCns(scope.row)" type="text" size="small">CNS</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>
            </div>
        </div>
        <abi-dialog :show="abiDialogShow" v-if="abiDialogShow" :data='abiData' @close="abiClose"></abi-dialog>
        <el-dialog :title="$t('title.callContract')" :visible.sync="dialogVisible" width="600px" :before-close="sendClose" v-if="dialogVisible" center class="send-dialog">
            <send-transation @success="sendSuccess($event)" @close="handleClose" ref="send" :data="data" :abi='abiData' :version='version'></send-transation>
        </el-dialog>
        <v-editor v-if='editorShow' :show='editorShow' :data='editorData' :sendConstant="sendConstant" @close='editorClose' :input='editorInput' :editorOutput="editorOutput"></v-editor>
        <el-dialog :title="$t('title.checkEvent')" :visible.sync="checkEventVisible" width="470px" center class="send-dialog">
            <check-event-dialog @checkEventSuccess="checkEventSuccess(arguments)" @checkEventClose="checkEventClose" :contractInfo="contractInfo"></check-event-dialog>
        </el-dialog>
        <el-dialog v-if="checkEventResultVisible" :title="$t('table.checkEventResult')" :visible.sync="checkEventResultVisible" width="1070px" center class="send-dialog">
            <check-event-result @checkEventResultSuccess="checkEventResultSuccess($event)" @checkEventResultClose="checkEventResultClose" :checkEventResult="checkEventResult" :contractInfo="contractInfo"></check-event-result>
        </el-dialog>
        <el-dialog v-if="mgmtCnsVisible" :title="$t('text.cns')" :visible.sync="mgmtCnsVisible" width="470px" center class="send-dialog">
            <mgmt-cns :mgmtCnsItem="mgmtCnsItem" @mgmtCnsResultSuccess="mgmtCnsResultSuccess($event)" @mgmtCnsResultClose="mgmtCnsResultClose"></mgmt-cns>
        </el-dialog>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import sendTransation from "@/components/sendTransaction"
import editor from "./dialog/editor"
import abiDialog from "./dialog/abiDialog"
import checkEventDialog from "./dialog/checkEventDialog"
import checkEventResult from "./dialog/checkEventResult"
import mgmtCns from "./dialog/mgmtCns"
import { getContractList } from "@/util/api"
import router from '@/router'
import Bus from "@/bus"
export default {
    name: "oldContract",
    components: {
        "v-contentHead": contentHead,
        "v-editor": editor,
        "abi-dialog": abiDialog,
        "send-transation": sendTransation,
        checkEventDialog,
        checkEventResult,
        mgmtCns
    },
    data: function () {
        return {
            contractList: [],
            loading: false,
            editorShow: false,
            editorData: null,
            abiDialogShow: false,
            abiData: null,
            contractData: "",
            contractName: "",
            contractAddress: "",
            version: "",
            data: null,
            dialogVisible: false,
            currentPage: 1,
            pageSize: 10,
            total: 0,
            checkEventVisible: false,
            checkEventResultVisible: false,
            contractInfo: null,
            checkEventResult: null,
            eventName: '',
            groupId: localStorage.getItem("groupId"),
            sendConstant: null,
            editorInput: null,
            editorOutput: null,
            mgmtCnsVisible: false,
            mgmtCnsItem: {}

        }
    },
    beforeDestroy: function () {
        Bus.$off("changeGroup")
    },
    mounted: function () {
        Bus.$on("changeGroup", data => {
            this.groupId = data
            this.changeGroup()
        })
        if (localStorage.getItem("groupId")) {
            this.getContracts()
        }
    },
    methods: {
        changeGroup: function () {
            this.getContracts()
        },
        getContracts: function () {
            let data = {
                groupId: this.groupId,
                pageNumber: this.currentPage,
                pageSize: this.pageSize,
                contractName: this.contractName,
                contractAddress: this.contractAddress,
                contractStatus: 2
            }
            getContractList(data).then(res => {
                if (res.data.code == 0) {
                    var dataArray = [];
                    dataArray = res.data.data;
                    console.time("耗时");
                    dataArray.forEach(item => {
                        item.haveEvent = false
                        if (item.contractAbi) {
                            let contractAbi = JSON.parse(item.contractAbi)
                            for (let index = 0; index < contractAbi.length; index++) {
                                if (contractAbi[index]['type'] === "event") {
                                    item.haveEvent = true
                                    break;
                                }
                            }
                        }
                    });
                    console.timeEnd("耗时");
                    this.contractList = dataArray;
                    this.total = res.data.totalCount || 0;
                } else {
                    this.$message({
                        message: this.$chooseLang(res.data.code),
                        type: "error",
                        duration: 2000
                    });
                }
            })
                .catch(err => {
                    this.$message.error(this.$t('text.systemError'));
                });
        },
        copyPubilcKey: function (val) {
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
        open: function (val) {
            router.push({
                path: "/contract",
                query: {
                    id: val.id,
                    contractPath: val.contractPath
                }
            })
        },
        editorClose: function () {
            this.editorShow = false;
        },
        openAbi: function (val) {
            this.abiData = val.contractAbi;
            this.abiDialogShow = true
        },
        abiClose: function () {
            this.abiDialogShow = false;
            this.abiData = null
        },
        search: function () {
            if (this.contractData && this.contractData.length && this.contractData.length < 20) {
                this.contractName = this.contractData;
                this.contractAddress = ""
            } else if (this.contractData && this.contractData.length && (this.contractData.length > 20 || this.contractData.length == 20)) {
                this.contractName = "";
                this.contractAddress = this.contractData;
            } else {
                this.contractName = "";
                this.contractAddress = ""
            }
            this.currentPage = 1;
            this.getContracts();
        },
        send: function (val) {
            this.data = val;
            this.abiData = val.contractAbi;
            this.version = val.contractVersion;
            this.dialogVisible = true
        },
        sendClose: function () {
            this.dialogVisible = false
        },
        handleClose: function () {
            this.dialogVisible = false
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
        handleSizeChange: function (val) {
            this.pageSize = val;
            this.currentPage = 1;
            this.getContracts();
        },
        handleCurrentChange: function (val) {
            this.currentPage = val;
            this.getContracts();
        },
        handleEvent: function (val) {
            this.contractInfo = val;
            this.$router.push({
                path: '/onlineTools',
                query: {
                    groupId: this.groupId,
                    type: 'contract',
                    contractAddress: val.contractAddress
                }
            })
            // this.checkEventVisible = true
        },
        checkEventSuccess(msg) {
            this.checkEventResult = msg

            this.checkEventResultVisible = true
        },
        checkEventClose() {
            this.checkEventVisible = false;
        },
        checkEventResultSuccess() {
            this.checkEventResultVisible = false
        },
        checkEventResultClose() {
            this.checkEventResultVisible = false
        },
        handleMgmtCns(item) {
            this.mgmtCnsVisible = true;
            this.mgmtCnsItem = item;
        },
        mgmtCnsResultSuccess() {
            this.mgmtCnsVisible = false;
        },
        mgmtCnsResultClose() {
            this.mgmtCnsVisible = false;
        },
        clearInput() {
            this.contractName = "";
            this.contractAddress = "";
            this.contractData = "";
            this.currentPage = 1;
            this.getContracts()
        }
    }
}
</script>
<style scoped>
.input-with-select >>> .el-input__inner {
    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    border: 1px solid #eaedf3;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
.input-with-select >>> .el-input-group__append {
    border-top-right-radius: 20px;
    border-bottom-right-radius: 20px;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
.input-with-select >>> .el-button {
    border: 1px solid #1f83e7;
    border-radius: inherit;
    background: #1f83e7;
    color: #fff;
}
.grayColor {
    color: #666 !important;
}
.copy-public-key {
    margin-right: 5px;
}
</style>


