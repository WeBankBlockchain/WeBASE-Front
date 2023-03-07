<template>
  <div>
    <div class="">
      <div class="ace-wrapper">
        <h2 class="h2-inscription font-color-fff">
          {{ this.$t("text.parseTitle") }}
        </h2>
        <div class="ace-editor" ref="ace"></div>
        <span v-if="errorMsg" class="font-color-ed5454">{{ errorMsg }}</span>
        <div class="parse-button">
          <el-button type="primary" @click="parseAbi">{{
            this.$t("text.parse")
          }}</el-button>
        </div>
      </div>
      <div class="ace-wrapper">
        <h2 class="h2-inscription font-color-fff">
          {{ this.$t("text.parseAbiManually") }}
        </h2>
        <el-row :gutter="10" style="margin-bottom: 10px">
          <el-col :span="7">
            <div class="font-color-fff">{{ this.$t("text.functionType") }}</div>
            <el-select v-model="functionType" @change="changeFunType">
              <el-option
                v-for="(item, index) in functionList"
                :key="index"
                :label="item"
                :value="item"
              ></el-option>
            </el-select>
          </el-col>
          <el-col :span="17" v-if="functionType != 'constructor'">
            <span class="font-color-fff text-hidden">name</span>
            <el-input
              v-model="functionValue"
              @input="inputFunctionValue"
            ></el-input>
          </el-col>
        </el-row>
        <el-row
          :gutter="10"
          v-for="(item, index) in argumentList"
          :key="index"
          style="margin-bottom: 10px"
        >
          <el-col :span="7">
            <div class="font-color-fff">{{ item.name }}</div>
            <el-select v-model="item.type" @change="changeArgType(index)">
              <el-option
                v-for="(it, num) in item.argumentOption"
                :key="num"
                :label="it"
                :value="it"
              ></el-option>
            </el-select>
          </el-col>
          <el-col :span="17">
            <span class="font-color-fff text-hidden">value</span>
            <input
              class="input-inner"
              type="text"
              v-model="item.argumentValue"
              @input="inputArgumentValue($event, item.type)"
            />
            <!-- <span v-if="item.tip" class="font-color-ed5454">
                            {{errors}}
                        </span> -->
          </el-col>
        </el-row>
      </div>
      <div class="add-abi">
        <el-button type="primary" @click="addAbiEncodeStroke">{{
          this.$t("text.addParameter")
        }}</el-button>
        <el-button @click="removeAbiEncodeStroke">{{
          this.$t("text.remove")
        }}</el-button>
      </div>
      <div class="container copy-text">
        <el-input
          v-model="textarea"
          type="textarea"
          :autosize="{ minRows: 4 }"
        ></el-input>
        <el-button
          type="primary"
          style="margin: 20px auto; visibility: hidden"
          @click="copy"
          >{{ this.$t("text.copy") }}</el-button
        >
      </div>
    </div>
  </div>
</template>

