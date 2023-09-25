# WeBase-Listen.sh 文件详解

------

作者：深职院-麦立健

ps:shell语言的编写，平时不常接触，有不足，请包涵，欢迎各位指出问题。

获取WeBase子系统的同级路径

```shell
wb_path=$(pwd) 	#默认脚本与WeBase子系统处于同级目录,如有不同，自行修改
port=0
web_Port=5000	#默认5000
```

Webase_Front函数的思路是去获取子系统下的status.sh函数，判断其是否有正常启动，获得输出字符，经过字符串的截取获得WeBase-Front启动时的浏览器端口,通过losf -i:端口,进行判断是否有在监听状态。

```shell
function  webase_front(){
	echo "check webase-front..."	#在同级目录下查找webase-front文件夹
	wabse_front_path=$(find $wb_path -name 'webase-front' -type d)
	cd $wabse_front_path			#进入WeBase-Front目录
	status="$(bash status.sh)"		#运行状态脚本
	if [[ $status == *"running"* ]] 
		then 
			msg=`echo ${status#*Port}`	
			port=`echo ${msg%%i*}` 		#进行字符串截取获得端口(默认5002)
	fi

	port_msg=`lsof -i:$port`			#lsof -i:port 查看端口连接
	if [[ $port_msg == *"LISTEN"* ]] 	#判断端口是否被监听，是则正常运行，否则运行有误
		then 							#后续两个子系统方法大致相同
		echo "WeBase-Front is Successful"
	else 
		echo "WeBase-Front is Fail"
		return
	fi
	echo -e "Check webase-front finish\n"
}
```

WeBase-Node-Msg和WeBase-Sign方法同上，不再赘述。

------

WeBase-Web本身主要是存放静态页面等资源，所以不存在start.sh和status.sh等脚本文件，而log日志文件也记录的是浏览器的请求信息，对于判断意义不大，所以用监听nginx服务的方法。通过获取WeBase一键部署时的nginx配置文件路径，再查看nginx服务，通过字符串比较来判断nginx服务有无采用WeBase一键部署的配置文件，有则说明WeBase-Web服务成功跑起来了~

```shell
function  webase_web(){
	echo "check webase_web..."
	nginx_conf=$wb_path/comm/nginx.conf		#获取nginx.conf的工作路径
	nginx_msg="`ps -ef |grep nginx`"		#ps(英文全拼：process status)命令用于显示当前进程的状态 ps -ef -e显示所有进程,-f全格式。

	if [[ $nginx_msg == *$nginx_conf* ]] 	#进行匹配查看，nginx服务有无使用webase-web自带的nginx配置
		then 
			echo "WeBase-Web is Successful"
	else
		echo "WeBase-Web is Fail"
	fi	
	echo -e "Check WeBase-Web finish\n"
}
```

