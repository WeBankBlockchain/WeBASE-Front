#!/bin/bash
#获取webase系统路径
wb_path=$(pwd) 	#默认脚本与WeBase子系统处于同级目录,如有不同，自行修改
port=0
web_Port=5000	#默认5000

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

function  webase_node_mgr(){
	#查找webase-node-mgr文件夹
	echo "check webase-node-mgr..."
	webase_node_mgr_path=$(find $wb_path -name 'webase-node-mgr' -type d)
	cd $webase_node_mgr_path
	status=$(bash status.sh)
	if [[ $status == *"running"* ]] 
		then 
			msg=`echo ${status#*Port}`
			port=`echo ${msg%%i*}` #获得端口
	fi
	port_msg=`lsof -i:$port`
	if [[ $port_msg == *"LISTEN"* ]] 
		then 
		echo "WeBase-Node-Mgr is Successful"
	else 
		echo "WeBase-Node-Mgr is Fail"
		return
	fi
	echo -e "Check WeBase-Node-Mgr finish\n"
}

function  webase_sign(){
	#查找webase_sign文件夹
	echo "check webase_sign..."
	webase_sign_path=$(find $wb_path -name 'webase-sign' -type d)
	cd $webase_sign_path
	status=$(bash status.sh)
	if [[ $status == *"running"* ]] 
		then 
			msg=`echo ${status#*Port}`
			port=`echo ${msg%%i*}` #获得端口
	else 
		echo "no running"
	fi

	port_msg=`lsof -i:$port`
	if [[ $port_msg == *"LISTEN"* ]] 
		then 
		echo "WeBase-Sign is Successful"
	else 
		echo "WeBase-Sign is Fail"
	fi
	echo -e "Check WeBase-Sign finish\n"
}
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
# WeBase-Front子系统测试
sleep 3
webase_front
# WeBase-Node-Msg子系统测试
sleep 3
webase_node_mgr
# WeBase-Sign子系统测试
sleep 3
webase_sign
# WeBase-Web子系统测试
sleep 3
webase_web