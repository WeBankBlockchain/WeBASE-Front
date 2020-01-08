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
