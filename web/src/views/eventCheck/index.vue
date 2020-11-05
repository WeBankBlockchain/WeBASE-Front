<template>
    <div class="rivate-key-management-wrapper">
        <content-head :headTitle="$t('route.contractManagementQ')" :headSubTitle="$t('route.eventCheck')" @changeGroup="changeGroup"></content-head>
        <div class="module-wrapper">
            <div class="search-part " style="display: flex;">
                <el-form :model="contractEventForm" :rules="rules" ref="contractEventForm" class="demo-ruleForm" :inline='true'>
                    <el-form-item :label="$t('table.contractAddress')" prop="contractAddress">
                        <!-- <el-input v-model.trim="contractEventForm.contractAddress" style="width: 260px;" clearable></el-input> -->
                        <el-select v-model="contractEventForm.contractAddress" :placeholder="$t('placeholder.selected')" clearable style="width: 240px;" @change="changeAddress">
                            <el-option v-for="item in addressList" :key="item.id" :label="item.contractAddress" :value="item.contractAddress">
                                <span style="float: left; font-size: 12px">{{ item.contractAddress }}</span>
                                <span style="float: right; color: #8492a6; font-size: 12px; margin-left: 4px;">{{ item.contractName }}</span>
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item :label="$t('table.contractAbi')" prop="contractAbi">
                        <el-input v-model="contractEventForm.contractAbi" clearable :rows="1" type="textarea" style="width: 240px;"></el-input>
                    </el-form-item>
                    <el-form-item :label="$t('table.fromBlock')" prop="fromBlock">
                        <el-input v-model.number="contractEventForm.fromBlock" clearable style="width: 120px;"></el-input>
                    </el-form-item>
                    <el-form-item :label="$t('table.toBlock')" prop="toBlock">
                        <el-input v-model.number="contractEventForm.toBlock" clearable style="width: 120px;"></el-input>
                    </el-form-item>
                    <el-form-item :label="$t('table.eventName')" prop="eventName" class="event-option">
                        <el-select v-model="contractEventForm.eventName" :placeholder="$t('placeholder.selected')" style="width: 240px;" @change="changeEventName" class="event-name">
                            <el-option v-for="item in eventNameList" :key="item.value" :label="item.label" :value="item.value">
                            </el-option>
                        </el-select>
                        <!-- <el-tooltip class="item" effect="dark" :content="$t('text.eventParam')" placement="right">
                            <i class="el-icon-info"></i>
                        </el-tooltip> -->
                        <li v-for="item in inputList" class="event-info">
                            <div v-if="item.indexed" >
                                <div>{{item.name}}:</div>
                                <el-input v-model="item.value" :placeholder="item.type" style="width: 240px;" @input="e => (item.msgObj = isType(e,item.type))"></el-input>
                                <span v-if="item.msgObj&&!item.msgObj.is" class="font-color-ed5454 font-12" style="display:inline-block">
                                    {{item.msgObj.msg}}
                                </span>
                            </div>
                        </li>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="submit('contractEventForm')" :loading="loading">{{$t('dialog.confirm')}}</el-button>
                    </el-form-item>
                </el-form>
                <!-- <div slot="footer" class="dialog-footer">
                    <el-button type="primary" @click="submit('contractEventForm')" :loading="loading">{{$t('dialog.confirm')}}</el-button>
                </div> -->
            </div>
            <div class="search-table">
                <el-table :data="eventList" tooltip-effect="dark" v-loading="loading">
                    <el-table-column type="expand" align="center">
                        <template slot-scope="scope">
                            <decode-log :logInfo="logInfo(scope.row)"></decode-log>
                        </template>
                    </el-table-column>
                    <el-table-column prop="address" :label="$t('table.contractAddress')" show-overflow-tooltip  align="center">
                        <template slot-scope="scope">
                            <span>{{scope.row.address}}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="blockNumber" :label="$t('table.blockHeight')" show-overflow-tooltip width="120" align="center"></el-table-column>
                    <el-table-column prop="transactionHash" :label="$t('table.transactionHash')" show-overflow-tooltip align="center">
                        <template slot-scope="scope">
                            <span>{{scope.row.transactionHash}}</span>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </div>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import decodeLog from "@/components/decodeLog";