<script>
import ace from "ace-builds";
import "ace-builds/webpack-resolver";
import "ace-builds/src-noconflict/theme-monokai";
import "ace-builds/src-noconflict/theme-chrome";
import "ace-builds/src-noconflict/mode-javascript";
import "ace-builds/src-noconflict/mode-json";
import "ace-builds/src-noconflict/ext-language_tools";
import constant from "@/util/constant";
import { dataType, unique, unique1 } from "@/util/util.js";
import { validate } from "@/util/validate";
import contentHead from "@/components/contentHead";
import web3 from "@/util/ethAbi";
let web3Abi = web3;
import inputFilter from "@/directives/input-filter/index.js";
export default {
  name: "parseAbi",

  components: {
    contentHead,
  },
  directives: {
    inputFilter,
  },
  props: {},

  data() {
    return {
      aceEditor: null,
      themePath: "ace/theme/monokai",
      modePath: "ace/mode/json",
      abiContent: "",
      abiList: [
        {
          name: "aaa",
          content: "",
        },
        {
          name: "aaa",
          content: "",
        },
        {
          name: "aaa",
          content: "",
        },
        {
          name: "aaa",
          content: "",
        },
      ],
      functionList: [],
      functionType: "",
      functionValue: "",
      argumentList: [
        {
          argumentOption: constant.ABI_ARGUMENT_TYPE,
          type: "string",
          argumentValue: "",
          name: this.$t("text.argument"),
        },
      ],
      textarea: "",
      errorMsg: "",
      abiJsonContent: [],
      errors: "参数输入有误，请重新输入",
    };
  },

  computed: {},

  watch: {},

  created() {},

  mounted() {
    this.initEditor();
    this.initFuction();
    console.log(
      web3Abi.encodeFunctionCall(
        {
          name: "set",
          type: "set",
          inputs: [{ type: "string[2]", name: "n" }],
        },
        [["1", "1"]]
      )
    );
  },

  methods: {
    changGroup() {},
    initEditor() {
      let _this = this;
      this.aceEditor = ace.edit(this.$refs.ace, {
        fontSize: 14,
        fontFamily: "Consolas,Monaco,monospace",
        theme: this.themePath,
        mode: this.modePath,
        tabSize: 4,
        useSoftTabs: true,
      });
      this.aceEditor.setOptions({
        enableSnippets: true,
        enableLiveAutocompletion: true,
        enableBasicAutocompletion: true,
        autoScrollEditorIntoView: true,
        copyWithEmptySelection: true,
      });
      this.aceEditor.getSession().setUseWrapMode(true);
      this.aceEditor.setHighlightActiveLine(true);
      this.aceEditor.getSession().on("change", this.changeAce);
      this.aceEditor.resize();
    },
    emptyArgument() {
      this.argumentList = [];
    },
    initArgument() {
      this.argumentList = [
        {
          argumentOption: constant.ABI_ARGUMENT_TYPE,
          type: "string",
          argumentValue: "",
          name: this.$t("text.argument"),
        },
      ];
    },
    initFuction() {
      this.functionList = ["constructor", "your function"];
      this.functionType = "constructor";
    },

    changeAce() {
      this.textarea = "";
      this.abiContent = this.aceEditor.getSession().getValue();
    },
    changeFunType(val) {
      this.validateArgumentValue();
      if (!this.abiJsonContent.length) {
        this.initArgument();
      } else {
        if (val === "constructor") {
          this.functionValue = val;
          this.emptyArgument();
          this.abiJsonContent.forEach((item) => {
            if (this.functionType == item.type) {
              var inputs = item.inputs;
              for (let index = 0; index < inputs.length; index++) {
                this.argumentList.push(
                  Object.assign(
                    {},
                    { argumentOption: constant.ABI_ARGUMENT_TYPE },
                    inputs[index]
                  )
                );
              }
            }
          });
          sessionStorage.setItem(
            "temporaryArgument",
            JSON.stringify(this.argumentList)
          );
        } else if (val === "your function") {
          this.functionValue = "";
          this.argumentList = [];
          var temporaryArgumentList = JSON.parse(
            sessionStorage.getItem("temporaryArgument")
          );
          if (temporaryArgumentList.length) {
            this.argumentList.push(temporaryArgumentList[0]);
          } else {
            this.initArgument();
          }
        } else {
          this.functionValue = val;
          this.emptyArgument();
          this.abiJsonContent.forEach((item) => {
            if (this.functionType == item.name) {
              var inputs = item.inputs;
              for (let index = 0; index < inputs.length; index++) {
                this.argumentList.push(
                  Object.assign(
                    {},
                    { argumentOption: constant.ABI_ARGUMENT_TYPE },
                    inputs[index]
                  )
                );
              }
            }
          });
          sessionStorage.setItem(
            "temporaryArgument",
            JSON.stringify(this.argumentList)
          );
        }
      }
      this.textarea = "";
    },
    changeArgType(val) {
      this.textarea = "";
      this.argumentList.forEach((item, index) => {
        if (index === val) {
          item.argumentValue = "";
        }
      });
      this.validateArgumentValue();
    },
    parseAbi() {
      if (typeof this.abiContent == "string") {
        try {
          var obj = JSON.parse(this.abiContent);
          this.abiJsonContent = obj;

          if (typeof obj == "object" && obj) {
            this.errorMsg = "";
            this.extractAbi(obj);
          }
        } catch (error) {
          this.errorMsg = this.$t("rule.correctJson");
          console.log("error：" + this.abiContent + "!!!" + error);
        }
      }
    },
    extractAbi(obj) {
      var array = [];
      obj.forEach((item) => {
        if (
          item.inputs.length &&
          (item.type === "function" || item.type === "constructor")
        ) {
          array.push(item);
        }
      });
      this.abiJsonContent = array;
      this.executeAbi();
    },
    executeAbi() {
      this.initFuction();
      this.emptyArgument();
      this.abiJsonContent.forEach((item) => {
        this.functionList.push(item.name);
      });
      this.functionList = unique1(this.functionList);
      this.functionList = this._.compact(this.functionList);
      this.functionType = this.functionList[2] || this.functionList[0];
      this.functionValue = this.functionList[2];
      this.abiJsonContent = unique(this.abiJsonContent, "name");
      this.abiJsonContent.forEach((item) => {
        if (this.functionType == item.name) {
          var inputs = item.inputs;
          for (let index = 0; index < inputs.length; index++) {
            this.argumentList.push(
              Object.assign(
                {},
                {
                  argumentOption: constant.ABI_ARGUMENT_TYPE,
                  argumentValue: "",
                },
                inputs[index]
              )
            );
          }
        }
      });

      sessionStorage.setItem(
        "temporaryArgument",
        JSON.stringify(this.argumentList)
      );
      if (this.functionType === "constructor") {
        this.parseConstructorAbi();
      } else {
        this.parseNewAbi();
      }
    },
    inputFunctionValue() {
      if (this.functionType === "constructor") {
        this.parseConstructorAbi();
      } else {
        this.parseNewAbi();
      }
    },
    validateArgumentValue() {
      this.argumentList.forEach((item) => {
        if (item.argumentValue) {
          item.msgObj = validate(item.type, item.argumentValue);
        } else {
          item.msgObj = undefined;
        }
      });
    },
    inputArgumentValue(val, type) {
      this.validateArgumentValue();
      if (val) {
        if (this.functionType === "constructor") {
          this.parseConstructorAbi();
        } else {
          this.parseNewAbi();
        }
      } else {
        this.textarea = "";
      }
    },
    parseNewAbi() {
      var inputs = [],
        inputsVal = [];
      this.argumentList.forEach((item) => {
        inputs.push({
          type: item.type,
          name: item.argumentValue,
        });
        console.log(dataType);
        inputsVal.push(dataType(item.type, item.argumentValue));
      });
      if (!inputs.length) {
        this.textarea = "";
        return;
      }
      
      for (let i = 0; i < inputsVal.length; i++) {
        if (!inputsVal[i] && typeof (inputsVal[i]) != 'boolean') {
        //if (!inputsVal[i]) {
          this.textarea = inputsVal[i];
          return false;
        }
      }
      try {
        if (localStorage.getItem("encryptionId") == 1) {
          
          this.textarea = web3Abi.smEncodeFunctionCall(
            {
              name: this.functionValue,
              type: this.functionType,
              inputs: inputs,
            },
            inputsVal
          );
        } else {
          console.info(
            this.functionValue,
            this.functionType,
            inputs,
            inputsVal
          );
          this.textarea = web3Abi.encodeFunctionCall(
            {
              name: this.functionValue,
              type: this.functionType,
              inputs: inputs,
            },
            inputsVal
          );
        }
      } catch (error) {
        this.textarea = error;
      }
    },
    parseConstructorAbi() {
      var inputs = [],
        inputsVal = [];
      this.argumentList.forEach((item) => {
        inputs.push(item.type);
        // inputsVal.push(dataType(item.type, item.argumentValue))
        inputsVal.push(item.argumentValue);
      });

      if (!inputs.length) {
        this.textarea = "";
        return;
      }
      for (let i = 0; i < inputsVal.length; i++) {
        // if (!inputsVal[i] && typeof (inputsVal[i]) != 'boolean') {
        //     return false
        // }
        if (!inputsVal[i]) {
          this.argumentList[i].tip = true;
          this.textarea = "";
          return false;
        }
      }
      try {
        this.textarea = web3Abi.encodeParameters(inputs, inputsVal);
      } catch (error) {
        this.textarea = error;
      }
    },
    addAbiEncodeStroke() {
      this.argumentList.push({
        argumentOption: constant.ABI_ARGUMENT_TYPE,
        type: "string",
        argumentValue: "",
        name: this.$t("text.argument"),
      });
    },
    removeAbiEncodeStroke() {
      this.argumentList.splice(this.argumentList.length - 1, 1);
      if (this.functionType === "constructor") {
        this.parseConstructorAbi();
      } else {
        this.parseNewAbi();
      }
    },
    copy() {
      if (!this.textarea) {
        this.$message({
          type: "fail",
          showClose: true,
          message: this.$t("text.copyErrorMsg"),
          duration: 2000,
        });
      } else {
        this.$copyText(this.textarea).then((e) => {
          this.$message({
            type: "success",
            showClose: true,
            message: this.$t("text.copySuccessMsg"),
            duration: 2000,
          });
        });
      }
    },
  },
};
</script>

