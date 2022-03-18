/**
 * Copyright 2014-2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.task;

import com.webank.webase.front.base.enums.CompileStatus;
import com.webank.webase.front.contract.LiquidCompileService;
import com.webank.webase.front.contract.entity.wasm.CompileTask;
import com.webank.webase.front.contract.entity.wasm.CompileTaskRepository;
import com.webank.webase.front.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DeleteLiquidCacheTask {

  @Autowired
  private CompileTaskRepository compileTaskRepository;

  @Scheduled(fixedDelayString = "${constant.deleteLiquidCacheTaskFixedDelay}")
  public void taskStart() {
    deleteTask();
  }

  public synchronized void deleteTask() {
    Instant now = Instant.now();
    log.debug("start deleteTask task, startTime:{}", now);
    List<CompileTask> allTask = new ArrayList<>();
    List<CompileTask> sucTask = compileTaskRepository.findByStatus(CompileStatus.SUCCESS.getValue());
    log.debug("deleteTask sucTask size:{}", sucTask.size());
    List<CompileTask> failTask = compileTaskRepository.findByStatus(CompileStatus.FAIL.getValue());
    log.debug("deleteTask sucTask size:{}", sucTask.size());
    allTask.addAll(sucTask);
    allTask.addAll(failTask);
    // 半小时后
    LocalDateTime halfHourAgo = LocalDateTime.now().minusMinutes(30L);
    allTask.stream()
        .filter(t -> t.getModifyTime().isBefore(halfHourAgo))
        .forEach(t -> {
          String targetPath = LiquidCompileService.getLiquidTargetPath(t.getGroupId(), t.getContractPath(), t.getContractName());
          File targetFile = new File(targetPath);
          boolean resultDir = false;
          if (targetFile.exists()) {
            resultDir = CommonUtils.deleteDir(targetFile);
            log.warn("delete path [{}], result:{}", targetPath, resultDir);
          }
          if (resultDir || !targetFile.exists()) {
            t.setStatus(CompileStatus.INIT.getValue());
            compileTaskRepository.save(t);
            log.debug("update db as deleted");
          }
        });
    log.debug("end deleteTask task, duration:{}", Duration.between(now, Instant.now()).toMillis());
  }
}
