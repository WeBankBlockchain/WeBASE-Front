<template>
    <div>
        <v-content-head :headTitle="'系统监控'" :headSubTitle="'节点指标'" @changeGroup="changeGroup"></v-content-head>
        <div class="module-wrapper">
            <div class="more-search-table">
                <div class="search-item">
                    <span>显示日期</span>
                    <el-date-picker v-model="currentDate" type="date" placeholder="选择日期" :picker-options="pickerOption" format="yyyy 年 MM 月 dd 日" value-format="yyyy-MM-dd" :default-value="`${Date()}`" class=" select-32" @change="changeCurrentDate">
                    </el-date-picker>
                </div>
                <div class="search-item">
                    <span>对比日期</span>
                    <el-date-picker v-model="contrastDate" type="date" placeholder="选择日期" :picker-options="pickerOption" format="yyyy 年 MM 月 dd 日" value-format="yyyy-MM-dd" class=" select-32" @change="changeContrastDate">
                    </el-date-picker>
                </div>
                <div class="search-item">
                    <span>起止时间</span>
                    <el-time-picker is-range v-model="startEndTime" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" placeholder="选择时间范围" class="time-select-32">
                    </el-time-picker>
                </div>
                <div class="search-item">
                    <span>数据粒度</span>
                    <el-radio-group v-model="timeGranularity">
                        <el-radio :label="60">5分钟</el-radio>
                        <el-radio :label="12">1分钟</el-radio>
                        <el-radio :label="1">5秒钟</el-radio>
                    </el-radio-group>
                    <el-button type="primary" @click="confirmParam()" size="small" style="margin-left: 12px;" :loading="sureing">确认</el-button>
                </div>
            </div>
            <div class="metric-content">
                <div class="metric-split-line"></div>
                <el-row v-loading="loadingInit">
                    <template v-for="item in nodesHealthData">
                        <el-col :xs='24' :sm="24" :md="24" :lg="12" :xl="12">
                            <v-metric-chart :chartOption="item" :reload="nodesReloadNum" v-loading="loading" :metricName="'nodes'"></v-metric-chart>
                        </el-col>
                    </template>
                </el-row>
        </div>
        </div>
        
    </div>
