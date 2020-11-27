
<template>
    <div v-show="visible" class="loading-wrap">
        <ul class="loading-box">
            加载中...
        </ul>
    </div>
</template>

<script>
export default {
    name: "Loading",
    data() {
        return {
            visible: false
        }
    }
}
</script>
<style  scoped>
.loading-wrap {
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.5);
}
.loading-box {
    position: absolute;
    left: 50%;
    top: 50%;
    height: 60px;
    width: 60px;
    margin-top: -30px;
    margin-left: -30px;
}
</style>
