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

package com.webank.webase.front.base.enums;

/**
 * @author marsli
 */
public enum PrecompiledTypes {
  /**
   * System Config
   */
  SYSTEM_CONFIG(1000),

  /**
   * System Config Liquid
   */
  SYSTEM_CONFIG_LIQUID(11000),

  /**
   * CRUD
   */
  CRUD(1009),

  /**
   * CRUD Liquid
   */
  CRUD_LIQUID(11009),

  /**
   * Consensus
   */
  CONSENSUS(1003),

  /**
   * Consensus Liquid
   */
  CONSENSUS_LIQUID(11003),

  /**
   * Contract
   */
  CNS(1004),

  /**
   * Contract Liquid
   */
  CNS_LIQUID(11004),

  /**
   * BFS
   */
  BFS(100111),

  /**
   * BFS Liquid
   */
  BFS_LIQUID(1100111),

  /**
   * CONTRACT AUTH
   */
  CONTRACT_AUTH(1005),

  /**
   * Committee Manager
   */
  COMMITTEE_MANAGER(10001);

  private int value;

  PrecompiledTypes(Integer type) {
    this.value = type;
  }

  public int getValue() {
    return this.value;
  }
}
