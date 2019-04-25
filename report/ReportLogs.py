# -*- coding: utf-8 -*-

import os
import linecache
import time
import datetime
from AgentConfig import *
from AgentNetwork import postToBrowserServer
import requests
import re

from AgentUtils import currentMs
from AgentNetwork import *

NO_FILE_FOUND = False
IS_UPDATE_LOG = False
ONE_HOUR = 3600
ALL_ERROR_LOG_NAME = list()


log_info_sessions = []
for i in BROWSER_SERVER_INFO_URLS:
    log_info_sessions.append(requests.Session())


def readLogs(path, linenum=None):
    """read content of file"""
    if os.path.exists(path):
        if linenum is not None and linenum !="":
            content_list = linecache.getline(path, linenum)
        else:
            content_list = linecache.getlines(path)
            # log_line = len(cache_data)
        logger.debug(content_list)
        return content_list
    else:
        return NO_FILE_FOUND



def getErrorLogName(log_dir):
    log_name_list = os.listdir(log_dir)
    #logger.info("error log file dir：{}".format(log_dir))
    if len(log_name_list) == 0:
        logger.info("No Error Log File!")
        return NO_FILE_FOUND
    for log_name in log_name_list:
        global ALL_ERROR_LOG_NAME
        if log_name.startswith("webase-front-error") and log_name not in ALL_ERROR_LOG_NAME:
            create_time = getLogCreateTime(log_dir + log_name)
            now_time = datetime.datetime.now()
            offset = datetime.timedelta(days=-3)
            re_date = (now_time + offset).strftime('%Y-%m-%d %H:%M:%S')
            re_struct_time = time.strptime(re_date, '%Y-%m-%d %H:%M:%S')
            if create_time > re_struct_time:
                logger.info("log file：{}".format(log_name))
                ALL_ERROR_LOG_NAME.append(log_name)
                ALL_ERROR_LOG_NAME.sort(key=lambda fn:os.path.getmtime(log_dir+'/'+fn))  # Sort log file change times in ascending order
            else:
                continue


def scheduleGetNewFile(log_dir):
    while True:
        now = datetime.datetime.now()
        if now.minute == 2:
            getErrorLogName(log_dir)
            time.sleep(ONE_HOUR)
        else:
            time.sleep(0.5)
            continue


def getReportLines(node_state):
    global log_info_sessions
    arguement = {
        "attr": GET_LAST_NODE_LOG,
        "nodeP2PPort": int(node_state.p2pport),
        "hostIp": HOST_IP
    }
    try:
        rsp = randomPost(log_info_sessions, BROWSER_SERVER_INFO_URLS, arguement)
        rsp_data = json.loads(rsp.text)
        if rsp_data["code"] == 0 and rsp_data["data"] is not None:
            logger.debug(rsp_data)
            return rsp_data["data"]
        else:
            return None
    except Exception,e:
        logger.error(e)
        info = sys.exc_info()
        logger.error("get browser log number except for " + str(info[0]) + "." + str(info[1]))
        return None


def getLogCreateTime(log_path):
    """get the file's create time"""
    create_time = time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(os.stat(log_path).st_ctime))
    create_time_datetime = time.strptime(create_time, '%Y-%m-%d %H:%M:%S')
    return create_time_datetime


def getLastModifyTime(log_path):
    """get file's last modify time"""
    last_modify_time = time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(os.stat(log_path).st_mtime))
    modify_time_datetime = time.strptime(last_modify_time, '%Y-%m-%d %H:%M:%S')
    return modify_time_datetime


def getLogTime(log_content):
    """get line's time of log"""
    # DEFAULT_TIME = datetime.datetime.now()
    # DEFAULT_TIME_STR = DEFAULT_TIME.strftime('%Y-%m-%d %H:%M:%S')
    pattern = re.compile(r"\d{4}-\d{2}-\d{2}\s{1}\d{2}:\d{2}:\d{2}")
    result = re.findall(pattern, log_content)
    logger.debug("Matching time result：{}".format(result))
    try:
        return result[0]
    except:
        return None



def readLogsAndPost(start_line, end_line, node_state, file_name,log_content_list):
    while True:
        params = []
        for i in range(start_line, end_line):
            log_data = {}
            if i <= len(log_content_list):
                if not log_content_list[i - 1] or log_content_list[i - 1] == "":
                    log_data["logMsg"] = "DefaultNull"
                    logger.info(log_data["logMsg"])
                else:
                    log_data["logMsg"] = log_content_list[i - 1]
                    logger.info(log_data["logMsg"])
                log_time = getLogTime(log_content_list[i-1])
                if log_time:
                    log_data["logTime"] = log_time
                else:
                    log_data["logTime"] = getLogTime(log_content_list[i-2])
                log_data["rowNumber"] = i

                log_data["fileName"] = file_name
                params.append(log_data)
            else:
                break
        start_line = end_line
        end_line += REPORT_LOG_LINE
        postLogsToBrowserServer(BROWSER_SERVER_REPORT_URLS, node_state, NODE_LOG_INFO,
                            REPORT_REAL_NAME[NODE_LOG_INFO], params, currentMs())
        if start_line > len(log_content_list):
            break


def cycleReportLog(node_state, log_file_list=None):
    for log_name in ALL_ERROR_LOG_NAME:
        log_path = node_state.log_dir + "/" + log_name
        all_log_content = readLogs(log_path)
        if not all_log_content:
            continue
        # log_line = len(log_content)
        star_line = 1
        end_line = REPORT_LOG_LINE + 1  # Ending read log lines
        readLogsAndPost(star_line, end_line, node_state, log_name, all_log_content)



def uploadLogsInfo(nodes_state):
    if not ENABLE_ERROR_LOG_REPORT:
        return
    if len(UPLOAD_LOG_NODES) is 0:
        return
    while True:
        for name in UPLOAD_LOG_NODES:
            node_state = nodes_state[name]
            error_log = getErrorLogName(node_state.log_dir)
            if not ALL_ERROR_LOG_NAME:
                continue
            # logger.info(ALL_ERROR_LOG_NAME)
            last_report_line = getReportLines(node_state)
            logger.debug("last report log line：{}".format(last_report_line))
            if not last_report_line:
                cycleReportLog(node_state)
            else:
                row_num = last_report_line["rowNumber"]
                file_name = last_report_line["fileName"]
                last_report_file_index = ALL_ERROR_LOG_NAME.index(file_name)

                not_report_error_log = ALL_ERROR_LOG_NAME[last_report_file_index+1:]
                # for log_name in ALL_ERROR_LOG_NAME:
                log_path = node_state.log_dir + file_name
                log_content_list = readLogs(log_path)
                # log_count = len(log_content_list)
                star_line = row_num + 1
                end_line = REPORT_LOG_LINE + 1
                readLogsAndPost(star_line, end_line, node_state, file_name, log_content_list)
                if not_report_error_log:
                    continue
                cycleReportLog(node_state, not_report_error_log)
        time.sleep(REPORT_LOGS_GAP)


