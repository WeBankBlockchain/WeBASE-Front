/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.front.solc;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.solc.entity.RspDownload;
import com.webank.webase.front.solc.entity.SolcInfo;
import com.webank.webase.front.util.FrontUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * upload and download solc js file
 */
@Slf4j
@Api(value = "/solc", tags = "upload/download solc js controller")
@RestController
@RequestMapping("solc")
public class SolcController {

    @Autowired
    private SolcService solcService;

    @ApiOperation(value = "upload solc js file", notes = "upload solc js file")
//    @PostMapping("/upload")
    public BaseResponse upload(@RequestParam("fileName") String fileName,
                               @RequestParam("solcFile") MultipartFile solcFile,
                               @RequestParam(value = "description", required = false, defaultValue = "") String description) throws IOException {
        if (StringUtils.isBlank(fileName)) {
            throw new FrontException(ConstantCode.PARAM_FAIL_SOLC_FILE_NAME_EMPTY);
        }
        if (solcFile.getSize() == 0L) {
            throw new FrontException(ConstantCode.PARAM_FAIL_SOLC_FILE_EMPTY);
        }
        solcService.saveSolcFile(fileName, solcFile, description);
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }

    @ApiOperation(value = "get solc file name in solcjs dir", notes = "list solc file info")
    @GetMapping("/list")
    public BaseResponse getSolcList() {
        // get list
        List<String> resList = solcService.checkSolcFile();
        return new BaseResponse(ConstantCode.RET_SUCCESS, resList);
    }

    @ApiOperation(value = "delete uploaded solc js", notes = "delete uploaded solc js file")
    @ApiImplicitParam(name = "solcId", value = "solc info id", required = true,
            dataType = "Integer", paramType = "path")
//    @DeleteMapping("/{solcId}")
    public BaseResponse deleteSolcFile(@PathVariable("solcId") Integer solcId) {

        boolean deleteRsp = solcService.deleteFile(solcId);
        return new BaseResponse(ConstantCode.RET_SUCCESS);
    }


    /**
     * download Solc js file
     * @return fileName in headers, file InputStream in body
     */
    @ApiOperation(value = "download existed solc js", notes = "download solc js file")
    @ApiImplicitParam(name = "fileName", value = "solc file name", required = true,
            dataType = "String")
//    @PostMapping("/download")
    public ResponseEntity<InputStreamResource> downloadSolcFile(@RequestParam("fileName") String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new FrontException(ConstantCode.PARAM_FAIL_SOLC_FILE_NAME_EMPTY);
        }
        log.info("downloadSolcFile start. fileName:{}", fileName);
        RspDownload rspDownload = solcService.getSolcFile(fileName);
        return ResponseEntity.ok().headers(FrontUtils.headers(rspDownload.getFileName()))
                .body(new InputStreamResource(rspDownload.getInputStream()));
    }

}
