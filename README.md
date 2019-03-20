# 目录
> * [功能说明](#chapter-1)
> * [前提条件](#chapter-2)
> * [接口说明](#chapter-3)
> * [部署说明](#chapter-4)
> * [问题排查](#chapter-5)
> * [附录](#chapter-6)

# 1. <a id="chapter-1"></a>功能说明
节点前置是和fisco-bcos节点配合使用的一个子系统，可以通过HTTP请求和节点进行通信，集成了web3jsdk，对接口进行了封装和抽象。同时具备了可视化控制台。可以通过HTTP请求的形式发交易，降低了开发者的门槛。

# 2. <a id="chapter-2"></a>前提条件

| 环境     | 版本              |
| ------ | --------------- |
| java   | jdk1.8.0_121或以上版本|
| gradle | gradle-5.0或以上版本 |
| python | Python2.7 |
| fisco-bcos |v1.3.x版本  |
备注：安装说明请参看附录。

# 3. <a id="chapter-3"></a>接口说明

- [接口说明请点击](interface.md)

# 4. <a id="chapter-4"></a>部署说明

## 4.1 拉取代码

执行命令：
```
git clone https://github.com/WeBankFinTech/webase-front.git

```

**注意**：代码拉取后，切换到相应分支。

```shell
cd webase-front
git checkout XXXXX
```

## 4.2 编译代码

在代码的根目录webase-front执行构建命令：
```
gradle build
```
构建完成后，会在根目录webase-front下生成已编译的代码目录dist。

## 4.3 修改配置
（1）进入目录：
```
cd dist/conf
```
```
修改当前服务端口：sed -i "s/8081/${your_server_port}/g" application.yml
修改机构名称：sed -i "s/orgTest/${your_org_name}/g" application.yml
修改节点目录：sed -i "s/\/data\/app\/build\/node0/${your_node_dir}/g" application.yml
修改节点管理服务ip端口：sed -i "s/10.0.0.1:8082/${your_ip_port}/g" application.yml
例子（将端口由8081改为8090）：sed -i "s/8081/8090/g" application.yml
```
**备注：如果字符串中存在斜杠'/'，在前面加单个反斜杠'\\'转义**

（2）进入目录：
```
cd dist/report
```
```
修改节点管理服务ip：sed -i "s/10.0.0.1/${your_ip }/g" config.json
修改节点管理服务端口：sed -i "s/8082/${your_ port}/g" config.json
修改节点目录：sed -i "s/\/data\/app\/build\/node0/${your_node_dir}/g" config.json
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
```
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

# 5. <a id="chapter-5"></a>问题排查

## 5.1 gradle build失败

如果出现以下错误，**请配置一下lombok**，lombok的配置和使用请在网上查询。
```
> Task :compileJava
E:\webase-transcation\src\main\java\com\webank\webase\transaction\Application.java:21: 错误: 找不到符号
        log.info("start success...");
        ^
  符号:   变量 log
  位置: 类 Application
```

如果出现以下错误，**请检查gradle版本，需要使用5.0或以上版本。**
```
> Could not find method annotationProcessor() for arguments [org.projectlombok:lombok:1.18.2] on object of type org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler.
```

# 6. <a id="chapter-6"></a>附录

## 6.1 Java部署

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

## 6.2 gradle部署

此处给出简单步骤，供快速查阅。更详细的步骤，请参考[官网](http://www.gradle.org/downloads)。

（1）从[官网](http://www.gradle.org/downloads)下载对应版本的gradle安装包，并解压到相应目录

```shell
mkdir /software/
unzip -d /software/ gradleXXX.zip
```

（2）配置环境变量

```shell
export GRADLE_HOME=/software/gradle-XXX
export PATH=$GRADLE_HOME/bin:$PATH
```

## 6.3 Python部署

```shell
pip install requests或yum install requests
```
