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
| ------ | --------------- |
| java   | jdk1.8.0_121或以上版本|
| gradle | gradle-5.0或以上版本 |
| fisco-bcos |v2.0.x版本  |
 
﻿  服务器性能监控功能需要使用sigar，只需将tool目录下libsigar-amd64-Linux.so（64bit）添加到服务器的usr/lib目录即可。 

 备注：安装说明请参看附录。

# 3. <a id="chapter-3"></a>接口说明

- [接口说明请点击](interface.md)

# 4. <a id="chapter-4"></a>部署说明

## 4.1 拉取代码

执行命令：
```
git clone https://github.com/WeBankFinTech/webase-front.git
```

## 4.2 编译代码
 拷贝节点sdk目录下的ca.crt、node.crt、node.key证书到项目的src/main/resources目录。
 然后修改application.yml配置文件。
```
constant:  
  transMaxWait: 30          //交易等待时间
  mgrIpPorts: 127.0.0.1:8080 // 配置node-managerIP和端口
  monitorDisk: /home   //要监控的硬盘目录
```
在代码的根目录webase-front执行构建命令：
```
gradle build -x test 
（没有安装gradle  则使用 ./gradlew build -x test）
```
构建完成后，会在根目录webase-front下生成已编译的代码目录dist。


## 4.3 服务启停

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

## 4.4 查看日志

进入到已编译的代码根目录：
```shell
cd dist
```
```
前置服务日志：tail -f log/webase-front.log
web3连接日志：tail -f log/web3sdk.log
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

