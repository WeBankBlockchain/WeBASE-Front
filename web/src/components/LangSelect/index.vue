<template>
    <el-radio-group v-model="currentLang" @change="handleSetLanguage" size="mini">
        <el-radio-button label="zh">&ensp;中 &ensp;文&ensp;</el-radio-button>
        <el-radio-button label="en">English</el-radio-button>
    </el-radio-group>
</template>

<script>
import Bus from "@/bus"
export default {
    data(){
        return {
            currentLang: this.$store.getters.language
        }
    },
    mounted() {
        localStorage.setItem('lang', this.$store.getters.language)
    },
    methods: {
        handleSetLanguage(lang) {
            this.$i18n.locale = lang
            this.$store.dispatch('setLanguage', lang)
            localStorage.setItem('lang', this.$store.getters.language)
            Bus.$emit('chooselanguage', lang)
            // this.$message({
            //     message: 'Switch Language Success',
            //     type: 'success'
            // })
        }
    }
}
</script>
