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

package com.webank.webase.front.scaffold;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.scaffold.entity.ReqProject;
import com.webank.webase.front.scaffold.entity.RspFile;
import com.webank.webase.front.util.CommonUtils;
import com.webank.webase.front.util.ValidateUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author marsli
 */
@Log4j2
@RestController
@RequestMapping(value = "scaffold")
public class ScaffoldController extends BaseController {
    @Autowired
    private ScaffoldService scaffoldService;


    @PostMapping("/export")
    public BaseResponse exportProjectApi(@Valid @RequestBody ReqProject param) {
        Instant startTime = Instant.now();
        log.info("start exportProjectApi param:{} groupId:{}", startTime.toEpochMilli(),
            param);
        if (StringUtils.isBlank(param.getChannelIp())) {
            param.setChannelIp("127.0.0.1");
        }
        // check artifact name and group name
        if (!CommonUtils.startWithLetter(param.getArtifactName())) {
            log.error("must start with letter");
            throw new FrontException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
        }
        // validate group name, ex: org.example
        if (!param.getGroup().contains(".")) {
            // only org
            if (!CommonUtils.startWithLetter(param.getGroup())) {
                log.error("group name must start with letter");
                throw new FrontException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
            }
        } else {
            // include org.xxx
            String[] groupNameArray = param.getGroup().split("\\.");
            for (String group: groupNameArray) {
                // not start or end with dot "."
                if (StringUtils.isBlank(group)) {
                    log.error("group cannot start or end with dot");
                    throw new FrontException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
                }
                if (!CommonUtils.startWithLetter(group)) {
                    log.error("package name must start with letter");
                    throw new FrontException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
                }
            }
        }
        // start export
        RspFile rspFile = scaffoldService.exportProject(param);
        log.info("end exportProjectApi useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), rspFile.getFileName());
        return new BaseResponse(ConstantCode.RET_SUCCESS, rspFile);
    }

    @GetMapping("/check")
    public BaseResponse checkChannelPort(@RequestParam("nodeIp") String nodeIp,
        @RequestParam("channelPort") int channelPort) {
        Instant startTime = Instant.now();
        log.info("start checkChannelPort startTime:{}, nodeIp:{} channelPort:{}",
            startTime.toEpochMilli(), nodeIp, channelPort);
        if(!ValidateUtil.ipv4Valid(nodeIp)) {
            log.error("not valid nodeIp:{}", nodeIp);
            throw new FrontException(ConstantCode.IP_FORMAT_ERROR);
        }
        Boolean result = scaffoldService.telnetChannelPort(nodeIp, channelPort);

        log.info("end exportProjectApi useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), result);
        return new BaseResponse(ConstantCode.RET_SUCCESS, result);
    }
}
