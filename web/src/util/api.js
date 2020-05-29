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
// import url from './url'
import { post, get, patch, put, deleted } from './http'
import { reviseParam } from './util'
import qs from 'qs'

var HANDLE = null;
if (process.env.NODE_ENV === 'development') {
    HANDLE = '/handle/'
} else {
    HANDLE = '';
}
//login
export function login(data) {
    return post({
        url: `${url.ORG_LIST}/account/login`,
        method: 'post',
        data: qs.stringify(data),
        headers: {
            'Content-Type': "application/x-www-form-urlencoded"
        }
    })
}
//out login
export function loginOut() {
    return get({
        url: `${url.ORG_LIST}/account/logout`,
        method: 'get'
    })
}
//init change passWord
export function resetPassword(data) {
    return put({
        url: `${url.ORG_LIST}/account/passwordUpdate`,
        method: 'put',
        data: data
    })
}

/**daily transaction data */
export function getChartData(data) {
    return get({
        url: `${url.ORG_LIST}/network/transDaily/${data}`,
        method: 'get'
    })
}
/**Chain overview */
export function getNetworkStatistics(data) {
    return get({
        url: `${url.ORG_LIST}/network/general/${data}`,
        method: 'get'
    })
}
/**Block list */
export function getBlockPage(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/block/blockList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/**Node list */
export function getNodeList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.FRONT_PROXY}/node/nodeList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/** Error log query node*/
export function getErrorNodeList(data) {
    return get({
        url: `${url.ORG_LIST}/node/nodeList/${data}`,
        method: 'get'
    })
}

/**Organization list */
export function getOrgList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/organization/organizationList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}

/**Add node */
export function addnodes(data) {
    return post({
        url: `${url.ORG_LIST}/node/nodeInfo`,
        method: 'post',
        data: data
    })
}
/**Increase organization */
export function addgroup(data) {
    return post({
        url: `${url.ORG_LIST}/organization/organizationInfo`,
        method: 'post',
        data: data
    })
}

/**Compile contract */
export function setCompile(data) {
    return post({
        url: `${url.ORG_LIST}/contract/compile`,
        method: 'post',
        data: data
    })
}


/**Blockchain list */
export function networkList() {
    return get({
        url: `${url.ORG_LIST}/network/all`,
        method: 'get'
    })
}
/**Modify contract */
export function editChain(data) {
    return put({
        url: `${url.ORG_LIST}/contract/contractInfo`,
        method: 'put',
        data: data
    })
}
/**add users */
export function getAddUser(data) {
    return post({
        url: `${url.ORG_LIST}/user/userInfo`,
        method: 'post',
        data: data
    })
}

/** Transaction receipt based on transaction hash*/
export function getTransactionReceipt(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/web3/transactionReceipt/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/** Get transaction information based on transaction hash*/
export function hashTransactionInfo(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/web3/transaction/${params.str}`,
        method: 'get',
        params: params.querys
    })
}

/** creat account*/
export function creatAccountInfo(data) {
    return post({
        url: `${url.ORG_LIST}/account/accountInfo`,
        method: 'post',
        data: data
    })
}
/**Change account information*/
export function modifyAccountInfo(data) {
    return put({
        url: `${url.ORG_LIST}/account/accountInfo`,
        method: 'put',
        data: data
    })
}
/** deleteAccount*/
export function deleteAccountInfo(data) {
    return deleted({
        url: `${url.ORG_LIST}/account/${data}`,
        method: 'delete'
    })
}
/** Query role list*/
export function roleList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/role/roleList${params.str}`,
        method: 'get',
        params: params.querys
    })
}

/**Query account list*/
export function accountList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/account/accountList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/** Query error log list*/
export function errorLogList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/nodeLog/nodeLogList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/** Bind user*/
export function bindUser(data) {
    return post({
        url: `${url.ORG_LIST}/user/bind`,
        method: 'post',
        data: data
    })
}

/** Regulate user transaction information*/
export function monitorTransactionInfo(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/monitor/transList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}

/** Transaction list*/
export function getTransactionList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/transaction/transList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/** Supervised user list*/
export function monitorUserList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/monitor/userList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/**
 * 监管用户接口列表
 * @param networkId
 * @param ?userName
 * */
export function monitorUserInterfaceList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/monitor/interfaceList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/**
 * 监管异常用户信息列表
 * @param networkId
 * @param pageNumber
 * @param pageSize
 * @param ?userName
 * */
export function unusualUserList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/monitor/unusualUserList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/** Regulatory abnormal contract information*/
export function unusualContractList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/monitor/unusualContractList/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/** bytecode*/
export function getByteCode(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/web3/code/${params.str}`,
        method: 'get',
        params: params.querys
    })
}

/** Query block details based on block height*/
export function getBlockDetail(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/web3/blockByNumber/${params.str}`,
        method: 'get',
        params: params.querys
    })
}

/** delete nodes*/
export function deleteNodes(data) {
    return deleted({
        url: `${url.ORG_LIST}/node/nodeInfo/${data}`,
        method: 'delete'
    })
}


