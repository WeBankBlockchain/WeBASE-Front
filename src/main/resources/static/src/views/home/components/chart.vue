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
    <div style="padding-left: 30px;padding-bottom: 2px; marin-bottom:12px;">
        <div :id="chartId" style=" height: 200px; margin: 0 auto;"></div>
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

export default {
    name: "charts",
    props: ["type", "id", "data", "transactionDataArr", "size"],
    data: function() {
        return {
            chartId: this.id,
            chartData: this.data,
            chartTransactionDataArr: this.transactionDataArr,
            chartSize: this.size,
            chart: ""
        };
    },
    watch: {
        data: function() {
            this.chartId = this.id;
            this.chartData = this.data;
            this.chartTransactionDataArr = this.transactionDataArr;
            this.chartSize = this.size;
            this.chartShow();
        }
    },
    mounted: function() {
        this.$nextTick(function() {
            this.chartShow();
        });
    },
    beforeDestroy: function() {
        window.onresize = null;
        if (this.chart) {
            this.chart.dispose();
        }
    },
    methods: {
        chartShow: function() {
            this.chart = echarts.init(document.getElementById(this.chartId));
            let dayNum = this.chartData.length;
            let option = {
                legend: {
                    height: this.chartSize.height,
                    width: this.chartSize.width
                },
                tooltip: {
                    show: true,
                    trigger: "axis",
                    formatter: function(data) {
                        return (
                            '<span style="font-size:10px">' +
                            data[0].name +
                            '</span><br><table ><tr><td style="padding:0">' +
                            '<span style="font-size:10px;color:white">交易量：' +
                            data[0].value +
                            "笔</a></span><br></td></tr></table>"
                        );
                    }
                },
                grid: {
                    left: 43,
                    right: 33,
                    top: 7,
                    bottom: 40
                },
                series: [
                    {
                        type: "line",
                        symbolSize: 1,
                        itemStyle: {
                            normal: {
                                color: "#2878ff",
                                lineStyle: {
                                    color: "#2878ff"
                                }
                            }
                        },
                        data: this.chartTransactionDataArr
                    }
                ],
                xAxis: {
                    data: this.chartData,
                    axisLine: {
                        lineStyle: {
                            color: "#e9e9e9",
                            width: 2
                        }
                    },
                    axisLabel: {
                        interval: 1,
                        textStyle: {
                            color: "rgba(0,14,31,0.62)"
                        }
                    }
                },
                yAxis: {
                    axisLine: {
                        show: false,
                        lineStyle: {
                            color: "#333"
                        }
                    },
                    splitLine: { show: true ,lineStyle: { type: "dashed", color: "#e9e9e9" }},
                    axisTick: { show: false },
                    axisLabel: {
                        formatter: function(value, index) {
                            if (value > 1000 && value < 1000000) {
                                return value / 1000 + "K";
                            } else if (value > 1000000 || value === 1000000) {
                                return value / 1000000 + "M";
                            } else {
                                return value + "";
                            }
                        },
                        textStyle: {
                            color: "rgba(0,14,31,0.62)"
                        }
                    }
                }
            };
            this.chart.setOption(option, true);
            window.onresize = () => {
                this.chart.resize();
            };
        }
    }
};
</script>
<style>

</style>
