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
    <div class="contract-content" v-loading="loading">
        <v-content-head :headTitle="urlQuery.storeName" @changeGroup="changeGroup" :icon="true"></v-content-head>
        <div class="code-menu-wrapper" :style="{width: menuWidth+'px'}">
            <v-menu @change="changeCode($event)" ref="menu" v-show="menuHide" :urlQuery="urlQuery">
            </v-menu>
            <div class="move" @mousedown="dragDetailWeight($event)"></div>
        </div>
        <div :class="[!menuHide ?  'code-detail-wrapper' : 'code-detail-reset-wrapper']" :style="{width: contentWidth}">
            <v-code :changeStyle="changeWidth" :data="contractData" :show="showCode" :urlQuery="urlQuery"></v-code>
        </div>
    </div>
</template>

<script>
import menu from "./components/contractCatalog";
import codes from "./components/code";
import contentHead from "@/components/contentHead";
import Bus from "@/bus"
export default {
    name: "toolsContract",
    components: {
        "v-menu": menu,
        "v-code": codes,
        "v-content-head": contentHead
    },
    watch: {
        $route() {
            this.urlQuery = this.$root.$route.query;
        },
        menuHide(val) {
            if (val) {
                this.menuWidth = 180;
            } else {
                this.menuWidth = 0;
            }
        }
    },
    data() {
        return {
            contractData: {},
            showCode: false,
            menuHide: true,
            changeWidth: false,
            contractHide: false,
            menuWidth: 240,
            urlQuery: this.$root.$route.query,
            loading: false,
            storeName: ""
        };
    },
    computed: {
        contentWidth() {
            if (this.menuWidth) {
                return `calc(100% - ${this.menuWidth}px)`;
            } else {
                return `100%`;
            }
        }
    },
    beforeDestroy() {
        Bus.$off("changeGroup");
    },
    mounted() {
        Bus.$on("changeGroup", data => {
            this.changeGroup()
        })

    },
    methods: {
        changeGroup() {
            this.$refs.menu.getContractPaths()
        },
        dragDetailWeight(e) {
            let startX = e.clientX,
                menuWidth = this.menuWidth;
            document.onmousemove = e => {
                let moveX = e.clientX - startX;
                if (menuWidth + moveX > 180) {
                    this.menuWidth = menuWidth + moveX;
                }
            };

            document.onmouseup = e => {
                document.onmousemove = null;
                document.onmouseup = null;
            };
        },
        changeCode(val) {
            this.contractData = val;
            this.showCode = true;
        },
        uploadLoading(val) {
            this.loading = val;
        }
    }
};
</script>
<style scoped>
.contract-content {
    width: 100%;
    height: 100%;
    font-size: 0;
}
.contract-content::after {
    display: block;
    content: "";
    clear: both;
}
.code-menu-wrapper {
    float: left;
    position: relative;
    padding-left: 20px;
    height: calc(100% - 57px);
    font-size: 12px;
    box-sizing: border-box;
}
.move {
    position: absolute;
    top: 0;
    left: 98%;
    width: 3px;
    height: 100%;
    z-index: 9999;
    cursor: w-resize;
}
.menu-spread {
    position: relative;
    width: 31px;
    height: 47px;
    line-height: 47px;
    border: 1px solid #e7ebf0;
    border-bottom: 2px solid #e7ebf0;
    color: #aeb1b5;
    background-color: #fff;
    text-align: center;
    cursor: pointer;
}
.menu-spread i {
    font-size: 12px;
}
.code-detail-wrapper {
    float: left;
    height: calc(100% - 57px);
    padding-right: 20px;
    font-size: 12px;
    box-sizing: border-box;
}
.code-detail-reset-wrapper {
    float: left;
    height: calc(100% - 57px);
    padding-right: 20px;
    font-size: 12px;
    box-sizing: border-box;
}
.menu-drag {
    position: absolute;
    width: 36px;
    height: 36px;
    line-height: 32px;
    border: 1px solid #e8e8e8;
    left: 370px;
    top: 50%;
    background-color: #fff;
    cursor: pointer;
}
.version-selector {
    position: absolute;
    top: 1px;
    right: 5px;
    z-index: 1;
    box-sizing: border-box;
}
.version-selector >>> .el-select {
    width: 100%;
    max-width: 118px;
}
</style>