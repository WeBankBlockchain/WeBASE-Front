# 常见问题解答

### 一般问题
* 1：执行shell脚本报下面错误permission denied： 

   答：chmod +x 给文件增加权限
   
* 2: gradle build -x test 失败,不能编译Lombok注解
```
...
/data/temp/webase-front/src/main/java/com/webank/webase/front/performance/PerformanceService.java:167: error: cannot find symbol
        log.info("begin sync performance");
        ^
  symbol:   variable log
  location: class PerformanceService
Note: /data/temp/webase-front/src/main/java/com/webank/webase/front/contract/CommonContract.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
100 errors

> Task :compileJava FAILED

FAILURE: Build failed with an exception.
...
```

  答： 修改 build.gradle文件，将以下代码的注释去掉
```
 //annotationProcessor 'org.projectlombok:lombok:1.18.6'
```