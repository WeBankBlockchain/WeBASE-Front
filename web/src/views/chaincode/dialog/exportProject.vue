<template>
    <div>
        <el-dialog :modal="true" :modal-append-to-body="false" :title="$t('text.exportJavaProject')" :close-on-click-modal="false" :visible.sync="dialogVisible" :before-close="modelClose" class="dialog-wrapper" width="750px">
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
                <el-form-item label="channelPort" prop="channelPort">
                    <el-input v-model="projectFrom.channelPort" :disabled="queryPort ? true : false " style="width: 300px"></el-input>
                    <el-tooltip effect="dark" :content="$t('text.haveChannelPort')" placement="top-start">
                        <i class="el-icon-info"></i>
                    </el-tooltip>
                 <el-button type="text" size="mini" @click="checkChannelIP('projectFrom')">{{$t('privateKey.checkNodeActive')}}</el-button>
                </el-form-item>
                <el-form-item :label="$t('text.projectUser')">
                    <!-- <el-select v-model="projectFrom.userAddress" :placeholder="$t('text.select')" style="width: 300px">
                        <el-option v-for="item in userList" :key="item.address" :label="item.userName" :value="item.address">
                        </el-option>
                    </el-select> -->
                      <el-select  v-model="projectFrom.userAddress"  class="filter-item"  :placeholder="$t('text.select')"  multiple style="width: 300px">
                        <el-option v-for="item in userList" :key="item.address" :label="item.userName"  :value="item.address">
                        </el-option>
                    </el-select>
                    <el-button v-if="isShowAddUserBtn" type="text" size="mini" @click="createUser()">{{$t('privateKey.addUser')}}</el-button>
                </el-form-item> 
                <!-- <el-form-item :label="'p12密码'" prop="p12Password">
                  <el-input v-model="projectFrom.p12Password" style="width: 300px"></el-input>
              </el-form-item> -->
            </el-form>
            <el-divider></el-divider>
            <h3 style="padding-left: 18px">{{$t('text.projectContract')}}</h3>
            <p style="padding-left: 28px">{{$t('text.exportJavaProjectInfo1')}}</p>
            <p style="padding:5px 0;color: #F56C6C;padding-left: 28px">{{$t('text.exportJavaProjectInfo2')}}</p>
            <el-table :show-header='false' :data="tableData" class="block-table-content" style="width: 100%;padding: 0 20px" :row-key="getRowKeys" :expand-row-keys="expands" @expand-change="handleExpand" @row-click="clickTable" ref="refTable">
                <el-table-column type="expand">
                    <template slot-scope="scope">
                        <!-- <span>{{contractList}}</span> -->
                        <div class="table-content">
                            <el-table ref="multipleTable" :data="scope.row.contractList" :show-header='true' @select-all="handleSelectAll" @selection-change="handleSelectionChange($event, scope.row)" :default-sort="{prop: 'contractPath', order: 'descending'}">
                                <el-table-column type="selection" width="55">
                                     <!-- <el-table-column type="selection" :selectable='selectDisabled' width="55"> -->
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
         <el-dialog :title="$t('dialog.addUsername')" :visible.sync="creatUserNameVisible"  class="dialog-wrapper" width="640px" :center="true">
            <v-createUser  @close='createUserClose'></v-createUser>
         </el-dialog>
    </div>
</template>

<script>
import { searchContract, queryLocalKeyStores, exportJavaProject, fetchChannelPort,queryChannelIP } from "@/util/api";
import createUser from "@/views/toolsContract/components/createUser";
let Base64 = require("js-base64").Base64;
import {
    complie
} from "@/util/compile";

