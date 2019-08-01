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
const Mock = require('mockjs');

let dayLyData = { "code": 0, "data": { "TxDaily": [{ "Day": "2018-11-09", "NetworkId": "1", "TxCount": 11 }, { "Day": "2018-11-12", "NetworkId": "1", "TxCount": 9 }] }, "message": "Success" };

Mock.mock('/api/bcosproxy/interface', (req, res) => {
    req.body = JSON.parse(req.body)
    if (req.body.Module === 'tx_dayly' && req.body.Operation === 'get_dayly') {
        return dayLyData;
    }


});
