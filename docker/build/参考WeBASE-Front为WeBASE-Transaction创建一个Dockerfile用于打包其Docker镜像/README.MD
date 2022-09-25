### 1、编译代码修改配置
  按照 [交易部署说明](https://webasedoc.readthedocs.io/zh_CN/latest/docs/WeBASE-Transaction/install.html)（链接[https://webasedoc.readthedocs.io/zh_CN/latest/docs/WeBASE-Transaction/install.html](https://webasedoc.readthedocs.io/zh_CN/latest/docs/WeBASE-Transaction/install.html)）完成到第4步，主要需要修改的是“#连接节点的ip和channelPort” 和 “配置数据库连接信息” 这两个配置。
### 2、创建镜像
复制Dockerfile文件到编译后生成的dist目录中，执行：（注意最后有个点）
```
docker build -f Dockerfile -t webase-trans:v1.4.1 .
```


执行结果：

```
Sending build context to Docker daemon  125.9MB
Step 1/10 : FROM java:8
 ---> d23bdf5b1b1b
Step 2/10 : COPY lib                  /dist/lib
 ---> df14d6fed1fb
Step 3/10 : COPY conf                 /dist/conf
 ---> 830d6a2cb593
Step 4/10 : COPY apps                 /dist/apps
 ---> 6b8c21e2afbd
Step 5/10 : WORKDIR /dist
 ---> Running in 84c37f169d3e
Removing intermediate container 84c37f169d3e
 ---> e506fcc9581c
Step 6/10 : EXPOSE 5003
 ---> Running in 5b6e472675a4
Removing intermediate container 5b6e472675a4
 ---> 353bfebd9d5c
Step 7/10 : ENV CLASSPATH "/dist/conf/:/dist/apps/*:/dist/lib/*"
 ---> Running in f7f466cb58f1
Removing intermediate container f7f466cb58f1
 ---> 8ce9d49e5bfe
Step 8/10 : ENV JAVA_OPTS " -server -Dfile.encoding=UTF-8 -Xmx512m -Xms512m -Xmn256m -Xss512k -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/log/heap_error.log  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 "
 ---> Running in 977234d6ad94
Removing intermediate container 977234d6ad94
 ---> 2c95f0fa82c4
Step 9/10 : ENV APP_MAIN "com.webank.webase.transaction.Application"
 ---> Running in 183817f288f2
Removing intermediate container 183817f288f2
 ---> 252310f20fdd
Step 10/10 : ENTRYPOINT   java ${JAVA_OPTS} -Djdk.tls.namedGroups="secp256k1", -Duser.timezone="Asia/Shanghai" -Djava.security.egd=file:/dev/./urandom, -Djava.library.path=/dist/conf -cp ${CLASSPATH}  ${APP_MAIN}
 ---> Running in fc12f86cdbac
Removing intermediate container fc12f86cdbac
 ---> f7224f77380c
Successfully built f7224f77380c
Successfully tagged webase-trans:v1.4.1
```
查看生成的镜像：

```
docker images
```

```
REPOSITORY                                                  TAG       IMAGE ID       CREATED              SIZE
webase-trans                                                v1.4.1    f7224f77380c   About a minute ago   760MB
```

### 3、运行镜像到容器

```
docker run -d --name WeBASE-Trans -v /docker_file/trans/WeBASE-Transaction/dist/log:/dist/log -p 5003:5003 webase-trans:v1.4.1

```
其中参数“-v /docker_file/trans/WeBASE-Transaction/dist/log:/dist/log”把日志挂载出来，（冒号前面的参数是宿主机的绝对路径），方便查看输出日志：

```
tail -f log/transaction.log
```

```
[main] INFO  Application() - main run success...
```






