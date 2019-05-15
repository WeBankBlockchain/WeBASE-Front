# 节点前置服务说明

# 目录
> * [功能说明](#chapter-1)
> * [前提条件](#chapter-2)
> * [接口说明](#chapter-3)
> * [部署说明](#chapter-4)
> * [附录](#chapter-5)

# 1. <a id="chapter-1"></a>功能说明
节点前置是和fisco-bcos节点配合使用的一个子系统，可以通过HTTP请求和节点进行通信，集成了web3jsdk，对接口进行了封装和抽象。同时具备了可视化控制台。可以通过HTTP请求的形式发交易，降低了开发者的门槛, 前置需要跟节点同机部署。

# 2. <a id="chapter-2"></a>前提条件

| 环境     | 版本              |
| ------  | --------------- |
| java     | jdk1.8.0_121或以上版本|
| python   | Python2.7|
| fisco-bcos |v1.3.x版本  |

  备注：安装说明请参看附录。

# 3. <a id="chapter-3"></a>接口说明

- [接口说明请点击](interface.md)

# 4. <a id="chapter-4"></a>部署说明

## 4.1 拉取代码

执行命令：
```shell
git clone https://github.com/WeBankFinTech/webase-front.git

```

**注意**：代码拉取后，可以切换到相应分支（如：dev）。

```shell
cd webase-front
git checkout dev
```

## 4.2 编译代码

在代码的根目录webase-front编译，如果出现问题可以查看[常见问题解答](install_FAQ.md)</br>
方式一：如果服务器已安装gradle，且版本为gradle-4.10或以上
```shell
gradle build -x test
```
方式二：如果服务器未安装gradle，或者版本不是gradle-4.10或以上，使用gradlew编译
```shell
./gradlew build -x test
```
构建完成后，会在根目录webase-front下生成已编译的代码目录dist。

## 4.3 修改配置
（1）进入目录：
```shell
cd dist/conf
```
修改application.yml
```
...
server: 
  # 服务端口
  port: 8081
...
constant:
  # 对应节点路径（需指明到节点目录）
  nodeDir: /data/app/build/node0
  # 节点管理服务请求url
  mgrBaseUrl: http://%s/webase-node-mgr
  # 节点管理服务ip端口，可配置多个（如：10.0.0.1:8082,10.0.0.1:8083）
  mgrIpPorts: 10.0.0.1:8082
  # 监控的磁盘目录
  monitorDisk: /home
...
```

（2）进入目录：
```shell
cd dist/report
```
修改config.json
```
...
// 节点管理服务，可以配置多个
"node_manager_servers": [
	{
		"ip": "10.0.0.1", // 节点管理服务ip
		"port": "8082"    // 节点管理服务端口
	}
],
"blockchain_nodes": [
	{
		"chainname": "network1",
		"name": "node0",
		"upload_block": "ON",
		"upload_log": "ON",
		"node_dir":"/data/app/build/node0", // 对应节点路径（需指明到节点目录）
		"front_error_log_dir":"default"
	}
],
// 节点管理服务数据上报接口url
"browser_request_url": "/webase-node-mgr/report/blockChainInfo",
...
```

## 4.4 服务启停

进入到已编译的代码根目录：
```shell
cd dist
```
```shell
启动：sh start.sh
停止：sh stop.sh
检查：sh status.sh
```
**备注**：如果脚本执行错误，尝试以下命令:
```shell
赋权限：chmod + *.sh
转格式：dos2unix *.sh
```

## 4.5 查看日志

进入到已编译的代码根目录：
```shell
cd dist
```
```
前置服务日志：tail -f log/webase-front.log
web3连接日志：tail -f log/web3sdk.log
report服务日志：tail -f report/log/report.log
```

# 5. <a id="chapter-5"></a>附录

## 5.1 Java部署

此处给出简单步骤，供快速查阅。更详细的步骤，请参考[官网](http://www.oracle.com/technetwork/java/javase/downloads/index.html)。

（1）从[官网](http://www.oracle.com/technetwork/java/javase/downloads/index.html)下载对应版本的java安装包，并解压到相应目录

```shell
mkdir /software
tar -zxvf jdkXXX.tar.gz /software/
```

（2）配置环境变量

```shell
export JAVA_HOME=/software/jdk1.8.0_121
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
```

## 5.2 Python部署

```shell
pip install requests或yum install requests
```
