<template>
    <div>
        <content-head :headTitle="$t('route.abiList')"></content-head>
        <div class="module-wrapper">
            <div class="search-part">
                <div class="search-part-left" style="padding-top: 20px;">
                    <el-button type="primary" class="search-part-left-btn" @click="generateAbi">{{this.$t("text.addAbi")}}</el-button>
                </div>
            </div>
            <div class="search-table">

                <el-table :data="abiList" class="search-table-content" v-loading="loading">
                    <el-table-column v-for="head in abiHead" :label="head.name" :key="head.enName" :prop="head.enName" :width="head.width" show-overflow-tooltip>
                        <template slot-scope="scope">
                            <template v-if="head.enName !='operation'">
                                <span v-if='head.enName === "contractAbi"'>
                                    <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row.contractAbi)" :title="$t('text.copy')"></i>
                                    <span class="link" @click='openAbi(scope.row)'>{{scope.row.contractAbi}}</span>
                                </span>
                                <span v-else-if='head.enName === "contractAddress"'>
                                    <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row.contractAddress)" :title="$t('text.copy')"></i>
                                    <span>{{scope.row.contractAddress}}</span>
                                </span>
                                <span v-else-if='head.enName === "contractBin"'>
                                    <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row.contractBin)" :title="$t('text.copy')"></i>
                                    <span>{{scope.row.contractBin}}</span>
                                </span>
                                <span v-else>{{scope.row[head.enName]}}</span>
                            </template>
                            <template v-else>
                                <el-button :disabled="!scope.row.contractAddress" :class="{'grayColor': !scope.row.contractAddress}" @click="send(scope.row)" type="text" size="small">{{$t('contracts.sendTransaction')}}</el-button>
                                <el-button :disabled="!scope.row.contractAddress || !scope.row.haveEvent" :class="{'grayColor': !scope.row.contractAddress}" @click="checkEvent(scope.row)" type="text" size="small">{{$t('title.checkEvent')}}</el-button>
                                <el-button @click="updateAbi(scope.row)" type="text" size="small">{{$t('contracts.updateAbi')}}</el-button>
                                <el-button @click="deleteAbi(scope.row)" type="text" size="small">{{$t('contracts.deleteAbi')}}</el-button>

                            </template>

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
        <editor v-if='editorShow' :show='editorShow' :data='editorData' @close='editorClose'></editor>
        <el-dialog :title="$t('nodes.addAbi')" :visible.sync="importVisibility" width="600px" v-if="importVisibility" center class="send-dialog">
            <import-abi @importSuccess="importSuccess" @closeImport="closeImport"></import-abi>
        </el-dialog>
        <el-dialog :title="$t('nodes.updateAbi')" :visible.sync="updateVisibility" width="600px" v-if="updateVisibility" center class="send-dialog">
            <update-abi @updateSuccess="updateSuccess" @closeUpdate="closeUpdate" :updateItem="updateItem"></update-abi>
        </el-dialog>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import abiDialog from "@/components/abiDialog";
