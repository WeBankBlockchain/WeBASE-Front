<template>
    <div>
        <v-content-head :headTitle="$t('route.subscribeEvent')" :headSubTitle="$t('route.blockEvent')" @changeGroup="changeGroup"></v-content-head>
        <div class="module-wrapper">
            <div class="search-part">
                <div style="display: flex;">
                    <el-button type="primary" class="search-part-left-btn" @click="addBlockEvent">{{$t('table.addBlockEvent')}}</el-button>
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
        <el-dialog :title="$t('table.addBlockEvent')" :visible.sync="creatBlockEventVisible" width="550px" center class="send-dialog">

            <block-event-dialog @success="success" @close="close"></block-event-dialog>
        </el-dialog>
        <el-dialog :title="$t('table.blockEventInfo')" :visible.sync="checkBlockEventVisible" width="550px" v-if="checkBlockEventVisible" center class="send-dialog">
            <el-table :data="eventDetail">
                <el-table-column show-overflow-tooltip property="appId" :label="$t('table.appId')" width="150"></el-table-column>
                <el-table-column show-overflow-tooltip property="exchangeName" :label="$t('table.exchangeName')" width="200"></el-table-column>
                <el-table-column show-overflow-tooltip property="queueName" :label="$t('table.queueName')"></el-table-column>
            </el-table>
        </el-dialog>
    </div>
</template>

<script>
import { blockEventList, checkBlockEvent, deleteBlockEvent } from "@/util/api";
import contentHead from "@/components/contentHead";
import blockEventDialog from "./components/blockEventDialog";
import Bus from "@/bus"
export default {
    name: 'blockEvent',

    components: {
        "v-content-head": contentHead,
        blockEventDialog
    },

    props: {
    },

    data() {
        return {
            loading: false,
            creatBlockEventVisible: false,
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
            this.queryBlockEventList()
        }
    },

    methods: {
        changeGroup(val) {
            this.group = val
            this.queryBlockEventList();
        },
        success() {
            this.queryBlockEventList()
        },
        close() {
            this.creatBlockEventVisible = false;
        },
        queryBlockEventList() {
            this.loading = true;
            let param = {
                groupId: this.group,
                pageNumber: this.currentPage,
                pageSize: this.pageSize
            }
            blockEventList(param, {})
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
            this.queryBlockEventList();
        },
        handleCurrentChange(val) {
            this.currentPage = val;
            this.queryBlockEventList();
        },
        addBlockEvent() {
            this.creatBlockEventVisible = true;
        },
        checkEvent(val) {
            let param = {
                groupId: this.group,
                appId: val.appId,

            }
            checkBlockEvent(param, {})
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
            deleteBlockEvent(param)
                .then(res => {
                    if (res.data.code === 0) {
                        this.queryBlockEventList()
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
