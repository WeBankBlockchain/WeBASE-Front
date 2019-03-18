#!/usr/bin/env python 
# -*- coding: utf-8 -*-

import json
import socket
import sys
import logging
from logging.handlers import TimedRotatingFileHandler
import os, re

CONFIG_FILE = "config.json"


##################  Default fields  ###################
ACCESS_NODE_INTERVAL = 10  #  How long access and report node info
HOST_IP = ""  # The external network IP of the machine
BLOCKCHAIN_NODES = []


BROWSER_REQUEST_URL = "/fisco-bcos-node-mgr/report/blockChainInfo"

POST_WORKER_NUM = 2  # the number of workers report to server
REPORT_LOGS_QUEUE = 0     # the queue which report logs
REPORT_BLOCK_INFO_QUEUE = 1  # the queue which report block info

POST_WORKER_LEAK_GAP = 1  # report worker gap
METRIC_DATA_LIST_LIMIT = 20  # How many data every worker report to server 
MAX_POST_QUEUE_SIZE = 1000  # The limit of report queue
REPORT_BLOCK_GAP_LIMIT = 10  # How many blocks each report
UPLODA_BLOCK_GAP = 5  # The gap of report blocks
REPORT_POST_TIMEOUT = 5  # The post timeout to server

#################  Report Logs Configuration ##########################
REPORT_LOGS_GAP = 60  # The gap of read logs
REPORT_LOGS_LINES = 10  # Lines of read logs
ERROR_LOG_ROUTE = "../log/"


#####################  Logs Configuration ######################
def CURR_DIR():
    path = os.path.realpath(sys.argv[0])
    if os.path.isfile(path):
        path = os.path.dirname(path)
    return os.path.abspath(path)


log_path = os.path.join(CURR_DIR(), 'log')
if not os.path.exists(log_path):
    os.makedirs(log_path)

logger = logging.getLogger("Rotating Log")
logger.setLevel(logging.INFO)

handler = TimedRotatingFileHandler(log_path + "/report.log", when="d", interval=1)
# handler = TimedRotatingFileHandler(log_path + "/report.log", when="s", interval=1)
# handler.suffix = "%Y-%m-%d.log"

logFormat = logging.Formatter("[%(asctime)s] [%(filename)s] [line:%(lineno)d] [%(levelname)s] %(message)s")
handler.setFormatter(logFormat)

logger.addHandler(handler)


#########################################################

def mustLoad(cfg, key):
    try:
        return cfg[key]
    except:
        logger.error("config.json error: key " + str(key) + " not found")
        sys.exit()


def mayLoad(cfg, key, default):
    try:
        value = cfg[key]
        if value == "default":
            return default
        return value
    except:
        return default


def getHostIp():
    try:
        TRY_IP = BROWSER_SERVERS[0]["ip"]
        TRY_PORT = BROWSER_SERVERS[0]["port"]
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect((TRY_IP, int(TRY_PORT)))
        ip = s.getsockname()[0]
    except:
        logger.error("Could not connect to server" + TRY_IP + ":" + TRY_PORT)
    finally:
        s.close()

    return ip


######################  Read Configuration  ######################
with open(CONFIG_FILE, "r") as f:
    try:
        cfg = json.load(f)
        BROWSER_SERVERS = mustLoad(cfg, "node_manager_servers")
        BLOCKCHAIN_NODES = mustLoad(cfg, "blockchain_nodes")

        ACCESS_NODE_INTERVAL = mayLoad(cfg, "access_node_interval", ACCESS_NODE_INTERVAL)

        HOST_IP = mayLoad(cfg, "host_ip", HOST_IP)
        if HOST_IP == "" or HOST_IP == "default" or HOST_IP == None:
            HOST_IP = getHostIp()

        BROWSER_REQUEST_URL = mayLoad(cfg, "browser_request_url", BROWSER_REQUEST_URL)

        POST_WORKER_NUM = mayLoad(cfg, "post_worker_num", POST_WORKER_NUM)
        MAX_POST_QUEUE_SIZE = mayLoad(cfg, "max_post_queue_size", MAX_POST_QUEUE_SIZE)
        REPORT_BLOCK_GAP_LIMIT = mayLoad(cfg, "report_block_gap_limit", REPORT_BLOCK_GAP_LIMIT)
        REPORT_POST_TIMEOUT = mayLoad(cfg, "report_post_timeout", REPORT_POST_TIMEOUT)
        REPORT_LOG_LINE = mayLoad(cfg, "report_log_lines", REPORT_LOGS_LINES)
    except:
        logger.error(str(CONFIG_FILE) + " format error")
        exit()


