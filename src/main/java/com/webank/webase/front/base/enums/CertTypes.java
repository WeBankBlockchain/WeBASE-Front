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

package com.webank.webase.front.base.enums;

/**
 * cert type enums
 * chain: ca.crt, node: node.crt
 * sdk: certs in sdk directory
 */
public enum CertTypes {
    CHAIN(1), NODE(2), OTHERS(3);

    private int value;

    CertTypes(Integer certTypes) {
        this.value = certTypes;
    }

    public int getValue() {
        return this.value;
    }
}
