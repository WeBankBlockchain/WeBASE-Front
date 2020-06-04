/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import Vue from 'vue'
import Router from 'vue-router'
import { getCookie } from '@/util/util'

const main = resolve => require(['@/views/index/main'], resolve);
const home = resolve => require(['@/views/home/home'], resolve);
const group = resolve => require(['@/views/group'], resolve);
const contract = resolve => require(['@/views/chaincode/contract'], resolve);
const oldContract = resolve => require(['@/views/chaincode/oldContract'], resolve);
const rivateKeyManagement = resolve => require(['@/views/rivateKeyManagement'], resolve);
const hostMetric = resolve => require(['@/views/hostMetric'], resolve);
const nodesMetric = resolve => require(['@/views/nodesMetric'], resolve);
const blockEvent = resolve => require(['@/views/blockEvent'], resolve);
const contractEvent = resolve => require(['@/views/contractEvent'], resolve);
const abiList = resolve => require(['@/views/abiList'], resolve);
const parseAbi = resolve => require(['@/views/parseAbi'], resolve);
const blank = resolve => require(['@/views/blank'], resolve);
Vue.use(Router);
const routes = [
    {
        path: '/',
        redirect: '/home',
    },
    {
        path: '/main',
        name: 'main',
        redirect: '/home',
        leaf: true,
        menuShow: true,
        iconCls: 'wbs-icon-gailan sidebar-icon',
        component: main,
        children: [
            {
                path: '/home', component: home, name: '数据概览', enName: 'statistics', menuShow: true, meta: { requireAuth: false }
            }
        ]
    },
    {
        path: '/',
        component: main,
        name: '节点管理',
        leaf: true,
        menuShow: true,
        iconCls: 'wbs-icon-group sidebar-icon',
        children: [
            { path: '/group', component: group, name: '节点管理', enName: 'nodeManagement', menuShow: true, meta: { requireAuth: false } },
        ]
    },
    {
        path: '/',
        component: main,
        name: '合约管理',
        enName: 'contractManagement',
        menuShow: true,
        iconCls: 'wbs-icon-heyueguanli sidebar-icon',
        children: [
            { path: '/contract', component: contract, name: '合约IDE', enName: 'contractIDE', menuShow: true, meta: { requireAuth: false } },
            { path: '/contractList', component: oldContract, name: '合约列表', enName: 'contractList', menuShow: true, meta: { requireAuth: false } },
            { path: '/abiList', component: abiList, name: 'Abi列表', enName: "abiList", menuShow: true, meta: { requireAuth: false } },
            { path: '/parseAbi', component: parseAbi, name: '解析Abi', enName: "parseAbi", menuShow: true, meta: { requireAuth: false } },
            { path: '/privateKeyManagement', component: rivateKeyManagement, name: '测试用户', enName: 'testUserList', menuShow: true, meta: { requireAuth: false } },
        ]
    },
    {
        path: '/',
        component: main,
        name: '系统监控',
        enName: 'systemMonitoring',
        menuShow: true,
        iconCls: 'wbs-icon-monitor sidebar-icon',
        children: [
            { path: '/hostMetric', component: hostMetric, name: '主机指标', enName: 'hostMetrics', menuShow: true, meta: { requireAuth: false } },
            { path: '/nodesMetric', component: nodesMetric, name: '节点指标', enName: 'nodeMetrics', menuShow: true, meta: { requireAuth: false } },
        ]
    }, 
    {
        path: '/',
        component: main,
        name: '订阅事件',
        enName: 'subscribeEvent',
        menuShow: true,
        iconCls: 'wbs-icon-dingyue sidebar-icon',
        children: [
            { path: '/blockEvent', component: blockEvent, name: '出块事件', enName: 'blockEvent', menuShow: true, meta: { requireAuth: false } },
            { path: '/contractEvent', component: contractEvent, name: '合约Event事件', enName: 'contractEvent', menuShow: true, meta: { requireAuth: false } }
        ]
    },
    {
        path: '/',
        component: main,
        name: '订阅事件',
        enName: 'subscribeEvent',
        menuShow: false,
        iconCls: 'wbs-icon-dingyue sidebar-icon',
        children: [
            { path: '/blank', component: blank, name: '出块事件', enName: 'blank', menuShow: true, meta: { requireAuth: false } },
        
        ]
    },
]
const router = new Router({
    routes
});

const originalPush = Router.prototype.push;
Router.prototype.push = function push(location, onResolve, onReject) {
    if (onResolve || onReject) return originalPush.call(this, location, onResolve, onReject)
    return originalPush.call(this, location).catch(err => err)
}

export default router
