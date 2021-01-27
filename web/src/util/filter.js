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

import Vue from 'vue';
Vue.filter('formatErrorMessage', function (value) {
    try {
        return `${value.formattedMessage} `
    } catch (error) {
        console.warn(error);
    }
});

Vue.filter('contractSource', function (value) {
    switch (value) {
        case "0x0000000000000000000000000000000000001000":
            return "SystemConfig"
            break;
        case "0x0000000000000000000000000000000000001001":
            return "TableFactory"
            break;
        case "0x0000000000000000000000000000000000001002":
            return "CRUD"
            break;
        case "0x0000000000000000000000000000000000001003":
            return "Consensus"
            break;
        case "0x0000000000000000000000000000000000001004":
            return "CNS"
            break;
        case "0x0000000000000000000000000000000000001005":
            return "Permission"
            break;
        case "0x0000000000000000000000000000000000001007":
            return "CSM"
            break;
        case "0x0000000000000000000000000000000000001008":
            return "ChainGovern"
            break;
        case "0x0000000000000000000000000000000000001009":
            return "ChainCharge"
            break;
        default:
            return ""
            break;
    }
});