import { contractFindOne, contractListAll ,checkEvent} from "@/util/api"
import { validateEvent } from "@/util/validate";
const Web3Utils = require('web3-utils');
export default {
    name: 'eventCheck',

    components: {
        contentHead,
        decodeLog
    },

    props: {
    },

    data() {
        return {
            loading: false,
            contractEventForm: {
                groupId: '',
                contractAbi: '',
                contractAddress: '',
                fromBlock: '',
                toBlock: '',
                eventName: ''
            },
            groupList: localStorage.getItem("cluster")
                ? JSON.parse(localStorage.getItem("cluster"))
                : [],
            groupId: localStorage.getItem("groupId"),
            inputList: [],
            addressList: [],
            contractId: '',
            eventList: []
        }
    },

    computed: {
        rules() {
            var isAddress = (rule, value, callback) => {
                if (value == '' || value == undefined || value == null) {
                    callback();
                } else {
                    if ((!Web3Utils.isAddress(value)) && value != '') {
                        callback(new Error('Invalid input: Unexpected end of address input'));
                    } else {
                        callback();
                    }
                }
            }
            var validateBlock = (rule, value, callback) => {
                if (value == '' || value == undefined || value == null) {
                    callback();
                } else {
                    if (!Number.isInteger(value)) {
                        callback(new Error('Invalid input: Unexpected end of number input'));
                    } else {
                        callback();
                    }
                }
            }
            var obj = {
                contractAbi: [
                    {
                        required: true,
                        message: this.$t('dialog.contractAbi'),
                        trigger: "blur"
                    },
                ],
                contractAddress: [
                    {
                        required: true,
                        message: this.$t('dialog.contractAddress'),
                        trigger: "blur"
                    },
                    {
                        validator: isAddress, trigger: 'blur'
                    }
                ],
                fromBlock: [
                    {
                        required: true,
                        message: this.$t('dialog.fromBlock'),
                        trigger: "blur"
                    },
                    {
                        validator: validateBlock, trigger: 'blur'
                    }
                ],
                toBlock: [
                    {
                        required: true,
                        message: this.$t('dialog.toBlock'),
                        trigger: "blur"
                    },
                    {
                        validator: validateBlock, trigger: 'blur'
                    }
                ],

                eventName: [
                    {
                        required: true,
                        message: this.$t('dialog.eventName'),
                        trigger: "change"
                    },
                ]
            }
            return obj
        },
        eventNameList() {
            var options = []
            if(!this.contractEventForm.contractAbi) {
                return
            }
            var abiList = JSON.parse(this.contractEventForm.contractAbi)
            abiList.forEach(item => {
                if (item.type == 'event') {
                    var param = [];
                    item.inputs.forEach(it => {
                        param.push(`${it.type}`)

                    })
                    options.push({
                        label: `${item.name}`,
                        value: `${item.name}(${param.join(',')})`
                    })
                }
            });
            return options
        }
    },

    watch: {
    },

    created() {
    },

    mounted() {
        if (this.$route.query.id) {
            this.contractId = this.$route.query.id
            this.queryContractAbi()
        }
        this.queryAllAddress()
    },

    methods: {
        changeGroup() { },
        queryAllAddress() {
            let reqParam = {
                groupId: this.groupId,
                contractStatus: 2
            }
            contractListAll(reqParam)
                .then(res => {
                    if (res.data.code === 0) {
                        this.addressList = res.data.data
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        queryContractAbi() {
            contractFindOne(this.contractId)
                .then(res => {
                    if (res.data.code === 0) {
                        this.contractEventForm.contractAddress = res.data.data.contractAddress
                        this.contractEventForm.contractAbi = res.data.data.contractAbi
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        changeAddress($event) {
            this.addressList.forEach(item => {
                if (item.contractAddress == $event) {
                    this.contractId = item.id
                }
            })
            this.queryContractAbi()
        },
        changeEventName($event) {
            var eventName = $event.replace(/\((.+?)\)/g, '')
            var abiList = JSON.parse(this.contractEventForm.contractAbi)
            var eventListInfo = []
            abiList.forEach(item => {
                if (item.type === "event") {
                    eventListInfo.push(item)
                }
            })
            var inputList = []
            eventListInfo.forEach(item => {
                if (item.name === eventName) {
                    inputList = item.inputs
                }
            })
            inputList.forEach(item => {
                if (item.indexed) {
                    item.value = null;
                }
            })
            this.inputList = inputList;
        },
        submit(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.queryAdd()
                } else {
                    return false;
                }
            })
        },
        queryAdd() {
            try {
                JSON.parse(this.contractEventForm.contractAbi)
            } catch (error) {
                this.$message({
                    type: 'error',
                    message: error
                })
                return
            }
            var topicObj = {
                eventName: this.contractEventForm.eventName
            }
            var indexedList = []
            this.inputList.forEach((item, index) => {
                if (item.indexed) {
                    indexedList.push(item)

                }
            })
            indexedList.forEach((item, index) => {
                topicObj[`indexed${index + 1}`] = {
                    type: item.type,
                    value: item.value
                }
            })
            this.loading = true;
            let param = Object.assign({}, this.contractEventForm, {
                contractAbi: JSON.parse(this.contractEventForm.contractAbi),
                topics: topicObj,
                groupId: this.groupId
            })
            delete param.eventName;
            checkEvent(param)
                .then(res => {
                    this.loading = false;
                    if (res.data.code === 0) {
                        console.log(res.data.data);
                        var eventList = res.data.data;
                        var newEventList = [];
                        if(!eventList.length){
                            this.eventList = newEventList
                            return
                        }
                        eventList.forEach(item=>{
                            newEventList.push(item.log)
                        })
                        this.eventList = newEventList;
                        console.log(this.eventList);
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
        isType(val, type) {
            return validateEvent(type, val)
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

            return val

        },
        transform(type, val) {
            if (!val) {
                return val = null;
            }
            var uintReg = RegExp(/uint/);
            var bytesReg = RegExp(/bytes/);
            var boolReg = RegExp(/bool/);
            if (uintReg.test(type) || bytesReg.test(type)) {
                try {
                    return val = +val
                } catch (error) {
                    console.log(console.error())
                }

            } else if (boolReg.test(type)) {
                return val = eval(val.toLowerCase())
            } else {
                return val
            }
        },
        logInfo(row){
            var obj = {
                contractAbi: this.contractEventForm.contractAbi, 
                eventName: this.contractEventForm.eventName.replace(/[(][^）]+[\))]/g,'')
            }
            var logInfo = Object.assign({},row, obj)
            return logInfo
        }
    }
}
</script>

<style scoped>
.event-info {
    color:#fff;
}
.event-option {
    /* display: flex; */
}
.event-name >>>.el-form-item__content {
    display: flex;
}
</style>
