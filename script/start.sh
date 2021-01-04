#!/usr/bin/env bash

LOG_WARN()
{
    local content=${1}
    echo -e "\033[31m[WARN] ${content}\033[0m"
}

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}


# 命令返回非 0 时，就退出
set -o errexit
# 管道命令中任何一个失败，就退出
set -o pipefail
# 遇到不存在的变量就会报错，并停止执行
set -o nounset
# 在执行每一个命令之前把经过变量展开之后的命令打印出来
#set -o xtrace

# 退出时，执行的命令
trap 'echo -e "Aborted, error $? in command: $BASH_COMMAND"; trap ERR; exit 1' ERR

# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
#__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
#__base="$(basename ${__file} .sh)"
__root="$(cd "$(dirname "${__dir}")" && pwd)" # <-- change this as it depends on your app

####### 参数解析 #######
cmdname=$(basename $0)
bcos_sdk_dir=""
guomi="n"

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-s fisco_bcos_sdk_dir] [-g]
    -s          FISCO-BCOS nodes sdk directory, like /xxx/xxx/nodes/[IP]/sdk
    -g          Use guomi, default no.
    -h          Show help info.
USAGE
    exit 1
}

while getopts s:gh OPT;do
    case $OPT in
        s)
            bcos_sdk_dir=$OPTARG
            ;;
        g)
            guomi="y"
            ;;
        h)
            usage
            exit 4
            ;;
        \?)
            usage
            exit 4
            ;;
    esac
done

# application.yml 配置中的加密类型
encrypt_type="0"

LOG_INFO "params: 是否使用国密:[${guomi}], bcos_sdk_dir:[${bcos_sdk_dir}]"

if [[ "${guomi}"x == "y"x ]] ; then
  # 使用国密
  encrypt_type="1"
fi

# change directory
cd "${__root}"

# check gradle installed
rm -rf "${__root}"/dist/*
rm -rf lib/*
# shellcheck disable=SC2086
bash gradlew clean build -x test && cd ..

# update yml config
LOG_INFO "Updating  yml config....."
cd "${__root}"/dist
cp -rf conf_template conf
# upate application.yml file
sed -i "s/encryptType.*#/encryptType: ${encrypt_type} #/g" conf/application.yml

# cp certifications from sdk dir
cp -frv "${bcos_sdk_dir}"/node.* ./conf
cp -frv "${bcos_sdk_dir}"/ca.crt ./conf

# start service
LOG_INFO "Starting WeBASE-Front......."
bash stop.sh
bash start.sh
