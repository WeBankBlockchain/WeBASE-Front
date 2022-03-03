<template>
  <div class="rivate-key-management-wrapper">
    <div class="">
      <div class="">
        <el-form :model="contractEventForm" :rules="rules" ref="contractEventForm" class="demo-ruleForm" label-width="110px">
          <el-form-item :label="$t('table.contractAddress')" prop="contractAddress">
            <el-autocomplete v-model.trim="contractEventForm.contractAddress" :fetch-suggestions="querySearch" @select="selectAddress" style="width: 500px" clearable>
              <template slot-scope="{ item }">
                <div class="name">
                  {{ item.contractAddress }} {{ item.contractName }}
                </div>
              </template>
            </el-autocomplete>
            <span></span>
          </el-form-item>
          <el-form-item :label="$t('table.contractAbi')" prop="contractAbi">
            <el-input v-model="contractEventForm.contractAbi" :rows="1" type="textarea" style="width: 500px"></el-input>
          </el-form-item>
          <div class="block-wrapper">
            <el-form-item :label="$t('table.fromBlock')" prop="fromBlock">
              <el-input v-model.number="contractEventForm.fromBlock" clearable style="width: 195px"></el-input>
            </el-form-item>
            <el-form-item :label="$t('table.toBlock')" prop="toBlock">
              <el-input v-model.number="contractEventForm.toBlock" clearable style="width: 195px"></el-input>
            </el-form-item>
            <el-tooltip effect="dark" :content="$t('transaction.blockTips')" placement="top-start" style="margin-top:12px">
              <i class="el-icon-info contract-icon font-15"></i>
            </el-tooltip>
          </div>
          <el-form-item :label="$t('table.eventName')" prop="eventName" class="event-option">
            <el-select v-model="contractEventForm.eventName" :placeholder="$t('placeholder.selected')" style="width: 500px" @change="changeEventName" class="event-name">
              <el-option v-for="item in eventNameList" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
            <li v-for="item in inputList" class="event-info">
              <div v-if="item.indexed" style="position: relative">
                <div class="param-name">{{ item.name }}:</div>
                <el-input v-model="item.value" :placeholder="item.type" style="width: 500px" @input="(e) => (item.msgObj = isType(e, item.type))"></el-input>
                <span v-if="item.msgObj && !item.msgObj.is" class="font-color-ed5454 font-12 form-error" style="display: inline-block">
                  {{ item.msgObj.msg }}
                </span>
              </div>
            </li>
          </el-form-item>
          <el-form-item>
            <div class="text-center" style="width: 500px">
              <el-button type="primary" @click="submit('contractEventForm')" :loading="loading">{{ $t("dialog.search") }}</el-button>
              <span class="font-color-fff" v-text="searchMessage"></span>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <div class="module-wrapper">
      <div class="search-table" v-if="eventList.length > 0" style="padding-bottom: 13px">
        <el-table :data="eventList" tooltip-effect="dark" v-loading="loading">
          <el-table-column prop="log" :label="$t('table.blockHeight')" show-overflow-tooltip width="120" align="center">
            <template slot-scope="scope">
              <span>{{ scope.row.log.blockNumber }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="eventVal" :label="$t('table.eventValue')" show-overflow-tooltip align="center"></el-table-column>
        </el-table>
      </div>
      <div v-if="isSearch && eventList.length == 0" class="text-center" style="padding: 10px 0">
        <span class="font-color-fff">{{ $t("text.noData") }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import decodeLog from "@/components/decodeLog";
import {
  checkEvent,
  queryBlockNumber,
  listAddress,
  eventContractInfo,
} from "@/util/api";
import { validateEvent } from "@/util/validate";
import { isJson } from "@/util/util";
const Web3Utils = require("web3-utils");
import Bus from "@/bus";
export default {
  name: "eventCheck",

  components: {
    contentHead,
    decodeLog,
  },

  props: ["groupId"],

  data() {
    var isAddress = (rule, value, callback) => {
      if (value === "" || value == undefined || value == null) {
        callback();
      } else {
        if (!Web3Utils.isAddress(value) && value != "") {
          callback(new Error(this.$t("rule.inputIsAddress")));
        } else {
          callback();
        }
      }
    };
    var validateBlock = (rule, value, callback) => {
      if (value === "" || value == undefined || value == null) {
        callback();
      } else {
        if (!Number.isInteger(value)) {
          callback(new Error(this.$t("rule.inputIsNumber")));
        } else {
          if (value > 0 || value == -1) {
            callback();
          } else {
            callback(new Error(this.$t("rule.blockNumber")));
          }
        }
      }
    };
    var validateAbi = (rule, value, callback) => {
      if (value === "" || value == undefined || value == null) {
        callback();
      } else {
        if (!isJson(value)) {
          callback(new Error(this.$t("rule.inputIsJson")));
        } else {
          callback();
        }
      }
    };
    var validateEventName = (rule, value, callback) => {
      if (value === "" || value == undefined || value == null) {
        callback(new Error(this.$t("dialog.eventName")));
      } else {
        callback();
      }
    };
    return {
      loading: false,
      contractEventForm: {
        groupId: "",
        contractAbi: "",
        contractAddress: "",
        fromBlock: 1,
        toBlock: "",
        eventName: "",
      },
      inputList: [],
      addressList: [],
      contractId: "",
      abiId: "",
      eventList: [],
      restaurants: [],
      queryTypeParam: {},
      isSearch: false,
      searchMessage: "",
      rules: {
        contractAbi: [
          {
            required: true,
            message: this.$t("dialog.contractAbi"),
            trigger: "change",
          },
          { validator: validateAbi, trigger: "blur" },
        ],
        contractAddress: [
          {
            required: true,
            message: this.$t("dialog.contractAddress"),
            trigger: "change",
          },
          {
            validator: isAddress,
            trigger: "blur",
          },
        ],
        fromBlock: [
          {
            required: true,
            message: this.$t("dialog.fromBlock"),
            trigger: "blur",
          },
          {
            validator: validateBlock,
            trigger: "blur",
          },
        ],
        toBlock: [
          {
            required: true,
            message: this.$t("dialog.toBlock"),
            trigger: "blur",
          },
          {
            validator: validateBlock,
            trigger: "blur",
          },
        ],

        eventName: [
          // {
          //     required: true,
          //     message: this.$t('dialog.eventName'),
          //     trigger: "change"
          // },
          {
            validator: validateEventName,
            trigger: "change",
          },
        ],
      },
      eventNameList: [],
      //     eventNameList() {
      //     var options = []
      //     if (!this.contractEventForm.contractAbi) {
      //         this.contractEventForm.eventName = ''
      //         this.inputList = []
      //         return
      //     }
      //     try {
      //         var abiList = JSON.parse(this.contractEventForm.contractAbi)
      //         abiList.forEach(item => {
      //             if (item.type == 'event') {
      //                 var param = [], label = [];
      //                 item.inputs.forEach(it => {
      //                     param.push(`${it.type}`)
      //                     label.push(this.labelParam(it).replace(/(^\s*)|(\s*$)/g, ""))
      //                 })
      //                 options.push({
      //                     label: `${item.name}(${label.join(',')})`,
      //                     value: `${item.name}(${param.join(',')})`
      //                 })
      //             }
      //         });
      //     } catch (error) {

      //     }
      //     debugger
      //     return options
      // }
    };
  },

  computed: {
    // rules() {
    //     var isAddress = (rule, value, callback) => {
    //         if (value === '' || value == undefined || value == null) {
    //             callback();
    //         } else {
    //             if ((!Web3Utils.isAddress(value)) && value != '') {
    //                 callback(new Error(this.$t('rule.inputIsAddress')));
    //             } else {
    //                 callback();
    //             }
    //         }
    //     };
    //     var validateBlock = (rule, value, callback) => {
    //         if (value === '' || value == undefined || value == null) {
    //             callback();
    //         } else {
    //             if (!Number.isInteger(value)) {
    //                 callback(new Error(this.$t('rule.inputIsNumber')));
    //             } else {
    //                 if (value <= 0) {
    //                     callback(new Error(this.$t('rule.blockNumber')));
    //                 } else {
    //                     callback();
    //                 }
    //             }
    //         }
    //     };
    //     var validateAbi = (rule, value, callback) => {
    //         if (value === '' || value == undefined || value == null) {
    //             callback();
    //         } else {
    //             if (!isJson(value)) {
    //                 callback(new Error(this.$t('rule.inputIsJson')));
    //             } else {
    //                 callback();
    //             }
    //         }
    //     };
    //            var validateEventName = (rule, value, callback) => {
    //                debugger
    //         if (value === '' || value == undefined || value == null) {
    //            callback(new Error(this.$t('dialog.eventName')));
    //         } else {
    //                callback();
    //         }
    //     };
    //     var obj = {
    //         contractAbi: [
    //             {
    //                 required: true,
    //                 message: this.$t('dialog.contractAbi'),
    //                 trigger: "change"
    //             },
    //             { validator: validateAbi, trigger: 'blur' }
    //         ],
    //         contractAddress: [
    //             {
    //                 required: true,
    //                 message: this.$t('dialog.contractAddress'),
    //                 trigger: "change"
    //             },
    //             {
    //                 validator: isAddress, trigger: 'blur'
    //             }
    //         ],
    //         fromBlock: [
    //             {
    //                 required: true,
    //                 message: this.$t('dialog.fromBlock'),
    //                 trigger: "blur"
    //             },
    //             {
    //                 validator: validateBlock, trigger: 'blur'
    //             }
    //         ],
    //         toBlock: [
    //             {
    //                 required: true,
    //                 message: this.$t('dialog.toBlock'),
    //                 trigger: "blur"
    //             },
    //             {
    //                 validator: validateBlock, trigger: 'blur'
    //             }
    //         ],
    //         eventName: [
    //             // {
    //             //     required: true,
    //             //     message: this.$t('dialog.eventName'),
    //             //     trigger: "change"
    //             // },
    //               {
    //                 validator: validateEventName, trigger: 'change'
    //             }
    //         ]
    //     }
    //     return obj
    // },
    // eventNameList() {
    //     var options = []
    //     if (!this.contractEventForm.contractAbi) {
    //         this.contractEventForm.eventName = ''
    //         this.inputList = []
    //         return
    //     }
    //     try {
    //         var abiList = JSON.parse(this.contractEventForm.contractAbi)
    //         abiList.forEach(item => {
    //             if (item.type == 'event') {
    //                 var param = [], label = [];
    //                 item.inputs.forEach(it => {
    //                     param.push(`${it.type}`)
    //                     label.push(this.labelParam(it).replace(/(^\s*)|(\s*$)/g, ""))
    //                 })
    //                 options.push({
    //                     label: `${item.name}(${label.join(',')})`,
    //                     value: `${item.name}(${param.join(',')})`
    //                 })
    //             }
    //         });
    //     } catch (error) {
    //     }
    //     return options
    // }
  },

  watch: {
    $route() {
      this.queryInit();
    },
    contractEventForm(e) {
      console.log(e);
      var options = [];
      try {
        var abiList = JSON.parse(e.contractAbi);
        abiList.forEach((item) => {
          if (item.type == "event") {
            var param = [],
              label = [];
            item.inputs.forEach((it) => {
              param.push(`${it.type}`);
              label.push(this.labelParam(it).replace(/(^\s*)|(\s*$)/g, ""));
            });
            options.push({
              label: `${item.name}(${label.join(",")})`,
              value: `${item.name}(${param.join(",")})`,
            });
          }
        });
        this.eventNameList = options;
      } catch (error) {}
    },
  },

  created() {},

  mounted() {
    this.$on("changeGroup", (data) => {
      this.groupId = data;
      this.changeGroup();
    });
    if (localStorage.getItem("groupId")) {
      this.queryInit();
    }
  },

  methods: {
    queryInit() {
      if (this.$route && this.$route.query.type) {
        this.queryTypeParam = this.$route.query;
        this.queryContractAbi(this.queryTypeParam);
      } else {
        this.contractEventForm = {
          contractAbi: "",
          contractAddress: "",
          fromBlock: 1,
        };
      }
      this.queryAllAddress();
      this.getBlockNumber();
    },
    changeGroup() {
      this.contractEventForm = {
        groupId: "",
        contractAbi: "",
        contractAddress: "",
        fromBlock: 1,
        toBlock: "",
        eventName: "",
      };
      this.queryAllAddress();
      this.getBlockNumber();
    },
    queryAllAddress() {
      listAddress(this.groupId).then((res) => {
        if (res.data.code === 0) {
          var addressList = res.data.data;
          addressList.forEach((item) => {
            item.value = item.contractAddress;
          });
          this.restaurants = addressList;
        } else {
          this.$message({
            type: "error",
            message: this.$chooseLang(res.data.code),
          });
        }
      });
    },
    queryContractAbi(queryParam) {
      eventContractInfo(queryParam).then((res) => {
        if (res.data.code === 0) {
          this.contractEventForm.contractAddress =
            res.data.data.contractAddress;
          this.contractEventForm.contractAbi = res.data.data.contractAbi;
          var options = [];
          try {
            var abiList = JSON.parse(this.contractEventForm.contractAbi);
            abiList.forEach((item) => {
              if (item.type == "event") {
                var param = [],
                  label = [];
                item.inputs.forEach((it) => {
                  param.push(`${it.type}`);
                  label.push(this.labelParam(it).replace(/(^\s*)|(\s*$)/g, ""));
                });
                options.push({
                  label: `${item.name}(${label.join(",")})`,
                  value: `${item.name}(${param.join(",")})`,
                });
              }
            });
            this.eventNameList = options;
          } catch (error) {}
        } else {
          this.$message({
            type: "error",
            message: this.$chooseLang(res.data.code),
          });
        }
      });
    },
    changeEventName($event) {
      this.$set(this.contractEventForm, "eventName", $event);
      //this.contractEventForm.eventName=$event
      var eventName = $event.replace(/\((.+?)\)/g, "");
      var abiList = JSON.parse(this.contractEventForm.contractAbi);
      var eventListInfo = [];
      abiList.forEach((item) => {
        if (item.type === "event") {
          eventListInfo.push(item);
        }
      });
      var inputList = [];
      eventListInfo.forEach((item) => {
        if (item.name === eventName) {
          inputList = item.inputs;
        }
      });
      inputList.forEach((item) => {
        if (item.indexed) {
          item.value = null;
        }
      });
      this.inputList = inputList;
    },
    submit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let indexedArr = [];
          this.inputList.forEach((item) => {
            if (item.msgObj) {
              indexedArr.push(item.msgObj.is);
            }
          });
          if (indexedArr.includes(false)) {
            return;
          }
          this.queryAdd();
        } else {
          return false;
        }
      });
    },
    queryAdd() {
      try {
        JSON.parse(this.contractEventForm.contractAbi);
      } catch (error) {
        this.$message({
          type: "error",
          message: error,
        });
        return;
      }
      var topicObj = {
        eventName: this.contractEventForm.eventName,
      };
      var indexedList = [];
      this.inputList.forEach((item, index) => {
        if (item.indexed) {
          indexedList.push(item);
        }
      });
      indexedList.forEach((item, index) => {
        topicObj[`indexed${index + 1}`] = {
          type: item.type,
          value: item.value,
        };
      });
      this.loading = true;
      let param = Object.assign({}, this.contractEventForm, {
        contractAbi: JSON.parse(this.contractEventForm.contractAbi),
        topics: topicObj,
        groupId: this.groupId,
      });
      delete param.eventName;
      checkEvent(param)
        .then((res) => {
          this.loading = false;
          this.isSearch = true;
          if (res.data.code === 0) {
            this.searchMessage = this.$t("text.searchMessage");
            setTimeout(() => {
              this.searchMessage = "";
            }, 3000);
            var eventList = res.data.data;
            var newEventList = [];
            if (!eventList.length) {
              this.eventList = newEventList;
              return;
            }
            eventList.forEach((item) => {
              if (item != null) {
                newEventList.push(item);
              }
            });
            if (newEventList && newEventList.length) {
              newEventList.forEach((item) => {
                item.eventVal = this.decodeEvent(item.log, item.data);
              });
            }
            this.eventList = newEventList;
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
    isType(val, type) {
      return validateEvent(type, val);
    },
    isnumber(val) {
      // console.log(val, type)
      // var uintReg = RegExp(/uint/);
      // var bytesReg = RegExp(/bytes/);
      // var boolReg = RegExp(/bool/);
      // if(uintReg.test(type) || bytesReg.test(type)){
      //     val = val.replace(/[^0-9]/gi, "");
      //     return val;
      // }

      return val;
    },
    transform(type, val) {
      if (!val) {
        return (val = null);
      }
      var uintReg = RegExp(/uint/);
      var bytesReg = RegExp(/bytes/);
      var boolReg = RegExp(/bool/);
      if (uintReg.test(type) || bytesReg.test(type)) {
        try {
          return (val = +val);
        } catch (error) {
          console.log(console.error());
        }
      } else if (boolReg.test(type)) {
        return (val = eval(val.toLowerCase()));
      } else {
        return val;
      }
    },
    logInfo(row) {
      var obj = {
        contractAbi: this.contractEventForm.contractAbi,
        eventName: this.contractEventForm.eventName.replace(
          /[(][^）]+[\))]/g,
          ""
        ),
      };
      var logInfo = Object.assign({}, row, obj);
      return logInfo;
    },
    labelParam(it) {
      if (it.indexed) {
        return `${it.type} indexed ${it.name} `;
      } else {
        return `${it.type} ${it.name} `;
      }
    },
    decodeEvent(paramVal, dataList) {
      let Web3EthAbi = require("web3-eth-abi");
      let contractAbi = JSON.parse(this.contractEventForm.contractAbi);
      let inputs = [];
      contractAbi.forEach((item) => {
        if (
          item.type == "event" &&
          item.name ===
            this.contractEventForm.eventName.replace(/[(][^）]+[\))]/g, "")
        ) {
          inputs = item.inputs;
        }
      });
      let eventResult = Web3EthAbi.decodeLog(
        inputs,
        paramVal.data,
        paramVal.topics.slice(1)
      );
      let outData = {},
        eventLgData = [];
      inputs.forEach((input) => {
        input.data = eventResult[input["name"]];
      });
      let eventFun = [];
      inputs.forEach((input) => {
        eventFun.push(`${input.data}`);
      });
      return `${this.contractEventForm.eventName.replace(
        /[(][^）]+[\))]/g,
        ""
      )} (${dataList.join()})`;
    },
    copyKey(val) {
      if (!val) {
        this.$message({
          type: "fail",
          showClose: true,
          message: this.$t("text.copyErrorMsg"),
          duration: 2000,
        });
      } else {
        this.$copyText(val).then((e) => {
          this.$message({
            type: "success",
            showClose: true,
            message: this.$t("text.copySuccessMsg"),
            duration: 2000,
          });
        });
      }
    },
    getBlockNumber: function () {
      queryBlockNumber(this.groupId)
        .then((res) => {
          const { data, status } = res;
          if (status === 200) {
            this.$set(this.contractEventForm, "toBlock", data);
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
    createFilter(queryString) {
      return (restaurant) => {
        return (
          restaurant.contractAddress
            .toLowerCase()
            .indexOf(queryString.toLowerCase()) === 0
        );
      };
    },
    querySearch(queryString, cb) {
      var restaurants = this.restaurants;
      var results = queryString
        ? restaurants.filter(this.createFilter(queryString))
        : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    selectAddress(item) {
      let queryParam = {
        groupId: this.groupId,
        type: item.type,
        contractAddress: item.contractAddress,
      };
      //  this.contractEventForm.eventName = ''
      // this.$refs['contractEventForm'].clearValidate()
      this.$set(this.contractEventForm, "eventName", "");

      this.queryContractAbi(queryParam);
    },
  },
};
</script>

<style scoped>
.event-info {
  color: #fff;
}
.event-option {
  /* display: flex; */
}
.event-name >>> .el-form-item__content {
  display: flex;
}
.block-wrapper {
  display: flex;
}
.param-name {
  height: 42px;
  line-height: 42px;
}
.form-error {
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
  position: absolute;
  top: 100%;
  left: 0;
}
</style>
