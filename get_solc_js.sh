#!/usr/bin/env bash

# 在源码编译得到的WeBASE-Front/dist目录中执行

CURRENT_DIR=$(pwd)
# if before build, target dir is [src/main/resources/static/static/js]
OUTPUT_DIR="static/static/js"


####### 参数解析 #######
PARAM_ERROR=1
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-o output_relative_directory]

    -o    solidity js file download output [relative] directory, ex: static/static/js is [./static/static/js]
USAGE
    exit $PARAM_ERROR
}


while getopts o:h OPT;do
    case ${OPT} in
        o)
            OUTPUT_DIR="$OPTARG"
            ;;
        h)
            usage
            exit ${PARAM_ERROR}
            ;;
        \?)
            usage
            exit ${PARAM_ERROR}
            ;;
    esac
done

get_solc_js(){
    STATIC_JS_DIR=${CURRENT_DIR}/${OUTPUT_DIR}
    if [ ! -d "${STATIC_JS_DIR}" ];then
        echo "Directory not exist: ${STATIC_JS_DIR}"
        exit 1
    else
        echo "Now download all solidity js in ${STATIC_JS_DIR}, if offline, download js file and put them in this dir"
        if [[ ! -f "${STATIC_JS_DIR}/v0.4.25.js" ]]; then
            curl -#L https://github.com/WeBankBlockchain/WeBASELargeFiles/releases/download/v3.0.0/v0.4.25.js -o "${STATIC_JS_DIR}"/v0.4.25.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.4.25-gm.js" ]]; then
            curl -#L https://github.com/WeBankBlockchain/WeBASELargeFiles/releases/download/v3.0.0/v0.4.25-gm.js -o "${STATIC_JS_DIR}"/v0.4.25-gm.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.5.2.js" ]]; then
            curl -#L https://github.com/WeBankBlockchain/WeBASELargeFiles/releases/download/v3.0.0/v0.5.2.js -o "${STATIC_JS_DIR}"/v0.5.2.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.5.2-gm.js" ]]; then
            curl -#L https://github.com/WeBankBlockchain/WeBASELargeFiles/releases/download/v3.0.0/v0.5.2-gm.js -o "${STATIC_JS_DIR}"/v0.5.2-gm.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.6.10.js" ]]; then
            curl -#L https://github.com/WeBankBlockchain/WeBASELargeFiles/releases/download/v3.0.0/v0.6.10.js -o "${STATIC_JS_DIR}"/v0.6.10.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.6.10-gm.js" ]]; then
            curl -#L https://github.com/WeBankBlockchain/WeBASELargeFiles/releases/download/v3.0.0/v0.6.10-gm.js -o "${STATIC_JS_DIR}"/v0.6.10-gm.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.8.11.js" ]]; then
            curl -#L https://github.com/WeBankBlockchain/WeBASELargeFiles/releases/download/v3.0.0/v0.8.11.js -o "${STATIC_JS_DIR}"/v0.8.11.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.8.11-gm.js" ]]; then
            curl -#L https://github.com/WeBankBlockchain/WeBASELargeFiles/releases/download/v3.0.0/v0.8.11-gm.js -o "${STATIC_JS_DIR}"/v0.8.11-gm.js
        fi
    fi
}
get_solc_js

echo "end of script"
exit 0