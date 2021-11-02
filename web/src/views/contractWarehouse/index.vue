<template>
    <div>
        <content-head :headTitle="$t('route.contractWarehouse')"></content-head>
        <div class="module-wrapper" style="padding: 20px;" v-loading="loading">
            <div class="contract-introduction">
                <!-- <p>{{$t('text.appIntroduction')}}</p> -->
                <p>{{$t('text.warehouseDes')}}</p>
            </div>

            <div class="contract-app">
                <el-row :gutter="20">
                    <el-col :span="12" v-for="(item, index) in wareHouseList" :key="index">
                        <li class="item-warehouse">
                            <div class="left-warehouse">
                                <svg-icon :icon-class='item.storeIcon' class="font-120" style=""></svg-icon>
                                <!-- <el-image style="width: 120px; height: 120px" :src="item.storeIcon">
                                    <div slot="error" class="image-slot">
                                        <i class="el-icon-picture-outline"></i>
                                    </div>
                                </el-image> -->
                            </div>
                            <div class="right-warehouse">
                                <div class="right-warehouse-item store-name">
                                    <p v-if="language=='zh'">{{item.storeName}}</p>
                                    <p v-else>{{item.storeName_en}}</p>
                                </div>
                                <div class="right-warehouse-item store-desc">
                                    <p v-if="language=='zh'">{{item.storeDesc}}</p>
                                    <p v-else>{{item.storeDetail_en}}</p>
                                </div>
                                <div class="right-warehouse-item">
                                    <el-button type="primary" v-show="item.storeType!=1" size="small" @click="exportContract(item)" class="btn-item">{{$t('contracts.exportToIde')}}</el-button>
                                    <el-button type="primary" size="small" @click="toDetail(item)" class="right-btn-item">{{$t('text.previewAndDescription')}}</el-button>
                                </div>
                            </div>
                        </li>
                    </el-col>
                    <el-col :span="12">
                        <li class="item-warehouse">
                            <div class="left-warehouse">
                                <svg-icon icon-class='comingSoon' class="font-120" style=""></svg-icon>
                            </div>
                            <div class="item-warehouse-none right-warehouse">
                                {{$t('text.developing')}}
                            </div>
                            
                        </li>
                    </el-col>
                </el-row>
            </div>
            <folder v-if='folderVisible' :folderItem="folderItem" :folderVisible="folderVisible" @close="close" @success="success($event)"></folder>
        </div>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import Bus from "@/bus"
import { getContractStore, getContractItemByFolderId, batchSaveContract, getFolderItemListByStoreId } from "@/util/api"
import Folder from "@/components/Folder";
export default {
    name: 'contractWarehouse',

    components: {
        contentHead,
        Folder
    },

    props: {
    },

    data() {
        return {
            wareHouseList: [],
            language: localStorage.getItem('lang'),
            folderVisible: false,
            folderName: false,
            loading: false,
            folderItem: {}
        }
    },

    computed: {
    },

    watch: {
    },

    created() {
    },
    beforeDestroy() {
        Bus.$off("chooselanguage")
    },
    mounted() {
        if(localStorage.getItem("groupId")){
            this.queryContractStore()
        }
        Bus.$on("chooselanguage", data => {
            this.language = data
            this.queryContractStore()
        })
    },

    methods: {
        queryContractStore() {
            this.loading = true;




            
            getContractStore()
                .then(res => {
                    this.loading = false;
                     if (res.data.code === 0) {
                        var list = res.data.data;
                        this.wareHouseList = list;

                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        exportContract(item) {
            this.folderVisible = true;
            this.folderItem = item;
            console.log(item);
        },
        toDetail(val) {
            let warehouseType = val.storeType;
            this.$router.push({
                path: '/toolsContract',
                query: {
                    storeId: val.storeId,
                    storeType: val.storeType,
                    storeName: this.language != 'en'? val.storeName : val.storeName_en 
                }
            })

        },
        close() {
            this.folderVisible = false;
        },
        success(val) {
            this.folderVisible = false;
            this.folderName = val;
            this.queryContractFolder()
        },
        //通过合约仓库id 获取合约文件夹
        queryContractFolder() {
            getFolderItemListByStoreId(this.folderItem.storeId)
                .then(res => {
                    if (res.data.code === 0) {
                        let list = res.data.data;
                        if (list.length) {
                            var contractFolderId = list[0]['contractFolderId'];
                            this.queryContract(contractFolderId)
                        }
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        //通过合约文件夹id 获取合约列表
        queryContract(contractFolderId) {
            getContractItemByFolderId(contractFolderId)
                .then(res => {
                    if (res.data.code === 0) {
                        var folderContract = res.data.data;
                        this.queryBatchSaveContract(folderContract)

                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })
        },
        queryBatchSaveContract(folderContract) {
            let contractItems = folderContract.map(item => {
                return {
                    contractName: item.contractName,
                    contractSource: item.contractSrc
                }
            })
            let param = {
                contractItems: contractItems,
                contractPath: this.folderName,
                groupId: localStorage.getItem("groupId")
            }
            batchSaveContract(param)
                .then(res => {
                    if (res.data.code === 0) {
                        this.$message({
                            type: 'success',
                            message: this.$t('text.exportSuccessed')
                        })
                        this.$router.push({
                            path: "/contract",
                            query: {
                                contractPath: this.folderName
                            }
                        })
                    } else {
                        this.$message({
                            type: "error",
                            message: this.$chooseLang(res.data.code)
                        });
                    }
                })

        },
    }
}
</script>

<style scoped>
.contract-introduction {
    margin-bottom: 20px;
}
.item-warehouse {
    border: 1px solid;
    display: flex;
    flex-direction: row;
    margin-bottom: 20px;
    height: 125px;
}
.left-warehouse {
    margin-left: 10px;
    height: 100%;
}
.right-warehouse {
    margin-left: 20px;
    padding-right: 10px;
    
}
.el-row {
    /* margin-bottom: 20px; */
}
.right-warehouse-item {
    margin-top: 10px;
}
.btn-item {
    margin-left: 0;
}
.item-warehouse-none {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 120px;
}
.right-btn-item {
    margin-left: 0px;
}
.store-name {
    font-size: 14px;
}
.store-desc {
    font-size: 12px;
    height: 34px;
}
</style>
