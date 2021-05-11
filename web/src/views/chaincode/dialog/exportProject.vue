<template>
    <div>
        <el-dialog :title="$t('text.exportJavaProject')" :visible.sync="dialogVisible" :before-close="modelClose" class="dialog-wrapper" width="750px">
            <h3 style="padding-left: 18px">{{$t('text.projectTitle')}}</h3>
            <el-form :model="projectFrom" :rules="rules" ref="projectFrom" label-width="116px" class="demo-ruleForm">
                <el-form-item :label="$t('text.projectName')" prop="artifactName">
                    <el-input v-model="projectFrom.artifactName" style="width: 300px"></el-input>
                </el-form-item>
                <el-form-item :label="$t('text.projectGroupName')" prop="group">
                    <el-input v-model="projectFrom.group" style="width: 300px"></el-input>
                </el-form-item>
                
                <el-form-item label="channelIp" prop="channelIp">
                    <el-input v-model="projectFrom.channelIp" style="width: 300px"></el-input>
                     <el-tooltip effect="dark" :content="$t('text.actualChannelIp')" placement="top-start">
                        <i class="el-icon-info"></i>
                    </el-tooltip>
                </el-form-item>
                <el-form-item :label="$t('text.projectUser')">
                    <el-select v-model="projectFrom.userAddress" :placeholder="$t('text.select')" style="width: 300px">
                        <el-option v-for="item in userList" :key="item.address" :label="item.userName" :value="item.address">
                        </el-option>
                    </el-select>
                </el-form-item>
                <!-- <el-form-item :label="'p12密码'" prop="p12Password">
                  <el-input v-model="projectFrom.p12Password" style="width: 300px"></el-input>
              </el-form-item> -->
            </el-form>
            <el-divider></el-divider>
            <h3 style="padding-left: 18px">{{$t('text.projectContract')}}</h3>
            <p style="padding-left: 28px">{{$t('text.exportJavaProjectInfo1')}}</p>
            <p style="padding:5px 0;color: #F56C6C;padding-left: 28px">{{$t('text.exportJavaProjectInfo2')}}</p>
            <el-table :show-header='false' :data="tableData" style="width: 100%;padding: 0 20px" :row-key="getRowKeys" :expand-row-keys="expands" @expand-change="clickTable" ref="refTable">
                <el-table-column type="expand">
                    <template slot-scope="scope">
                        <!-- <span>{{contractList}}</span> -->
                        <div class="table-content">
                            <el-table ref="multipleTable" :data="scope.row.contractList" :show-header='true' @selection-change="handleSelectionChange" :default-sort="{prop: 'contractPath', order: 'descending'}">
                                <el-table-column type="selection" :selectable='selectDisabled' width="55">
                                </el-table-column>
                                <el-table-column prop="contractName" show-overflow-tooltip :label="$t('contracts.contractName')"></el-table-column>
                                <el-table-column prop="contractPath" :label="$t('text.compileStatus')">
                                    <template slot-scope="prop">
                                        <span v-if='prop.row.contractAbi' style="color: #67C23A">{{$t('text.compiled')}}</span>
                                        <span v-if='!prop.row.contractAbi' style="color: #F56C6C">{{$t('text.uncomplie')}}</span>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                        <!-- <div v-else>暂无数据</div> -->
                    </template>
                </el-table-column>
                <el-table-column prop="contractPath" :label="$t('text.uncomplie')" show-overflow-tooltip></el-table-column>
                <el-table-column prop="modifyTime" :label="$t('nodes.modifyTime')"></el-table-column>
            </el-table>
            <div slot="footer" class="dialog-footer">
                <el-button @click="modelClose">{{$t('dialog.cancel')}}</el-button>
                <el-button type="primary" @click="submit('projectFrom')">{{$t('dialog.confirm')}}</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { searchContract, queryLocalKeyStores, exportJavaProject } from "@/util/api";
