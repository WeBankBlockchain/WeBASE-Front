<template>
  <div>
    <el-tabs v-model="activeName" @tab-click="handleClick">
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
                </div>
                <div class="input-label">
                  <span class="label">methodId</span>
                </div>
                <div class="input-label">
                  <span class="label">data</span>
                </div>
              </div>
            </template>
            <template v-else-if="item=='to'">
              <span class="base-p">{{txInfoMap[item]}} </span><span v-if="txInfoMap[item]">{{txInfoMap[item] | contractSource}}</span>
              <el-tooltip v-if="txInfoMap.to==''||txInfoMap.to=='0x0000000000000000000000000000000000000000'" class="tip" effect="dark" :content="$t('contracts.toTip')" placement="top-start">
                <i class="el-icon-info"></i>
              </el-tooltip>
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
            <template v-if="item == 'logEntries'">
              <p class="base-p" v-html="txInfoReceiptMap[item]"></p>
              <div v-show="!showReceiptDecode">
                解码后
              </div>
            </template>
            <template v-else-if="item == 'status'">
              <span class="base-p" :style="{'color': txStatusColor(txInfoReceiptMap[item])}">{{txInfoReceiptMap[item]}}</span>
              <el-tooltip class="tip" effect="dark" :content="txStatusMessage(txInfoReceiptMap[item])" placement="top-start">
                <i class="el-icon-info"></i>
              </el-tooltip>
            </template>
            <template v-else-if="item=='to'">
              <span class="base-p">{{txInfoReceiptMap[item]}}</span> <span v-if="txInfoReceiptMap[item]">{{txInfoReceiptMap[item] | contractSource}}</span>
              <el-tooltip v-if="txInfoReceiptMap[item]==''||txInfoReceiptMap[item]=='0x0000000000000000000000000000000000000000'" class="tip" effect="dark" :content="$t('contracts.toTip')" placement="top-start">
                <i class="el-icon-info"></i>
              </el-tooltip>
            </template>
            <template v-else-if="item == 'contractAddress'">
              <span class="base-p">{{txInfoReceiptMap[item]}}</span> <span v-if="txInfoReceiptMap[item]">{{txInfoReceiptMap[item] | contractSource}}</span>
              <el-tooltip v-if="txInfoReceiptMap[item]==''||txInfoReceiptMap[item]=='0x0000000000000000000000000000000000000000'" class="tips" effect="dark" :content="$t('contracts.contractAddressTip')"
                placement="top-start">
                <i class="el-icon-info"></i>
              </el-tooltip>
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
      type: String,
    },
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
        maxRows: 5,
      },
      txInfoMap: {},
      txInfoReceiptMap: {},
      group: localStorage.getItem("groupId")
        ? localStorage.getItem("groupId")
        : "1",
      txInfoList: [
        // "blockHash",
        // "blockNumber",
        // "gas",
        "from",
        // "transactionIndex",
        "to",
        // "nonceRaw",
        // "value",
        "hash",
        // "gasPrice",
        "input",
      ],
      txReceiptInfoList: [
        "output",
        //"blockHash",
        "gasUsed",
        "blockNumber",
        "contractAddress",
        "from",
        // "transactionIndex",
        "to",
        //  "logsBloom",
        "transactionHash",
        "message",
        "status",
        "logEntries",
      ],
    };
  },
  mounted() {
    this.getTxInfo();
  },
  methods: {
    getTxInfo() {
      queryTxInfo(this.group, this.transHash)
        .then((res) => {
          const { data, status } = res;
          if (status === 200) {
            this.txInfoMap = data;
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
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
      if (tab.name == "txReceiptInfo") {
        this.getTxReceiptInfo();
      }
    },
    getTxReceiptInfo() {
      queryTxReceiptInfo(this.group, this.transHash)
        .then((res) => {
          const { data, status } = res;
          if (status === 200) {
            this.txInfoReceiptMap = data;
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.$message({
            type: "error",
            message: err.data || this.$t("text.systemError"),
          });
        });
    },
    txStatusColor(val) {
      if (val == "0x0") {
        return "#67C23A";
      } else {
        return "#F56C6C";
      }
    },
    txStatusMessage(val) {
      switch (val) {
        case 0:
          return this.$t("editor.None");
        case 1:
          return this.$t("editor.Unknown");
        case 2:
          return this.$t("editor.BadRLP");
        case 3:
          return this.$t("editor.InvalidFormat");
        case 4:
          return this.$t("editor.OutOfGasIntrinsic");
        case 5:
          return this.$t("editor.InvalidSignature");
        case 6:
          return this.$t("editor.InvalidNonce");
        case 7:
          return this.$t("editor.NotEnoughCash");
        case 8:
          return this.$t("editor.OutOfGasBase");
        case 9:
          return this.$t("editor.BlockGasLimitReached");
        case 10:
          return this.$t("editor.BadInstruction");
        case 11:
          return this.$t("editor.BadJumpDestination");
        case 12:
          return this.$t("editor.OutOfGas");
        case 13:
          return this.$t("editor.OutOfStack");
        case 14:
          return this.$t("editor.StackUnderflow");
        case 15:
          return this.$t("editor.NonceCheckFail");
        case 16:
          return this.$t("editor.BlockLimitCheckFail");
        case 17:
          return this.$t("editor.FilterCheckFail");
        case 18:
          return this.$t("editor.NoDeployPermission");
        case 19:
          return this.$t("editor.NoCallPermission");
        case 20:
          return this.$t("editor.NoTxPermission");
        case 21:
          return this.$t("editor.PrecompiledError");
        case 22:
          return this.$t("editor.RevertInstruction");
        case 23:
          return this.$t("editor.InvalidZeroSignatureFormat");
        case 24:
          return this.$t("editor.AddressAlreadyUsed");
        case 25:
          return this.$t("editor.PermissionDenied");
        case 26:
          return this.$t("editor.CallAddressError");
        case 27:
          return this.$t("editor.GasOverflow");
        case 28:
          return this.$t("editor.TxPoolIsFull");
        case 29:
          return this.$t("editor.TransactionRefused");
        case 30:
          return this.$t("editor.ContractFrozen");
        case 31:
          return this.$t("editor.AccountFrozen");
        case 32:
          return this.$t("editor.WASMValidationFailure");
        case 33:
          return this.$t("editor.WASMArgumentOutOfRange");
        case 34:
          return this.$t("editor.WASMUnreachableInstruction");
        case 10000:
          return this.$t("editor.AlreadyKnown");
        case 10001:
          return this.$t("editor.AlreadyInChain");
        case 10002:
          return this.$t("editor.InvalidChainId");
        case 10003:
          return this.$t("editor.InvalidGroupId");
        case 10004:
          return this.$t("editor.RequestNotBelongToTheGroup");
        case 10005:
          return this.$t("editor.MalformedTx");
        case 10006:
          return this.$t("editor.OverGroupMemoryLimit");
        default:
          return this.$t("editor.None");
      }
    },
  },
};
</script>
<style scoped>
.base-p {
  overflow: hidden;
  word-break: break-all;
  word-wrap: break-word;
}
</style>