<style scoped>
.ace-editor {
  height: 300px;
  position: relative;
  text-align: left;
  letter-spacing: 0.1px;
  text-rendering: geometricPrecision;
  font-feature-settings: "liga" 0;
  font-variant-ligatures: none;
  font: 14px / normal "Monaco", "Menlo", "Ubuntu Mono", "Consolas",
    "source-code-pro", monospace !important;
  border: 1px solid #575a5f;
}
.ace-editor >>> .ace_print-margin {
  display: none;
  text-rendering: geometricPrecision;
}
.h2-inscription {
  font-size: 20px;
  font-weight: 600;
  text-align: left;
  padding-bottom: 20px;
  margin: 0;
}
.ace-wrapper {
  padding: 0px 20px 0 20px;
  padding-right: 50%;
}
.parse-button {
  text-align: center;
  padding-top: 30px;
}
.add-abi {
  text-align: center;
  padding: 20px 0;
  padding-right: 50%;
}
.copy-text {
  margin-top: 15px;
  text-align: center;
}
.container {
  padding: 0 20px;
  padding-right: 50%;
}
.input-inner {
  -webkit-appearance: none;
  background-color: #fff;
  background-image: none;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  color: #606266;
  display: inline-block;
  font-size: inherit;
  height: 36px;
  line-height: 36px;
  outline: 0;
  padding: 0 15px;
  -webkit-transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  width: 100%;
  background-color: #252f43;
  border-color: #252f43;
  color: #70889e;
}
</style>
