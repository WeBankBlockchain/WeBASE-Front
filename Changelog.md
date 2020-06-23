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
