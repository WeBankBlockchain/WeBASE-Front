<template>
    <el-table :data="eventList" tooltip-effect="dark">
        <el-table-column type="expand" align="center">
            <template slot-scope="scope">
                <decode-log :logInfo="logInfo(scope.row)"></decode-log>
            </template>
        </el-table-column>
        <el-table-column prop="address" :label="$t('table.contractAddress')" show-overflow-tooltip  align="center">
            <template slot-scope="scope">
                <span>{{scope.row.address}}</span>
            </template>
        </el-table-column>
        <el-table-column prop="blockNumber" :label="$t('table.blockHeight')" show-overflow-tooltip width="100" align="center"></el-table-column>
        <el-table-column prop="transactionHash" :label="$t('table.transactionHash')" show-overflow-tooltip align="center">
            <template slot-scope="scope">
                <span>{{scope.row.transactionHash}}</span>
            </template>
        </el-table-column>
    </el-table>
</template>

<script>
import decodeLog from "@/components/decodeLog";
export default {
    name: 'eventResult',

    components: {
        decodeLog
    },

    props: ['checkEventResult', 'contractInfo'],

    data() {
        return {
            resultHead: [
                {
                    enName: "blockNumber",
                    name: this.$t('table.blockHeight'),
                    tdWidth: ''
                }
            ],
            eventList: this.checkEventResult[0],
        }
    },

    computed: {
    },

    watch: {
    },

    created() {
    },

    mounted() {
    },

    methods: {
        logInfo(row){
            var obj = {
                contractAbi: this.contractInfo.contractAbi, 
                eventName: this.checkEventResult[1].replace(/[(][^）]+[\))]/g,'')
            }
            var logInfo = Object.assign({},row, obj);
            return logInfo
        }
    }
}
</script>

<style scoped>
</style>
