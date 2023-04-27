### v1.5.5(2023-04-17)

**Fix**
- 支持合约函数复杂参数如Struct类型的入参与返回值，部署合约与发交易相关接口中`funcParam`类型从`List<Object>`改为`List<String>`

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Node-Manager v1.5.5+
- WeBASE-Sign v1.5.0+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.5.4(2022-02-22)

**Fix**
- 优化合约仓库加载方式，通过`conf/warehouse`目录中的.json文件加载合约模板
- 优化创建随机私钥所调用的JAVASDK的keyFactory问题

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Node-Manager v1.5.0+
- WeBASE-Sign v1.5.0+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.5.3(2021-09-27)

**Add**
- 新增节点前置Docker镜像，`webasepro/webase-front:v1.5.3`
- 合约仓库新增代理合约模板、溯源合约模板

**Fix**
- 优化组装交易代码，支持struct交易返回值
- 优化节点证书与sdk证书读取逻辑
- 优化合约IDE回退问题，仅回退到后台记录版本
- 修复合约IDE创建文件夹未自动刷新问题、修复IDE菜单栏宽度问题
- 修复交易回执解析失败问题

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Node-Manager v1.5.0+
- WeBASE-Sign v1.5.0+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.5.2(2021-07-16)

**Add**
- 优化合约Java项目导出功能，支持批量编译合约，支持多用户与channel端口检查
- 合约仓库新增资产合约模板
- 增加交易组装接口`/tran/convertRawTxStr`和编码交易函数接口`/trans/encodeFunction`
- 支持合约IDE绑定合约地址

**Fix**
- 优化交易窗口，支持无私钥用户时直接创建私钥

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Node-Manager v1.5.0+
- WeBASE-Sign v1.5.0+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.5.1(2021-05-28)

**Add**
- 合约IDE支持导出合约的Java工程脚手架
- 合约仓库新增SmartDev存证合约

**Fix**
- 优化合约仓库中LibString工具合约，增加bytes32与string互转方法
- 修复无群组1启动报错问题
- 修复导出SDK证书目录问题
- 修复合约IDE中合约调用参数为字符串时不能输入空格
- 优化合约IDE，通过worker加载编译js文件，修复部分chrome浏览器加载js失败问题

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Node-Manager v1.5.0+
- WeBASE-Sign v1.5.0+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.5.0(2021-04-06)

**Add**
- 切换到Java SDK
- 支持导出前置的SDK证书与私钥
- 支持导出Pem/P12/WeID私钥

**Fix**
- 支持导出JavaSDK的合约Java文件

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Node-Manager v1.5.0+
- WeBASE-Sign v1.5.0+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.4.3(2021-01-27)

**Add**
- 合约仓库
- 在线工具

**Fix**
- 合约IDE支持空数组
- 合约编译结果信息显示优化
- 私钥支持明文导入
- 支持CNS

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Node-Manager v1.4.1+
- WeBASE-Sign v1.4.3+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.4.2(2020-12-03)

**Add**
- 支持solidity v0.6.10
- 新增EventLog查询功能
- 新增工具类接口`tool`，如解析output，string转Bytes32等

**Fix**
- 优化合约IDE合约加载，支持按路径导出合约
- 升级web3sdk 2.6.2，修复部分jdk导致SSL失败问题


**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Node-Manager v1.4.1+
- WeBASE-Sign v1.4.0+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.4.1(2020-09-18)

**Add**
- 升级web3sdk为2.6.1版本
- 新增ChainGovernance接口
- 新增getBlockHeader接口

**Fix**
- 优化节点前置异常处理，优化预编译合约错误提示
- 修复合约部署权限检查bug
- 修复新建群组的区块推送与合约event推送bug
- 修复合约中byte32编码补位报错问题

**兼容性**
- 支持FISCO-BCOS v2.4.x 版本及以上版本
- WeBASE-Node-Manager v1.4.0+
- WeBASE-Sign v1.4.0+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.4.0(2020-08-06)

**Add**
- 增加返回 FISCO-BCOS, WeBASE-Front, WeBASE-Sign Version 版本接口；


**兼容性**
- 支持FISCO-BCOS v2.4.x 版本（推荐）
- 支持FISCO-BCOS v2.5.x 版本
- WeBASE-Node-Manager v1.4.0+
- WeBASE-Sign v1.4.0+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。



### v1.3.2(2020-06-17)

**Fix**
- 移除Fastjson，替换为Jackson 2.11.0; web3sdk升级为2.4.1
- 升级依赖包：spring: 4.3.27; log4j: 2.13.3; slf4j: 1.7.30; netty-all: 4.1.44+; guava: 29.0;
- 修改`/contract/contractList`接口分页页码`pageNumber`从0开始改为1开始

**兼容性**
- 支持FISCO-BCOS v2.4.x 版本
- WeBASE-Node-Manager v1.3.1+
- WeBASE-Sign v1.3.1+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.3.1(2020-06-01)

**Add**
- 新增动态管理群组接口
- 新增导入已部署合约的ABI接口，支持导入abi进行合约调用
- 支持导入.p12格式私钥
- 新增导入私钥到webase-sign接口
- 新增BSN分支中合约状态管理接口
- 引入fisco-solcJ jar包，支持自动切换国密后台编译

