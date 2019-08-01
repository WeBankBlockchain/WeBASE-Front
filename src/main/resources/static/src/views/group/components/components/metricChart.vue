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
    name: "metricChart",
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
            console.log('metric')
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
                    text: `${this.chartOption.metricName}${this.chartOption.metricUint}(${this.chartOption.metricU})`,
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
                    }
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
                    echarts.init(document.getElementById("cpu")).resize();
                    echarts.init(document.getElementById("memory")).resize();
                    echarts.init(document.getElementById("disk")).resize();
                    echarts.init(document.getElementById("txbps")).resize();
                    echarts.init(document.getElementById("rxbps")).resize();
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
