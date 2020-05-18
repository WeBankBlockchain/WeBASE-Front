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
import com.webank.webase.front.solc.entity.ReqUploadSolc;
import com.webank.webase.front.solc.entity.RspDownload;
import com.webank.webase.front.solc.entity.SolcInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SolcService {

	private static final String SOLC_BASE_PATH = "./solcjs" + File.separator;
	private static final String SOLC_FILE_PATH = SOLC_BASE_PATH + "%1s.js";

	@Autowired
	private SolcRepository solcRepository;

	@Transactional
	public void saveSolcFile(String fileName, MultipartFile solcFile, String description) {
		Long fileSize = solcFile.getSize();

		if (description.isEmpty()) {
			description = solcFile.getOriginalFilename();
		}

		// save file info db
		saveSolcInfo(fileName, description, fileSize);

		// save file
		String filePath = String.format(SOLC_FILE_PATH, fileName);
		File fileTarget = new File(filePath);
		// check parent path
		if (!fileTarget.getParentFile().exists()){
			fileTarget.getParentFile().mkdir();
		}
		try {
			solcFile.transferTo(fileTarget);
			log.info("saveSolcFile success, file name:{}", fileName);
		} catch (IOException e) {
			log.error("saveSolcFile write to file, fileName:{},error:[]", fileName, e);
			throw new FrontException(ConstantCode.SAVE_SOLC_FILE_ERROR.getCode(),
					e.getMessage());
		}

	}

	private void saveSolcInfo(String fileName, String description, Long fileSize) {
		log.info("start saveSolcInfo");
		SolcInfo solcInfo = new SolcInfo();
		solcInfo.setSolcName(fileName);
		solcInfo.setDescription(description);
		solcInfo.setFileSize(Math.toIntExact(fileSize));
		solcInfo.setCreateTime(LocalDateTime.now());
		solcRepository.save(solcInfo);
	}

	public RspDownload getSolcFile(String fileName) {
		try {
			String filePath = String.format(SOLC_FILE_PATH, fileName);
			File file = new File(filePath);
			InputStream targetStream = new FileInputStream(file);
			return new RspDownload(fileName, targetStream);
		} catch (FileNotFoundException e) {
			log.error("getSolcFile: file not found:{}", fileName);
			throw new FrontException(ConstantCode.READ_SOLC_FILE_ERROR);
		}
	}

	public List<SolcInfo> getAllSolcInfo() {
		return solcRepository.findAll();
	}
}
