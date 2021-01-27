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
    <div id="app" class="web-font" v-loading='loading'>
        <!-- <div v-if="!show"  >页面加载中...</div> -->
        <router-view v-if="show"></router-view>
    </div>
</template>

<script>
import { VueLoading } from 'vue-loading-template'
import {
    queryGroup
} from "@/util/api";
export default {
    name: "App",
    components: {
        VueLoading
    },
    data() {
        return {
            group: null,
            groupName: '',
            groupList: [],
            load: this.$root.load,
            show: false,
            loading: false,
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
    mounted() {
        this.getGroup()
    },
    methods: {
        getGroup() {
            this.loading = true
            queryGroup()
                .then(res => {
                    this.loading = false
                    const { data, status, statusText } = res;
                    if (status === 200 && data && data.length) {
                        let arr = data.sort((a, b) => {
                            return a - b
                        }),
                            list = [];
                        for (let i = 0; i < arr.length; i++) {
                            list.push({
                                group: arr[i],
                                groupName: `group${arr[i]}`
                            });
                        }
                        this.groupList = list;
                        if (!this.group) {
                            this.group = this.groupList[0].group;
                            this.groupName = this.groupList[0].groupName;
                        } else {

                        }
                        localStorage.setItem("groupName", this.groupName)
                        localStorage.setItem('groupId', this.group);
                        this.show = true
                        localStorage.setItem("cluster", JSON.stringify(list));
                    } else {
                        this.show = true
                        localStorage.setItem("groupName", "")
                        localStorage.setItem('groupId', "");
                        if(res.data.code){
                            this.$message({
                                type: "error",
                                message: this.$chooseLang(res.data.code)
                            });
                        }
                    }
                })
                .catch(err => {
                    console.log(err)
                    this.loading = false
                    this.show = true
                    localStorage.setItem("groupName", "")
                    localStorage.setItem('groupId', "");
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
    }
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
    background-color: #20293c;
}
ul,
li {
    list-style: none;
}
</style>
