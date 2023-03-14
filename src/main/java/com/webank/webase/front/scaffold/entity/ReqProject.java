/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.front.scaffold.entity;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;

/**
 * @author marsli
 */
@Data
@NoArgsConstructor
public class ReqProject {
    @NotNull
    private List<Integer> contractIdList;
    @NotBlank
    private String group;
    @NotBlank
    private String artifactName;
    @NotNull
    private Integer groupId;
    /**
     * channel ip for exported project to connect node
     */
    private String channelIp;
    private Integer channelPort;
    /**
     * select multi user to export in project by p12 format
     */
    private List<String> userAddressList;
}
