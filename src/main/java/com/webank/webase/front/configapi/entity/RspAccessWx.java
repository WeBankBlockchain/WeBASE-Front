/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.configapi.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * {"access_token":"72_x3ndcM1sAfaAIVGQPL4RvnVK_p-iCepw9mAQLBkKPTuXTOop2kFEwzTVXkHGOpjKTedp3YmJ2nsjT7B1o4pL2JsHZhDZi9c8-kUFKR-3mcY",
 * "expires_in":7200,
 * "refresh_token":"72_K9moQzPW78pFeFiD-5sAao_oZTVRO-hX1ecbY9Zexd4zmghQagF7YFGn68xJHiHJd5Ro0vi3E5tIHBm1zWamSsF_deWkgCgYVAs4PXNITxg",
 * "openid":"oQ3dT6SSdJOIMiIk20JNezcyDYnI",
 * "scope":"snsapi_base"}
 */
@Data
public class RspAccessWx {
    private String access_token;
    private String refresh_token;
    private String openid;
    private String scope;
    private Long expires_in;
}
