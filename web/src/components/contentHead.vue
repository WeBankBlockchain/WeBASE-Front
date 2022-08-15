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
    <div class="content-head-wrapper">
        <div class="content-head-title">
            <span class="content-head-icon" v-if="icon" @click="skip">
                <i class="wbs-icon-back"></i>
            </span>
            <span :class="{ 'font-color-9da2ab': headSubTitle}">{{title}}</span>
            <span v-show="headSubTitle" class="font-color-9da2ab">/</span>
            <span>{{headSubTitle}}</span>
        </div>
        <div class="content-head-lang">
            <lang-select class="right-menu-item hover-effect" />
        </div>
    </div>
</template>

<script>
import router from "@/router";
import LangSelect from './LangSelect'
import {
    queryGroup,
    queryClientVersion
} from "@/util/api";
import { delCookie } from "@/util/util";
export default {
    name: "conetnt-head",
    props: ["headTitle", "icon", "route", "headSubTitle"],
    components: {
        LangSelect
    },
    watch: {
        headTitle: function (val) {
            this.title = val;
        }
    },
    data: function () {
        return {
            title: this.headTitle,
            headIcon: this.icon || false,
            way: this.route || "",
            path: "",
            group: localStorage.getItem('groupId') ? localStorage.getItem('groupId') : "1",
            groupName: localStorage.getItem('groupName') ? localStorage.getItem('groupName') : "group1",
            groupVisible: false,
            groupList: localStorage.getItem("cluster")
                ? JSON.parse(localStorage.getItem("cluster"))
                : [],
            version: localStorage.getItem('fisco-bcos-version') ? localStorage.getItem('fisco-bcos-version') : ""
        };
    },
    mounted: function () {
        if (localStorage.getItem("groupId")) {
            this.group = localStorage.getItem("groupId");
        }
        if (this.$route.path == "/home") {
            // this.getGroup();
            // this.getClientVersion();
        }
    },
    methods: {
        getClientVersion: function () {
            queryClientVersion(this.group)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.version = data['FISCO-BCOS Version'];
                        localStorage.setItem('fisco-bcos-version', this.version)
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        getGroup: function (callback) {
            queryGroup()
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        let arr = data.sort(),
                            list = [];
                        for (let i = 0; i < arr.length; i++) {
                            list.push({
                                group: arr[i],
                                groupName: `group${arr[i]}`
                            });
                        }
                        this.groupList = list;
                        localStorage.setItem('groupId', this.group);
                        localStorage.setItem("cluster", JSON.stringify(list));
                        callback()
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        skip: function () {
            if (this.route) {
                router.push(this.way);
            } else {
                this.$router.go(-1);
            }
        },
        changeGroup: function (val) {
            this.group = val.group;
            this.groupName = val.groupName;
            this.path = this.$route.path;
            localStorage.setItem('groupId', this.group);
            localStorage.setItem("groupName", this.groupName);
            this.$emit("changeGroup", this.group);
            this.getClientVersion();
        },
    }
};
</script>
<style scoped>
.content-head-wrapper {
    width: calc(100%);
    min-width: 400px;
}
.content-head-wrapper::after {
    display: block;
    content: "";
    clear: both;
}
.content-head-icon {
    color: #fff;
    font-weight: bold;
    cursor: pointer;
}
.content-head-title {
    margin-left: 40px;
    float: left;
    font-size: 16px;
    color: #fff;
    font-weight: bold;
    line-height: 54px;
}
.content-head-network {
    float: right;
    padding-right: 10px;
    line-height: 54px;
}
.content-head-item {
    display: inline-block;
}
.group-content {
    position: relative;
    cursor: pointer;
}
.group-content ul {
    position: absolute;
    left: 20px;
    top: 35px;
    color: #666;
    z-index: 2;
    background-color: #fff;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.group-content ul li {
    height: 32px;
    line-height: 32px;
    cursor: pointer;
    padding: 0 20px;
}
.group-content ul li:hover {
    background-color: #ecf5ff;
    color: #66b1ff;
}
.browse-user {
    text-align: center;
    text-decoration: none;
    font-size: 12px;
    cursor: pointer;
    color: #cfd7db;
}
.sign-out-wrapper {
    text-align: center;
}
.sign-out {
    cursor: pointer;
    color: #ed5454;
}
.change-password {
    color: #2d5f9e;
    cursor: pointer;
}
.network-name {
    font-size: 12px;
    color: #9da2ab;
    padding: 3px 0px;
    /* border-right: 2px solid #e7ebf0; */
    margin-right: 16px;
}
.select-network {
    color: #2d5f9e;
    cursor: default;
}
.content-head-network a:nth-child(1) {
    text-decoration: none;
    outline: none;
    color: #cfd7db;
    padding-right: 15px;
    border-right: 1px solid #657d95;
    margin-right: 15px;
}
.content-head-network::after {
    display: block;
    content: "";
    clear: both;
}
a {
}
.dialog-text {
    word-break: break-all;
}
.content-head-lang {
    line-height: 54px;
    float: right;
}
.right-menu-item {
    padding: 0 20px;
}
.hover-effect {
    cursor: pointer;
    /* transition: background 0.3s; */
}
/* .hover-effect:hover {
    background: rgba(0, 0, 0, 0.025);
} */
</style>
