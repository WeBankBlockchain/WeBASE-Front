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
        <div class="noData" style="color: #20D4D9" v-if="chartOption.data.contrastDataList.valueList.length === 0 && chartOption.data.contrastDataList.contractDataShow">暂无数据</div>
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
        },
        metricName: {
            type: String
        }
    },
    watch: {
        reload: function() {
            this.getChartData();
        }
    },
    data() {
        return {
            chartId: this.chartOption.metricType,
            chart: "",
            showContrast: false
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
            var _this = this;
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
            for (let i = 0; i < yList.length; i++) {
                if(yList[i] === null) {
                    yList[i] = 0
                }
            }
            yContrastList = this.chartOption.data.contrastDataList.valueList.map(item => {
                return item
            })
            for (let i = 0; i < yList.length; i++) {
                if(yContrastList[i] === null) {
                    yContrastList[i] = 0
                }
            }
            if(this.chartOption.data.contrastDataList.valueList.length > 0){
                yContrastTitle = this.$t('text.comparingByDay');
                this.showContrast = true;
            }else {
                this.showContrast = false;
            }
            let option = {
                title: {
                    text: this.metricName ==='nodes'? `${this.chartOption.metricName}` : `${this.chartOption.metricName}${this.chartOption.metricUint}(${this.chartOption.metricU})`,
                    textStyle: {
                        color: "#fff",
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
                    },
                    formatter: function(data) {
                        let str = ''
                        if(data.length === 2 ){
                            if(data[0]['data'] && data[1]['data']){
                                str = `
                                <span>${data[0]['name']}</span><br/>
                                <span>${data[0]['seriesName']}:${data[0]['data']}</span><br/>
                                <span>${data[1]['seriesName']}:${data[1]['data']}</span>
                                `
                            }else if(data[0]['data']===0&&data[1]['data']!=0){
                                str = `
                                <span>${data[0]['name']}</span><br/>
                                <span>${data[0]['seriesName']}:${_this.$t('text.noMetrics')}</span><br/>
                                <span>${data[1]['seriesName']}:${data[1]['data']}</span><br/>
                                `
                            }else if(data[0]['data']!=0&&data[1]['data']===0){
                                str = `
                                <span>${data[0]['name']}</span><br/>
                                <span>${data[0]['seriesName']}:${data[0]['data']}</span><br/>
                                <span>${data[1]['seriesName']}:${_this.$t('text.noMetrics')}</span><br/>
                                `
                            }else {
                                str = `
                                <span>${data[0]['name']}</span><br/>
                                <span>${_this.$t('text.noMetrics')}</span><br/>
                                `
                            }
                        }else if(data.length === 1) {
                            if(data[0]['data']){

                                str = `
                                <span>${data[0]['name']}</span><br/>
                                <span>${data[0]['seriesName']}:${data[0]['data']}</span><br/>
                                `
                            }else {
                                str = `
                                <span>${data[0]['name']}</span><br/>
                                <span>${data[0]['seriesName']}:${_this.$t('text.noMetrics')}</span><br/>
                                `
                            }
                        }
                        return str
                    }
                },
                legend: {
                    // data: ["显示日数据", yContrastTitle],
                    data: [
                        {
                            name: this.$t('text.showByDay'),
                            textStyle: {
                                color: '#1f8efa',
                            }
                        },
                        {
                            name: yContrastTitle,
                            textStyle: {
                                color: '#20D4D9'
                            }
                        }
                    ],
                     textStyle: {
                        color: '#fff'
                    },
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
                            yAxisIndex: 'none',
                            title: {
                                zoom: this.$t('title.zoom'),
                                back: this.$t('title.back')
                            },
                            iconStyle: {
                                borderColor: '#fff'
                            }
                        },
                        restore: {
                            title: this.$t('title.restore'),
                            iconStyle: {
                                borderColor: '#fff'
                            }
                        },
                        magicType: {
                            show: this.showContrast ? true : false, 
                            type: ['stack', 'tiled'],
                            title: {
                                stack: this.$t('title.stack'),
                                tiled: this.$t('title.tiled'),
                            },
                            iconStyle: {
                                borderColor: '#fff'
                            }
                        }
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
                        },
                        color: "#fff"
                    },
                    splitLine: {
                        show: true,
                        lineStyle: {
                            type: "dashed",
                            color: '#242e42'
                        }
                    },
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
                        lineStyle: { type: "dashed", color: "#242e42" }
                    },
                    axisTick: { show: false },
                    axisLabel: {
                        // textStyle: {
                        //     color: "rgba(0,14,31,0.62)"
                        // },
                        color: "#fff"
                    },
                    scale: this.metricName==='nodes'? true : false
                },
                series: [
                    {
                        name: this.$t('text.showByDay'),
                        type: "line",
                        data: yList,
                        smooth: true,
                        lineStyle: {
                            color: '#1f8efa'
                        }
                    },
                    {
                        name: yContrastTitle,
                        type: "line",
                        data: yContrastList,
                        smooth: true,
                        lineStyle: {
                            color: '#20D4D9'
                        }
                    }
                ]
            };
            this.chart.setOption(option, true);
            setTimeout(() => {
                window.onresize = () => {
                    
                    if(this.metricName === 'nodes'){
                        echarts.init(document.getElementById("pbftView")).resize();
                        echarts.init(document.getElementById("blockHeight")).resize();
                    }else {
                        echarts.init(document.getElementById("cpu")).resize();
                        echarts.init(document.getElementById("memory")).resize();
                        echarts.init(document.getElementById("disk")).resize();
                        echarts.init(document.getElementById("txbps")).resize();
                        echarts.init(document.getElementById("rxbps")).resize();
                    }
                    
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
        color: #1f8efa;
    }
</style>
