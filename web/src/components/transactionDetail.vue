<template>
    <div>
        <el-tabs v-model="activeName"  @tab-click="handleClick">
            <el-tab-pane :label="$t('table.transactionInfo')" name="txInfo">
                <el-row v-for="item in txInfoList" :key="item">
                    <el-col :xs='24' :sm="24" :md="6" :lg="4" :xl="2">
                        <span>{{item}}：</span>
                    </el-col>
                    <el-col :xs='24' :sm="24" :md="18" :lg="20" :xl="22">
                        <template v-if="item=='input'">
                            <el-input v-show="showDecode" type="textarea" :autosize="autosizeMao" v-model="txInfoMap[item]">
                            </el-input>
                            <div v-show="!showDecode" class="input-data">
                                <div class="input-label">
                                    <span class="label">function</span>
                                    <!-- <span>{{funcData + "(" + abiType + ")"}}</span> -->
                                </div>
                                <div class="input-label">
                                    <span class="label">methodId</span>
                                    <!-- <span>{{methodId}}</span> -->
                                </div>
                                <div class="input-label">
                                    <span class="label">data</span>
                                    <!-- <el-table :data="inputData" v-if="inputData.length" style="display:inline-block;width:400px">
                                        <el-table-column prop="name" label="name" align="left" v-if="inputData[0].name"></el-table-column>
                                        <el-table-column prop="type" label="type" align="left"></el-table-column>
                                        <el-table-column prop="data" label="data" align="left" :show-overflow-tooltip="true">
                                            <template slot-scope="scope">
                                                <i class="wbs-icon-baocun font-12 copy-public-key" @click="copyPubilcKey(scope.row.data)" title="复制"></i>
                                                <span>{{scope.row.data}}</span>
                                            </template>
                                        </el-table-column>
                                    </el-table> -->
                                </div>
                            </div>
                            <!-- <el-button @click="deCodeInput">{{btnText}}</el-button> -->
                        </template>
                        <template v-else>
                            <p class="base-p">{{txInfoMap[item]}}</p>
                        </template>
                    </el-col>
                </el-row>
            </el-tab-pane>
            <el-tab-pane :label="$t('table.transactionReceipt')" name="txReceiptInfo">
                <el-row v-for="item in txReceiptInfoList" :key="item">
                    <el-col :xs='24' :sm="24" :md="6" :lg="4" :xl="2">
                        <span>{{item}}：</span>
                    </el-col>
                    <el-col :xs='24' :sm="24" :md="18" :lg="20" :xl="22">
                        <template v-if="item == 'logs'">
                            <p class="base-p" v-html="txInfoReceiptMap[item]"></p>
                            <!-- <el-input v-show="showReceiptDecode" type="textarea" :autosize="autosizeMao" v-model="txInfoReceiptMap[item]">
                            </el-input> -->
                            <div v-show="!showReceiptDecode">
                                解码后
                            </div>
                            <!-- <el-button @click="deCodeLogs">{{btnReceiptText}}</el-button> -->
                        </template>
                        <template v-else>
                            <p class="base-p">{{txInfoReceiptMap[item]}}</p>
                        </template>
                    </el-col>
                </el-row>

            </el-tab-pane>
        </el-tabs>
    </div>
</template>
<script>
import { queryTxInfo, queryTxReceiptInfo } from "@/util/api";
export default {
    name: "transactionDetail",
    props: {
        transHash: {
            type: String
        }
    },
    data() {
        return {
            activeName: "txInfo",
            showDecode: true,
            showReceiptDecode: true,
            btnText: "解码",
            btnReceiptText: "解码",
            autosizeMao: {
                minRows: 2,
                maxRows: 5
            },
            txInfoMap: {},
            txInfoReceiptMap: {},
            group: localStorage.getItem("groupId")
                ? localStorage.getItem("groupId")
                : "1",
            txInfoList: [
                "blockHash",
                "blockNumber",
                "gas",
                "from",
                "transactionIndex",
                "to",
                "nonce",
                "value",
                "hash",
                "gasPrice",
                "input"
            ],
            txReceiptInfoList: [
                "output",
                "blockHash",
                "gasUsed",
                "blockNumber",
                "contractAddress",
                "from",
                "transactionIndex",
                "to",
                "logsBloom",
                "transactionHash",
                "status",
                "logs"
            ]
        };
    },
    mounted() {
        this.getTxInfo();
    },
    methods: {
        getTxInfo() {
            queryTxInfo(this.group, this.transHash)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.txInfoMap = data;
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
                        message: this.$t('text.systemError')
                    });
                });
        },
        // deCodeInput() {
        //     this.showDecode = !this.showDecode;
        //     if (!this.showDecode) {
        //         this.btnText = "还原";
        //     } else {
        //         this.btnText = "解码";
        //     }
        // },
        // deCodeLogs() {
        //     this.showReceiptDecode = !this.showReceiptDecode;
        //     if (!this.showReceiptDecode) {
        //         this.btnReceiptText = "还原";
        //     } else {
        //         this.btnReceiptText = "解码";
        //     }
        // },
        decodeInputfun() {},
        decodeLogsFun() {},
        handleClick(tab) {
            if(tab.name =='txReceiptInfo'){
                this.getTxReceiptInfo()
            }
        },
        getTxReceiptInfo() {
            queryTxReceiptInfo(this.group, this.transHash)
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.txInfoReceiptMap = data;
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
                        message: this.$t('text.systemError')
                    });
                });
        }
    }
};
</script>
<style scoped>
.base-p {
    overflow: hidden;
    word-break: break-all;
    word-wrap: break-word;
}
</style>
