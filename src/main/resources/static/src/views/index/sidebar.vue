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
    <div style="height: 100%;">
        <div style="height: 100%;background-color: #242e42" class="sidebar-content" ref='sidebarContent'>
            <div class="image-flex justify-center center" style="height: 54px;position:relative;" v-if="menuShowC">
                <!-- <img :src="maxLog" alt=""> -->
                <div style="color: #fff;">
                    节点控制台
                </div>
                <span class="sidebar-contract-icon">
                    <i class="el-icon-caret-left font-color-aeb1b5" @click="hideMune(true)" style="font-size: 18px;"></i>
                </span>
            </div>
            <div class="mini-sidebar-contract-icon" v-if="!menuShowC" style="padding-bottom:40px">
                <i class="el-icon-caret-right font-color-aeb1b5" @click="hideMune(false)" style="font-size: 18px;"></i>
            </div>
            <div class='sidebar-check-group' :style="{'padding-left': menuShowC ? '': '4px','font-size': menuShowC?'':'9px'}">
                <span class="group-content" @click="groupVisible = !groupVisible">
                    {{groupName}}
                    <ul v-show="groupVisible" :style="{'left': menuShowC ? '': '0'}">
                        <li v-for=" item in groupList" :key="item.group" @click="changeGroup(item)" :style="{'padding': menuShowC ? '': '0 5px'}">{{item.groupName}}</li>
                    </ul>
                </span>
                <i :class="[groupVisible?'el-icon-caret-top':'el-icon-caret-bottom','select-network']"></i>
            </div>
            <el-menu default-active="999" router class="el-menu-vertical-demo" text-color="#9da2ab" active-text-color="#1f83e7" active-background-color="#20293c" background-color="#242e42" @select="select" :collapse="!menuShowC" @open="handleOpen" @close="handleClose">
                <template v-for="(item,index) in routesList" v-if="item.menuShow">
                    <el-submenu v-if="!item.leaf" :index="`${index}`" ref="ele" class="">
                        <template slot="title">
                            <div :style="{'padding-left':  menuShowC ? '13px':''}">
                                <i :class="item.iconCls" :style="{'color': activeIndex == index ? '#1f83e7':''}"></i>
                                <span :class="{'font-color-37eef2': activeIndex == index}">{{item.name}}</span>
                            </div>
                        </template>

                        <el-menu-item v-for="term in item.children" :key="term.path" :index="term.path" v-if="term.menuShow" style="padding-left: 58px" :style="{'border-left':term.path == activeRoute ? '3px solid #1f83e7': '',
                                    'padding-left':term.path == activeRoute ? '55px': '58px',
                                    'background-color':term.path == activeRoute ? '#1e293e': '#242e42',}">
                            <span>{{term.name}}</span>
                        </el-menu-item>
                    </el-submenu>
                    <el-menu-item v-else-if="item.leaf&&item.children&&item.children.length" :index="item.children[0].path" :style="{'border-left':item.children[0].path == activeRoute ? '3px solid #1f83e7': '',
                                'padding-left':item.children[0].path == activeRoute ? '30px': '33px',
                                'background-color':item.children[0].path == activeRoute ? '#1e293e': '#242e42',}">
                        <i :class="item.iconCls"></i>
                        <span slot="title">{{item.children[0].name}}</span>
                    </el-menu-item>
                </template>
            </el-menu>
            <div :class="['sidebar-version',{'buttom-none':buttomNone,'font-12': !menuShowC, 'font-16': menuShowC}]">
                {{version}}
            </div>
        </div>
    </div>
</template>