</template>
<script>
import contentHead from "@/components/contentHead";
import metricChart from "@/components/metricChart";
import { metricInfo, nodesHealth } from "@/util/api";
import { format, numberFormat } from "@/util/util.js";
import errcode from "@/util/errcode";
import Bus from "@/bus"
export default {
    name: "nodesMetric",
    components: {
        "v-content-head": contentHead,
        "v-metric-chart": metricChart
    },

    data() {
        return {
            sureing: false,
            loading: false,
            loadingInit: false,
            currentDate: format(new Date().getTime(), "yyyy-MM-dd"),
            contrastDate: null,
            startEndTime: [
                new Date(new Date().toLocaleDateString()),
                new Date()
            ],
            timeGranularity: 60,
            pickerOption: {
                disabledDate(time) {
                    return time.getTime() > Date.now() - 8.64e6;
                }
            },
            chartParam: {
                gap: 60,
                beginDate: `${format(new Date().getTime(),'yyyy-MM-dd')}T${format(new Date(new Date().toLocaleDateString()).getTime(),'HH:mm:ss')}`,
                endDate: `${format(new Date().getTime(),'yyyy-MM-dd')}T${format(new Date().getTime(),'HH:mm:ss')}`,
                contrastBeginDate: "",
                contrastEndDate: "",
                group: localStorage.getItem('groupId') || null
            },
            nodesReloadNum: 1,
            nodesHealthData: [],
        };
    },
    beforeDestroy: function(){
        Bus.$off("changeGroup")
    },
    mounted() {
        Bus.$on("changeGroup",data => {
            this.changeGroup(data)
        })
        if(this.chartParam.group){
            this.getHealthData();
        }
    },
    methods: {
        changeGroup(val){
            this.chartParam.group = val
            this.getHealthData();
        },
        changeCurrentDate($event) {
            this.startEndTime = [
                new Date(new Date().toLocaleDateString()),
                new Date()
            ];
        },
        changeContrastDate($event) {},
        getHealthData() {
            if (this.nodesReloadNum === 1) {
                this.loadingInit = true;
            }
            this.loading = true;
            this.sureing = true;
            var reqData = {
                    // nodeId: 500001
                },
                reqQurey = {};
            reqQurey = this.chartParam;
            nodesHealth(reqData, reqQurey)
                .then(res => {
                    this.loading = false;
                    this.sureing = false;
                    this.loadingInit = false;
                    console.log(res)
                    const {data,status,statusText} = res;
                    if (status === 200) {
                        if (
                            data[0]["data"]["lineDataList"]["timestampList"]
                                .length > 0
                        ) {
                            var timestampList =
                                data[0]["data"]["lineDataList"][
                                    "timestampList"
                                ] || [];
                        } else {
                            var timestampList =
                                data[0]["data"]["contrastDataList"][
                                    "timestampList"
                                ] || [];
                        }
                        this.nodesHealthData = data;
                        this.nodesHealthData.forEach(item => {
                            if (item.metricType === "blockHeight") {
                                item.metricName = "区块高度";
                            } else if (item.metricType === "pbftView") {
                                item.metricName = "pbftView";
                            }else if (item.metricType === 'pendingCount') {
                                item.metricName = "待打包的交易数";
                            }
                            if(this.chartParam.contrastBeginDate){
                                item.data.contrastDataList.contractDataShow = true
                            }else{
                                item.data.contrastDataList.contractDataShow = false
                            }
                            item.data.contrastDataList.timestampList = timestampList;
                            item.data.lineDataList.timestampList = timestampList;
                        });
                        this.nodesReloadNum++;
                    } else {
                        this.$message({
                            type: "error",
                            message: errcode.errCode[res.data.code].cn || "系统错误"
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: "系统错误"
                    });
                });
        },
        confirmParam() {
            this.timeParam()
            this.getHealthData()
        },
        timeParam() {
            let initStartTime = format(new Date(this.startEndTime[0]).getTime(),'HH:mm:ss') ,
                initEndTime = format(new Date(this.startEndTime[1]).getTime(),'HH:mm:ss');
            if (this.currentDate) {
                this.beginDate = `${this.currentDate}T${initStartTime}`;
                this.endDate = `${this.currentDate}T${initEndTime}`;
            }
            if (this.currentDate && this.contrastDate) {
                this.beginDate = `${this.currentDate}T${initStartTime}`;
                this.endDate = `${this.currentDate}T${initEndTime}`;
                this.contrastBeginDate = `${this
                    .contrastDate}T${initStartTime}`;
                this.contrastEndDate = `${this.contrastDate}T${initEndTime}`;
            }
            if (!this.contrastDate) {
                this.beginDate = `${this.currentDate}T${initStartTime}`;
                this.endDate = `${this.currentDate}T${initEndTime}`;
                this.contrastBeginDate = "";
                this.contrastEndDate = "";
            }
            this.chartParam.beginDate = this.beginDate;
            this.chartParam.endDate = this.endDate;
            this.chartParam.contrastBeginDate = this.contrastBeginDate;
            this.chartParam.contrastEndDate = this.contrastEndDate;
            this.chartParam.gap = this.timeGranularity;
            this.chartParam.group = localStorage.getItem('groupName') ? localStorage.getItem('groupName') : '1';
        }
    }
};
</script>
<style scoped>
.search-item {
    display: inline-block;
    margin-bottom: 8px;
    margin-right: 12px;
}
.search-item > span {
    margin-right: 5px;
}
.metric-content {
    min-height: 700px;
}
.metric-split-line {
    margin: 0 30px;
    border-top: 2px dashed #f6f6f6;
}
</style>
