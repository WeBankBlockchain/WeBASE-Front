# 目录
<!-- TOC -->
- [1.合约接口]( )    
   - [1.1. 发送abi接口（未使用）](#11-发送abi接口)
   - [1.2. 合约部署接口](#12-合约部署接口)       
   - [1.3. 获取公私钥接口](#13-获取公私钥接口)        
   - [1.4. 删除abi接口（未使用）](#14-删除abi接口未使用)     
- [2. 交易接口](#2-交易接口)  
   - [2.1. 交易处理接口](#21-交易处理接口) 
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
   - [3.10. 获取指定账户交易数接口](#310-获取指定账户交易数接口)     
   - [3.11. 根据块hash和交易index获取交易接口](#311-根据块hash和交易index获取交易接口)      
   - [3.12. 根据块高和交易index获取交易接口](#312-根据块高和交易index获取交易接口)      
   - [3.13. 获取节点信息接口](#313-获取节点信息接口)  
   - [3.14. 节点心跳接口](#314-节点心跳接口)     
- [4. 性能检测接口](#4-性能检测接口)   
   - [4.1. 获取机器配置信息](#41-获取机器配置信息)      
   - [4.2. 获取机器历史性能信息](#42-获取机器历史性能信息)      
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
| -------- | -------- | ------------ | -------------- | ------------ | -------- | ---------------------------------- |
| 1        | 合约名称 | contractName | String         |              | 是       |                                    |
| 2        | 合约地址 | address      | String         |              | 是       |                                    |
| 3        | 合约abi  | abiInfo      | List\<Object\> |              | 是       | abi文件里面的内容，是一个JSONArray |

**2）数据格式**
```
{

"contractName": "HelloWorld",

"address": "0x31b26e43651e9371c88af3d36c14cfd938baf4fd",

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

**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **必填** | **说明**              |
| -------- | -------- | ---------- | -------- | -------- | --------------------- |
| 1        | 返回码   | code       | String   | 是       | 返回码信息请参看附录1 |
| 2        | 提示信息 | message    | String   | 是       |                       |
| 3        | 返回数据 | data       | Object   |          |                       |

**2）数据格式**
```
{

"code": 0,

"message": "success",

"data": null

}
```
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
| 1        | 用户编号     | userId       | int            | 16           | 是       |          |
| 2        | 合约名称     | contractName | String         |              | 是       |          |
| 3        | 合约版本     | version      | String         |              | 是       |          |
| 4        | 合约abi      | abiInfo      | List\<Object\> |              | 是       |          |
| 5        | 合约bin      | bytecodeBin  | String         |              | 是       |          |
| 6        | 构造函数参数 | funcParam    | List\<Object\> |              | 否       |          |
| 7        | 群组ID       | groupId      | int |              | 否       |          |
**2）数据格式**
```
{

"userId":700001,

"contractName":"HelloWorld",

"version":"1.0",

"abiInfo":[],

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
## 1.3. 获取公私钥接口 
[top](#目录)

### 接口描述

通过调用此接口获取公私钥对和对应账户信息

### 接口URL

**http://localhost:8081/webase-front/privateKey**

### 调用方法

HTTP GET

### 请求参数

1. **参数表**

无入参

**2）数据格式**

### 响应参数

**1）数据格式**
```
{

"publicKey":"1c7073dc185af0644464b178da932846666a858bc492450e9e94c77203428ed54e2ce45679dbb36bfed714dbe055a215dc1aaf4a75faeddce6a62b39c0158e1e",

"privateKey":"008cf98bd0f37fb0984ab43ed6fc2dcdf58811522af7e4a3bedbe84636a79a501c",

"address":"0x2e8ff65fb1b2ce5b0c9476b8f8beb221445f42ee"
}
```
## 1.4. 删除abi接口（未使用）
[top](#目录)

### 接口描述

删除合约时，删除前置对应的abi文件

### 接口URL

**http://localhost:8081/webase-front/1/contract/deleteAbi
/{contractName}/{version}**

### 调用方法

HTTP DELETE

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**   | **类型** | **最大长度** | **必填** | **说明** |
| -------- | -------- | ------------ | -------- | ------------ | -------- | -------- |
| 1        | 合约名称 | contractName | String   |              | 是       |          |
| 2        | 合约版本 | version      | String   |              | 是       |          |

1. **数据格式**

http://localhost:8081/webase-front/1/contract/deleteAbi/HelloWorld/2.0

### 响应参数

**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **必填** | **说明**              |
| -------- | -------- | ---------- | -------- | -------- | --------------------- |
| 1        | 返回码   | code       | String   | 是       | 返回码信息请参看附录1 |
| 2        | 提示信息 | message    | String   | 是       |                       |
| 3        | 返回数据 | data       | Object   |          |                       |

**2）数据格式**
```
{

"code": 0,

"message": "success",

"data": null

}
```
# 2. 交易接口

## 2.1. 交易处理接口 
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
| 1        | 用户编号 | userId       | Integer        | 16           | 是       |          |
| 2        | 合约名称 | contractName | String         |              | 是       |          |
| 3        | 合约版本 | version      | String         |              | 是       |          |
| 4        | 方法名   | funcName     | String         |              | 是       |          |
| 5        | 方法参数 | funcParam    | List\<Object\> |              |          |JSONArray，对应合约方法参数，多个参数以“,”分隔|
| 6        | 群组ID | groupId    | int |              |          |    |

**2）数据格式**
```
{

"userId ":700001,

"contractName":"HelloWorld",

"version":"1.0",

"funcName":"set",

"funcParam":["Hi,Welcome!"],
"groupId" :"1"
}
```
示例：curl -l -H "Content-type: application/json" -X POST -d '{"contractName":
"HelloWorld", "funcName": "set", "funcParam": ["Hi,Welcome!"], "userId": 700001, "version":
"1.0","groupId": 1}' http://10.0.0.1:8081/webase-front/1/trans/handle

### 响应参数


1. **数据格式**

a、正确查询交易返回值信息
```
{
"Hi,Welcome!"
}
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

"transactionHash":
"0x0653a8e959771955330461456dd094a96d9071bfa31e6f43b68b30f10a85689c"

}

}
```

# 3. web3接口

## 3.1. 获取块高接口 
[top](#目录)

### 接口描述

获取节点最新块高

### 接口URL

**http://localhost:8081/webase-front/1/web3/blockNumber**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

无入参

**2）数据格式**

### 响应参数
**1）数据格式**
```
{
8346
}
```


## 3.2. 根据块高获取块信息接口 
[top](#目录)

### 接口描述

通过块高获取块信息

### 接口URL

**http://localhost:8081/webase-front/1/web3/blockByNumber/{blockNumber}**

### 调用方法

HTTP GET

### 请求参数

1. **参数表**

| **序号** | **中文** | **参数名**  | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ----------- | ---------- | ------------ | -------- | -------- |
| 1        | 块高     | blockNumber | BigInteger |              | 是       |          |

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

"parentHash":
"0xc784a2af86e6726fcdc63b57ed1b91a40efc7d8b1b7285154d399ea78bd18754",

"nonce": 0,

"sha3Uncles":
"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",

"logsBloom":
"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000040000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000800000000000000000000000000",

"transactionsRoot":
"0x27055bac669a67e8aaa1455ad9fd70b75dd86dc905c6bd0d111ed67ab182d1dc",

"stateRoot":
"0xa05ad5db11b8be6aed3b591f2f64fdbb241506cbe9ef591f3a4b946ca777f838",

"receiptsRoot":
"0xc1d4b43ed68d7263ddf50861feec26440e933a0b152917e938194f5079b48ce4",

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

"nonce":
9.16230554435730198871878912757792916872575970694115116791456805402893497978e+74,

"blockHash":
"0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",

"blockNumber": 100,

"transactionIndex": 0,

"from": "0x59bd3815f73b197d6ef327f2a45089f50aba942a",

"to": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",

"value": 0,

"gasPrice": 30000000,

"gas": 30000000,

"input":
"0x48f85bce000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a4",

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

**http://localhost:8081/webase-front/1/web3/blockByHash/{blockHash}**

### 调用方法

HTTP GET

### 请求参数

**1）数据格式**

http://localhost:8081/webase-front/1/web3/blockByHash/0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade

### 响应参数

**1）数据格式**
```
{

"number": 100,

"hash": "0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",

"parentHash":
"0xc784a2af86e6726fcdc63b57ed1b91a40efc7d8b1b7285154d399ea78bd18754",

"nonce": 0,

"sha3Uncles":
"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",

"logsBloom":
"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000040000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000800000000000000000000000000",

"transactionsRoot":
"0x27055bac669a67e8aaa1455ad9fd70b75dd86dc905c6bd0d111ed67ab182d1dc",

"stateRoot":
"0xa05ad5db11b8be6aed3b591f2f64fdbb241506cbe9ef591f3a4b946ca777f838",

"receiptsRoot":
"0xc1d4b43ed68d7263ddf50861feec26440e933a0b152917e938194f5079b48ce4",

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

"nonce":
9.16230554435730198871878912757792916872575970694115116791456805402893497978e+74,

"blockHash":
"0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",

"blockNumber": 100,

"transactionIndex": 0,

"from": "0x59bd3815f73b197d6ef327f2a45089f50aba942a",

"to": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",

"value": 0,

"gasPrice": 30000000,

"gas": 30000000,

"input":
"0x48f85bce000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a4",

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

**http://localhost:8081/webase-front/1/web3/blockTransCnt/{blockNumber}**

### 调用方法

HTTP GET

### 请求参数

1. **参数表**

| **序号** | **中文** | **参数名**  | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ----------- | ---------- | ------------ | -------- | -------- |
| 1        | 块高     | blockNumber | BigInteger |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/blockTransCnt/100

### 响应参数

**1）数据格式**
```
{
 1
}
```
}

## 3.5. 获取PbftView接口  
[top](#目录)

### 接口描述

通过调用此接口获取PbftView

### 接口URL

**http://localhost:8081/webase-front/1/web3/pbftView**

### 调用方法

HTTP GET

### 请求参数

1. **参数表**

无入参

**2）数据格式**

### 响应参数

**1）数据格式**
```
{
 160565
}
```

## 3.6. 获取交易回执接口 
[top](#目录)

### 接口描述

> 根据交易hash获取交易回执

### 接口URL

**http://localhost:8081/webase-front/1/web3/transactionReceipt/{transHash}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------- | -------- | ------------ | -------- | -------- |
| 1        | 交易hash | transHash  | String   |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/transactionReceipt/0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b

### 响应参数

**2）数据格式**
```
{

"transactionHash":
"0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",

"transactionIndex": 0,

"blockHash":
"0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",

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

"transactionHash":
"0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",

"blockHash":
"0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",

"blockNumber": 100,

"address": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",

"data":
"0x00000000000000000000000000000000000000000000000000000000000000c000000000000000000000000000000000000000000000000000000000000001200000000000000000000000000000000000000000000000000000000000000160000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a40000000000000000000000000000000000000000000000000000000000000040666164363863656230616530316530643731616635356331316561643031613532656638363435343866306134643133633836363164393664326461366239380000000000000000000000000000000000000000000000000000000000000002363000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000023630000000000000000000000000000000000000000000000000000000000000",

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

**http://localhost:8081/webase-front/1/web3/transaction/{transHash}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------- | -------- | ------------ | -------- | -------- |
| 1        | 交易hash | transHash  | String   |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/transaction/0xa6750b812b1a7e36313879b09f0c41fc583b463c15e57608416f3a32688b432b

### 响应参数


**1）数据格式**
```
{

"hash": "0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",

"nonce":
9.16230554435730198871878912757792916872575970694115116791456805402893497978e+74,

"blockHash":
"0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",

"blockNumber": 100,

"transactionIndex": 0,

"from": "0x59bd3815f73b197d6ef327f2a45089f50aba942a",

"to": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",

"value": 0,

"gasPrice": 30000000,

"gas": 30000000,

"input":
"0x48f85bce000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a4",

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

**http://localhost:8081/webase-front/1/web3/clientVersion**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

无入参

1. **数据格式**

### 响应参数

**1）参数表**

| **序号** | **中文** | **参数名** | **类型** | **必填** | **说明**              |
| -------- | -------- | ---------- | -------- | -------- | --------------------- |
| 1        | 返回码   | code       | String   | 是       | 返回码信息请参看附录1 |
| 2        | 提示信息 | message    | String   | 是       |                       |
| 3        | 返回数据 | data       | Object   |          |                       |

**2）数据格式**

{

"code": 0,

"message": "success",

"data": {

"version": "eth/v1.3.0/Linux/g++/Interpreter/RelWithDebInfo/0/"

}

}

## 3.9. 获取合约二进制代码接口  
[top](#目录)

### 接口描述

> 获取指定块高区块指定合约地址的二进制代码

### 接口URL

**http://localhost:8081/webase-front/1/web3/code/{address}/{blockNumber}**

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
{
"code": "0x"
}
```
## 3.10. 获取指定账户交易数接口  
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

http://localhost:8081/webase-front/1/web3/transCnt/0x0000000000000000000000000000000000000000/1

### 响应参数

**2）数据格式**
```
{
 0
}
```

## 3.11. 根据块hash和交易index获取交易接口  
[top](#目录)

### 接口描述

> 获取指定区块指定位置的交易信息

### 接口URL

**http://localhost:8081/webase-front/1/web3/transByBlockHashAndIndex/{blockHash}/{transactionIndex}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 块hash   | blockHash        | String     |              | 是       |          |
| 2        | 交易位置 | transactionIndex | BigInteger |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/transByBlockHashAndIndex/0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade/0

### 响应参数


**2）数据格式**
```
{

"hash": "0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",

"nonce":
9.16230554435730198871878912757792916872575970694115116791456805402893497978e+74,

"blockHash":
"0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",

"blockNumber": 100,

"transactionIndex": 0,

"from": "0x59bd3815f73b197d6ef327f2a45089f50aba942a",

"to": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",

"value": 0,

"gasPrice": 30000000,

"gas": 30000000,

"input":
"0x48f85bce000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a4",

"creates": null,

"publicKey": null,

"raw": null,

"r": null,

"s": null,

"v": 0,

"blockNumberRaw": "0x64",

"transactionIndexRaw": "0x0",

"nonceRaw": "0x2069170146129593df177e2c37f1b7fe74e2d0fda53dcbbca34a243d46e367a",

"valueRaw": "0x0",

"gasPriceRaw": "0x1c9c380",

"gasRaw": "0x1c9c380"

}
```

## 3.12. 根据块高和交易index获取交易接口  
[top](#目录)

### 接口描述

> 获取指定区块指定位置的交易信息

### 接口URL

**http://localhost:8081/webase-front/1/web3/transByBlockNumberAndIndex/{blockNumber}/{transactionIndex}**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

| **序号** | **中文** | **参数名**       | **类型**   | **最大长度** | **必填** | **说明** |
| -------- | -------- | ---------------- | ---------- | ------------ | -------- | -------- |
| 1        | 块高     | blockNumber      | BigInteger |              | 是       |          |
| 2        | 交易位置 | transactionIndex | BigInteger |              | 是       |          |

**2）数据格式**

http://localhost:8081/webase-front/1/web3/transByBlockNumberAndIndex/100/0

### 响应参数

**1）数据格式**
```
{

"hash": "0xb2c733b742045e61c0fd6e7e2bafece04d56262a4887de9f78dad2c5dd2f944b",

"nonce":
9.16230554435730198871878912757792916872575970694115116791456805402893497978e+74,

"blockHash":
"0xf27ff42d4be65329a1e7b11365e190086d92f9836168d0379e92642786db7ade",

"blockNumber": 100,

"transactionIndex": 0,

"from": "0x59bd3815f73b197d6ef327f2a45089f50aba942a",

"to": "0x986278eb8e8b4ef98bdfc055c02d65865fc87ad2",

"value": 0,

"gasPrice": 30000000,

"gas": 30000000,

"input":
"0x48f85bce000000000000000000000000000000000000000000000000000000000000001caf3fbec3675eabb85c0b25e2992d6f0a5e1546dad85c20733fdb27cfa4ca782a5fdfb621b416f3494c7d8ca436c12309884550d402ea79f03ef8ddfdd494f7a4",

"creates": null,

"publicKey": null,

"raw": null,

"r": null,

"s": null,

"v": 0,

"blockNumberRaw": "0x64",

"transactionIndexRaw": "0x0",

"nonceRaw": "0x2069170146129593df177e2c37f1b7fe74e2d0fda53dcbbca34a243d46e367a",

"valueRaw": "0x0",

"gasPriceRaw": "0x1c9c380",

"gasRaw": "0x1c9c380"

}
```

## 3.13. 获取节点信息接口  
[top](#目录)

### 接口描述

> 获取节点相关配置信息

### 接口URL

**http://localhost:8081/webase-front/1/web3/nodeInfo**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

无入参

**2）数据格式**

### 响应参数

**1）参数表**

| **序号** | **中文**     | **参数名**         | **类型** | **必填** | **说明**              |
| -------- | ------------ | ------------------ | -------- | -------- | --------------------- |
| 1        | 返回码       | code               | String   | 是       | 返回码信息请参看附录1 |
| 2        | 提示信息     | message            | String   | 是       |                       |
| 3        | 返回数据     | data               | Object   |          |                       |
| 3.1      | 系统合约地址 | systemproxyaddress | String   | 是       |                       |
| 3.2      | 机构名       | orgName            | String   | 是       |                       |
| 3.3      | p2pip        | p2pip              | String   | 是       |                       |
| 3.4      | 监听ip       | listenip           | String   | 是       |                       |
| 3.5      | rpc端口      | rpcport            | String   | 是       |                       |
| 3.6      | p2p端口      | p2pport            | String   | 是       |                       |
| 3.7      | 链上链下端口 | channelPort        | String   | 是       |                       |
| 3.8      | data路径     | datadir            | String   | 是       |                       |

**2）数据格式**
```
{

"channelPort": "8545",

"listenip": "10.0.0.1",

"orgName": "WB",

"systemproxyaddress": "0x2ccc8d30d414c1be62b39e4e97931b18d76035f3",

"p2pport": "3030",

"datadir": "/data/app/build/node0/data/",

"p2pip": "10.0.0.1",

"rpcport": "8861"

}
```

## 3.14. 节点心跳接口  
[top](#目录)

### 接口描述

> 验证节点是否存活

### 接口URL

**http://localhost:8081/webase-front/1/web3/nodeHeartBeat**

### 调用方法

HTTP GET

### 请求参数

**1）参数表**

无入参

**2）数据格式**

### 响应参数

**1）数据格式**
```
{

"blockNumber": 1,

"pbftView": 1232

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