/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.performance.entity;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Data
public class Cpu {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private BigDecimal usedVolumn;
    private BigDecimal totalVolumn;
    private BigDecimal useRatio;
    private Long timestamp;

    /**
     * Cpu constructor.
     * 
     * @param usedVolumn usedVolumn
     * @param totalVolumn totalVolumn
     * @param useRatio useRatio
     * @param timestamp timestamp
     */
    public Cpu(BigDecimal usedVolumn, BigDecimal totalVolumn, BigDecimal useRatio, Long timestamp) {
        this.usedVolumn = usedVolumn;
        this.totalVolumn = totalVolumn;
        this.useRatio = useRatio;
        this.timestamp = timestamp;
    }

    public Cpu() {}
}