let Base64 = require("js-base64").Base64;
export default {
    name: 'exportProject',
    props: {
        folderList: {
            type: Array,
            default: []
        },
        show: {
            type: Boolean,
            default: false
        }
    },
    data() {
        return {
            tableData: [],
            dialogVisible: this.show,
            expands: [],
            getRowKeys: function (row) {
                return row.contractPath;
            },
            contractList: [],
            multipleSelection: [],
            selectDisabled(row, index) {
                if (!row.contractAbi) {
                    return false
                } else {
                    return true
                }
            },
            userList: [],
            projectFrom: {
                artifactName: "demo",
                group: 'org_example',
                userAddress: [],
                channelIp: '127.0.0.1',
            },
            rules: {
                artifactName: [
                    {
                        required: true,
                        message: this.$t("rule.artifactName"),
                        trigger: "blur",
                    },
                    {
                        min: 1,
                        max: 32,
                        message: this.$t("rule.contractLong"),
                        trigger: "blur",
                    },
                    {
                        pattern: /^[A-Za-z0-9_]+$/,
                        message: this.$t("rule.contractRule"),
                        trigger: "blur",
                    },
                ],
                group: [
                    {
                        required: true,
                        message: this.$t("rule.group"),
                        trigger: "blur",
                    },
                    {
                        min: 1,
                        max: 32,
                        message: this.$t("rule.contractLong"),
                        trigger: "blur",
                    },
                    {
                        pattern: /^[A-Za-z0-9_]+$/,
                        message: this.$t("rule.contractRule"),
                        trigger: "blur",
                    },
                ],
                
                p12Password: [
                    {
                        required: true,
                        message: this.$t('privateKey.placeholderPassword'),
                        trigger: "blur"
                    },
                ],
                channelIp: [
                    {
                        required: true,
                        message: this.$t('rule.ipName'),
                        trigger: "blur",
                    },
                    {
                        pattern: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/,
                        message: this.$t("rule.IpRule"),
                        trigger: "blur",
                    },
                ]
            }
        }
    },
    destroyed(){
        this.$store.state.exportProjectShow = false
    },
    mounted() {
        this.getList();
        this.getUserInfoData()
    },
    methods: {
        getList() {
            this.expands = []
            this.tableData = this.folderList.map((value) => {
                value.contractList = []
                return value
            })
            this.expands.push(this.$store.state.selectedContracts.contractPath)
            this.$nextTick(() => {
                // console.log(this.$refs.multipleTable);
                // this.multipleSelection.push(this.$store.state.selectedContracts)
                // this.$refs.multipleTable.toggleRowSelection(this.$store.state.selectedContracts,true);
                this.getContractList(this.$store.state.selectedContracts)
            })
        },
        getUserInfoData() {
            queryLocalKeyStores()
            .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        this.userList = data
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
                        message: err.data || this.$t('text.systemError')
                    });
                })
        },
        modelClose() {
            this.$emit('close')
        },

        clickTable: function (row, expandedRows) {
            if (expandedRows.length) {
                this.expands = []
                if (row) {
                    this.expands.push(row.contractPath)
                }
            } else {
                this.expands = []
            }
            this.$nextTick(() => {
                this.getContractList(row)
            })
        },
        getContractList(row) {
            const reqData = {
                groupId: localStorage.getItem("groupId"),
                contractPathList: [row.contractPath]
            }
            searchContract(reqData).then(res => {
                if (res.data.code === 0) {
                    this.contractList = res.data.data
                    let num
                    // 调用数组方法强刷数据
                    this.tableData.forEach((value, index) => {
                        if (value.contractPath === row.contractPath) {
                            row.contractList = this.contractList
                            num = index
                        }
                    });
                    this.$set(this.tableData, num, row)
                    var selectedDirectoryInfo = {}
                    var rootDirectoryInfo = {}
                    for (var i = 0; i < this.tableData.length; i++) {
                        if (this.tableData[i]['contractPath'] == row.contractPath) {
                            selectedDirectoryInfo = this.tableData[i]
                            this.tableData.splice(i, 1); 
                            break;
                        }
                    }
                    this.tableData.unshift(selectedDirectoryInfo);
                    for (var i = 0; i < this.tableData.length; i++) {
                        if (this.tableData[i]['contractPath'] == '/') {
                            rootDirectoryInfo = this.tableData[i]
                            this.tableData.splice(i, 1); 
                            break;
                        }
                    }
                    this.tableData.unshift(rootDirectoryInfo);
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
                        message: err.data || this.$t('text.systemError')
                    });
                });
        },
        handleSelectionChange(val) {

            this.multipleSelection = val;
            console.log(val)
        },
        submit(formName) {
            if (this.multipleSelection.length === 0) {
                this.$message({
                    type: "error",
                    message: this.$t('rule.checkContract')
                });
            }
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    this.export()
                } else {
                    return false;
                }
            })
        },
        export() {
            const idList = this.multipleSelection.map(value => {
                return value.id
            })
            const reqData = {
                contractIdList: idList,
                group: this.projectFrom.group,
                artifactName: this.projectFrom.artifactName,
                groupId: localStorage.getItem("groupId"),
                channelIp: this.projectFrom.channelIp
            }
            if (this.projectFrom.userAddress) {
                reqData.userAddressList = [this.projectFrom.userAddress]
            }
            exportJavaProject(reqData).then(res => {
                if (res.data.code === 0) {
                    const content = Base64.toUint8Array(res.data.data.fileStreamBase64);
                    const blob = new Blob([content], { type: `application/zip;charset=utf-8` })
                    const fileName = res.data.data.fileName
                    if ('download' in document.createElement('a')) {
                        const elink = document.createElement('a')
                        elink.download = fileName
                        elink.style.display = 'none'
                        elink.href = URL.createObjectURL(blob)
                        document.body.appendChild(elink)
                        elink.click()
                        URL.revokeObjectURL(elink.href)
                        document.body.removeChild(elink)
                    } else {
                        navigator.msSaveBlob(blob, fileName)
                    }
                }
            })
        },
    }
}
</script>

<style scoped>
.table-content {
    max-height: 270px;
    overflow: auto;
}
</style>