// ==========================================================================

// client version
export function queryClientVersion(data) {
    return get({
        url: `${HANDLE}${data}/web3/clientVersion`,
        method: 'get'
    })
}
/**  Collection node metric*/
export function metricInfo(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}performance/${params.str}`,
        method: 'get',
        params: params.querys
    })
}
/** Machine configuration information*/
export function nodesHostInfo(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}performance/${params.str}`,
        method: 'get'
    })
}

export function nodesHealth(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}chain${params.str}`,
        method: 'get',
        params: params.querys
    })
}


//group
export function queryGroup() {
    return get({
        url: `${HANDLE}1/web3/groupList`,
        method: 'get'
    })
}
//create privateKey 
export function queryCreatePrivateKey(data) {
    return get({
        url: `${HANDLE}privateKey`,
        method: 'get',
        params: data
    })
}
//import privateKey 
export function queryImportPrivateKey(data) {
    return get({
        url: `${HANDLE}privateKey/import`,
        method: 'get',
        params: data
    })
}
//import pem privateKey 
export function ImportPemPrivateKey(data) {
    return post({
        url: `${HANDLE}privateKey/importPem`,
        method: 'post',
        data: data,
    })
}
//import p12 privateKey 
export function ImportP12PrivateKey(data) {
    return post({
        url: `${HANDLE}privateKey/importP12`,
        method: 'post',
        data: data,
        headers: {
            'Content-Type': "multipart/form-data"
        }
        
    })
}

// localKeyStores
export function queryLocalKeyStores() {
    return get({
        url: `${HANDLE}privateKey/localKeyStores`,
        method: 'get'
    })
}
//delete privateKey 
export function queryDeletePrivateKey(data) {
    return deleted({
        url: `${HANDLE}privateKey/${data}`,
        method: 'delete'
    })
}
//overview block number
export function queryBlockNumber(data) {
    return get({
        url: `${HANDLE}${data}/web3/blockNumber`,
        method: 'get'
    })
}

//overview group nodes number
export function queryNodesNumber(data) {
    return get({
        url: `${HANDLE}${data}/web3/groupPeers`,
        method: 'get'
    })
}

//overview group TX number
export function queryTxNumber(data) {
    return get({
        url: `${HANDLE}${data}/web3/transaction-total`,
        method: 'get'
    })
}
//overview pending  transactions count
export function queryPendingTxNumber(group) {
    return get({
        url: `${HANDLE}${group}/web3/pending-transactions-count`,
        method: 'get'
    })
}
//home search 
export function queryHomeSearch(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}${data}/web3/search`,
        method: 'get',
        params: params.querys,
        responseType: 'json'
    })
}
// nodes info 
export function queryNodesInfo(group) {
    return get({
        url: `${HANDLE}${group}/web3/peers`,
        method: 'get'
    })
}

// nodes info 
export function querySyncStatus(group) {
    return get({
        url: `${HANDLE}${group}/web3/syncStatus`,
        method: 'get'
    })
}
//nodes status info 
export function queryNodesStatusInfo(group) {
    return get({
        url: `${HANDLE}${group}/web3/getNodeStatusList`,
        method: 'get'
    })
}
//tx info 

export function queryTxInfo(group, txHash) {
    return get({
        url: `${HANDLE}${group}/web3/transaction/${txHash}`,
        method: 'get'
    })
}

// tx receipt info 
export function queryTxReceiptInfo(group, txHash) {
    return get({
        url: `${HANDLE}${group}/web3/transactionReceipt/${txHash}`,
        method: 'get'
    })
}

// block info 
export function queryBlockInfo(group, blockNumber) {
    return get({
        url: `${HANDLE}${group}/web3/blockByNumber/${blockNumber}`,
        method: 'get'
    })
}


/**Contract list */
export function getContractList(data) {
    return get({
        url: `${HANDLE}contract/contractList`,
        method: 'post',
        data: data
    })
}

/**add contract */
export function saveChaincode(data) {
    return post({
        url: `${HANDLE}contract/save`,
        method: 'post',
        data: data
    })
}

/**Delete contract */
export function deleteCode(data, list) {
    const params = reviseParam(data, list)
    return deleted({
        url: `${HANDLE}contract/${params.str}`,
        method: 'delete'
    })
}

/**Deployment contract */
export function getDeployStatus(data) {
    return post({
        url: `${HANDLE}contract/deploy`,
        method: 'post',
        data: data,
        responseType: 'text'
    })
}

/**Send transaction */
export function sendTransation(data) {
    return post({
        url: `${HANDLE}trans/handle`,
        method: 'post',
        data: data
    })
}
export function ifChangedDepaloy(groupId, contractId) {
    return post({
        url: `${HANDLE}contract/ifChanged/${groupId}/${contractId}`,
        method: 'get',
    })
}
//download java class 
export function queryJavaClass(data, str) {
    return post({
        url: `${HANDLE}contract/compile-java`,
        method: 'post',
        data: data,
        responseType: 'blob/json'
    })
}

