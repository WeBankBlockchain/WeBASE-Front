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
import com.webank.webase.front.solc.entity.RspDownload;
import com.webank.webase.front.solc.entity.SolcInfo;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SolcService {

	public static final String SOLC_DIR_PATH = "solcjs";
	private static final String SOLC_JS_SUFFIX = ".js";

	@Autowired
	private SolcRepository solcRepository;

	@Transactional
	public void saveSolcFile(String fileNameParam, MultipartFile solcFileParam, String fileDesc) throws IOException {
		// format filename end with js
		String fileName = formatFileName(fileNameParam);
		Long fileSize = solcFileParam.getSize();
		if (StringUtils.isBlank(fileDesc)) {
			fileDesc = solcFileParam.getOriginalFilename();
		}
		// check name and md5 not repeat
		checkSolcInfoNotExist(fileName);
		String md5 = DigestUtils.md5Hex(solcFileParam.getInputStream());
		checkSolcMd5NotExist(md5);
		// save file info db
		saveSolcInfo(fileName, fileDesc, fileSize, md5);

		// get solcjs dir and save file
		File solcDir = getSolcDir();
		try {
			File newFile = new File(solcDir.getAbsolutePath() + File.separator + fileName);
			solcFileParam.transferTo(newFile);
			log.info("saveSolcFile success, file name:{}", fileName);
		} catch (IOException e) {
			log.error("saveSolcFile write to file, fileName:{},error:[]", fileName, e);
			throw new FrontException(ConstantCode.SAVE_SOLC_FILE_ERROR.getCode(),
					e.getMessage());
		}
	}

	/**
	 * check file's md5 not exist
	 * @param md5
	 */
	private void checkSolcMd5NotExist(String md5) {
		SolcInfo checkExist = solcRepository.findByMd5(md5);
		if (Objects.nonNull(checkExist)) {
			throw new FrontException(ConstantCode.PARAM_FAIL_FILE_NAME_EXISTS);
		}
	}

	/**
	 * if exist, throw exception
	 * @param fileName
	 */
	private void checkSolcInfoNotExist(String fileName) {
		SolcInfo checkExist = solcRepository.findBySolcName(fileName);
		if (Objects.nonNull(checkExist)) {
			throw new FrontException(ConstantCode.PARAM_FAIL_FILE_NAME_EXISTS);
		}
	}

	/**
	 * if not exist, throw exception
	 * @param fileName
	 */
	private void checkSolcInfoExist(String fileName) {
		SolcInfo checkExist = solcRepository.findBySolcName(fileName);
		if (Objects.isNull(checkExist)) {
			throw new FrontException(ConstantCode.PARAM_FAIL_FILE_NAME_NOT_EXISTS);
		}
	}


	private void saveSolcInfo(String fileName, String description, Long fileSize, String md5) {
		log.info("start saveSolcInfo fileName:{}", fileName);
		SolcInfo solcInfo = new SolcInfo();
		solcInfo.setSolcName(fileName);
		solcInfo.setDescription(description);
		solcInfo.setMd5(md5);
		solcInfo.setFileSize(Math.toIntExact(fileSize));
		solcInfo.setCreateTime(LocalDateTime.now());
		solcRepository.save(solcInfo);
	}

	private String formatFileName(String fileName) {
		return fileName.endsWith(SOLC_JS_SUFFIX) ? fileName : (fileName + SOLC_JS_SUFFIX);
	}
	/**
	 * get solcjs dir's path
	 * @return File, file instance of dir
	 */
	private File getSolcDir() {

		File fileDir = new File(SOLC_DIR_PATH);
		// check parent path
		if(!fileDir.exists()){
			// 递归生成文件夹
			fileDir.mkdirs();
		}
		return fileDir;
	}

	/**
	 * download file
	 * @param fileNameParam
	 * @return
	 */
	public RspDownload getSolcFile(String fileNameParam) {
		// format filename end with js
		String fileName = formatFileName(fileNameParam);
		checkSolcInfoExist(fileName);
		File solcDir = getSolcDir();
		try {
			String solcLocate = solcDir.getAbsolutePath() + File.separator + fileName;
			File file = new File(solcLocate);
			InputStream targetStream = Files.newInputStream(Paths.get(file.getPath()));
			return new RspDownload(fileName, targetStream);
		} catch (FileNotFoundException e) {
			log.error("getSolcFile: file not found:{}, e:{}", fileName, e.getMessage());
			throw new FrontException(ConstantCode.READ_SOLC_FILE_ERROR);
		} catch (IOException e) {
			log.error("getSolcFile: file not found:{}", e.getMessage());
			throw new FrontException(ConstantCode.READ_SOLC_FILE_ERROR);
		}
	}

	public List<SolcInfo> getAllSolcInfo() {
		return solcRepository.findAll();
	}

	@Transactional
	public boolean deleteFile(Integer solcId) {
		SolcInfo solcInfo = solcRepository.findOne(solcId);
		String fileName = solcInfo.getSolcName();
		File solcDir = getSolcDir();
		String solcLocate = solcDir.getAbsolutePath() + File.separator + fileName;
		File file = new File(solcLocate);
		removeSolcInfo(solcId);
		if (!file.exists()) {
			throw new FrontException(ConstantCode.FILE_IS_NOT_EXIST);
		}
		return file.delete();
	}

	private void removeSolcInfo(Integer solcId) {
		SolcInfo checkExist = solcRepository.findOne(solcId);
		if (Objects.isNull(checkExist)) {
			throw new FrontException(ConstantCode.FILE_IS_NOT_EXIST);
		}
		solcRepository.delete(solcId);
	}
}
