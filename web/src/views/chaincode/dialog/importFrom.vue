<template>
    <div>
        <p>{{$t('contracts.importContractDec')}}</p>
        <p>{{$t('contracts.importContractEg')}}</p>
        <p>
            https://github.com/0xcert/ethereum-erc721/src/contracts/tokens/nf-token-metadata.sol
        </p>
        <p>
            https://github.com/0xcert/ethereum-erc721/src/contracts/tokens/nf-token-metadata.sol
        </p>
    </div>
</template>

<script>
const fs = require('fs');
export default {
    name: 'ImportFrom',

    components: {

    },

    props: {
    },

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
        console.log(fs.createWriteStream);
    },

    methods: {
    }
}
</script>

<style scoped>
</style>
