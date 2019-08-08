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
    <div style="padding-left: 30px;">
        <div class="charts-title text-left">
            <span class="total-count">当前总数量：{{numberFormat(chartStatistics.totalCount, 0, ".", ",")}}</span>
        </div>
        <div :id="chartId" style="min-width: 250px; height: 476px; margin: 0 auto;"></div>
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
import { format, numberFormat } from "@/util/util.js";
export default {
    name: "charts",
    props: {
        chartStatistics: {
            type: Object
        },
        id: {
            type: String
        },
        reload: {
            type: Number
        }
    },
    data: function() {
        return {
            chartId: this.id,
            chartSize: this.chartStatistics.chartSize,
            chart: "",
            numberFormat: numberFormat
        };
    },
    watch: {
        reload: {
            handler(item) {
                
                // echarts.init(document.getElementById(this.chartId)).showLoading();
                this.chartShow();
            }
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
            setTimeout(()=>{
                this.chart.dispose();
            }, 600)
        }
    },
    methods: {
        chartShow: function() {
            this.chart = echarts.init(document.getElementById(this.chartId));
            // setTimeout(() => {
            //     this.chart.hideLoading();
            // }, 600);
            let dayNum = this.chartStatistics.data.length;
            var showInterval = 0;
            if(0 <= dayNum && dayNum<28){
                showInterval = 0
            }else if(28 <= dayNum && dayNum < 60) {
                showInterval = 1
            }else if( 60 <= dayNum && dayNum <80){
                showInterval = 2
            }else if( 80 <= dayNum && dayNum < 100){
                showInterval = 4
            }else if(100 <= dayNum && dayNum < 120){
                showInterval = 6
            }else if(dayNum >= 120){
                showInterval = 8
            }
            let option = {
                legend: {
                    height: this.chartSize.height,
                    width: this.chartSize.width
                },
                noDataLoadingOption: {
                    text: "暂无数据",
                    effect: "bubble",
                    effectOption: {
                        effect: {
                            n: 0
                        }
                    }
                },
                default: {
                    text: "loading",
                    color: "#2878ff",
                    textColor: "#2878ff",
                    maskColor: "rgba(255, 255, 255, 0.8)",
                    zlevel: 0
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
                    top: 5,
                    bottom: 60
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
                        data: this.chartStatistics.transactionDataArr
                    }
                ],
                xAxis: {
                    data: this.chartStatistics.data,
                    axisLine: {
                        lineStyle: {
                            color: "#e9e9e9",
                            width: 2
                        }
                    },
                    axisLabel: {
                        interval: showInterval,
                        rotate: 0,
                        textStyle: {
                            color: "rgba(0,14,31,0.62)"
                        },
                        fontSize: 10
                    }
                },
                yAxis: {
                    axisLine: {
                        show: false,
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
.charts-title {
    margin-top: 29px;
    margin-bottom: 38px;
}
.total-count {
    font-size: 12px;
    color: #727476;
    background: #f6f6f6;
    border: 13px;
    padding: 2px 12px;
}
</style>
