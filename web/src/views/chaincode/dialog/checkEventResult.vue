<template>
    <div>
        <json-viewer :value="checkEventResult" :expand-depth=4 copyable sort boxed></json-viewer>
    </div>
</template>

<script>
export default {
    name: 'eventResult',

    components: {
    },

    props: ['checkEventResult'],

    data() {
        return {
            resultHead: [
                {
                    enName: "blockNumber",
                    name: this.$t('table.blockHeight'),
                    tdWidth: ''
                }
            ],

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
    }
}
</script>

<style scoped>
</style>
