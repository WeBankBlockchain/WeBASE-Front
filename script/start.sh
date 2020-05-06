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

if [[ $# -lt 1 ]] ; then
    LOG_WARN "USAGE: $0 bcos_sdk_dir"
    LOG_WARN "       bcos_sdk_dir    : bcos nodes sdk directory, like /xxx/xxx/nodes/[IP]/sdk."
    exit 1;
fi

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
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
#__base="$(basename ${__file} .sh)"
__root="$(cd "$(dirname "${__dir}")" && pwd)" # <-- change this as it depends on your app

###### Read user input value.
# $1 tip message
# $2 regex express of valid value
# $3 default value
read_value=""
function readValue(){
    read_value=""
    tip_msg="$1"
    regex_of_value="$2"
    default_value="$3"

    until  [[ ${read_value} =~ ${regex_of_value} ]];do
        read -r -p "${tip_msg}" read_value;read_value=${read_value:-${default_value}}
    done
}

# sdk 证书的目录
bcos_sdk_dir="$1"
# 是否使用国密，y: 使用，n: 不使用️，默认: n
guomi=n
# gradle 编译时国密参数
gradle_guomi_param=""
# application.yml 配置中的加密类型
encrypt_type="0"

readValue "是否使用国密 (Yy/Nn), 默认: Nn ? " "^([Yy]|[Nn])$" "n"
guomi=$(echo "${read_value}" | tr [A-Z]  [a-z])
LOG_INFO "params: 是否使用国密:[${guomi}], bcos_sdk_dir:[${bcos_sdk_dir}]"

if [[ "${guomi}"x == "y"x ]] ; then
  ## set front gradle build param
  echo ""
  echo "1 : 使用用默认 solcj-all-0.4.25-gm.jar."
  echo "2 : 使用本地目录 lib 下的 solcJ-all-x.x.x-gm.jar，请手动下载后放到 lib 目录中"
  echo "3 : 从官方下载指定版本 solcJ-all-x.x.x-gm.jar"
  echo ""
  readValue "请选择国密 solc 的版本（1/2/3）, 默认: 1 ? " "^(1|2|3)$" "1"
  case ${read_value} in
   1)
          gradle_guomi_param=" -Pguomi "
          ;;
   2)
          gradle_guomi_param=" -Pguomi=local"
          ;;
   3)
          readValue "请输入 solcJ 的版本号，e.g: 0.4.25, 默认: 0.4.25 ? " "^[0-9]+\.[0-9]+\.[0-9]+$" "0.4.25"
          gradle_guomi_param=" -Pguomi=${read_value} "
          ;;
   *)
          LOG_WARN "选择国密 solcJ 版本错误"
          exit 1
  esac

  # 使用国密
  encrypt_type="1"
fi

# change directory
cd "${__root}"

# check gradle installed
rm -rf ${__root}/dist/*
rm -rf lib/*
bash gradlew clean build -x test ${gradle_guomi_param} && cd ..

# update yml config
LOG_INFO "Updating  yml config....."
cd ${__root}/dist
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
