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
  <div class="send-wrapper">
    <div class="font-color-ed5454 text-center" v-if="sendErrorMessage">
      {{ sendErrorMessage }}
    </div>
    <div class="send-body">
      <div class="send-item">
        <span class="send-item-title">{{ $t("text.contractName") }}:</span>
        <span class="font-color-fff">{{ data.contractName }}</span>
      </div>
      <div class="send-item">
        <span class="send-item-title">CNS：</span>
        <el-checkbox v-model="isCNS" @change="changeCns"></el-checkbox>
      </div>
      <div v-if="isCNS" class="send-item">
        <span class="send-item-title"></span>
        <el-input v-model.trim="cnsName" style="width: 400px" :placeholder="$t('dialog.cnsName')">
          <template slot="prepend">
            <span>name</span>
          </template>
        </el-input>
      </div>
      <div v-if="isCNS" class="send-item">
        <span class="send-item-title"></span>
        <el-input v-model.trim="cnsVersion" style="width: 400px" :placeholder="$t('dialog.cnsVersion')">
          <template slot="prepend">
            <span>version</span>
          </template>
        </el-input>
      </div>
      <div v-else class="send-item">
        <span class="send-item-title">{{ $t("text.contractAddress") }}:</span>
        <el-input v-model.trim="contractAddress" style="width: 400px; margin-bottom: 4px" :placeholder="$t('placeholder.selectedContractAddress')"></el-input>
        <el-tooltip class="item" effect="dark" :content="$t('title.txnContractAddExp')" placement="top-start">
          <i class="el-icon-info"></i>
        </el-tooltip>
      </div>
      <div class="send-item">
        <span class="send-item-title">{{ $t("text.sendFunction") }}:</span>
        <el-select v-model="transation.funcName" filterable :placeholder="$t('placeholder.functionName')" v-if="funcList.length > 0" popper-class="func-name" @change="changeFunc" style="width: 400px">
          <el-option :label="item.name" :key="item.funcId" :value="item.funcId" v-for="item in funcList">
            <span :class="{ 'func-color': !item.constant }">{{
              item.name
            }}</span>
          </el-option>
        </el-select>
      </div>
      <div v-show="showUser" class="send-item">
        <span class="send-item-title">{{ $t("text.acountAddress") }}:</span>
        <el-select v-model="transation.userName" style="width: 400px; margin-bottom: 4px" class="plac-op" @change="changeId">
          <el-option :label="item.address" :value="item.address" :key="item.address" v-for="(item, index) in userList">
            <span class="font-12">{{ item.userName }}</span>
            <span>{{ item.address }}</span>
          </el-option>
        </el-select>
        <span class="user-explain" v-show="userId">
          (<span class="ellipsis-info">{{ userId }}</span>)
        </span>
        <el-button v-if="isShowAddUserBtn" type="text" size="mini" @click="createUser()">{{ $t("privateKey.addUser") }}</el-button>
      </div>
      <div v-show="form.pramasData.length" class="send-item">
        <el-form class="send-item" v-show="form.pramasData.length" style="line-height: 25px" :rules="rules" :model="form" ref="sendTransation">
          <span class="send-item-title" style="position: relative; top: 5px">{{ this.$t("contracts.params") }}:</span>
          <div v-for="(item, index) in form.pramasData" :key='index'>
            <el-form-item style="position: relative; top: -25px"  :prop="'pramasData.'+index+'.value'"  :rules='rules[item.type]'>
            <span class="send-item-title"></span>
            <template v-if="item.type == 'string'">
              <el-input v-model="item.value" style="width: 400px" :placeholder="item.type">
                <template slot="prepend">
                  <span class="">{{ item.name }}</span>
                </template>
              </el-input>
            </template>
            <template v-else>
              <el-input v-model="item.value" style="width: 400px" :placeholder="placeholderText(item.type)">
                <template slot="prepend">
                  <span class="">{{ item.name }}</span>
                </template>
              </el-input>
            </template>
          </el-form-item>
          </div>
      
          <div style="padding: 5px 0 0 28px; color: 'gray'">
            <i class="el-icon-info" style="padding-right: 4px"></i>{{ this.$t("contracts.paramsInfo") }}
          </div>
        </el-form>
      </div>
    </div>
    <div class="text-right send-btn java-class">
      <el-button @click="close">{{ $t("dialog.cancel") }}</el-button>
      <el-button type="primary" @click="submit('transation')" :disabled="buttonClick">{{ $t("dialog.confirm") }}</el-button>
    </div>
    <el-dialog :title="$t('dialog.addUsername')" :visible.sync="creatUserNameVisible" :before-close="createUserClose" class="dialog-wrapper" width="640px" :center="true" :append-to-body="true">
      <v-createUser @close="createUserClose"></v-createUser>
    </el-dialog>
  </div>