def printConfig():
    logger.info("access_node_interval: " + str(ACCESS_NODE_INTERVAL))

    logger.info("host_ip: " + str(HOST_IP))
    logger.info("browser_servers: " + str(BROWSER_SERVERS))
    logger.info("blockchain_nodes: " + str(BLOCKCHAIN_NODES))
    logger.info("browser_report_method: " + str(BROWSER_REQUEST_URL))
    # logger.info("browser_info_method: " + str(BROWSER_INFO_METHOD))
    logger.info("post_worker_num: " + str(POST_WORKER_NUM))
    logger.info("max_post_queue_size: " + str(MAX_POST_QUEUE_SIZE))
    logger.info("report_block_gap_limit: " + str(REPORT_BLOCK_GAP_LIMIT))
    logger.info("report_post_timeout: " + str(REPORT_POST_TIMEOUT))


printConfig()

####################### Fields Definition #########################
BLOCK_HEIGHT = "block_height"
GET_LAST_BLOCK = "latest_block"
BLOCK_INFO = "block_info"
PENDING_TX_COUNT = "pending_tx_count"
NODE_LOG_INFO = "node_log"
GET_LAST_NODE_LOG = "latest_node_log"
PBFT_VIEW = "pbft_view"

ENLARGE_FACTOR = 10000
SEP_SYMBLE = "|"
NORMAL_SEP_SYMBLE = "_"

REPORT_REAL_NAME = {
    BLOCK_HEIGHT: u"区块高度",
    BLOCK_INFO: u"块信息",
    PENDING_TX_COUNT: u"正在处理的交易数",
    NODE_LOG_INFO: u"错误日志",
    GET_LAST_NODE_LOG: u"最后上报的错误日志信息",
    GET_LAST_BLOCK: u"浏览器当前块高",
    PBFT_VIEW: u'区块view'
}

####################### Report Switch #########################
ENABLE_BLOCK_REPORT = True
ENABLE_SINGLE_POINT_REPORT = False
ENABLE_ERROR_LOG_REPORT = True  

ATTR_NAME = "attr"
REPORT_KEY_RULE = {}

####################################################### 

BLOCKCHAIN_NODE_CONFIGS = []
UPLOAD_BLOCK_NODES = []
UPLOAD_LOG_NODES = []
# load nodes' config.json
for node in BLOCKCHAIN_NODES:
    chainname = node["chainname"]
    name = node["name"]
    node_dir = node["node_dir"]
    node_config_dir = node["node_dir"] +'/' + 'config.json'
    if node["front_error_log_dir"] == "default":
        node_log_dir = ERROR_LOG_ROUTE
    else:
        node_log_dir = node["front_error_log_dir"] + '/'
    try:
        with open(node_config_dir) as file:
            data = file.read()
            config_json = json.loads(data)

            if node.has_key("upload_block") and node["upload_block"] == "ON":
                UPLOAD_BLOCK_NODES.append(name)
            if node.has_key("upload_log") and node["upload_log"] == "ON":
                UPLOAD_LOG_NODES.append(name)
            config_json["name"] = name
            config_json["config"] = node_config_dir
            config_json["chainname"] = chainname
            config_json["log_dir"] = node_log_dir
            config_json["nodedir"] = os.path.dirname(node_config_dir)
            BLOCKCHAIN_NODE_CONFIGS.append(config_json)
    except Exception as e:
        logger.error("handler_block_info error. node:{} error:{} ".format(name, e))
        exit()

# global variable generation
BROWSER_SERVER_REPORT_URLS = []  # Report url
BROWSER_SERVER_INFO_URLS = []  # Get report info url
for server in BROWSER_SERVERS:
    BROWSER_SERVER_REPORT_URLS.append("http://" + server["ip"] + ":" + server["port"] + BROWSER_REQUEST_URL)
    BROWSER_SERVER_INFO_URLS.append("http://" + server["ip"] + ":" + server["port"] + BROWSER_REQUEST_URL)

NODES_URLS = []
for node in BLOCKCHAIN_NODE_CONFIGS:
    NODES_URLS.append("http://127.0.0.1:" + str(node["rpcport"]))