import sendTransation from "@/components/sendTransaction"
import editor from "@/components/editor"
import importAbi from "./components/importAbi"
import updateAbi from "./components/updateAbi"
import { getAbiList, deleteImportAbi } from "@/util/api"
import Bus from "@/bus"
export default {
    name: 'nodeList',

    components: {
        contentHead,
        abiDialog,
        sendTransation,
        editor,
        importAbi,
        updateAbi
    },

    props: {
    },

    data() {
        return {
            loading: false,
            abiDialogShow: false,
            dialogVisible: false,
            editorShow: false,
            disabled: false,
            abiList: [],
            abiData: null,
            data: null,
            version: '',
            importVisibility: false,
            updateVisibility: false,
            currentPage: 1,
            pageSize: 10,
            total: 0,
            updateItem: {},
            editorData: null,
            editorInput: null,
            editorOutput: null,
            groupId: localStorage.getItem('groupId')
        }
    },

    computed: {
        abiHead() {
            let data = [
                {
                    enName: "contractName",
                    name: this.$t("contracts.contractName"),
                    width: '165'
                },
                {
                    enName: "contractAddress",
                    name: this.$t("contracts.contractAddress"),
                    width: '330'
                },
                {
                    enName: "contractAbi",
                    name: this.$t("contracts.contractAbi"),
                    width: ''
                },
                // {
                //     enName: "contractBin",
                //     name: this.$t("contracts.runtimeBin"),
                //     width: ''
                // },
                {
                    enName: "operation",
                    name: this.$t('contracts.operation'),
                    width: '250'
                }
            ];
            return data
        }
    },

    watch: {

    },

    created() {
    },
    beforeDestroy: function () {
        Bus.$off("changeGroup")
    },
    mounted() {
        Bus.$on("changeGroup", data => {
            this.groupId = data
            this.changeGroup()
        })
        if (localStorage.getItem('groupId')) {
            this.queryAbiList()
        }
    },

    methods: {
        changeGroup() {
            this.queryAbiList()
        },
        closeImport() {
            this.importVisibility = false
        },
        importSuccess() {
            this.importVisibility = false;
            this.queryAbiList()
        },
        closeUpdate() {
            this.updateVisibility = false
        },
        updateSuccess() {
            this.updateVisibility = false;
            this.queryAbiList()
        },
        generateAbi() {
            this.importVisibility = true;
        },
        queryAbiList() {
            let reqData = {
                groupId: this.groupId,
                pageNumber: this.currentPage,
                pageSize: this.pageSize
            },
                reqQuery = {};
            getAbiList(reqData, reqQuery)
                .then(res => {
                    if (res.data.code === 0) {
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
                        this.abiList = dataArray;
                        this.total = res.data.totalCount;
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        })
                    }
                })
                .catch(error => {
                    this.loading = false;
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    })
                })
        },
        openAbi(val) {
            this.abiData = val.contractAbi;
            this.abiDialogShow = true
        },
        abiClose() {
            this.abiDialogShow = false;
            this.abiData = null
        },
        send(val) {
            this.data = val;
            this.abiData = val.contractAbi;
            this.version = val.contractVersion;
            this.dialogVisible = true
        },
        sendClose() {
            this.dialogVisible = false
        },
        handleClose() {
            this.dialogVisible = false
        },
        sendSuccess(val) {
            this.dialogVisible = false;
            this.editorShow = true;
            this.editorData = null;
            this.editorData = val.resData;
            this.editorInput = val.input;
            this.editorOutput = val.data.outputs;
        },
        editorClose() {
            this.editorShow = false;
        },
        copyPubilcKey(val) {
            if (!val) {
                this.$message({
                    type: "fail",
                    showClose: true,
                    message: this.$t("notice.copyFailure"),
                    duration: 2000
                });
            } else {
                this.$copyText(val).then(e => {
                    this.$message({
                        type: "success",
                        showClose: true,
                        message: this.$t("notice.copySuccessfully"),
                        duration: 2000
                    });
                });
            }
        },
        handleSizeChange(val) {
            this.pageSize = val;
            this.currentPage = 1;
            this.queryAbiList();
        },
        handleCurrentChange(val) {
            this.currentPage = val;
            this.queryAbiList();
        },
        updateAbi(val) {
            this.updateItem = val;
            this.updateVisibility = true;
        },
        deleteAbi(val) {
            this.$confirm(this.$t('text.confirmDelete'))
                .then(_ => {
                    this.sureDeleteAbi(val)
                })
                .catch(_ => { });

        },
        sureDeleteAbi(val) {
            this.loading = true
            deleteImportAbi(val.abiId)
                .then(res => {
                    this.loading = false
                    if (res.data.code === 0) {
                        this.$message({
                            type: "success",
                            message: this.$t('text.deleteUserSuccessed')
                        })
                        this.queryAbiList()
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        })
                    }
                })
                .catch(err => {
                    this.loading = false;
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    })
                })
        },
        checkEvent: function (val) {
            this.$router.push({
                path: '/onlineTools',
                query: {
                    groupId: this.groupId,
                    type: 'abi',
                    contractAddress: val.contractAddress
                }
            })
        },
    }
}
</script>

<style scoped>
.grayColor {
    color: #666 !important;
}
.copy-public-key {
    margin-right: 5px;
}
</style>