<script>
// import maxLog from "@/../static/image/webase_35x35.png";
import router from "@/router";
import {
    queryGroup,
    queryClientVersion
} from "@/util/api";
import errcode from "@/util/errcode";
import Bus from "@/bus"
export default {
    name: "sidebar",
    props: ["minMenu"],
    data() {
        return {
            // maxLog: maxLog,
            activeIndex: 0,
            activeRoute: "",
            userRole: localStorage.getItem("root"),
            routesList: [],
            groupName: "",
            groupVisible: false,
            groupList: [],
            version: "",
            group: null,
            screenWidth: null,
            buttomNone: true,
        };
    },
    computed: {
        menuShowC() {
            if (this.minMenu) {
                return this.minMenu;
            } else {
                return false;
            }
        }
    },
    mounted: function() {   
        if (localStorage.getItem("groupId")) {
            this.group = localStorage.getItem("groupId");
        }
        if(localStorage.getItem("groupName")){
            this.groupName = localStorage.getItem("groupName");
        }
        this.$nextTick(function() {
            this.getGroup(this.getClientVersion);
            localStorage.setItem("sidebarHide", false);
            this.changeRouter();
            if(this.$refs.sidebarContent.offsetHeight > document.body.clientHeight){
                this.buttomNone = false
            }else{
                this.buttomNone = true
            }
        });
        let _this =this
        window.onresize = () => {
          return (() => {
            if( _this.$refs.sidebarContent.offsetHeight > document.body.clientHeight){
                 _this.buttomNone = false
            }else{
                 _this.buttomNone = true
            }
          })()
        }
    },
    methods: {
        changeGroup: function(val){
            this.group = val.group;
            this.groupName = val.groupName;
            // this.path = this.$route.path;
            localStorage.setItem('groupId', this.group);
            localStorage.setItem("groupName", this.groupName);
            Bus.$emit("changeGroup", this.group);
            this.getClientVersion();
        },
        getClientVersion: function() {
            queryClientVersion(this.group)
                .then(res => {
                    const { data, status, statusText } = res;
                    if (status === 200) {
                        this.version = data['FISCO-BCOS Version'];
                        localStorage.setItem('fisco-bcos-version', this.version)
                    } else {
                        this.$message({
                            type: "error",
                            message:  errcode.errCode[res.data.code].cn || "系统错误"
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: "系统错误"
                    });
                });
        },
        getGroup: function(callback) {
            queryGroup()
                .then(res => {
                    const { data, status, statusText } = res;
                    if (status === 200) {
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
                        if(!this.group){
                            this.group = this.groupList[0].group;
                            this.groupName = this.groupList[0].groupName;
                        }else{

                        }
                        localStorage.setItem("groupName",this.groupName)
                        localStorage.setItem('groupId', this.group);
                        localStorage.setItem("cluster", JSON.stringify(list));
                        callback()
                    } else {
                        this.$message({
                            type: "error",
                            message:  errcode.errCode[res.data.code].cn || "系统错误"
                        });
                    }
                })
                .catch(err => {
                    this.$message({
                        type: "error",
                        message: "系统错误"
                    });
                });
        },
        changeRouter: function() {
            let list = this.$router.options.routes;
            list.forEach(item => {
                if (item.name === "帐号管理") {
                    item.menuShow = false;
                }
            });
            list.forEach(item => {
                if (this.userRole === "admin" && item.name === "帐号管理") {
                    item.menuShow = true;
                }
            });
            this.routesList = list;
        },
        select: function(index, indexPath) {
            this.activeIndex = indexPath[0];
            this.activeRoute = index;
        },
        handleOpen(key, keyPath) {
        },
        handleClose(key, keyPath) {
        },
        hideMune: function(val) {
            this.$emit("sidebarChange", val);
            if (this.menuShow) {
                this.menuShow = false;
            } else {
                this.menuShow = true;
            }
            if (this.$route.path === "contract" && val) {
                localStorage.setItem("sidebarHide", true);
            } else {
                localStorage.setItem("sidebarHide", false);
            }
        },
        getAdmin: function() {}
    }
};
</script>

<style scoped>
.sidebar-content{
    position: relative;
    overflow-y: auto;
    overflow-x: hidden;
}
.sidebar-version{
    position: absolute;
    left:0;
    width: 100%;
    padding: 20px 0;
    text-align: center;
    color: #92a1b3;
    /* font-size: 16px; */
    border-top: 2px solid #20293c;
    background-color: #242e42;
    z-index: 999;
}
.buttom-none{
   bottom: 0; 
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
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
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
.sidebar-check-group{
    color: #92a1b3;
    padding: 20px 0 20px 0px;
    border-top: 2px solid #20293c;
    border-bottom: 2px solid #20293c;
    text-align: center;
}
.select-network {
    cursor: default;
}
.el-menu-vertical-demo>>>.is-active{
    background-color: #20293c !important;
}
.el-menu-vertical-demo {
    padding-top: 14px;
    border: none;
}
.el-menu-vertical-demo>>>.el-menu-item {
    font-size: 14px;
    color: #9da2ab;
    text-align: left;
}
.el-menu-vertical-demo>>>.el-submenu__title {
    padding-left: 33px;
}
.el-menu-item-group > ul > .el-menu-item {
    font-size: 14px;
    color: #9da2ab;
    text-align: left;
    padding-left: 57px !important;
    height: 46px;
    line-height: 46px;
}
/* .el-menu-vertical-demo>>> .el-icon-arrow-down:before {
    content: "\e60b"
} */
.sidebar-content>>>.el-menu--collapse {
    width: 56px;
}
.sidebar-content>>>.el-menu--collapse .is-active .el-tooltip {
    padding-left: 17px !important;
    background-color: #20293c;
}
.mini-sidebar-contract-icon {
    position: relative;
    text-align: center;
}
.mini-sidebar-contract-icon i {
    position: absolute;
    top: 20px;
    right: 10px;
    z-index: 9999;
    cursor: pointer;
}

.image-flex {
    display: -webkit-box !important;
    display: -webkit-flex !important;
    display: -ms-flexbox !important;
    display: flex !important;
    -webkit-flex-wrap: wrap;
    -ms-flex-wrap: wrap;
    flex-wrap: wrap;
}
.image-flex,
.image-flex *,
.image-flex :after,
.image-flex :before {
}
.image-flex.justify-center {
    -webkit-box-pack: center;
    -webkit-justify-content: center;
    -ms-flex-pack: center;
    justify-content: center;
}
.image-flex.center {
    -webkit-box-pack: center;
    -webkit-justify-content: center;
    -ms-flex-pack: center;
    justify-content: center;
    -webkit-box-align: center;
    -webkit-align-items: center;
    -ms-flex-align: center;
    align-items: center;
}
.sidebar-icon {
    font-size: 15px;
    padding-right: 5px;
}
.sidebar-contract-icon {
    position: absolute;
    display: inline-block;
    left: 180px;
    top: 18px;
    font-size: 12px;
    letter-spacing: 0;
    text-align: right;
    cursor: pointer;
    z-index: 6666;
}
.sidebar-contract-icon i {
    cursor: pointer;
}
</style>
