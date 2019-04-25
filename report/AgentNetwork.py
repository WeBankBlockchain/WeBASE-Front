#!/usr/bin/env python 
# -*- coding: utf-8 -*-
#

import time
import random
import sys
import Queue
import requests
import urllib2
import threading
import copy

from AgentConfig import *
from urllib2 import URLError


def randomPost(_sessions, urls, json_args):
    if len(urls) is 0:
        raise Exception("Servers port is None")
    t = random.randint(0, len(urls) - 1)
    f = t

    get_block_urls = copy.deepcopy(urls)
    get_node_log_urls = copy.deepcopy(urls)
    if json_args.has_key("attr") and json_args.has_key("hostIp") and json_args.has_key("nodeP2PPort"):
        for i in range(0, len(urls)):
            get_block_urls[i] += "/"+GET_LAST_BLOCK + "/" + json_args["hostIp"] + "/" + str(json_args["nodeP2PPort"])
            get_node_log_urls[i] += "/" + json_args["attr"] + "/" + json_args["hostIp"] + "/" + str(json_args["nodeP2PPort"])

    while True:
        try:

            if json_args.has_key("attr") and json_args["attr"] == GET_LAST_BLOCK:
                rsp = _sessions[f].get(get_block_urls[f])
            elif json_args.has_key("attr") and json_args["attr"] == GET_LAST_NODE_LOG:
                rsp = _sessions[f].get(get_node_log_urls[f])
            else:
                rsp = _sessions[f].post(urls[f], json=json_args, timeout=REPORT_POST_TIMEOUT)
            rsp.raise_for_status()

            return rsp
        except URLError as u:
            logger.error(u)
            f = (f + 1) % len(urls)
        if f == t:
            logger.info( "###########Post ERROR: ")
            # logger.info( json.dumps(json_args, sort_keys=True, indent=4, separators=(',', ':')))
            raise Exception("Servers port not avaliable")


post_queues = []
for i in range(0, POST_WORKER_NUM):
    post_queues.append(Queue.Queue())



def isPostQueueFull(idx):
    queue_total_size = post_queues[idx].qsize()
    return queue_total_size >= MAX_POST_QUEUE_SIZE


def isPostQueuesEmpty():
    for post_queue in post_queues:
        if post_queue.qsize() is not 0:
            return False
    return True


def isPostQueueHasEmpty():
    for post_queue in post_queues:
        if post_queue.qsize() is 0:
            return True
    return False

def isPostQueueEmpty(idx):
    if post_queues[idx].qsize() is 0:
        return True
    return False


def postWorker(idx):
    """Continuously process the contents of the queue"""
    global post_queues

    while True:
        time.sleep(POST_WORKER_LEAK_GAP)  # the gap of report workers
        if post_queues[idx].qsize() != 0:
            logger.info("Post worker start post_queues[" + str(idx) + "] size " + str(post_queues[idx].qsize()))
            parameters = post_queues[idx].get()
            post_queues[idx].task_done()

            # request url
            post_sessions = []
            browser_server_urls = parameters["browser_server_urls"]
            for i in range(0, len(browser_server_urls)):
                post_sessions.append(requests.Session())

            # post params
            arguement = dict()
            arguement["metricDataList"] = parameters["metricDataList"]
            # logger.info("parameters['metricDataList']:{}".format(parameters["metricDataList"]))


            while True:
                try:
                    if arguement["metricDataList"] != []:
                        logger.info("report contents：{}".format(arguement))
                        rsp = randomPost(post_sessions, browser_server_urls, arguement)
                    break
                except:
                    info = sys.exc_info()
                    logger.error(arguement)
                    logger.error("Post queue[" + str(idx) + "] to browser server except for " + str(info[0]) + "." + str(info[1]))
                    time.sleep(0.7)
            logger.info("Post worker done post_queues[" + str(idx) + "] size " + str(post_queues[idx].qsize()))


def thread_putToPostBuffer(urls, node_state, attr, attr_name, value, timestamp):
    global post_queues
    parameters = {
        "browser_server_urls": urls,
        "metricDataList": [
            {
                "nodeP2PPort": node_state.p2pport,
                "attr": attr,
                "metricValue": value,
                "hostIp": HOST_IP
            }
        ]
    }

    # logger.info( arguement)
    while True:
        try:
            if attr == NODE_LOG_INFO:
                post_queues[REPORT_LOGS_QUEUE].put(parameters, block=True, timeout=0)
                break
            else:
                post_queues[REPORT_BLOCK_INFO_QUEUE].put(parameters, block=True, timeout=0)
                break
        except:
            logger.info("The queue is full,waiting.....")
            # time.sleep(5)
            continue
    # try:
    #    rsp = requests.post(BROWSER_SERVER_URL, json=arguement)
    # except:
    #    logger.info( "Could not post to BROWSER_SERVER")


def postToBrowserServer(urls, node_state, attr, attr_name, value, timestamp):
    if value == []:
        return

    while isPostQueueFull(REPORT_BLOCK_INFO_QUEUE):
        logger.info("Post block info queue is full, waiting.")
        time.sleep(10)

    # open a thread send message
    t = threading.Thread(target=thread_putToPostBuffer,
                         args=(urls, node_state, attr, attr_name, value, timestamp),
                         name="thread_putToPostBuffer")
    t.start()


def postLogsToBrowserServer(urls, node_state, attr, attr_name, value, timestamp):
    if value == []:
        return
    while not isPostQueueEmpty(REPORT_BLOCK_INFO_QUEUE):
        logger.info("waiting until finish post block info ......")
        time.sleep(5)

    logger.info("##start post logs to browser ...")
    while isPostQueueFull(REPORT_LOGS_QUEUE):
        logger.info("Post logs queue is full, waiting.")
        time.sleep(10)

    # open a thread send message
    t = threading.Thread(target=thread_putToPostBuffer,
                         args=(urls, node_state, attr, attr_name, value, timestamp),
                         name="thread_putToPostBuffer")
    t.start()




def accessNodeRpcPort(arguement, node_name, rpcPort):
    try:
        rsp = requests.post("http://" + HOST_IP + ":" + str(rpcPort), json=arguement)
        info = json.loads(rsp.text)
        return info  # return json object of query
    except:
        logger.warning("Could not access" + node_name + " RPC port " + str(rpcPort))

        # RPC端口错误，则告警
        # global last_report_time
        # if currentMs() - last_report_time > 60000 : #相同的告警60s上报一次
        # last_report_time = currentMs()
        raise Exception("Node RPC port access error")