<template>
    <div>
        {{logInfo}}
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
        decodeEvent(){
            let Web3EthAbi = require('web3-eth-abi');
            let contractAbi = JSON.parse(this.logInfo.contractAbi)
            let inputs = []
            contractAbi.forEach(item => {
                if(item.type == 'event' && item.name === this.logInfo.eventName){
                    console.log(item);
                    inputs = item.inputs
                }
            });
            let eventResult = Web3EthAbi.decodeLog(inputs, this.logInfo.data, this.logInfo.topics.slice(1));
            console.log(eventResult);
        }
    }
}
</script>

<style scoped>
</style>