**Fix**
- 修复链上事件通知-订阅合约event中同时订阅多个event和单个event有多个参数的bug
- 修复web页面中节点监控的入参bug

**兼容性**
- 支持FISCO-BCOS v2.4.x 版本
- WeBASE-Node-Manager v1.3.1+
- WeBASE-Sign v1.3.1+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.3.0(2020-04-29)

**Add**
- 新增链上事件通知的GET接口
- WeBASE-Node-Manager的私钥管理与交易由节点前置通过**WeBASE-Sign**进行私钥创建与交易签名
- 节点前置的web页面中，私钥管理移入合约管理中，改为“测试用户管理”；
- 私钥接口中，除节点前置的本地私钥用户外，其他类型的私钥用户不再保存与返回私钥
- 预编译合约接口由WeBASE-Sign签名后调用
- 签名服务的`userId`改为`signUserId`, 接口中的`useAes`默认为true，不再需要传入值

**Fix**
- 修复链上事件通知的数据一致性bug
- 升级依赖包log4j, fastjson, jackson
- 优化web3ApiService的异常捕获
- 修复可为空的合约`funcParam`不可为空的bug

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 - v2.4.x 版本
- WeBASE-Web v1.2.2+
- WeBASE-Node-Manager v1.2.2+
- WeBASE-Sign v1.2.2+
- WeBASE-Transaction v1.2.2+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.2.4 (2020-04-08)

**Fix**
- bugifx: 升级fastjson v1.2.67
- bugifx: Precompiled预编译相关接口中的useAes默认为false，改为默认true
- bugifx: 链上事件通知中修复内存与数据库的数据一致性问题，增加try-catch的异常处理、出块事件HashMap改为ConcurrentHashMap
- 优化: 修复Web3jMap的空指针问题，修复获取groupList失败的数组越界问题

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 及以上版本
- WeBASE-Web v1.2.2+
- WeBASE-Node-Manager v1.2.2+
- WeBASE-Sign v1.2.2+
- WeBASE-Transaction v1.2.2+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.2.3 (2020-03-11)

**Add**
- 可搭建RabbitMQ连接WeBASE-Front，接收链上事件通知
- 支持应用层订阅出块事件、合约Event事件
- WeBASE-Front新增事件通知订阅接口`/event/newBlockEvent`, `/event/contractEvent`
- 新增[WeBASE-Event-Client](https://github.com/WeBankFinTech/WeBASE-Event-Client)工程，可测试链上事件通知。

**Fix**
- 优化：优化了WeBASE-Sign的国密签名性能，子系统间通过长连接，提升了交易链路的整体性能
- bugifx: 升级jackson与log4j包
- bugifx: 修复接口`deployWithSign`的`contractBin`字段，改为`bytecodeBin`

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 及以上版本
- WeBASE-Web v1.2.2+
- WeBASE-Node-Manager v1.2.2+
- WeBASE-Sign v1.2.2+
- WeBASE-Transaction v1.2.2+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.2.2 (2020-01-02)

**Add**
- 支持国密（sm2, sm3, 前端/后端合约编译）
- 私钥管理支持导入pem私钥
- 合约接口支持传入`abi`进行合约调用
- 新增`/encrypt`接口判断是否国密

**Fix**
- bugfix：precompiled api加入`useAes`字段，默认为false
- 优化：KeystoreService分离getCredentials方法
- 优化：web3sdk升级至v2.2.0
- bugifx: 修复start.sh启动时间过长的问题
- bugfix：CommonUtils的`SignatureData`序列化支持国密

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 及以上版本
- WeBASE-Web v1.2.2
- WeBASE-Node-Manager v1.2.2
- WeBASE-Sign v1.2.2
- WeBASE-Transaction v1.2.2

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.2.1 (2019-11-22)

**Add**
- 证书管理增加SDK证书

**Fix**
- 优化：precompiled节点列表接口增加离线的共识/观察节点
- 优化：调整代码结构，增加代码注释
- 优化：sh脚本支持secp256k1
- 优化：升级fastjson为1.2.60, gradle-wrapper为v6.0.1

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 版本
- WeBASE-Web v1.2.1
- WeBASE-Node-Manager v1.2.1

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.2.0 (2019-10-29)

**Add**
- 采集监控数据开关
- 证书管理接口

**Fix**
- 不依赖特定版本的JDK
- 解决ubuntu下jps识别进程启动有点问题
- 脚本问题，启动成功，但是报启动失败
- 权限管理功能接口

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 版本
- WeBASE-Web v1.2.0
- WeBASE-Node-Manager v1.2.0

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.1.0 (2019-09-09)

**Fix**
- 植入默认的Asset合约和存证合约
- 通过端口判断进程，可以支持多个进程
- 支持批量upload合约
- 前置支持合约解析和交易解析功能


- 兼容性
支持FISCO-BCOS v2.0.0-rc1 版本
支持FISCO-BCOS v2.0.0-rc2 版本
支持FISCO-BCOS v2.0.0-rc3 版本
支持FISCO-BCOS v2.0.0 版本
WeBASE-Web v1.1.0
WeBASE-Node-Manager v1.1.0


详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。



### v1.0.0 (2019-06-27)

WeBASE-Front（微众区块链中间件平台-前置服务）。
