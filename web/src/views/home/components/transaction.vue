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
    <div>
        <v-content-head :headTitle="'交易列表'" :icon="true" :route="'block'"></v-content-head>
        <div class="block-wrapper">
            <el-table :data="transactionList" class="block-table-content" :row-key="getRowKeys" :expand-row-keys="expands">
                <el-table-column type="expand" align="center">
                    <template slot-scope="scope">
                        <v-transaction-detail :data="scope.row"></v-transaction-detail>
                    </template>
                </el-table-column>
                <el-table-column prop="hash" label="交易哈希" align="center" :show-overflow-tooltip="true">
                </el-table-column>
                <el-table-column prop="blockNumber" label="块高" :show-overflow-tooltip="true" align="center" width="100"></el-table-column>
            </el-table>
        </div>
    </div>
</template>
<script>
import contentHead from "@/components/contentHead";
import transactionDetail from "./transactionDetail";
import { getblockHash } from "@/util/api";
import { getContractList } from "@/util/api";
import router from "@/router";
import errcode from '@/util/errcode'

export default {
    name: "transaction",
    components: {
        "v-content-head": contentHead,
        "v-transaction-detail": transactionDetail
    },
    data: function() {
        return {
            transactionList: [],
            expands: [],
            getRowKeys: function(row) {
                return row.hash;
            }
        };
    },
    mounted: function() {
        this.getTransaction();
    },
    methods: {
        getTransaction: function() {
            let data = {
                blockHash: this.$route.query.blockHash
            };
            getblockHash(data).then(res => {
                if (res.data.code === 0) {
                    this.transactionList = res.data.data.transactions;
                    // this.expands.push(this.transactionList[0].hash);
                }
            });
        }
    }
};
</script>
<style scoped>
.block-table-content {
    width: 100%;
    padding: 0px 40px 16px 41px;
    font-size: 12px;
}
.block-wrapper {
    margin: 30px 0 0 31px;
    background-color: #fff;
    box-shadow: 0 4px 12px 0 #dfe2e9;
    border-radius: 10px;
}
.block-table-content >>> .el-table__expanded-cell {
    padding: 12px 6px;
}
</style>

