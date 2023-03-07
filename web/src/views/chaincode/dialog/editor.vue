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
  <el-dialog
    :title="$t('title.transactionReceipt')"
    :visible.sync="editorDialog"
    @close="modelClose"
    width="650px"
    top="10vh"
  >
    <div v-if="!transationData">{{ $t("text.noData") }}</div>
    <div
      v-if="transationData && !transationData.logs"
      slot
      style="overflow-y: scroll; height: 500px"
    >
      <json-viewer
        :value="transationData"
        :expand-depth="5"
        copyable
      ></json-viewer>
    </div>
    <div
      v-if="transationData && transationData.logs"
      slot
      style="overflow-y: scroll; height: 500px"
    >
      <div>{</div>
      <div v-for="(val, key) in transationData" style="padding-left: 10px">
        <div v-if="key != 'logs' && key != 'output'">
          <template v-if="key == 'status'">
            <span class="transation-title">{{ key }}:</span>
            <span :style="{ color: txStatusColor(val) }">{{ val }}</span>
          </template>
          <template v-else>
            <span class="transation-title">{{ key }}:</span>
            <span
              class="transation-content string-color"
              v-if="typeof val == 'string'"
              >"{{ val }}"</span
            >
            <span class="transation-content null-color" v-else-if="val === null"
              >{{ val }}null</span
            >
            <span
              class="transation-content"
              v-else-if="typeof val == 'object'"
              >{{ val }}</span
            >
            <span class="transation-content other-color" v-else>{{ val }}</span>
          </template>
        </div>

        <div v-else-if="key == 'output'">
          <span class="transation-title">{{ key }}:</span>
          <span class="transation-content string-color" v-if="showDecode"
            >"{{ val }}"</span
          >
          <div v-if="!showDecode" class="transation-data" style="width: 500px">
            <div class="input-label">
              <span class="label">function</span>
              <span>{{
                funcData + "(" + abiType + ")" + " " + outputType
              }}</span>
            </div>
            <div class="input-label">
              <span class="label">data:</span>
              <el-table
                :data="inputData"
                v-if="inputData.length"
                style="display: inline-block; width: 350px"
              >
                <el-table-column
                  prop="name"
                  label="name"
                  align="left"
                ></el-table-column>
                <el-table-column
                  prop="type"
                  label="type"
                  align="left"
                ></el-table-column>
                <el-table-column
                  prop="data"
                  label="data"
                  align="left"
                  :show-overflow-tooltip="true"
                >
                  <template slot-scope="scope">
                    <i
                      class="wbs-icon-baocun font-12 copy-public-key"
                      @click="copyPubilcKey(scope.row.data)"
                      :title="$t('title.copy')"
                    ></i>
                    <span>{{ scope.row.data }}</span>
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
        <div v-if="key == 'logs'">
          <span>{{ key }}:</span>
          <span v-if="!val.length">{{ val }}</span>
          <span v-if="val.length"
            >[
            <div v-for="item in val" style="padding-left: 10px">
              <div>{</div>
              <div style="padding-left: 10px">
                <div>
                  <span class="transation-title">removed:</span>
                  <span class="transation-content other-color">{{
                    item.removed
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">logIndex:</span>
                  <!-- <span
                    v-if="typeof item.logIndex == 'string'"
                    class="transation-content string-color"
                    >{{ item.logIndex }}</span
                  >
                  <span
                    v-else-if="item.logIndex === null"
                    class="transation-content null-color"
                    >{{ item.logIndex }}null</span
                  > -->
                  <span class="transation-content">{{ item.logIndex }}</span>
                </div>
                <div>
                  <span class="transation-title">transactionIndex:</span>
                  <!-- <span
                    v-if="item.transactionIndex == null||item.transactionIndex == undefined"
                    class="transation-content null-color"
                    >{{ item.transactionIndex }}null</span
                  > -->
                  <span class="transation-content">{{
                    item.transactionIndex
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">transactionHash:</span>
                  <span
                    v-if="typeof item.transactionHash == 'string'"
                    class="transation-content string-color"
                    >{{ item.transactionHash }}</span
                  >
                  <span
                    v-else-if="item.transactionHash === null"
                    class="transation-content null-color"
                    >{{ item.transactionHash }}null</span
                  >
                  <span v-else class="transation-content">{{
                    item.transactionHash
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">blockHash:</span>
                  <span
                    v-if="typeof item.blockHash == 'string'"
                    class="transation-content string-color"
                    >{{ item.blockHash }}</span
                  >
                  <span
                    v-else-if="item.blockHash === null"
                    class="transation-content null-color"
                    >{{ item.blockHash }}null</span
                  >
                  <span v-else class="transation-content">{{
                    item.blockHash
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">blockNumber:</span>
                  <span
                    v-if="item.blockNumber === null"
                    class="transation-content null-color"
                    >{{ item.blockNumber }}null</span
                  >
                  <span v-else class="transation-content">{{
                    item.blockNumber
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">address:</span>
                  <span
                    v-if="typeof item.address == 'string'"
                    class="transation-content string-color"
                    >{{ item.address }}</span
                  >
                  <span
                    v-else-if="item.address === null"
                    class="transation-content null-color"
                    >{{ item.address }}null</span
                  >
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
                  <span
                    class="transation-content string-color"
                    v-show="!item.eventDataShow && eventSHow"
                    >{{ item.data }}</span
                  >
                  <div
                    class="transation-data"
                    v-show="item.eventDataShow && eventSHow"
                  >
                    <el-table
                      class="input-data"
                      :data="item.eventLgData"
                      style="display: inline-block; width: 100%"
                    >
                      <el-table-column
                        prop="name"
                        width="150"
                        label="name"
                        align="left"
                      ></el-table-column>
                      <el-table-column
                        prop="data"
                        label="data"
                        align="left"
                        :show-overflow-tooltip="true"
                      >
                        <template slot-scope="scope">
                          <i
                            class="wbs-icon-baocun font-12 copy-public-key"
                            @click="copyPubilcKey(scope.row.data)"
                            :title="$t('title.copy')"
                          ></i>
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
                <div>
                  <span class="transation-title">type:</span>
                  <span
                    v-if="typeof item.type == 'string'"
                    class="transation-content string-color"
                    >{{ item.type }}</span
                  >
                  <span
                    v-else-if="item.type === null"
                    class="transation-content null-color"
                    >{{ item.type }}null</span
                  >
                  <span v-else class="transation-content">{{ item.type }}</span>
                </div>
                <div>
                  <span class="transation-title">topics:</span>
                  <span
                    v-if="typeof item.topics == 'string'"
                    class="transation-content string-color"
                    >{{ item.topics }}</span
                  >
                  <span
                    v-else-if="item.topics === null"
                    class="transation-content null-color"
                    >{{ item.topics }}null</span
                  >
                  <span v-else class="transation-content">{{
                    item.topics
                  }}</span>
                </div>
                <div>
                  <span class="transation-title">logIndexRaw:</span>
                  <span
                    v-if="typeof item.logIndexRaw == 'string'"
                    class="transation-content string-color"
                    >{{ item.logIndexRaw }}</span
                  >
                  <span
                    v-else-if="item.logIndexRaw === null"
                    class="transation-content null-color"
                    >{{ item.logIndexRaw }}null</span
                  >
                  <span v-else class="transation-content">{{
                    item.logIndexRaw
                  }}</span>
                </div>
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
  props: ["data", "show", "input", "editorOutput", "sendConstant"],
  data() {
    return {
      editorShow: true,
      aceEditor: null,
      transationData: this.data || null,
      modePath: "ace/mode/solidity",
      editorDialog: this.show || false,
      eventSHow: false,
      eventTitle: this.$t("text.txnEncodeBtn"),
      funcData: "",
      methodId: "",
      abiType: "",
      inputData: [],
      decodeData: "",
      showDecode: true,
      buttonTitle: this.$t("text.txnDecodeBtn"),
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
    if (this.transationData && this.transationData.logs) {
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
      //   var str = "[";
      //   arr.forEach(function(item,index,arr){
      //     str += item+',';
      // })
      //    var a= str.substring(0,(str.length-1));
      //  return a+"]";

      //  abc(arr){
      //     arr.forEach(function(item,index,arrs){
      //       if(Number(item)){
      //           arrs[index]=Number(item)
      //       }
      //   })
      // //   return arr
      // },
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
    decodeEvent() {
      for (let i = 0; i < this.transationData.logs.length; i++) {
        let data = {
          groupId: localStorage.getItem("groupId"),
          data: this.transationData.logs[i].topics[0],
        };
        getFunctionAbi(data)
          .then((res) => {
            if (res.data.code == 0 && res.data.data) {
              this.transationData.logs[i] = this.decodeLogs(
                res.data.data,
                this.transationData.logs[i]
              );
              setTimeout(() => {
                this.eventSHow = true;
              }, 200);
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
        list.topics.slice(1)
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
