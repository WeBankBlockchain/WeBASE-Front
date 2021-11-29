<template>
  <div>
    <el-form
      :model="sdkForm"
      :rules="rules"
      ref="sdkForm"
      label-width="110px"
      class="demo-ruleForm"
    >
      <el-form-item :label="$t('table.type')" style="width: 320px">
        <el-radio-group v-model="sdkForm.useSmSsl" size="medium">
          <el-radio-button label="true">国密</el-radio-button>
          <el-radio-button label="false">非国密</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <div class="useSmSsl" v-if="sdkForm.useSmSsl == 'false'">
        <el-form-item
          :label="$t('sdk.caCertStr')"
          prop="caCert"
          style="width: 320px"
          ref="caCert"
        >
          <el-upload
            class="upload-demo"
            action="caCertStr"
            multiple
            :limit="1"
            :http-request="uploadSDK"
            :before-upload="onBeforeUpload"
            :on-exceed="handleExceed"
            :file-list="sdkForm.caCert"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item
          :label="$t('sdk.sdkCertStr')"
          prop="sdkCert"
          style="width: 320px"
          ref="sdkCert"
        >
          <el-upload
            class="upload-demo"
            name="upload"
            action="sdkCertStr"
            multiple
            :limit="1"
            :http-request="uploadSDK"
            :before-upload="onBeforeUpload"
            :on-exceed="handleExceed"
            :file-list="sdkForm.sdkCert"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item
          :label="$t('sdk.sdkKeyStr')"
          prop="sdkKey"
          style="width: 320px"
          ref="sdkKey"
        >
          <el-upload
            class="upload-demo"
            action="sdkKeyStr"
            multiple
            :limit="1"
            ref="sdk3"
            :http-request="uploadSDK"
            :before-upload="onBeforeUpload"
            :on-exceed="handleExceed"
            :file-list="sdkForm.sdkKey"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
      </div>
      <div class="nouseGmSsl" v-else>
        <el-form-item
          :label="$t('sdk.smCaCertStr')"
          prop="smCaCert"
          style="width: 320px"
          ref="upload"
        >
          <el-upload
            class="upload-demo"
            action="smCaCertStr"
            multiple
            :limit="1"
            :http-request="uploadSDK"
            :before-upload="onBeforeUpload"
            :on-exceed="handleExceed"
            :file-list="sdkForm.smCaCert"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item
          :label="$t('sdk.smEnSdkCertStr')"
          prop="smEnSdkCert"
          style="width: 320px"
          ref="upload"
        >
          <el-upload
            class="upload-demo"
            action="smEnSdkCertStr"
            multiple
            :limit="1"
            :http-request="uploadSDK"
            :before-upload="onBeforeUpload"
            :on-exceed="handleExceed"
            :file-list="sdkForm.smEnSdkCert"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item
          :label="$t('sdk.smEnSdkKeyStr')"
          prop="smEnSdkKey"
          style="width: 320px"
          ref="upload"
        >
          <el-upload
            class="upload-demo"
            action="smEnSdkKeyStr"
            multiple
            :limit="1"
            :http-request="uploadSDK"
            :before-upload="onBeforeUpload"
            :on-exceed="handleExceed"
            :file-list="sdkForm.smEnSdkKey"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item
          :label="$t('sdk.smSdkCertStr')"
          prop="smSdkCert"
          style="width: 320px"
          ref="upload"
        >
          <el-upload
            class="upload-demo"
            action="smSdkCertStr"
            multiple
            :limit="1"
            :http-request="uploadSDK"
            :before-upload="onBeforeUpload"
            :on-exceed="handleExceed"
            :file-list="sdkForm.smSdkCert"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item
          :label="$t('sdk.smSdkKeyStr')"
          prop="smSdkKey"
          style="width: 320px"
          ref="upload"
        >
          <el-upload
            class="upload-demo"
            action="smSdkKeyStr"
            multiple
            :limit="1"
            :http-request="uploadSDK"
            :before-upload="onBeforeUpload"
            :on-exceed="handleExceed"
            :file-list="sdkForm.smSdkKey"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
      </div>

      <div v-for="(item, index) in sdkForm.ipPort" :key="index">
        <el-form-item
          label="节点ip"
          class="ipPort"
          :prop="'ipPort.' + index + '.ip'"
          :rules="rules.ip"
        >
          <el-input v-model="item.ip"></el-input>
        </el-form-item>
        <el-form-item
          label="port"
          class="ipPort"
          :prop="'ipPort.' + index + '.port'"
          :rules="rules.port"
        >
          <el-input v-model="item.port"></el-input>
          <i class="el-icon-plus" @click="addIpPort"></i>
          <i
            class="el-icon-minus"
            v-if="sdkForm.ipPort.length > 1"
            @click="delIpPort(index)"
          ></i>
        </el-form-item>
      </div>
    </el-form>
    <template>
      <el-alert
        title="请配置SDK后进入页面！"
        type="warning"
        :closable="false"
        show-icon
      >
      </el-alert>
    </template>
    <div class="text-right sure-btn" style="margin-top: 10px">
      <el-button type="primary" :loading="loading" @click="submit('sdkForm')">{{
        this.$t("permission.confirm")
      }}</el-button>
    </div>
  </div>
</template>