export default {
       components: {
        "v-createUser": createUser
    },
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
    computed: {
    },
    data() {
        var isPort = (rule, value, callback) => {
            var parten = /^([0-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/;

            if (value === '') {
                callback(new Error(this.$t('rule.isPort')))
            } else {

                if (!parten.test(value)) {
                    callback(new Error(this.$t('rule.portRule')))
                } else {
                    callback()
                }
            }

        }
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
                group: 'org.example',
                userAddress: [],
                channelIp: '127.0.0.1',
                channelPort: ''
            },
            queryPort: '',
            dynamicObject: {},
            selectedParentPath: '',
            multipleSelectedId: [],
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
                        pattern: /^[a-zA-Z_]+([a-zA-Z_][a-zA-Z0-9_]*[_])*([a-zA-Z_][a-zA-Z0-9_]*)$/,
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
                        pattern: /^[a-zA-Z_]+([a-zA-Z_][a-zA-Z0-9_]*[.])*([a-zA-Z_][a-zA-Z0-9_]*)$/,
                        message: this.$t("rule.contractGroupRule"),
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
                ],
                channelPort: [
                    { validator: isPort, trigger: 'change' }
                ]
            },
            creatUserNameVisible: false,
            isShowAddUserBtn: false
        }
    },
    destroyed() {
        this.$store.state.exportProjectShow = false
    },
    mounted() {
        this.getList();
        this.getUserInfoData()
        this.queryChannelPort()

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
                        this.userList = data;
                        if(this.userList.length==0){
                            this.isShowAddUserBtn = true;
                        }
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
        handleExpand(row) {
            this.getContractList(row, 'ExpandEvent')
        },
        clickTable: function (row, column, $event) {
            // if (expandedRows.length) {
            //     this.expands = []
            //     if (row) {
            //         this.expands.push(row.contractPath)
            //     }
            // } else {
            //     this.expands = []
            // }
            this.$nextTick(() => {
                let nodeName = $event.target.nodeName;
                if (nodeName === "I") {
                    return
                }
                this.$refs.refTable.toggleRowExpansion(row);
                this.getContractList(row, 'ExpandEvent')
            })
        },
        getContractList(row, handleType) {
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
                    if (!handleType) {
                        var selectedDirectoryInfo = {}
                        var rootDirectoryInfo = {}
                        for (var i = 0; i < this.tableData.length; i++) {
                            if (this.tableData[i]['contractPath'] == row.contractPath) {
                                selectedDirectoryInfo = this.tableData[i]
                                this.tableData.splice(i, 1);
                                break;
                            }
                        }
                        if (Object.keys(selectedDirectoryInfo).length > 0) {
                            this.tableData.unshift(selectedDirectoryInfo);
                        }
                        for (var i = 0; i < this.tableData.length; i++) {
                            if (this.tableData[i]['contractPath'] == '/') {
                                rootDirectoryInfo = this.tableData[i]
                                this.tableData.splice(i, 1);
                                break;
                            }
                        }
                        if (Object.keys(rootDirectoryInfo).length > 0) {
                            this.tableData.unshift(rootDirectoryInfo);
                        }
                    }


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
        handleSelectionChange($event, val) {
            let num = 0
            this.selectedParentPath = val.contractPath
            this.multipleSelection = $event;
            if (this.selectedParentPath) {
                this.dynamicObject[this.selectedParentPath] = this.multipleSelection
            }
            const dynamicObject = Object.values(this.dynamicObject)
            this.multipleSelectedId = []
            dynamicObject.forEach(item => { 
                item.forEach(it => {
                    this.multipleSelectedId.push(it.id)
                    if(!it.contractAbi){
                        num++
                        complie(it,this);
                    }
                })
            })
           if(num>0){  
                setTimeout(() => {
                    num =0;
                    this.getContractList(val,true);
               }, 3000)
            }  
            this.multipleSelectedId = Array.from(new Set(this.multipleSelectedId))
        },
        submit(formName) {
            if (this.multipleSelectedId.length === 0) {
                this.$message({
                    type: "error",
                    message: this.$t('rule.checkContract'),
                    customClass: 'zZindex'
                });
            } else {
                this.$refs[formName].validate((valid) => {
                    if (valid) {
                        this.export()
                    } else {
                        return false;
                    }
                })
            }

        },
        handleSelectAll(selection) {
            // console.log(selection);
            // if(!selection.length){
            //     this.$message({
            //         type:"error",
            //         message: this.$t('text.haveSelectionAll')
            //     })
            // }
        },
        export() {
            // const idList = this.multipleSelection.map(value => {
            //     return value.id
            // })
            const reqData = {
                contractIdList: this.multipleSelectedId,
                group: this.projectFrom.group,
                artifactName: this.projectFrom.artifactName,
                groupId: localStorage.getItem("groupId"),
                channelIp: this.projectFrom.channelIp,
                channelPort:this.projectFrom.channelPort
            }
            if (this.projectFrom.userAddress) {
                reqData.userAddressList = this.projectFrom.userAddress
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
                    this.$message({
                        type: 'success',
                        message: this.$t('text.exportSuccessed'),
                        customClass: 'zZindex'
                    })
                } else {
                    this.$message({
                        type: "error",
                        message: this.$chooseLang(res.data.code),
                        customClass: 'zZindex'
                    });
                }
            })
        },
        queryChannelPort() {
            fetchChannelPort(localStorage.getItem("groupId"))
                .then(res => {
                    const { data, status } = res;
                    if (status === 200) {
                        if (data.channelPort == "null") {
                            this.projectFrom.channelPort = ""
                        } else {
                            this.queryPort = true
                            this.projectFrom.channelPort = data.channelPort
                        }

                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }

                })
        },

         checkChannelIP(){
           if(!this.projectFrom.channelIp || !this.projectFrom.channelPort){
                this.$message({
                    message: "channelIp 和 channelPort 必填",
                    type: "error",
                    duration: 2000
                });
                return false;
           } 

           let param = {
                nodeIp: this.projectFrom.channelIp,
                channelPort: this.projectFrom.channelPort
            }
            queryChannelIP(param)
                .then(res => {
                    if (res.data.code === 0) {
                        if (res.data.data) {
                            this.$message({
                                type: 'success',
                                message: this.$t('text.pass')
                            })
                        }else{
                             this.$message({
                                message: this.$t('text.no'),
                                type: "error",
                            });
                        }
                    } else {
                        this.$message({
                            message: this.$chooseLang(res.data.code),
                            type: "error",
                            duration: 2000
                        });

                    }
                })

        },
        createUser(){
            this.creatUserNameVisible = true;
        },
        createUserClose(data){
             this.creatUserNameVisible = false;
             this.userList = data;
             if(this.userList.length > 0 ){
                this.isShowAddUserBtn = false;
             }
        },
    }
}
</script>

<style scoped>
.table-content {
    max-height: 270px;
    overflow: auto;
}
.block-table-content >>> .el-table__row {
    cursor: pointer;
}
.zZindex {
    z-index: 3000 !important;
}
</style>
<style>
.zZindex {
    z-index: 3000 !important;
}
</style>