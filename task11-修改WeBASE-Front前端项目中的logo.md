# Second quarter 第二季

## task11：修改WeBASE-Front前端项目中的logo

### 第一步：克隆WeBASE-Front项目

``git clone https://gitee.com/WeBank/WeBASE-Front.git``

### 第二步：修改WeBASE-Front前端项目logo

> 注意：修改的logo图片类型以及名称需要跟原来的一致

在 /WeBASE-Front/src/main/resources/static/static/image 目录下找到 logo.jpg 将其替换调即可

如果是浏览器显示的logo则修改 /WeBASE-Front/src/main/resources/static/static/image 目录下的 webase_35x35.png

### 第三步：编译代码

在 WeBASE-Front 目录下运行命令：

```bash
chmod +x ./gradlew && ./gradlew build -x test
```

### 第四步：修改配置

按照[官方的配置说明](https://webasedoc.readthedocs.io/zh_CN/latest/docs/WeBASE-Front/install.html#id6)即可

#### 第五步：启动

返回到dist目录执行：

```bash
启动: bash start.sh
停止: bash stop.sh
检查: bash status.sh
```

