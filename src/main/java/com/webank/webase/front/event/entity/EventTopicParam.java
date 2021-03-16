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

package com.webank.webase.front.event.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * event's topics, one eventName topic (sha name(type) as topic) and most 3 indexed param topic(sha value as topic)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventTopicParam {
    private String eventName;
    private IndexedParamType indexed1;
    private IndexedParamType indexed2;
    private IndexedParamType indexed3;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IndexedParamType {
        private String type;
        // indexed value
        private String value;

    }
}
