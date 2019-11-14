# 常见问题解答

### 一般问题
* 1：执行shell脚本报下面错误permission denied： 

   答：chmod +x 给文件增加权限
   
* 2: eclipse环境编译源码失败，错误提示如下；
```
...
/data/temp/WeBASE-Front/src/main/java/com/webank/webase/front/performance/PerformanceService.java:167: error: cannot find symbol
        log.info("begin sync performance");
        ^
  symbol:   variable log
  location: class PerformanceService
Note: /data/temp/WeBASE-Front/src/main/java/com/webank/webase/front/contract/CommonContract.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
100 errors

> Task :compileJava FAILED

FAILURE: Build failed with an exception.
...
```

  答：问题是不能编译Lombok注解 ，修改 build.gradle文件，将以下代码的注释加上
```
 //annotationProcessor 'org.projectlombok:lombok:1.18.6'
```

   
* 3: 节点运行一段时间后新增了一个群组，front查不到新群组信息。 
 
   答： 调用 http://{ip}:{port}/WeBASE-Front/8081/1/web3/refresh 方法，即可手动更新。
   
   
* 4: 觉得url中WeBASE-Front输入比较麻烦,怎么修改？
 
   答：修改application.yml 文件中的context-path即可：
    ```
    server:
        port: 8081
        context-path: /WeBASE-Front   //修改此处即可如 webase-front;
    ```    