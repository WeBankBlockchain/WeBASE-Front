<template>
    <div style="position: relative">
        <div :id="chartId" style="height: 350px; margin: 0 auto;"></div>
        <div class="noData" v-if="chartOption.data.lineDataList.valueList.length === 0">暂无数据</div>
    </div>
</template>

<script>
let echarts = require("echarts/lib/echarts");
require("echarts/lib/chart/line");
require("echarts/lib/component/title");
require("echarts/lib/component/tooltip");
require("echarts/lib/component/grid");
require("echarts/lib/component/legend");
require("echarts/lib/component/dataZoom");
require("echarts/lib/component/toolbox");
import { format, numberFormat } from "@/util/util.js";
export default {
    name: "nodesChart",
    components: {},
    props: {
        chartOption: {
            type: Object
        },
        reload: {
            type: Number
        }
    },
    watch: {
        reload: function() {
            console.log('nodes')
            this.getChartData();
        }
    },
    data() {
        return {
            chartId: this.chartOption.metricType,
            chart: ""
        };
    },
    beforeDestroy: function() {
        window.onresize = null;
    },
    mounted() {
        this.$nextTick(() => {
            this.getChartData();
        });
    },
    methods: {
        getChartData() {
            this.chart = echarts.init(document.getElementById(this.chartId));
            let xList = [],
                yList = [],
                yContrastList= [],
                yContrastTitle = "";
            xList = this.chartOption.data.lineDataList.timestampList.map(item => {
                return format(new Date(item).getTime(), "HH:mm:ss");
            });
            yList = this.chartOption.data.lineDataList.valueList.map(item => {
                return item;
            });
            yContrastList = this.chartOption.data.contrastDataList.valueList.map(item => {
                return item
            })
            if(this.chartOption.data.contrastDataList.valueList.length > 0){
                yContrastTitle = "对比日数据"
            }
            let option = {
                title: {
                    text: `${this.chartOption.metricName}`,
                    textStyle: {
                        color: "#1e53a4",
                        fontStyle: "normal",
                        fontWeight: "bold",
                        fontSize: 14
                    },
                    left: 28,
                    top: 20
                },
                tooltip: {
                    trigger: "axis",
                    axisPointer: {
                        type: "cross",
                        animation: false,
                        label: {
                            backgroundColor: "#505765"
                        }
                    }
                },
                legend: {
                    data: ["显示日数据", yContrastTitle],
                    y: 20
                },
                dataZoom: [{
                    type:'inside',
                }],
                grid: {
                    left: 33,
                    right: 33,
                    bottom: 17,
                    containLabel: true
                },
                toolbox: {
                    right: '30',
                    top: '16',
                    feature: {
                        dataZoom: {
                            yAxisIndex: 'none'
                        },
                        restore: {}
                    }
                },
                xAxis: {
                    type: "category",
                    boundaryGap: false,
                    data: xList,
                    axisLabel: {
                        interval: "auto",
                        formatter: function(value, index) {
                            return value.substr(0,5)
                        }
                    },
                    splitLine: {
                        show: true,
                        lineStyle: {
                            type: "dashed"
                        }
                    }
                },
                yAxis: {
                    axisLine: {
                        show: true,
                        lineStyle: {
                            color: "#333"
                        }
                    },
                    splitLine: {
                        show: true,
                        lineStyle: { type: "dashed", color: "#e9e9e9" }
                    },
                    axisTick: { show: false },
                    axisLabel: {
                        textStyle: {
                            color: "rgba(0,14,31,0.62)"
                        }
                    },
                    scale: true
                },
                series: [
                    {
                        name: "显示日数据",
                        type: "line",
                        data: yList,
                        smooth: true
                    },
                    {
                        name: yContrastTitle,
                        type: "line",
                        data: yContrastList,
                        smooth: true
                    }
                ]
            };
            this.chart.setOption(option, true);
            setTimeout(() => {
                window.onresize = () => {
                    echarts.init(document.getElementById("pbftView")).resize();
                    echarts.init(document.getElementById("blockHeight")).resize();
                };
            }, 200);
        }
    }
};
</script>

<style scoped>
    .noData {
        position: absolute;
        display: inline-block;
        top: 50%;
        left: 46%;
    }
</style>
