<template>
    <div>
        {{content}}
        <!-- <el-table class="input-data" :data="eventLgData"  style="display:inline-block;width:100%;">
            <el-table-column prop="name" width="150" label="name" align="left"></el-table-column>
            <el-table-column prop="data" label="data" align="left" :show-overflow-tooltip="true">
                <template slot-scope="scope">
                    <i class="wbs-icon-copy font-12 copy-public-key" @click="copyPubilcKey(scope.row.data)" title="复制"></i>
                    <span>{{scope.row.data}}</span>
                </template>
            </el-table-column>
        </el-table> -->
    </div>
</template>

<script>
export default {
    name: 'decodeLog',

    components: {
    },

    props: ['logInfo'],

    data() {
        return {
            eventLgData: [],
            content: ''
        }
    },

    computed: {

    },

    watch: {
    },

    created() {
    },

    mounted() {
        this.decodeEvent()
    },

    methods: {
        decodeEvent() {
            let Web3EthAbi = require('web3-eth-abi');
            let contractAbi = JSON.parse(this.logInfo.contractAbi)
            let inputs = []
            contractAbi.forEach(item => {
                if (item.type == 'event' && item.name === this.logInfo.eventName) {
                    console.log(item);
                    inputs = item.inputs
                }
            });
            let eventResult = Web3EthAbi.decodeLog(inputs, this.logInfo.data, this.logInfo.topics.slice(1));
            let outData = {}, eventLgData = [];
            console.log('eventResult:', eventResult);
            console.log('inputs:', inputs);
            inputs.forEach(input => {
                input.data = eventResult[input['name']]
            })
            console.log('Newinputs:', inputs);
            let eventFun = []
            inputs.forEach(input => {
                eventFun.push(`${input.type} ${input.name} ${input.data}`)
                // console.log()
            })
            console.log(eventFun)
            this.content = `${this.logInfo.eventName} (${eventFun.join()})`
            // for (const key in eventResult) {
            //     console.log(key);
            //     if (+key || +key == 0) {
            //         outData[key] = eventResult[key];
            //     }
            // }
            // if (inputs.length && JSON.stringify(outData) != "{}") {
            //     for (const key in outData) {
            //         inputs.forEach((items, index) => {
            //             if (index == key) {
            //                 eventLgData[index] = {};
            //                 eventLgData[index].name = items.name;
            //                 eventLgData[index].data = outData[key];
            //             }
            //         });
            //     }
            // }
            // this.eventLgData = eventLgData

        },
        copyPubilcKey: function (val) {
            if (!val) {
                this.$message({
                    type: "fail",
                    showClose: true,
                    message: "key为空，不复制。",
                    duration: 2000
                });
            } else {
                this.$copyText(val).then(e => {
                    this.$message({
                        type: "success",
                        showClose: true,
                        message: "复制成功",
                        duration: 2000
                    });
                });
            }
        },
    }
}
</script>

<style scoped>
</style>
