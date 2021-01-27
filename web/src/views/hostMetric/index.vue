<template>
    <div style="background-color: #20293c">
        <v-content-head :headTitle="$t('route.systemMonitoring')" :headSubTitle="$t('route.hostMetrics')" @changeGroup="changeGroup"></v-content-head>
        <div class="module-wrapper">
            <div class="more-search-table">
                <div class="search-item">
                    <span>{{$t('text.showDate')}}</span>
                    <el-date-picker v-model="currentDate" type="date" :placeholder="$t('placeholder.selectedDay')" :picker-options="pickerOption" format="yyyy - MM - dd" value-format="yyyy-MM-dd" :default-value="`${Date()}`" class=" select-32" @change="changeCurrentDate">
                    </el-date-picker>
                </div>
                <div class="search-item">
                    <span>{{$t('text.comparingDate')}}</span>
                    <el-date-picker v-model="contrastDate" type="date" :placeholder="$t('placeholder.selectedDay')" :picker-options="pickerOption" format="yyyy - MM - dd" value-format="yyyy-MM-dd" class=" select-32" @change="changeContrastDate">
                    </el-date-picker>
                </div>
                <div class="search-item">
                    <span>{{$t('text.fromTo')}}</span>
                    <el-time-picker is-range v-model="startEndTime" :range-separator="$t('placeholder.to')" :start-placeholder="$t('placeholder.startTime')" :end-placeholder="$t('placeholder.endTime')" :placeholder="$t('placeholder.selectedTimeRange')" class="time-select-32">
                    </el-time-picker>
                </div>
                <div class="search-item">
                    <span>{{$t('text.dataGranularity')}}</span>
                    <el-radio-group v-model="timeGranularity">
                        <el-radio :label="60" class="font-color-fff">5{{$t('text.minutes')}}</el-radio>
                        <el-radio :label="12" class="font-color-fff">1{{$t('text.minutes')}}</el-radio>
                        <el-radio :label="1" class="font-color-fff">5{{$t('text.seconds')}}</el-radio>
                    </el-radio-group>
                    <el-button type="primary" @click="confirmParam()" size="small" style="margin-left: 12px;" :loading="sureing">{{$t('text.confirm')}}</el-button>
                </div>
                <el-switch v-model="switchBtn" active-color="#13ce66" :active-text="$t('text.toggleOpen')" :inactive-text="$t('text.toggleDown')" inactive-color="#ff4949" :title="$t('title.acquisitionSwitch')" @change="changeToggle">
                </el-switch>
            </div>
            <div class="metric-content">
                <div class="metric-split-line"></div>
                <el-row v-loading="loadingInit">
                    <template v-for="item in metricData">
                        <el-col :xs='24' :sm="24" :md="24" :lg="12" :xl="12">
                            <v-metric-chart :chartOption="item" :reload="reloadNum" v-loading="loading"></v-metric-chart>
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
import { metricInfo, performanceSwitch, postPerformanceSwitch } from "@/util/api";
import { format, numberFormat } from "@/util/util.js";
import Bus from "@/bus"
export default {
    name: "hostMetric",
    components: {
        "v-content-head": contentHead,
        "v-metric-chart": metricChart
    },

    data() {
        return {
            sureing: false,
            loading: false,
            loadingInit: false,
            switchBtn: false,
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
                beginDate: `${format(new Date().getTime(), 'yyyy-MM-dd')}T${format(new Date(new Date().toLocaleDateString()).getTime(), 'HH:mm:ss')}`,
                endDate: `${format(new Date().getTime(), 'yyyy-MM-dd')}T${format(new Date().getTime(), 'HH:mm:ss')}`,
                contrastBeginDate: "",
                contrastEndDate: "",
                group: localStorage.getItem('groupId') || null
            },
            reloadNum: 1,
            metricData: []
        };
    },

    beforeDestroy: function () {
        Bus.$off("changeGroup")
        Bus.$off("chooselanguage")
    },

    mounted() {
        var group = localStorage.getItem('groupId');
        Bus.$on("changeGroup", data => {
            this.changeGroup(data)
            group = data
        })
        Bus.$on("chooselanguage", data => {
            this.changeGroup(group)
        })
        if (this.chartParam.group) {
            this.getChartData();
        }
        this.getPerformanceSwitch();
    },
    methods: {
        changeGroup(val) {
            this.chartParam.group = val
            this.getChartData();
        },
        changeCurrentDate($event) {
            this.startEndTime = [
                new Date(new Date().toLocaleDateString()),
                new Date()
            ];
        },
        changeContrastDate($event) { },
        getChartData() {
            if (this.reloadNum === 1) {
                this.loadingInit = true;
            }
            this.loading = true;
            this.sureing = true;
            var reqData = {
                // nodeId: 500001
            },
                reqQurey = {};
            reqQurey = this.chartParam;
            metricInfo(reqData, reqQurey)
                .then(res => {
                    this.loadingInit = false;
                    this.loading = false;
                    this.sureing = false;
                    const { data, status, statusText } = res;
                    if (status === 200) {
                        if (data[0]["data"]["lineDataList"]["timestampList"].length > 0) {
                            var timestampList = data[0]["data"]["lineDataList"]["timestampList"] || [];
                        } else {
                            var timestampList = data[0]["data"]["contrastDataList"]["timestampList"] || [];
                        }
                        this.metricData = data;
                        this.metricData.forEach(item => {
                            item.gap = this.timeGranularity;
                            if (item.metricType === "cpu") {
                                item.metricName = "CPU";
                                item.metricUint = this.$t('text.usage');
                                item.metricU = "%";
                            } else if (item.metricType === "memory") {
                                item.metricName = this.$t('text.memory');
                                item.metricUint = this.$t('text.usage');
                                item.metricU = "%";
                            } else if (item.metricType === "disk") {
                                item.metricName = this.$t('text.hardDisk');;
                                item.metricUint = this.$t('text.usage');
                                item.metricU = "%";
                            } else if (item.metricType === "txbps") {
                                item.metricName = this.$t('text.uplink');
                                item.metricUint = this.$t('text.bandwidth');
                                item.metricU = "KB/s";
                            } else if (item.metricType === "rxbps") {
                                item.metricName = this.$t('text.downlink');;
                                item.metricUint = this.$t('text.bandwidth');;
                                item.metricU = "KB/s";
                            }
                            if (this.chartParam.contrastBeginDate) {
                                item.data.contrastDataList.contractDataShow = true
                            } else {
                                item.data.contrastDataList.contractDataShow = false
                            }
                            item.data.contrastDataList.timestampList = timestampList;
                            item.data.lineDataList.timestampList = timestampList;
                        });
                        this.reloadNum++;
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
        confirmParam() {
            this.timeParam()
            this.getChartData()
        },
        timeParam() {
            let initStartTime = format(new Date(this.startEndTime[0]).getTime(), 'HH:mm:ss'),
                initEndTime = format(new Date(this.startEndTime[1]).getTime(), 'HH:mm:ss');
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
            this.chartParam.groupId = localStorage.getItem('groupId') ? localStorage.getItem('groupId') : '1';
        },
        getPerformanceSwitch() {
            performanceSwitch().then(res => {
                const { data } = res;
                if (data.code === 0) {
                    this.switchBtn = data.data;
                }
            })
        },
        changeToggle(val) {
            let data = {
                enable: val
            }
            postPerformanceSwitch(data).then(res => {
                const { data } = res;
                if (data.code === 0) {
                    this.$message({
                        type: 'success',
                        message: this.$t('text.toggleSuccessed')
                    })
                } else {
                    this.$message({
                        type: 'error',
                        message: this.$t('text.toggleFailed')
                    })
                }

            })
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
    color: #f6f6f6;
}
.metric-content {
    min-height: 700px;
}
.metric-split-line {
    margin: 0 30px;
    border-top: 2px dashed #f6f6f6;
}
</style>
