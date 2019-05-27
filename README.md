# 目录
> * [功能说明](#chapter-1)
> * [前提条件](#chapter-2)
> * [接口说明](#chapter-3)
> * [部署说明](#chapter-4)
> * [附录](#chapter-5)

# 1. <a id="chapter-1"></a>功能说明
webase-front是和fisco-bcos节点配合使用的一个子系统，此分支支持fisco-bcos 2.0以上版本，可通过HTTP请求和节点进行通信，集成了web3jsdk，对接口进行了封装和抽象，具备可视化控制台，可以在控制台上查看交易和区块详情，开发智能合约，管理私钥，并对节点健康度进行监控和统计。 
  
   部署方式有两种: (1)可以front组件单独部署作为独立控制台使用,打开http://{nodeIP}:8081/webase-front 即可访问控制台界面；(2)也可以结合[webase-node-mgr](https://github.com/WeBankFinTech/webase-node-mgr) 和 [webase-web](https://github.com/WeBankFinTech/webase-web)服务一起部署。

 注意：webase-front需要跟节点同机部署。一台机器部署多个节点，建议只部署一个front服务即可。

# 2. <a id="chapter-2"></a>前提条件

| 环境     | 版本              |
| ------ | --------------- |
| java   | jdk1.8.0_121或以上版本|
| gradle | gradle-4.10或以上版本 |
| fisco-bcos |v2.0.x版本        | 

 备注：安装说明请参看附录。

# 3. <a id="chapter-3"></a>接口说明

- [接口说明请点击](interface.md)

# 4. <a id="chapter-4"></a>部署说明

## 4.1 拉取代码

执行命令：
```
git clone -b dev-0.7 https://github.com/WeBankFinTech/webase-front.git
```

## 4.2 拷贝证书
 
 拷贝节点sdk目录下的ca.crt、node.crt、node.key证书到项目的src/main/resources目录。
 ```
 cp ~/nodes/127.0.0.1/sdk/*  ~/webase-front/src/main/resources
 ```
 
## 4.3 修改配置文件
 然后修改application.yml配置文件。
```
constant:  
  transMaxWait: 30            //交易等待时间
  monitorDisk: /home          //要监控的硬盘目录 
  keyServer: 10.0.0.1:8080   // 配置密钥服务(可以是node-mgr服务)的IP和端口（front独立使用可不配） 
```
 application.yml配置文件中sdk的配置采用默认配置，无需修改。如果想修改连接的节点和端口，设置如下：
``` 
 sdk: 
   ip: 127.0.0.1   //连接节点的ip，是本机ip，建议写成内网ip
   channelPort: 20200 // 连接节点的端口
```
   
## 4.4 编译
在代码的根目录webase-front执行构建命令：
```
  chmod +x ./gradlew
 ./gradlew build -x test
```
构建完成后，会在根目录webase-front下生成已编译的代码目录dist。 安装碰到问题，请参考 [安装问题帮助](install_FAQ.md)


## 4.5 服务启停

进入到已编译的代码根目录：
```shell
cd dist
```
```shell
启动: sh start.sh
停止: sh stop.sh
检查: sh status.sh
```
<font color="#dd0000">备注：如果脚本执行错误，尝试以下命令: </font>
```
赋权限：chmod + *.sh
转格式：dos2unix *.sh
```

## 4.6 查看日志

进入到已编译的代码根目录：
```shell
cd dist
```
```
前置服务日志：tail -f log/webase-front.log
web3连接日志：tail -f log/web3sdk.log
```

## 4.7 打开控制台

http://{nodeIP}:8081/webase-front

基于可视化控制台，可以查看节点数据概览，查看链上节点的运行情况，开发智能合约，管理私钥等。

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

## 5.2 gradle部署

此处给出简单步骤，供快速查阅。更详细的步骤，请参考[官网](http://www.gradle.org/downloads)。

（1）从[官网](http://www.gradle.org/downloads)下载对应版本的gradle安装包，并解压到相应目录

```shell
mkdir /software/
unzip -d /software/ gradleXXX.zip
```

（2）配置环境变量

```shell
export GRADLE_HOME=/software/gradle-2.1
export PATH=$GRADLE_HOME/bin:$PATH
```

