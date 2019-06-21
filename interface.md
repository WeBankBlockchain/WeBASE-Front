# 目录
<!-- TOC -->
- [目录](#目录)
- [1.合约接口](#1合约接口)    
   - [1.1. 发送abi接口](#11-发送abi接口)        
    - [1.2. 合约部署接口](#12-合约部署接口)  
    - [1.3. cns接口](#13-cns接口)     
    - [1.4. java转译接口](#14-java转译接口)
    - [1.5. 保存合约接口](#15-保存合约接口)         
    - [1.6. 分页查询合约列表](#16-分页查询合约列表)  
- [2. 私钥接口](#2-私钥接口)
    - [2.1. 获取公私钥接口](#21-获取公私钥接口)
    - [2.2. 导入私钥接口](#22-导入私钥接口)          
- [3. web3接口](#3-web3接口)    
    - [3.1. 获取块高接口](#31-获取块高接口)        
    - [3.2. 根据块高获取块信息接口](#32-根据块高获取块信息接口) 
    - [3.3. 根据块hash获取块信息接口](#33-根据块hash获取块信息接口)        
    - [3.4. 获取块中交易个数接口](#34-获取块中交易个数接口)        
    - [3.5. 获取PbftView接口](#35-获取pbftview接口)  
    - [3.6. 获取交易回执接口](#36-获取交易回执接口) 
    - [3.7. 根据交易hash获取交易信息接口](#37-根据交易hash获取交易信息接口) 
    - [3.8. 获取web3j版本接口](#38-获取web3j版本接口)
    - [3.9. 获取合约二进制代码接口](#39-获取合约二进制代码接口) 
    - [3.10. 获取总交易数](#310-获取总交易数)
    - [3.11. 根据块hash和交易index获取交易接口](#311-根据块hash和交易index获取交易接口)
    - [3.12. 根据块高和交易index获取交易接口](#312-根据块高和交易index获取交易接口)
    - [3.13. 获取群组内的共识状态信息接口](#313-获取群组内的共识状态信息接口)
    - [3.14. 获取节点状态列表接口](#314-获取节点状态列表接口) 
    - [3.15. 获取群组列表接口](#315-获取群组列表接口)
    - [3.16. 获取观察及共识节点列表](#316-获取观察及共识节点列表)
    - [3.17. 获取群组内观察节点列表](#317-获取群组内观察节点列表)
    - [3.18. 获取群组最新的pbftview视图](#318-获取群组最新的pbftview视图)
    - [3.19. 获取已连接的P2P节点信息](#319-获取已连接的p2p节点信息)
    - [3.20. 获取群组内正在处理的交易数](#320-获取群组内正在处理的交易数)
    - [3.22. 区块/交易](#322-区块交易) 
    - [3.23. 获取群组内同步状态信息](#323-获取群组内同步状态信息)
    - [3.24. 获取交易信息接口](#324-获取交易信息接口)
    - [3.25. 获取交易回执接口](#325-获取交易回执接口)   
- [4. 性能检测接口](#4-性能检测接口)    
    - [4.1. 获取机器配置信息](#41-获取机器配置信息)     
    - [4.2. 获取机器历史性能信息](#42-获取机器历史性能信息)
- [5. 交易接口](#5-交易接口)   
    - [5.1. 交易处理接口](#51-交易处理接口)      
- [附录](#附录)    
    - [1. 返回码信息列表](#1-返回码信息列表)
<!-- /TOC -->

# 1.合约接口

## 1.1. 发送abi接口
 [top](#目录)

### 接口描述

根据abi内容判断合约是否已部署，未部署则生成对应abi文件

### 接口URL

**http://localhost:8081/webase-front/contract/abiInfo**

### 调用方法

HTTP POST

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**   | **类型**       | **最大长度** | **必填** | **说明**                           |
| -------- | -------- | ------------ | -------------- | ------------ | -------- | -------------- |
| 1        | 所属群组 | groupId | Integer         |              | 是        |                      |
| 2        | 合约名称 | contractName | String         |              | 是        |                      |
| 3        | 合约地址 | address      | String         |              | 是       |                        |
| 4        | 合约abi  | abiInfo      | List   |              | 是       | abi文件里面的内容，是一个JSONArray |
| 5        | 合约bin  | contractBin | String         |     | 是       | |


**2）数据格式**
```
{
"groupId": 1,
"contractName": "HelloWorld",
"address": "0x31b26e43651e9371c88af3d36c14cfd938baf4fd",
"contractBin": "608060405234801561001057600080fd5b5060405161031d38038061031d8339810180",
"abiInfo": [
{"inputs": [{"type": "string", "name": "n"}], "constant": false, "name": "set",
"outputs": [], "payable": false, "type": "function"},
{"inputs": [], "constant": true, "name": "get", "outputs": [{"type": "string",
"name": ""}], "payable": false, "type": "function"},
{"inputs": [], "constant": false, "name": "HelloWorld", "outputs": [],
"payable": false, "type": "function"}
]
}
```
### 响应参数

**1）数据格式**
无



## 1.2. 合约部署接口
[top](#目录)

### 接口描述

将合约部署到当前节点

### 接口URL

**http://localhost:8081/webase-front/contract/deploy**

### 调用方法

HTTP POST

### 请求参数

**1）参数表**

| **序号** | **中文**     | **参数名**   | **类型**       | **最大长度** | **必填** | **说明** |
| -------- | ------------ | ------------ | -------------- | ------------ | -------- | -------- |
| 1        | 所属群组     | groupId       | int            |            | 是       |          |
| 2        | 用户编号     | user         | String          |            | 是       | 用户编号或者用户地址 |
| 3        | 合约名称     | contractName | String         |              | 是       |          |
| 4        | 合约abi      | abiInfo      | List |              | 是       |          |
| 5        | 合约bin      | bytecodeBin  | String         |              | 是       |          |
| 6        | 构造函数参数 | funcParam    | List|              | 否       |          |
**2）数据格式**
```
{
"user":700001,
"contractName":"HelloWorld",
"abiInfo": [],
"bytecodeBin":"",
"funcParam":[]
}
```
### 响应参数
**1）数据格式**
```
{

"0x60aac015d5d41adc74217419ea77815ecb9a2192"
}
```


## 1.3. cns接口
[top](#目录)

### 接口描述

根据合约名及版本号查询合约地址

### 接口URL

**http://localhost:8081/webase-front/contract/cns?groupId={groupId}&name={name}&version={version}**

### 调用方法

HTTP POST

### 请求参数

**1）参数表**

| **序号** | **中文**     | **参数名**   | **类型**       | **最大长度** | **必填** | **说明** |
| -------- | ------------ | ------------ | -------------- | ------------ | -------- | -------- |
| 1        | 所属群组     | groupId       | int            |            | 是       |          |
| 2        | 合约名称     | name       | String         |              | 是       |          |
| 3        | 合约版本     | version      | String |              | 是       |          |

**2）数据格式**
http://localhost:8081/webase-front/contract/cns?groupId=1&name=HelloWorld&version=2

### 响应参数
**1）数据格式**
```
{

"0x31b26e43651e9371c88af3d36c14cfd938baf4fd"
}
```


## 1.4. java转译接口
[top](#目录)

### 接口描述

将合约abi转成java文件

### 接口URL

**http://localhost:8081/webase-front/contract/compile-java**

### 调用方法

HTTP POST

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**   | **类型** | **最大长度** | **必填** | **说明** |
| -------- | -------- | ------------ | -------- | ------------ | -------- | -------- |
| 1        | 合约名称     | contractName | String         |              | 是       |          |
| 2        | 合约abi      | abiInfo      | List |              | 是       |          |
| 3        | 合约bin      | bytecodeBin  | String         |              | 是       |          |
| 4        | 所在目录      | packageName  | String         |              | 是       | 生成java所在的包名 |

1. **数据格式**

```
{
    "contractName": "HeHe",
    "abiInfo": [],
    "contractBin": "60806040526004361061004c576000357c0100000000000000000000029",
    "packageName": "com.webank"
}
```

### 响应参数

**1）参数表**
java文件

**2）数据格式**
无


## 1.5. 保存合约接口
[top](#目录)

### 接口描述

支持前置的控制台保存合约信息

### 接口URL

**http://localhost:8081/webase-front/contract/save**

### 调用方法

HTTP POST

### 请求参数

**1）参数表**

| **序号** | **中文**     | **参数名**   | **类型**       | **最大长度** | **必填** | **说明** |
| -------- | ------------ | ------------ | -------------- | ------------ | -------- | -------- |
| 1        | 所属群组     | groupId       | int            |            | 是       |          |
| 2        | 合约编号     | contractId    | int          |            | 否       |  |
| 3        | 合约名称     | contractName | String         |              | 是       |          |
| 4        | 合约所在目录  | contractPath | String         |              | 是       |          |
| 5        | 合约abi      | contractAbi      | String |              | 否       |          |
| 6        | bytecodeBin      | bytecodeBin  | String         |              | 否       |          |
| 7        | 合约bin | contractBin    | String|              | 否       |          |
| 8        | 合约源码 | contractSource    | String|              | 否       |          |

**2）数据格式**
```
{
    "groupId": "1",
    "contractName": "HeHe",
    "contractPath": "/",
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsn0=",
    "contractAbi": “[]”
    "contractBin": "60806040526004361061004c576000357c0100000000000000000000000029",
    "bytecodeBin": "6080604052348015610010572269b80029",
    "contractId": 1
}
```

### 响应参数
**1）参数表**

| **序号** | **中文** | **参数名**   | **类型**       | **最大长度** | **必填** | **说明**                           |
| -------- | -------- | ------------ | -------------- | ------------ | -------- | -------------- |
| 1        | 合约编号 | id            | Integer         |              | 是        |           |
| 2        | 所在目录  | contractPath | String         |     | 是       | |
| 3        | 合约bin  | contractBin | String         |     | 是       | |
| 4        | 合约名称 | contractName | String         |              | 是        |   |
| 5        | 合约状态 | contractStatus | Integer         |       | 是       |1未部署，2已部署  |
| 6        | 所属群组 | groupId | Integer         |              | 是        |                      |
| 7        | 合约源码 | contractSource | String         |      | 否       |           |
| 8        | 合约abi | contractAbi | String         |      | 否       |           |
| 9        | 合约bin | contractBin | String         |      | 否       |           |
| 10        | bytecodeBin | bytecodeBin | String         |      | 否       |           |
| 11        | 合约地址 | contractAddress | String         |      | 否       |           |
| 12        | 部署时间 | deployTime | String         |      | 否       |           |
| 13        | 修改时间 | modifyTime | String         |      | 是       |           |
| 14        | 创建时间 | createTime | String         |      | 是       |           |
| 15        | 备注 | description | String         |      | 否       |           |



**2）数据格式**
```
{
    "id": 1,
    "contractPath": "/",
    "contractName": "HeHe",
    "contractStatus": 1,
    "groupId": 1,
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsKCmICB9Cn0=",
    "contractAbi": "[{\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]",
    "contractBin": "60806040526004361061004c5760003569b80029",
    "bytecodeBin": "608060405234801561001057600080fd5b506029",
    "contractAddress": null,
    "deployTime": null,
    "description": null,
    "createTime": "2019-06-10 11:48:51",
    "modifyTime": "2019-06-10 15:31:29"
}
```


## 1.5. 删除合约接口
[top](#目录)

### 接口描述

支持前置的控制台通过群组编号和合约编号删除未部署的合约信息

### 接口URL

**http://localhost:8081/webase-front/contract/{groupId}/{contractId}**

### 调用方法

HTTP DELETE

### 请求参数

**1）参数表**

| **序号** | **中文**     | **参数名**   | **类型**       | **最大长度** | **必填** | **说明** |
| -------- | ------------ | ------------ | -------------- | ------------ | -------- | -------- |
| 1        | 所属群组     | groupId       | int            |            | 是       |          |
| 2        | 合约编号     | contractId    | int          |            | 是       |  |

**2）数据格式**
http://localhost:8081/webase-front/contract/1/1

### 响应参数
**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **必填** | **说明**              |
| -------- | -------- | ---------- | -------- | -------- | --------------------- |
| 1        | 返回码   | code       | String   | 是       | 返回码信息请参看附录1 |
| 2        | 提示信息 | message    | String   | 是       |                       |
| 3        | 返回数据 | data       | Object   |  否        |                       |

**2）数据格式**
```
{
"code": 0,
"message": "success",
"data": null
}
```


## 1.6. 分页查询合约列表
[top](#目录)

### 接口描述

支持前置的控制台分页查询合约列表

### 接口URL

**http://localhost:8081/webase-front/contract/contractList**

### 调用方法

HTTP POST

### 请求参数

**1）参数表**

| **序号** | **中文**     | **参数名**   | **类型**       | **最大长度** | **必填** | **说明** |
| -------- | ------------ | ------------ | -------------- | ------------ | -------- | -------- |
| 1        | 所属群组 | groupId | Integer         |              | 是        |                      |
| 2        | 合约名称 | contractName | String         |              | 否        |   |
| 3        | 合约状态 | contractStatus | Integer         |       | 否       |1未部署，2已部署  |
| 4        | 合约地址 | contractAddress | String         |      | 否       |           |
| 5        | 当前页码 | pageNumber | Integer         |       | 是       | 从0开始 |
| 6        | 每页记录数 | pageSize | Integer         |       | 是       |  |


**2）数据格式**
```
{
    "groupId": "1",
    "pageNumber": 0,
    "pageSize": 10,
    "contractName": "",
    "contractAddress": "",
    "contractStatus": 2
}
```

### 响应参数
**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **必填** | **说明**              |
| -------- | -------- | ---------- | -------- | -------- | --------------------- |
| 1        | 返回码   | code       | String   | 是       | 返回码信息请参看附录1 |
| 2        | 提示信息 | message    | String   | 是       |                       |
| 3        | 返回数据 | data       | Object   |  否        |                       |
| 3.1        | 合约编号 | id            | Integer                      | 是        |           |
| 3.2        | 所在目录  | contractPath | String             | 是       | |
| 3.3        | 合约bin  | contractBin | String          | 是       | |
| 3.4        | 合约名称 | contractName | String                    | 是        |   |
| 3.5        | 合约状态 | contractStatus | Integer             | 是       |1未部署，2已部署  |
| 3.6        | 所属群组 | groupId | Integer                  | 是        |                      |
| 3.7        | 合约源码 | contractSource | String            | 否       |           |
| 3.8        | 合约abi | contractAbi | String          | 否       |           |
| 3.9        | 合约bin | contractBin | String            | 否       |           |
| 3.10        | bytecodeBin | bytecodeBin | String           | 否       |           |
| 3.11        | 合约地址 | contractAddress | String           | 否       |           |
| 3.12        | 部署时间 | deployTime | String            | 否       |           |
| 3.13        | 修改时间 | modifyTime | String            | 是       |           |
| 3.14        | 创建时间 | createTime | String             | 是       |           |
| 3.15        | 备注 | description | String           | 否       |           |

**2）数据格式**
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "id": 2,
            "contractPath": "/",
            "contractName": "HeHe",
            "contractStatus": 1,
            "groupId": 1,
            "contractSource": "cHJhZ21hIHNvbGlkaXR5IICB9Cn0=",
            "contractAbi": "",
            "contractBin": "",
            "bytecodeBin": null,
            "contractAddress": null,
            "deployTime": null,
            "description": null,
            "createTime": "2019-06-10 16:42:50",
            "modifyTime": "2019-06-10 16:42:52"
        }
    ],
    "totalCount": 1
}
```



# 2. 私钥接口

## 2.1. 获取公私钥接口
[top](#目录)

### 接口描述

通过调用此接口获取公私钥对和对应账户信息

### 接口URL

**http://localhost:8081/webase-front/privateKey?useAes={useAes}&userName={userName}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文**     | **参数名**   | **类型**       | **最大长度** | **必填** | **说明** |
| -------- | ------------ | ------------ | -------------- | ------------ | -------- | -------- |
| 1        | 是否需要加密私钥 | useAes | boolean      |             | 是        |                      |
| 2        | 用户名 | userName | String        |             | 是        |   |

**2）数据格式**
`http://localhost:8081/webase-front/privateKey?useAes=false&userName=test`
### 响应参数

**1）数据格式**
```
{

"publicKey":"1c7073dc185af0644464b178da932846666a858bc492450e9e94c77203428ed54e2ce45679dbb36bfed714dbe055a215dc1aaf4a75faeddce6a62b39c0158e1e",
"privateKey":"008cf98bd0f37fb0984ab43ed6fc2dcdf58811522af7e4a3bedbe84636a79a501c",
"address":"0x2e8ff65fb1b2ce5b0c9476b8f8beb221445f42ee"
}
```



## 2.2. 导入私钥接口
[top](#目录)

### 接口描述

导入私钥信息，并返回对应的公钥及用户地址

### 接口URL

**http://localhost:8081/webase-front/privateKey/import?privateKey={privateKey}&useAes={useAes}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文**     | **参数名**   | **类型**       | **最大长度** | **必填** | **说明** |
| -------- | ------------ | ------------ | -------------- | ------------ | -------- | -------- |
| 1        | 私钥信息 | privateKey | String      |             | 是        |                      |
| 2        | 是否是加密私钥 | useAes | boolean      |             | 否        |      默认true       |

**2）数据格式**
`http://localhost:8081/webase-front/privateKey/import?privateKey=008cf98bd0f37fb0984ab43ed6fc2dcdf58811522af7e4a3bedbe84636a79a501c&useAes=false`

### 响应参数

**1）数据格式**
```
{

"publicKey":"1c7073dc185af0644464b178da932846666a858bc492450e9e94c77203428ed54e2ce45679dbb36bfed714dbe055a215dc1aaf4a75faeddce6a62b39c0158e1e",
"privateKey":"008cf98bd0f37fb0984ab43ed6fc2dcdf58811522af7e4a3bedbe84636a79a501c",
"address":"0x2e8ff65fb1b2ce5b0c9476b8f8beb221445f42ee"
}
```




# 3. web3接口

## 3.1. 获取块高接口
[top](#目录)

### 接口描述

获取节点最新块高

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/blockNumber**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文**     | **参数名**   | **类型**       | **最大长度** | **必填** | **说明** |
| -------- | ------------ | ------------ | -------------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |

**2）数据格式**
http://localhost:8081/webase-front/1/web3/blockNumber


### 响应参数
**1）数据格式**
```
8346
```


## 3.2. 根据块高获取块信息接口
[top](#目录)

### 接口描述

通过块高获取块信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/blockByNumber/{blockNumber}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**  | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ----------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |
| 2        | 块高     | blockNumber | BigInteger |       | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/blockByNumber/100


### 响应参数

**1）数据格式**
```
{
    "code": 0,
    "message": "success",
    "data": {
        "number": 100,
        "hash": "0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",
        "parentHash": "0xc784a2af86e6726fcdc63b57ed1b91a40efc7d8b1b7285154d399ea78bd18754",
        "nonce": 0,
        "sha3Uncles": "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
        "logsBloom": "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000040000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000800000000000000000000000000",
        "transactionsRoot": "0x27055bac669a67e8aaa1455ad9fd70b75dd86dc905c6bd0d111ed67ab182d1dc",
        "stateRoot": "0xa05ad5db11b8be6aed3b591f2f64fdbb241506cbe9ef591f3a4b946ca777f838",
        "receiptsRoot": "0xc1d4b43ed68d7263ddf50861feec26440e933a0b152917e938194f5079b48ce4",
        "author": "0x0000000000000000000000000000000000000000",
        "miner": "0x0000000000000000000000000000000000000000",
        "mixHash": null,
        "difficulty": 1,
        "totalDifficulty": 101,
        "extraData": "0xd98097312e332e302b2b302d524c696e75782f672b2b2f496e74",
        "size": 71,
        "gasLimit": 2000000000,
        "gasUsed": 121038,
        "timestamp": 1526437108478,
        "transactions": [
            {
                "hash": "0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",
                "nonce": 9.1623055443573E+74,
                "blockHash": "0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",
                "blockNumber": 100,
                "transactionIndex": 0,
                "from": "0x59bd3815f73b197d6ef327f2a45089f50aba942a",
                "to": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",
                "value": 0,
                "gasPrice": 30000000,
                "gas": 30000000,
                "input": "0x48f85bce000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a4",
                "creates": null,
                "publicKey": null,
                "raw": null,
                "r": null,
                "s": null,
                "v": 0,
                "valueRaw": "0x0",
                "gasPriceRaw": "0x1c9c380",
                "gasRaw": "0x1c9c380",
                "blockNumberRaw": "0x64",
                "transactionIndexRaw": "0x0",
                "nonceRaw": "0x2069170146129593df177e2c37f1b7fe74e2d0fda53dcbbca34a243d46e367a"
            }
        ],
        "uncles": [],
        "sealFields": null,
        "gasUsedRaw": "0x1d8ce",
        "totalDifficultyRaw": "0x65",
        "numberRaw": "0x64",
        "nonceRaw": null,
        "sizeRaw": "0x47",
        "gasLimitRaw": "0x77359400",
        "timestampRaw": "0x16366bddafe",
        "difficultyRaw": "0x1"
    }
}
```



## 3.3. 根据块hash获取块信息接口
[top](#目录)

### 接口描述

通过块hash获取块信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/blockByHash/{blockHash}**

### 调用方法

HTTP GET


### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**  | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ----------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |
| 2        | 区块hash     | blockByHash | String |       | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/blockByHash/0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade



### 响应参数

**1）数据格式**
```
{
    "code": 0,
    "message": "success",
    "data": {
        "number": 100,
        "hash": "0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",
        "parentHash": "0xc784a2af86e6726fcdc63b57ed1b91a40efc7d8b1b7285154d399ea78bd18754",
        "nonce": 0,
        "sha3Uncles": "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
        "logsBloom": "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000040000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000800000000000000000000000000",
        "transactionsRoot": "0x27055bac669a67e8aaa1455ad9fd70b75dd86dc905c6bd0d111ed67ab182d1dc",
        "stateRoot": "0xa05ad5db11b8be6aed3b591f2f64fdbb241506cbe9ef591f3a4b946ca777f838",
        "receiptsRoot": "0xc1d4b43ed68d7263ddf50861feec26440e933a0b152917e938194f5079b48ce4",
        "author": "0x0000000000000000000000000000000000000000",
        "miner": "0x0000000000000000000000000000000000000000",
        "mixHash": null,
        "difficulty": 1,
        "totalDifficulty": 101,
        "extraData": "0xd98097312e332e302b2b302d524c696e75782f672b2b2f496e74",
        "size": 71,
        "gasLimit": 2000000000,
        "gasUsed": 121038,
        "timestamp": 1526437108478,
        "transactions": [
            {
                "hash": "0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",
                "nonce": 9.1623055443573E+74,
                "blockHash": "0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",
                "blockNumber": 100,
                "transactionIndex": 0,
                "from": "0x59bd3815f73b197d6ef327f2a45089f50aba942a",
                "to": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",
                "value": 0,
                "gasPrice": 30000000,
                "gas": 30000000,
                "input": "0x48f85bce000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a4",
                "creates": null,
                "publicKey": null,
                "raw": null,
                "r": null,
                "s": null,
                "v": 0,
                "valueRaw": "0x0",
                "gasPriceRaw": "0x1c9c380",
                "gasRaw": "0x1c9c380",
                "blockNumberRaw": "0x64",
                "transactionIndexRaw": "0x0",
                "nonceRaw": "0x2069170146129593df177e2c37f1b7fe74e2d0fda53dcbbca34a243d46e367a"
            }
        ],
        "uncles": [],
        "sealFields": null,
        "gasUsedRaw": "0x1d8ce",
        "totalDifficultyRaw": "0x65",
        "numberRaw": "0x64",
        "nonceRaw": null,
        "sizeRaw": "0x47",
        "gasLimitRaw": "0x77359400",
        "timestampRaw": "0x16366bddafe",
        "difficultyRaw": "0x1"
    }
}
```




## 3.4. 获取块中交易个数接口
[top](#目录)

### 接口描述

> 根据块高获取该块中的交易个数

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/blockTransCnt/{blockNumber}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**  | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ----------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |
| 2        | 块高     | blockNumber | BigInteger |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/blockTransCnt/100

### 响应参数

**1）数据格式**
```
 1
```


## 3.5. 获取PbftView接口
[top](#目录)

### 接口描述

通过调用此接口获取PbftView

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/pbftView**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**
| **序号** | **中文** | **参数名**  | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ----------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |


**2）数据格式**
`http://localhost:8081/webase-front/1/web3/pbftView`


### 响应参数

**1）数据格式**
```
 438328
```



## 3.6. 获取交易回执接口
[top](#目录)

### 接口描述

> 根据交易hash获取交易回执

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/transactionReceipt/{transHash}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------- | -------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |
| 2        | 交易hash | transHash  | String   |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/transactionReceipt/0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b

### 响应参数

**2）数据格式**
```
{
    "transactionHash": "0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",
    "transactionIndex": 0,
    "blockHash": "0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",
    "blockNumber": 100,
    "cumulativeGasUsed": 121038,
    "gasUsed": 121038,
    "contractAddress": "0x0000000000000000000000000000000000000000",
    "root": null,
    "from": null,
    "to": null,
    "logs": [
        {
            "removed": false,
            "logIndex": 0,
            "transactionIndex": 0,
            "transactionHash": "0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",
            "blockHash": "0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",
            "blockNumber": 100,
            "address": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",
            "data": "0x00000000000000000000000000000000000000000000000000000000000000c000000000000000000000000000000000000000000000000000000000000001200000000000000000000000000000000000000000000000000000000000000160000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a40000000000000000000000000000000000000000000000000000000000000040666164363863656230616530316530643731616635356331316561643031613532656638363435343866306134643133633836363164393664326461366239380000000000000000000000000000000000000000000000000000000000000002363000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000023630000000000000000000000000000000000000000000000000000000000000",
            "type": "mined",
            "topics": [
                "0xbf474e795141390215f4f179557402a28c562b860f7b74dce4a3c0e0604cd97e"
            ],
            "logIndexRaw": "0",
            "blockNumberRaw": "100",
            "transactionIndexRaw": "0"
        }
    ],
    "logsBloom": null,
    "gasUsedRaw": "0x1d8ce",
    "blockNumberRaw": "100",
    "transactionIndexRaw": "0",
    "cumulativeGasUsedRaw": "0x1d8ce"
}
```




## 3.7. 根据交易hash获取交易信息接口
[top](#目录)

### 接口描述

> 根据交易hash获取交易信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/transaction/{transHash}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------- | -------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |
| 2        | 交易hash | transHash  | String   |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/transaction/0xa6750b812b1a7e36313879b09f0c41fc583b463c15e57608416f3a32688b432b

### 响应参数

**1）数据格式**
```
{
    "hash": "0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",
    "nonce": 9.1623055443573E+74,
    "blockHash": "0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",
    "blockNumber": 100,
    "transactionIndex": 0,
    "from": "0x59bd3815f73b197d6ef327f2a45089f50aba942a",
    "to": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",
    "value": 0,
    "gasPrice": 30000000,
    "gas": 30000000,
    "input": "0x48f85bce000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a4",
    "creates": null,
    "publicKey": null,
    "raw": null,
    "r": null,
    "s": null,
    "v": 0,
    "nonceRaw": "0x2069170146129593df177e2c37f1b7fe74e2d0fda53dcbbca34a243d46e367a",
    "blockNumberRaw": "0x64",
    "transactionIndexRaw": "0x0",
    "valueRaw": "0x0",
    "gasPriceRaw": "0x1c9c380",
    "gasRaw": "0x1c9c380"
}
```

## 3.8. 获取web3j版本接口
[top](#目录)

### 接口描述

> 获取web3j版本

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/clientVersion**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

无入参

**2）数据格式**
http://localhost:8081/webase-front/1/web3/clientVersion

### 响应参数

**1）参数表**
无

**2）数据格式**
···
{
    "Build Time": "20190318 10:56:37",
    "Build Type": "Linux/g++/RelWithDebInfo",
    "FISCO-BCOS Version": "2.0.0-rc1",
    "Git Branch": "master",
    "Git Commit Hash": "2467ddf73b091bc8e0ee611ccee85db7989ad389"
}
···

## 3.9. 获取合约二进制代码接口
[top](#目录)

### 接口描述

> 获取指定块高区块指定合约地址的二进制代码

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/code/{address}/{blockNumber}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**  | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ----------- | ---------- | ------------ | -------- | -------- |
| 1        | 合约地址 | address     | String     |              | 是       |          |
| 2        | 块高     | blockNumber | BigInteger |              | 是       |          |

1. **数据格式**

http://localhost:8081/webase-front/1/web3/code/0x0000000000000000000000000000000000000000/1

### 响应参数

**2）数据格式**
```
0x
```


## 3.10. 获取总交易数
[top](#目录)

### 接口描述

> 获取总交易数量

### 接口URL

**http://localhost:8081/webase-front/1/transaction-total**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

**2）数据格式**

http://localhost:8081/webase-front/1/web3/transaction-total

### 响应参数

**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------- | -------- | ------------ | -------- | -------- |
| 1        | 总交易数 | txSum | int      |             | 是        |                      |
| 2        | 快高 | blockNumber  | int   |              | 是       |          |
| 3        |  | blockNumberRaw  | String   |              | 是       |          |
| 4        |  | txSumRaw  | String   |              | 是       |          |


**2）数据格式**
```
{
  "txSum": 125,
  "blockNumber": 125,
  "blockNumberRaw": "0x7d",
  "txSumRaw": "0x7d"
}
```

## 3.11. 根据块hash和交易index获取交易接口
[top](#目录)

### 接口描述

> 获取指定区块指定位置的交易信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/transByBlockHashAndIndex/{blockHash}/{transactionIndex}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 块hash   | blockHash        | String     |              | 是       |          |
| 2        | 交易位置 | transactionIndex | BigInteger |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/transByBlockHashAndIndex/0x0d9ed7b20645d5b8200347a72e7fb15347b83d74c6e1b6c3995cdb7a849f95d9/0

### 响应参数

**2）数据格式**
```
{
    "hash": "0x7c503f202a5e275d8792dd2419ac48418dbec602038fb2a85c899403471f065d",
    "nonce": 1.26575985412899E+75,
    "blockHash": "0x0d9ed7b20645d5b8200347a72e7fb15347b83d74c6e1b6c3995cdb7a849f95d9",
    "blockNumber": 100,
    "transactionIndex": 0,
    "from": "0x6f00a620a61fd6b33e6076880fecc49959eaa4ea",
    "to": "0x9cb5641d991df690ed905c34f9aaf22370034220",
    "value": 0,
    "gasPrice": 1,
    "gas": 100000000,
    "input": "0x4ed3885e000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000016100000000000000000000000000000000000000000000000000000000000000",
    "creates": null,
    "publicKey": null,
    "raw": null,
    "r": null,
    "s": null,
    "v": 0,
    "blockNumberRaw": "0x64",
    "nonceRaw": "0x2cc650a5cbeb268577ac15c7dd2afee0680901de94f8543e86e906247e7edbf",
    "valueRaw": "0x0",
    "gasPriceRaw": "0x1",
    "gasRaw": "0x5f5e100",
    "transactionIndexRaw": "0x0"
}
```


## 3.12. 根据块高和交易index获取交易接口
[top](#目录)

### 接口描述

> 获取指定区块指定位置的交易信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/transByBlockNumberAndIndex/{blockNumber}/{transactionIndex}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |
| 2        | 块高     | blockNumber      | BigInteger |              | 是       |          |
| 3        | 交易位置 | transactionIndex | BigInteger |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/transByBlockNumberAndIndex/100/0

### 响应参数

**1）数据格式**
```
{
    "hash": "0x7c503f202a5e275d8792dd2419ac48418dbec602038fb2a85c899403471f065d",
    "nonce": 1.26575985412899E+75,
    "blockHash": "0x0d9ed7b20645d5b8200347a72e7fb15347b83d74c6e1b6c3995cdb7a849f95d9",
    "blockNumber": 100,
    "transactionIndex": 0,
    "from": "0x6f00a620a61fd6b33e6076880fecc49959eaa4ea",
    "to": "0x9cb5641d991df690ed905c34f9aaf22370034220",
    "value": 0,
    "gasPrice": 1,
    "gas": 100000000,
    "input": "0x4ed3885e000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000016100000000000000000000000000000000000000000000000000000000000000",
    "creates": null,
    "publicKey": null,
    "raw": null,
    "r": null,
    "s": null,
    "v": 0,
    "blockNumberRaw": "0x64",
    "nonceRaw": "0x2cc650a5cbeb268577ac15c7dd2afee0680901de94f8543e86e906247e7edbf",
    "valueRaw": "0x0",
    "gasPriceRaw": "0x1",
    "gasRaw": "0x5f5e100",
    "transactionIndexRaw": "0x0"
}
```



## 3.13. 获取群组内的共识状态信息接口
[top](#目录)

### 接口描述

> 返回指定群组内的共识状态信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/consensusStatus**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/consensusStatus

### 响应参数

**1）数据格式**
```
[
    {
        "accountType": 1,
        "allowFutureBlocks": true,
        "cfgErr": false,
        "connectedNodes": 3,
        "consensusedBlockNumber": 126,
        "currentView": 499824,
        "groupId": 1,
        "highestblockHash": "0x563d4ec57b597d5d81f0c1b0045c04e57ffebe3a02ff3fef402be56742dc8fd1",
        "highestblockNumber": 125,
        "leaderFailed": false,
        "max_faulty_leader": 1,
        "node index": 2,
        "nodeId": "d822165959a0ed217df6541f1a7dd38b79336ff571dd5f8f85ad76f3e7ec097e1eabd8b03e4a757fd5a9fb0eea905aded56aaf44df83c34b73acb9ab7ac65010",
        "nodeNum": 4,
        "omitEmptyBlock": true,
        "protocolId": 264,
        "sealer.0": "552398be0eef124c000e632b0b76a48c52b6cfbd547d92c15527c2d1df15fab2bcded48353db22526c3540e4ab2027630722889f20a4a614bb11a7887a85941b",
        "sealer.1": "adfa2f9116d7ff68e0deb75307fa1595d636bf097ad1de4fb55cff00e4fef40b453abb30388aa2112bf5cd4c987afe2e047250f7049791aa1ee7091c9e2ab7bb",
        "sealer.2": "d822165959a0ed217df6541f1a7dd38b79336ff571dd5f8f85ad76f3e7ec097e1eabd8b03e4a757fd5a9fb0eea905aded56aaf44df83c34b73acb9ab7ac65010",
        "sealer.3": "dde0bbf5eb3a731e6da861586e98e088e16e6fdd9afae2f2c213cead20a4f5eaa3910042b70d62266d2350d98a43c1f235c8e0da384448384893857873abdb75",
        "toView": 499824
    },
    [
        {
            "nodeId": "552398be0eef124c000e632b0b76a48c52b6cfbd547d92c15527c2d1df15fab2bcded48353db22526c3540e4ab2027630722889f20a4a614bb11a7887a85941b",
            "view": 499823
        },
        {
            "nodeId": "adfa2f9116d7ff68e0deb75307fa1595d636bf097ad1de4fb55cff00e4fef40b453abb30388aa2112bf5cd4c987afe2e047250f7049791aa1ee7091c9e2ab7bb",
            "view": 499820
        },
        {
            "nodeId": "d822165959a0ed217df6541f1a7dd38b79336ff571dd5f8f85ad76f3e7ec097e1eabd8b03e4a757fd5a9fb0eea905aded56aaf44df83c34b73acb9ab7ac65010",
            "view": 499824
        },
        {
            "nodeId": "dde0bbf5eb3a731e6da861586e98e088e16e6fdd9afae2f2c213cead20a4f5eaa3910042b70d62266d2350d98a43c1f235c8e0da384448384893857873abdb75",
            "view": 499822
        }
    ],
    {
        "prepareCache_blockHash": "0x0000000000000000000000000000000000000000000000000000000000000000",
        "prepareCache_height": -1,
        "prepareCache_idx": "65535",
        "prepareCache_view": "9223372036854775807"
    },
    {
        "rawPrepareCache_blockHash": "0x0000000000000000000000000000000000000000000000000000000000000000",
        "rawPrepareCache_height": -1,
        "rawPrepareCache_idx": "65535",
        "rawPrepareCache_view": "9223372036854775807"
    },
    {
        "committedPrepareCache_blockHash": "0x15cf36c1f15572c448f7d4295958972e6b876deef319c532b8f7d79fcbde072f",
        "committedPrepareCache_height": 125,
        "committedPrepareCache_idx": "1",
        "committedPrepareCache_view": "62209"
    },
    {
        "signCache_cachedSize": "0"
    },
    {
        "commitCache_cachedSize": "0"
    },
    {
        "viewChangeCache_cachedSize": "0"
    }
]
```


## 3.14. 获取节点状态列表接口
[top](#目录)

### 接口描述

> 返回节点的快高、pbftview及状态

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/getNodeStatusList**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |


**2）数据格式**

http://localhost:8081/webase-front/1/web3/getNodeStatusList

### 响应参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 节点Id | nodeId | String      |             | 是        |                      |
| 2        | 节点块高 | blockNumber | bigInteger      |             | 是        |                      |
| 3        | 节点pbftView | pbftView | bigInteger      |             | 是        |                      |
| 4        | 节点状态 | status | int     |     | 是    |   1正常，2异常     |
| 5        | 上次状态修改时间 | latestStatusUpdateTime | String   |   | 是   | 跟上次状态变更时间间隔至少大于三秒才会重新检测节点状态 |


## 3.15. 获取群组列表接口
[top](#目录)

### 接口描述

> 返回群组列表

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/groupList**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |


**2）数据格式**

http://localhost:8081/webase-front/1/web3/groupList

### 响应参数

**1）数据格式**
```
[
  "1",
  "2"
]
```



## 3.16. 获取观察及共识节点列表
[top](#目录)

### 接口描述

> 返回指定群组内的共识节点和观察节点列表

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/groupPeers**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |


**2）数据格式**

http://localhost:8081/webase-front/1/web3/groupPeers

### 响应参数

**1）数据格式**
```
[
    "d822165959a0ed217df6541f1a7dd38b79336ff571dd5f8f85ad76f3e7ec097e1eabd8b03e4a757fd5a9fb0eea905aded56aaf44df83c34b73acb9ab7ac65010",
 "adfa2f9116d7ff68e0deb75307fa1595d636bf097ad1de4fb55cff00e4fef40b453abb30388aa2112bf5cd4c987afe2e047250f7049791aa1ee7091c9e2ab7bb",
 "552398be0eef124c000e632b0b76a48c52b6cfbd547d92c15527c2d1df15fab2bcded48353db22526c3540e4ab2027630722889f20a4a614bb11a7887a85941b",
 "dde0bbf5eb3a731e6da861586e98e088e16e6fdd9afae2f2c213cead20a4f5eaa3910042b70d62266d2350d98a43c1f235c8e0da384448384893857873abdb75"
]
```



## 3.17. 获取群组内观察节点列表
[top](#目录)

### 接口描述

> 返回指定群组内的观察节点列表

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/observerList**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |


**2）数据格式**

http://localhost:8081/webase-front/1/web3/observerList

### 响应参数

**1）数据格式**
```
[
    "d822165959a0ed217df6541f1a7dd38b79336ff571dd5f8f85ad76f3e7ec097e1eabd8b03e4a757fd5a9fb0eea905aded56aaf44df83c34b73acb9ab7a165010"
]
```



## 3.18. 获取群组最新的pbftview视图
[top](#目录)

### 接口描述

> 返回指定群组内的pbftview

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/pbftView**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |


**2）数据格式**

http://localhost:8081/webase-front/1/web3/pbftView

### 响应参数

**1）数据格式**
```
859
```


## 3.19. 获取已连接的P2P节点信息
[top](#目录)

### 接口描述

> 返回指定群组内已连接的P2P节点信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/peers**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |


**2）数据格式**

http://localhost:8081/webase-front/1/web3/peers

### 响应参数

**1）数据格式**
```
[
    {
        "ipandPort": "127.0.0.1:30301",
        "IPAndPort": "127.0.0.1:30301",
        "NodeID": "adfa2f9116d7ff68e0deb75307fa1595d636bf097ad1de4fb55cff00e4fef40b453abb30388aa2112bf5cd4c987afe2e047250f7049791aa1ee7091c9e2ab7bb",
        "Topic": []
    },
    {
        "ipandPort": "127.0.0.1:57678",
        "IPAndPort": "127.0.0.1:57678",
        "NodeID": "e28f3d7f5b82e21918a15639eac342dcf678ebb0efe7c65c76514b0ba6b28ace8e47b4a25c9b3f9763b79db847e250a19f827b132f230298980f3ca9779c2564",
        "Topic": []
    },
    {
        "ipandPort": "127.0.0.1:57608",
        "IPAndPort": "127.0.0.1:57608",
        "NodeID": "dde0bbf5eb3a731e6da861586e98e088e16e6fdd9afae2f2c213cead20a4f5eaa3910042b70d62266d2350d98a43c1f235c8e0da384448384893857873abdb75",
        "Topic": []
    },
    {
        "ipandPort": "127.0.0.1:57616",
        "IPAndPort": "127.0.0.1:57616",
        "NodeID": "552398be0eef124c000e632b0b76a48c52b6cfbd547d92c15527c2d1df15fab2bcded48353db22526c3540e4ab2027630722889f20a4a614bb11a7887a85941b",
        "Topic": []
    },
    {
        "ipandPort": "127.0.0.1:57670",
        "IPAndPort": "127.0.0.1:57670",
        "NodeID": "56edfaf60bcb09b9814ad31dcd959eb388f0314445db3deb92cedde97c0ecec210f713591a15f3a7168ba023290cfbe78656b57c37157e6ec74a85182630bd61",
        "Topic": []
    }
]
```



## 3.20. 获取群组内正在处理的交易数
[top](#目录)

### 接口描述

> 获取群组内正在处理的交易数

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/pending-transactions-count**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |


**2）数据格式**

http://localhost:8081/webase-front/1/web3/pending-transactions-count

### 响应参数

**1）数据格式**
```
0
```


## 3.21. 获取共识节点接口
[top](#目录)

### 接口描述

> 返回群组内共识节点列表

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/sealerList**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |


**2）数据格式**

http://localhost:8081/webase-front/1/web3/sealerList

### 响应参数

**1）数据格式**
```
[
    "d822165959a0ed217df6541f1a7dd38b79336ff571dd5f8f85ad76f3e7ec097e1eabd8b03e4a757fd5a9fb0eea905aded56aaf44df83c34b73acb9ab7ac65010",
 "adfa2f9116d7ff68e0deb75307fa1595d636bf097ad1de4fb55cff00e4fef40b453abb30388aa2112bf5cd4c987afe2e047250f7049791aa1ee7091c9e2ab7bb",
 "552398be0eef124c000e632b0b76a48c52b6cfbd547d92c15527c2d1df15fab2bcded48353db22526c3540e4ab2027630722889f20a4a614bb11a7887a85941b",
 "dde0bbf5eb3a731e6da861586e98e088e16e6fdd9afae2f2c213cead20a4f5eaa3910042b70d62266d2350d98a43c1f235c8e0da384448384893857873abdb75"
]
```


## 3.22. 区块/交易
[top](#目录)

### 接口描述

> 如果输入块高就返回区块信息，如果输入交易hash就返回交易信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/search?input={inputValue}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |
| 2        | 查询参数 | inputValue | int/String | | 是 | 如果输入块高就返回区块信息，如果输入交易hash就返回交易信息|


**2）数据格式**

http://localhost:8081/webase-front/1/web3/search?input=1



### 响应参数

**1）数据格式**
```
{
    "number": 1,
    "hash": "0x3875dbec6e0ad0790dc0a0e8535b7c286ef7cee4149e5b1494f5c65631a9e321",
    "parentHash": "0xed3350d191d23cbc609c98e920baa583403b9a02fa934df868e7f425cd72f5c3",
    "nonce": 0,
    "sha3Uncles": null,
    "logsBloom": "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
    "transactionsRoot": "0xa3db8478e08931f8967023a60d260b182d828aad959433e0b77f097d7650b742",
    "stateRoot": "0xf32d3e504fc8813c139d1f6f61ae1c8e355502e10b9ea24e5ad5d3ada01ea400",
    "receiptsRoot": null,
    "author": null,
    "sealer": "0x0",
    "mixHash": null,
    "difficulty": 0,
    "totalDifficulty": 0,
    "extraData": [],
    "size": 0,
    "gasLimit": 0,
    "gasUsed": 0,
    "timestamp": 1557304350431,
    "transactions": [
        {
            "hash": "0x4145b921309fcaa92b05b782e0181d671b8e68fc6d61d939358ed558fa3489c9",
            "nonce": 1.47418536037145E+75,
            "blockHash": "0x3875dbec6e0ad0790dc0a0e8535b7c286ef7cee4149e5b1494f5c65631a9e321",
            "blockNumber": 1,
            "transactionIndex": 0,
            "from": "0x33a41878e78fb26735bf425f9328990e3a1a89df",
            "to": null,
            "value": 0,
            "gasPrice": 1,
            "gas": 100000000,
            "input": "0x6080604052348015600f57600080fd5b5060868061001e6000396000f300608060405260043610603f576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806335b09a6e146044575b600080fd5b348015604f57600080fd5b5060566058565b005b5600a165627a7a723058204aacdb57d6f2ae0f7f6c89c28236bba0205631183fd99785de220481566e683f0029",
            "creates": null,
            "publicKey": null,
            "raw": null,
            "r": null,
            "s": null,
            "v": 0,
            "nonceRaw": "0x3425bfe0f36e343686ccbe34a4fe8b05e0e0257ea7ee87417a6d898f0eb43ec",
            "transactionIndexRaw": "0x0",
            "blockNumberRaw": "0x1",
            "valueRaw": "0x0",
            "gasPriceRaw": "0x1",
            "gasRaw": "0x5f5e100"
        }
    ],
    "uncles": null,
    "sealFields": null,
    "nonceRaw": null,
    "numberRaw": "0x1",
    "difficultyRaw": null,
    "totalDifficultyRaw": null,
    "sizeRaw": null,
    "gasLimitRaw": "0x0",
    "gasUsedRaw": "0x0",
    "timestampRaw": "0x16a969296df"
}
```



## 3.23. 获取群组内同步状态信息
[top](#目录)

### 接口描述

> 获取群组内同步状态信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/syncStatus**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |


**2）数据格式**

http://localhost:8081/webase-front/1/web3/syncStatus

### 响应参数

**1）数据格式**
```
{
    "blockNumber": 126,
    "genesisHash": "0xed3350d191d23cbc609c98e920baa583403b9a02fa934df868e7f425cd72f5c3",
    "isSyncing": false,
    "latestHash": "0x49ca6eb004f372c71ed900ec6992582cd107e4f3ea36aaa5a0a78829ebef1f14",
    "nodeId": "d822165959a0ed217df6541f1a7dd38b79336ff571dd5f8f85ad76f3e7ec097e1eabd8b03e4a757fd5a9fb0eea905aded56aaf44df83c34b73acb9ab7ac65010",
    "peers": [
        {
            "blockNumber": 126,
            "genesisHash": "0xed3350d191d23cbc609c98e920baa583403b9a02fa934df868e7f425cd72f5c3",
            "latestHash": "0x49ca6eb004f372c71ed900ec6992582cd107e4f3ea36aaa5a0a78829ebef1f14",
            "nodeId": "552398be0eef124c000e632b0b76a48c52b6cfbd547d92c15527c2d1df15fab2bcded48353db22526c3540e4ab2027630722889f20a4a614bb11a7887a85941b"
        },
        {
            "blockNumber": 126,
            "genesisHash": "0xed3350d191d23cbc609c98e920baa583403b9a02fa934df868e7f425cd72f5c3",
            "latestHash": "0x49ca6eb004f372c71ed900ec6992582cd107e4f3ea36aaa5a0a78829ebef1f14",
            "nodeId": "adfa2f9116d7ff68e0deb75307fa1595d636bf097ad1de4fb55cff00e4fef40b453abb30388aa2112bf5cd4c987afe2e047250f7049791aa1ee7091c9e2ab7bb"
        },
        {
            "blockNumber": 126,
            "genesisHash": "0xed3350d191d23cbc609c98e920baa583403b9a02fa934df868e7f425cd72f5c3",
            "latestHash": "0x49ca6eb004f372c71ed900ec6992582cd107e4f3ea36aaa5a0a78829ebef1f14",
            "nodeId": "dde0bbf5eb3a731e6da861586e98e088e16e6fdd9afae2f2c213cead20a4f5eaa3910042b70d62266d2350d98a43c1f235c8e0da384448384893857873abdb75"
        }
    ],
    "protocolId": 265,
    "txPoolSize": "0"
}
```





## 3.24. 获取交易信息接口
[top](#目录)

### 接口描述

> 根据交易hash查询交易信息

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/transaction/{transHash}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |
| 2        | 交易hash | transHash | String      |             | 是        |                      |



**2）数据格式**

http://localhost:8081/webase-front/1/web3/transaction/0x4145b921309fcaa92b05b782e0181d671b8e68fc6d61d939358ed558fa3489c9

### 响应参数

**1）数据格式**
```
{
    "hash": "0x4145b921309fcaa92b05b782e0181d671b8e68fc6d61d939358ed558fa3489c9",
    "nonce": 1.47418536037145E+75,
    "blockHash": "0x3875dbec6e0ad0790dc0a0e8535b7c286ef7cee4149e5b1494f5c65631a9e321",
    "blockNumber": 1,
    "transactionIndex": 0,
    "from": "0x33a41878e78fb26735bf425f9328990e3a1a89df",
    "to": "0x0000000000000000000000000000000000000000",
    "value": 0,
    "gasPrice": 1,
    "gas": 100000000,
    "input": "0x6080604052348015600f57600080fd5b5060868061001e6000396000f300608060405260043610603f576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806335b09a6e146044575b600080fd5b348015604f57600080fd5b5060566058565b005b5600a165627a7a723058204aacdb57d6f2ae0f7f6c89c28236bba0205631183fd99785de220481566e683f0029",
    "creates": null,
    "publicKey": null,
    "raw": null,
    "r": null,
    "s": null,
    "v": 0,
    "transactionIndexRaw": "0x0",
    "valueRaw": "0x0",
    "gasPriceRaw": "0x1",
    "blockNumberRaw": "0x1",
    "gasRaw": "0x5f5e100",
    "nonceRaw": "0x3425bfe0f36e343686ccbe34a4fe8b05e0e0257ea7ee87417a6d898f0eb43ec"
}
```


## 3.25. 获取交易回执接口
[top](#目录)

### 接口描述

> 根据交易hash查询交易回执

### 接口URL

**http://localhost:8081/webase-front/{groupId}/web3/transactionReceipt/{transHash}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 群组编号 | groupId | int      |             | 是        |                      |
| 2        | 交易hash | transHash | String      |             | 是        |                      |



**2）数据格式**

http://localhost:8081/webase-front/1/web3/transactionReceipt/0x4145b921309fcaa92b05b782e0181d671b8e68fc6d61d939358ed558fa3489c9

### 响应参数

**1）数据格式**
```
{
    "transactionHash": "0x4145b921309fcaa92b05b782e0181d671b8e68fc6d61d939358ed558fa3489c9",
    "transactionIndex": 0,
    "blockHash": "0x3875dbec6e0ad0790dc0a0e8535b7c286ef7cee4149e5b1494f5c65631a9e321",
    "blockNumber": 1,
    "cumulativeGasUsed": 0,
    "gasUsed": 88537,
    "contractAddress": "0x30905b39fa9f08822f342377d229d781ae8f9be6",
    "root": null,
    "status": "0x0",
    "from": "0x33a41878e78fb26735bf425f9328990e3a1a89df",
    "to": "0x0000000000000000000000000000000000000000",
    "output": "0x",
    "logs": [],
    "logsBloom": "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
    "transactionIndexRaw": "0x0",
    "blockNumberRaw": "0x1",
    "statusOK": true,
    "cumulativeGasUsedRaw": null,
    "gasUsedRaw": "0x159d9"
}
```




# 4. 性能检测接口

## 4.1. 获取机器配置信息
[top](#目录)

### 接口描述

获取机器配置信息

### 接口URL

**http://localhost:8081/webase-front/performance/config**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

无入参

### 响应参数

## 4.2. 获取机器历史性能信息
[top](#目录)

### 接口描述

获取机器历史性能信息

### 接口URL

**http://localhost:8081/webase-front/performance**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明** |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 开始日期 | beginDate | LocalDateTime |   |   |   |
| 2 | 结束日期 | endDate | LocalDateTime |   |   |   |
| 3 | 对比开始日期 | contrastBeginDate | LocalDateTime |   |   |   |
| 4 | 对比结束日期 | contrastEndDate | LocalDateTime |   |   |   |
| 5 | 间隔 | gap | int |   |   |   |

### 响应参数
**1）参数表**
``` 
{
  [{"metricType":"cpu","data":{"lineDataList":{"timestampList":[],"valueList":[]},"contrastDataList":{"timestampList":[],"valueList":[]}}},{"metricType":"memory","data":{"lineDataList":{"timestampList":null,"valueList":[]},"contrastDataList":{"timestampList":null,"valueList":[]}}},{"metricType":"disk","data":{"lineDataList":{"timestampList":null,"valueList":[]},"contrastDataList":{"timestampList":null,"valueList":[]}}},{"metricType":"txbps","data":{"lineDataList":{"timestampList":null,"valueList":[]},"contrastDataList":{"timestampList":null,"valueList":[]}}},{"metricType":"rxbps","data":{"lineDataList":{"timestampList":null,"valueList":[]},"contrastDataList":{"timestampList":null,"valueList":[]}}}]}
}
```


# 5. 交易接口

## 5.1. 交易处理接口
[top](#目录)

### 接口描述

通过合约信息进行调用，前置根据调用的合约方法是否是“constant”方法区分返回信息，“constant”方法为查询，返回要查询的信息。非“constant”方法为发送数据上链，返回块hash、块高、交易hash等信息。

### 接口URL

**http://localhost:8081/webase-front/trans/handle**

### 调用方法

HTTP POST

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**   | **类型**       | **最大长度** | **必填** | **说明** |
| -------- | -------- | ------------ | -------------- | ------------ | -------- | -------- |
| 1        | 用户编号 | user       | Integer        |            | 是       |   用户编号或者用户名    |
| 2        | 合约名称 | contractName | String         |              | 是       |          |
| 3        | 合约地址 | contractAddress | String         |              | 是       |          |
| 4        | 方法名   | funcName     | String         |              | 是       |          |
| 5        | 方法参数 | funcParam    | List|   |        |JSONArray，对应合约方法参数，多个参数以“,”分隔|
| 6        | 群组ID | groupId    | int |              |          |    |
| 7        | 是否是加密私钥 | useAes | boolean      |             | 否        |             |

**2）数据格式**
```
{
"useAes ":false,
"user ":700001,
"contractName":"HelloWorld",
"contractAddress":"dasdfav23rf213vbcdvadf3bcdf2fc23rqde",
"funcName":"set",
"funcParam":["Hi,Welcome!"],
"groupId" :"1"
}
```
示例：curl -l -H "Content-type: application/json" -X POST -d '{"contractName":
"HelloWorld", "funcName": "set", "funcParam": ["Hi,Welcome!"], "userId": 700001, "useAes": false, "contractAddress":"dasdfav23rf213vbcdvadf3bcdf2fc23rqde","groupId": 1}' http://10.0.0.1:8081/webase-front/trans/handle

### 响应参数


1. **数据格式**

a、正确查询交易返回值信息
```
{"Hi,Welcome!"}
```
b、正确发送数据上链返回值信息(交易收据)
```
{
"code": 0,
"message": "success",
"data": {
"blockHash":
"0x1d8d8275aa116d65893291c140849be272dac1d4ca0a0a722f44404b2f2356c3",
"gasUsed": 32798,
"transactionIndexRaw": "0",
"blockNumberRaw": "33",
"blockNumber": 33,
"contractAddress": "0x0000000000000000000000000000000000000000",
"cumulativeGasUsed": 32798,
"transactionIndex": 0,
"gasUsedRaw": "0x801e",
"logs": [],
"cumulativeGasUsedRaw": "0x801e",
"transactionHash":"0x0653a8e959771955330461456dd094a96d9071bfa31e6f43b68b30f10a85689c"
}
}
```



# 附录

## 1. 返回码信息列表 
 [top](#目录)

------

| code   | message                                      | 描述                 |
| ------ | -------------------------------------------- | -------------------- |
| 0      | success                                      | 成功                 |
| 101001 | system error                                 | 系统异常             |
| 101002 | param valid fail                             | 参数校验异常         |
| 201001 | uuid cannot be empty                         | 业务流水号不能为空   |
| 201002 | userId cannot be empty                       | 用户编号不能为空     |
| 201003 | contractName cannot be empty                 | 合约名不能为空       |
| 201004 | version cannot be empty                      | 合约版本不能为空     |
| 201005 | funcName cannot be empty                     | 方法名不能为空       |
| 201006 | abiInfo cannot be empty                      | abi内容不能为空      |
| 201007 | contractBin cannot be empty                  | 合约bin不能为空      |
| 201008 | contract's current version has been deployed | 该合约版本已部署     |
| 201009 | contract is not deployed                     | 合约未部署           |
| 201010 | save abi error                               | abi保存错误          |
| 201011 | request funcParam is error                   | 请求的方法参数错误   |
| 201012 | requst blockNumber is greater than latest    | 请求块高大于最新块高 |
| 201013 | get abi from chain error                     | 获取合约abi错误      |
| 201014 | contract deploy error                        | 合约部署错误         |
| 201015 | user's privateKey is null                    | 用户私钥为空         |
| 201016 | file is not exist                            | 文件不存在           |
| 201017 | failed to get node config                    | 获取节点配置失败     |
| 201018 | blockNumber and pbftView unchanged           | 块高和view没有变化   |
| 201019 | request function is error                    | 请求的方法错误       |
| 201020 | transaction query from chain failed          | 交易查询失败         |
| 201021 | transaction send to chain failed             | 交易上链失败         |
| 201022 | node request failed                          | 节点请求失败         |