<template>
    <div>
        <v-content-head :headTitle="$t('route.subscribeEvent')" :headSubTitle="$t('route.contractEvent')" @changeGroup="changeGroup"></v-content-head>
        <div class="module-wrapper">
            <div class="search-part">
                <div style="display: flex;">
                    <el-button type="primary" class="search-part-left-btn" @click="addContractEvent">{{$t('table.addContractEvent')}}</el-button>
                </div>
            </div>
            <div class="search-table">
                <el-table :data="eventList" v-loading="loading">
                    <el-table-column v-for="head in eventHead" :label="head.name" :key="head.enName" show-overflow-tooltip align="center">
                        <template slot-scope="scope">
                            <span>{{scope.row[head.enName]}}</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('table.actions')" width="100">
                        <template slot-scope="scope">
                            <el-button @click="checkEvent(scope.row)" type="text" size="mini">{{$t('title.check')}}</el-button>
                            <el-button @click="deleteEvent(scope.row)" type="text" size="mini">{{$t('title.detele')}}</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>
            </div>
        </div>
        <el-dialog :title="$t('table.addContractEvent')" :visible.sync="creatContractEventVisible" width="550px" center class="send-dialog">

            <contract-event-dialog @success="success" @close="close"></contract-event-dialog>
        </el-dialog>
        <el-dialog :title="$t('table.blockEventInfo')" :visible.sync="checkBlockEventVisible" width="850px" v-if="checkBlockEventVisible" center class="send-dialog">
            <el-table :data="eventDetail">
                <el-table-column show-overflow-tooltip property="appId" :label="$t('table.appId')"></el-table-column>
                <el-table-column show-overflow-tooltip property="exchangeName" :label="$t('table.exchangeName')"></el-table-column>
                <el-table-column show-overflow-tooltip property="queueName" :label="$t('table.queueName')"></el-table-column>
                <el-table-column show-overflow-tooltip property="contractAbi" :label="$t('table.contractAbi')"></el-table-column>
                <el-table-column show-overflow-tooltip property="contractAddress" :label="$t('table.contractAddress')"></el-table-column>
                <el-table-column show-overflow-tooltip property="fromBlock" :label="$t('table.fromBlock')"></el-table-column>
                <el-table-column show-overflow-tooltip property="toBlock" :label="$t('table.toBlock')"></el-table-column>
                <el-table-column show-overflow-tooltip property="topicList" :label="$t('table.topicList')"></el-table-column>
            </el-table>
        </el-dialog>
    </div>
</template>

<script>
import { contractEventList, checkContractEvent, deleteContractEvent } from "@/util/api";
import contentHead from "@/components/contentHead";
import contractEventDialog from "./components/contractEventDialog";
import Bus from "@/bus"
export default {
    name: 'contractEvent',

    components: {
        "v-content-head": contentHead,
        contractEventDialog
    },

    props: {
    },

    data() {
        return {
            loading: false,
            creatContractEventVisible: false,
            checkBlockEventVisible: false,
            currentPage: 1,
            pageSize: 10,
            total: 0,
            eventList: [],
            group: localStorage.getItem('groupId') || null,
            eventDetail: []
        }
    },

    computed: {
        eventHead() {
            var arr = [
                {
                    enName: 'appId',
                    name: this.$t('table.appId')
                },
                {
                    enName: 'groupId',
                    name: this.$t('table.groupId')
                },
                {
                    enName: 'exchangeName',
                    name: this.$t('table.exchangeName')
                },
                {
                    enName: 'routingKey',
                    name: this.$t('table.routingKey')
                },
                {
                    enName: 'queueName',
                    name: this.$t('table.queueName')
                },
                {
                    enName: 'contractAddress',
                    name: this.$t('table.contractAddress')
                },
                {
                    enName: 'topicList',
                    name: this.$t('table.topicList')
                },
            ]
            return arr
        }
    },

    watch: {
    },

    created() {
    },
    beforeDestroy() {
        Bus.$off("changeGroup")
    },
    mounted() {
        Bus.$on("changeGroup", data => {
            this.changeGroup(data)
        })
        if (this.group) {
            this.queryContractEventList()
        }
    },

    methods: {
        changeGroup(val) {
            this.group = val
            this.queryContractEventList();
        },
        success() {
            this.queryContractEventList()
        },
        close() {
            this.creatContractEventVisible = false;
        },
        queryContractEventList() {
            this.loading = true;
            let param = {
                groupId: this.group,
                pageNumber: this.currentPage,
                pageSize: this.pageSize
            }
            contractEventList(param, {})
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        this.eventList = res.data.data;
                        this.total = res.data.totalCount;
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
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        handleSizeChange(val) {
            this.pageSize = val;
            this.currentPage = 1;
            this.queryContractEventList();
        },
        handleCurrentChange(val) {
            this.currentPage = val;
            this.queryContractEventList();
        },
        addContractEvent() {
            this.creatContractEventVisible = true;
        },
        checkEvent(val) {
            let param = {
                groupId: this.group,
                appId: val.appId,

            }
            checkContractEvent(param, {})
                .then(res => {
                    if (res.data.code === 0) {
                        this.checkBlockEventVisible = true;
                        this.eventDetail = res.data.data
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
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        deleteEvent(val) {
            let param = {
                id: val.id,
                appId: val.appId,
                groupId: val.groupId,
                exchangeName: val.exchangeName,
                queueName: val.queueName
            }
            deleteContractEvent(param)
                .then(res => {
                    if (res.data.code === 0) {
                        this.queryContractEventList()
                        this.$message({
                            type: 'success',
                            message: this.$t('text.deleteUserSuccessed')
                        })
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
                        message: err.data || this.$t('text.systemError')
                    });
                });

        }
    }
}
</script>

<style scoped>
.search-part::after {
    display: block;
    content: "";
    clear: both;
}
.search-part-left-btn {
    border-radius: 20px;
}
</style>
