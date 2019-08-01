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
    <div class="network-wrapper">
        <span>选择区块链</span>
        <el-select v-model="value" placeholder="请选择" @change="change($event)" class="block-network" filterable clearable>
            <el-option v-for="item in options" :key="item.networkId" :label="item.networkName" :value="item.networkName">
            </el-option>
        </el-select>
        <span class="block-name">区块链Id</span>
        <el-input v-model="contractId" class="block-name-input" disabled></el-input>
        <div class="network-footer">
            <el-button @click="close" class="network-footer-close">取 消</el-button>
            <el-button type="primary" @click="submit" class="network-footer-sure">确 定</el-button>
        </div>
    </div>
</template>

<script>
import { networkList } from "@/util/api";
import router from "@/router";
export default {
    name: "networkDialog",
    props: ["networkDialog", "route"],
    data: function() {
        return {
            networkName: "",
            value: "",
            contractId: "",
            options: [],
            path: this.route || "",
            dialogShow: this.networkDialog || false
        };
    },
    mounted: function() {
        this.getchain();
    },
    methods: {
        handleClose: function() {
            this.dialogShow = false;
        },
        getchain: function() {
            networkList().then(res => {
                if (res.data.code === 0) {
                    this.options = res.data.data;
                }
            });
        },
        change: function(row) {
            localStorage.setItem("networkName", row);
            let networkId = "";
            for (let i = 0; i < this.options.length; i++) {
                if (this.options[i].networkName === row) {
                    networkId = this.options[i].networkId;
                }
            }
            this.contractId = networkId;
            localStorage.setItem("networkId", networkId);
        },
        close: function() {
            this.$emit("close", false);
        },
        submit: function() {
            router.go(0);
            this.handleClose();
            this.$emit("success");
        }
    }
};
</script>
<style scoped>
.network-wrapper {
    position: absolute;
    z-index: 999;
    background: #ffffff;
    border: 1px solid #eaedf3;
    box-shadow: 0 12px 27px 0 rgba(159, 166, 189, 0.33);
    border-radius: 6px;
    width: 144px;
    height: 197px;
    padding: 0 13px 20px 12px;
    right: 10px;
}
.network-wrapper>>>.el-input__inner {
    height: 30px;
    line-height: 30px;
    font-size: 12px;
    color: #36393d;
}
.network-wrapper > span {
    font-size: 12px;
    color: #737a86;
}
.block-network {
    top: -22px;
}
.block-name {
    position: relative;
    top: -40px;
}
.block-name-input {
    position: relative;
    top: -57px;
}
.network-footer {
    position: relative;
    top: -62px;
}
.network-footer>>>.el-button {
    padding: 7px 15px;
    font-size: 12px;
}
.network-footer>>>.el-button + .el-button {
    margin-left: 12px;
}
.network-footer-close {
    border: 1px solid #eaedf3;
    color: #36393d;
}
.network-footer-close,
.network-footer-sure {
    border-radius: 2px;
}
</style>
