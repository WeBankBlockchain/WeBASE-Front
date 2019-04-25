#常见问题解答

### 一般问题
* 1：执行shell脚本报下面错误permission denied： 

   答：chmod +x 给文件增加权限
*  2: gradle build -x test 失败   不能编译Lombok注解
  答： 打开 build.gradle 文件中  // annotationProcessor 'org.projectlombok:lombok:1.18.6'