export function addFunctionAbi(data) {
    return post({
        url: `${HANDLE}method/add`,
        method: 'post',
        data: data,
        headers: {
            Authorization: "Token " + localStorage.getItem("token") || ""
        }
    })
}
export function getFunctionAbi(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}method/findById/${params.str}`,
        method: 'get',
        headers: {
            Authorization: "Token " + localStorage.getItem("token") || ""
        }
    })
}
// add contractpath
export function addContractpath(data) {
    return post({
        url: `${HANDLE}contract/addContractPath`,
        method: 'post',
        data: data,
        headers: {
            Authorization: "Token " + localStorage.getItem("token") || ""
        }
    })
}
// performance toggle
export function performanceSwitch() {
    return get({
        url: `${HANDLE}performance/toggle`,
        method: 'get',
        headers: {
            Authorization: "Token " + localStorage.getItem("token") || ""
        }
    })
}

// post performance toggle
export function postPerformanceSwitch(data) {
    return post({
        url: `${HANDLE}performance/toggle`,
        method: 'post',
        data: data,
        headers: {
            Authorization: "Token " + localStorage.getItem("token") || ""
        }
    })
}

/* Background compilation*/
export function backgroundCompile(data) {
    return post({
        url: `${HANDLE}contract/contractCompile `,
        method: 'post',
        data: data
    })
}
export function encryption() {
    return get({
        url: `${HANDLE}encrypt`,
        method: 'get',
        responseType: 'text'
    })
}
/*订阅事件*/

export function blockEventList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}event/newBlockEvent/list/${params.str}`,
        method: 'get'
    })
}

export function addBlockEvent(data) {
    return post({
        url: `${HANDLE}event/newBlockEvent`,
        method: 'post',
        data: data
    })
}

export function deleteBlockEvent(data) {
    return deleted({
        url: `${HANDLE}event/newBlockEvent `,
        method: 'delete',
        data: data
    })
}
export function checkBlockEvent(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}event/newBlockEvent/${params.str}`,
        method: 'get'
    })
}


export function contractEventList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}event/contractEvent/list/${params.str}`,
        method: 'get',
        data: data
    })
}

export function addContractEvent(data) {
    return post({
        url: `${HANDLE}event/contractEvent`,
        method: 'post',
        data: data
    })
}

export function deleteContractEvent(data) {
    return deleted({
        url: `${HANDLE}event/contractEvent `,
        method: 'delete',
        data: data
    })
}
export function checkContractEvent(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}event/contractEvent/${params.str}`,
        method: 'get'
    })
}
//abi 列表
export function getAbiList(data, list) {
    const params = reviseParam(data, list);
    return get({
        url: `${HANDLE}abi/list/${params.str}`,
        method: 'GET',
        headers: {
            AuthorizationToken: "Token " + localStorage.getItem("token") || ""
        }
    })
}
export function getAbiInfo(abiId) {
    return get({
        url: `${HANDLE}abi/${abiId}`,
        method: 'GET',
        headers: {
            AuthorizationToken: "Token " + localStorage.getItem("token") || ""
        }
    })
}

export function importAbi(data) {
    return post({
        url: `${HANDLE}abi`,
        method: 'post',
        data: data,
        headers: {
            AuthorizationToken: "Token " + localStorage.getItem("token") || ""
        }
    })
}

//update imported abi
export function updateImportAbi(data) {
    return put({
        url: `${HANDLE}abi`,
        method: 'put',
        data: data,
        headers: {
            AuthorizationToken: "Token " + localStorage.getItem("token") || ""
        }
    })
}

export function deleteImportAbi(data) {
    return deleted({
        url: `${HANDLE}abi/${data}`,
        method: 'delete',
        headers: {
            AuthorizationToken: "Token " + localStorage.getItem("token") || ""
        }
    })
}


///solc/list
export function solcList() {
    return get({
        url: `${HANDLE}solc/list`,
        method: 'GET',
        headers: {
            AuthorizationToken: "Token " + localStorage.getItem("token") || ""
        }
    })
}
///solc/upload
export function solcUpload(data) {
    return post({
        url: `${HANDLE}solc/upload`,
        method: 'post',
        data: data,
        // responseType: '',
        headers: {
            AuthorizationToken: "Token " + localStorage.getItem("token") || "",
            // 'Content-Type': "application/x-www-form-urlencoded"
        }
    })
}

///solc/download
export function solcDownload(data) {
    return post({
        url: `${HANDLE}solc/download`,
        method: 'post',
        params: data,
        headers: {
            'Content-Type': "application/x-www-form-urlencoded"
        }
    })
}
//read solc version
export function readSolcVersion(fielName) {
    return get({
        url: `${HANDLE}solcjs/${fielName}`,
        method: 'GET',
        headers: {
            AuthorizationToken: "Token " + localStorage.getItem("token") || ""
        }
    })
}

//删除solc
export function deleteSolcId(solcId) {
    return deleted({
        url: `${HANDLE}solc/${solcId}`,
        method: 'delete',
        headers: {
            AuthorizationToken: "Token " + localStorage.getItem("token") || ""
        }
    })
}