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
    <div id="app" class="web-font">
        <router-view></router-view>
    </div>
</template>

<script>
import {VueLoading } from 'vue-loading-template'
export default {
    name: "App",
    components: {
        VueLoading 
    },
    data() {
        return {
            load: this.$root.load,
            userForm: {
                password: ""
            },
            rules: {
                password: [
                    {
                        required: true,
                        message: "请输入组织名称",
                        trigger: "blur"
                    },
                    {
                        min: 1,
                        max: 12,
                        message: "长度在 1 到 12 个字符",
                        trigger: "blur"
                    }
                ]
            }
        };
    },
    methods: {}
};
</script>

<style>
#app {
    height: 100%;
    margin: 0;
    padding: 0;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #2c3e50;
    background-image: url("../static/image/bg.jpg");
    background-repeat: no-repeat;
}
ul,
li {
    list-style: none;
}
</style>
