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

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqSdkConfig {
    private Boolean useSmSsl;
    @NotNull
    private List<String> peers;
    private String caCertStr;
    private String sdkCertStr;
    private String sdkKeyStr;
    private String smCaCertStr;
    private String smSdkCertStr;
    private String smSdkKeyStr;
    private String smEnSdkCertStr;
    private String smEnSdkKeyStr;

}