</template>
<script>
import { sendTransation, queryLocalKeyStores, findCnsInfo } from "@/util/api";
import createUser from "@/views/toolsContract/components/createUser";
import { isJson } from "@/util/util";
export default {
  components: {
    "v-createUser": createUser,
  },
  name: "sendTransation",
  props: ["data", "dialogClose", "abi", "version", "sendErrorMessage"],
  data: function () {
    let intEight = (rule, value, callback) => {
      console.log(value);
      if (value >= -128 && value <= 127) {
        callback();
      } else {
        callback(this.$t("text.intEight"));
      }
    };
    let intSixTeen = (rule, value, callback) => {
      console.log(value);
      if (value >= -32768 && value <= 32767) {
        callback();
      } else {
        callback(this.$t("text.intSixTeen"));
      }
    };
    let uintEight = (rule, value, callback) => {
      console.log(value);
      if (value >= 0 && value <= 255) {
        callback();
      } else {
        callback(this.$t("text.uintEight"));
      }
    };
    let uintSixTeen = (rule, value, callback) => {
      console.log(value);
      if (value >= 0 && value <= 65535) {
        callback();
      } else {
        callback(this.$t("text.uintSixTeen"));
      }
    };
    let obj = {};
    for (let i = 0; i < 999; i++) {
      obj[`bytes${i}`] = [
        {
          required: true,
          message: this.$t("text.sendInput"),
          trigger: "blur",
        },
        {
          pattern: `^0[xX][0-9a-fA-F]{${i * 2}}$`,
          message: `必须是十六进制的数字或字母，长度是` + 2 * i,
          trigger: "blur",
        },
      ];
    }
    return {
      transation: {
        userName: "",
        funcName: "",
        funcValue: [],
        funcType: "function",
        reqVal: [],
      },
      userId: "",
      userList: [],
      abiList: [],
      form:{pramasData: []},
      funcList: [],
      buttonClick: false,
      contractVersion: this.version,
      constant: false,
      contractAddress: this.data.contractAddress || "",
      errorMessage: "",
      // placeholderText: this.$t('placeholder.selectedAccountAddress'),
      pramasObj: null,
      stateMutability: "",
      isCNS: false,
      cnsList: [],
      cnsVersion: "",
      cnsName: "",
      isShowAddUserBtn: false,
      creatUserNameVisible: false,
      ruleForm: {},
      ruleForms: {},
      rules: {
        int: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
          {
            pattern: /^-?[0-9]\d*$/,
            message: "必须是数字，可以是负数",
            trigger: "blur",
          },
        ],
          string: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
        ],
         uint256: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
          {
            pattern: /^[0-9]\d*$/,
            message: "必须是数字，不可以是负数",
            trigger: "blur",
          },
        ],
          int256: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
          {
            pattern: /^-?[0-9]\d*$/,
            message: "必须是数字，可以是负数",
            trigger: "blur",
          },
        ],
          bytes: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
        ],
        bool: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
        ],
          tuple: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
        ],
          address: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
          {
            pattern: `^0[xX][0-9a-fA-F]{40}$`,
            message: "必须是十六进制的数字或字母,长度是42",
            trigger: "blur",
          },
        ],
        uint: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
          {
            pattern: /^[0-9]\d*$/,
            message: "必须是数字，不可以是负数",
            trigger: "blur",
          },
        ],
        int8: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
          { validator: intEight, trigger: "blur" },
        ],
        int16: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
          { validator: intSixTeen, trigger: "blur" },
        ],
        uint8: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
          { validator: uintEight, trigger: "blur" },
        ],
        uint16: [
          {
            required: true,
            message: this.$t("text.sendInput"),
            trigger: "blur",
          },
          { validator: uintSixTeen, trigger: "blur" },
        ],
        ...obj,
      },
    };
  },
  computed: {
    showUser() {
      let showUser = true;
      if (
        this.constant ||
        this.stateMutability === "view" ||
        this.stateMutability === "pure"
      ) {
        showUser = false;
      }
      return showUser;
    },
  },
  mounted: function () {
    this.getLocalKeyStores();
    this.formatAbi();
    this.changeFunc();
  },
  methods: {
       arrayLimit() {
      console.log(this.form.pramasData);
      this.form.pramasData.map((item, index) => {
        if (item.type.indexOf("[]") != -1) {
          this.rules[item.type] = [
            {
              required: true,
              message: this.$t("text.sendInput"),
              trigger: "blur",
            },
            {
              pattern: `^\\[.*?\\]$`,
              message: "必须是以[开头,以]结尾的数组",
              trigger: "blur",
            },
          ];
        }
        // if(item.name==''){
        //   item.name='param'+index
        // }
      });
    },
     placeholderText(type) {
      if (
        type.length > 5 &&
        type.substring(0, 5) == "bytes" &&
        type.substring(type.length, type.length - 2) != "[]"
      ) {
        
        return type +"(十六进制，长度是" +type.substring(5, type.length)*2+")";
      }
      switch (type) {
        case "string":
          return "string";
          break;
        case "bytes":
          return "bytes";
          break;
        case "int":
          return "int,整数";
          break;
        case "unit":
          return "unit,大于等于0的整数";
          break;
        default:
          return type;
          break;
      }
    },
    submit: function (formName) {
      this.$refs.sendTransation.validate((valid) => {
        if (valid) {
          this.send();
        } else {
          return false;
        }
      });
    },
    close: function (formName) {
      this.$emit("close", false);
    },
    getLocalKeyStores() {
      queryLocalKeyStores()
        .then((res) => {
          const { data, status } = res;
          if (status === 200) {
            this.userList = data;
            if (this.userList.length) {
              this.transation.userName = this.userList[0]["address"];
              this.userId = this.userList[0]["userName"];
            } else {
              // this.placeholderText = this.$t('placeholder.selectedNoUser')
              this.isShowAddUserBtn = true;
            }
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
    changeId: function (val) {
      this.userList.forEach((value) => {
        if (val === value.address) {
          this.userId = value.userName;
        }
      });
    },
    changeType: function (val) {
      this.funcList = [];
      if (val && val === "function") {
        this.abiList.forEach((value, index) => {
          if (value.type === val) {
            value.funcId = index;
            this.funcList.push(value);
          }
        });
      } else if (val && val === "constructor") {
        this.abiList.forEach((value) => {
          if (value.type === val) {
            this.form.pramasData = value.inputs;
            this.pramasObj = value;
            this.arrayLimit();
          }
        });
      } else {
        this.abiList.forEach((value, index) => {
          if (value.type === "function") {
            value.funcId = index;
            this.funcList.push(value);
          }
        });
      }
      if (this.funcList.length) {
        this.transation.funcName = this.funcList[0].funcId;
      }
      this.changeFunc();
    },
    formatAbi: function () {
      let abi = this.abi;
      if (abi) {
        this.abiList = JSON.parse(abi);
        this.changeType();
      }
    },
    changeFunc: function () {
      this.transation.funcValue = [];
      this.constant = false;
      this.funcList.forEach((value) => {
        if (value.funcId === this.transation.funcName) {
          this.form.pramasData = value.inputs;
          this.constant = value.constant;
          this.pramasObj = value;
          this.stateMutability = value.stateMutability;
          this.arrayLimit();
        }
      });
      this.funcList.sort(function (a, b) {
        return (a.name + "").localeCompare(b.name + "");
      });
    },
    send: function () {
      // this.buttonClick = true;
      let pattren = /^\s+|\s+$/g;
      if (this.transation.funcType === "constructor") {
        this.transation.funcName = this.data.contractName;
      }
      let rules = [];
      for (let item in this.form.pramasData) {
        let data = this.form.pramasData[item].value;
        if (data && isJson(data)) {
          try {
             rules.push(JSON.parse(data))
          } catch (error) {
            console.log(error);
          }
        } else if (data === "true" || data === "false") {
             rules.push(eval(data.toLowerCase()))
        }
         else {
          rules.push(data)
        }
      } 
      
      // for (var i in this.form.pramasData) {
      //   for (var key in this.ruleForms) {
      //     if (this.form.pramasData[i].name == key) rules.push(this.ruleForms[key]);
      //   }
      // }

      let functionName = "";
      this.funcList.forEach((value) => {
        if (value.funcId == this.transation.funcName) {
          functionName = value.name;
        }
      });
      let data = {
        groupId: localStorage.getItem("groupId"),
        user:
          this.constant ||
          this.stateMutability === "view" ||
          this.stateMutability === "pure"
            ? ""
            : this.transation.userName,
        contractName: this.data.contractName,
        contractPath: this.data.contractPath,
        version: this.isCNS && this.cnsVersion ? this.cnsVersion : "",
        funcName: functionName || "",
        funcParam:  rules,
        contractAddress: this.isCNS ? "" : this.contractAddress,
        contractAbi: [this.pramasObj],
        useAes: false,
        useCns: this.isCNS,
        cnsName: this.isCNS && this.cnsName ? this.cnsName : "",
      };
      sendTransation(data)
        .then((res) => {
          this.buttonClick = false;
          const { data, status } = res;
          if (status === 200) {
            this.close();
            let resData = data;
            let successData = {
              resData: resData,
              input: {
                name: functionName,
                inputs: this.form.pramasData,
              },
              data: this.pramasObj,
            };
            this.$emit(
              "success",
              Object.assign({}, successData, {
                constant: this.constant,
              })
            );
            if (
              this.constant ||
              this.stateMutability === "view" ||
              this.stateMutability === "pure"
            ) {
              this.$message({
                type: "success",
                message: this.$t("text.searchSucceeded"),
              });
            } else {
              if (resData.status == "0x0") {
                this.$message({
                  type: "success",
                  message: this.$t("text.txnSucceeded"),
                });
              } else {
                this.$message({
                  type: "error",
                  message: this.$t("text.txnFailed"),
                  })
              }
            }
          } else {
            this.$message({
              type: "error",
              message: this.$chooseLang(res.data.code),
            });
          }
        })
        .catch((err) => {
          this.buttonClick = false;
         // this.close();
          this.$message({
            type: "error",
            message: this.$t("text.sendFailed"),
          });
        });
    },
    changeCns(val) {
      if (val) {
        this.queryFindCnsInfo();
      }
    },
    queryFindCnsInfo() {
      let param = {
        groupId: localStorage.getItem("groupId"),
        contractAddress: this.data.contractAddress,
      };
      findCnsInfo(param).then((res) => {
        const { data, status } = res;
        if (status === 200) {
          if (data.data) {
            this.cnsVersion = data.data.version;
            this.cnsName = data.data.cnsName;
          }
        } else {
          this.$message({
            type: "error",
            message: this.$chooseLang(res.data.code),
          });
        }
      });
    },
    createUser() {
      this.creatUserNameVisible = true;
    },
    createUserClose(data) {
      this.userList = data;
      if (this.userList.length > 0) {
        this.isShowAddUserBtn = false;
        this.transation.userName = this.userList[0]["address"];
        this.userId = this.userList[0]["userName"];
      }
      this.creatUserNameVisible = false;
    },
  },
};
</script>

<style scoped>
.send-wrapper {
}
.send-item {
  line-height: 30px;
  margin-bottom: 24px;
}
.send-btn >>> .el-button {
  padding: 9px 16px;
}
.java-class {
  margin-top: 32px;
}
.func-color {
  color: #409eff;
}
.func-name .el-select-dropdown__list .el-select-dropdown__item.selected {
  color: #606266;
  font-weight: 700;
}
.user-explain {
  margin-left: 4px;
}
.user-explain > span {
  display: inline-block;
  max-width: 45px;
  height: 25px;
  line-height: 25px;
  position: relative;
  top: 9px;
}
.text-td {
  white-space: nowrap;
}
.el-input .el-input--medium {
}
.send-body {
  overflow-y: scroll;
  max-height: 400px;
  min-height: 200px;
}
.send-item >>> .el-form-item__error {
  left: 180px !important;
}
.send-item >>> .el-input-group__prepend {
  width: 60px;
}
.send-item >>> .el-form-item {
  line-height: 30px;
  margin-bottom: 24px;
}
.send-item {
  line-height: 30px;
  margin-bottom: 24px;
}
.send-item-title {
  display: inline-block;
  width: 60px;
  text-align: right;
}
.send-item-params {
  display: inline-block;
}
.send-item >>> .el-input__inner {
  height: 32px;
  line-height: 32px;
}
</style>
