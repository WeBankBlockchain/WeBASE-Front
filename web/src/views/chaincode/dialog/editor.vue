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
  <el-dialog :title="$t('title.transactionReceipt')" :visible.sync="editorDialog" @close="modelClose" width="650px" top="10vh" z-index='1000'>
    <div v-if="!transationData">{{ $t("text.noData") }}</div>
    <div v-if="transationData && !transationData.logEntries" slot :style="{ height: editorHeight + 'px' }" style="overflow-y: auto">
      <json-viewer :value="transationData" :expand-depth="5" copyable></json-viewer>
    </div>
    <div v-if="transationData && transationData.logEntries" slot :style="{ height: editorHeight + 'px' }" style="overflow-y: auto">
      <div>{</div>
      <div v-for="(val, key) in transationData" style="padding-left: 10px">
        <div v-if="key != 'logEntries' && key != 'output'&& key != 'input'">
          <template v-if="key == 'status'">
            <span class="transation-title">{{ key }}:</span>
            <span :style="{ color: txStatusColor(val) }">{{ val }}</span>
             <el-tooltip class="tip" effect="dark" :content="txStatusMessage(val)" placement="top-start">
              <i class="el-icon-info"></i>
            </el-tooltip>
          </template>
          <template v-else>
            <span class="transation-title">{{ key }}:</span>
            <span class="transation-content string-color" v-if="typeof val == 'string'">"{{ val }}"</span>
            <span class="transation-content null-color" v-else-if="val === null">{{ val }}null</span>
            <span class="transation-content" v-else-if="typeof val == 'object'">{{ val }}</span>
            <span class="transation-content other-color" v-else>{{ val }}</span>
          </template>
        </div>
        <div v-else-if='key == "input"'>
          <span class="transation-title">{{key}}:</span>
          <span class="transation-content string-color" v-if="showDecodeInput">"{{val}}"</span>
          <div v-if="!showDecodeInput" class="transation-data" style="width: 500px">
            <div class="input-label">
              <span class="label">function</span>
              <span>{{funcData + "(" + abiType + inputType + ")"}}</span>
              <el-tooltip v-if="funcData == '' " effect="dark" :content="$t('text.importContractTip')" placement="top-start">
                <i class="el-icon-info"></i>
              </el-tooltip>
            </div>
            <div class="input-label">
              <span class="label">methodId</span>
              <span>{{methodId}}</span>
            </div>
            <div class="input-label">
              <span class="label">data:</span>
              <el-table :data="inputDatas" v-if="inputDatas.length" style="display:inline-block;width:350px">
                <el-table-column prop="name" label="name" align="left"></el-table-column>
                <el-table-column prop="type" label="type" align="left"></el-table-column>
                <el-table-column prop="data" label="data" align="left" :show-overflow-tooltip="true">
                  <template slot-scope="scope">
                    <i class="wbs-icon-baocun font-12 copy-public-key" @click="copyPubilcKey(scope.row.data)" :title="$t('text.copy')"></i>
                    <span>{{abc(scope.row.data)}}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="item">
            <span class="label"></span>
            <el-button @click="decodeInputCheck" type="primary" v-if="!ifLiquid">{{inputTitle}}</el-button>
          </div>
        </div>
        <div v-else-if="key == 'output'">
          <span class="transation-title">{{ key }}:</span>
          <span class="transation-content string-color" v-if="showDecode">"{{ val }}"</span>
          <div v-if="!showDecode" class="transation-data" style="width: 500px">
            <div class="input-label">
              <span class="label">function</span>
              <span>{{
                funcData + "(" + abiType + ")" + " " + outputType
              }}</span>
            </div>
            <div class="input-label">
              <span class="label">data:</span>
              <el-table :data="inputData" v-if="inputData.length" style="display: inline-block; width: 350px">
                <el-table-column prop="name" label="name" align="left"></el-table-column>
                <el-table-column prop="type" label="type" align="left"></el-table-column>
                <el-table-column prop="data" label="data" align="left" :show-overflow-tooltip="true">
                  <template slot-scope="scope">
                    <i class="wbs-icon-baocun font-12 copy-public-key" @click="copyPubilcKey(scope.row.data)" :title="$t('title.copy')"></i>
                    <span>{{ abc(scope.row.data) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="item" v-show="inputButtonShow">
            <span class="label"></span>
            <el-button @click="decodeOutput" type="primary">{{
              buttonTitle
            }}</el-button>
          </div>
        </div>
        <div v-if="key == 'logEntries'">
          <span>{{ key }}:</span>
          <span v-if="!val.length">{{ val }}</span>
          <span v-if="val.length">[
            <div v-for="item in val" style="padding-left: 10px">
              <div>{</div>
              <div style="padding-left: 10px">
                <!-- <div>
                  <span class="transation-title">removed:</span>
                  <span class="transation-content other-color">{{
                    item.removed
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">logIndex:</span>
                  <span class="transation-content">{{ item.logIndex }}</span>
                </div>
                <div>
                  <span class="transation-title">transactionIndex:</span>
                  <span class="transation-content">{{
                    item.transactionIndex
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">transactionHash:</span>
                  <span v-if="typeof item.transactionHash == 'string'" class="transation-content string-color">{{ item.transactionHash }}</span>
                  <span v-else-if="item.transactionHash === null" class="transation-content null-color">{{ item.transactionHash }}null</span>
                  <span v-else class="transation-content">{{
                    item.transactionHash
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">blockHash:</span>
                  <span v-if="typeof item.blockHash == 'string'" class="transation-content string-color">{{ item.blockHash }}</span>
                  <span v-else-if="item.blockHash === null" class="transation-content null-color">{{ item.blockHash }}null</span>
                  <span v-else class="transation-content">{{
                    item.blockHash
                  }}</span>
                </div> -->
                <div>
                  <span class="transation-title">blockNumber:</span>
                  <span v-if="item.blockNumber === null" class="transation-content null-color">{{ item.blockNumber }}null</span>
                  <span v-else class="transation-content">{{
                    item.blockNumber
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">address:</span>
                  <span v-if="typeof item.address == 'string'" class="transation-content string-color">{{ item.address }}</span>
                  <span v-else-if="item.address === null" class="transation-content null-color">{{ item.address }}null</span>
                  <span v-else class="transation-content">{{
                    item.address
                  }}</span>
                </div>
                <div v-if="item.eventDataShow && eventSHow">
                  <span class="transation-title">eventName :</span>
                  <span class="transation-content">{{ item.eventName }}</span>
                </div>
                <div>
                  <span class="transation-title">data:</span>
                  <span class="transation-content string-color" v-show="!item.eventDataShow && eventSHow">{{ item.data }}</span>
                  <div class="transation-data" v-show="item.eventDataShow && eventSHow">
                    <el-table class="input-data" :data="item.eventLgData" style="display: inline-block; width: 100%">
                      <el-table-column prop="name" width="150" label="name" align="left"></el-table-column>
                      <el-table-column prop="data" label="data" align="left" :show-overflow-tooltip="true">
                        <template slot-scope="scope">
                          <i class="wbs-icon-baocun font-12 copy-public-key" @click="copyPubilcKey(scope.row.data)" :title="$t('title.copy')"></i>
                          <span>{{ abc(scope.row.data) }}</span>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                  <div class="item">
                    <span class="label"></span>
                    <el-button @click="decode(item)" type="primary">{{
                      eventTitle
                    }}</el-button>
                  </div>
                </div>
                <!-- <div>
                  <span class="transation-title">type:</span>
                  <span v-if="typeof item.type == 'string'" class="transation-content string-color">{{ item.type }}</span>
                  <span v-else-if="item.type === null" class="transation-content null-color">{{ item.type }}null</span>
                  <span v-else class="transation-content">{{ item.type }}</span>
                </div> -->
                <div>
                  <span class="transation-title">topic:</span>
                  <span v-if="typeof item.topic == 'string'" class="transation-content string-color">{{ item.topic }}</span>
                  <span v-else-if="item.topic === null" class="transation-content null-color">{{ item.topic }}null</span>
                  <span v-else class="transation-content">{{
                    item.topic
                  }}</span>
                </div>
                <!-- <div>
                  <span class="transation-title">logIndexRaw:</span>
                  <span v-if="typeof item.logIndexRaw == 'string'" class="transation-content string-color">{{ item.logIndexRaw }}</span>
                  <span v-else-if="item.logIndexRaw === null" class="transation-content null-color">{{ item.logIndexRaw }}null</span>
                  <span v-else class="transation-content">{{
                    item.logIndexRaw
                  }}</span>
                </div> -->
              </div>
              <div>}</div>
            </div>
            ]
          </span>
        </div>

      </div>
      <div>}</div>
    </div>
  </el-dialog>
</template>
<script>
import { getFunctionAbi } from "@/util/api";
// import func from 'vue-editor-bridge';
export default {
  name: "editor",
  props: ["data", "show", "input", "editorOutput", "sendConstant",'liquidChecks'],
  data() {
    return {
      ifLiquid: this.liquidChecks,
      editorShow: true,
      aceEditor: null,
      transationData: this.data || { logEntries: [""] },
      modePath: "ace/mode/solidity",
      editorDialog: this.show || false,
      eventSHow: false,
      eventTitle: this.$t("text.txnEncodeBtn"),
      funcData: "",
      methodId: "",
      abiType: "",
      inputType: [],
      inputData: [],
      inputDatas: [],
      decodeData: "",
      showDecode: true,
      showDecodeInput: true,
      buttonTitle: this.$t("text.txnDecodeBtn"),
      inputTitle: this.$t("text.txnEncodeBtn"),
      typesArray: this.input,
      inputButtonShow: true,
      editorHeight: "",
      outputType: null,
    };
  },
  mounted() {
    this.editorHeight = document.body.offsetHeight * 0.75;
    if (this.transationData.output == "0x") {
      this.inputButtonShow = false;
    } else {
      this.inputButtonShow = true;
    }
    this.decodeInputApi(this.transationData.input);
    if (this.transationData && this.transationData.logEntries) {
      this.decodeEvent();
    }
    if (!this.sendConstant) {
      if (this.typesArray && this.transationData.output != "0x") {
        this.decodefun();
      }
    }
  },
  methods: {
    abc(arr) {
      // return arr.replace(/\'/g, "");
      if (!Array.isArray(arr)) {
        return arr;
      }
      return "[" + arr.toString() + "]";
    },
    decodeInputCheck: function () {
      if (this.showDecodeInput) {
        this.showDecodeInput = false;
        this.inputTitle = this.$t("text.txnEncodeBtn");
      } else {
        this.showDecodeInput = true;
        this.inputTitle = this.$t("text.txnDecodeBtn");
      }
    },
    modelClose() {
      this.$emit("close");
    },
    decodeOutput() {
      if (this.showDecode) {
        this.showDecode = false;
        this.buttonTitle = this.$t("text.txnEncodeBtn");
      } else {
        this.showDecode = true;
        this.buttonTitle = this.$t("text.txnDecodeBtn");
      }
    },
    decodefun() {
      let Web3EthAbi = require("web3-eth-abi");
      if (this.typesArray) {
        this.typesArray.inputs.forEach((val, index) => {
          if (val && index < this.typesArray.inputs.length - 1) {
            this.abiType = this.abiType + val.type + " " + val.name + ",";
          } else if (val && index == this.typesArray.inputs.length - 1) {
            this.abiType = this.abiType + val.type + " " + val.name;
          }
        });
        this.funcData = this.typesArray.name;
        if (this.editorOutput.length) {
          this.decodeData = Web3EthAbi.decodeParameters(
            this.editorOutput,
            this.transationData.output
          );
          if (JSON.stringify(this.decodeData) != "{}") {
            for (const key in this.decodeData) {
              for (let index = 0; index < this.editorOutput.length; index++) {
                this.inputData[index] = {};
                this.inputData[index].name = this.editorOutput[index].name;
                this.inputData[index].type = this.editorOutput[index].type;
                this.inputData[index].data = this.decodeData[index];
              }
            }
          }

          let outputType = [];
          this.editorOutput.forEach((val, index) => {
            if (val && val.type && val.name) {
              outputType[index] = val.type + " " + val.name;
            } else if (val && val.name) {
              outputType[index] = val.name;
            } else if (val && val.type) {
              outputType[index] = val.type;
            } else if (val) {
              outputType[index] = val;
            }
          });
          this.outputType = `returns(${outputType.join(", ")})`;
        } else {
          this.outputType = "";
        }
        this.showDecode = false;
        this.buttonTitle = this.$t("text.txnEncodeBtn");
      }
    },
    decodeInput: function (input, abiData) {
      let Web3EthAbi = require("web3-eth-abi");

      this.methodId = input.substring(0, 10);
      // this.methodId = data;
      let inputDatas = "0x" + input.substring(10);
      if (abiData) {
        abiData.abiInfo = JSON.parse(abiData.abiInfo);
        abiData.abiInfo.inputs.forEach((val, index) => {
          if (val && val.type && val.name) {
            this.inputType[index] = val.type + " " + val.name;
          } else if (val && val.name) {
            this.inputType[index] = val.name;
          } else if (val && val.type) {
            this.inputType[index] = val.type;
          } else if (val) {
            this.inputType[index] = val;
          }
        });

        this.funcData = abiData.abiInfo.name;
        if (abiData.abiInfo.inputs.length) {
          this.decodeData = Web3EthAbi.decodeParameters(
            abiData.abiInfo.inputs,
            inputDatas
          );
          if (JSON.stringify(this.decodeData) != "{}") {
            for (const key in this.decodeData) {
              abiData.abiInfo.inputs.forEach((val, index) => {
                if (val && val.name && val.type) {
                  if (key === val.name) {
                    this.inputDatas[index] = {};
                    this.inputDatas[index].name = val.name;
                    this.inputDatas[index].type = val.type;
                    this.inputDatas[index].data = this.decodeData[key];
                  }
                } else if (val) {
                  if (index == key) {
                    this.inputDatas[index] = {};
                    this.inputDatas[index].type = val;
                    this.inputDatas[index].data = this.decodeData[key];
                  }
                }
              });
            }
          }
        }
        this.showDecodeInput = false;
        this.inputTitle = this.$t("text.txnEncodeBtn");
      }
    },
    decodeEvent() {
      for (let i = 0; i < this.transationData.logEntries.length; i++) {
        console.log(this.transationData.logEntries);
        console.log(this.transationData.logEntries[i].topic[0]);
        let data = {
          groupId: localStorage.getItem("groupId"),
          data: this.transationData.logEntries[i].topic[0],
        };
        getFunctionAbi(data)
          .then((res) => {
            if (res.data.code == 0 && res.data.data) {
              this.transationData.logEntries[i] = this.decodeLogs(
                res.data.data,
                this.transationData.logEntries[i]
              );
              let that = this;
              setTimeout(() => {
                that.eventSHow = true;
              }, 200);
              if(!this.ifLiquid){
              this.decodeInputApi(this.transationData.input);
              }
            } else if (res.data.code !== 0) {
              this.$message({
                type: "error",
                message: errcode.errCode[res.data.code].cn,
              });
            }
          })
          .catch((err) => {
            this.$message({
              type: "error",
              message: "系统错误！",
            });
          });
      }
    },
    decodeInputApi(param) {
      let data = {
        groupId: localStorage.getItem("groupId"),
        data: param,
      };
      getFunctionAbi(data)
        .then((res) => {
          if (res.data.code == 0 && res.data.data) {
            //注释liquid合约解码
            if(!this.ifLiquid){
            this.decodeInput(param, res.data.data); 
            }
            // this.decodeInput(param, res.data.data);
          } else if (res.data.code !== 0) {
            this.$message({
              type: "error",
              message: errcode.errCode[res.data.code].cn,
            });
          }
        })
        .catch((err) => {
          console.log(err)
          this.$message({
            type: "error",
            message: "系统错误！",
          });
        });
    },

    decodeLogs(eventData, data) {
      let Web3EthAbi = require("web3-eth-abi");
      let abi = "";
      eventData.abiInfo = JSON.parse(eventData.abiInfo);
      let list = data;
      list.eventTitle = this.$t("text.txnEncodeBtn");
      list.eventDataShow = true;
      list.eventButtonShow = true;
      list.eventName = eventData.abiInfo.name + "(";
      for (let i = 0; i < eventData.abiInfo.inputs.length; i++) {
        if (i == eventData.abiInfo.inputs.length - 1) {
          if (eventData.abiInfo.inputs[i]["indexed"]) {
            list.eventName =
              list.eventName +
              eventData.abiInfo.inputs[i].type +
              " " +
              "indexed" +
              " " +
              eventData.abiInfo.inputs[i].name;
          } else {
            list.eventName =
              list.eventName +
              eventData.abiInfo.inputs[i].type +
              " " +
              eventData.abiInfo.inputs[i].name;
          }
        } else {
          if (eventData.abiInfo.inputs[i]["indexed"]) {
            list.eventName =
              list.eventName +
              eventData.abiInfo.inputs[i].type +
              " " +
              "indexed" +
              " " +
              eventData.abiInfo.inputs[i].name +
              ",";
          } else {
            list.eventName =
              list.eventName +
              eventData.abiInfo.inputs[i].type +
              " " +
              eventData.abiInfo.inputs[i].name +
              ",";
          }
        }
      }
      list.eventName = list.eventName + ")";
      let eventResult = Web3EthAbi.decodeLog(
        eventData.abiInfo.inputs,
        list.data,
        list.topic.slice(1)
      );
      list.outData = {};
      list.eventLgData = [];
      for (const key in eventResult) {
        if (+key || +key == 0) {
          list.outData[key] = eventResult[key];
        }
      }
      if (
        eventData.abiInfo.inputs.length &&
        JSON.stringify(list.outData) != "{}"
      ) {
        for (const key in list.outData) {
          eventData.abiInfo.inputs.forEach((items, index) => {
            if (index == key) {
              list.eventLgData[index] = {};
              list.eventLgData[index].name = items.name;
              list.eventLgData[index].data = list.outData[key];
            }
          });
        }
      }
      return list;
    },
    copyPubilcKey(val) {
      if (!val) {
        this.$message({
          type: "fail",
          showClose: true,
          message: this.$t("notice.copyFailure"),
          duration: 2000,
        });
      } else {
        this.$copyText(val).then((e) => {
          this.$message({
            type: "success",
            showClose: true,
            message: this.$t("notice.copySuccessfully"),
            duration: 2000,
          });
        });
      }
    },
    decode(val) {
      if (val.eventDataShow) {
        this.$set(val, "eventDataShow", false);
        this.eventTitle = this.$t("text.txnDecodeBtn");
      } else {
        this.$set(val, "eventDataShow", true);
        this.eventTitle = this.$t("text.txnEncodeBtn");
      }
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
<style>
.transation-content {
  word-wrap: break-word;
  word-break: break-all;
}
.transation-title {
  color: #b5b5b5;
}
.string-color {
  color: #42b983;
}
.null-color {
  color: #e08331;
}
.other-color {
  color: #fc1e70;
}
.input-data {
  display: inline-block;
  width: 100%;
  padding: 10px;
  max-height: 200px;
  overflow: auto;
  word-break: break-all;
  word-wrap: break-word;
  box-sizing: border-box;
}
.transation-data {
  display: inline-block;
  width: 400px;
  vertical-align: top;
}
.label {
  vertical-align: top;
  padding-right: 5px;
}
</style>
