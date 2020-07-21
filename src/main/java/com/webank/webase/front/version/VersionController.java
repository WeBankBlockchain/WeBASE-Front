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

package com.webank.webase.front.version;

import com.webank.webase.front.base.properties.VersionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * return version of local server
 */
@Slf4j
@RestController
@RequestMapping("version")
public class VersionController {

    @Autowired
    private VersionProperties versionProperties;
    @Autowired
    private VersionService versionService;

    /**
     * webase-web: when add first front, return version and tips
     * @return
     */
    @GetMapping()
    public String getServerVersion() {
        return versionProperties.getVersion();
    }

    @GetMapping("sign")
    public String getSignVersion() {
        return versionService.getSignServerVersion();
    }

}