<script>
import { getUserList, updateSDK } from "@/util/api";
export default {
  name: "updateSDK",
  components: {},

  props: {
    modifyNode: {
      type: Object,
    },
  },

  data() {
    return {
      loading: false,
      sdkForm: {
        caCert: [],
        sdkKey: [],
        sdkCert: [],
        smCaCert: [],
        smEnSdkCert: [],
        smEnSdkKey: [],
        smSdkCert: [],
        smSdkKey: [],
        useSmSsl: "false",
        caCertStr: "",
        sdkKeyStr: "",
        sdkCertStr: "",
        smCaCertStr: "",
        smEnSdkCertStr: "",
        smEnSdkKeyStr: "",
        smSdkCertStr: "",
        smSdkKeyStr: "",
        ipPort: [{ ip: "", port: "" }],
      },
    };
  },
  computed: {
    rules() {
      var obj = {
        caCert: [{ required: true, message: "请上传文件", trigger: "change" }],
        sdkCert: [{ required: true, message: "请上传文件", trigger: "change" }],
        sdkKey: [{ required: true, message: "请上传文件", trigger: "change" }],
        smCaCert: [
          { required: true, message: "请上传文件", trigger: "change" },
        ],
        smEnSdkCert: [
          { required: true, message: "请上传文件", trigger: "change" },
        ],
        smEnSdkKey: [
          { required: true, message: "请上传文件", trigger: "change" },
        ],
        smSdkCert: [
          { required: true, message: "请上传文件", trigger: "change" },
        ],
        smSdkKey: [
          { required: true, message: "请上传文件", trigger: "change" },
        ],
        ip: [
          { required: true, message: "请输入IP", trigger: "blur" },
          {
            pattern:
              /^((2[0-4]\d|25[0-5]|[01]?\d\d?)\.){3}(2[0-4]\d|25[0-5]|[01]?\d\d?)$/,
            message: "请输入正确的ip",
            trigger: "blur",
          },
        ],
        port: [
          { required: true, message: "请输入IPPort", trigger: "blur" },
          {
            pattern:
              /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
            message: "请输入正确的ipPort",
            trigger: "blur",
          },
        ],
      };
      return obj;
    },
  },

  watch: {},

  created() {},

  mounted() {},

  methods: {
    addIpPort() {
      if (this.sdkForm.ipPort.length >= 5) {
        this.$message.warning(`最大限制5个`);
        return;
      }
      this.sdkForm.ipPort.push({});
    },
    delIpPort(index) {
      this.sdkForm.ipPort.splice(index, 1);
    },
    uploadSDK(param) {
      console.log(param);
      this.sdkForm[param.action.substring(0, param.action.length - 3)] = [
        param.file,
      ];
      var reader = new FileReader(),
        self = this;
      reader.readAsText(param.file, "UTF-8");
      reader.onload = function (e) {
        var fileString = e.target.result;
        console.log(fileString);
        self.sdkForm[param.action] = fileString;
      };
      //self.$refs[param.action].clearFiles()
      this.$refs["sdkForm"].clearValidate(
        param.action.substring(0, param.action.length - 3)
      );
    },
    onBeforeUpload(file) {
      const isLt1M = Math.ceil(file.size / 1024) < 100;
      if (!isLt1M) {
        this.$message.error(this.$t("system.fileSize"));
      }
      return isLt1M;
    },
    handleExceed(files) {
      this.$message.warning(`当前限制选择 1 个文件`);
    },
    close() {
      this.$emit("nodeModifyClose");
    },
    submit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.updateSDK();
        } else {
          return false;
        }
      });
    },
    updateSDK() {
      this.loading = true;
      let peers = [];
      let reqData;
      this.sdkForm.ipPort.map((item) => {
        peers.push(item.ip + ":" + item.port);
      });
      if (this.sdkForm.useSmSsl == "false") {
        reqData = {
          peers,
          caCertStr: this.sdkForm.caCertStr,
          sdkCertStr: this.sdkForm.sdkCertStr,
          sdkKeyStr: this.sdkForm.sdkKeyStr,
          useSmSsl: this.sdkForm.useSmSsl,
        };
      } else {
        reqData = {
          peers,
          smCaCertStr: this.sdkForm.smCaCert,
          smEnSdkCertStr: this.sdkForm.smEnSdkCertStr,
          smEnSdkKeyStr: this.sdkForm.smEnSdkKeyStr,
          smSdkCertStr: this.sdkForm.smSdkCertStr,
          smSdkKeyStr: this.sdkForm.smSdkKeyStr,
          useSmSsl: this.sdkForm.useSmSsl,
        };
      }
      updateSDK(reqData)
        .then((res) => {
          this.loading = false;
          if (res.data.code === 0) {
            this.$emit("updateSDKsucess");
            this.$message({
              type: "success",
              message: this.$t("text.updateSDKsucess"),
            });
          } else {
            this.$message({
              message: this.$chooseLang(res.data.code),
              type: "error",
              duration: 2000,
            });
          }
        })
        .catch((err) => {
          this.loading = false;
          this.$message({
            message: err.data || this.$t("text.systemError"),
            type: "error",
            duration: 2000,
          });
        });
    },
  },
};
</script>

<style scoped>
.sure-btn >>> .el-button {
  padding: 9px 16px;
}
.info {
  padding-left: 30px;
  color: #f00;
}
.ipPort {
  width: 80%;
}
.ipPort .el-input {
  width: 80%;
}
.el-icon-plus,
.el-icon-minus {
  cursor: pointer;
  margin-left: 10px;
}
.el-alert--warning.is-light {
  background-color: #2f3b52;
}
</style>
