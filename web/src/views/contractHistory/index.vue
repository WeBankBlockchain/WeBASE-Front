<template>
    <div>
        <v-content-head :headTitle="'合约管理'" :headSubTitle="'合约列表'"></v-content-head>
        <div class="module-wrapper">
            <div class="search-table">
                <el-table :data="contractList">
                    <el-table-column v-for="head in contractHead" :label="head.name" :key="head.enName" show-overflow-tooltip align="center">
                        <template slot-scope="scope">
                            <span>{{scope.row[head.enName]}}</span>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>
            </div>
        </div>
    </div>
</template>
<script>
import contentHead from "@/components/contentHead";
export default {
    name: "contractHistory",
    components: {
        "v-content-head": contentHead
    },
    data() {
        return {
            contractHead: [
                {
                    enName: "contractName",
                    name: "合约名"
                },
                {
                    enName: "contractAddress",
                    name: "合约地址"
                },
                {
                    enName: "contractAbi",
                    name: "abi"
                },
                {
                    enName: "contractBin",
                    name: "bin"
                },
                {
                    enName: "deployTime",
                    name: "部署时间"
                }
            ],
            contractList: [
                {
                    contractName: "UserCheck",
                    contractAddress: "0xd587a4247982173d90d6fbe77464068b0eb1e417",
                    contractAbi: JSON.stringify([
                        {
                            constant: true,
                            inputs: [{ name: "user", type: "address" }],
                            name: "check",
                            outputs: [{ name: "", type: "bool" }],
                            payable: false,
                            type: "function"
                        },
                        {
                            constant: false,
                            inputs: [{ name: "userList", type: "address[]" }],
                            name: "addUser",
                            outputs: [{ name: "", type: "address[]" }],
                            payable: false,
                            type: "function"
                        },
                        {
                            constant: true,
                            inputs: [],
                            name: "listUser",
                            outputs: [{ name: "", type: "address[]" }],
                            payable: false,
                            type: "function"
                        },
                        { inputs: [], payable: false, type: "constructor" }
                    ]),
                    contractBin: "60606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063c23697a814610051578063cdfca7f21461009f578063ed815f6214610159575bfe5b341561005957fe5b610085600480803573ffffffffffffffffffffffffffffffffffffffff169060200190919050506101ce565b604051808215151515815260200191505060405180910390f35b34156100a757fe5b6100f46004808035906020019082018035906020019080806020026020016040519081016040528093929190818152602001838360200280828437820191505050505050919050506102b3565b6040518080602001828103825283818151815260200191508051906020019060200280838360008314610146575b80518252602083111561014657602082019150602081019050602083039250610122565b5050509050019250505060405180910390f35b341561016157fe5b6101696103e4565b60405180806020018281038252838181518152602001915080519060200190602002808383600083146101bb575b8051825260208311156101bb57602082019150602081019050602083039250610197565b5050509050019250505060405180910390f35b600060008273ffffffffffffffffffffffffffffffffffffffff163273ffffffffffffffffffffffffffffffffffffffff1614151561021057600091506102ad565b600090505b6000805490508110156102a85760008181548110151561023157fe5b906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff16141561029a57600191506102ad565b5b8080600101915050610215565b600091505b50919050565b6102bb610479565b6000600090505b825181101561035457600080548060010182816102df919061048d565b916000526020600020900160005b85848151811015156102fb57fe5b90602001906020020151909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505b80806001019150506102c2565b60008054806020026020016040519081016040528092919081815260200182805480156103d657602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001906001019080831161038c575b505050505091505b50919050565b6103ec610479565b600080548060200260200160405190810160405280929190818152602001828054801561046e57602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019060010190808311610424575b505050505090505b90565b602060405190810160405280600081525090565b8154818355818115116104b4578183600052602060002091820191016104b391906104b9565b5b505050565b6104db91905b808211156104d75760008160009055506001016104bf565b5090565b905600a165627a7a72305820e8af5d7d58c25ae37a02f26733b2f26e2bd153221fe834aee394247d28fbabb50029",
                    deployTime: "2019-04-09 11:24:15"
                }
            ],
            currentPage: 1,
            pageSize: 10,
            total: 0,
        };
    },
    mounted() {},
    methods: {
         handleSizeChange: function(val) {
            this.pageSize = val;
            this.currentPage = 1;
            // this.getNodeTable();
        },
        handleCurrentChange: function(val) {
            this.currentPage = val;
            // this.getNodeTable();
        },
    }
};
</script>
<style scoped>
.search-table {
    padding: 30px 40px 10px 41px;
}
</style>
