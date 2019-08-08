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
let errCode = {
    "101001": {
        en: "system error",
        zh: '系统异常'
    },
    "101002": {
        en: "param valid fail",
        zh: '参数校验异常'
    },
    "102000": {
        en: "system exception",
        zh: '系统异常'
    },


    "201001": {
        en: "groupId cannot be empty",
        zh: 'groupId不能为空'
    },
    "201002": {
        en: "user cannot be empty",
        zh: '用户编号不能为空'
    },
    "201003": {
        en: "useAes cannot be empty",
        zh: 'useAes不能为空'
    },
    "201004": {
        en: "version cannot be empty",
        zh: '版本号不能为空'
    },
    "201005": {
        en: "funcName cannot be empty",
        zh: '方法名不能为空'
    },
    "201006": {
        en: "abiInfo cannot be empty",
        zh: 'abi内容不能为空'
    },
    "201007": {
        en: "contractBin cannot be empty",
        zh: '合约bin不能为空'
    },
    "201008": {
        en: "contract's current version has been deployed",
        zh: '合同的当前版本已部署'
    },
    "201009": {
        en: "contract is not deployed",
        zh: '合约未部署'
    },
    "201010": {
        en: "save abi error",
        zh: 'abi保存错误'
    },
    "201011": {
        en: "contract's parameter is error",
        zh: '合约参数错误'
    },
    "201012": {
        en: "requst blockNumber is greater than latest",
        zh: '请求块高大于最新块高'
    },
    "201013": {
        en: "get abi from chain error",
        zh: '从链上获取abi失败'
    },
    "201014": {
        en: "contract deploy error",
        zh: '合约部署错误'
    },
    "201015": {
        en: "user's privateKey is null",
        zh: '用户私钥为空'
    },
    "201016": {
        en: "file is not exist",
        zh: '文件不存在'
    },
    "201017": {
        en: "failed to get node config",
        zh: '获取节点配置失败'
    },
    "201018": {
        en: "blockNumber and pbftView unchanged",
        zh: '块高和pbftview没有发生变化'
    },
    "201019": {
        en: "request function is error",
        zh: '请求错误'
    },
    "201020": {
        en: "transaction query from chain failed",
        zh: '链中的事务查询失败'
    },
    "201021": {
        en: "transaction send to chain failed",
        zh: '事务发送到链失败'
    },
    "201022": {
        en: "node request failed",
        zh: '节点请求失败'
    },
    "201023": {
        en: "contract already exists",
        zh: '合同已存在'
    },
    "201024": {
        en: "contract name cannot be repeated",
        zh: '合同名称不能重复'
    },
    "201025": {
        en: "invalid contract id",
        zh: '合同ID无效'
    },
    "201026": {
        en: "contract has been deployed",
        zh: '合同已部署'
    },
    "201027": {
        en: "send abiInfo fail",
        zh: '发送abi失败'
    },
    "201028": {
        en: "contractbin is null",
        zh: '合约bin为空'
    },
    "201029": {
        en: "contractAddress is null",
        zh: '合约地址为空'
    },
    "201030": {
        en: "contractAddress invalid",
        zh: '合约地址无效'
    },
    "201031": {
        en: "privateKey decode fail",
        zh: '私钥解码失败'
    },
    "201032": {
        en: "not found config of keyServer",
        zh: '找不到keyServer的配置'
    },
    "201033": {
        en: "cloud sign fail",
        zh: '云标志失败'
    },
    "201034": {
        en: "groupId not exist",
        zh: 'groupId不存在'
    },
    "201035": {
        en: "version and address cannot all be  null",
        zh: '合约地址不能为空'
    },
    "201037": {
        en: "user name is null",
        zh: '用户名为空'
    },
    "201038": {
        en: "user name already exists",
        zh: '用户名已经存在'
    },
    "201039": {
        en: "private key already exists",
        zh: '私钥已经存在'
    },
    "202000": {
        en: "invalid node info",
        zh: '无效的节点信息'
    },
    "202001": {
        en: "database exception",
        zh: '数据库异常'
    },
    "202002": {
        en: "organization already exists",
        zh: '组织已经存在'
    },
    "202003": {
        en: "organization not exists",
        zh: '组织信息不存在'
    },
    "202004": {
        en: "node already exists",
        zh: '节点已经存在'
    },
    "202005": {
        en: "network id cannot be empty",
        zh: '网络编号不能为空'
    },
    "202006": {
        en: "invalid network id",
        zh: '无效的网络编号'
    },
    "202007": {
        en: "organization id cannot be empty",
        zh: '组织编号不能为空'
    },
    "202008": {
        en: "invalid organization id",
        zh: '无效的组织编号'
    },
    "202009": {
        en: "network_organization_mapping already exists",
        zh: '已有该网络和组织的关联'
    },
    "202010": {
        en: "network_organization_mapping not exist",
        zh: '没有该组织和网络的关联信息'
    },
    "202011": {
        en: "did not find the current organization",
        zh: '未找到本组织信息'
    },
    "202012": {
        en: "user id cannot be empty",
        zh: '用户编号不能为空'
    },
    "202013": {
        en: "invalid user id",
        zh: '无效的用户编号'
    },
    "202014": {
        en: "user already exists",
        zh: '用户信息已经存在'
    },
    "202015": {
        en: "contract already exists",
        zh: '合约信息已经存在'
    },
    "202016": {
        en: "contract id cannot be empty",
        zh: '合约编号不能为空'
    },
    "202017": {
        en: "invalid contract id",
        zh: '无效的合约编号'
    },
    "202018": {
        en: "invalid param info",
        zh: '无效的入参信息'
    },
    "202019": {
        en: "did not find node info",
        zh: '未找到节点信息'
    },
    "202020": {
        en: "current organization already exist",
        zh: '已存在本组织信息'
    },
    "202021": {
        en: "invalid organization type",
        zh: '无效的组织类型'
    },
    "202022": {
        en: "unable to delete deployed contract",
        zh: '不能删除已部署的合约'
    },
    "202023": {
        en: "node ip cannot be empty",
        zh: '节点编号不能为空'
    },
    "202024": {
        en: "node p2p port cannot be empty",
        zh: '节点p2p端口不能为空'
    },
    "202025": {
        en: "did not find node log",
        zh: '找不到节点日志'
    },
    "202026": {
        en: "account info already exists",
        zh: '该帐号已经存在'
    },
    "202027": {
        en: "ccount info not exists",
        zh: '该帐号不存在'
    },
    "202028": {
        en: "account name empty",
        zh: '帐号名称不能为空'
    },
    "202029": {
        en: "invalid account name",
        zh: '无效的账号名称'
    },
    "202030": {
        en: "password error",
        zh: '密码错误'
    },
    "202031": {
        en: "role id cannot be empty",
        zh: '角色编号不能为空'
    },
    "202032": {
        en: "invalid role id",
        zh: '无效的角色编号'
    },
    "202033": {
        en: "invalid attr",
        zh: '无效的attr值'
    },
    "202034": {
        en: "login fail",
        zh: '登录失败'
    },
    "202035": {
        en: "contract has been deployed",
        zh: '该合约已经部署'
    },
    "202036": {
        en: "publicKey cannot be empty",
        zh: '公钥不能为空'
    },
    "202037": {
        en: "user does not exist",
        zh: '用户信息不存在'
    },
    "202038": {
        en: "interface does not exist",
        zh: '接口不存在'
    },
    "202039": {
        en: "do not save this block height",
        zh: '不保存该块高信息'
    },
    "202040": {
        en: "contract deploy not success",
        zh: '合约部署失败'
    },
    "202041": {
        en: "invalid user index",
        zh: '不能删除已部署的合约'
    },
    "202042": {
        en: "invalid contract index",
        zh: '无效的合约索引'
    },
    "202043": {
        en: "did not found system contract:contractDetail",
        zh: '找不到系统合约【contractDetail】'
    },
    "202044": {
        en: "did not found system contract:user",
        zh: '找不到系统合约【user】'
    },
    "202045": {
        en: "the new password cannot be same as old",
        zh: '新旧密码不能一致'
    },
    "202046": {
        en: "contract has not compiled",
        zh: "合约的abi或bin不存在"
    },
    "202047": {
        en: "did not found system contract:node",
        zh: "找不到系统合约【node】"
    },
    "202048": {
        en: "invalid node index",
        zh: "无效的节点索引"
    },
    "202049": {
        en: "contract name is empty",
        zh: "合约名为空"
    },
    "202050": {
        en: "publicKey's length is 130,address's length is 42",
        zh: "公钥长度为130，公钥地址长度为42"
    },
    "202051": {
        en: "invalid node ip",
        zh: "IP或者端口错误"
    },
    "202052": {
        en: "did not find current node info",
        zh: "找不到当前节点信息"
    },
    "202053": {
        en: "system user has not been initialized yet",
        zh: "系统用户尚未初始化"
    },
    "202054": {
        en: "contract had not deploy",
        zh: "合约尚未部署"
    },
    "202055": {
        en: "invalid contract",
        zh: "无效的合约"
    },
    "202058": {
        en: "not support transaction",
        zh: "不支持发交易"
    },

    "302000": {
        en: "user not logged in",
        zh: '未登录的用户'
    },
    "302001": {
        en: "access denied",
        zh: "没有权限"
    },
}
export function chooseLang(code) {
    let lang = localStorage.getItem('lang')
    let message = errCode[code][lang];
    return message
}